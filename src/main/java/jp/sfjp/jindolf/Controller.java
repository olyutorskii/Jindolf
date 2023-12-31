/*
 * MVC controller
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;
import jp.sfjp.jindolf.config.AppSetting;
import jp.sfjp.jindolf.config.ConfigStore;
import jp.sfjp.jindolf.config.JsonIo;
import jp.sfjp.jindolf.config.OptionInfo;
import jp.sfjp.jindolf.data.Anchor;
import jp.sfjp.jindolf.data.DialogPref;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.data.LandsTreeModel;
import jp.sfjp.jindolf.data.Period;
import jp.sfjp.jindolf.data.RegexPattern;
import jp.sfjp.jindolf.data.Talk;
import jp.sfjp.jindolf.data.Village;
import jp.sfjp.jindolf.data.html.PeriodLoader;
import jp.sfjp.jindolf.data.html.VillageInfoLoader;
import jp.sfjp.jindolf.data.html.VillageListLoader;
import jp.sfjp.jindolf.data.xml.VillageLoader;
import jp.sfjp.jindolf.dxchg.CsvExporter;
import jp.sfjp.jindolf.dxchg.WebIPCDialog;
import jp.sfjp.jindolf.dxchg.WolfBBS;
import jp.sfjp.jindolf.glyph.AnchorHitEvent;
import jp.sfjp.jindolf.glyph.AnchorHitListener;
import jp.sfjp.jindolf.glyph.Discussion;
import jp.sfjp.jindolf.glyph.FontChooser;
import jp.sfjp.jindolf.glyph.FontInfo;
import jp.sfjp.jindolf.glyph.TalkDraw;
import jp.sfjp.jindolf.log.LogFrame;
import jp.sfjp.jindolf.log.LogUtils;
import jp.sfjp.jindolf.net.ProxyInfo;
import jp.sfjp.jindolf.net.ServerAccess;
import jp.sfjp.jindolf.summary.DaySummary;
import jp.sfjp.jindolf.summary.VillageDigest;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sfjp.jindolf.util.StringUtils;
import jp.sfjp.jindolf.view.ActionManager;
import jp.sfjp.jindolf.view.AvatarPics;
import jp.sfjp.jindolf.view.FilterPanel;
import jp.sfjp.jindolf.view.FindPanel;
import jp.sfjp.jindolf.view.HelpFrame;
import jp.sfjp.jindolf.view.LandsTree;
import jp.sfjp.jindolf.view.OptionPanel;
import jp.sfjp.jindolf.view.PeriodView;
import jp.sfjp.jindolf.view.TabBrowser;
import jp.sfjp.jindolf.view.TopFrame;
import jp.sfjp.jindolf.view.TopView;
import jp.sfjp.jindolf.view.WindowManager;
import jp.sourceforge.jindolf.corelib.VillageState;
import jp.sourceforge.jovsonz.JsObject;
import org.xml.sax.SAXException;

/**
 * いわゆるMVCでいうとこのコントローラ。
 */
