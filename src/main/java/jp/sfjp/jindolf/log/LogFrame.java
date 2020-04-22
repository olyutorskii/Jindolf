/*
 * Log frame
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.log;

import io.github.olyutorskii.quetexj.HeightKeeper;
import io.github.olyutorskii.quetexj.MaxTracker;
import io.github.olyutorskii.quetexj.MvcFacade;
import io.github.olyutorskii.quetexj.SwingLogHandler;
import java.awt.Container;
import java.awt.Frame;
import java.util.logging.Handler;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.text.Document;
import jp.sfjp.jindolf.dxchg.TextPopup;
import jp.sfjp.jindolf.util.Monodizer;

/**
 * ログ表示ウィンドウ。
 */
@SuppressWarnings("serial")
public final class LogFrame extends JDialog {

    private static final int HEIGHT_LIMIT = 5000;
    private static final int HEIGHT_NEW   = 4000;

    private static final int AROUND_TEXT   = 3;
    private static final int AROUND_BUTTON = 5;


    private final MvcFacade facade;

    private final JScrollPane scrollPane;
    private final JButton clearButton;
    private final JButton closeButton;
    private final JCheckBox trackButton;

    private final Handler handler;


    /**
     * コンストラクタ。
     *
     * @param owner フレームオーナー
     */
    public LogFrame(Frame owner){
        super(owner);

        this.facade = new MvcFacade();

        this.scrollPane = buildScrollPane(this.facade);

        this.clearButton = new JButton();
        this.closeButton = new JButton();
        this.trackButton = new JCheckBox();

        setupButtons();

        MaxTracker tracker = this.facade.getMaxTracker();
        HeightKeeper keeper = this.facade.getHeightKeeper();

        tracker.setTrackingMode(true);
        keeper.setConditions(HEIGHT_LIMIT, HEIGHT_NEW);

        Handler logHandler = null;
        if(LogUtils.hasLoggingPermission()){
            Document document = this.facade.getDocument();
            logHandler = new SwingLogHandler(document);
        }
        this.handler = logHandler;

        setResizable(true);
        setLocationByPlatform(true);
        setModal(false);

        design();

        return;
    }


    /**
     * ログ用スクロール領域を生成する。
     *
     * @param facadeArg ファサード
     * @return スクロール領域
     */
    private static JScrollPane buildScrollPane(MvcFacade facadeArg){
        JScrollPane scrollPane = new JScrollPane();

        JScrollBar vbar = scrollPane.getVerticalScrollBar();
        BoundedRangeModel rangeModel =
                facadeArg.getVerticalBoundedRangeModel();
        vbar.setModel(rangeModel);

        JTextArea textArea = buildTextArea(facadeArg);
        scrollPane.setViewportView(textArea);

        return scrollPane;
    }

    /**
     * ログ用テキストエリアを生成する。
     *
     * @param facadeArg ファサード
     * @return テキストエリア
     */
    private static JTextArea buildTextArea(MvcFacade facadeArg){
        JTextArea textArea = facadeArg.getTextArea();

        textArea.setEditable(false);
        textArea.setLineWrap(true);
        Monodizer.monodize(textArea);

        Border border = BorderFactory.createEmptyBorder(
                AROUND_TEXT,
                AROUND_TEXT,
                AROUND_TEXT,
                AROUND_TEXT
        );
        textArea.setBorder(border);

        JPopupMenu popup = new TextPopup();
        textArea.setComponentPopupMenu(popup);

        return textArea;
    }


    /**
     * ボタンの各種設定。
     */
    private void setupButtons(){
        Action clearAction = this.facade.getClearAction();
        this.clearButton.setAction(clearAction);
        this.clearButton.setText("クリア");

        this.closeButton.addActionListener(event -> {
            if(event.getSource() == this.closeButton){
                setVisible(false);
            }
        });
        this.closeButton.setText("閉じる");

        JToggleButton.ToggleButtonModel toggleModel;
        toggleModel = this.facade.getTrackSwitchButtonModel();
        this.trackButton.setModel(toggleModel);
        this.trackButton.setText("末尾に追従");

        return;
    }

    /**
     * レイアウトデザインを行う。
     */
    private void design(){
        Box buttonPanel = Box.createHorizontalBox();

        buttonPanel.add(this.clearButton);
        buttonPanel.add(Box.createHorizontalStrut(AROUND_BUTTON));
        buttonPanel.add(this.trackButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(this.closeButton);

        Border border = BorderFactory.createEmptyBorder(
                AROUND_BUTTON,
                AROUND_BUTTON,
                AROUND_BUTTON,
                AROUND_BUTTON
        );
        buttonPanel.setBorder(border);

        Container content = getContentPane();
        BoxLayout layout = new BoxLayout(content, BoxLayout.Y_AXIS);
        content.setLayout(layout);

        content.add(this.scrollPane);
        content.add(buttonPanel);

        return;
    }

    /**
     * ロギングハンドラを返す。
     *
     * @return ロギングハンドラ
     */
    public Handler getHandler(){
        return this.handler;
    }

}
