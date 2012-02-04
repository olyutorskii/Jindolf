/*
 * lock file warning dialog
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import jp.sfjp.jindolf.config.FileUtils;
import jp.sfjp.jindolf.config.InterVMLock;

/**
 * ロックエラー用ダイアログ。
 * <ul>
 * <li>強制解除
 * <li>リトライ
 * <li>設定ディレクトリを無視
 * <li>起動中止
 * </ul>
 * の選択を利用者に求める。
 */
@SuppressWarnings("serial")
public class LockErrorPane extends JOptionPane implements ActionListener{

    private final InterVMLock lock;

    private final JRadioButton continueButton =
            new JRadioButton("設定ディレクトリを使わずに起動を続行");
    private final JRadioButton retryButton =
            new JRadioButton("再度ロック取得を試す");
    private final JRadioButton forceButton =
            new JRadioButton(
            "<html>"
            + "ロックを強制解除<br>"
            + " (※他のJindolfと設定ファイル書き込みが衝突するかも…)"
            + "</html>");

    private final JButton okButton = new JButton("OK");
    private final JButton abortButton = new JButton("起動中止");

    private boolean aborted = false;

    /**
     * コンストラクタ。
     * @param lock 失敗したロック
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public LockErrorPane(InterVMLock lock){
        super();

        this.lock = lock;

        String htmlMessage =
                "<html>"
                + "設定ディレクトリのロックファイル<br>"
                + "<center>[&nbsp;"
                + FileUtils.getHtmledFileName(this.lock.getLockFile())
                + "&nbsp;]</center>"
                + "<br>"
                + "のロックに失敗しました。<br>"
                + "考えられる原因としては、<br>"
                + "<ul>"
                + "<li>前回起動したJindolfの終了が正しく行われなかった"
                + "<li>今どこかで他のJindolfが動いている"
                + "</ul>"
                + "などが考えられます。<br>"
                + "<hr>"
                + "</html>";

        ButtonGroup bgrp = new ButtonGroup();
        bgrp.add(this.continueButton);
        bgrp.add(this.retryButton);
        bgrp.add(this.forceButton);
        this.continueButton.setSelected(true);

        Object[] msg = {
            htmlMessage,
            this.continueButton,
            this.retryButton,
            this.forceButton,
        };
        setMessage(msg);

        Object[] opts = {
            this.okButton,
            this.abortButton,
        };
        setOptions(opts);

        setMessageType(JOptionPane.ERROR_MESSAGE);

        this.okButton   .addActionListener(this);
        this.abortButton.addActionListener(this);

        return;
    }

    /**
     * 「設定ディレクトリを無視して続行」が選択されたか判定する。
     * @return 「無視して続行」が選択されていればtrue
     */
    public boolean isRadioContinue(){
        return this.continueButton.isSelected();
    }

    /**
     * 「リトライ」が選択されたか判定する。
     * @return 「リトライ」が選択されていればtrue
     */
    public boolean isRadioRetry(){
        return this.retryButton.isSelected();
    }

    /**
     * 「強制解除」が選択されたか判定する。
     * @return 「強制解除」が選択されていればtrue
     */
    public boolean isRadioForce(){
        return this.forceButton.isSelected();
    }

    /**
     * 「起動中止」が選択されたか判定する。
     * @return 「起動中止」が押されていたならtrue
     */
    public boolean isAborted(){
        return this.aborted;
    }

    /**
     * {@inheritDoc}
     * @param parentComponent {@inheritDoc}
     * @param title {@inheritDoc}
     * @return {@inheritDoc}
     * @throws HeadlessException {@inheritDoc}
     */
    @Override
    public JDialog createDialog(Component parentComponent,
                                    String title)
            throws HeadlessException{
        final JDialog dialog =
                super.createDialog(parentComponent, title);

        ActionListener listener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                dialog.setVisible(false);
                return;
            }
        };

        this.okButton   .addActionListener(listener);
        this.abortButton.addActionListener(listener);

        return dialog;
    }

    /**
     * ボタン押下を受信する。
     * @param event イベント
     */
    public void actionPerformed(ActionEvent event){
        Object source = event.getSource();
        if(source == this.okButton) this.aborted = false;
        else                        this.aborted = true;
        return;
    }

}
