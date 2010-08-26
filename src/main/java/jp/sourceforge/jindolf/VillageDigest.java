/*
 * Village digest GUI
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.border.Border;
import jp.sourceforge.jindolf.corelib.GameRole;
import jp.sourceforge.jindolf.corelib.Team;

/**
 * 決着のついた村のサマリを表示する。
 */
@SuppressWarnings("serial")
public class VillageDigest
        extends JDialog
        implements ActionListener,
                   ItemListener {

    private static final String FRAMETITLE =
            "村のダイジェスト - " + Jindolf.TITLE;
    private static final String ITEMDELIM = " : ";

    /**
     * キャプション付き項目をコンテナに追加。
     * @param container コンテナ
     * @param caption 項目キャプション名
     * @param delimiter デリミタ文字
     * @param item 項目アイテム
     */
    private static void addCaptionedItem(Container container,
                                           CharSequence caption,
                                           CharSequence delimiter,
                                           Object item ){
        LayoutManager layout = container.getLayout();
        if( ! (layout instanceof GridBagLayout) ){
            throw new IllegalArgumentException();
        }

        JLabel captionLabel   = new JLabel(caption.toString());
        JLabel delimiterLabel = new JLabel(delimiter.toString());
        JComponent itemComp;
        if(item instanceof JComponent){
            itemComp = (JComponent) item;
        }else{
            itemComp = new JLabel(item.toString());
        }

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.fill    = GridBagConstraints.NONE;
        constraints.insets  = new Insets(2, 2, 2, 2);

        constraints.gridwidth = 1;
        constraints.anchor    = GridBagConstraints.NORTHEAST;
        container.add(captionLabel, constraints);
        container.add(delimiterLabel, constraints);

        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor    = GridBagConstraints.NORTHWEST;
        container.add(itemComp, constraints);

        return;
    }

    /**
     * キャプション付き項目をコンテナに追加。
     * @param container コンテナ
     * @param caption 項目キャプション名
     * @param item 項目アイテム
     */
    private static void addCaptionedItem(Container container,
                                           CharSequence caption,
                                           Object item ){
        addCaptionedItem(container, caption, ITEMDELIM, item);
        return;
    }

    /**
     * レイアウトの最後に詰め物をする。
     * @param container コンテナ
     */
    private static void addFatPad(Container container){
        LayoutManager layout = container.getLayout();
        if( ! (layout instanceof GridBagLayout) ){
            throw new IllegalArgumentException();
        }

        JComponent pad = new JPanel();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill    = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        container.add(pad, constraints);

        return;
    }

    /**
     * GridBagLayoutでレイアウトする空コンポーネントを生成する。
     * @return 空コンポーネント
     */
    private static JComponent createGridBagComponent(){
        JComponent result = new JPanel();
        LayoutManager layout = new GridBagLayout();
        result.setLayout(layout);
        return result;
    }

    private final JComponent summaryPanel = buildSummaryPanel();

    private final JLabel faceLabel = new JLabel();
    private final ImageIcon faceIcon = new ImageIcon();
    private final JComboBox playerBox = new JComboBox();
    private final DefaultComboBoxModel playerListModel =
            new DefaultComboBoxModel();
    private final JButton prevPlayer = new JButton("↑");
    private final JButton nextPlayer = new JButton("↓");
    private final JLabel roleLabel = new JLabel();
    private final JLabel destinyLabel = new JLabel();
    private final JLabel specialSkillLabel = new JLabel();
    private final JLabel entryLabel = new JLabel();
    private final JLabel idLabel = new JLabel();
    private final WebButton urlLine = new WebButton();
    private final JComponent playerPanel = buildPlayerPanel();

    private final JComboBox iconSetBox = new JComboBox();
    private final DefaultComboBoxModel iconSetListModel =
            new DefaultComboBoxModel();
    private final JLabel authorLabel = new JLabel();
    private final JLabel authorUrlLabel = new JLabel();
    private final WebButton iconCatalog = new WebButton();
    private final JButton genCastTableButton =
            new JButton("キャスト表Wiki生成");
    private final JButton copyClipButton =
            new JButton("クリップボードにコピー");
    private final JTextArea templateArea = new JTextArea();
    private final JButton voteButton = new JButton("投票Wiki生成");
    private final JButton vlgWikiButton = new JButton("村詳細Wiki生成");
    private final JComponent clipboardPanel = buildClipboardPanel();

    private final JButton closeButton = new JButton("閉じる");

    private Village village;

    private GameSummary gameSummary;

    /**
     * コンストラクタ。
     * @param owner 親フレーム
     */
    public VillageDigest(Frame owner){
        super(owner, FRAMETITLE, true);

        GUIUtils.modifyWindowAttributes(this, true, false, true);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent event){
                actionClose();
                return;
            }
        });

        this.faceLabel.setIcon(this.faceIcon);

        this.playerBox.setModel(this.playerListModel);
        this.playerBox.addItemListener(this);

        this.prevPlayer.setMargin(new Insets(1, 1, 1, 1));
        this.prevPlayer.addActionListener(this);
        this.prevPlayer.setToolTipText("前のプレイヤー");

        this.nextPlayer.setMargin(new Insets(1, 1, 1, 1));
        this.nextPlayer.addActionListener(this);
        this.nextPlayer.setToolTipText("次のプレイヤー");

        this.iconSetBox.setModel(this.iconSetListModel);
        this.iconSetBox.addItemListener(this);
        for(FaceIconSet iconSet : WolfBBS.getFaceIconSetList()){
            this.iconSetListModel.addElement(iconSet);
        }

        this.iconCatalog.setURLText(
                "http://wolfbbs.jp/"
                +"%A4%DE%A4%C8%A4%E1%A5%B5%A5%A4%A5%C8%A4%C7"
                +"%CD%F8%CD%D1%B2%C4%C7%BD%A4%CA%A5%A2%A5%A4"
                +"%A5%B3%A5%F3%B2%E8%C1%FC.html");
        this.iconCatalog.setCaption("顔アイコン見本ページ");

        this.templateArea.setEditable(true);
        this.templateArea.setLineWrap(true);
        Monodizer.monodize(this.templateArea);
        JPopupMenu popup = new TextPopup();
        this.templateArea.setComponentPopupMenu(popup);

        this.genCastTableButton.addActionListener(this);
        this.voteButton.addActionListener(this);
        this.vlgWikiButton.addActionListener(this);
        this.copyClipButton.addActionListener(this);

        this.closeButton.addActionListener(this);

        Monodizer.monodize(this.idLabel);
        Monodizer.monodize(this.authorUrlLabel);

        Container content = getContentPane();
        design(content);

        return;
    }

    /**
     * 村サマリ画面の生成。
     * @return 村サマリ画面
     */
    private JComponent buildSummaryPanel(){
        JComponent result = createGridBagComponent();

        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        result.setBorder(border);

        return result;
    }

    /**
     * プレイヤーサマリ画面の生成。
     * @return プレイヤーサマリ画面
     */
    private JComponent buildPlayerPanel(){
        JComponent result = createGridBagComponent();
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.fill    = GridBagConstraints.NONE;
        constraints.anchor  = GridBagConstraints.NORTHEAST;
        constraints.insets  = new Insets(2, 2, 2, 2);
        result.add(this.faceLabel, constraints);

        result.add(new JLabel(ITEMDELIM), constraints);

        constraints.anchor  = GridBagConstraints.NORTHWEST;
        result.add(this.playerBox, constraints);
        result.add(this.prevPlayer, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        result.add(this.nextPlayer, constraints);

        addCaptionedItem(result, "役職",      this.roleLabel);
        addCaptionedItem(result, "運命",      this.destinyLabel);
        addCaptionedItem(result, "特殊技能",  this.specialSkillLabel);
        addCaptionedItem(result, "エントリ#", this.entryLabel);
        addCaptionedItem(result, "ID",        this.idLabel);
        addCaptionedItem(result, "URL",       this.urlLine);

        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill    = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        result.add(new JPanel(), constraints);

        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        result.setBorder(border);

        return result;
    }

    /**
     * キャスト表生成画面を生成する。
     * @return キャスト表生成画面
     */
    private JComponent buildCastPanel(){
        JComponent result = createGridBagComponent();
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.anchor  = GridBagConstraints.NORTHEAST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets  = new Insets(2, 2, 2, 2);
        result.add(this.iconCatalog, constraints);

        addCaptionedItem(result, "顔アイコンセットを選択", this.iconSetBox);
        addCaptionedItem(result, "作者", this.authorLabel);
        addCaptionedItem(result, "URL", this.authorUrlLabel);

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill    = GridBagConstraints.NONE;
        constraints.insets  = new Insets(2, 2, 2, 2);
        constraints.anchor  = GridBagConstraints.NORTHEAST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        result.add(this.genCastTableButton, constraints);

        Border border = BorderFactory.createTitledBorder("キャスト表Wiki生成");
        result.setBorder(border);

        return result;
    }

    /**
     * 投票Box生成画面を生成する。
     * @return 投票Box生成画面
     */
    private JComponent buildVotePanel(){
        JComponent result = createGridBagComponent();
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill    = GridBagConstraints.NONE;
        constraints.anchor  = GridBagConstraints.NORTHEAST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets  = new Insets(2, 2, 2, 2);
        result.add(this.voteButton, constraints);

        Border border = BorderFactory.createTitledBorder("投票Wiki生成");
        result.setBorder(border);

        return result;
    }

    /**
     * 村詳細Wiki生成画面を生成する。
     * @return 村詳細Wiki生成画面
     */
    private JComponent buildVillageWikiPanel(){
        JComponent result = createGridBagComponent();
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill    = GridBagConstraints.NONE;
        constraints.anchor  = GridBagConstraints.NORTHEAST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets  = new Insets(2, 2, 2, 2);
        result.add(this.vlgWikiButton, constraints);

        Border border = BorderFactory.createTitledBorder("村詳細Wiki生成");
        result.setBorder(border);

        return result;
    }

    /**
     * Wikiテキスト領域GUIの生成。
     * @return Wikiテキスト領域GUI
     */
    private JComponent buildClipText(){
        JComponent result = createGridBagComponent();
        GridBagConstraints constraints = new GridBagConstraints();

        Border border;

        constraints.insets = new Insets(2, 2, 2, 2);

        constraints.weightx   = 0.0;
        constraints.weighty   = 0.0;
        constraints.fill      = GridBagConstraints.NONE;
        constraints.anchor    = GridBagConstraints.NORTHEAST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        result.add(this.copyClipButton, constraints);

        border = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        this.templateArea.setBorder(border);
        JScrollPane scroller = new JScrollPane();
        JViewport viewPort = scroller.getViewport();
        viewPort.setView(this.templateArea);
        scroller.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroller.setMinimumSize(new Dimension(10, 50));

        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill    = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        result.add(scroller, constraints);

        border = BorderFactory.createTitledBorder("PukiWikiテキスト");
        result.setBorder(border);

        return result;
    }

    /**
     * テンプレート生成画面を生成する。
     * @return テンプレート生成画面
     */
    private JComponent buildClipboardPanel(){
        JComponent result = createGridBagComponent();
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(3, 3, 3, 3);

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill    = GridBagConstraints.HORIZONTAL;
        constraints.anchor  = GridBagConstraints.NORTHWEST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        JComponent castPanel = buildCastPanel();
        result.add(castPanel, constraints);

        JComponent vlgWikiPanel = buildVillageWikiPanel();
        result.add(vlgWikiPanel, constraints);

        JComponent votePanel = buildVotePanel();
        result.add(votePanel, constraints);

        constraints.fill    = GridBagConstraints.NONE;
        constraints.anchor  = GridBagConstraints.CENTER;
        result.add(new JLabel("↓↓↓"), constraints);
        constraints.fill    = GridBagConstraints.HORIZONTAL;
        constraints.anchor  = GridBagConstraints.NORTHWEST;

        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill    = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        JComponent clipText = buildClipText();
        result.add(clipText, constraints);

        return result;
    }

    /**
     * 画面レイアウトを行う。
     * @param container コンテナ
     */
    private void design(Container container){
        LayoutManager layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        container.setLayout(layout);

        JScrollPane scroller1 = new JScrollPane();
        scroller1.getVerticalScrollBar().setUnitIncrement(15);
        scroller1.getHorizontalScrollBar().setUnitIncrement(15);
        JViewport viewPort1 = scroller1.getViewport();
        viewPort1.setView(this.summaryPanel);

        JScrollPane scroller2 = new JScrollPane();
        scroller2.getVerticalScrollBar().setUnitIncrement(15);
        scroller2.getHorizontalScrollBar().setUnitIncrement(15);
        JViewport viewPort2 = scroller2.getViewport();
        viewPort2.setView(this.playerPanel);

        JTabbedPane tabComp = new JTabbedPane();
        tabComp.add("村詳細", scroller1);
        tabComp.add("プレイヤー詳細", scroller2);
        tabComp.add("まとめサイト用Wiki生成", this.clipboardPanel);

        constraints.weightx   = 1.0;
        constraints.weighty   = 1.0;
        constraints.fill      = GridBagConstraints.BOTH;
        constraints.anchor    = GridBagConstraints.NORTHWEST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        container.add(tabComp, constraints);

        constraints.insets = new Insets(3, 3, 3, 3);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.fill    = GridBagConstraints.NONE;
        constraints.anchor  = GridBagConstraints.SOUTHEAST;
        container.add(this.closeButton, constraints);

        return;
    }

    /**
     * このモーダルダイアログを閉じる。
     */
    private void actionClose(){
        setVisible(false);
        dispose();
        return;
    }

    /**
     * 村を設定する。
     * @param village 村
     */
    public void setVillage(Village village){
        clear();

        this.village = village;
        if(village == null) return;

        this.gameSummary = new GameSummary(this.village);

        updateSummary();

        for(Player player : this.gameSummary.getPlayerList()){
            Avatar avatar = player.getAvatar();
            this.playerListModel.addElement(avatar);
        }

        if(this.playerListModel.getSize() >= 2){ // 強制イベント発生
            Object player2nd = this.playerListModel.getElementAt(1);
            this.playerListModel.setSelectedItem(player2nd);
            Object player1st = this.playerListModel.getElementAt(0);
            this.playerListModel.setSelectedItem(player1st);
        }

        return;
    }

    /**
     * 村詳細画面の更新。
     */
    private void updateSummary(){
        String villageName = this.village.getVillageFullName();

        Team winnerTeam = this.gameSummary.getWinnerTeam();
        String wonTeam = winnerTeam.getTeamName();

        int avatarNum = this.gameSummary.countAvatarNum();
        String totalMember = "ゲルト + " + (avatarNum - 1) + "名 = "
                            + avatarNum + "名";

        JComponent roleDetail = createGridBagComponent();
        for(GameRole role : GameRole.values()){
            List<Player> players = this.gameSummary.getRoledPlayerList(role);
            if(players.size() <= 0) continue;
            String roleName = role.getRoleName();
            addCaptionedItem(roleDetail, roleName, " × ", players.size());
        }

        String suddenDeath = this.gameSummary.countSuddenDeath() + "名";

        DateFormat dform =
                DateFormat.getDateTimeInstance(DateFormat.FULL,
                                               DateFormat.FULL);
        Date date;
        date = this.gameSummary.get1stTalkDate();
        String talk1st = dform.format(date);
        date = this.gameSummary.getLastTalkDate();
        String talkLast = dform.format(date);

        int limitHour   = this.village.getLimitHour();
        int limitMinute = this.village.getLimitMinute();
        StringBuilder limit = new StringBuilder();
        if(limitHour < 10) limit.append('0');
        limit.append(limitHour).append(':');
        if(limitMinute < 10) limit.append('0');
        limit.append(limitMinute);

        JComponent transition = createGridBagComponent();
        for(int day = 1; day < this.village.getPeriodSize(); day++){
            List<Player> players = this.gameSummary.getSurvivorList(day);
            CharSequence roleSeq =
                    GameSummary.getRoleBalanceSequence(players);
            String daySeq;
            Period period = this.village.getPeriod(day);
            daySeq = period.getCaption();
            addCaptionedItem(transition, daySeq, roleSeq);
        }

        StringBuilder schedule = new StringBuilder();
        int progressDays = this.village.getProgressDays();
        schedule.append("プロローグ + ")
                .append(progressDays)
                .append("日 + エピローグ");

        CharSequence exeInfo = this.gameSummary.dumpExecutionInfo();
        CharSequence eatInfo = this.gameSummary.dumpAssaultInfo();
        CharSequence scoreSeer = this.gameSummary.dumpSeerActivity();
        CharSequence scoreHunter = this.gameSummary.dumpHunterActivity();

        this.summaryPanel.removeAll();

        addCaptionedItem(this.summaryPanel, "村名",     villageName);
        addCaptionedItem(this.summaryPanel, "勝者",     wonTeam);
        addCaptionedItem(this.summaryPanel, "所要日数", schedule);
        addCaptionedItem(this.summaryPanel, "更新時刻", limit);
        addCaptionedItem(this.summaryPanel, "発言開始", talk1st);
        addCaptionedItem(this.summaryPanel, "最終発言", talkLast);
        addCaptionedItem(this.summaryPanel, "参加人数", totalMember);
        addCaptionedItem(this.summaryPanel, "役職内訳", roleDetail);
        addCaptionedItem(this.summaryPanel, "処刑内訳", exeInfo);
        addCaptionedItem(this.summaryPanel, "襲撃内訳", eatInfo);
        addCaptionedItem(this.summaryPanel, "突然死",   suddenDeath);
        addCaptionedItem(this.summaryPanel, "人口推移", transition);
        addCaptionedItem(this.summaryPanel, "占成績", scoreSeer);
        addCaptionedItem(this.summaryPanel, "狩成績", scoreHunter);

        addFatPad(this.summaryPanel);

        return;
    }

    /**
     * アクションイベントの振り分け。
     * @param event アクションイベント
     */
    public void actionPerformed(ActionEvent event){
        Object source = event.getSource();

        if(source == this.closeButton){
            actionClose();
        }else if(source == this.copyClipButton){
            actionCopyToClipboard();
        }else if(source == this.genCastTableButton){
            actionGenCastTable();
        }else if(source == this.voteButton){
            actionGenVoteBox();
        }else if(source == this.vlgWikiButton){
            actionGenVillageWiki();
        }else if(source == this.prevPlayer){
            int index = this.playerBox.getSelectedIndex();
            if(index <= 0) return;
            index--;
            this.playerBox.setSelectedIndex(index);
        }else if(source == this.nextPlayer){
            int index = this.playerBox.getSelectedIndex();
            int playerNum = this.playerBox.getItemCount();
            if(index >= playerNum - 1) return;
            index++;
            this.playerBox.setSelectedIndex(index);
        }

        return;
    }

    /**
     * キャスト表Wikiデータの生成を行う。
     */
    private void actionGenCastTable(){
        Object selected = this.iconSetListModel.getSelectedItem();
        if( ! (selected instanceof FaceIconSet) ) return;
        FaceIconSet iconSet = (FaceIconSet) selected;

        CharSequence wikiText = this.gameSummary.dumpCastingBoard(iconSet);

        putWikiText(wikiText);

        return;
    }

    /**
     * 投票Boxを生成する。
     */
    private void actionGenVoteBox(){
        CharSequence wikiText = this.gameSummary.dumpVoteBox();
        putWikiText(wikiText);
        return;
    }

    /**
     * 村詳細Wikiを生成する。
     */
    private void actionGenVillageWiki(){
        CharSequence wikiText = this.gameSummary.dumpVillageWiki();
        putWikiText(wikiText);
        return;
    }

    /**
     * Wikiテキストをテキストボックスに出力する。
     * スクロール位置は一番上に。
     * @param wikiText Wikiテキスト
     */
    private void putWikiText(CharSequence wikiText){
        this.templateArea.setText(wikiText.toString());
        // 最上部へスクロールアップ
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                templateArea.scrollRectToVisible(new Rectangle());
            }
        });
        // TODO あらかじめテキストを全選択させておきたい
        return;
    }

    /**
     * Wiki文字列をクリップボードへコピーする。
     */
    private void actionCopyToClipboard(){
        CharSequence text = this.templateArea.getText();
        ClipboardAction.copyToClipboard(text);
        return;
    }

    /**
     * プレイヤーの選択操作。
     * @param avatar 選択されたAvatar
     */
    private void selectPlayer(Avatar avatar){
        if(avatar == this.playerBox.getItemAt(0)){
            this.prevPlayer.setEnabled(false);
        }else{
            this.prevPlayer.setEnabled(true);
        }

        int playerNum = this.playerBox.getItemCount();
        if(avatar == this.playerBox.getItemAt(playerNum - 1)){
            this.nextPlayer.setEnabled(false);
        }else{
            this.nextPlayer.setEnabled(true);
        }

        Image image = village.getAvatarFaceImage(avatar);
        this.faceIcon.setImage(image);
        this.faceLabel.setIcon(null);          // なぜかこれが必要
        this.faceLabel.setIcon(this.faceIcon);

        Player player = this.gameSummary.getPlayer(avatar);

        GameRole role = player.getRole();
        this.roleLabel.setText(role.getRoleName());

        String destinyMessage = player.getDestinyMessage();
        this.destinyLabel.setText(destinyMessage);

        CharSequence specialSkill = "";
        switch(role){
        case SEER:
            specialSkill = this.gameSummary.dumpSeerActivity();
            break;
        case HUNTER:
            specialSkill = this.gameSummary.dumpHunterActivity();
            break;
        default:
            break;
        }
        this.specialSkillLabel.setText(specialSkill.toString());

        this.entryLabel.setText(String.valueOf(player.getEntryNo()));

        String userId = player.getIdName();
        this.idLabel.setText(userId);

        String urlText = player.getUrlText();
        String caption = urlText;

        if(urlText == null || urlText.length() <= 0){
            urlText = WolfBBS.encodeURLFromId(userId);
            caption = "もしかして " + urlText;
        }

        this.urlLine.setURLText(urlText);
        this.urlLine.setCaption(caption);

        return;
    }

    /**
     * 顔アイコンセットの選択操作。
     * @param iconSet 顔アイコンセット
     */
    private void selectIconSet(FaceIconSet iconSet){
        String author  = iconSet.getAuthor();
        String urlText = iconSet.getUrlText();
        this.authorLabel   .setText(author + "氏");
        this.authorUrlLabel.setText(urlText);
        return;
    }

    /**
     * コンボボックス操作の受信。
     * @param event コンボボックス操作イベント
     */
    public void itemStateChanged(ItemEvent event){
        int state = event.getStateChange();
        if(state != ItemEvent.SELECTED) return;

        Object source = event.getSource();
        Object item = event.getItem();
        if(item == null) return;

        if(source == this.playerBox){
            if( ! (item instanceof Avatar) ) return;
            Avatar avatar = (Avatar) item;
            selectPlayer(avatar);
        }else if(source == this.iconSetBox){
            if( ! (item instanceof FaceIconSet) ) return;
            FaceIconSet iconSet = (FaceIconSet) item;
            selectIconSet(iconSet);
        }

        return;
    }

    /**
     * 表示内容をクリアする。
     */
    private void clear(){
        this.templateArea.setText("");
        this.playerListModel.removeAllElements();
        return;
    }

    // TODO ハムスター対応
}
