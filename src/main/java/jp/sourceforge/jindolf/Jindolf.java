/*
 * Jindolf main class
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.security.Permission;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LoggingPermission;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.UIManager;

/**
 * Jindolf スタートアップクラス。
 *
 * コンストラクタは無いよ。
 * アプリ開始はstaticメソッド{@link #main(String[])}呼び出しから。
 */
public final class Jindolf{

    /** 実行に最低限必要なJREの版数。 */
    public static final String MINIMUM_JREVER = "1.5";


    /** このClass。 */
    public static final Class<?>        SELF_KLASS;
    /** このPackage。 */
    public static final Package         SELF_PACKAGE;
    /** ランタイムPackage。 */
    public static final Package         JRE_PACKAGE;
    /** 実行環境。 */
    public static final Runtime         RUNTIME;
    /** セキュリティマネージャ。 */
    public static final SecurityManager SEC_MANAGER;
    /** クラスローダ。 */
    public static final ClassLoader     LOADER;


    /** クラスロード時のナノカウント。 */
    public static final long NANOCT_LOADED;
    /** クラスロード時刻(エポックmsec)。 */
    public static final long EPOCHMS_LOADED;


    /** バージョン定義リソース。 */
    private static final String RES_VERDEF = "resources/version.properties";

    /** タイトル。 */
    public static final String TITLE;
    /** バージョン。 */
    public static final String VERSION;
    /** 作者名。 */
    public static final String AUTHOR;
    /** 著作権表記。 */
    public static final String COPYRIGHT;
    /** ライセンス表記。 */
    public static final String LICENSE;
    /** 連絡先。 */
    public static final String CONTACT;
    /** 初出。 */
    public static final String DEBUT;
    /** その他、何でも書きたいこと。 */
    public static final String COMMENT;
    /** クレジット。 */
    public static final String ID;

    /** 共通ロガー。 */
    private static final LogWrapper COMMON_LOGGER;

    /** 多重起動防止用セマフォ。 */
    private static final AtomicBoolean INVOKE_FLAG;

    /** スプラッシュロゴ。 */
    private static final String RES_LOGOICON =
            "resources/image/logo.png";

    private static OptionInfo option;
    private static AppSetting setting;

    static{
        SELF_KLASS   = Jindolf.class;
        SELF_PACKAGE = SELF_KLASS.getPackage();
        JRE_PACKAGE  = java.lang.Object.class.getPackage();
        RUNTIME      = Runtime.getRuntime();
        SEC_MANAGER  = System.getSecurityManager();

        ClassLoader thisLoader;
        try{
            thisLoader = SELF_KLASS.getClassLoader();
        }catch(SecurityException e){
            thisLoader = null;
        }
        LOADER = thisLoader;

        if( ! JRE_PACKAGE.isCompatibleWith(MINIMUM_JREVER) ){
            String jreInstalled;
            try{
                jreInstalled = System.getProperty("java.home");
            }catch(SecurityException e){
                jreInstalled = "※インストール位置不明";
            }
            String errmsg =
                    "今このプログラム " + SELF_KLASS.getName() + " は\n"
                    +"[ " + jreInstalled
                    +" ]\nにインストールされた"
                    +" JRE" + JRE_PACKAGE.getSpecificationVersion()
                    +" の実行環境で実行されようとしました。\n"
                    +"しかしこのプログラムの実行には"
                    +" JRE" + MINIMUM_JREVER
                    + " 以降の実行環境が必要です。\n"
                    +"おそらく http://www.java.com/ などからの"
                    +"入手が可能でしょう。";

            errorDialog("実行系の不備", errmsg);

            RUNTIME.exit(1);
        }

        // ここからJRE1.5解禁。

        NANOCT_LOADED  = System.nanoTime();
        EPOCHMS_LOADED = System.currentTimeMillis();

        Properties verProp = loadVersionDefinition(SELF_KLASS);
        TITLE   = getPackageInfo(verProp, "pkg-title.",   "Unknown");
        VERSION = getPackageInfo(verProp, "pkg-version.", "0");
        AUTHOR  = getPackageInfo(verProp, "pkg-author.",  "nobody");
        LICENSE = getPackageInfo(verProp, "pkg-license.", "Unknown");
        CONTACT = getPackageInfo(verProp, "pkg-contact.", "Unknown");
        DEBUT   = getPackageInfo(verProp, "pkg-debut.",   "2008");
        COMMENT = getPackageInfo(verProp, "pkg-comment.", "");
        COPYRIGHT = "Copyright(c)" +"\u0020"+ DEBUT +"\u0020"+ AUTHOR;
        ID = TITLE
            +"\u0020"+ "Ver." + VERSION
            +"\u0020"+ COPYRIGHT
            +"\u0020"+ "("+ LICENSE +")";

        Logger jre14Logger = Logger.getLogger(SELF_PACKAGE.getName());
        COMMON_LOGGER = new LogWrapper(jre14Logger);

        INVOKE_FLAG = new AtomicBoolean(false);

        new Jindolf();
    }

