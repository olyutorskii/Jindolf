/*
 * window manager
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import jp.sfjp.jindolf.VerInfo;
import jp.sfjp.jindolf.editor.TalkPreview;
import jp.sfjp.jindolf.log.LogFrame;
import jp.sfjp.jindolf.summary.DaySummary;
import jp.sfjp.jindolf.summary.VillageDigest;

/**
 * ウィンドウ群の管理を行う。
 */
public class WindowManager {

    private static final String TITLE_FILTER =
            getFrameTitle("発言フィルタ");
    private static final String TITLE_LOGGER =
            getFrameTitle("ログ表示");
    private static final String TITLE_EDITOR =
            getFrameTitle("発言エディタ");
    private static final String TITLE_OPTION =
            getFrameTitle("オプション設定");
    private static final String TITLE_FIND =
            getFrameTitle("発言検索");
    private static final String TITLE_ACCOUNT =
            getFrameTitle("アカウント管理");
    private static final String TITLE_DIGEST =
            getFrameTitle("村のダイジェスト");
    private static final String TITLE_DAYSUMMARY =
            getFrameTitle("発言集計");
    private static final String TITLE_HELP =
            getFrameTitle("ヘルプ");

    private static final Frame NULLPARENT = null;


    private FilterPanel filterPanel;
    private LogFrame logFrame;
    private TalkPreview talkPreview;
    private OptionPanel optionPanel;
    private FindPanel findPanel;
    private AccountPanel accountPanel;
    private VillageDigest villageDigest;
    private DaySummary daySummary;
    private HelpFrame helpFrame;
    private TopFrame topFrame;

    private final List<Window> windowSet = new LinkedList<>();


    /**
     * コンストラクタ。
     */
    public WindowManager(){
        super();
        return;
    }


    /**
     * ウィンドウタイトルに前置詞をつける。
     * @param text 元タイトル
     * @return タイトル文字列
     */
    private static String getFrameTitle(String text){
        String result = VerInfo.getFrameTitle(text);
        return result;
    }


