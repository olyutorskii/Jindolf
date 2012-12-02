/*
 * Log panel
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.log;

import java.awt.Adjustable;
import java.awt.EventQueue;
import java.util.logging.Handler;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import jp.sfjp.jindolf.dxchg.TextPopup;
import jp.sfjp.jindolf.util.Monodizer;

/**
 * スクロールバー付きログ表示パネル。
 * 垂直スクロールバーは自動的に最下部へトラックする。
 */
@SuppressWarnings("serial")
public class LogPanel extends JScrollPane {

    private static final Document DOC_EMPTY = new PlainDocument();


    private final Document document = new PlainDocument();
    private final Handler handler;

    private final JTextArea textarea = new JTextArea();


    /**
     * コンストラクタ。
     */
    public LogPanel(){
        super();

        if(LogUtils.hasLoggingPermission()){
            this.handler = new SwingDocHandler(this.document);
        }else{
            this.handler = null;
        }

        this.textarea.setDocument(DOC_EMPTY);
        this.textarea.setEditable(false);
        this.textarea.setLineWrap(true);
        Monodizer.monodize(this.textarea);

        Border border = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        this.textarea.setBorder(border);

        JPopupMenu popup = new TextPopup();
        this.textarea.setComponentPopupMenu(popup);

        setViewportView(this.textarea);

        DocumentListener docListener = new DocWatcher();
        this.document.addDocumentListener(docListener);

        AncestorListener ancestorListener = new AncestorWatcher();
        addAncestorListener(ancestorListener);

        return;
    }

    /**
     * ロギングハンドラを返す。
     * @return ロギングハンドラ
     */
    public Handler getHandler(){
        return this.handler;
    }

    /**
     * 垂直スクロールバーをドキュメント下端に設定し、
     * ログの最新部を表示する。
     * 不可視状態なら何もしない。
     */
    private void showLastPos(){
        if(this.textarea.getDocument() != this.document) return;

        final Adjustable yPos = getVerticalScrollBar();
        EventQueue.invokeLater(new Runnable(){
            @Override
            public void run(){
                yPos.setValue(Integer.MAX_VALUE);
                return;
            }
        });

        return;
    }

    /**
     * モデルとビューを連携させる。
     * スクロール位置は末端に。
     */
    private void attachModel(){
        if(this.textarea.getDocument() != this.document){
            this.textarea.setDocument(this.document);
        }
        showLastPos();
        return;
    }

    /**
     * モデルとビューを切り離す。
     */
    private void detachModel(){
        if(this.textarea.getDocument() == DOC_EMPTY) return;
        this.textarea.setDocument(DOC_EMPTY);
        return;
    }

    /**
     * ログ内容をクリアする。
     */
    public void clearLog(){
        try{
            int docLength = this.document.getLength();
            this.document.remove(0, docLength);
        }catch(BadLocationException e){
            assert false;
        }
        return;
    }


    /**
     * 画面更新が必要な状態か監視し、必要に応じてモデルとビューを切り離す。
     */
    private final class AncestorWatcher implements AncestorListener{

        /**
         * コンストラクタ。
         */
        AncestorWatcher(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * @param event {@inheritDoc}
         */
        @Override
        public void ancestorAdded(AncestorEvent event){
            attachModel();
            return;
        }

        /**
         * {@inheritDoc}
         * @param event {@inheritDoc}
         */
        @Override
        public void ancestorRemoved(AncestorEvent event){
            detachModel();
            return;
        }

        /**
         * {@inheritDoc}
         * @param event {@inheritDoc}
         */
        @Override
        public void ancestorMoved(AncestorEvent event){
            return;
        }

    }


    /**
     * ドキュメント操作を監視し、スクロールバーを更新する。
     */
    private final class DocWatcher implements DocumentListener{

        /**
         * コンストラクタ。
         */
        DocWatcher(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * @param event {@inheritDoc}
         */
        @Override
        public void changedUpdate(DocumentEvent event){
            showLastPos();
            return;
        }

        /**
         * {@inheritDoc}
         * @param event {@inheritDoc}
         */
        @Override
        public void insertUpdate(DocumentEvent event){
            showLastPos();
            return;
        }

        /**
         * {@inheritDoc}
         * @param event {@inheritDoc}
         */
        @Override
        public void removeUpdate(DocumentEvent event){
            showLastPos();
            return;
        }

    }

}
