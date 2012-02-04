/*
 * Jindolf main class
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.UIManager;
import jp.sfjp.jindolf.config.AppSetting;
import jp.sfjp.jindolf.config.CmdOption;
import jp.sfjp.jindolf.config.ConfigFile;
import jp.sfjp.jindolf.config.ConfigStore;
import jp.sfjp.jindolf.config.EnvInfo;
import jp.sfjp.jindolf.config.OptionInfo;
import jp.sfjp.jindolf.data.LandsModel;
import jp.sfjp.jindolf.glyph.Discussion;
import jp.sfjp.jindolf.glyph.GlyphDraw;
import jp.sfjp.jindolf.log.LogUtils;
import jp.sfjp.jindolf.log.LogWrapper;
import jp.sfjp.jindolf.log.LoggingDispatcher;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sfjp.jindolf.view.ActionManager;
import jp.sfjp.jindolf.view.TabBrowser;
import jp.sfjp.jindolf.view.TopView;

/**
 * Jindolf スタートアップクラス。
 *
 * コンストラクタは無いよ。
 * アプリ開始はstaticメソッド{@link #main(String[])}呼び出しから。
 */
public final class JindolfOld {

    /** このClass。 */
    public static final Class<?> SELF_KLASS;
    /** クラスローダ。 */
    public static final ClassLoader LOADER;


    /** クラスロード時のナノカウント。 */
    public static final long NANOCT_LOADED;
    /** クラスロード時刻(エポックmsec)。 */
    public static final long EPOCHMS_LOADED;


    /** ロガー。 */
    private static final LogWrapper LOGGER = new LogWrapper();

    /** スプラッシュロゴ。 */
    private static final String RES_LOGOICON =
            "resources/image/logo.png";

    static{
        SELF_KLASS = JindolfOld.class;

        ClassLoader thisLoader;
        try{
            thisLoader = SELF_KLASS.getClassLoader();
        }catch(SecurityException e){
            thisLoader = null;
        }
        LOADER = thisLoader;

        NANOCT_LOADED  = System.nanoTime();
        EPOCHMS_LOADED = System.currentTimeMillis();

        new JindolfOld().hashCode();
    }


    /**
     * 隠れコンストラクタ。
     */
    private JindolfOld(){
        super();
        assert this.getClass() == SELF_KLASS;
        return;
    }


    /**
     * 標準出力端末にヘルプメッセージ（オプションの説明）を表示する。
     */
    private static void showHelpMessage(){
        System.out.flush();
        System.err.flush();

        CharSequence helpText = CmdOption.getHelpText();
        System.out.print(helpText);

        System.out.flush();
        System.err.flush();

        return;
    }

    /**
     * スプラッシュウィンドウを表示する。
     * <p>JRE1.6以降では何も表示しない。
     * @return スプラッシュウィンドウ。JRE1.6以降ならnullを返す。
     */
    @SuppressWarnings("CallToThreadYield")
    private static Window showSplash(){
        if(JreChecker.has16Runtime()) return null;

        Window splashWindow = new JWindow();

        ImageIcon logo = ResourceManager.getImageIcon(RES_LOGOICON);
        JLabel splashLabel = new JLabel(logo);
        splashWindow.add(splashLabel);

        splashWindow.pack();
        splashWindow.setLocationRelativeTo(null); // locate center
        splashWindow.setVisible(true);

        Thread.yield();

        return splashWindow;
    }

    /**
     * スプラッシュウィンドウを隠す。
     * @param splashWindow スプラッシュウィンドウ。nullならなにもしない。
     */
    @SuppressWarnings("CallToThreadYield")
    private static void hideSplash(Window splashWindow){
        if(splashWindow == null) return;

        splashWindow.setVisible(false);
        splashWindow.dispose();

        Thread.yield();

        return;
    }

