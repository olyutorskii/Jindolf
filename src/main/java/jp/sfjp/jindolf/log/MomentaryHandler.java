/*
 * momentary logging handler
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
 * なにもしない一時的なロギングハンドラ。
 * なにがロギングされたのかあとから一括して取得することができる。
 * <p>知らないうちにメモリを圧迫しないよう注意。
 */
public class MomentaryHandler extends Handler{

    private final List<LogRecord> logList =
            Collections.synchronizedList(new LinkedList<LogRecord>());
    private final List<LogRecord> unmodList =
            Collections.unmodifiableList(this.logList);


    /**
     * コンストラクタ。
     */
    public MomentaryHandler(){
        super();
        return;
    }


    /**
     * ロガーに含まれる{@link MomentaryHandler}型ハンドラのリストを返す。
     * @param logger ロガー
     * @return {@link MomentaryHandler}型ハンドラのリスト
     */
    public static List<MomentaryHandler>
            getMomentaryHandlers(Logger logger){
        List<MomentaryHandler> result = new LinkedList<MomentaryHandler>();

        for(Handler handler : logger.getHandlers()){
            if( ! (handler instanceof MomentaryHandler) ) continue;
            MomentaryHandler momentaryHandler = (MomentaryHandler) handler;
            result.add(momentaryHandler);
        }

        return result;
    }

    /**
     * ロガーに含まれる{@link MomentaryHandler}型ハンドラを全て削除する。
     * @param logger ロガー
     */
    public static void removeMomentaryHandlers(Logger logger){
        for(MomentaryHandler handler : getMomentaryHandlers(logger)){
            logger.removeHandler(handler);
        }
        return;
    }

    /**
     * 蓄積されたログレコードのリストを返す。
     * 古いログが先頭に来る。
     * @return 刻一刻と成長するログレコードのリスト。変更不可。
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

        record.getSourceMethodName();

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
     * 以降のログ出力を無視する。
     */
    @Override
    public void close(){
        setLevel(Level.OFF);
        flush();
        return;
    }

    /**
     * 自分自身をクローズし、
     * 蓄積したログを他のハンドラへまとめて出力する。
     * 最後に蓄積されたログを解放する。
     * @param handler 他のハンドラ
     * @throws NullPointerException 引数がnull
     */
    public void transfer(Handler handler) throws NullPointerException {
        if(handler == null) throw new NullPointerException();
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
