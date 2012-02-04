/*
 * logging common
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf.log;

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

    /**
     * 隠しコンストラクタ。
     */
    private LogUtils(){
        super();
        return;
    }

    /**
     * ログ操作のアクセス権があるか否か判定する。
     * @return アクセス権があればtrue
     */
    public static boolean hasLoggingPermission(){
        SecurityManager manager = System.getSecurityManager();
        boolean result = hasLoggingPermission(manager);
        return result;
    }

    /**
     * ログ操作のアクセス権があるか否か判定する。
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
     * ルートロガーの初期化を行う。
     * ルートロガーの既存ハンドラを全解除し、
     * {@link PileHandler}ハンドラを登録する。
     * @param useConsoleLog trueなら
     * {@link java.util.logging.ConsoleHandler}も追加する。
     */
    public static void initRootLogger(boolean useConsoleLog){
        if( ! hasLoggingPermission() ){
            System.err.println(
                      "セキュリティ設定により、"
                    + "ログ設定を変更できませんでした" );
            return;
        }

        Logger rootLogger = Logger.getLogger("");

        for(Handler handler : rootLogger.getHandlers()){
            rootLogger.removeHandler(handler);
        }

        Handler pileHandler = new PileHandler();
        rootLogger.addHandler(pileHandler);

        if(useConsoleLog){
            Handler consoleHandler = new ConsoleHandler();
            rootLogger.addHandler(consoleHandler);
        }

        return;
    }

    /**
     * ロガーに新ハンドラを追加する。
     * ロガー中の全{@link PileHandler}型ハンドラに蓄積されていたログは、
     * 新ハンドラに一気に転送される。
     * {@link PileHandler}型ハンドラはロガーから削除される。
     * ログ操作のパーミッションがない場合、何もしない。
     * @param logger ロガー
     * @param newHandler 新ハンドラ
     */
    public static void switchHandler(Logger logger, Handler newHandler){
        if( ! hasLoggingPermission() ) return;

        List<PileHandler> pileHandlers = PileHandler.getPileHandlers(logger);
        PileHandler.removePileHandlers(logger);

        logger.addHandler(newHandler);

        for(PileHandler pileHandler : pileHandlers){
            pileHandler.delegate(newHandler);
            pileHandler.close();
        }

        return;
    }

}
