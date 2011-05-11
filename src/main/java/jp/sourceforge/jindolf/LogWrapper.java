/*
 * log wrapper
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 各種ログAPIへの共通ラッパー。
 * 現時点では java.util.logging のみサポート。
 */
public class LogWrapper{

    private final Logger jre14Logger;

    /**
     * コンストラクタ。
     * @param logger ラップ対象のjava.util.loggingロガー
     */
    public LogWrapper(Logger logger){
        super();
        if(logger == null) throw new NullPointerException();
        this.jre14Logger = logger;
        return;
    }

    /**
     * ラップ対象のjava.util.loggingロガーを取得する。
     * @return ラップ対象のjava.util.loggingロガー
     */
    public Logger getJre14Logger(){
        return this.jre14Logger;
    }

    /**
     * ログレコードにスタックトレース情報を埋め込む。
     * @param record ログレコード
     */
    private void fillStackInfo(LogRecord record){
        Thread selfThread = Thread.currentThread();
        StackTraceElement[] stacks = selfThread.getStackTrace();

        String thisName = this.getClass().getName();

        boolean foundMySelf = false;
        for(StackTraceElement frame : stacks){
            String frameClassName = frame.getClassName();

            if( ! foundMySelf && frameClassName.equals(thisName) ){
                foundMySelf = true;
                continue;
            }

            if( foundMySelf &&  ! frameClassName.equals(thisName) ){
                record.setSourceClassName(frameClassName);
                record.setSourceMethodName(frame.getMethodName());
                break;
            }
        }

        return;
    }

    /**
     * java.util.loggingロガーへログ出力。
     * @param level ログレベル
     * @param msg メッセージ
     */
    private void logJre14(Level level, CharSequence msg){
        logJre14(level, msg, null);
        return;
    }

    /**
     * java.util.loggingロガーへログ出力。
     * @param level ログレベル
     * @param msg メッセージ
     * @param thrown 例外
     */
    private void logJre14(Level level, CharSequence msg, Throwable thrown){
        String message;
        if(msg == null) message = null;
        else            message = msg.toString();

        LogRecord record = new LogRecord(level, message);

        if(thrown != null){
            record.setThrown(thrown);
        }

        fillStackInfo(record);

        this.jre14Logger.log(record);

        return;
    }

    /**
     * 単純な情報を出力する。
     * @param msg メッセージ
     */
    public void info(CharSequence msg){
        logJre14(Level.INFO, msg);
        return;
    }

    /**
     * 警告を出力する。
     * @param msg メッセージ
     */
    public void warn(CharSequence msg){
        warn(msg, null);
        return;
    }

    /**
     * 警告を出力する。
     * @param msg メッセージ
     * @param thrown 例外
     */
    public void warn(CharSequence msg, Throwable thrown){
        logJre14(Level.WARNING, msg, thrown);
        return;
    }

    /**
     * 致命的な障害情報を出力する。
     * @param msg メッセージ
     */
    public void fatal(CharSequence msg){
        fatal(msg, null);
        return;
    }

    /**
     * 致命的な障害情報を出力する。
     * @param msg メッセージ
     * @param thrown 例外
     */
    public void fatal(CharSequence msg, Throwable thrown){
        logJre14(Level.SEVERE, msg, thrown);
        return;
    }

    // TODO Apache log4j サポート
}
