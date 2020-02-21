/*
 * village list loader
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
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
import jp.sourceforge.jindolf.corelib.VillageState;

/**
 * 人狼各国のHTTPサーバから村一覧リストを取得する。
 */
public final class VillageListLoader {

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    // 古国ID
    private static final String ID_VANILLAWOLF = "wolf";

    private static final List<VillageRecord> EMPTY_LIST =
            Collections.emptyList();


    /**
     * Hidden constructor.
     */
    private VillageListLoader() {
        assert false;
    }


    /**
     * 村一覧リストをサーバからダウンロードする。
     *
     * <p>リスト元情報は国のトップページと村一覧ページ。
     *
     * <p>古国(wolf)の場合は村一覧にアクセスせずトップページのみ。
     * 古国以外で村建てをやめた国はトップページにアクセスしない。
     *
     * <p>戻される村一覧リストはソート済みで重複がない。
     *
     * @param land 国
     * @return 村一覧リスト
     * @throws java.io.IOException ネットワーク入出力の異常
     */
    public static List<Village> loadVillageList(Land land)
            throws IOException{
        List<VillageRecord> records = loadVillageRecords(land);

        LandDef landDef = land.getLandDef();
        LandState landState = landDef.getLandState();
        boolean isHistorical = landState == LandState.HISTORICAL;

        List<Village> result = new ArrayList<>(records.size());

        for(VillageRecord record : records){
            String id = record.getVillageId();
            String fullVillageName = record.getFullVillageName();

            VillageState status;
            if(isHistorical){
                status = VillageState.GAMEOVER;
            }else{
                status = record.getVillageStatus();
            }

            Village village = new Village(land, id, fullVillageName);
            village.setState(status);

            result.add(village);
        }

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
     * <p>戻される村一覧リストは順序づけられており重複はない。
     *
     * @param land 国
     * @return 村一覧リスト
     * @throws java.io.IOException ネットワーク入出力の異常
     */
    private static List<VillageRecord> loadVillageRecords(Land land)
            throws IOException{
        LandDef landDef = land.getLandDef();
        boolean isVanillaWolf = landDef.getLandId().equals(ID_VANILLAWOLF);
        LandState state = landDef.getLandState();

        boolean needTopPage =
                state.equals(LandState.ACTIVE) || isVanillaWolf;
        boolean hasVillageList = ! isVanillaWolf;

        ServerAccess server = land.getServerAccess();

        List<VillageRecord> result = new LinkedList<>();

        // トップページ
        if(needTopPage){
            List<VillageRecord> recList = EMPTY_LIST;
            HtmlSequence html = server.getHTMLTopPage();
            try{
                recList = parseVillageRecords(html);
            }catch(HtmlParseException e){
                LOGGER.log(Level.WARNING, "トップページを認識できない", e);
            }
            result.addAll(recList);
        }

        // 村一覧ページ
        if(hasVillageList){
            List<VillageRecord> recList = EMPTY_LIST;
            HtmlSequence html = server.getHTMLLandList();
            try{
                recList = parseVillageRecords(html);
            }catch(HtmlParseException e){
                LOGGER.log(Level.WARNING, "村一覧ページを認識できない", e);
            }
            result.addAll(recList);
        }

        // 昇順ソートと重複排除処理。 重複例) B国116村
        SortedSet<VillageRecord> uniq = new TreeSet<>(result);
        result = new ArrayList<>(uniq);

        return result;
    }

    /**
     * HTMLをパースし村一覧リストを返す。
     *
     * @param html HTML文書
     * @return 村一覧リスト
     * @throws HtmlParseException HTMLパースエラーによるパース停止
     */
    private static List<VillageRecord> parseVillageRecords(HtmlSequence html)
            throws HtmlParseException{
        HtmlParser parser = new HtmlParser();
        VillageListHandler handler = new VillageListHandler();
        parser.setBasicHandler(handler);

        DecodedContent content = html.getContent();
        parser.parseAutomatic(content);

        List<VillageRecord> result = handler.getVillageRecords();

        return result;
    }

}
