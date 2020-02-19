/*
 * period loader
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.osdn.jindolf.parser.HtmlParseException;
import jp.osdn.jindolf.parser.HtmlParser;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sfjp.jindolf.net.HtmlSequence;
import jp.sfjp.jindolf.net.ServerAccess;
import jp.sourceforge.jindolf.corelib.PeriodType;
import jp.sourceforge.jindolf.corelib.VillageState;

/**
 * 人狼各国のHTTPサーバから各村の個別の日(Period)をHTMLで取得する。
 *
 * <p>Periodには、プレイヤー同士の会話や
 * システムが自動生成するメッセージが正しい順序で納められる。
 *
 * <p>※ 人狼BBS:G国におけるG2087村のエピローグが終了した段階で、
 * 人狼BBSは過去ログの提供しか行っていない。
 * だがこのクラスには進行中の村の各日をパースするための
 * 冗長な処理が若干残っている。
 */
public final class PeriodLoader {

    private static final HtmlParser PARSER = new HtmlParser();
    private static final PeriodHandler HANDLER =
            new PeriodHandler();

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    static{
        PARSER.setBasicHandler   (HANDLER);
        PARSER.setSysEventHandler(HANDLER);
        PARSER.setTalkHandler    (HANDLER);
    }


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

        if(village.getState() != VillageState.PROGRESS){
            period.setFullOpen(true);
        }else if(period.getType() != PeriodType.PROGRESS){
            period.setFullOpen(true);
        }else{
            period.setFullOpen(false);
        }

        HtmlSequence html = server.getHTMLPeriod(period);

        period.clearTopicList();

        boolean wasHot = period.isHot();

        HANDLER.setPeriod(period);
        DecodedContent content = html.getContent();
        try{
            PARSER.parseAutomatic(content);
        }catch(HtmlParseException e){
            LOGGER.log(Level.WARNING, "発言抽出に失敗", e);
        }

        if(wasHot && ! period.isHot() ){
            parsePeriod(period, true);
            return;
        }

        return;
    }

}
