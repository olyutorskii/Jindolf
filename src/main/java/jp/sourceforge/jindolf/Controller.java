/*
 * MVC controller
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: Controller.java 999 2010-03-15 11:59:28Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.TreePath;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.VillageState;

/**
 * いわゆるMVCでいうとこのコントローラ。
 */
public class Controller
        implements ActionListener,
                   TreeWillExpandListener,
                   TreeSelectionListener,
                   ChangeListener,
                   AnchorHitListener {

    private final ActionManager actionManager;
    private final TopView topView;
    private final LandsModel model;

    private final FilterPanel filterFrame;
    private final LogFrame showlogFrame;
    private final OptionPanel optionPanel;
    private final FindPanel findPanel;
    private final TalkPreview talkPreview;
    private JFrame helpFrame;
    private AccountPanel accountFrame;
    private DaySummary daySummaryPanel;
    private VillageDigest digestPanel;
    private final Map<Window, Boolean> windowMap =
            new HashMap<Window, Boolean>();

    private volatile boolean isBusyNow;

    private JFrame topFrame = null;

    /**
     * コントローラの生成。
     * @param actionManager アクション管理
     * @param topView 最上位ビュー
     * @param model 最上位データモデル
     */
    public Controller(ActionManager actionManager,
                       TopView topView,
                       LandsModel model){
        super();

        this.actionManager = actionManager;
        this.topView = topView;
        this.model = model;

        JToolBar toolbar = this.actionManager.getBrowseToolBar();
        this.topView.setBrowseToolBar(toolbar);

        this.actionManager.addActionListener(this);

        JTree treeView = this.topView.getTreeView();
        treeView.setModel(this.model);
        treeView.addTreeWillExpandListener(this);
        treeView.addTreeSelectionListener(this);

        this.topView.getTabBrowser().addChangeListener(this);
        this.topView.getTabBrowser().addActionListener(this);
        this.topView.getTabBrowser().addAnchorHitListener(this);

        JButton reloadVillageListButton = this.topView
                                         .getLandsTree()
                                         .getReloadVillageListButton();
        reloadVillageListButton.addActionListener(this);
        reloadVillageListButton.setEnabled(false);

        this.filterFrame = new FilterPanel(this.topFrame);
        this.filterFrame.addChangeListener(this);
        this.filterFrame.pack();
        this.filterFrame.setVisible(false);

        this.showlogFrame = new LogFrame(this.topFrame);
        this.showlogFrame.pack();
        this.showlogFrame.setSize(600, 500);
        this.showlogFrame.setLocationByPlatform(true);
        this.showlogFrame.setVisible(false);
        if(Jindolf.hasLoggingPermission()){
            Handler newHandler = this.showlogFrame.getHandler();
            Logger jre14Logger = Jindolf.logger().getJre14Logger();
            jre14Logger.addHandler(newHandler);
            Handler[] handlers = jre14Logger.getHandlers();
            for(Handler handler : handlers){
                if( ! (handler instanceof PileHandler) ) continue;
                PileHandler pile = (PileHandler) handler;
                pile.delegate(newHandler);
                pile.close();
            }
        }

        this.talkPreview = new TalkPreview();
        this.talkPreview.pack();
        this.talkPreview.setSize(700, 500);
        this.talkPreview.setVisible(false);
        this.talkPreview.loadDraft();

        this.optionPanel = new OptionPanel(this.topFrame);
        this.optionPanel.pack();
        this.optionPanel.setSize(450, 500);
        this.optionPanel.setVisible(false);

        this.findPanel = new FindPanel(this.topFrame);
        this.findPanel.pack();
        this.findPanel.setVisible(false);
        this.findPanel.loadHistory();

        this.windowMap.put(this.filterFrame,  true);
        this.windowMap.put(this.showlogFrame, false);
        this.windowMap.put(this.talkPreview,  false);
        this.windowMap.put(this.optionPanel,  false);
        this.windowMap.put(this.findPanel,    true);

        AppSetting setting = Jindolf.getAppSetting();

        FontInfo fontInfo = setting.getFontInfo();
        this.topView.getTabBrowser().setFontInfo(fontInfo);
        this.talkPreview.setFontInfo(fontInfo);
        this.optionPanel.getFontChooser().setFontInfo(fontInfo);

        ProxyInfo proxyInfo = setting.getProxyInfo();
        this.optionPanel.getProxyChooser().setProxyInfo(proxyInfo);

        DialogPref pref = setting.getDialogPref();
        this.topView.getTabBrowser().setDialogPref(pref);
        this.optionPanel.getDialogPrefPanel().setDialogPref(pref);

        return;
    }

    /**
     * トップフレームを生成する。
     * @return トップフレーム
     */
    @SuppressWarnings("serial")
    public JFrame createTopFrame(){
        this.topFrame = new JFrame();

        Container content = this.topFrame.getContentPane();
        LayoutManager layout = new BorderLayout();
        content.setLayout(layout);
        content.add(this.topView, BorderLayout.CENTER);

        Component glassPane = new JComponent() {};
        glassPane.addMouseListener(new MouseAdapter() {});
        glassPane.addKeyListener(new KeyAdapter() {});
        this.topFrame.setGlassPane(glassPane);

        this.topFrame.setJMenuBar(this.actionManager.getMenuBar());
        setFrameTitle(null);

        this.windowMap.put(this.topFrame, false);

        this.topFrame.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE);
        this.topFrame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosed(WindowEvent event){
                shutdown();
            }
        });

        return this.topFrame;
    }

    /**
     * About画面を表示する。
     */
    private void actionAbout(){
        String message =
                Jindolf.TITLE
                + "   Version " + Jindolf.VERSION + "\n"
                + Jindolf.COPYRIGHT + "\n"
                + "ライセンス: " + Jindolf.LICENSE + "\n"
                + "連絡先: " + Jindolf.CONTACT;

        if(Jindolf.COMMENT.length() > 0){
            message += "\n" + Jindolf.COMMENT;
        }

        JOptionPane pane = new JOptionPane(message,
                                           JOptionPane.INFORMATION_MESSAGE,
                                           JOptionPane.DEFAULT_OPTION,
                                           GUIUtils.getLogoIcon());

        JDialog dialog = pane.createDialog(this.topFrame,
                                           Jindolf.TITLE + "について");

        dialog.pack();
        dialog.setVisible(true);
        dialog.dispose();

        return;
    }

    /**
     * アプリ終了。
     */
    private void actionExit(){
        shutdown();
        return;
    }

    /**
     * Help画面を表示する。
     */
    private void actionHelp(){
        if(this.helpFrame != null){                 // show Toggle
            toggleWindow(this.helpFrame);
            return;
        }

        this.helpFrame = new HelpFrame();
        this.helpFrame.pack();
        this.helpFrame.setSize(450, 450);

        this.windowMap.put(this.helpFrame, false);

        this.helpFrame.setVisible(true);

        return;
    }

    /**
     * 村をWebブラウザで表示する。
     */
    private void actionShowWebVillage(){
        TabBrowser browser = this.topView.getTabBrowser();
        Village village = browser.getVillage();
        if(village == null) return;

        Land land = village.getParentLand();
        ServerAccess server = land.getServerAccess();

        URL url = server.getVillageURL(village);

        String urlText = url.toString();
        if(village.getState() != VillageState.GAMEOVER){
            urlText += "#bottom";
        }

        WebIPCDialog.showDialog(this.topFrame, urlText);

        return;
    }

    /**
     * 村に対応するまとめサイトをWebブラウザで表示する。
     */
    private void actionShowWebWiki(){
        TabBrowser browser = this.topView.getTabBrowser();
        Village village = browser.getVillage();
        if(village == null) return;

        String villageName;
        LandDef landDef = village.getParentLand().getLandDef();
        if(landDef.getLandId().equals("wolfg")){
            String vnum = "000" + village.getVillageID();
            vnum = vnum.substring(vnum.length() - 3);
            villageName = landDef.getLandPrefix() + vnum;
        }else{
            villageName = village.getVillageName();
        }

        StringBuilder url =
                new StringBuilder()
                .append("http://wolfbbs.jp/")
                .append(villageName)
                .append("%C2%BC.html");

        WebIPCDialog.showDialog(this.topFrame, url.toString());

        return;
    }

    /**
     * 村に対応するキャスト紹介表ジェネレーターをWebブラウザで表示する。
     */
    private void actionShowWebCast(){
        TabBrowser browser = this.topView.getTabBrowser();
        Village village = browser.getVillage();
        if(village == null) return;

        Land land = village.getParentLand();
        ServerAccess server = land.getServerAccess();

        URL villageUrl = server.getVillageURL(village);

        StringBuilder url = new StringBuilder("http://hon5.com/jinro/");

        try{
            url.append("?u=")
               .append(URLEncoder.encode(villageUrl.toString(), "UTF-8"));
        }catch(UnsupportedEncodingException e){
            return;
        }

        url.append("&s=1");

        WebIPCDialog.showDialog(this.topFrame, url.toString());

        return;
    }

    /**
     * 日(Period)をWebブラウザで表示する。
     */
    private void actionShowWebDay(){
        PeriodView periodView = currentPeriodView();
        if(periodView == null) return;

        Period period = periodView.getPeriod();
        if(period == null) return;

        TabBrowser browser = this.topView.getTabBrowser();
        Village village = browser.getVillage();
        if(village == null) return;

        Land land = village.getParentLand();
        ServerAccess server = land.getServerAccess();

        URL url = server.getPeriodURL(period);

        String urlText = url.toString();
        if(period.isHot()) urlText += "#bottom";

        WebIPCDialog.showDialog(this.topFrame, urlText);

        return;
    }

    /**
     * 個別の発言をWebブラウザで表示する。
     */
    private void actionShowWebTalk(){
        TabBrowser browser = this.topView.getTabBrowser();
        Village village = browser.getVillage();
        if(village == null) return;

        PeriodView periodView = currentPeriodView();
        if(periodView == null) return;

        Discussion discussion = periodView.getDiscussion();
        Talk talk = discussion.getPopupedTalk();
        if(talk == null) return;

        Period period = periodView.getPeriod();
        if(period == null) return;

        Land land = village.getParentLand();
        ServerAccess server = land.getServerAccess();

        URL url = server.getPeriodURL(period);

        String urlText = url.toString();
        urlText += "#" + talk.getMessageID();
        WebIPCDialog.showDialog(this.topFrame, urlText);

        return;
    }

    /**
     * ポータルサイトをWebブラウザで表示する。
     */
    private void actionShowPortal(){
        WebIPCDialog.showDialog(this.topFrame, Jindolf.CONTACT);
        return;
    }

    /**
     * L&Fの変更を行う。
     */
    // TODO Nimbus対応
    private void actionChangeLaF(){
        String className = this.actionManager.getSelectedLookAndFeel();

        LookAndFeel lnf;
        try{
            Class<?> lnfClass = Class.forName(className);
            lnf = (LookAndFeel)( lnfClass.newInstance() );
        }catch(Exception e){
            String message = "このLook&Feel["
                            + className
                            + "]を読み込む事ができません。";
            Jindolf.logger().warn(message, e);
            JOptionPane.showMessageDialog(
                this.topFrame,
                message,
                "Look&Feel - " + Jindolf.TITLE,
                JOptionPane.WARNING_MESSAGE );
            return;
        }

        try{
            UIManager.setLookAndFeel(lnf);
        }catch(UnsupportedLookAndFeelException e){
            String message = "このLook&Feel["
                            + lnf.getName()
                            + "]はサポートされていません。";
            Jindolf.logger().warn(message, e);
            JOptionPane.showMessageDialog(
                this.topFrame,
                message,
                "Look&Feel - " + Jindolf.TITLE,
                JOptionPane.WARNING_MESSAGE );
            return;
        }

        Jindolf.logger().info(
                "Look&Feelが["
                +lnf.getName()
                +"]に変更されました。");

        final Runnable updateUITask = new Runnable(){
            public void run(){
                Set<Window> windows = Controller.this.windowMap.keySet();
                for(Window window : windows){
                    SwingUtilities.updateComponentTreeUI(window);
                    window.validate();
                    boolean needPack = Controller.this.windowMap.get(window);
                    if(needPack){
                        window.pack();
                    }
                }

                return;
            }
        };

        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable(){
            public void run(){
                setBusy(true);
                updateStatusBar("Look&Feelを更新中…");
                try{
                    SwingUtilities.invokeAndWait(updateUITask);
                }catch(Exception e){
                    Jindolf.logger().warn(
                            "Look&Feelの更新に失敗しました。", e);
                }finally{
                    updateStatusBar("Look&Feelが更新されました");
                    setBusy(false);
                }
                return;
            }
        });

        return;
    }

    /**
     * 発言フィルタ画面を表示する。
     */
    private void actionShowFilter(){
        toggleWindow(this.filterFrame);
        return;
    }

    /**
     * アカウント管理画面を表示する。
     */
    private void actionShowAccount(){
        if(this.accountFrame != null){                 // show Toggle
            toggleWindow(this.accountFrame);
            return;
        }

        this.accountFrame = new AccountPanel(this.topFrame, this.model);
        this.accountFrame.pack();
        this.accountFrame.setVisible(true);

        this.windowMap.put(this.accountFrame, true);

        return;
    }

    /**
     * ログ表示画面を表示する。
     */
    private void actionShowLog(){
        toggleWindow(this.showlogFrame);
        return;
    }

    /**
     * 発言エディタを表示する。
     */
    private void actionTalkPreview(){
        toggleWindow(this.talkPreview);
        return;
    }

    /**
     * オプション設定画面を表示する。
     */
    private void actionOption(){
        AppSetting setting = Jindolf.getAppSetting();

        FontInfo fontInfo = setting.getFontInfo();
        this.optionPanel.getFontChooser().setFontInfo(fontInfo);

        ProxyInfo proxyInfo = setting.getProxyInfo();
        this.optionPanel.getProxyChooser().setProxyInfo(proxyInfo);

        DialogPref dialogPref = setting.getDialogPref();
        this.optionPanel.getDialogPrefPanel().setDialogPref(dialogPref);

        this.optionPanel.setVisible(true);
        if(this.optionPanel.isCanceled()) return;

        fontInfo = this.optionPanel.getFontChooser().getFontInfo();
        updateFontInfo(fontInfo);

        proxyInfo = this.optionPanel.getProxyChooser().getProxyInfo();
        updateProxyInfo(proxyInfo);

        dialogPref = this.optionPanel.getDialogPrefPanel().getDialogPref();
        updateDialogPref(dialogPref);

        return;
    }

    /**
     * フォント設定を変更する。
     * @param newFontInfo 新フォント設定
     */
    private void updateFontInfo(final FontInfo newFontInfo){
        AppSetting setting = Jindolf.getAppSetting();
        FontInfo oldInfo = setting.getFontInfo();

        if(newFontInfo.equals(oldInfo)) return;
        setting.setFontInfo(newFontInfo);

        this.topView.getTabBrowser().setFontInfo(newFontInfo);
        this.talkPreview.setFontInfo(newFontInfo);
        this.optionPanel.getFontChooser().setFontInfo(newFontInfo);

        return;
    }

    /**
     * プロクシ設定を変更する。
     * @param newProxyInfo 新プロクシ設定
     */
    private void updateProxyInfo(ProxyInfo newProxyInfo){
        AppSetting setting = Jindolf.getAppSetting();
        ProxyInfo oldProxyInfo = setting.getProxyInfo();

        if(newProxyInfo.equals(oldProxyInfo)) return;
        setting.setProxyInfo(newProxyInfo);

        for(Land land : this.model.getLandList()){
            ServerAccess server = land.getServerAccess();
            server.setProxy(newProxyInfo.getProxy());
        }

        return;
    }

    /**
     * 発言表示設定を更新する。
     * @param newDialogPref 表示設定
     */
    private void updateDialogPref(DialogPref newDialogPref){
        AppSetting setting = Jindolf.getAppSetting();
        DialogPref oldDialogPref = setting.getDialogPref();

        if(newDialogPref.equals(oldDialogPref)) return;
        setting.setDialogPref(newDialogPref);

        this.topView.getTabBrowser().setDialogPref(newDialogPref);

        return;
    }

    /**
     * 村ダイジェスト画面を表示する。
     */
    private void actionShowDigest(){
        TabBrowser browser = this.topView.getTabBrowser();
        final Village village = browser.getVillage();
        if(village == null) return;

        VillageState villageState = village.getState();
        if((   villageState != VillageState.EPILOGUE
            && villageState != VillageState.GAMEOVER
           ) || ! village.isValid() ){
            String message = "エピローグを正常に迎えていない村は\n"
                            +"ダイジェスト機能を利用できません";
            String title = "ダイジェスト不可 - " + Jindolf.TITLE;
            JOptionPane pane = new JOptionPane(message,
                                               JOptionPane.WARNING_MESSAGE,
                                               JOptionPane.DEFAULT_OPTION );
            JDialog dialog = pane.createDialog(this.topFrame, title);
            dialog.pack();
            dialog.setVisible(true);
            dialog.dispose();
            return;
        }

        if(this.digestPanel == null){
            this.digestPanel = new VillageDigest(this.topFrame);
            this.digestPanel.pack();
            this.digestPanel.setSize(600, 550);
            this.windowMap.put(this.digestPanel, false);
        }

        final VillageDigest digest = this.digestPanel;
        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable(){
            public void run(){
                taskFullOpenAllPeriod();
                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        digest.setVillage(village);
                        digest.setVisible(true);
                        return;
                    }
                });
                return;
            }
        });

        return;
    }

    /**
     * 全日程の一括フルオープン。ヘビータスク版。
     */
    // TODO taskLoadAllPeriodtと一体化したい。
    private void taskFullOpenAllPeriod(){
        setBusy(true);
        updateStatusBar("一括読み込み開始");
        try{
            TabBrowser browser = this.topView.getTabBrowser();
            Village village = browser.getVillage();
            if(village == null) return;
            for(PeriodView periodView : browser.getPeriodViewList()){
                Period period = periodView.getPeriod();
                if(period == null) continue;
                if(period.isFullOpen()) continue;
                String message =
                        period.getDay()
                        + "日目のデータを読み込んでいます";
                updateStatusBar(message);
                try{
                    Period.parsePeriod(period, true);
                }catch(IOException e){
                    showNetworkError(village, e);
                    return;
                }
                periodView.showTopics();
            }
        }finally{
            updateStatusBar("一括読み込み完了");
            setBusy(false);
        }
        return;
    }

    /**
     * 検索パネルを表示する。
     */
    private void actionShowFind(){
        this.findPanel.setVisible(true);
        if(this.findPanel.isCanceled()){
            updateFindPanel();
            return;
        }
        if(this.findPanel.isBulkSearch()){
            bulkSearch();
        }else{
            regexSearch();
        }
        return;
    }

    /**
     * 検索処理。
     */
    private void regexSearch(){
        Discussion discussion = currentDiscussion();
        if(discussion == null) return;

        RegexPattern regPattern = this.findPanel.getRegexPattern();
        int hits = discussion.setRegexPattern(regPattern);

        String hitMessage = "［" + hits + "］件ヒットしました";
        updateStatusBar(hitMessage);

        String loginfo = "";
        if(regPattern != null){
            Pattern pattern = regPattern.getPattern();
            if(pattern != null){
                loginfo = "正規表現 " + pattern.pattern() + " に";
            }
        }
        loginfo += hitMessage;
        Jindolf.logger().info(loginfo);

        return;
    }

    /**
     * 一括検索処理。
     */
    private void bulkSearch(){
        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable(){
            public void run(){
                taskBulkSearch();
                return;
            }
        });
    }

    /**
     * 一括検索処理。ヘビータスク版。
     */
    private void taskBulkSearch(){
        taskLoadAllPeriod();
        int totalhits = 0;
        RegexPattern regPattern = this.findPanel.getRegexPattern();
        StringBuilder hitDesc = new StringBuilder();
        TabBrowser browser = this.topView.getTabBrowser();
        for(PeriodView periodView : browser.getPeriodViewList()){
            Discussion discussion = periodView.getDiscussion();
            int hits = discussion.setRegexPattern(regPattern);
            totalhits += hits;

            if(hits > 0){
                Period period = discussion.getPeriod();
                hitDesc.append(' ').append(period.getDay()).append("d:");
                hitDesc.append(hits).append("件");
            }
        }
        String hitMessage =
                  "［" + totalhits + "］件ヒットしました。"
                + hitDesc.toString();
        updateStatusBar(hitMessage);

        String loginfo = "";
        if(regPattern != null){
            Pattern pattern = regPattern.getPattern();
            if(pattern != null){
                loginfo = "正規表現 " + pattern.pattern() + " に";
            }
        }
        loginfo += hitMessage;
        Jindolf.logger().info(loginfo);

        return;
    }

    /**
     * 検索パネルに現在選択中のPeriodを反映させる。
     */
    private void updateFindPanel(){
        Discussion discussion = currentDiscussion();
        if(discussion == null) return;
        RegexPattern pattern = discussion.getRegexPattern();
        this.findPanel.setRegexPattern(pattern);
        return;
    }

    /**
     * 発言集計パネルを表示。
     */
    private void actionDaySummary(){
        PeriodView periodView = currentPeriodView();
        if(periodView == null) return;

        Period period = periodView.getPeriod();
        if(period == null) return;

        if(this.daySummaryPanel == null){
            this.daySummaryPanel = new DaySummary(this.topFrame);
            this.daySummaryPanel.pack();
            this.daySummaryPanel.setSize(400, 500);
        }

        this.daySummaryPanel.summaryPeriod(period);
        this.daySummaryPanel.setVisible(true);

        this.windowMap.put(this.daySummaryPanel, false);

        return;
    }

    /**
     * 表示中PeriodをCSVファイルへエクスポートする。
     */
    private void actionDayExportCsv(){
        PeriodView periodView = currentPeriodView();
        if(periodView == null) return;

        Period period = periodView.getPeriod();
        if(period == null) return;

        File file = CsvExporter.exportPeriod(period, this.filterFrame);
        if(file != null){
            String message = "CSVファイル("
                            +file.getName()
                            +")へのエクスポートが完了しました";
            updateStatusBar(message);
        }

        // TODO 長そうなジョブなら別スレッドにした方がいいか？

        return;
    }

    /**
     * 検索結果の次候補へジャンプ。
     */
    private void actionSearchNext(){
        Discussion discussion = currentDiscussion();
        if(discussion == null) return;

        discussion.nextHotTarget();

        return;
    }

    /**
     * 検索結果の全候補へジャンプ。
     */
    private void actionSearchPrev(){
        Discussion discussion = currentDiscussion();
        if(discussion == null) return;

        discussion.prevHotTarget();

        return;
    }

    /**
     * Period表示の強制再更新処理。
     */
    private void actionReloadPeriod(){
        updatePeriod(true);
        return;
    }

    /**
     * 全日程の一括ロード。
     */
    private void actionLoadAllPeriod(){
        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable(){
            public void run(){
                taskLoadAllPeriod();
                return;
            }
        });

        return;
    }

    /**
     * 全日程の一括ロード。ヘビータスク版。
     */
    private void taskLoadAllPeriod(){
        setBusy(true);
        updateStatusBar("一括読み込み開始");
        try{
            TabBrowser browser = this.topView.getTabBrowser();
            Village village = browser.getVillage();
            if(village == null) return;
            for(PeriodView periodView : browser.getPeriodViewList()){
                Period period = periodView.getPeriod();
                if(period == null) continue;
                String message =
                        period.getDay()
                        + "日目のデータを読み込んでいます";
                updateStatusBar(message);
                try{
                    Period.parsePeriod(period, false);
                }catch(IOException e){
                    showNetworkError(village, e);
                    return;
                }
                periodView.showTopics();
            }
        }finally{
            updateStatusBar("一括読み込み完了");
            setBusy(false);
        }
        return;
    }

    /**
     * 村一覧の再読み込み。
     */
    private void actionReloadVillageList(){
        JTree tree = this.topView.getTreeView();
        TreePath path = tree.getSelectionPath();
        if(path == null) return;

        Land land = null;
        for(int ct = 0; ct < path.getPathCount(); ct++){
            Object obj = path.getPathComponent(ct);
            if(obj instanceof Land){
                land = (Land) obj;
                break;
            }
        }
        if(land == null) return;

        this.topView.showInitPanel();

        execReloadVillageList(land);

        return;
    }

    /**
     * 選択文字列をクリップボードにコピーする。
     */
    private void actionCopySelected(){
        Discussion discussion = currentDiscussion();
        if(discussion == null) return;

        CharSequence copied = discussion.copySelected();
        if(copied == null) return;

        copied = StringUtils.suppressString(copied);
        updateStatusBar(
                "[" + copied + "]をクリップボードにコピーしました");
        return;
    }

    /**
     * 一発言のみクリップボードにコピーする。
     */
    private void actionCopyTalk(){
        Discussion discussion = currentDiscussion();
        if(discussion == null) return;

        CharSequence copied = discussion.copyTalk();
        if(copied == null) return;

        copied = StringUtils.suppressString(copied);
        updateStatusBar(
                "[" + copied + "]をクリップボードにコピーしました");
        return;
    }

    /**
     * アンカーにジャンプする。
     */
    private void actionJumpAnchor(){
        PeriodView periodView = currentPeriodView();
        if(periodView == null) return;
        Discussion discussion = periodView.getDiscussion();

        final TabBrowser browser = this.topView.getTabBrowser();
        final Village village = browser.getVillage();
        final Anchor anchor = discussion.getPopupedAnchor();
        if(anchor == null) return;

        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable(){
            public void run(){
                setBusy(true);
                updateStatusBar("ジャンプ先の読み込み中…");

                if(anchor.hasTalkNo()){
                    // TODO もう少し賢くならない？
                    taskLoadAllPeriod();
                }

                final List<Talk> talkList;
                try{
                    talkList = village.getTalkListFromAnchor(anchor);
                    if(talkList == null || talkList.size() <= 0){
                        updateStatusBar(
                                  "アンカーのジャンプ先["
                                + anchor.toString()
                                + "]が見つかりません");
                        return;
                    }

                    final Talk targetTalk = talkList.get(0);
                    final Period targetPeriod = targetTalk.getPeriod();
                    final int tabIndex = targetPeriod.getDay() + 1;
                    final PeriodView target = browser.getPeriodView(tabIndex);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            browser.setSelectedIndex(tabIndex);
                            target.setPeriod(targetPeriod);
                            target.scrollToTalk(targetTalk);
                            return;
                        }
                    });
                    updateStatusBar(
                              "アンカー["
                            + anchor.toString()
                            + "]にジャンプしました");
                }catch(IOException e){
                    updateStatusBar(
                            "アンカーの展開中にエラーが起きました");
                }finally{
                    setBusy(false);
                }

                return;
            }
        });

        return;
    }

    /**
     * 指定した国の村一覧を読み込む。
     * @param land 国
     */
    private void execReloadVillageList(final Land land){
        final LandsTree treePanel = this.topView.getLandsTree();
        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable(){
            public void run(){
                setBusy(true);
                updateStatusBar("村一覧を読み込み中…");
                try{
                    try{
                        Controller.this.model.loadVillageList(land);
                    }catch(IOException e){
                        showNetworkError(land, e);
                    }
                    treePanel.expandLand(land);
                }finally{
                    updateStatusBar("村一覧の読み込み完了");
                    setBusy(false);
                }
                return;
            }
        });
        return;
    }

    /**
     * Period表示の更新処理。
     * @param force trueならPeriodデータを強制再読み込み。
     */
    private void updatePeriod(final boolean force){
        final TabBrowser tabBrowser = this.topView.getTabBrowser();
        final Village village = tabBrowser.getVillage();
        if(village == null) return;
        setFrameTitle(village.getVillageFullName());

        final PeriodView periodView = currentPeriodView();
        Discussion discussion = currentDiscussion();
        if(discussion == null) return;
        discussion.setTopicFilter(this.filterFrame);
        final Period period = discussion.getPeriod();
        if(period == null) return;

        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable(){
            public void run(){
                setBusy(true);
                try{
                    boolean wasHot = loadPeriod();

                    if(wasHot && ! period.isHot() ){
                        if( ! updatePeriodList() ) return;
                    }

                    renderBrowser();
                }finally{
                    setBusy(false);
                }
                return;
            }

            private boolean loadPeriod(){
                updateStatusBar("1日分のデータを読み込んでいます…");
                boolean wasHot;
                try{
                    wasHot = period.isHot();
                    try{
                        Period.parsePeriod(period, force);
                    }catch(IOException e){
                        showNetworkError(village, e);
                    }
                }finally{
                    updateStatusBar("1日分のデータを読み終わりました");
                }
                return wasHot;
            }

            private boolean updatePeriodList(){
                updateStatusBar("村情報を読み直しています…");
                try{
                    Village.updateVillage(village);
                }catch(IOException e){
                    showNetworkError(village, e);
                    return false;
                }
                try{
                    SwingUtilities.invokeAndWait(new Runnable(){
                        public void run(){
                            tabBrowser.setVillage(village);
                            return;
                        }
                    });
                }catch(Exception e){
                    Jindolf.logger().fatal(
                            "タブ操作で致命的な障害が発生しました", e);
                }
                updateStatusBar("村情報を読み直しました…");
                return true;
            }

            private void renderBrowser(){
                updateStatusBar("レンダリング中…");
                try{
                    final int lastPos = periodView.getVerticalPosition();
                    try{
                        SwingUtilities.invokeAndWait(new Runnable(){
                            public void run(){
                                periodView.showTopics();
                                return;
                            }
                        });
                    }catch(Exception e){
                        Jindolf.logger().fatal(
                                "ブラウザ表示で致命的な障害が発生しました", e);
                    }
                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            periodView.setVerticalPosition(lastPos);
                        }
                    });
                }finally{
                    updateStatusBar("レンダリング完了");
                }
                return;
            }
        });

        return;
    }

    /**
     * 発言フィルタの操作による更新処理。
     */
    private void filterChanged(){
        final Discussion discussion = currentDiscussion();
        if(discussion == null) return;
        discussion.setTopicFilter(this.filterFrame);

        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable(){
            public void run(){
                setBusy(true);
                updateStatusBar("フィルタリング中…");
                try{
                    discussion.filtering();
                }finally{
                    updateStatusBar("フィルタリング完了");
                    setBusy(false);
                }
                return;
            }
        });

        return;
    }

    /**
     * 現在選択中のPeriodを内包するPeriodViewを返す。
     * @return PeriodView
     */
    private PeriodView currentPeriodView(){
        TabBrowser tb = this.topView.getTabBrowser();
        PeriodView result = tb.currentPeriodView();
        return result;
    }

    /**
     * 現在選択中のPeriodを内包するDiscussionを返す。
     * @return Discussion
     */
    private Discussion currentDiscussion(){
        PeriodView periodView = currentPeriodView();
        if(periodView == null) return null;
        Discussion result = periodView.getDiscussion();
        return result;
    }

    /**
     * フレーム表示のトグル処理。
     * @param window フレーム
     */
    private void toggleWindow(Window window){
        if(window == null) return;

        if(window instanceof Frame){
            Frame frame = (Frame) window;
            int winState = frame.getExtendedState();
            boolean isIconified = (winState & Frame.ICONIFIED) != 0;
            if(isIconified){
                winState &= ~(Frame.ICONIFIED);
                frame.setExtendedState(winState);
                frame.setVisible(true);
                return;
            }
        }

        if(window.isVisible()){
            window.setVisible(false);
            window.dispose();
        }else{
            window.setVisible(true);
        }
        return;
    }

    /**
     * ネットワークエラーを通知するモーダルダイアログを表示する。
     * OKボタンを押すまでこのメソッドは戻ってこない。
     * @param village 村
     * @param e ネットワークエラー
     */
    public void showNetworkError(Village village, IOException e){
        Land land = village.getParentLand();
        showNetworkError(land, e);
        return;
    }

    /**
     * ネットワークエラーを通知するモーダルダイアログを表示する。
     * OKボタンを押すまでこのメソッドは戻ってこない。
     * @param land 国
     * @param e ネットワークエラー
     */
    public void showNetworkError(Land land, IOException e){
        Jindolf.logger().warn("ネットワークで障害が発生しました", e);

        ServerAccess server = land.getServerAccess();
        String message =
                land.getLandDef().getLandName()
                +"を運営するサーバとの間の通信で"
                +"何らかのトラブルが発生しました。\n"
                +"相手サーバのURLは [ " + server.getBaseURL() + " ] だよ。\n"
                +"プロクシ設定は正しいかな？\n"
                +"Webブラウザでも遊べないか確認してみてね!\n";

        JOptionPane pane = new JOptionPane(message,
                                           JOptionPane.WARNING_MESSAGE,
                                           JOptionPane.DEFAULT_OPTION );

        JDialog dialog = pane.createDialog(this.topFrame,
                                           "通信異常発生 - " + Jindolf.TITLE);

        dialog.pack();
        dialog.setVisible(true);
        dialog.dispose();

        return;
    }

    /**
     * {@inheritDoc}
     * ツリーリストで何らかの要素（国、村）がクリックされたときの処理。
     * @param event イベント {@inheritDoc}
     */
    public void valueChanged(TreeSelectionEvent event){
        TreePath path = event.getNewLeadSelectionPath();
        if(path == null) return;

        Object selObj = path.getLastPathComponent();

        if( selObj instanceof Land ){
            Land land = (Land)selObj;
            setFrameTitle(land.getLandDef().getLandName());
            this.topView.showLandInfo(land);
            this.actionManager.appearVillage(false);
            this.actionManager.appearPeriod(false);
        }else if( selObj instanceof Village ){
            final Village village = (Village)selObj;

            Executor executor = Executors.newCachedThreadPool();
            executor.execute(new Runnable(){
                public void run(){
                    setBusy(true);
                    updateStatusBar("村情報を読み込み中…");

                    try{
                        Village.updateVillage(village);
                    }catch(IOException e){
                        showNetworkError(village, e);
                        return;
                    }finally{
                        updateStatusBar("村情報の読み込み完了");
                        setBusy(false);
                    }

                    Controller.this.actionManager.appearVillage(true);
                    setFrameTitle(village.getVillageFullName());

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            Controller.this.topView.showVillageInfo(village);
                            return;
                        }
                    });

                    return;
                }
            });
        }

        return;
    }

    /**
     * {@inheritDoc}
     * Periodがタブ選択されたときもしくは発言フィルタが操作されたときの処理。
     * @param event イベント {@inheritDoc}
     */
    public void stateChanged(ChangeEvent event){
        Object source = event.getSource();

        if(source == this.filterFrame){
            filterChanged();
        }else if(source instanceof TabBrowser){
            updateFindPanel();
            updatePeriod(false);
            PeriodView periodView = currentPeriodView();
            if(periodView == null) this.actionManager.appearPeriod(false);
            else                   this.actionManager.appearPeriod(true);
        }
        return;
    }

    /**
     * {@inheritDoc}
     * 主にメニュー選択やボタン押下など。
     * @param e イベント {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e){
        if(this.isBusyNow) return;

        String cmd = e.getActionCommand();
        if(cmd.equals(ActionManager.CMD_ACCOUNT)){
            actionShowAccount();
        }else if(cmd.equals(ActionManager.CMD_EXIT)){
            actionExit();
        }else if(cmd.equals(ActionManager.CMD_COPY)){
            actionCopySelected();
        }else if(cmd.equals(ActionManager.CMD_SHOWFIND)){
            actionShowFind();
        }else if(cmd.equals(ActionManager.CMD_SEARCHNEXT)){
            actionSearchNext();
        }else if(cmd.equals(ActionManager.CMD_SEARCHPREV)){
            actionSearchPrev();
        }else if(cmd.equals(ActionManager.CMD_ALLPERIOD)){
            actionLoadAllPeriod();
        }else if(cmd.equals(ActionManager.CMD_SHOWDIGEST)){
            actionShowDigest();
        }else if(cmd.equals(ActionManager.CMD_WEBVILL)){
            actionShowWebVillage();
        }else if(cmd.equals(ActionManager.CMD_WEBWIKI)){
            actionShowWebWiki();
        }else if(cmd.equals(ActionManager.CMD_WEBCAST)){
            actionShowWebCast();
        }else if(cmd.equals(ActionManager.CMD_RELOAD)){
            actionReloadPeriod();
        }else if(cmd.equals(ActionManager.CMD_DAYSUMMARY)){
            actionDaySummary();
        }else if(cmd.equals(ActionManager.CMD_DAYEXPCSV)){
            actionDayExportCsv();
        }else if(cmd.equals(ActionManager.CMD_WEBDAY)){
            actionShowWebDay();
        }else if(cmd.equals(ActionManager.CMD_OPTION)){
            actionOption();
        }else if(cmd.equals(ActionManager.CMD_LANDF)){
            actionChangeLaF();
        }else if(cmd.equals(ActionManager.CMD_SHOWFILT)){
            actionShowFilter();
        }else if(cmd.equals(ActionManager.CMD_SHOWEDIT)){
            actionTalkPreview();
        }else if(cmd.equals(ActionManager.CMD_SHOWLOG)){
            actionShowLog();
        }else if(cmd.equals(ActionManager.CMD_HELPDOC)){
            actionHelp();
        }else if(cmd.equals(ActionManager.CMD_SHOWPORTAL)){
            actionShowPortal();
        }else if(cmd.equals(ActionManager.CMD_ABOUT)){
            actionAbout();
        }else if(cmd.equals(ActionManager.CMD_VILLAGELIST)){
            actionReloadVillageList();
        }else if(cmd.equals(ActionManager.CMD_COPYTALK)){
            actionCopyTalk();
        }else if(cmd.equals(ActionManager.CMD_JUMPANCHOR)){
            actionJumpAnchor();
        }else if(cmd.equals(ActionManager.CMD_WEBTALK)){
            actionShowWebTalk();
        }
        return;
    }

    /**
     * {@inheritDoc}
     * 村選択ツリーリストが畳まれるとき呼ばれる。
     * @param event ツリーイベント {@inheritDoc}
     */
    public void treeWillCollapse(TreeExpansionEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * 村選択ツリーリストが展開されるとき呼ばれる。
     * @param event ツリーイベント {@inheritDoc}
     */
    public void treeWillExpand(TreeExpansionEvent event){
        if(!(event.getSource() instanceof JTree)){
            return;
        }

        TreePath path = event.getPath();
        Object lastObj = path.getLastPathComponent();
        if(!(lastObj instanceof Land)){
            return;
        }
        final Land land = (Land) lastObj;
        if(land.getVillageCount() > 0){
            return;
        }

        execReloadVillageList(land);

        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    public void anchorHitted(AnchorHitEvent event){
        PeriodView periodView = currentPeriodView();
        if(periodView == null) return;
        Period period = periodView.getPeriod();
        if(period == null) return;
        final Village village = period.getVillage();

        final TalkDraw talkDraw = event.getTalkDraw();
        final Anchor anchor = event.getAnchor();
        final Discussion discussion = periodView.getDiscussion();

        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable(){
            public void run(){
                setBusy(true);
                updateStatusBar("アンカーの展開中…");

                if(anchor.hasTalkNo()){
                    // TODO もう少し賢くならない？
                    taskLoadAllPeriod();
                }

                final List<Talk> talkList;
                try{
                    talkList = village.getTalkListFromAnchor(anchor);
                    if(talkList == null || talkList.size() <= 0){
                        updateStatusBar(
                                  "アンカーの展開先["
                                + anchor.toString()
                                + "]が見つかりません");
                        return;
                    }
                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            talkDraw.showAnchorTalks(anchor, talkList);
                            discussion.layoutRows();
                            return;
                        }
                    });
                    updateStatusBar(
                              "アンカー["
                            + anchor.toString()
                            + "]の展開完了");
                }catch(IOException e){
                    updateStatusBar(
                            "アンカーの展開中にエラーが起きました");
                }finally{
                    setBusy(false);
                }

                return;
            }
        });

        return;
    }

    /**
     * ヘビーなタスク実行をアピール。
     * プログレスバーとカーソルの設定を行う。
     * @param isBusy trueならプログレスバーのアニメ開始&WAITカーソル。
     *                falseなら停止&通常カーソル。
     */
    private void setBusy(final boolean isBusy){
        this.isBusyNow = isBusy;

        Runnable microJob = new Runnable(){
            public void run(){
                Cursor cursor;
                if(isBusy){
                    cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                }else{
                    cursor = Cursor.getDefaultCursor();
                }

                Component glass = Controller.this.topFrame.getGlassPane();
                glass.setCursor(cursor);
                glass.setVisible(isBusy);
                Controller.this.topView.setBusy(isBusy);

                return;
            }
        };

        if(SwingUtilities.isEventDispatchThread()){
            microJob.run();
        }else{
            try{
                SwingUtilities.invokeAndWait(microJob);
            }catch(Exception e){
                Jindolf.logger().fatal("ビジー処理で失敗", e);
            }
        }

        return;
    }

    /**
     * ステータスバーを更新する。
     * @param message メッセージ
     */
    private void updateStatusBar(String message){
        this.topView.updateSysMessage(message);
    }

    /**
     * トップフレームのタイトルを設定する。
     * タイトルは指定された国or村名 + " - Jindolf"
     * @param name 国or村名
     */
    private void setFrameTitle(CharSequence name){
        String title = Jindolf.TITLE;

        if(name != null && name.length() > 0){
            title = name + " - " + title;
        }

        this.topFrame.setTitle(title);

        return;
    }

    /**
     * アプリ正常終了処理。
     */
    private void shutdown(){
        this.findPanel.saveHistory();
        this.talkPreview.saveDraft();
        Jindolf.getAppSetting().saveConfig();
        Jindolf.exit(0);
        return;
    }

}
