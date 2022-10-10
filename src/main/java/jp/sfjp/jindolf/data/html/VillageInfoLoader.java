/*
 * village information loader
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data.html;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.osdn.jindolf.parser.HtmlParseException;
import jp.osdn.jindolf.parser.HtmlParser;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.data.Village;
import jp.sfjp.jindolf.net.HtmlSequence;
import jp.sfjp.jindolf.net.ServerAccess;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.LandState;

/**
 * 人狼各国のHTTPサーバから個別の村の村情報をHTMLで取得する。
 *
 * <p>村情報には村毎の更新時刻、日程、進行状況などが含まれる。
 *
 * <p>各Periodの会話はまだロードされない。
 */
public final class VillageInfoLoader {

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    /**
     * Hidden constructor.
     */
    private VillageInfoLoader() {
        assert false;
    }


    /**
     * 人狼BBSサーバから
     * HTMLで記述された各村の村情報ページをダウンロードする。
     *
     * <p>村情報ページのURLは各国の状態及び村の進行状況により異なる。
     *
     * <p>※ G国HISTORICAL運用移行に伴い、
     * 2020-02の時点で進行中の村はもはや存在しないため、
     * 若干の冗長なコードが残存する。
     *
     * <p>例: G1000村(エピローグ終了状態)の村情報ページは
     * <a href="http://www.wolfg.x0.com/index.rb?vid=1000">
     * http://www.wolfg.x0.com/index.rb?vid=1000</a>
     *
     * @param village 村
     * @return HTML文書
     * @throws IOException 入出力エラー
     */
    private static DecodedContent loadVillageInfo(Village village)
            throws IOException{
        Land land = village.getParentLand();
        ServerAccess server = land.getServerAccess();
        LandDef landDef = land.getLandDef();
        LandState landState = landDef.getLandState();

        HtmlSequence html;
        if(landState == LandState.ACTIVE){
            html = server.getHTMLBoneHead(village);
        }else{
            html = server.getHTMLVillage(village);
        }

        DecodedContent content = html.getContent();

        return content;
    }

    /**
     * 人狼BBSサーバから各村のPeriod一覧情報が含まれたHTML(村情報)を取得し、
     * 更新時刻や日程、空PeriodのリストをVillageインスタンスに設定する。
     *
     * @param village 村
     * @throws java.io.IOException ネットワーク入出力の異常
     */
    public static void updateVillageInfo(Village village)
            throws IOException{
        DecodedContent content = loadVillageInfo(village);

        HtmlParser parser = new HtmlParser();
        VillageInfoHandler handler = new VillageInfoHandler();
        parser.setBasicHandler   (handler);
        parser.setSysEventHandler(handler);
        parser.setTalkHandler    (handler);

        handler.setVillage(village);
        try{
            parser.parseAutomatic(content);
        }catch(HtmlParseException e){
            LOGGER.log(Level.WARNING, "村の状態が不明", e);
        }

        parser.reset();
        handler.reset();

        return;
    }

}
