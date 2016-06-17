/*
 * エディタ集合の操作
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.editor;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position.Bias;

/**
 * エディタ集合の操作。
 * ※ このクラスはすべてシングルスレッドモデルで作られている。
 */
@SuppressWarnings("serial")
public class EditArray extends JPanel
                       implements Scrollable,
                                  FocusListener {

    private static final int MAX_EDITORS = 50;

    private final List<TalkEditor> editorList = new ArrayList<>();
    private boolean onAdjusting = false;

    private final NavigationFilter keyNavigator = new CustomNavigation();
    private final DocumentListener documentListener = new DocWatcher();

    private TalkEditor activeEditor;

    private Font textFont;

    /**
     * コンストラクタ。
     */
    public EditArray(){
        super();

        setOpaque(false);

        LayoutManager layout = new GridBagLayout();
        setLayout(layout);

        TalkEditor firstEditor = incrementTalkEditor();
        setActiveEditor(firstEditor);

        return;
    }

    /**
     * 個別エディタの生成を行う。
     * @return エディタ
     */
    private TalkEditor createTalkEditor(){
        TalkEditor editor = new TalkEditor();
        editor.setNavigationFilter(this.keyNavigator);
        editor.addTextFocusListener(this);
        Document document = editor.getDocument();
        document.addDocumentListener(this.documentListener);

        if(this.textFont == null){
            this.textFont = editor.getTextFont();
        }else{
            editor.setTextFont(this.textFont);
        }

        return editor;
    }

    /**
     * エディタ集合を一つ増やす。
     * @return 増えたエディタ
     */
    private TalkEditor incrementTalkEditor(){
        TalkEditor editor = createTalkEditor();

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridheight = 1;

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHEAST;

        add(editor, constraints);

        this.editorList.add(editor);

        int sequenceNumber = this.editorList.size();
        editor.setSequenceNumber(sequenceNumber);

        return editor;
    }

    /**
     * 1から始まる通し番号指定でエディタを取得する。
     * 存在しない通し番号が指定された場合は新たにエディタが追加される。
     * @param sequenceNumber 通し番号
     * @return エディタ
     */
    private TalkEditor getTalkEditor(int sequenceNumber){
        while(this.editorList.size() < sequenceNumber){
            incrementTalkEditor();
        }

        TalkEditor result = this.editorList.get(sequenceNumber - 1);

        return result;
    }

    /**
     * 指定したエディタの次の通し番号を持つエディタを返す。
     * エディタがなければ追加される。
     * @param editor エディタ
     * @return 次のエディタ
     */
    private TalkEditor nextEditor(TalkEditor editor){
        int sequenceNumber = editor.getSequenceNumber();
        TalkEditor nextEditor = getTalkEditor(sequenceNumber + 1);
        return nextEditor;
    }

    /**
     * 指定したエディタの前の通し番号を持つエディタを返す。
     * @param editor エディタ
     * @return 前のエディタ。
     * 最初のエディタ(通し番号1)が指定されればnullを返す。
     */
    private TalkEditor prevEditor(TalkEditor editor){
        int sequenceNumber = editor.getSequenceNumber();
        if(sequenceNumber <= 1) return null;
        TalkEditor prevEditor = getTalkEditor(sequenceNumber - 1);
        return prevEditor;
    }

    /**
     * 指定したエディタがエディタ集合の最後のエディタか判定する。
     * @param editor エディタ
     * @return 最後のエディタならtrue
     */
    private boolean isLastEditor(TalkEditor editor){
        int seqNo = editor.getSequenceNumber();
        int size = this.editorList.size();
        if(seqNo >= size) return true;
        return false;
    }

    /**
     * Documentからその持ち主であるエディタを取得する。
     * @param document Documentインスタンス
     * @return 持ち主のエディタ。見つからなければnull。
     */
    private TalkEditor getEditorFromDocument(Document document){
        for(TalkEditor editor : this.editorList){
            if(editor.getDocument() == document) return editor;
        }
        return null;
    }

    /**
     * エディタ集合から任意のエディタを除く。
     * ただし最初のエディタは消去不可。
     * @param editor エディタ
     */
    private void removeEditor(TalkEditor editor){
        if(editor.getParent() != this) return;

        int seqNo = editor.getSequenceNumber();
        if(seqNo <= 1) return;
        TalkEditor prevEditor = prevEditor(editor);
        if(editor.isActive()){
            setActiveEditor(prevEditor);
        }
        if(editor.hasEditorFocus()){
            prevEditor.requestEditorFocus();
        }

        this.editorList.remove(seqNo - 1);

        editor.setNavigationFilter(null);
        editor.removeTextFocusListener(this);
        Document document = editor.getDocument();
        document.removeDocumentListener(this.documentListener);
        editor.clearText();

        remove(editor);
        revalidate();

        int renumber = 1;
        for(TalkEditor newEditor : this.editorList){
            newEditor.setSequenceNumber(renumber++);
        }

        return;
    }

    /**
     * エディタ間文字調整タスクをディスパッチスレッドとして事後投入する。
     * エディタ間文字調整タスクが実行中であれば何もしない。
     * きっかけとなったエディタ上でIME操作が確定していなければ何もしない。
     * @param triggerEvent ドキュメント変更イベント
     */
    private void detachAdjustTask(DocumentEvent triggerEvent){
        if(this.onAdjusting) return;

        Document document = triggerEvent.getDocument();
        final TalkEditor triggerEditor = getEditorFromDocument(document);
        if(triggerEditor.onIMEoperation()) return;

        this.onAdjusting = true;

        EventQueue.invokeLater(new Runnable(){
            @Override
            public void run(){
                try{
                    adjustTask(triggerEditor);
                }finally{
                    EditArray.this.onAdjusting = false;
                }
                return;
            }
        });

        return;
    }

    /**
     * エディタ間文字調整タスク本体。
     * @param triggerEditor タスク実行のきっかけとなったエディタ
     */
    private void adjustTask(TalkEditor triggerEditor){
        int initCaretPos = triggerEditor.getCaretPosition();

        TalkEditor newFocus = null;
        int newCaretPos = -1;

        TalkEditor current = triggerEditor;
        for(;;){
            TalkEditor next;

            if( ! isLastEditor(current) ){
                next = nextEditor(current);
                String nextContents = next.getText();
                int nextLength = nextContents.length();

                current.appendTail(nextContents);
                String rest = current.chopRest();
                int restLength;
                if(rest == null) restLength = 0;
                else             restLength = rest.length();

                int chopLength = nextLength - restLength;
                if(chopLength > 0){
                    next.chopHead(chopLength);
                }else if(chopLength < 0){
                    rest = rest.substring(0, -chopLength);
                    next.appendHead(rest);
                }else{
                    if(newFocus == null){
                        newFocus = current;
                        newCaretPos = initCaretPos;
                    }
                    break;
                }
            }else{
                String rest = current.chopRest();
                if(rest == null || this.editorList.size() >= MAX_EDITORS){
                    if(newFocus == null){
                        newFocus = current;
                        if(current.getTextLength() >= initCaretPos){
                            newCaretPos = initCaretPos;
                        }else{
                            newCaretPos = current.getTextLength();
                        }
                    }
                    break;
                }
                next = nextEditor(current);
                next.appendHead(rest);
            }

            if(newFocus == null){
                int currentLength = current.getTextLength();
                if(initCaretPos >= currentLength){
                    initCaretPos -= currentLength;
                }else{
                    newFocus = current;
                    newCaretPos = initCaretPos;
                }
            }

            current = next;
        }

        if(newFocus != null){
            newFocus.requestEditorFocus();
            newFocus.setCaretPosition(newCaretPos);
        }

        adjustEditorsTail();

        return;
    }

    /**
     * エディタ集合末尾の空エディタを切り詰める。
     * ただし最初のエディタ(通し番号1)は削除されない。
     * フォーカスを持つエディタが削除された場合は、
     * 削除されなかった最後のエディタにフォーカスが移る。
     */
    private void adjustEditorsTail(){
        int editorNum = this.editorList.size();
        if(editorNum <= 0) return;
        TalkEditor lastEditor = this.editorList.get(editorNum - 1);

        TalkEditor prevlostEditor = null;

        boolean lostFocusedEditor = false;

        for(;;){
            int textLength = lastEditor.getTextLength();
            int seqNo = lastEditor.getSequenceNumber();

            if(lostFocusedEditor){
                prevlostEditor = lastEditor;
            }

            if(textLength > 0) break;
            if(seqNo <= 1) break;

            if(lastEditor.hasEditorFocus()) lostFocusedEditor = true;
            removeEditor(lastEditor);

            lastEditor = prevEditor(lastEditor); // TODO ちょっと変
        }

        if(prevlostEditor != null){
            int textLength = prevlostEditor.getTextLength();
            prevlostEditor.requestEditorFocus();
            prevlostEditor.setCaretPosition(textLength);
        }

        return;
    }

    /**
     * フォーカスを持つエディタを取得する。
     * @return エディタ
     */
    public TalkEditor getFocusedTalkEditor(){
        for(TalkEditor editor : this.editorList){
            if(editor.hasEditorFocus()) return editor;
        }
        return null;
    }

    /**
     * フォーカスを持つエディタの次エディタがあればフォーカスを移し、
     * カレット位置を0にする。
     */
    // TODO エディタのスクロール位置調整が必要。
    public void forwardEditor(){
        TalkEditor editor = getFocusedTalkEditor();
        if(isLastEditor(editor)) return;
        TalkEditor next = nextEditor(editor);
        next.setCaretPosition(0);
        next.requestEditorFocus();
        return;
    }

    /**
     * フォーカスを持つエディタの前エディタがあればフォーカスを移し、
     * カレット位置を末尾に置く。
     */
    public void backwardEditor(){
        TalkEditor editor = getFocusedTalkEditor();
        TalkEditor prev = prevEditor(editor);
        if(prev == null) return;
        int length = prev.getTextLength();
        prev.setCaretPosition(length);
        prev.requestEditorFocus();
        return;
    }

    /**
     * 任意のエディタをアクティブにする。
     * 同時にアクティブなエディタは一つのみ。
     * @param editor アクティブにするエディタ
     */
    private void setActiveEditor(TalkEditor editor){
        if(this.activeEditor != null){
            this.activeEditor.setActive(false);
        }

        this.activeEditor = editor;

        if(this.activeEditor != null){
            this.activeEditor.setActive(true);
        }

        fireChangeActive();

        return;
    }

    /**
     * アクティブなエディタを返す。
     * @return アクティブなエディタ。
     */
    public TalkEditor getActiveEditor(){
        return this.activeEditor;
    }

    /**
     * 全発言を連結した文字列を返す。
     * @return 連結文字列
     */
    public CharSequence getAllText(){
        StringBuilder result = new StringBuilder();

        for(TalkEditor editor : this.editorList){
            String text = editor.getText();
            result.append(text);
        }

        return result;
    }

    /**
     * 先頭エディタの0文字目から字を詰め込む。
     * 2番目移行のエディタへはみ出すかもしれない。
     * @param seq 詰め込む文字列
     */
    public void setAllText(CharSequence seq){
        TalkEditor firstEditor = getTalkEditor(1);
        Document doc = firstEditor.getDocument();
        try{
            doc.insertString(0, seq.toString(), null);
        }catch(BadLocationException e){
            assert false;
        }
        return;
    }

    /**
     * 全エディタをクリアする。
     */
    public void clearAllEditor(){
        int editorNum = this.editorList.size();
        if(editorNum <= 0) return;

        TalkEditor lastEditor = this.editorList.get(editorNum - 1);
        for(;;){
            removeEditor(lastEditor);
            lastEditor = prevEditor(lastEditor);
            if(lastEditor == null) break;
        }

        TalkEditor firstEditor = getTalkEditor(1);
        firstEditor.clearText();
        setActiveEditor(firstEditor);

        return;
    }

    /**
     * テキスト編集用フォントを指定する。
     * @param textFont フォント
     */
    public void setTextFont(Font textFont){
        this.textFont = textFont;
        for(TalkEditor editor : this.editorList){
            editor.setTextFont(this.textFont);
            editor.repaint();
        }
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
     * アクティブエディタ変更通知用リスナの登録。
     * @param listener リスナ
     */
    public void addChangeListener(ChangeListener listener){
        this.listenerList.add(ChangeListener.class, listener);
        return;
    }

    /**
     * アクティブエディタ変更通知用リスナの削除。
     * @param listener リスナ
     */
    public void removeChangeListener(ChangeListener listener){
        this.listenerList.remove(ChangeListener.class, listener);
        return;
    }

    /**
     * アクティブエディタ変更通知を行う。
     */
    private void fireChangeActive(){
        ChangeEvent event = new ChangeEvent(this);

        ChangeListener[] listeners =
                this.listenerList.getListeners(ChangeListener.class);
        for(ChangeListener listener : listeners){
            listener.stateChanged(event);
        }

        return;
    }

    /**
     * {@inheritDoc}
     * エディタのフォーカス取得とともにアクティブ状態にする。
     * @param event {@inheritDoc}
     */
    @Override
    public void focusGained(FocusEvent event){
        Object source = event.getSource();
        if( ! (source instanceof JTextComponent) ) return;
        JTextComponent textComp = (JTextComponent) source;

        Document document = textComp.getDocument();
        TalkEditor editor = getEditorFromDocument(document);

        setActiveEditor(editor);

        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void focusLost(FocusEvent event){
        // NOTHING
        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Dimension getPreferredScrollableViewportSize(){
        Dimension result = getPreferredSize();
        return result;
    }

    /**
     * {@inheritDoc}
     * 横スクロールバーを極力出さないようレイアウトでがんばる。
     * @return {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportWidth(){
        return true;
    }

    /**
     * {@inheritDoc}
     * 縦スクロールバーを出しても良いのでレイアウトでがんばらない。
     * @return {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportHeight(){
        return false;
    }

    /**
     *  {@inheritDoc}
     * @param visibleRect {@inheritDoc}
     * @param orientation {@inheritDoc}
     * @param direction {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,
                                           int direction ){
        if(orientation == SwingConstants.VERTICAL){
            return visibleRect.height;
        }
        return 10;
    }

    /**
     * {@inheritDoc}
     * @param visibleRect {@inheritDoc}
     * @param orientation {@inheritDoc}
     * @param direction {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction ){
        return 30; // TODO フォント高の1.5倍くらい？
    }

    /**
     * エディタ内のカーソル移動を監視するための、
     * カスタム化したナビゲーションフィルター。
     * 必要に応じてエディタ間カーソル移動を行う。
     */
    private class CustomNavigation extends NavigationFilter{

        /**
         * コンストラクタ。
         */
        public CustomNavigation(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * カーソル移動が行き詰まった場合、
         * 隣接するエディタ間でカーソル移動を行う。
         * @param text {@inheritDoc}
         * @param pos {@inheritDoc}
         * @param bias {@inheritDoc}
         * @param direction {@inheritDoc}
         * @param biasRet {@inheritDoc}
         * @return {@inheritDoc}
         * @throws javax.swing.text.BadLocationException {@inheritDoc}
         */
        @Override
        public int getNextVisualPositionFrom(JTextComponent text,
                                                 int pos,
                                                 Bias bias,
                                                 int direction,
                                                 Bias[] biasRet )
                                                 throws BadLocationException {
            int result = super.getNextVisualPositionFrom(text,
                                                         pos,
                                                         bias,
                                                         direction,
                                                         biasRet );
            if(result != pos) return result;

            switch(direction){
            case SwingConstants.WEST:
            case SwingConstants.NORTH:
                backwardEditor();
                break;
            case SwingConstants.EAST:
            case SwingConstants.SOUTH:
                forwardEditor();
                break;
            default:
                assert false;
            }

            return result;
        }
    }

    /**
     * エディタの内容変更を監視し、随時エディタ間調整を行う。
     */
    private class DocWatcher implements DocumentListener{

        /**
         * コンストラクタ。
         */
        public DocWatcher(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * @param event {@inheritDoc}
         */
        @Override
        public void changedUpdate(DocumentEvent event){
            detachAdjustTask(event);
            return;
        }

        /**
         * {@inheritDoc}
         * @param event {@inheritDoc}
         */
        @Override
        public void insertUpdate(DocumentEvent event){
            detachAdjustTask(event);
            return;
        }

        /**
         * {@inheritDoc}
         * @param event {@inheritDoc}
         */
        @Override
        public void removeUpdate(DocumentEvent event){
            detachAdjustTask(event);
            return;
        }
    }

}
