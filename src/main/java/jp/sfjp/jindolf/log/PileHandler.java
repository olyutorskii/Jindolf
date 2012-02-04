/*
 * Dummy logging handler
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * なにもしないロギングハンドラ。
 * あとからなにがロギングされたのか一括して出力することができる。
 */
public class PileHandler extends Handler{

    private final List<LogRecord> logList = new LinkedList<LogRecord>();
    private final List<LogRecord> unmodList =
            Collections.unmodifiableList(this.logList);

    /**
     * ロギングハンドラを生成する。
     */
    public PileHandler(){
        super();
        return;
    }

    /**
     * ロガーに含まれる{@link PileHandler}型ハンドラのリストを返す。
     * @param logger ロガー
     * @return {@link PileHandler}型ハンドラのリスト
     */
    public static List<PileHandler> getPileHandlers(Logger logger){
        List<PileHandler> result = new LinkedList<PileHandler>();

        for(Handler handler : logger.getHandlers()){
            if( ! (handler instanceof PileHandler) ) continue;
            PileHandler pileHandler = (PileHandler) handler;
            result.add(pileHandler);
        }

        return result;
    }

    /**
     * ロガーに含まれる{@link PileHandler}型ハンドラを全て削除する。
     * @param logger ロガー
     */
    public static void removePileHandlers(Logger logger){
        for(PileHandler pileHandler : getPileHandlers(logger)){
            logger.removeHandler(pileHandler);
        }
        return;
    }

    /**
     * 蓄積されたログレコードのリストを返す。
     * 古いログが先頭に来る。
     * @return ログレコードのリスト。変更不可。
     */
    public List<LogRecord> getRecordList(){
        return this.unmodList;
    }

    /**
     * {@inheritDoc}
     * ログを内部に溜め込む。
     * @param record {@inheritDoc}
     */
    @Override
    public void publish(LogRecord record){
        if(record == null) return;

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
     * 最後に自分自身をクローズし、蓄積されたログを解放する。
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