    /**
     * 起動時の諸々の情報をログ出力する。
     * @param optinfo コマンドライン情報
     * @param configStore 設定ディレクトリ情報
     */
    private static void dumpBootInfo(OptionInfo optinfo,
                                       ConfigStore configStore ){
        DateFormat dform = DateFormat.getDateTimeInstance();
        NumberFormat nform = NumberFormat.getNumberInstance();

        LOGGER.info(
                VerInfo.ID + " は "
                + dform.format(new Date(EPOCHMS_LOADED))
                + " にVM上のクラス "
                + SELF_KLASS.getName() + " としてロードされました。 " );

        LOGGER.info("Initial Nano-Count : " + nform.format(NANOCT_LOADED));

        Runtime runtime = Runtime.getRuntime();
        LOGGER.info(
                "Max-heap : "
                + nform.format(runtime.maxMemory()) + " Byte"
                + "   Total-heap : "
                + nform.format(runtime.totalMemory()) + " Byte");

        StringBuilder bootArgs = new StringBuilder();
        bootArgs.append("\n\n").append("起動時引数:\n");
        for(String arg : optinfo.getInvokeArgList()){
            bootArgs.append("\u0020\u0020").append(arg).append('\n');
        }
        bootArgs.append('\n');
        bootArgs.append(EnvInfo.getVMInfo());
        LOGGER.info(bootArgs);

        if(configStore.useStoreFile()){
            LOGGER.info("設定格納ディレクトリに[ "
                    + configStore.getConfigPath().getPath()
                    + " ]が指定されました。");
        }else{
            LOGGER.info("設定格納ディレクトリは使いません。");
        }

        if(   JreChecker.has16Runtime()
           && optinfo.hasOption(CmdOption.OPT_NOSPLASH) ){
            LOGGER.warn(
                      "JRE1.6以降では、"
                    +"Jindolfの-nosplashオプションは無効です。"
                    + "Java実行系の方でスプラッシュ画面の非表示を"
                    + "指示してください(おそらく空の-splash:オプション)" );
        }

        if(LOADER == null){
            LOGGER.warn(
                    "セキュリティ設定により、"
                    +"クラスローダを取得できませんでした");
        }

        return;
    }

    /**
     * 任意のクラス群に対して一括ロード／初期化を単一スレッドで順に行う。
     * どーしてもクラス初期化の順序に依存する障害が発生する場合や
     * クラス初期化のオーバーヘッドでGUIの操作性が損なわれるときなどにどうぞ。
     *
     * @throws java.lang.LinkageError クラス間リンケージエラー。
     * @throws java.lang.ExceptionInInitializerError クラス初期化で異常
     */
    private static void preInitClass()
            throws LinkageError,
                   ExceptionInInitializerError {
        Object[] classes = {            // Class型 または String型
            "java.lang.Object",
            TabBrowser.class,
            Discussion.class,
            GlyphDraw.class,
            java.net.HttpURLConnection.class,
            java.text.SimpleDateFormat.class,
            Void.class,
        };

        for(Object obj : classes){
            String className;
            if(obj instanceof Class){
                className = ((Class<?>)obj).getName();
            }else if(obj instanceof String){
                className = obj.toString();
            }else{
                continue;
            }

            try{
                if(LOADER != null){
                    Class.forName(className, true, LOADER);
                }else{
                    Class.forName(className);
                }
            }catch(ClassNotFoundException e){
                LOGGER.warn("クラスの明示的ロードに失敗しました", e);
                continue;
            }
        }

        return;
    }

    /**
     * JindolfOld のスタートアップエントリ。
     *
     * @param args コマンドライン引数
     */
    static void main(String... args){
        OptionInfo optinfo;

        try{
            optinfo = OptionInfo.parseOptions(args);
        }catch(IllegalArgumentException e){
            String message = e.getLocalizedMessage();
            System.err.println(message);
            System.err.println(
                "起動オプション一覧は、"
                + "起動オプションに「"
                + CmdOption.OPT_HELP.toString()
                + "」を指定すると確認できます。" );
            System.exit(1);
            assert false;
            return;
        }

        main(optinfo);

        return;
    }

