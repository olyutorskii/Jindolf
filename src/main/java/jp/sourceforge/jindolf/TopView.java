/*
 * Top view
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 * 最上位ビュー。
 * メインアプリウィンドウのコンポーネントの親コンテナ。
 */
@SuppressWarnings("serial")
public class TopView extends JPanel{

    private static final String INITCARD = "INITCARD";
    private static final String LANDCARD = "LANDINFO";
    private static final String BROWSECARD = "BROWSER";

    private final JComponent cards;
    private final CardLayout cardLayout = new CardLayout();

    private final LandsTree landsTreeView = new LandsTree();

    private final LandInfoPanel landInfo = new LandInfoPanel();

    private final JTextField sysMessage = new JTextField();
    private final JProgressBar progressBar = new JProgressBar();

    private final TabBrowser tabBrowser = new TabBrowser();

    private JComponent browsePanel;

    /**
     * トップビューを生成する。
     */
    public TopView(){
        super();

        this.cards = createCards();
        JComponent split = createSplitPane(this.landsTreeView, this.cards);
        JComponent statusBar = createStatusBar();

        BorderLayout layout = new BorderLayout();
        setLayout(layout);
        add(split, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        return;
    }

    /**
     * カードパネルを生成する。
     * @return カードパネル
     */
    private JComponent createCards(){
        this.browsePanel = createBrowsePanel();

        JPanel panel = new JPanel();
        panel.setLayout(this.cardLayout);
        panel.add(INITCARD, createInitCard());
        panel.add(LANDCARD, createLandInfoCard());
        panel.add(BROWSECARD, this.browsePanel);

        return panel;
    }

    /**
     * 初期パネルを生成。
     * @return 初期パネル
     */
    private JComponent createInitCard(){
        JLabel initMessage = new JLabel("← 村を選択してください");

        StringBuilder acct = new StringBuilder();
        acct.append("※ 参加中の村がある人は<br></br>");
        acct.append("メニューの「アカウント管理」から<br></br>");
        acct.append("ログインしてください");
        acct.insert(0, "<center>").append("</center>");
        acct.insert(0, "<body>")  .append("</body>");
        acct.insert(0, "<html>")  .append("</html>");
        JLabel acctMessage = new JLabel(acct.toString());

        StringBuilder warn = new StringBuilder();
        warn.append("※ たまにはWebブラウザでアクセスして、");
        warn.append("<br></br>");
        warn.append("運営の動向を確かめようね！");
        warn.insert(0, "<center>").append("</center>");
        warn.insert(0, "<body>")  .append("</body>");
        warn.insert(0, "<html>")  .append("</html>");
        JLabel warnMessage = new JLabel(warn.toString());

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        panel.setLayout(layout);

        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = GridBagConstraints.REMAINDER;
        panel.add(initMessage, constraints);
        panel.add(acctMessage, constraints);
        panel.add(warnMessage, constraints);

        JScrollPane scrollPane = new JScrollPane(panel);

        return scrollPane;
    }

    /**
     * 国別情報を生成。
     * @return 国別情報
     */
    private JComponent createLandInfoCard(){
        this.landInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(this.landInfo);
        return scrollPane;
    }

    /**
     * 内部ブラウザを生成。
     * @return 内部ブラウザ
     */
    private JComponent createBrowsePanel(){
        JPanel panel = new JPanel();
        BorderLayout layout = new BorderLayout();
        panel.setLayout(layout);

        panel.add(this.tabBrowser, BorderLayout.CENTER);

        return panel;
    }

    /**
     * ブラウザ用ツールバーをセットする。
     * @param toolbar ツールバー
     */
    public void setBrowseToolBar(JToolBar toolbar){
        this.browsePanel.add(toolbar, BorderLayout.NORTH);
        return;
    }

    /**
     * SplitPaneを生成。
     * @param left 左コンポーネント
     * @param right 右コンポーネント
     * @return SplitPane
     */
    private JComponent createSplitPane(JComponent left, JComponent right){
        JSplitPane split = new JSplitPane();
        split.setLeftComponent(left);
        split.setRightComponent(right);
        split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        split.setContinuousLayout(false);
        split.setOneTouchExpandable(true);
        split.setDividerLocation(200);

        return split;
    }

    /**
     * ステータスバーを生成する。
     * @return ステータスバー
     */
    private JComponent createStatusBar(){
        this.sysMessage.setText(
                  Jindolf.TITLE + " " + Jindolf.VERSION
                + " を使ってくれてありがとう！" );
        this.sysMessage.setEditable(false);
        Border inside  = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
        Border outside = BorderFactory.createEmptyBorder(2, 5, 2, 2);
        Border border = new CompoundBorder(inside, outside);
        this.sysMessage.setBorder(border);

        this.progressBar.setIndeterminate(false);
        this.progressBar.setOrientation(SwingConstants.HORIZONTAL);
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(99);
        this.progressBar.setValue(0);

        JPanel statusBar = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        statusBar.setLayout(layout);

        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        statusBar.add(this.sysMessage, constraints);

        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(2, 2, 2, 2);
        statusBar.add(this.progressBar, constraints);

        return statusBar;
    }

    /**
     * 国村選択ツリービューを返す。
     * @return 国村選択ツリービュー
     */
    public JTree getTreeView(){
        return this.landsTreeView.getTreeView();
    }

    /**
     * タブビューを返す。
     * @return タブビュー
     */
    public TabBrowser getTabBrowser(){
        return this.tabBrowser;
    }

    /**
     * 村一覧ビューを返す。
     * @return 村一番ビュー
     */
    public LandsTree getLandsTree(){
        return this.landsTreeView;
    }

    /**
     * プログレスバーとカーソルの設定を行う。
     * @param busy trueならプログレスバーのアニメ開始&WAITカーソル。
     *              falseなら停止&通常カーソル。
     */
    public void setBusy(boolean busy){
        this.progressBar.setIndeterminate(busy);
        return;
    }

    /**
     * ステータスバーの更新。
     * @param message 更新文字列
     */
    public void updateSysMessage(String message){
        if(message == null) return;
        String text;
        if(message.length() <= 0) text = " ";
        else                      text = message;
        this.sysMessage.setText(text);   // Thread safe
        GUIUtils.dispatchEmptyAWTEvent();
        return;
    }

    /**
     * 初期パネルを表示する。
     */
    public void showInitPanel(){
        this.cardLayout.show(this.cards, INITCARD);
        return;
    }

    /**
     * 村情報を表示する。
     * @param village 村
     */
    public void showVillageInfo(Village village){
        this.tabBrowser.setVillage(village);
        this.cardLayout.show(this.cards, BROWSECARD);
        this.tabBrowser.repaint();
        this.tabBrowser.revalidate();

        return;
    }

    /**
     * 国情報を表示する。
     * @param land 国
     */
    public void showLandInfo(Land land){
        this.landInfo.update(land);
        this.cardLayout.show(this.cards, LANDCARD);
        return;
    }

    // TODO setEnabled()を全子フレームにも波及させるべきか
}
