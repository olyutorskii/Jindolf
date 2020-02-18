/*
 * village information loader
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
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
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.LandState;

/**
 * 人狼各国のHTTPサーバから村情報を取得する。
 *
 * <p>村情報には村毎の更新時刻、日程、進行状況などが含まれる。
 *
 * <p>各Periodの会話はまだロードされない。
 */
public class VillageInfoLoader {

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private static final HtmlParser PARSER = new HtmlParser();
    private static final VillageInfoHandler HANDLER =
            new VillageInfoHandler();

    static{
        PARSER.setBasicHandler   (HANDLER);
        PARSER.setSysEventHandler(HANDLER);
        PARSER.setTalkHandler    (HANDLER);
    }

    /**
     * 人狼BBSサーバからPeriod一覧情報が含まれたHTMLを取得し、
     * Periodリストを更新する。
     *
     * @param village 村
     * @throws java.io.IOException ネットワーク入出力の異常
     */
    public static synchronized void updateVillage(Village village)
            throws IOException{
        Land land = village.getParentLand();
        LandDef landDef = land.getLandDef();
        LandState landState = landDef.getLandState();
        ServerAccess server = land.getServerAccess();

        HtmlSequence html;
        if(landState == LandState.ACTIVE){
            html = server.getHTMLBoneHead(village);
        }else{
            html = server.getHTMLVillage(village);
        }

        DecodedContent content = html.getContent();
        HANDLER.setVillage(village);
        try{
            PARSER.parseAutomatic(content);
        }catch(HtmlParseException e){
            LOGGER.log(Level.WARNING, "村の状態が不明", e);
        }

        return;
    }

}