    /**
     * JindolfOld のスタートアップエントリ。
     *
     * @param optinfo コマンドライン引数情報
     */
    static void main(OptionInfo optinfo){
        if(optinfo.hasOption(CmdOption.OPT_HELP)){
            showHelpMessage();
            System.exit(0);
            assert false;
            return;
        }

        if(optinfo.hasOption(CmdOption.OPT_VERSION)){
            System.out.println(VerInfo.ID);
            System.exit(0);
            assert false;
            return;
        }

        // ここ以降、アプリウィンドウの生成と表示に向けてまっしぐら。

        // あらゆるSwing文字列表示処理より前に必要。
        // システムプロパティ swing.boldMetal は無視される。
        Boolean boldFlag;
        if(optinfo.hasOption(CmdOption.OPT_BOLDMETAL)){
            // もの凄く日本語表示が汚くなるかもよ！注意
            boldFlag = Boolean.TRUE;
        }else{
            boldFlag = Boolean.FALSE;
        }
        UIManager.put("swing.boldMetal", boldFlag);

        // JRE1.5用スプラッシュウィンドウ
        Window splashWindow = null;
        if( ! optinfo.hasOption(CmdOption.OPT_NOSPLASH) ){
            splashWindow = showSplash();
        }

        boolean hasError = false;
        try{
            hasError = splashedMain(optinfo);
        }finally{
            hideSplash(splashWindow);
        }

        if(hasError) System.exit(1);

        return;
    }

    /**
     * JindolfOld のスタートアップエントリ。
     * <p>スプラッシュウィンドウが出ている状態。
     * @param optinfo コマンドライン引数情報
     * @return エラーがあればtrue
     */
    static boolean splashedMain(OptionInfo optinfo){
        final AppSetting appSetting = new AppSetting();
        appSetting.applyOptionInfo(optinfo);

        if(optinfo.hasOption(CmdOption.OPT_VMINFO)){
            System.out.println(EnvInfo.getVMInfo());
        }

        LogUtils.initRootLogger(optinfo.hasOption(CmdOption.OPT_CONSOLELOG));
        // ここからロギング解禁

        ConfigStore configStore = appSetting.getConfigStore();
        dumpBootInfo(optinfo, configStore);

        ConfigFile.setupConfigDirectory(configStore);
        ConfigFile.setupLockFile(configStore);
        // ここから設定格納ディレクトリ解禁

        appSetting.loadConfig();

        final Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(){
            @Override
            @SuppressWarnings("CallToThreadYield")
            public void run(){
                LOGGER.info("シャットダウン処理に入ります…");
                System.out.flush();
                System.err.flush();
                runtime.gc();
                Thread.yield();
                runtime.runFinalization(); // 危険？
                Thread.yield();
                return;
            }
        });

        preInitClass();

        LoggingDispatcher.replaceEventQueue();

        boolean hasError = false;
        try{
            EventQueue.invokeAndWait(new Runnable(){
                public void run(){
                    startGUI(appSetting);
                    return;
                }
            });
        }catch(InvocationTargetException e){
            LOGGER.fatal("アプリケーション初期化に失敗しました", e);
            e.printStackTrace(System.err);
            hasError = true;
        }catch(InterruptedException e){
            LOGGER.fatal("アプリケーション初期化に失敗しました", e);
            e.printStackTrace(System.err);
            hasError = true;
        }

        return hasError;
    }

    /**
     * AWTイベントディスパッチスレッド版スタートアップエントリ。
     * @param appSetting アプリ設定
     */
    private static void startGUI(AppSetting appSetting){
        LandsModel model = new LandsModel();
        model.loadLandList();

        JFrame topFrame = buildMVC(appSetting, model);

        GUIUtils.modifyWindowAttributes(topFrame, true, false, true);

        topFrame.pack();

        Dimension initGeometry =
                new Dimension(appSetting.initialFrameWidth(),
                              appSetting.initialFrameHeight());
        topFrame.setSize(initGeometry);

        if(   appSetting.initialFrameXpos() <= Integer.MIN_VALUE
           || appSetting.initialFrameYpos() <= Integer.MIN_VALUE ){
            topFrame.setLocationByPlatform(true);
        }else{
            topFrame.setLocation(appSetting.initialFrameXpos(),
                                 appSetting.initialFrameYpos() );
        }

        topFrame.setVisible(true);

        return;
    }

    /**
     * モデル・ビュー・コントローラの結合。
     * @param appSetting アプリ設定
     * @param model 最上位のデータモデル
     * @return アプリケーションのトップフレーム
     */
    private static JFrame buildMVC(AppSetting appSetting, LandsModel model){
        ActionManager actionManager = new ActionManager();
        TopView topView = new TopView();

        Controller controller =
                new Controller(appSetting, actionManager, topView, model);

        JFrame topFrame = controller.createTopFrame();

        return topFrame;
    }

}
