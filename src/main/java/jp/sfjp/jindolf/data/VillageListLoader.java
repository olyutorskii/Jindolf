/*
 * village list loader
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.osdn.jindolf.parser.HtmlParseException;
import jp.osdn.jindolf.parser.HtmlParser;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sfjp.jindolf.net.HtmlSequence;
import jp.sfjp.jindolf.net.ServerAccess;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.LandState;
import jp.sourceforge.jindolf.corelib.VillageState;

/**
 * 人狼各国のHTTPサーバから村一覧リストを取得する。
 */
public class VillageListLoader {

    // 古国ID
    private static final String ID_VANILLAWOLF = "wolf";

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    /**
     * 村一覧リストをサーバからダウンロードする。
     *
     * <p>リスト元情報は国のトップページと村一覧ページ。
     *
     * <p>古国(wolf)の場合は村一覧にアクセスせずトップページのみ。
     * 古国以外で村建てをやめた国はトップページにアクセスしない。
     *
     * <p>戻される村一覧リストは重複がない。
     *
     * @param land 国
     * @return 村一覧リスト
     * @throws java.io.IOException ネットワーク入出力の異常
     */
    public static List<Village> loadVillageList(Land land)
            throws IOException{
        LandDef landDef = land.getLandDef();
        ServerAccess server = land.getServerAccess();
        List<VillageRecord> records = loadVillageRecords(landDef, server);

        List<Village> vList = new ArrayList<>(records.size());

        for(VillageRecord record : records){
            String id = record.getVillageId();
            String fullVillageName = record.getFullVillageName();

            VillageState status;
            if(landDef.getLandState() == LandState.HISTORICAL){
                status = VillageState.GAMEOVER;
            }else{
                status = record.getVillageStatus();
            }

            Village village = new Village(land, id, fullVillageName);
            village.setState(status);

            vList.add(village);
        }

        // たまに同じ村が複数回出現するので注意！
        SortedSet<Village> uniq = new TreeSet<>(vList);
        List<Village> result = new ArrayList<>(uniq);

        return result;
    }

    /**
     * 村一覧リストをサーバからダウンロードする。
     *
     * <p>リスト元情報は国のトップページと村一覧ページ。
     *
     * <p>古国(wolf)の場合は村一覧にアクセスせずトップページのみ。
     * 古国以外で村建てをやめた国はトップページにアクセスしない。
     *
     * <p>戻される村一覧リストは順不同で重複もありうる。
     *
     * @param landDef 国情報
     * @param server サーバ情報
     * @return 村一覧リスト
     * @throws java.io.IOException ネットワーク入出力の異常
     */
    public static List<VillageRecord> loadVillageRecords(LandDef landDef,
                                                         ServerAccess server)
            throws IOException{
        LandState state = landDef.getLandState();
        boolean isVanillaWolf = landDef.getLandId().equals(ID_VANILLAWOLF);

        List<VillageRecord> result = new LinkedList<>();

        HtmlParser parser = new HtmlParser();
        VillageListHandler handler = new VillageListHandler();
        parser.setBasicHandler(handler);

        // トップページ
        if(state.equals(LandState.ACTIVE) || isVanillaWolf){
            HtmlSequence html = server.getHTMLTopPage();
            DecodedContent content = html.getContent();

            try{
                parser.parseAutomatic(content);
            }catch(HtmlParseException e){
                LOGGER.log(Level.WARNING, "トップページを認識できない", e);
            }

            List<VillageRecord> topList = handler.getVillageRecords();
            result.addAll(topList);
        }

        // 村一覧ページ
        if( ! isVanillaWolf ){
            HtmlSequence html = server.getHTMLLandList();
            DecodedContent content = html.getContent();

            try{
                parser.parseAutomatic(content);
            }catch(HtmlParseException e){
                LOGGER.log(Level.WARNING, "村一覧ページを認識できない", e);
            }

            List<VillageRecord> recList = handler.getVillageRecords();
            result.addAll(recList);
        }

        parser.reset();
        handler.reset();

        return result;
    }

}
