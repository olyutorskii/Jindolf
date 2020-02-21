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
 * 冗長な処理(Hot判定、fullopen判定etc.)が若干残っている。
 */
public final class PeriodLoader {

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private static final HtmlParser PARSER;
    private static final PeriodHandler HANDLER;

    static{
        PARSER = new HtmlParser();
        HANDLER = new PeriodHandler();
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

        /*
            プレイ中の村でプロローグでもエピローグでもない日は
            灰ログetc.の非開示情報が含まれる。
            ※ 2020-02の時点で非開示情報の含まれるPeriodは存在しない。
               (常にFullOpen)
        */
        boolean isOpen = true;
        if(    village.getState() == VillageState.PROGRESS
            && period.getType() == PeriodType.PROGRESS ){
            isOpen = false;
        }
        period.setFullOpen(isOpen);

        Land land = village.getParentLand();
        ServerAccess server = land.getServerAccess();

        HtmlSequence html = server.getHTMLPeriod(period);
        DecodedContent content = html.getContent();

        // 2020-02の時点でHotなPeriodは存在しない。
        boolean wasHot = period.isHot();

        period.clearTopicList();

        PARSER.reset();
        HANDLER.reset();

        HANDLER.setPeriod(period);
        try{
            PARSER.parseAutomatic(content);
        }catch(HtmlParseException e){
            LOGGER.log(Level.WARNING, "発言抽出に失敗", e);
        }

        PARSER.reset();
        HANDLER.reset();

        /*
            2020-02の時点で、
            日付更新によるリロードを必要とするHotなPeriodは存在しない。
        */
        if(wasHot && ! period.isHot() ){
            parsePeriod(period, true);
            return;
        }

        return;
    }

}
