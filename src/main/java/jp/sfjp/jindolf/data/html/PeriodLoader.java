/*
 * period loader
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data.html;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.osdn.jindolf.parser.HtmlParseException;
import jp.osdn.jindolf.parser.HtmlParser;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.data.Period;
import jp.sfjp.jindolf.data.Village;
import jp.sfjp.jindolf.net.HtmlSequence;
import jp.sfjp.jindolf.net.ServerAccess;

/**
 * 人狼各国のHTTPサーバから各村の個別の日(Period)をHTMLで取得する。
 *
 * <p>Periodには、プレイヤー同士の会話や
 * システムが自動生成するメッセージが正しい順序で納められる。
 */
public final class PeriodLoader {

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    /**
     * hidden constructor.
     */
    private PeriodLoader(){
        assert false;
    }


    /**
     * Periodを更新する。Topicのリストが更新される。
     *
     * @param period 日
     * @param force trueなら強制再読み込み。
     *     falseならまだ読み込んで無い時のみ読み込み。
     * @throws IOException ネットワーク入力エラー
     */
    public static void parsePeriod(Period period, boolean force)
            throws IOException{
        if( ! force && period.hasLoaded() ) return;

        Village village = period.getVillage();

        Land land = village.getParentLand();
        ServerAccess server = land.getServerAccess();

        HtmlSequence html = server.getHTMLPeriod(period);
        DecodedContent content = html.getContent();

        period.clearTopicList();

        HtmlParser parser = new HtmlParser();
        PeriodHandler handler = new PeriodHandler();
        parser.setBasicHandler   (handler);
        parser.setSysEventHandler(handler);
        parser.setTalkHandler    (handler);

        handler.setPeriod(period);
        try{
            parser.parseAutomatic(content);
        }catch(HtmlParseException e){
            LOGGER.log(Level.WARNING, "発言抽出に失敗", e);
        }

        parser.reset();
        handler.reset();

        return;
    }

}