    /**
     * 起動オプション情報を返す。
     * @return 起動オプション情報
     */
    public static OptionInfo getOptionInfo(){
        return option;
    }

    /**
     * アプリ設定を返す。
     * @return アプリ設定
     */
    public static AppSetting getAppSetting(){
        return setting;
    }

    /**
     * エラーダイアログをビットマップディスプレイに出現させる。
     * メインウィンドウが整備されるまでの間だけ一時的に使う。
     * 努力目標：なるべく昔のJRE環境でも例外無く動くように。
     * @param title タイトル
     * @param message メッセージ
     */
    private static void errorDialog(String title, String message){
        System.err.println(message);
        System.err.flush();

        if( ! JRE_PACKAGE.isCompatibleWith("1.2") ){
            return;
        }

        if(   JRE_PACKAGE.isCompatibleWith("1.4")
           && GraphicsEnvironment.isHeadless()){
            return;
        }

        JOptionPane.showMessageDialog(null,
                                      message,
                                      title,
                                      JOptionPane.ERROR_MESSAGE);

        return;
    }

    /**
     * リソース上のパッケージ定義プロパティをロードする。
     * MANIFEST.MFが参照できない実行環境での代替品。
     * @param klass パッケージを構成する任意のクラス
     * @return プロパティ
     */
    private static Properties loadVersionDefinition(Class klass){
        Properties result = new Properties();

        InputStream istream = klass.getResourceAsStream(RES_VERDEF);
        try{
            result.load(istream);
        }catch(IOException e){
            return result;
        }finally{
            try{
                istream.close();
            }catch(IOException e){
                return result;
            }
        }

        return result;
    }

    /**
     * リソース上のプロパティから
     * このクラスのパッケージのパッケージ情報を取得する。
     * MANIFEST.MFが参照できない実行環境での代替品。
     * @param prop プロパティ
     * @param prefix 接頭辞
     * @param defValue 見つからなかった場合のデフォルト値
     * @return パッケージ情報
     */
    public static String getPackageInfo(Properties prop,
                                          String prefix,
                                          String defValue){
        return getPackageInfo(prop, SELF_PACKAGE, prefix, defValue);
    }

    /**
     * リソース上のプロパティからパッケージ情報を取得する。
     * MANIFEST.MFが参照できない実行環境での代替品。
     * @param prop プロパティ
     * @param pkg 任意のパッケージ
     * @param prefix 接頭辞
     * @param defValue デフォルト値
     * @return 見つからなかった場合のパッケージ情報
     */
    public static String getPackageInfo(Properties prop,
                                          Package pkg,
                                          String prefix,
                                          String defValue){
        String propName = prefix + pkg.getName();
        String result = prop.getProperty(propName, defValue);
        return result;
    }

    /**
     * Jindolf の実行が可能なGUI環境でなければ、即時VM実行を終了する。
     */
    private static void checkGUIEnvironment(){
        if(GraphicsEnvironment.isHeadless()){
            System.err.println(
                    TITLE
                    + " はGUI環境と接続できませんでした");

            String dispEnv;
            try{
                dispEnv = System.getenv("DISPLAY");
            }catch(SecurityException e){
                dispEnv = null;
            }

            // for X11 user
            if(dispEnv != null){
                System.err.println("環境変数 DISPLAY : " + dispEnv);
            }

            RUNTIME.exit(1);
        }

        return;
    }

