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
import javax.swing.text.Document;
import jp.sfjp.jindolf.dxchg.TextPopup;
import jp.sfjp.jindolf.util.Monodizer;

/**
 * スクロールバー付きログ表示パネル。
 *
 * <p>垂直スクロールバーは自動的に最下部へトラックする。
 */
@SuppressWarnings("serial")
public final class LogPanel extends JScrollPane {

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

        return;
    }

    /**
     * ロギングハンドラを返す。
     * @return ロギングハンドラ
     */
    public Handler getHandler(){
        return this.handler;
    }

}
