/*
 * Top view
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.view;

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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import jp.sfjp.jindolf.VerInfo;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.data.Village;

/**
 * 最上位ビュー。
 *
 * <p>メインアプリウィンドウの各種コンポーネントの祖先コンテナ。
 *
 * <p>{@link JSplitPane}の左に国村選択リスト、
 * 右にカードコンテナがレイアウトされる。
 *
 * <p>カードコンテナ上に
 * 初期画面、国情報パネル{@link LandsTree}とタブブラウザ{@link TabBrowser}
 * の3コンポーネントが重ねてレイアウトされ、必要に応じて切り替わる。
 *
 * <p>ヘビーなタスク実行をアピールするために、
 * プログレスバーとフッタメッセージの管理を行う。
 */
@SuppressWarnings("serial")
public final class TopView extends JPanel{

    private static final String INITCARD = "INITCARD";
    private static final String LANDCARD = "LANDINFO";
    private static final String BROWSECARD = "BROWSER";

    private static final String MSG_THANKS =
            VerInfo.TITLE + "\u0020" + VerInfo.VERSION
            + "\u0020を使ってくれてありがとう！";


    private final JComponent cards;
    private final CardLayout cardLayout = new CardLayout();

    private final JTabbedPane villageSelector =
            new JTabbedPane(JTabbedPane.BOTTOM);
    private final LandsTree landsTreeView = new LandsTree();
    private final LocalOpener localOpener = new LocalOpener();

    private final LandInfoPanel landInfo = new LandInfoPanel();

    private final JTextField sysMessage = new JTextField("");
    private final JProgressBar progressBar = new JProgressBar();

    private final TabBrowser tabBrowser = new TabBrowser();

    // to place toolbar
    private final JComponent browsePanel = createBrowsePanel();


    /**
     * コンストラクタ。
     */
    public TopView(){
        super();

        this.cards = createCards();

        this.villageSelector.addTab("サーバ", this.landsTreeView);
        this.villageSelector.addTab("ローカル", this.localOpener);

        JComponent split = createSplitPane(this.villageSelector, this.cards);
        JComponent statusBar = createStatusBar();

        BorderLayout layout = new BorderLayout();
        setLayout(layout);
        add(split, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        return;
    }

    /**
     * カードパネルを生成する。
     *
     * @return カードパネル
     */
    private JComponent createCards(){
        JComponent initCard = createInitCard();
        JComponent landInfoCard = createLandInfoCard();

        JPanel cardContainer = new JPanel();
        cardContainer.setLayout(this.cardLayout);

        cardContainer.add(INITCARD, initCard);
        cardContainer.add(LANDCARD, landInfoCard);
        cardContainer.add(BROWSECARD, this.browsePanel);

        return cardContainer;
    }

    /**
     * 初期パネルを生成。
     *
     * @return 初期パネル
     */
    private JComponent createInitCard(){
        StringBuilder init = new StringBuilder();
        init.append("←    人狼BBSサーバから村を選択してください");
        init.append("<br/><br/>または「ファイル」メニューから");
        init.append("<br/>JinArchive形式でダウンロードした");
        init.append("<br/>アーカイブXMLファイルを開いてください");
        init.insert(0, "<font 'size=+1'>").append("</font>");
        init.insert(0, "<center>").append("</center>");
        init.insert(0, "<body>").append("</body>");
        init.insert(0, "<html>").append("</html>");
        JLabel initMessage = new JLabel(init.toString());

        StringBuilder warn = new StringBuilder();
        warn.append("※ たまにはWebブラウザでアクセスして、");
        warn.append("<br></br>");
        warn.append("運営の動向を確かめようね！");
        warn.insert(0, "<font 'size=+1'>").append("</font>");
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
        panel.add(warnMessage, constraints);

        JScrollPane scrollPane = new JScrollPane(panel);

        return scrollPane;
    }

    /**
     * 国別情報を生成。
     *
     * @return 国別情報
     */
    private JComponent createLandInfoCard(){
        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        this.landInfo.setBorder(border);
        JScrollPane scrollPane = new JScrollPane(this.landInfo);
        return scrollPane;
    }

    /**
     * タブブラウザにツールバーを併設するためのコンテナを生成。
     *
     * @return コンテナ
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
     *
     * @param toolbar ツールバー
     */
    public void setBrowseToolBar(JToolBar toolbar){
        this.browsePanel.add(toolbar, BorderLayout.NORTH);
        return;
    }

    /**
     * SplitPaneを生成。
     *
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
     *
     * @return ステータスバー
     */
    private JComponent createStatusBar(){
        this.sysMessage.setText(MSG_THANKS);
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
     *
     * @return 国村選択ツリービュー
     */
    public JTree getTreeView(){
        return this.landsTreeView.getTreeView();
    }

    /**
     * タブビューを返す。
     *
     * @return タブビュー
     */
    public TabBrowser getTabBrowser(){
        return this.tabBrowser;
    }

    /**
     * 村一覧ビューを返す。
     *
     * @return 村一番ビュー
     */
    public LandsTree getLandsTree(){
        return this.landsTreeView;
    }

    /**
     * プログレスバーの設定を行う。
     *
     * @param busy trueならプログレスバーのアニメ開始。
     *     falseなら停止。
     */
    public void setBusy(boolean busy){
        this.progressBar.setIndeterminate(busy);
        return;
    }

    /**
     * ステータスバーの更新。
     *
     * @param message 更新文字列
     */
    public void updateSysMessage(String message){
        String text = message;
        if(message == null) text = "";
        this.sysMessage.setText(text);   // Thread safe
        return;
    }

    /**
     * ステータスバー文字列を返す。
     *
     * @return ステータスバー文字列
     */
    public String getSysMessage(){
        String result = this.sysMessage.getText();
        if(result == null) result = "";
        return result;
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
     *
     * @param village 村
     */
    public void showVillageInfo(Village village){
        this.tabBrowser.setVillage(village);
        this.cardLayout.show(this.cards, BROWSECARD);
        return;
    }

    /**
     * 国情報を表示する。
     *
     * @param land 国
     */
    public void showLandInfo(Land land){
        this.landInfo.update(land);
        this.cardLayout.show(this.cards, LANDCARD);
        return;
    }

}
