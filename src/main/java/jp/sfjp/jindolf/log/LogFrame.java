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
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Handler;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;

/**
 * ログ表示ウィンドウ。
 */
@SuppressWarnings("serial")
public final class LogFrame extends JDialog {

    private static final String CMD_CLOSELOG = "CMD_CLOSE_LOG";


    private final MvcFacade facade;

    private final LogPanel logPanel;
    private final JButton clearButton;
    private final JButton closeButton;
    private final JCheckBox trackButton;


    /**
     * コンストラクタ。
     *
     * @param owner フレームオーナー
     */
    public LogFrame(Frame owner){
        super(owner);

        this.facade = new MvcFacade();

        this.logPanel = new LogPanel(this.facade);
        this.clearButton = new JButton();
        this.closeButton = new JButton();
        this.trackButton = new JCheckBox();

        design();

        Action clearAction = this.facade.getClearAction();
        this.clearButton.setAction(clearAction);
        this.clearButton.setText("クリア");

        this.closeButton.setActionCommand(CMD_CLOSELOG);
        this.closeButton.addActionListener(event -> {
            String cmd = event.getActionCommand();
            if(CMD_CLOSELOG.equals(cmd)){
                setVisible(false);
            }
        });
        this.closeButton.setText("閉じる");

        JToggleButton.ToggleButtonModel toggleModel;
        toggleModel = this.facade.getTrackSwitchButtonModel();
        this.trackButton.setModel(toggleModel);
        this.trackButton.setText("末尾に追従");

        MaxTracker tracker = this.facade.getMaxTracker();
        tracker.setTrackingMode(true);

        HeightKeeper keeper = this.facade.getHeightKeeper();
        keeper.setConditions(5000, 4000);

        setResizable(true);
        setLocationByPlatform(true);
        setModal(false);

        return;
    }

    /**
     * デザインを行う。
     */
    private void design(){
        Container content = getContentPane();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        content.setLayout(layout);

        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        content.add(this.logPanel, constraints);

        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        content.add(new JSeparator(), constraints);

        constraints.weightx = 0.0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = 1;
        content.add(this.clearButton, constraints);
        content.add(this.trackButton, constraints);

        constraints.weightx = 1.0;
        constraints.anchor = GridBagConstraints.EAST;
        content.add(this.closeButton, constraints);

        return;
    }

    /**
     * ロギングハンドラを返す。
     * @return ロギングハンドラ
     */
    public Handler getHandler(){
        return this.logPanel.getHandler();
    }

}
