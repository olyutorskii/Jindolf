/*
 * 原稿作成支援用テキストコンポーネント
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.editor;

import java.awt.Rectangle;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.nio.CharBuffer;
import java.text.AttributedCharacterIterator;
import javax.swing.JTextArea;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.PlainDocument;

/**
 * 原稿作成支援用テキストコンポーネント。
 */
@SuppressWarnings("serial")
public class TextEditor extends JTextArea
        implements InputMethodListener {

    private static final int MAX_DOCUMENT = 10 * 1000;

    private final DocumentFilter documentFilter = new CustomFilter();

    private boolean onIMEoperation = false;

    /**
     * コンストラクタ。
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public TextEditor(){
        super();

        setLineWrap(true);
        setWrapStyleWord(false);

        Document document = new PlainDocument();
        setDocument(document);

        addInputMethodListener(this);

        return;
    }

    /**
     * エディタが現在IME操作中か判定する。
     * @return IME操作中ならtrue
     */
    public boolean onIMEoperation(){
        return this.onIMEoperation;
    }

    /**
     * 現在のカーソルが表示されるようスクロールエリアを操作する。
     */
    public void scrollCaretToVisible(){
        int caretPosition = getCaretPosition();

        Rectangle caretBounds;
        try{
            caretBounds = modelToView(caretPosition);
        }catch(BadLocationException e){
            assert false;
            return;
        }

        scrollRectToVisible(caretBounds);

        return;
    }

    /**
     * {@inheritDoc}
     * Document変更をフックしてフィルタを仕込む。
     * @param document {@inheritDoc}
     */
    @Override
    public final void setDocument(Document document){
        Document oldDocument = getDocument();
        if(oldDocument instanceof AbstractDocument){
            AbstractDocument abstractDocument =
                    (AbstractDocument) oldDocument;
            abstractDocument.setDocumentFilter(null);
        }

        super.setDocument(document);

        if(document instanceof AbstractDocument){
            AbstractDocument abstractDocument = (AbstractDocument) document;
            abstractDocument.setDocumentFilter(this.documentFilter);
        }

        return;
    }

    /**
     * {@inheritDoc}
     * このエディタ中の指定領域が表示されるようスクロールエリアを操作する。
     * キーボードフォーカスを保持しないときは無視。
     * @param rect {@inheritDoc}
     */
    @Override
    public void scrollRectToVisible(Rectangle rect){
        if( ! hasFocus() ) return;
        super.scrollRectToVisible(rect);
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void caretPositionChanged(InputMethodEvent event){
        // NOTHING
        return;
    }

    /**
     * {@inheritDoc}
     * このテキストエディタで現在IMEの変換中か否か判定する処理を含む。
     * @param event {@inheritDoc}
     */
    @Override
    public void inputMethodTextChanged(InputMethodEvent event){
        int committed = event.getCommittedCharacterCount();
        AttributedCharacterIterator aci = event.getText();
        if(aci == null){
            this.onIMEoperation = false;
            return;
        }
        int begin = aci.getBeginIndex();
        int end   = aci.getEndIndex();
        int span = end - begin;

        if(committed >= span) this.onIMEoperation = false;
        else                  this.onIMEoperation = true;

        return;
    }

    /**
     * 入力文字列に制限を加えるDocumentFilter。
     * \n,\f 以外の制御文字はタブも含め入力禁止。
     * U+FFFF はjava.textパッケージで特別扱いなのでこれも入力禁止。
     * ※ ただしIME操作中は制限なし。
     */
    private class CustomFilter extends DocumentFilter{

        /**
         * コンストラクタ。
         */
        public CustomFilter(){
            super();
            return;
        }

        /**
         * 入力禁止文字の判定。
         * @param ch 検査対象文字
         * @return 入力禁止ならfalse。ただしIME操作中は必ずtrue。
         */
        private boolean isValid(char ch){
            if(onIMEoperation()) return true;

            if(ch == '\n') return true;
            // if(ch == '\f') return true;

            if(ch == '\uffff')             return false;
            if(Character.isISOControl(ch)) return false;

            // if( ! CodeX0208.isValid(ch) ) return false;
            if(Character.isHighSurrogate(ch)) return false;
            if(Character.isLowSurrogate(ch) ) return false;

            return true;
        }

        /**
         * 与えられた文字列から入力禁止文字を除いた文字列に変換する。
         * @param input 検査対象文字列
         * @return 除去済み文字列
         */
        private String filter(CharSequence input){
            if(onIMEoperation()) return input.toString();

            int length = input.length();
            CharBuffer buf = CharBuffer.allocate(length);

            for(int pos = 0; pos < length; pos++){
                char ch = input.charAt(pos);
                if(ch == '\u2211') ch = '\u03a3'; // Σ変換
                if(ch == '\u00ac') ch = '\uffe2'; // ￢変換
                // if(ch ==  0x005c ) ch = '\u00a5';
                // バックスラッシュから円へ
                if(isValid(ch)) buf.append(ch);
            }

            buf.flip();
            return buf.toString();
        }

        /**
         * {@inheritDoc}
         * @param fb {@inheritDoc}
         * @param offset {@inheritDoc}
         * @param text {@inheritDoc}
         * @param attrs {@inheritDoc}
         * @throws javax.swing.text.BadLocationException {@inheritDoc}
         */
        @Override
        public void insertString(FilterBypass fb,
                                 int offset,
                                 String text,
                                 AttributeSet attrs)
                                 throws BadLocationException{
            String filtered = filter(text);

            if( ! onIMEoperation() ){
                Document document = fb.getDocument();
                int docLength = document.getLength();
                int rest = MAX_DOCUMENT - docLength;
                if(rest < 0){
                    return;
                }else if(rest < filtered.length()){
                    filtered = filtered.substring(0, rest);
                }
            }

            fb.insertString(offset, filtered, attrs);

            return;
        }

        /**
         *  {@inheritDoc}
         * @param fb {@inheritDoc}
         * @param offset {@inheritDoc}
         * @param length {@inheritDoc}
         * @param text {@inheritDoc}
         * @param attrs {@inheritDoc}
         * @throws javax.swing.text.BadLocationException {@inheritDoc}
         */
        @Override
        public void replace(FilterBypass fb,
                            int offset,
                            int length,
                            String text,
                            AttributeSet attrs)
                            throws BadLocationException{
            String filtered = filter(text);

            if( ! onIMEoperation() ){
                Document document = fb.getDocument();
                int docLength = document.getLength();
                docLength -= length;
                int rest = MAX_DOCUMENT - docLength;
                if(rest < 0){
                    return;
                }else if(rest < filtered.length()){
                    filtered = filtered.substring(0, rest);
                }
            }

            fb.replace(offset, length, filtered, attrs);

            return;
        }
    }

    // TODO 禁則チェック。20文字を超える長大なブレーク禁止文字列の出現の監視。
    // TODO 連続したホワイトスペースに対する警告。
    // TODO 先頭もしくは末尾のホワイトスペース出現に対する警告。
    // TODO 改行記号の表示
    // TODO 改発言記号の導入
}
