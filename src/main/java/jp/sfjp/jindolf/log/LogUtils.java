/*
 * logging common
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf.log;

import java.io.PrintStream;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LoggingPermission;

/**
 * ロギングの各種ユーティリティ。
 */
public final class LogUtils {

    /** ログ管理用パーミッション。 */
    public static final LoggingPermission PERM_LOGCTL =
            new LoggingPermission("control", null);

    private static final PrintStream STDERR = System.err;
    private static final String ERRMSG_LOGPERM =
            "セキュリティ設定により、ログ設定を変更できませんでした";


    /**
     * 隠しコンストラクタ。
     */
    private LogUtils(){
        assert false;
    }


    /**
     * ログ操作のアクセス権があるか否か判定する。
     *
     * @return アクセス権があればtrue
     */
    public static boolean hasLoggingPermission(){
        SecurityManager manager = System.getSecurityManager();
        boolean result = hasLoggingPermission(manager);
        return result;
    }

    /**
     * ログ操作のアクセス権があるか否か判定する。
     *
     * @param manager セキュリティマネージャ
     * @return アクセス権があればtrue
     */
    public static boolean hasLoggingPermission(SecurityManager manager){
        if(manager == null) return true;

        try{
            manager.checkPermission(PERM_LOGCTL);
        }catch(SecurityException e){
            return false;
        }

        return true;
    }

    /**
     * ルートロガーを返す。
     *
     * @return ルートロガー
     */
    public static Logger getRootLogger(){
        Logger rootLogger = Logger.getLogger("");
        return rootLogger;
    }

    /**
     * ルートロガーの初期化を行う。
     *
     * <p>ルートロガーの既存ハンドラを全解除し、
     * {@link MomentaryHandler}ハンドラを登録する。
     *
     * @param useConsoleLog trueなら
     * {@link java.util.logging.ConsoleHandler}も追加する。
     */
    public static void initRootLogger(boolean useConsoleLog){
        if( ! hasLoggingPermission() ){
            STDERR.println(ERRMSG_LOGPERM);
            return;
        }

        Logger rootLogger = getRootLogger();

        Handler[] oldHandlers = rootLogger.getHandlers();
        for(Handler handler : oldHandlers){
            rootLogger.removeHandler(handler);
        }

        Handler momentaryHandler = new MomentaryHandler();
        rootLogger.addHandler(momentaryHandler);

        if(useConsoleLog){
            Handler consoleHandler = new ConsoleHandler();
            rootLogger.addHandler(consoleHandler);
        }

        return;
    }

    /**
     * ルートロガーに新ハンドラを追加する。
     *
     * <p>ルートロガー中の全{@link MomentaryHandler}型ハンドラに
     * 蓄積されていたログは、新ハンドラに一気に転送される。
     *
     * <p>{@link MomentaryHandler}型ハンドラはルートロガーから削除される。
     *
     * <p>ログ操作のパーミッションがない場合、何もしない。
     *
     * @param newHandler 新ハンドラ
     */
    public static void switchHandler(Handler newHandler){
        if( ! hasLoggingPermission() ) return;

        Logger logger = getRootLogger();

        List<MomentaryHandler> momentaryHandlers =
                MomentaryHandler.getMomentaryHandlers(logger);
        MomentaryHandler.removeMomentaryHandlers(logger);

        logger.addHandler(newHandler);

        momentaryHandlers.forEach(momentaryHandler -> {
            momentaryHandler.transfer(newHandler);
            momentaryHandler.close();
        });

        return;
    }

}
