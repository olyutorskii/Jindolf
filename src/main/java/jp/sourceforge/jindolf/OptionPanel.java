/*
 * option panel
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

/**
 * オプション設定パネル。
 */
@SuppressWarnings("serial")
public class OptionPanel
        extends JDialog
        implements ActionListener, WindowListener{

    private static final String FRAMETITLE =
            "オプション設定 - " + Jindolf.TITLE;

    private final JTabbedPane tabPane = new JTabbedPane();

    private final FontChooser fontChooser;
    private final ProxyChooser proxyChooser;
    private final DialogPrefPanel dialogPrefPanel;

    private final JButton okButton     = new JButton("OK");
    private final JButton cancelButton = new JButton("キャンセル");

    private boolean isCanceled = false;

    /**
     * コンストラクタ。
     * @param owner フレームオーナ
     */
    public OptionPanel(Frame owner){
        super(owner, FRAMETITLE, true);

        GUIUtils.modifyWindowAttributes(this, true, false, true);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        this.fontChooser = new FontChooser(FontInfo.DEFAULT_FONTINFO);
        this.proxyChooser = new ProxyChooser();
        this.dialogPrefPanel = new DialogPrefPanel();

        this.tabPane.add("フォント", this.fontChooser);
        this.tabPane.add("プロクシ", this.proxyChooser);
        this.tabPane.add("発言表示", this.dialogPrefPanel);

        design(getContentPane());

        this.okButton    .addActionListener(this);
        this.cancelButton.addActionListener(this);

        return;
    }

    /**
     * レイアウトを行う。
     * @param content コンテナ
     */
    private void design(Container content){
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        content.setLayout(layout);

        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(5, 5, 5, 5);
        content.add(this.tabPane, constraints);

        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(new JSeparator(), constraints);

        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        content.add(this.okButton, constraints);

        constraints.weightx = 0.0;
        content.add(this.cancelButton, constraints);

        return;
    }

    /**
     * FontChooserを返す。
     * @return FontChooser
     */
    public FontChooser getFontChooser(){
        return this.fontChooser;
    }

    /**
     * ProxyChooserを返す。
     * @return ProxyChooser
     */
    public ProxyChooser getProxyChooser(){
        return this.proxyChooser;
    }

    /**
     * DialogPrefPanelを返す。
     * @return DialogPrefPanel
     */
    public DialogPrefPanel getDialogPrefPanel(){
        return this.dialogPrefPanel;
    }

    /**
     * ダイアログが閉じられた原因が「キャンセル」か否か判定する。
     * ウィンドウクローズ操作は「キャンセル」扱い。
     * @return 「キャンセル」ならtrue
     */
    public boolean isCanceled(){
        return this.isCanceled;
    }

    /**
     * OKボタン押下処理。
     * ダイアログを閉じる。
     */
    private void actionOk(){
        this.isCanceled = false;
        setVisible(false);
        dispose();
        return;
    }

    /**
     * キャンセルボタン押下処理。
     * ダイアログを閉じる。
     */
    private void actionCancel(){
        this.isCanceled = true;
        setVisible(false);
        dispose();
        return;
    }

    /**
     * ボタン押下イベント受信。
     * @param event イベント
     */
    public void actionPerformed(ActionEvent event){
        Object source = event.getSource();
        if     (source == this.okButton    ) actionOk();
        else if(source == this.cancelButton) actionCancel();
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowOpened(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * ダイアログを閉じる。
     * キャンセルボタン押下時と同じ。
     * @param event {@inheritDoc}
     */
    @Override
    public void windowClosing(WindowEvent event){
        actionCancel();
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowClosed(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowActivated(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowDeactivated(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowIconified(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowDeiconified(WindowEvent event){
        return;
    }

}