public class Controller
        implements ActionListener,
                   AnchorHitListener {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private static final String ERRTITLE_LAF = "Look&Feel";
    private static final String ERRFORM_LAFGEN =
            "このLook&Feel[{0}]を生成する事ができません。";


    private final LandsTreeModel model;
    private final WindowManager windowManager;
    private final ActionManager actionManager;
    private final AppSetting appSetting;

    private final TopView topView;

    private final JFileChooser xmlFileChooser = buildFileChooser();

    private final VillageTreeWatcher treeVillageWatcher =
            new VillageTreeWatcher();
    private final ChangeListener tabPeriodWatcher =
            new TabPeriodWatcher();
    private final ChangeListener filterWatcher =
            new FilterWatcher();

    private final BusyStatus busyStatus;


    /**
     * コントローラの生成。
     * @param model 最上位データモデル
     * @param windowManager ウィンドウ管理
     * @param actionManager アクション管理
     * @param setting アプリ設定
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public Controller(LandsTreeModel model,
                      WindowManager windowManager,
                      ActionManager actionManager,
                      AppSetting setting){
        super();

        this.appSetting = setting;
        this.actionManager = actionManager;
        this.windowManager = windowManager;
        this.model = model;

        this.topView = this.windowManager.getTopFrame().getTopView();

        JToolBar toolbar = this.actionManager.getBrowseToolBar();
        this.topView.setBrowseToolBar(toolbar);

        this.actionManager.addActionListener(this);

        JTree treeView = this.topView.getTreeView();
        treeView.setModel(this.model);
        treeView.addTreeWillExpandListener(this.treeVillageWatcher);
        treeView.addTreeSelectionListener(this.treeVillageWatcher);

        TabBrowser periodTab = this.topView.getTabBrowser();
        periodTab.addChangeListener(this.tabPeriodWatcher);
        periodTab.addActionListener(this);
        periodTab.addAnchorHitListener(this);

        JButton reloadVillageListButton = this.topView
                                         .getLandsTree()
                                         .getReloadVillageListButton();
        reloadVillageListButton.addActionListener(this);
        reloadVillageListButton.setEnabled(false);

        TopFrame topFrame         = this.windowManager.getTopFrame();
        OptionPanel optionPanel   = this.windowManager.getOptionPanel();
        FindPanel findPanel       = this.windowManager.getFindPanel();
        FilterPanel filterPanel   = this.windowManager.getFilterPanel();
        LogFrame logFrame         = this.windowManager.getLogFrame();
        HelpFrame helpFrame       = this.windowManager.getHelpFrame();

        topFrame.setJMenuBar(this.actionManager.getMenuBar());
        setFrameTitle(null);
        topFrame.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE);
        topFrame.addWindowListener(new WindowAdapter(){
            /** {@inheritDoc} */
            @Override
            public void windowClosed(WindowEvent event){
                shutdown();
            }
        });
        this.busyStatus = new BusyStatus(topFrame);

        filterPanel.addChangeListener(this.filterWatcher);

        Handler newHandler = logFrame.getHandler();
        EventQueue.invokeLater(() -> {
            LogUtils.switchHandler(newHandler);
        });

        JsonIo jsonIo = this.appSetting.getJsonIo();

        JsObject history = jsonIo.loadHistoryConfig();
        findPanel.putJson(history);

        FontInfo fontInfo = this.appSetting.getFontInfo();
        periodTab.setFontInfo(fontInfo);
        optionPanel.getFontChooser().setFontInfo(fontInfo);

        ProxyInfo proxyInfo = this.appSetting.getProxyInfo();
        optionPanel.getProxyChooser().setProxyInfo(proxyInfo);

        DialogPref pref = this.appSetting.getDialogPref();
        periodTab.setDialogPref(pref);
        optionPanel.getDialogPrefPanel().setDialogPref(pref);

        OptionInfo optInfo = this.appSetting.getOptionInfo();
        ConfigStore configStore = this.appSetting.getConfigStore();
        helpFrame.updateVmInfo(optInfo, configStore);

        return;
    }


    /**
     * フレーム表示のトグル処理。
     * @param window フレーム
     */
    private static void toggleWindow(Window window){
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
     * XMLファイルを選択するためのChooserを生成する。
     *
     * @return Chooser
     */
    private static JFileChooser buildFileChooser(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        FileFilter filter;
        filter = new FileNameExtensionFilter("XML files (*.xml)", "xml", "XML");
        chooser.setFileFilter(filter);

        chooser.setDialogTitle("アーカイブXMLファイルを開く");

        return chooser;
    }


    /**
     * ウィンドウマネジャを返す。
     * @return ウィンドウマネジャ
     */
    public WindowManager getWindowManager(){
        return this.windowManager;
    }

    /**
     * アプリ最上位フレームを返す。
     * @return アプリ最上位フレーム
     */
    public TopFrame getTopFrame(){
        TopFrame result = this.windowManager.getTopFrame();
        return result;
    }

    /**
     * トップフレームのタイトルを設定する。
     * タイトルは指定された国or村名 + " - Jindolf"
     * @param name 国or村名
     */
    private void setFrameTitle(String name){
        String title = VerInfo.getFrameTitle(name);
        TopFrame topFrame = this.windowManager.getTopFrame();
        topFrame.setTitle(title);
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
     * 現在選択中の村を返す。
     *
     * @return 選択中の村。なければnull。
     */
    private Village getVillage(){
        TabBrowser browser = this.topView.getTabBrowser();
        Village village = browser.getVillage();
        return village;
    }

    /**
     * ステータスバーを更新する。
     * @param message メッセージ
     */
    private void updateStatusBar(String message){
        this.topView.updateSysMessage(message);
        return;
    }

    /**
     * About画面を表示する。
     */
    private void actionAbout(){
        String message = VerInfo.getAboutMessage();
        JOptionPane pane = new JOptionPane(message,
                                           JOptionPane.INFORMATION_MESSAGE,
                                           JOptionPane.DEFAULT_OPTION,
                                           GUIUtils.getLogoIcon());

        JDialog dialog = pane.createDialog(VerInfo.TITLE + "について");

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
        HelpFrame helpFrame = this.windowManager.getHelpFrame();
        toggleWindow(helpFrame);
        return;
    }

    /**
     * 村をWebブラウザで表示する。
     */
    private void actionShowWebVillage(){
        Village village = getVillage();
        if(village == null) return;

        Land land = village.getParentLand();
        ServerAccess server = land.getServerAccess();

        URL url = server.getVillageURL(village);

        String urlText = url.toString();
        if(village.getState() != VillageState.GAMEOVER){
            urlText += "#bottom";
        }

        WebIPCDialog.showDialog(getTopFrame(), urlText);

        return;
    }

    /**
     * 村に対応するまとめサイトをWebブラウザで表示する。
     */
    private void actionShowWebWiki(){
        Village village = getVillage();
        if(village == null) return;

        String urlTxt = WolfBBS.getCastGeneratorUrl(village);
        WebIPCDialog.showDialog(getTopFrame(), urlTxt);

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

        Village village = getVillage();
        if(village == null) return;

        Land land = village.getParentLand();
        ServerAccess server = land.getServerAccess();

        URL url = server.getPeriodURL(period);

        String urlText = url.toString();

        WebIPCDialog.showDialog(getTopFrame(), urlText);

        return;
    }

    /**
     * 個別の発言をWebブラウザで表示する。
     */
    private void actionShowWebTalk(){
        Village village = getVillage();
        if(village == null) return;

        PeriodView periodView = currentPeriodView();
        if(periodView == null) return;

        Discussion discussion = periodView.getDiscussion();
        Talk talk = discussion.getActiveTalk();
        if(talk == null) return;

        Period period = periodView.getPeriod();
        if(period == null) return;

        Land land = village.getParentLand();
        ServerAccess server = land.getServerAccess();

        URL url = server.getPeriodURL(period);

        String urlText = url.toString();
        urlText += "#" + talk.getMessageID();
        WebIPCDialog.showDialog(getTopFrame(), urlText);

        return;
    }

    /**
     * ポータルサイトをWebブラウザで表示する。
     */
    private void actionShowPortal(){
        WebIPCDialog.showDialog(getTopFrame(), VerInfo.CONTACT);
        return;
    }

    /**
     * 例外発生による警告ダイアログへの反応を促す。
     * @param title タイトル文字列
     * @param message メッセージ
     * @param e 例外
     */
    private void warnDialog(String title, String message, Throwable e){
        LOGGER.log(Level.WARNING, message, e);
        JOptionPane.showMessageDialog(
            getTopFrame(),
            message,
            VerInfo.getFrameTitle(title),
            JOptionPane.WARNING_MESSAGE );
        return;
    }

    /**
     * L&amp;Fの変更指示を受信する。
     */
    private void actionChangeLaF(){
        String className = this.actionManager.getSelectedLookAndFeel();
        if(className == null) return;

        this.busyStatus.submitLightBusyTask(
            () -> {taskChangeLaF(className);},
            "Look&Feelを更新中…",
            "Look&Feelが更新されました"
        );

        return;
    }

    /**
     * LookAndFeelの実際の更新を行う軽量タスク。
     *
     * @param lnf LookAndFeel
     */
    private void taskChangeLaF(String className){
        assert EventQueue.isDispatchThread();

        try{
            this.windowManager.changeAllWindowUI(className);
        }catch(UnsupportedLookAndFeelException e){
            String warnMsg = MessageFormat.format(
                    "このLook&Feel[{0}]はサポートされていません。",
                    className);
            warnDialog(ERRTITLE_LAF, warnMsg, e);
            return;
        }catch(ReflectiveOperationException e){
            String warnMsg = MessageFormat.format(ERRFORM_LAFGEN, className);
            warnDialog(ERRTITLE_LAF, warnMsg, e);
            return;
        }

        this.xmlFileChooser.updateUI();

        LOGGER.log(Level.INFO,
                   "Look&Feelが[{0}]に変更されました。", className );

        return;
    }

    /**
     * 発言フィルタ画面を表示する。
     */
    private void actionShowFilter(){
        FilterPanel filterPanel = this.windowManager.getFilterPanel();
        toggleWindow(filterPanel);
        return;
    }

    /**
     * ログ表示画面を表示する。
     */
    private void actionShowLog(){
        LogFrame logFrame = this.windowManager.getLogFrame();
        toggleWindow(logFrame);
        return;
    }

    /**
     * オプション設定画面を表示する。
     */
    private void actionOption(){
        OptionPanel optionPanel = this.windowManager.getOptionPanel();

        FontInfo fontInfo = this.appSetting.getFontInfo();
        optionPanel.getFontChooser().setFontInfo(fontInfo);

        ProxyInfo proxyInfo = this.appSetting.getProxyInfo();
        optionPanel.getProxyChooser().setProxyInfo(proxyInfo);

        DialogPref dialogPref = this.appSetting.getDialogPref();
        optionPanel.getDialogPrefPanel().setDialogPref(dialogPref);

        optionPanel.setVisible(true);
        if(optionPanel.isCanceled()) return;

        fontInfo = optionPanel.getFontChooser().getFontInfo();
        updateFontInfo(fontInfo);

        proxyInfo = optionPanel.getProxyChooser().getProxyInfo();
        updateProxyInfo(proxyInfo);

        dialogPref = optionPanel.getDialogPrefPanel().getDialogPref();
        updateDialogPref(dialogPref);

        return;
    }

    /**
     * フォント設定を変更する。
     * @param newFontInfo 新フォント設定
     */
    private void updateFontInfo(final FontInfo newFontInfo){
        FontInfo oldInfo = this.appSetting.getFontInfo();

        if(newFontInfo.equals(oldInfo)) return;
        this.appSetting.setFontInfo(newFontInfo);

        this.topView.getTabBrowser().setFontInfo(newFontInfo);

        OptionPanel optionPanel = this.windowManager.getOptionPanel();
        FontChooser fontChooser = optionPanel.getFontChooser();

        fontChooser.setFontInfo(newFontInfo);

        return;
    }

    /**
     * プロクシ設定を変更する。
     * @param newProxyInfo 新プロクシ設定
     */
    private void updateProxyInfo(ProxyInfo newProxyInfo){
        ProxyInfo oldProxyInfo = this.appSetting.getProxyInfo();

        if(newProxyInfo.equals(oldProxyInfo)) return;
        this.appSetting.setProxyInfo(newProxyInfo);

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
        DialogPref oldDialogPref = this.appSetting.getDialogPref();

        if(newDialogPref.equals(oldDialogPref)) return;
        this.appSetting.setDialogPref(newDialogPref);

        this.topView.getTabBrowser().setDialogPref(newDialogPref);

        return;
    }

    /**
     * 村ダイジェスト画面を表示する。
     */
    private void actionShowDigest(){
        Village village = getVillage();
        if(village == null) return;

        VillageState villageState = village.getState();
        if( (   villageState != VillageState.EPILOGUE
             && villageState != VillageState.GAMEOVER
            ) || ! village.isValid() ){
            String message = "エピローグを正常に迎えていない村は\n"
                            +"ダイジェスト機能を利用できません";
            String title = VerInfo.getFrameTitle("ダイジェスト不可");
            JOptionPane pane = new JOptionPane(message,
                                               JOptionPane.WARNING_MESSAGE,
                                               JOptionPane.DEFAULT_OPTION );
            JDialog dialog = pane.createDialog(title);
            dialog.pack();
            dialog.setVisible(true);
            dialog.dispose();
            return;
        }

        VillageDigest villageDigest = this.windowManager.getVillageDigest();
        final VillageDigest digest = villageDigest;

        Runnable task = () -> {
            taskFullOpenAllPeriod();
            EventQueue.invokeLater(() -> {
                digest.setVillage(village);
                digest.setVisible(true);
            });
        };

        this.busyStatus.submitHeavyBusyTask(
                task,
                "一括読み込み開始",
                "一括読み込み完了"
        );

        return;
    }

    /**
     * 全日程の一括フルオープン。ヘビータスク版。
     */
    // TODO taskLoadAllPeriodtと一体化したい。
    private void taskFullOpenAllPeriod(){
        TabBrowser browser = this.topView.getTabBrowser();
        Village village = getVillage();
        if(village == null) return;
        for(PeriodView periodView : browser.getPeriodViewList()){
            Period period = periodView.getPeriod();
            if(period == null) continue;
            String message =
                    period.getDay()
                    + "日目のデータを読み込んでいます";
            updateStatusBar(message);
            try{
                PeriodLoader.parsePeriod(period, false);
            }catch(IOException e){
                showNetworkError(village, e);
                return;
            }
            periodView.showTopics();
        }

        return;
    }

    /**
     * 検索パネルを表示する。
     */
    private void actionShowFind(){
        FindPanel findPanel = this.windowManager.getFindPanel();

        findPanel.setVisible(true);
        if(findPanel.isCanceled()){
            updateFindPanel();
            return;
        }
        if(findPanel.isBulkSearch()){
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

        FindPanel findPanel = this.windowManager.getFindPanel();
        RegexPattern regPattern = findPanel.getRegexPattern();
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
        LOGGER.info(loginfo);

        return;
    }

    /**
     * 一括検索処理。
     */
    private void bulkSearch(){
        this.busyStatus.submitHeavyBusyTask(
                () -> {taskBulkSearch();},
                null, null
        );
        return;
    }

    /**
     * 一括検索処理。ヘビータスク版。
     */
    private void taskBulkSearch(){
        taskLoadAllPeriod();
        int totalhits = 0;
        FindPanel findPanel = this.windowManager.getFindPanel();
        RegexPattern regPattern = findPanel.getRegexPattern();
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
        LOGGER.info(loginfo);

        return;
    }

    /**
     * 検索パネルに現在選択中のPeriodを反映させる。
     */
    private void updateFindPanel(){
        Discussion discussion = currentDiscussion();
        if(discussion == null) return;
        RegexPattern pattern = discussion.getRegexPattern();
        FindPanel findPanel = this.windowManager.getFindPanel();
        findPanel.setRegexPattern(pattern);
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

        DaySummary daySummary = this.windowManager.getDaySummary();
        daySummary.summaryPeriod(period);
        daySummary.setVisible(true);

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

        FilterPanel filterPanel = this.windowManager.getFilterPanel();
        File file = CsvExporter.exportPeriod(period, filterPanel);
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

        Village village = getVillage();
        if(village == null) return;
        if(village.getState() != VillageState.EPILOGUE) return;

        Discussion discussion = currentDiscussion();
        if(discussion == null) return;
        Period period = discussion.getPeriod();
        if(period == null) return;
        if(period.getTopics() > 1000){
            JOptionPane.showMessageDialog(getTopFrame(),
                    "エピローグが1000発言を超えはじめたら、\n"
                    +"負荷対策のためWebブラウザによるアクセスを"
                    +"心がけましょう",
                    "長大エピローグ警告",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        return;
    }

    /**
     * 全日程の一括ロード。
     */
    private void actionLoadAllPeriod(){
        this.busyStatus.submitHeavyBusyTask(
                () -> {taskLoadAllPeriod();},
                "一括読み込み開始",
                "一括読み込み完了"
        );

        return;
    }

    /**
     * 全日程の一括ロード。ヘビータスク版。
     */
    private void taskLoadAllPeriod(){
        TabBrowser browser = this.topView.getTabBrowser();
        Village village = getVillage();
        if(village == null) return;
        for(PeriodView periodView : browser.getPeriodViewList()){
            Period period = periodView.getPeriod();
            if(period == null) continue;
            String message =
                    period.getDay()
                    + "日目のデータを読み込んでいます";
            updateStatusBar(message);
            try{
                PeriodLoader.parsePeriod(period, false);
            }catch(IOException e){
                showNetworkError(village, e);
                return;
            }
            periodView.showTopics();
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

        submitReloadVillageList(land);

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
     * アンカー先を含むPeriodの全会話を事前にロードする。
     *
     * @param village 村
     * @param anchor アンカー
     * @return アンカー先を含むPeriod。
     * アンカーがG国発言番号ならnull。
     * Periodが見つからないならnull。
     * @throws IOException 入力エラー
     */
    private Period loadAnchoredPeriod(Village village, Anchor anchor)
            throws IOException{
        if(anchor.hasTalkNo()) return null;

        Period anchorPeriod = village.getPeriod(anchor);
        if(anchorPeriod == null) return null;

        PeriodLoader.parsePeriod(anchorPeriod, false);

        return anchorPeriod;
    }

    /**
     * アンカーにジャンプする。
     */
    private void actionJumpAnchor(){
        PeriodView periodView = currentPeriodView();
        if(periodView == null) return;
        Discussion discussion = periodView.getDiscussion();

        TabBrowser browser = this.topView.getTabBrowser();
        Village village = getVillage();
        final Anchor anchor = discussion.getActiveAnchor();
        if(anchor == null) return;

        Runnable task = () -> {
            if(anchor.hasTalkNo()){
                // TODO もう少し賢くならない？
                taskLoadAllPeriod();
            }

            final List<Talk> talkList;
            try{
                loadAnchoredPeriod(village, anchor);
                talkList = village.getTalkListFromAnchor(anchor);
                if(talkList == null || talkList.size() <= 0){
                    updateStatusBar(
                            "アンカーのジャンプ先["
                                    + anchor.toString()
                                    + "]が見つかりません");
                    return;
                }

                Talk targetTalk = talkList.get(0);
                Period targetPeriod = targetTalk.getPeriod();
                int periodIndex = targetPeriod.getDay();
                PeriodView target = browser.getPeriodView(periodIndex);

                EventQueue.invokeLater(() -> {
                    browser.showPeriodTab(periodIndex);
                    target.setPeriod(targetPeriod);
                    target.scrollToTalk(targetTalk);
                });
                updateStatusBar(
                        "アンカー["
                                + anchor.toString()
                                + "]にジャンプしました");
            }catch(IOException e){
                updateStatusBar(
                        "アンカーの展開中にエラーが起きました");
            }

        };

        this.busyStatus.submitHeavyBusyTask(
                task,
                "ジャンプ先の読み込み中…",
                null
        );

        return;
    }

    /**
     * ローカルなXMLファイルを読み込む。
     */
    private void actionOpenXml(){
        int result = this.xmlFileChooser.showOpenDialog(getTopFrame());
        if(result != JFileChooser.APPROVE_OPTION) return;
        File selected = this.xmlFileChooser.getSelectedFile();

        this.busyStatus.submitHeavyBusyTask(() -> {
            Village village;

            try{
                village = VillageLoader.parseVillage(selected);
            }catch(IOException e){
                String warnMsg = MessageFormat.format(
                        "XMLファイル[ {0} ]を読み込むことができません",
                        selected.getPath()
                );
                warnDialog("XML I/O error", warnMsg, e);
                return;
            }catch(SAXException e){
                String warnMsg = MessageFormat.format(
                        "XMLファイル[ {0} ]の形式が不正なため読み込むことができません",
                        selected.getPath()
                );
                warnDialog("XML form error", warnMsg, e);
                return;
            }

            village.setLocalArchive(true);
            AvatarPics avatarPics = village.getAvatarPics();
            this.appSetting.applyLocalImage(avatarPics);
            avatarPics.preload();
            EventQueue.invokeLater(() -> {
                selectedVillage(village);
            });
        }, "XML読み込み中", "XML読み込み完了");

        return;
    }

    /**
     * 指定した国の村一覧を読み込むジョブを投下。
     * @param land 国
     */
    private void submitReloadVillageList(final Land land){
        this.busyStatus.submitHeavyBusyTask(
            () -> {taskReloadVillageList(land);},
            "村一覧を読み込み中…",
            "村一覧の読み込み完了"
        );
        return;
    }

    /**
     * 指定した国の村一覧を読み込む。(ヘビータスク本体).
     * @param land 国
     */
    private void taskReloadVillageList(Land land){
        List<Village> villageList;
        try{
            villageList = VillageListLoader.loadVillageList(land);
        }catch(IOException e){
            showNetworkError(land, e);
            return;
        }
        land.updateVillageList(villageList);

        this.model.updateVillageList(land);

        LandsTree treePanel = this.topView.getLandsTree();
        treePanel.expandLand(land);

        return;
    }

    /**
     * Period表示の更新処理。
     * @param force trueならPeriodデータを強制再読み込み。
     */
    private void updatePeriod(final boolean force){
        Village village = getVillage();
        if(village == null) return;

        String fullName = village.getVillageFullName();
        setFrameTitle(fullName);

        PeriodView periodView = currentPeriodView();
        Discussion discussion = currentDiscussion();
        if(discussion == null) return;

        FilterPanel filterPanel = this.windowManager.getFilterPanel();
        discussion.setTopicFilter(filterPanel);

        Period period = discussion.getPeriod();
        if(period == null) return;

        Runnable task = () -> {
            try{
                PeriodLoader.parsePeriod(period, force);
            }catch(IOException e){
                showNetworkError(village, e);
                return;
            }

            EventQueue.invokeLater(() -> {
                int lastPos = periodView.getVerticalPosition();
                periodView.showTopics();
                periodView.setVerticalPosition(lastPos);
            });
        };

        this.busyStatus.submitHeavyBusyTask(
                task,
                "会話の読み込み中",
                "会話の表示が完了"
        );

        return;
    }

    /**
     * 発言フィルタの操作による更新処理。
     */
    private void filterChanged(){
        final Discussion discussion = currentDiscussion();
        if(discussion == null) return;

        FilterPanel filterPanel = this.windowManager.getFilterPanel();

        discussion.setTopicFilter(filterPanel);
        discussion.filtering();

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
        LOGGER.log(Level.WARNING, "ネットワークで障害が発生しました", e);

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

        String title = VerInfo.getFrameTitle("通信異常発生");
        JDialog dialog = pane.createDialog(title);

        dialog.pack();
        dialog.setVisible(true);
        dialog.dispose();

        return;
    }

    /**
     * 国を選択する。
     *
     * @param land 国
     */
    private void selectedLand(Land land){
        String landName = land.getLandDef().getLandName();
        setFrameTitle(landName);

        this.actionManager.exposeVillage(false);
        this.actionManager.exposePeriod(false);

        this.topView.showLandInfo(land);

        return;
    }

    /**
     * 村を選択する。
     *
     * @param village 村
     */
    private void selectedVillage(Village village){
        setFrameTitle(village.getVillageFullName());
        if(village.isLocalArchive()){
            this.actionManager.exposeVillageLocal(true);
        }else{
            this.actionManager.exposeVillage(true);
        }

        Runnable task = () -> {
            try{
                if( ! village.hasSchedule() ){
                    VillageInfoLoader.updateVillageInfo(village);
                }
            }catch(IOException e){
                showNetworkError(village, e);
                return;
            }

            EventQueue.invokeLater(() -> {
                this.topView.showVillageInfo(village);
            });
        };

        this.busyStatus.submitHeavyBusyTask(
                task,
                "村情報を読み込み中…",
                "村情報の読み込み完了"
        );

        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>主にメニュー選択やボタン押下などのアクションをディスパッチする。
     *
     * <p>ビジーな状態では何もしない。
     *
     * @param ev {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent ev){
        if(this.busyStatus.isBusy()) return;

        String cmd = ev.getActionCommand();
        if(cmd == null) return;

        switch(cmd){
        case ActionManager.CMD_OPENXML:
            actionOpenXml();
            break;
        case ActionManager.CMD_EXIT:
            actionExit();
            break;
        case ActionManager.CMD_COPY:
            actionCopySelected();
            break;
        case ActionManager.CMD_SHOWFIND:
            actionShowFind();
            break;
        case ActionManager.CMD_SEARCHNEXT:
            actionSearchNext();
            break;
        case ActionManager.CMD_SEARCHPREV:
            actionSearchPrev();
            break;
        case ActionManager.CMD_ALLPERIOD:
            actionLoadAllPeriod();
            break;
        case ActionManager.CMD_SHOWDIGEST:
            actionShowDigest();
            break;
        case ActionManager.CMD_WEBVILL:
            actionShowWebVillage();
            break;
        case ActionManager.CMD_WEBWIKI:
            actionShowWebWiki();
            break;
        case ActionManager.CMD_RELOAD:
            actionReloadPeriod();
            break;
        case ActionManager.CMD_DAYSUMMARY:
            actionDaySummary();
            break;
        case ActionManager.CMD_DAYEXPCSV:
            actionDayExportCsv();
            break;
        case ActionManager.CMD_WEBDAY:
            actionShowWebDay();
            break;
        case ActionManager.CMD_OPTION:
            actionOption();
            break;
        case ActionManager.CMD_LANDF:
            actionChangeLaF();
            break;
        case ActionManager.CMD_SHOWFILT:
            actionShowFilter();
            break;
        case ActionManager.CMD_SHOWLOG:
            actionShowLog();
            break;
        case ActionManager.CMD_HELPDOC:
            actionHelp();
            break;
        case ActionManager.CMD_SHOWPORTAL:
            actionShowPortal();
            break;
        case ActionManager.CMD_ABOUT:
            actionAbout();
            break;
        case ActionManager.CMD_VILLAGELIST:
            actionReloadVillageList();
            break;
        case ActionManager.CMD_COPYTALK:
            actionCopyTalk();
            break;
        case ActionManager.CMD_JUMPANCHOR:
            actionJumpAnchor();
            break;
        case ActionManager.CMD_WEBTALK:
            actionShowWebTalk();
            break;
        default:
            break;
        }

        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void anchorHitted(AnchorHitEvent event){
        PeriodView periodView = currentPeriodView();
        if(periodView == null) return;
        Period period = periodView.getPeriod();
        if(period == null) return;
        final Village village = period.getVillage();

        final TalkDraw talkDraw = event.getTalkDraw();
        final Anchor anchor = event.getAnchor();
        final Discussion discussion = periodView.getDiscussion();

        Runnable task = () -> {
            if(anchor.hasTalkNo()){
                // TODO もう少し賢くならない？
                taskLoadAllPeriod();
            }

            final List<Talk> talkList;
            try{
                loadAnchoredPeriod(village, anchor);
                talkList = village.getTalkListFromAnchor(anchor);
                if(talkList == null || talkList.size() <= 0){
                    updateStatusBar(
                            "アンカーの展開先["
                                    + anchor.toString()
                                    + "]が見つかりません");
                    return;
                }
                EventQueue.invokeLater(() -> {
                    talkDraw.showAnchorTalks(anchor, talkList);
                    discussion.layoutRows();
                });
                updateStatusBar(
                        "アンカー["
                                + anchor.toString()
                                + "]の展開完了");
            }catch(IOException e){
                updateStatusBar(
                        "アンカーの展開中にエラーが起きました");
            }
        };

        this.busyStatus.submitHeavyBusyTask(
                task,
                "アンカーの展開中…",
                null
        );

        return;
    }

    /**
     * アプリ正常終了処理。
     */
    private void shutdown(){
        JsonIo jsonIo = this.appSetting.getJsonIo();

        FindPanel findPanel = this.windowManager.getFindPanel();
        JsObject findConf = findPanel.getJson();
        if( ! findPanel.hasConfChanged(findConf) ){
            jsonIo.saveHistoryConfig(findConf);
        }

        this.appSetting.saveConfig();

        LOGGER.info("VMごとアプリケーションを終了します。");
        System.exit(0);  // invoke shutdown hooks... BYE !

        assert false;
        return;
    }


    /**
     * 発言フィルタ操作を監視する。
     */
    private class FilterWatcher implements ChangeListener{

        /**
         * constructor.
         */
        FilterWatcher(){
            super();
            return;
        }


        /**
         * {@inheritDoc}
         *
         * <p>発言フィルタが操作されたときの処理。
         *
         * @param event {@inheritDoc}
         */
        @Override
        public void stateChanged(ChangeEvent event){
            Object source = event.getSource();

            if(source == Controller.this.windowManager.getFilterPanel()){
                filterChanged();
            }

            return;
        }

    }

    /**
     * Period一覧タブのタブ操作を監視する。
     */
    private class TabPeriodWatcher implements ChangeListener{

        /**
         * constructor.
         */
        TabPeriodWatcher(){
            super();
            return;
        }


        /**
         * {@inheritDoc}
         *
         * <p>Periodがタブ選択されたときの処理。
         *
         * @param event {@inheritDoc}
         */
        @Override
        public void stateChanged(ChangeEvent event){
            Object source = event.getSource();

            if(source instanceof TabBrowser){
                updateFindPanel();
                updatePeriod(false);
                PeriodView periodView = currentPeriodView();
                boolean hasCurrentPeriod;
                if(periodView == null) hasCurrentPeriod = false;
                else                   hasCurrentPeriod = true;
                Controller.this.actionManager.exposePeriod(hasCurrentPeriod);
                if(hasCurrentPeriod){
                    Village village = getVillage();
                    if(village.isLocalArchive()){
                        Controller.this.actionManager.exposeVillageLocal(hasCurrentPeriod);
                    }else{
                        Controller.this.actionManager.exposeVillage(hasCurrentPeriod);
                    }
                }
            }

            return;
        }

    }

    /**
     * 国村選択リストの選択展開操作を監視する。
     */
    private class VillageTreeWatcher
            implements TreeSelectionListener, TreeWillExpandListener{

        /**
         * Constructor.
         */
        VillageTreeWatcher(){
            super();
            return;
        }


        /**
         * {@inheritDoc}
         *
         * <p>ツリーリストで何らかの要素（国、村）がクリックされたときの処理。
         *
         * @param event {@inheritDoc}
         */
        @Override
        public void valueChanged(TreeSelectionEvent event){
            TreePath path = event.getNewLeadSelectionPath();
            if(path == null) return;

            Object selObj = path.getLastPathComponent();
            if(selObj instanceof Land){
                Land land = (Land) selObj;
                selectedLand(land);
            }else if(selObj instanceof Village){
                Village village = (Village) selObj;
                village.setLocalArchive(false);
                selectedVillage(village);
            }

            return;
        }

        /**
         * {@inheritDoc}
         *
         * <p>村選択ツリーリストが畳まれるとき呼ばれる。
         *
         * @param event ツリーイベント {@inheritDoc}
         */
        @Override
        public void treeWillCollapse(TreeExpansionEvent event){
            return;
        }

        /**
         * {@inheritDoc}
         *
         * <p>村選択ツリーリストが展開されるとき呼ばれる。
         *
         * @param event ツリーイベント {@inheritDoc}
         */
        @Override
        public void treeWillExpand(TreeExpansionEvent event){
            if(!(event.getSource() instanceof JTree)){
                return;
            }

            TreePath path = event.getPath();
            Object lastObj = path.getLastPathComponent();
            if(!(lastObj instanceof Land)){
                return;
            }
            Land land = (Land) lastObj;
            if(land.getVillageCount() > 0){
                return;
            }

            submitReloadVillageList(land);

            return;
        }

    }

}