    /**
     * コンパイル時のエラーを判定する。
     * ※ 非Unicode系の開発環境を使いたい人は適当に無視してね。
     */
    private static void checkCompileError(){
        String errmsg =
                "ソースコードの文字コードが"
               +"正しくコンパイルされていないかも。\n"
               +"あなたは今、オリジナル開発元の意図しない文字コード環境で"
               +"コンパイルされたプログラムを起動しようとしているよ。\n"
               +"ソースコードの入手に際して"
               +"どのような文字コード変換が行われたか認識しているかな？\n"
               +"コンパイルオプションで正しい文字コードを指定したかな？";

        if(   '狼' != 0x72fc
           || '　' != 0x3000
           || '~'  != 0x007e
           || '\\' != 0x005c  // バックスラッシュ
           || '¥'  != 0x00a5  // 半角円通貨
           || '～' != 0xff5e
           || '�' != 0xfffd  // Unicode専用特殊文字
           ){
            JOptionPane.showMessageDialog(null,
                                          errmsg,
                                          "コンパイルの不備",
                                          JOptionPane.ERROR_MESSAGE);
            RUNTIME.exit(1);
        }
        return;
    }

    /**
     * MANIFEST.MFパッケージ定義エラーの検出。
     * ビルド前にMANIFEST自動生成Antタスク「manifest」を忘れてないかい？
     */
    private static void checkPackageDefinition(){
        String implTitle   = SELF_PACKAGE.getImplementationTitle();
        String implVersion = SELF_PACKAGE.getImplementationVersion();
        String implVendor  = SELF_PACKAGE.getImplementationVendor();

        String errmsg = null;

        if(   implTitle != null
           && ! implTitle.equals(TITLE) ){
            errmsg = "パッケージ定義とタイトルが一致しません。"
                    +"["+ implTitle +"]≠["+ TITLE +"]";
        }else if(   implVersion != null
                 && ! implVersion.equals(VERSION) ){
            errmsg = "パッケージ定義とバージョン番号が一致しません。"
                    +"["+ implVersion +"]≠["+ VERSION +"]";
        }else if(   implVendor != null
                 && ! implVendor.equals(AUTHOR) ){
            errmsg = "パッケージ定義とベンダが一致しません。"
                    +"["+ implVendor +"]≠["+ AUTHOR +"]";
        }

        if(errmsg != null){
            JOptionPane.showMessageDialog(null,
                                          errmsg,
                                          "ビルドエラー",
                                          JOptionPane.ERROR_MESSAGE);
            RUNTIME.exit(1);
        }

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
     * スプラッシュウィンドウを作成する。
     * JRE1.6以降では呼ばれないはず。
     * @return 未表示のスプラッシュウィンドウ。
     */
    private static Window createSplashWindow(){
        Window splashWindow = new JWindow();

        URL url = getResource(RES_LOGOICON);
        ImageIcon logo = new ImageIcon(url);
        JLabel splashLabel = new JLabel(logo);

        splashWindow.add(splashLabel);
        splashWindow.pack();
        splashWindow.setLocationRelativeTo(null); // locate center

        return splashWindow;
    }

    /**
     * ロギング初期化。
     * @param useConsoleLog trueならConsoleHandlerを使う。
     */
    private static void initLogging(boolean useConsoleLog){
        boolean hasPermission = hasLoggingPermission();

        if( ! hasPermission){
            System.out.println(
                      "セキュリティ設定により、"
                    + "ログ設定を変更できませんでした" );
        }

        Logger jre14Logger = COMMON_LOGGER.getJre14Logger();

        if(hasPermission){
            jre14Logger.setUseParentHandlers(false);
            Handler pileHandler = new PileHandler();
            jre14Logger.addHandler(pileHandler);
        }

        if(hasPermission && useConsoleLog){
            Handler consoleHandler = new ConsoleHandler();
            jre14Logger.addHandler(consoleHandler);
        }

        return;
    }

    /**
     * ログ操作のアクセス権があるか否か判定する。
     * @return アクセス権があればtrue
     */
    public static boolean hasLoggingPermission(){
        if(SEC_MANAGER == null) return true;

        Permission logPermission = new LoggingPermission("control", null);
        try{
            SEC_MANAGER.checkPermission(logPermission);
        }catch(SecurityException e){
            return false;
        }

        return true;
    }

    /**
     * 起動時の諸々の情報をログ出力する。
     */
    private static void dumpBootInfo(){
        DateFormat dform = DateFormat.getDateTimeInstance();
        NumberFormat nform = NumberFormat.getNumberInstance();

        logger().info(
                ID + " は "
                + dform.format(new Date(EPOCHMS_LOADED))
                + " にVM上のクラス "
                + SELF_KLASS.getName() + " としてロードされました。 " );

        logger().info("Initial Nano-Count : " + nform.format(NANOCT_LOADED));

        logger().info(
                "Max-heap : "
                + nform.format(RUNTIME.maxMemory()) + " Byte"
                + "   Total-heap : "
                + nform.format(RUNTIME.totalMemory()) + " Byte");

        logger().info("\n" + EnvInfo.getVMInfo());

        if(getAppSetting().useConfigPath()){
            logger().info("設定格納ディレクトリに[ "
                    + getAppSetting().getConfigPath().getPath()
                    + " ]が指定されました。");
        }else{
            logger().info("設定格納ディレクトリは使いません。");
        }

        if(   JRE_PACKAGE.isCompatibleWith("1.6")
           && option.hasOption(CmdOption.OPT_NOSPLASH) ){
            logger().warn(
                      "JRE1.6以降では、"
                    +"Jindolfの-nosplashオプションは無効です。"
                    + "Java実行系の方でスプラッシュ画面の非表示を"
                    + "指示してください(おそらく空の-splash:オプション)" );
        }

        if(LOADER == null){
            logger().warn(
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
                logger().warn("クラスの明示的ロードに失敗しました", e);
                continue;
            }
        }

        return;
    }

    /**
     * AWTイベントディスパッチスレッド版スタートアップエントリ。
     */
    private static void startGUI(){
        LandsModel model = new LandsModel();
        model.loadLandList();

        JFrame topFrame = buildMVC(model);

        GUIUtils.modifyWindowAttributes(topFrame, true, false, true);

        topFrame.pack();

        Dimension initGeometry =
                new Dimension(setting.initialFrameWidth(),
                              setting.initialFrameHeight());
        topFrame.setSize(initGeometry);

        if(   setting.initialFrameXpos() <= Integer.MIN_VALUE
           || setting.initialFrameYpos() <= Integer.MIN_VALUE ){
            topFrame.setLocationByPlatform(true);
        }else{
            topFrame.setLocation(setting.initialFrameXpos(),
                                 setting.initialFrameYpos() );
        }

        topFrame.setVisible(true);

        return;
    }

    /**
     * モデル・ビュー・コントローラの結合。
     *
     * @param model 最上位のデータモデル
     * @return アプリケーションのトップフレーム
     */
    private static JFrame buildMVC(LandsModel model){
        ActionManager actionManager = new ActionManager();
        TopView topView = new TopView();

        Controller controller = new Controller(actionManager, topView, model);

        JFrame topFrame = controller.createTopFrame();

        return topFrame;
    }

    /**
     * リソースからUTF-8で記述されたテキストデータをロードする。
     * @param resourceName リソース名
     * @return テキスト文字列
     * @throws java.io.IOException 入出力の異常。おそらくビルドミス。
     */
    public static CharSequence loadResourceText(String resourceName)
            throws IOException{
        InputStream is;
        is = getResourceAsStream(resourceName);
        is = new BufferedInputStream(is);
        Reader reader = new InputStreamReader(is, "UTF-8");
        LineNumberReader lineReader = new LineNumberReader(reader);

        StringBuilder result = new StringBuilder();
        try{
            for(;;){
                String line = lineReader.readLine();
                if(line == null) break;
                if(line.startsWith("#")) continue;
                result.append(line).append('\n');
            }
        }finally{
            lineReader.close();
        }

        return result;
    }

    /**
     * クラスローダを介してリソースからの入力を生成する。
     * @param name リソース名
     * @return リソースからの入力
     */
    public static InputStream getResourceAsStream(String name){
        return SELF_KLASS.getResourceAsStream(name);
    }

    /**
     * クラスローダを介してリソース読み込み用URLを生成する。
     * @param name リソース名
     * @return URL
     */
    public static URL getResource(String name){
        return SELF_KLASS.getResource(name);
    }

    /**
     * 共通ロガーを取得する。
     * @return 共通ロガー
     */
    public static LogWrapper logger(){
        return COMMON_LOGGER;
    }

    /**
     * VMごとプログラムを終了する。
     * ※おそらく随所でシャットダウンフックが起動されるはず。
     *
     * @param exitCode 終了コード
     * @throws java.lang.SecurityException セキュリティ違反
     */
    public static void exit(int exitCode) throws SecurityException{
        logger().info(
                "終了コード["
                + exitCode
                + "]でVMごとアプリケーションを終了します。" );
        RUNTIME.runFinalization();
        System.out.flush();
        System.err.flush();
        try{
            RUNTIME.exit(exitCode);
        }catch(SecurityException e){
            logger().warn(
                     "セキュリティ設定により、"
                    +"VMを終了させることができません。", e);
            throw e;
        }
        return;
    }

    /**
     * Jindolf のスタートアップエントリ。
     *
     * @param args コマンドライン引数
     */
    public static void main(final String[] args){
        // VM内二重起動チェック
        boolean hasInvoked = ! INVOKE_FLAG.compareAndSet(false, true);
        if(hasInvoked){
            String errmsg = "二度目以降の起動がキャンセルされました。";
            errorDialog("多重起動", errmsg);

            // exitせずに戻るのみ
            return;
        }

        checkGUIEnvironment();

        // ここからGUIウィンドウとマウス解禁

        checkCompileError();
        checkPackageDefinition();

        try{
            option = OptionInfo.parseOptions(args);
        }catch(IllegalArgumentException e){
            String message = e.getLocalizedMessage();
            System.err.println(message);
            System.err.println(
                "起動オプション一覧は、"
                + "起動オプションに「"
                + CmdOption.OPT_HELP.toHyphened()
                + "」を指定すると確認できます。" );
            Jindolf.RUNTIME.exit(1);
            assert false;
            return;
        }

        if(option.hasOption(CmdOption.OPT_HELP)){
            showHelpMessage();
            RUNTIME.exit(0);
            return;
        }

        if(option.hasOption(CmdOption.OPT_VERSION)){
            System.out.println(ID);
            RUNTIME.exit(0);
            return;
        }

        // あらゆるSwingコンポーネント操作より前に必要。
        if(option.hasOption(CmdOption.OPT_BOLDMETAL)){
            // もの凄く日本語表示が汚くなるかもよ！注意
            UIManager.put("swing.boldMetal", Boolean.TRUE);
        }else{
            UIManager.put("swing.boldMetal", Boolean.FALSE);
        }

        // JRE1.5用スプラッシュウィンドウ
        Window splashWindow = null;
        if(   ! JRE_PACKAGE.isCompatibleWith("1.6")
           && ! option.hasOption(CmdOption.OPT_NOSPLASH) ){
            splashWindow = createSplashWindow();
            splashWindow.setVisible(true);
            Thread.yield();
        }

        setting = new AppSetting();
        setting.applyOptionInfo(option);

        if(option.hasOption(CmdOption.OPT_VMINFO)){
            System.out.println(EnvInfo.getVMInfo());
        }

        initLogging(option.hasOption(CmdOption.OPT_CONSOLELOG));
        // ここからロギング解禁
        // Jindolf.exit()もここから解禁

        dumpBootInfo();

        ConfigFile.setupConfigDirectory();
        ConfigFile.setupLockFile();
        // ここから設定格納ディレクトリ解禁

        setting.loadConfig();

        RUNTIME.addShutdownHook(new Thread(){
            @Override
            public void run(){
                logger().info("シャットダウン処理に入ります…");
                System.out.flush();
                System.err.flush();
                RUNTIME.gc();
                Thread.yield();
                RUNTIME.runFinalization(); // 危険？
                Thread.yield();
                return;
            }
        });

        preInitClass();

        GUIUtils.replaceEventQueue();

        boolean hasError = false;
        try{
            EventQueue.invokeAndWait(new Runnable(){
                public void run(){
                    startGUI();
                    return;
                }
            });
        }catch(Throwable e){
            logger().fatal("アプリケーション初期化に失敗しました", e);
            e.printStackTrace(System.err);
            hasError = true;
        }finally{
            if(splashWindow != null){
                splashWindow.setVisible(false);
                splashWindow.dispose();
                splashWindow = null;
            }
        }

        if(hasError) exit(1);

        return;
    }

    /**
     * 隠れコンストラクタ。
     */
    private Jindolf(){
        super();
        assert this.getClass() == SELF_KLASS;
        return;
    }

}
