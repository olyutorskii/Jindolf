/*
 * 原稿作成支援エディタ
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.NavigationFilter;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;

/**
 * 原稿作成支援エディタ。
 * 文字数行数管理などを行う。
 * 200文字もしくは10行まで入力可能。
 * ※ 10回目に出現する改行文字は許される。
 * ※ 2010-11-27以降、G国では5行制限が10行に緩和された。
 */
@SuppressWarnings("serial")
public class TalkEditor
        extends JPanel
        implements DocumentListener {

    private static final int MAX_CHARS = 200;
    private static final int MAX_LINES = 10;

    private static final Color COLOR_ACTIVATED = Color.GRAY;

    /**
     * 指定された文字列の指定された位置から、
     * 最大何文字まで1発言におさめる事ができるか判定する。
     * @param source 検査対象
     * @param start 検査開始位置
     * @return 1発言に納めていい長さ。
     */
    public static int choplimit(CharSequence source, int start){
        int length = source.length();
        if(start >= length) return 0;

        int chars = 0;
        int lines = 0;

        for(int pos = start; pos < length; pos++){
            chars++;
            if(chars >= MAX_CHARS) break;
            char ch = source.charAt(pos);
            if(ch == '\n'){
                lines++;
                if(lines >= MAX_LINES) break;
            }
        }

        return chars;
    }

    private final PlainDocument document = new PlainDocument();

    private int sequenceNumber;

    private boolean isActive = false;

    private final JLabel seqCount = new JLabel();
    private final JLabel talkStat = new JLabel();
    private final TextEditor textEditor = new TextEditor();

    private Font textFont;

    /**
     * コンストラクタ。
     * 通し番号は0が指定される。
     */
    public TalkEditor(){
        this(0);
        return;
    }

    /**
     * コンストラクタ。
     * @param seqNumber 通し番号
     */
    private TalkEditor(int seqNumber){
        super();

        setOpaque(true);

        this.document.addDocumentListener(this);

        this.seqCount.setForeground(Color.WHITE);
        this.talkStat.setForeground(Color.WHITE);
        this.seqCount.setOpaque(false);
        this.talkStat.setOpaque(false);

        this.textEditor.setMargin(new Insets(3, 3, 3, 3));
        this.textEditor.setDocument(this.document);

        JPopupMenu popup = new TextPopup();
        this.textEditor.setComponentPopupMenu(popup);

        this.textFont = this.textEditor.getFont();

        setSequenceNumber(seqNumber);
        updateStat();
        setActive(false);

        design();

        return;
    }

    /**
     * レイアウトを行う。
     */
    private void design(){
        LayoutManager layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        setLayout(layout);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 1, 3);
        add(this.seqCount, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        JComponent decorated =
                BalloonBorder.decorateTransparentBorder(this.textEditor);
        add(decorated, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTHEAST;
        constraints.insets = new Insets(0, 0, 0, 3);
        add(this.talkStat, constraints);

        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        setBorder(border);

        return;
    }

    /**
     * テキスト編集用フォントを指定する。
     * @param textFont フォント
     */
    public void setTextFont(Font textFont){
        this.textFont = textFont;
        this.textEditor.setFont(this.textFont);
        this.textEditor.repaint();
        revalidate();
        return;
    }

    /**
     * テキスト編集用フォントを取得する。
     * @return フォント
     */
    public Font getTextFont(){
        return this.textFont;
    }

    /**
     * テキストコンポーネントにNavigationFilterを設定する。
     * @param navigator ナビゲーションフィルタ
     */
    public void setNavigationFilter(NavigationFilter navigator){
        this.textEditor.setNavigationFilter(navigator);
        return;
    }

    /**
     * 通し番号を取得する。
     * @return 通し番号
     */
    public int getSequenceNumber(){
        return this.sequenceNumber;
    }

    /**
     * 通し番号を設定する。
     * @param seqNumber 通し番号
     */
    public void setSequenceNumber(int seqNumber){
        this.sequenceNumber = seqNumber;
        String seqText = "=== #" + this.sequenceNumber + " ===";
        this.seqCount.setText(seqText);
        return;
    }

    /**
     * Documentを取得する。
     * @return 管理中のDocument
     */
    public Document getDocument(){
        return this.document;
    }

    /**
     * 現在のエディタの文字列内容を取得する。
     * @return 文字列内容
     */
    public String getText(){
        int length = this.document.getLength();
        String result = "";
        try{
            result = this.document.getText(0, length);
        }catch(BadLocationException e){
            assert false;
        }
        return result;
    }

    /**
     * 現在のエディタの文字列長を取得する。
     * @return 文字列長
     */
    public int getTextLength(){
        return this.document.getLength();
    }

    /**
     * 現在の行数を取得する。
     * ※ 改行文字の総数より一つ多い場合もある。
     * @return 行数
     */
    private int getTextLines(){
        int lines = 0;

        Segment segment = new Segment();
        segment.setPartialReturn(true);

        boolean hasLineContents = false;
        int pos = 0;
        int remain = getTextLength();
        while(remain > 0){
            try{
                this.document.getText(pos, remain, segment);
            }catch(BadLocationException e){
                assert false;
            }

            for(;;){
                char ch = segment.current();
                if(ch == Segment.DONE) break;

                if(ch == '\n'){
                    if( ! hasLineContents ){
                        lines++;
                    }
                    hasLineContents = false;
                }else if( ! hasLineContents ){
                    hasLineContents = true;
                    lines++;
                }

                segment.next();
            }

            pos    += segment.count;
            remain -= segment.count;
        }

        return lines;
    }

    /**
     * エディタ先頭に文字列を挿入する。
     * @param text 挿入文字列
     */
    public void appendHead(CharSequence text){
        if(text == null) return;
        if(text.length() <= 0) return;

        try{
            this.document.insertString(0, text.toString(), null);
        }catch(BadLocationException e){
            assert false;
        }

        return;
    }

    /**
     * エディタ末尾に文字列を追加する。
     * @param text 追加文字列
     */
    public void appendTail(CharSequence text){
        if(text == null) return;
        if(text.length() <= 0) return;

        int offset = getTextLength();
        try{
            this.document.insertString(offset, text.toString(), null);
        }catch(BadLocationException e){
            assert false;
        }

        return;
    }

    /**
     * エディタ先頭から文字列を削除する。
     * @param chopLength 削除文字数
     */
    public void chopHead(int chopLength){
        if(chopLength <= 0) return;

        int modLength;
        int textLength = getTextLength();
        if(chopLength > textLength) modLength = textLength;
        else                        modLength = chopLength;

        try{
            this.document.remove(0, modLength);
        }catch(BadLocationException e){
            assert false;
        }

        return;
    }

    /**
     * テキストを空にする。
     */
    public void clearText(){
        int textLength = this.document.getLength();
        try{
            this.document.remove(0, textLength);
        }catch(BadLocationException e){
            assert false;
        }
        return;
    }

    /**
     * アクティブ状態を指定する。
     * アクティブ状態の場合、背景色が変わる。
     * @param isActiveArg trueならアクティブ状態
     */
    public void setActive(boolean isActiveArg){
        this.isActive = isActiveArg;

        if(this.isActive){
            setOpaque(true);
            setBackground(COLOR_ACTIVATED);
            Dimension size = getSize();
            Rectangle bounds = new Rectangle(size);
            scrollRectToVisible(bounds);
            this.textEditor.scrollCaretToVisible();
        }else{
            setOpaque(false);
        }

        repaint();

        return;
    }

    /**
     * アクティブ状態を取得する。
     * @return アクティブ状態ならtrue
     */
    public boolean isActive(){
        return this.isActive;
    }

    /**
     * エディタが現在IME操作中か判定する。
     * @return IME操作中ならtrue
     */
    public boolean onIMEoperation(){
        boolean result = this.textEditor.onIMEoperation();
        return result;
    }

    /**
     * エディタの現在のカーソル位置を取得する。
     * @return 0から始まるカーソル位置
     */
    public int getCaretPosition(){
        int caretPos = this.textEditor.getCaretPosition();
        return caretPos;
    }

    /**
     * エディタのカーソル位置を設定する。
     * @param pos 0から始まるカーソル位置
     * @throws java.lang.IllegalArgumentException 範囲外のカーソル位置指定
     */
    public void setCaretPosition(int pos) throws IllegalArgumentException{
        this.textEditor.setCaretPosition(pos);
        return;
    }

    /**
     * 集計情報表示(文字数、行数)を更新する。
     */
    private void updateStat(){
        if(onIMEoperation()) return;

        int charTotal = getTextLength();
        int lineNumber = getTextLines();

        StringBuilder statistics = new StringBuilder();
        statistics.append(charTotal)
                  .append("字 ")
                  .append(lineNumber)
                  .append("行");
        this.talkStat.setText(statistics.toString());

        return;
    }

    /**
     * このテキストエディタに収まらない末尾文章を切り出す。
     * @return 最小の末尾文書。余裕があり切り出す必要がなければnullを返す。
     */
    public String chopRest(){
        String text = getText();
        int textLength = getTextLength();
        int choppedlen = choplimit(text, 0);

        int restLength = textLength - choppedlen;
        if(restLength <= 0) return null;

        String rest = null;
        try{
            rest = this.document.getText(choppedlen, restLength);
            this.document.remove(choppedlen, restLength);
        }catch(BadLocationException e){
            assert false;
        }

        return rest;
    }

    /**
     * テキストエディタにフォーカスを設定する。
     * @return 絶対失敗する場合はfalse
     */
    public boolean requestEditorFocus(){
        boolean result = this.textEditor.requestFocusInWindow();
        return result;
    }

    /**
     * テキストエディタがフォーカスを保持しているか判定する。
     * @return フォーカスを保持していればtrue
     */
    public boolean hasEditorFocus(){
        boolean result = this.textEditor.hasFocus();
        return result;
    }

    /**
     * 子エディタのフォーカス監視リスナを登録する。
     * @param listener FocusListener
     */
    public void addTextFocusListener(FocusListener listener){
        this.textEditor.addFocusListener(listener);
        return;
    }

    /**
     * 子エディタからフォーカス監視リスナを外す。
     * @param listener FocusListener
     */
    public void removeTextFocusListener(FocusListener listener){
        this.textEditor.removeFocusListener(listener);
        return;
    }

    /**
     * {@inheritDoc}
     * 集計情報を更新する。
     * @param event {@inheritDoc}
     */
    // TODO いつ呼ばれるのか不明
    @Override
    public void changedUpdate(DocumentEvent event){
        updateStat();
        return;
    }

    /**
     * {@inheritDoc}
     * 集計情報を更新する。
     * @param event {@inheritDoc}
     */
    @Override
    public void insertUpdate(DocumentEvent event){
        updateStat();
        return;
    }

    /**
     * {@inheritDoc}
     * 集計情報を更新する。
     * @param event {@inheritDoc}
     */
    @Override
    public void removeUpdate(DocumentEvent event){
        updateStat();
        return;
    }

}
