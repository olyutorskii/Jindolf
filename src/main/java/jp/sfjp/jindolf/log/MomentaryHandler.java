/*
 * momentary logging handler
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.log;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * なにもしない一時的なロギングハンドラ。
 *
 * <p>なにがロギングされたのかあとから一括して取得することができる。
 *
 * <p>知らないうちにメモリを圧迫しないよう注意。
 */
public class MomentaryHandler extends Handler{

    private final List<LogRecord> logList =
            Collections.synchronizedList(new LinkedList<>());
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
     *
     * @param logger ロガー
     * @return {@link MomentaryHandler}型ハンドラのリスト
     */
    public static List<MomentaryHandler>
            getMomentaryHandlers(Logger logger){
        List<MomentaryHandler> result;

        result = Arrays.stream(logger.getHandlers())
                .filter(handler -> handler instanceof MomentaryHandler)
                .map(handler -> (MomentaryHandler) handler)
                .collect(Collectors.toList());

        return result;
    }

    /**
     * ロガーに含まれる{@link MomentaryHandler}型ハンドラを全て削除する。
     *
     * @param logger ロガー
     */
    public static void removeMomentaryHandlers(Logger logger){
        getMomentaryHandlers(logger).forEach(handler -> {
            logger.removeHandler(handler);
        });
        return;
    }

    /**
     * 蓄積されたログレコードのリストを返す。
     *
     * <p>古いログが先頭に来る。
     *
     * @return 刻一刻と成長するログレコードのリスト。変更不可。
     */
    public List<LogRecord> getRecordList(){
        return this.unmodList;
    }

    /**
     * {@inheritDoc}
     *
     * <p>ログを内部に溜め込む。
     *
     * @param record {@inheritDoc}
     */
    @Override
    public void publish(LogRecord record){
        if(record == null) return;

        if( ! isLoggable(record) ){
            return;
        }

        // recording caller method
        record.getSourceMethodName();

        this.logList.add(record);

        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>（何もしない）。
     */
    @Override
    public void flush(){
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>以降のログ出力を無視する。
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
     *
     * <p>最後に蓄積されたログを解放する。
     *
     * @param handler 他のハンドラ
     * @throws NullPointerException 引数がnull
     */
    public void transfer(Handler handler) throws NullPointerException {
        Objects.nonNull(handler);
        if(handler == this) return;

        close();

        this.logList.forEach(record -> {
            handler.publish(record);
        });

        handler.flush();
        this.logList.clear();

        return;
    }

}
