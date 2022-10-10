/*
 * Dummy logging handler
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: PileHandler.java 953 2009-12-06 16:42:14Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * なにもしないロギングハンドラ。
 * あとからなにがロギングされたのか一括して出力することができる。
 */
public class PileHandler extends Handler{

    private final List<LogRecord> logList = new LinkedList<LogRecord>();

    /**
     * ロギングハンドラを生成する。
     */
    public PileHandler(){
        super();
        return;
    }

    /**
     * {@inheritDoc}
     * ログを内部に溜め込む。
     * @param record {@inheritDoc}
     */
    @Override
    public void publish(LogRecord record){
        if( ! isLoggable(record) ){
            return;
        }
        this.logList.add(record);
        return;
    }

    /**
     * {@inheritDoc}
     * （何もしない）。
     */
    @Override
    public void flush(){
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close(){
        setLevel(Level.OFF);
        flush();
        return;
    }

    /**
     * 他のハンドラへ蓄積したログをまとめて出力する。
     * @param handler 他のハンドラ
     */
    public void delegate(Handler handler){
        if(handler == this) return;

        close();

        for(LogRecord record : this.logList){
            handler.publish(record);
        }

        handler.flush();
        this.logList.clear();

        return;
    }

}
