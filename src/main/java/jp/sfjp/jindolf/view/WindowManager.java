/*
 * window manager
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.EventQueue;
import java.awt.Window;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jp.sfjp.jindolf.VerInfo;
import jp.sfjp.jindolf.log.LogFrame;
import jp.sfjp.jindolf.summary.DaySummary;
import jp.sfjp.jindolf.summary.VillageDigest;

/**
 * ウィンドウ群の管理を行う。
 *
 * <p>原則として閉じても再利用されるウィンドウを管理対象とする。
 *
 * <p>管理対象ウィンドウは
 *
 * <ul>
 * <li>アプリのトップウィンドウ
 * <li>検索ウィンドウ
 * <li>フィルタウィンドウ
 * <li>発言集計ウィンドウ
 * <li>村プレイ記録のダイジェストウィンドウ
 * <li>オプション設定ウィンドウ
 * <li>ヘルプウィンドウ
 * <li>ログウィンドウ
 * </ul>
 *
 * <p>である。
 *
 * <p>トップウィンドウとヘルプウィンドウは{@link javax.swing.JFrame}、
 * その他は{@link javax.swing.JDialog}である。
 *
 * <p>非モーダルダイアログは、他のウィンドウの下側にも回れるのが望ましい。
 *
 * <p>各ウィンドウは、他のウィンドウの下に完全に隠れても
 * Windowsタスクバーなどを介して前面に引っ張り出す操作手段を
 * 提供することが望ましい。
 */
public class WindowManager {

    private static final String TITLE_FILTER =
            getFrameTitle("発言フィルタ");
    private static final String TITLE_LOGGER =
            getFrameTitle("ログ表示");
    private static final String TITLE_OPTION =
            getFrameTitle("オプション設定");
    private static final String TITLE_FIND =
            getFrameTitle("発言検索");
    private static final String TITLE_DIGEST =
            getFrameTitle("村のダイジェスト");
    private static final String TITLE_DAYSUMMARY =
            getFrameTitle("発言集計");
    private static final String TITLE_HELP =
            getFrameTitle("ヘルプ");


    private final TopFrame topFrame;

    private FilterPanel filterPanel;
    private LogFrame logFrame;
    private OptionPanel optionPanel;
    private FindPanel findPanel;
    private VillageDigest villageDigest;
    private DaySummary daySummary;
    private HelpFrame helpFrame;

    private final List<Window> windowSet;


    /**
     * コンストラクタ。
     */
    public WindowManager(){
        super();

        this.topFrame = new TopFrame();
        this.topFrame.setVisible(false);

        JOptionPane.setRootFrame(this.topFrame);

        this.windowSet = new LinkedList<>();
        this.windowSet.add(this.topFrame);

        return;
    }


    /**
     * ウィンドウタイトルに前置詞をつける。
     *
     * @param text 元タイトル
     * @return タイトル文字列
     */
    private static String getFrameTitle(String text){
        String result = VerInfo.getFrameTitle(text);
        return result;
    }


    /**
     * 発言フィルタウィンドウを生成する。
     *
     * @return 発言フィルタウィンドウ
     */
    protected FilterPanel createFilterPanel(){
        FilterPanel result;

        result = new FilterPanel();
        result.setTitle(TITLE_FILTER);
        result.pack();
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * 発言フィルタウィンドウを返す。
     *
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
     *
     * @return ログウィンドウ
     */
    protected LogFrame createLogFrame(){
        LogFrame result;

        result = new LogFrame();
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
     *
     * @return ログウィンドウ
     */
    public LogFrame getLogFrame(){
        if(this.logFrame == null){
            this.logFrame = createLogFrame();
        }
        return this.logFrame;
    }

    /**
     * オプション設定ウィンドウを生成する。
     *
     * @return オプション設定ウィンドウ
     */
    protected OptionPanel createOptionPanel(){
        OptionPanel result;

        result = new OptionPanel();
        result.setTitle(TITLE_OPTION);
        result.pack();
        result.setSize(450, 500);
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * オプション設定ウィンドウを返す。
     *
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
     *
     * @return 検索ウィンドウ
     */
    protected FindPanel createFindPanel(){
        FindPanel result;

        result = new FindPanel();
        result.setTitle(TITLE_FIND);
        result.pack();
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * 検索ウィンドウを返す。
     *
     * @return 検索ウィンドウ
     */
    public FindPanel getFindPanel(){
        if(this.findPanel == null){
            this.findPanel = createFindPanel();
        }
        return this.findPanel;
    }

    /**
     * 村ダイジェストウィンドウを生成する。
     *
     * @return 村ダイジェストウィンドウ
     */
    protected VillageDigest createVillageDigest(){
        VillageDigest result;

        result = new VillageDigest();
        result.setTitle(TITLE_DIGEST);
        result.pack();
        result.setSize(600, 550);
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * 村ダイジェストウィンドウを返す。
     *
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
     *
     * @return 発言集計ウィンドウ
     */
    protected DaySummary createDaySummary(){
        DaySummary result;

        result = new DaySummary();
        result.setTitle(TITLE_DAYSUMMARY);
        result.pack();
        result.setSize(400, 500);
        result.setVisible(false);

        this.windowSet.add(result);

        return result;
    }

    /**
     * 発言集計ウィンドウを返す。
     *
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
     *
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
     *
     * @return ヘルプウィンドウ
     */
    public HelpFrame getHelpFrame(){
        if(this.helpFrame == null){
            this.helpFrame = createHelpFrame();
        }
        return this.helpFrame;
    }

    /**
     * トップフレームを返す。
     *
     * @return トップフレーム
     */
    public TopFrame getTopFrame(){
        return this.topFrame;
    }

    /**
     * 管理下にある全ウィンドウのLookAndFeelを更新する。
     *
     * <p>必要に応じて再パッキングが行われる。
     *
     * @param className Look and Feel
     * @throws java.lang.ReflectiveOperationException reflection error
     * @throws javax.swing.UnsupportedLookAndFeelException Unsupported LAF
     */
    public void changeAllWindowUI(String className)
            throws ReflectiveOperationException,
                   UnsupportedLookAndFeelException {
        assert EventQueue.isDispatchThread();

        UIManager.setLookAndFeel(className);

        this.windowSet.forEach(window -> {
            SwingUtilities.updateComponentTreeUI(window);
        });

        if(this.filterPanel != null) this.filterPanel.pack();
        if(this.findPanel   != null) this.findPanel.pack();

        return;
    }

}
