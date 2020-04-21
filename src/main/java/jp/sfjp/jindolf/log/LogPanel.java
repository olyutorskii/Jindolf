/*
 * Log panel
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.log;

import io.github.olyutorskii.quetexj.MvcFacade;
import io.github.olyutorskii.quetexj.SwingLogHandler;
import java.util.logging.Handler;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import jp.sfjp.jindolf.dxchg.TextPopup;
import jp.sfjp.jindolf.util.Monodizer;

/**
 * スクロールバー付きログ表示パネル。
 *
 * <p>垂直スクロールバーは自動的に最下部へトラックする。
 */
@SuppressWarnings("serial")
public final class LogPanel extends JScrollPane {

    private static final Document DOC_EMPTY = new PlainDocument();


    private final JTextArea textarea;
    private final Document document;
    private final Handler handler;


    /**
     * コンストラクタ。
     *
     * @param facade MvcFacade
     */
    public LogPanel(MvcFacade facade){
        super();

        this.textarea = facade.getTextArea();
        this.document = facade.getDocument();

        if(LogUtils.hasLoggingPermission()){
            this.handler = new SwingLogHandler(this.document);
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

        JScrollBar vbar = getVerticalScrollBar();
        BoundedRangeModel rangeModel = facade.getVerticalBoundedRangeModel();
        vbar.setModel(rangeModel);

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
     * モデルとビューを連携させる。
     */
    private void attachModel(){
        if(this.textarea.getDocument() != this.document){
            this.textarea.setDocument(this.document);
        }
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

}
