/*
 * Log frame
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.log;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Handler;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JSeparator;

/**
 * ログ表示ウィンドウ。
 */
@SuppressWarnings("serial")
public class LogFrame extends JDialog {

    private static final String CMD_CLOSELOG = "CMD_CLOSE_LOG";
    private static final String CMD_CLEARLOG = "CMD_CLEAR_LOG";


    private final LogPanel logPanel = new LogPanel();
    private final JButton clearButton = new JButton("クリア");
    private final JButton closeButton = new JButton("閉じる");


    /**
     * コンストラクタ。
     * @param owner フレームオーナー
     */
    public LogFrame(Frame owner){
        super(owner);

        design();

        this.clearButton.setActionCommand(CMD_CLEARLOG);
        this.closeButton.setActionCommand(CMD_CLOSELOG);

        ActionListener actionListener = new ActionWatcher();
        this.clearButton.addActionListener(actionListener);
        this.closeButton.addActionListener(actionListener);

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

        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = 1;
        content.add(this.clearButton, constraints);

        constraints.weightx = 0.0;
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

    /**
     * ログ内容をクリアする。
     */
    public void clearLog(){
        this.logPanel.clearLog();
        return;
    }


    /**
     * ボタン操作を監視する。
     */
    private final class ActionWatcher implements ActionListener{

        /**
         * コンストラクタ。
         */
        ActionWatcher(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * ボタン押下イベント処理。
         * @param event {@inheritDoc}
         */
        @Override
        public void actionPerformed(ActionEvent event){
            String cmd = event.getActionCommand();

            if     (CMD_CLEARLOG.equals(cmd)) clearLog();
            else if(CMD_CLOSELOG.equals(cmd)) setVisible(false);

            return;
        }

    }

}