    /**
     * 発言フィルタウィンドウを生成する。
     * @return 発言フィルタウィンドウ
     */
    protected FilterPanel createFilterPanel(){
        FilterPanel result;

        result = new FilterPanel(NULLPARENT);
        result.setTitle(TITLE_FILTER);
        result.pack();
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * 発言フィルタウィンドウを返す。
     * @return 発言フィルタウィンドウ
     */
    public FilterPanel getFilterPanel(){
        if(this.filterPanel == null){
            this.filterPanel = createFilterPanel();
        }
        return this.filterPanel;
    }

    /**
     * ログウィンドウを生成する。
     * @return ログウィンドウ
     */
    protected LogFrame createLogFrame(){
        LogFrame result;

        result = new LogFrame(NULLPARENT);
        result.setTitle(TITLE_LOGGER);
        result.pack();
        result.setSize(600, 500);
        result.setLocationByPlatform(true);
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * ログウィンドウを返す。
     * @return ログウィンドウ
     */
    public LogFrame getLogFrame(){
        if(this.logFrame == null){
            this.logFrame = createLogFrame();
        }
        return this.logFrame;
    }

    /**
     * 発言エディタウィンドウを生成する。
     * @return 発言エディタウィンドウ
     */
    protected TalkPreview createTalkPreview(){
        TalkPreview result;

        result = new TalkPreview();
        result.setTitle(TITLE_EDITOR);
        result.pack();
        result.setSize(700, 500);
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * 発言エディタウィンドウを返す。
     * @return 発言エディタウィンドウ
     */
    public TalkPreview getTalkPreview(){
        if(this.talkPreview == null){
            this.talkPreview = createTalkPreview();
        }
        return this.talkPreview;
    }

    /**
     * オプション設定ウィンドウを生成する。
     * @return オプション設定ウィンドウ
     */
    protected OptionPanel createOptionPanel(){
        OptionPanel result;

        result = new OptionPanel(NULLPARENT);
        result.setTitle(TITLE_OPTION);
        result.pack();
        result.setSize(450, 500);
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * オプション設定ウィンドウを返す。
     * @return オプション設定ウィンドウ
     */
    public OptionPanel getOptionPanel(){
        if(this.optionPanel == null){
            this.optionPanel = createOptionPanel();
        }
        return this.optionPanel;
    }

    /**
     * 検索ウィンドウを生成する。
     * @return 検索ウィンドウ
     */
    protected FindPanel createFindPanel(){
        FindPanel result;

        result = new FindPanel(NULLPARENT);
        result.setTitle(TITLE_FIND);
        result.pack();
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * 検索ウィンドウを返す。
     * @return 検索ウィンドウ
     */
    public FindPanel getFindPanel(){
        if(this.findPanel == null){
            this.findPanel = createFindPanel();
        }
        return this.findPanel;
    }

    /**
     * ログインウィンドウを生成する。
     * @return ログインウィンドウ
     */
    protected AccountPanel createAccountPanel(){
        AccountPanel result;

        result = new AccountPanel(NULLPARENT);
        result.setTitle(TITLE_ACCOUNT);
        result.pack();
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * ログインウィンドウを返す。
     * @return ログインウィンドウ
     */
    public AccountPanel getAccountPanel(){
        if(this.accountPanel == null){
            this.accountPanel = createAccountPanel();
        }
        return this.accountPanel;
    }

    /**
     * 村ダイジェストウィンドウを生成する。
     * @return 村ダイジェストウィンドウ
     */
    protected VillageDigest createVillageDigest(){
        VillageDigest result;

        result = new VillageDigest(NULLPARENT);
        result.setTitle(TITLE_DIGEST);
        result.pack();
        result.setSize(600, 550);
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * 村ダイジェストウィンドウを返す。
     * @return 村ダイジェストウィンドウ
     */
    public VillageDigest getVillageDigest(){
        if(this.villageDigest == null){
            this.villageDigest = createVillageDigest();
        }
        return this.villageDigest;
    }

    /**
     * 発言集計ウィンドウを生成する。
     * @return 発言集計ウィンドウ
     */
    protected DaySummary createDaySummary(){
        DaySummary result;

        result = new DaySummary(NULLPARENT);
        result.setTitle(TITLE_DAYSUMMARY);
        result.pack();
        result.setSize(400, 500);
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * 発言集計ウィンドウを返す。
     * @return 発言集計ウィンドウ
     */
    public DaySummary getDaySummary(){
        if(this.daySummary == null){
            this.daySummary = createDaySummary();
        }
        return this.daySummary;
    }

    /**
     * ヘルプウィンドウを生成する。
     * @return ヘルプウィンドウ
     */
    protected HelpFrame createHelpFrame(){
        HelpFrame result;

        result = new HelpFrame();
        result.setTitle(TITLE_HELP);
        result.pack();
        result.setSize(450, 450);
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * ヘルプウィンドウを返す。
     * @return ヘルプウィンドウ
     */
    public HelpFrame getHelpFrame(){
        if(this.helpFrame == null){
            this.helpFrame = createHelpFrame();
        }
        return this.helpFrame;
    }

    /**
     * トップフレームを生成する。
     * @return トップフレーム
     */
    protected TopFrame createTopFrame(){
        TopFrame result = new TopFrame();
        result.setVisible(false);
        this.windowSet.add(result);
        return result;
    }

    /**
     * トップフレームを返す。
     * @return トップフレーム
     */
    public TopFrame getTopFrame(){
        if(this.topFrame == null){
            this.topFrame = createTopFrame();
        }
        return this.topFrame;
    }

    /**
     * 管理下にある全ウィンドウのLookAndFeelを更新する。
     * 必要に応じて再パッキングが行われる。
     */
    public void changeAllWindowUI(){
        for(Window window : this.windowSet){
            updateTreeUI(window);
        }

        if(this.filterPanel  != null) this.filterPanel.pack();
        if(this.findPanel    != null) this.findPanel.pack();
        if(this.accountPanel != null) this.accountPanel.pack();

        return;
    }

    /**
     * 再帰的に下層コンポーネントのLaFを更新する。
     * <p>{@link javax.swing.SwingUtilities#updateComponentTreeUI(Component)}
     * がポップアップメニューのLaF更新を正しく行わないSun製JREのバグ
     * [BugID:6299213]
     * を回避するために作られた。
     * @param comp 開始コンポーネント
     * @see <a href="http://bugs.sun.com/view_bug.do?bug_id=6299213">
     * BugID:6299213
     * </a>
     */
    public static void updateTreeUI(Component comp) {
        updateTreeUI(comp, true);
        return;
    }

    /**
     * 再帰的に下層コンポーネントのLaFを更新する。
     * @param comp 開始コンポーネント
     * @param isRoot このコンポーネントが最上位か否か指定する。
     * trueが指定された場合、LaF更新作業の後に再レイアウトを促す。
     * パフォーマンスの観点から、ポップアップ以外の下層コンポーネントには
     * 必要のない限りfalse指定を推奨。
     */
    public static void updateTreeUI(Component comp, boolean isRoot) {
        if(comp instanceof JComponent){
            JComponent jcomp = (JComponent) comp;
            jcomp.updateUI();

            JPopupMenu popup = jcomp.getComponentPopupMenu();
            if(popup != null){
                updateTreeUI(popup, true);
            }
        }

        if(comp instanceof JMenu){
            JMenu menu = (JMenu) comp;
            for(Component child : menu.getMenuComponents()){
                updateTreeUI(child, false);
            }
        }else if(comp instanceof Container){
            Container cont = (Container) comp;
            for(Component child : cont.getComponents()){
                updateTreeUI(child, false);
            }
        }

        if(isRoot){
            comp.invalidate();
            comp.validate();
            comp.repaint();
        }

        return;
    }

}
