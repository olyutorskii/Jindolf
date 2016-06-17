/*
 * Account panel
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import jp.sfjp.jindolf.VerInfo;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.data.LandsModel;
import jp.sfjp.jindolf.dxchg.TextPopup;
import jp.sfjp.jindolf.net.ServerAccess;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sfjp.jindolf.util.Monodizer;
import jp.sourceforge.jindolf.corelib.LandState;

/**
 * ログインパネル。
 */
@SuppressWarnings("serial")
public class AccountPanel
        extends JDialog
        implements ActionListener, ItemListener{

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    private final Map<Land, String> landUserIDMap =
            new HashMap<>();
    private final Map<Land, char[]> landPasswordMap =
            new HashMap<>();

    private final JComboBox<Land> landBox = new JComboBox<>();
    private final JTextField idField = new JTextField(15);
    private final JPasswordField pwField = new JPasswordField(15);
    private final JButton loginButton = new JButton("ログイン");
    private final JButton logoutButton = new JButton("ログアウト");
    private final JButton closeButton = new JButton("閉じる");
    private final JTextArea status = new JTextArea();

    /**
     * アカウントパネルを生成。
     * @param owner フレームオーナー
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public AccountPanel(Frame owner){
        super(owner);
        setModal(true);

        GUIUtils.modifyWindowAttributes(this, true, false, true);

        this.landBox.setToolTipText("アカウント管理する国を選ぶ");
        this.idField.setToolTipText("IDを入力してください");
        this.pwField.setToolTipText("パスワードを入力してください");

        Monodizer.monodize(this.idField);
        Monodizer.monodize(this.pwField);

        this.idField.setMargin(new Insets(1, 4, 1, 4));
        this.pwField.setMargin(new Insets(1, 4, 1, 4));

        this.idField.setComponentPopupMenu(new TextPopup());

        this.landBox.setEditable(false);
        this.landBox.addItemListener(this);

        this.status.setEditable(false);
        this.status.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.status.setRows(2);
        this.status.setLineWrap(true);

        this.loginButton.addActionListener(this);
        this.logoutButton.addActionListener(this);
        this.closeButton.addActionListener(this);

        getRootPane().setDefaultButton(this.loginButton);

        Container content = getContentPane();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        content.setLayout(layout);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);

        JComponent accountPanel = createCredential();
        JComponent buttonPanel = createButtonPanel();

        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(accountPanel, constraints);

        Border border = BorderFactory.createTitledBorder("ログインステータス");
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(this.status, BorderLayout.CENTER);
        panel.setBorder(border);

        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        content.add(panel, constraints);

        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(new JSeparator(), constraints);

        content.add(buttonPanel, constraints);

        return;
    }

    /**
     * 認証パネルを生成する。
     * @return 認証パネル
     */
    private JComponent createCredential(){
        JPanel credential = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        credential.setLayout(layout);

        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.NONE;

        constraints.anchor = GridBagConstraints.EAST;
        credential.add(new JLabel("国名 :"), constraints);
        constraints.anchor = GridBagConstraints.WEST;
        credential.add(this.landBox, constraints);

        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        credential.add(new JLabel("ID :"), constraints);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        credential.add(this.idField, constraints);

        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        credential.add(new JLabel("パスワード :"), constraints);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        credential.add(this.pwField, constraints);

        return credential;
    }

    /**
     * ボタンパネルの作成。
     * @return ボタンパネル
     */
    private JComponent createButtonPanel(){
        JPanel buttonPanel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        buttonPanel.setLayout(layout);

        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;

        buttonPanel.add(this.loginButton, constraints);

        constraints.insets = new Insets(0, 5, 0, 0);
        buttonPanel.add(this.logoutButton, constraints);

        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(0, 15, 0, 0);
        buttonPanel.add(this.closeButton, constraints);

        return buttonPanel;
    }

    /**
     * 現在コンボボックスで選択中の国を返す。
     * @return 現在選択中のLand
     */
    private Land getSelectedLand(){
        Land land = (Land)( this.landBox.getSelectedItem() );
        return land;
    }

    /**
     * ACTIVEな最初の国がコンボボックスで既に選択されている状態にする。
     */
    private void preSelectActiveLand(){
        for(int index = 0; index < this.landBox.getItemCount(); index++){
            Object item = this.landBox.getItemAt(index);
            Land land = (Land) item;
            LandState state = land.getLandDef().getLandState();
            if(state == LandState.ACTIVE){
                this.landBox.setSelectedItem(land);
                return;
            }
        }
        return;
    }

    /**
     * 指定された国のユーザIDを返す。
     * @param land 国
     * @return ユーザID
     */
    private String getUserID(Land land){
        return this.landUserIDMap.get(land);
    }

    /**
     * 指定された国のパスワードを返す。
     * @param land 国
     * @return パスワード
     */
    private char[] getPassword(Land land){
        return this.landPasswordMap.get(land);
    }

    /**
     * ネットワークエラーを通知するモーダルダイアログを表示する。
     * OKボタンを押すまでこのメソッドは戻ってこない。
     * @param e ネットワークエラー
     */
    protected void showNetworkError(IOException e){
        LOGGER.log(Level.WARNING,
                "アカウント処理中にネットワークのトラブルが発生しました", e);

        Land land = getSelectedLand();
        ServerAccess server = land.getServerAccess();
        String message =
                land.getLandDef().getLandName()
                +"を運営するサーバとの間の通信で"
                +"何らかのトラブルが発生しました。\n"
                +"相手サーバのURLは [ " + server.getBaseURL() + " ] だよ。\n"
                +"Webブラウザでも遊べないか確認してみてね！\n";

        JOptionPane pane = new JOptionPane(message,
                                           JOptionPane.WARNING_MESSAGE,
                                           JOptionPane.DEFAULT_OPTION );

        String title = VerInfo.getFrameTitle("通信異常発生");
        JDialog dialog = pane.createDialog(this, title);

        dialog.pack();
        dialog.setVisible(true);
        dialog.dispose();

        return;
    }

    /**
     * アカウントエラーを通知するモーダルダイアログを表示する。
     * OKボタンを押すまでこのメソッドは戻ってこない。
     */
    protected void showIllegalAccountDialog(){
        Land land = getSelectedLand();
        String message =
                land.getLandDef().getLandName()
                +"へのログインに失敗しました。\n"
                +"ユーザ名とパスワードは本当に正しいかな？\n"
                +"あなたは本当に [ " + getUserID(land) + " ] さんかな？\n"
                +"WebブラウザによるID登録手続きは本当に完了してるかな？\n"
                +"Webブラウザでもログインできないか試してみて！\n"
                +"…ユーザ名やパスワードにある種の特殊文字を使っている人は"
                +"問題があるかも。";

        JOptionPane pane = new JOptionPane(message,
                                           JOptionPane.WARNING_MESSAGE,
                                           JOptionPane.DEFAULT_OPTION );

        String title = VerInfo.getFrameTitle("ログイン認証失敗");
        JDialog dialog = pane.createDialog(this, title);

        dialog.pack();
        dialog.setVisible(true);
        dialog.dispose();

        return;
    }

    /**
     * 入力されたアカウント情報を基に現在選択中の国へログインする。
     * @return ログインに成功すればtrueを返す。
     */
    protected boolean login(){
        Land land = getSelectedLand();
        ServerAccess server = land.getServerAccess();

        String id = this.idField.getText();
        char[] password = this.pwField.getPassword();
        this.landUserIDMap.put(land, id);
        this.landPasswordMap.put(land, password);

        boolean result = false;
        try{
            result = server.login(id, password);
        }catch(IOException e){
            showNetworkError(e);
            return false;
        }

        if( ! result ){
            showIllegalAccountDialog();
        }

        return result;
    }

    /**
     * 現在選択中の国からログアウトする。
     */
    protected void logout(){
        try{
            logoutInternal();
        }catch(IOException e){
            showNetworkError(e);
        }
        return;
    }

    /**
     * 現在選択中の国からログアウトする。
     * @throws java.io.IOException ネットワークエラー
     */
    protected void logoutInternal() throws IOException{
        Land land = getSelectedLand();
        ServerAccess server = land.getServerAccess();
        server.logout();
        return;
    }

    /**
     * 現在選択中の国のログイン状態に合わせてGUIを更新する。
     */
    private void updateGUI(){
        Land land = getSelectedLand();
        if(land == null) return;

        LandState state = land.getLandDef().getLandState();
        ServerAccess server = land.getServerAccess();
        boolean hasLoggedIn = server.hasLoggedIn();

        if(state != LandState.ACTIVE){
            this.status.setText(
                     "この国は既に募集を停止しました。\n"
                    +"ログインは無意味です" );
            this.idField.setEnabled(false);
            this.pwField.setEnabled(false);
            this.loginButton.setEnabled(false);
            this.logoutButton.setEnabled(false);
        }else if(hasLoggedIn){
            this.status.setText("ユーザ [ " + getUserID(land) + " ] として\n"
                          +"現在ログイン中です");
            this.idField.setEnabled(false);
            this.pwField.setEnabled(false);
            this.loginButton.setEnabled(false);
            this.logoutButton.setEnabled(true);
        }else{
            this.status.setText("現在ログインしていません");
            this.idField.setEnabled(true);
            this.pwField.setEnabled(true);
            this.loginButton.setEnabled(true);
            this.logoutButton.setEnabled(false);
        }

        return;
    }

    /**
     * 国情報を設定する。
     * @param model 国情報
     * @throws NullPointerException 引数がnull
     */
    public void setModel(LandsModel model) throws NullPointerException{
        if(model == null) throw new NullPointerException();

        this.landUserIDMap.clear();
        this.landPasswordMap.clear();
        this.landBox.removeAllItems();

        for(Land land : model.getLandList()){
            String userID = "";
            char[] password = {};
            this.landUserIDMap.put(land, userID);
            this.landPasswordMap.put(land, password);
            this.landBox.addItem(land);
        }

        preSelectActiveLand();
        updateGUI();

        return;
    }

    /**
     * {@inheritDoc}
     * ボタン操作のリスナ。
     * @param event イベント {@inheritDoc}
     */
    // TODO Return キー押下によるログインもサポートしたい
    @Override
    public void actionPerformed(ActionEvent event){
        Object source = event.getSource();

        if(source == this.closeButton){
            setVisible(false);
            dispose();
            return;
        }

        if(source == this.loginButton){
            login();
        }else if(source == this.logoutButton){
            logout();
        }

        updateGUI();

        return;
    }

    /**
     * {@inheritDoc}
     * コンボボックス操作のリスナ。
     * @param event イベント {@inheritDoc}
     */
    @Override
    public void itemStateChanged(ItemEvent event){
        Object source = event.getSource();
        if(source != this.landBox) return;

        Land land = (Land) event.getItem();
        String id;
        char[] password;

        switch(event.getStateChange()){
        case ItemEvent.SELECTED:
            id = getUserID(land);
            password = getPassword(land);
            this.idField.setText(id);
            this.pwField.setText(new String(password));
            updateGUI();
            break;
        case ItemEvent.DESELECTED:
            id = this.idField.getText();
            password = this.pwField.getPassword();
            this.landUserIDMap.put(land, id);
            this.landPasswordMap.put(land, password);
            break;
        default:
            assert false;
            return;
        }

        return;
    }

    // TODO IDかパスワードが空の場合はログインボタンを無効にしたい
}
