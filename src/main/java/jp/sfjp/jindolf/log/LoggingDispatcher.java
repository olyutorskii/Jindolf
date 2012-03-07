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

    private static final String FATALMSG =
            "イベントディスパッチ中に異常が起きました。";

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    /**
     * コンストラクタ。
     */
    public LoggingDispatcher(){
        super();
        return;
    }

    /**
     * 独自ロガーにエラーや例外を吐く、
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
     * 異常系をログ出力。
     * @param e 例外
     */
    private void errlog(Throwable e){
        LOGGER.log(Level.SEVERE, FATALMSG, e);
        return;
    }

    /**
     * {@inheritDoc}
     * 発生した例外をログ出力する。
     * @param event {@inheritDoc}
     */
    @Override
    protected void dispatchEvent(AWTEvent event){
        try{
            super.dispatchEvent(event);
        }catch(RuntimeException e){
            errlog(e);
            throw e;
        }catch(Exception e){
            errlog(e);
        }catch(Error e){
            errlog(e);
            throw e;
        }
        // TODO Toolkit#beep()もするべきか
        // TODO モーダルダイアログを出すべきか
        // TODO 標準エラー出力抑止オプションを用意すべきか
        // TODO セキュリティバイパス
        return;
    }

}
