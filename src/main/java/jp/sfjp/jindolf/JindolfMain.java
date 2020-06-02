/*
 * Jindolf main class
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.UIManager;
import jp.sfjp.jindolf.config.AppSetting;
import jp.sfjp.jindolf.config.CmdOption;
import jp.sfjp.jindolf.config.ConfigDirUi;
import jp.sfjp.jindolf.config.ConfigStore;
import jp.sfjp.jindolf.config.EnvInfo;
import jp.sfjp.jindolf.config.FileUtils;
import jp.sfjp.jindolf.config.OptionInfo;
import jp.sfjp.jindolf.data.LandsTreeModel;
import jp.sfjp.jindolf.log.LogUtils;
import jp.sfjp.jindolf.log.LoggingDispatcher;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sfjp.jindolf.view.ActionManager;
import jp.sfjp.jindolf.view.WindowManager;

/**
 * Jindolf スタートアップクラス。
 *
 * <p>{@link JindolfJre18}の下請けとして本格的なJindolf起動処理に入る。
 */
public final class JindolfMain {

    /** クラスロード時のナノカウント。 */
    public static final long NANOCT_LOADED;
    /** クラスロード時刻(エポックmsec)。 */
    public static final long EPOCHMS_LOADED;


    /** ロガー。 */
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private static final String LOG_LOADED =
              "{0} は {1,date} {2,time} に"
            + "VM上のクラス {3} としてロードされました。 ";
    private static final String LOG_NANOCT =
            "Initial Nano-Count : {0}";
    private static final String LOG_HEAP =
            "Max-heap : {0} Bytes   Total-heap : {1} Bytes";
    private static final String LOG_CONF =
            "設定格納ディレクトリに[ {0} ]が指定されました。";
    private static final String LOG_NOCONF =
            "設定格納ディレクトリは使いません。";
    private static final String FATALMSG_INITFAIL =
            "アプリケーション初期化に失敗しました";
    private static final String ERRMSG_HELP =
              "起動オプション一覧は、"
            + "起動オプションに「{0}」を指定すると確認できます。";

    private static final PrintStream STDOUT = System.out;
    private static final PrintStream STDERR = System.err;

    static{
        NANOCT_LOADED  = System.nanoTime();
        EPOCHMS_LOADED = System.currentTimeMillis();
    }


    /**
     * 隠れコンストラクタ。
     */
    private JindolfMain(){
        assert false;
    }


    /**
     * 標準出力および標準エラー出力をフラッシュする。
     */
    private static void flush(){
        STDOUT.flush();
        STDERR.flush();
        return;
    }

    /**
     * 標準出力端末にヘルプメッセージ（オプションの説明）を表示する。
     */
    private static void showHelpMessage(){
        flush();

        String helpText = CmdOption.getHelpText();
        STDOUT.print(helpText);

        flush();

        return;
    }

    /**
     * 起動時の諸々の情報をログ出力する。
     * @param appSetting  アプリ設定
     */
    private static void dumpBootInfo(AppSetting appSetting){
        Object[] logArgs;

        logArgs = new Object[]{
            VerInfo.ID,
            EPOCHMS_LOADED,
            EPOCHMS_LOADED,
            Jindolf.class.getName(),
        };
        LOGGER.log(Level.INFO, LOG_LOADED, logArgs);

        LOGGER.log(Level.INFO, LOG_NANOCT, NANOCT_LOADED);

        Runtime runtime = Runtime.getRuntime();
        logArgs = new Object[]{
            runtime.maxMemory(),
            runtime.totalMemory(),
        };
        LOGGER.log(Level.INFO, LOG_HEAP, logArgs);

        OptionInfo optinfo = appSetting.getOptionInfo();
        StringBuilder bootArgs = new StringBuilder();
        bootArgs.append("\n\n").append("起動時引数:\n");
        for(String arg : optinfo.getInvokeArgList()){
            bootArgs.append("\u0020\u0020").append(arg).append('\n');
        }
        bootArgs.append('\n');
        bootArgs.append(EnvInfo.getVMInfo());
        LOGGER.info(bootArgs.toString());

        ConfigStore configStore = appSetting.getConfigStore();
        if(configStore.useStoreFile()){
            LOGGER.log(Level.INFO, LOG_CONF, configStore.getConfigDir());
        }else{
            LOGGER.info(LOG_NOCONF);
        }

        if(FileUtils.isWindowsOSFs()){
            LOGGER.info("Windows環境と認識されました。");
        }

        if(FileUtils.isMacOSXFs()){
            LOGGER.info("macOS環境と認識されました。");
        }

        Locale locale = Locale.getDefault();
        LOGGER.log(Level.INFO, "ロケールに{0}が用いられます。", locale.toString());

        return;
    }

