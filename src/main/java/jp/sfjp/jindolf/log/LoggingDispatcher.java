/*
 * event dispatcher with logging
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf.log;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 異常系をロギングするイベントディスパッチャ。
 */
public class LoggingDispatcher extends EventQueue{

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private static final String FATALMSG =
            "イベントディスパッチ中に異常が起きました。";


    /**
     * コンストラクタ。
     */
    public LoggingDispatcher(){
        super();
        return;
    }


    /**
     * 匿名ロガーにエラーや例外を吐く、
     * カスタム化されたイベントキューに差し替える。
     */
    public static void replaceEventQueue(){
        Toolkit kit = Toolkit.getDefaultToolkit();
        EventQueue oldQueue = kit.getSystemEventQueue();
        EventQueue newQueue = new LoggingDispatcher();
        oldQueue.push(newQueue);
        return;
    }

    /**
     * 異常系を匿名ロガーに出力する。
     *
     * @param throwable 例外
     */
    private static void logThrowable(Throwable throwable){
        LOGGER.log(Level.SEVERE, FATALMSG, throwable);
        return;
    }


    /**
     * {@inheritDoc}
     *
     * <p>イベントディスパッチにより発生した例外を匿名ログ出力する。
     *
     * @param event {@inheritDoc}
     */
    @Override
    protected void dispatchEvent(AWTEvent event){
        try{
            super.dispatchEvent(event);
        }catch(RuntimeException | Error e){
            logThrowable(e);
            throw e;
        }catch(Exception e){
            logThrowable(e);
        }
        return;
    }

    // TODO モーダルダイアログを出すべきか
    // TODO 標準エラー出力抑止オプションを用意すべきか
    // TODO セキュリティバイパス

}