    /**
     * JindolfMain のスタートアップエントリ。
     * @param args コマンドライン引数
     * @return 起動に成功すれば0。失敗したら0以外。
     */
    public static int main(String... args){
        OptionInfo optinfo;
        int exitCode;

        try{
            optinfo = OptionInfo.parseOptions(args);
        }catch(IllegalArgumentException e){
            String message = e.getLocalizedMessage();
            STDERR.println(message);

            String info =
                    MessageFormat.format(ERRMSG_HELP, CmdOption.OPT_HELP);
            STDERR.println(info);

            exitCode = 1;
            return exitCode;
        }

        exitCode = main(optinfo);

        return exitCode;
    }

    /**
     * JindolfMain のスタートアップエントリ。
     * @param optinfo コマンドライン引数情報
     * @return 起動に成功すれば0。失敗したら0以外。
     */
    public static int main(OptionInfo optinfo){
        int exitCode;

        if(optinfo.hasOption(CmdOption.OPT_HELP)){
            showHelpMessage();
            exitCode = 0;
            return exitCode;
        }

        if(optinfo.hasOption(CmdOption.OPT_VERSION)){
            STDOUT.println(VerInfo.ID);
            exitCode = 0;
            return exitCode;
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

        exitCode = splashedMain(optinfo);

        return exitCode;
    }

    /**
     * JindolfMain のスタートアップエントリ。
     *
     * <p>JRE1.5までの間、
     * スプラッシュウィンドウが出ている状態として想定されていた。
     * 時間のかかる初期化処理はなるべくこの中へ。
     *
     * @param optinfo コマンドライン引数情報
     * @return 起動に成功すれば0。失敗したら0以外。
     */
    public static int splashedMain(OptionInfo optinfo){
        if(optinfo.hasOption(CmdOption.OPT_VMINFO)){
            STDOUT.println(EnvInfo.getVMInfo());
        }

        LogUtils.initRootLogger(optinfo.hasOption(CmdOption.OPT_CONSOLELOG));
        // ここからロギング解禁

        final AppSetting appSetting = new AppSetting(optinfo);
        dumpBootInfo(appSetting);

        ConfigStore configStore = appSetting.getConfigStore();
        ConfigDirUi.prepareConfigDir(configStore);
        ConfigDirUi.tryLock(configStore);
        // ここから設定格納ディレクトリ解禁

        appSetting.loadConfig();

        final Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(){
            /** {@inheritDoc} */
            @Override
            @SuppressWarnings("CallToThreadYield")
            public void run(){
                LOGGER.info("シャットダウン処理に入ります…");
                flush();
                runtime.gc();
                Thread.yield();
                runtime.runFinalization(); // 危険？
                Thread.yield();
                return;
            }
        });

        LoggingDispatcher.replaceEventQueue();

        int exitCode = 0;
        try{
            EventQueue.invokeAndWait(new Runnable(){
                /** {@inheritDoc} */
                @Override
                public void run(){
                    startGUI(appSetting);
                    return;
                }
            });
        }catch(InvocationTargetException | InterruptedException e){
            LOGGER.log(Level.SEVERE, FATALMSG_INITFAIL, e);
            e.printStackTrace(STDERR);
            exitCode = 1;
        }

        return exitCode;
    }

    /**
     * AWTイベントディスパッチスレッド版スタートアップエントリ。
     * @param appSetting アプリ設定
     */
    private static void startGUI(AppSetting appSetting){
        JFrame topFrame = buildMVC(appSetting);

        GUIUtils.modifyWindowAttributes(topFrame, true, false, true);

        topFrame.pack();

        Dimension initGeometry =
                new Dimension(appSetting.initialFrameWidth(),
                              appSetting.initialFrameHeight());
        topFrame.setSize(initGeometry);

        if(    appSetting.initialFrameXpos() <= Integer.MIN_VALUE
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
     * @return アプリケーションのトップフレーム
     */
    private static JFrame buildMVC(AppSetting appSetting){
        LandsTreeModel model = new LandsTreeModel();
        WindowManager windowManager = new WindowManager();
        ActionManager actionManager = new ActionManager();

        Controller controller = new Controller(model,
                                               windowManager,
                                               actionManager,
                                               appSetting );

        JFrame topFrame = controller.getTopFrame();

        return topFrame;
    }

}
