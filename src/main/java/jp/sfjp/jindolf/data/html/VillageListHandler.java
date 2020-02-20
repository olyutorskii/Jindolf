/*
 * village list handler
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data.html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.osdn.jindolf.parser.HtmlAdapter;
import jp.osdn.jindolf.parser.HtmlParseException;
import jp.osdn.jindolf.parser.PageType;
import jp.osdn.jindolf.parser.SeqRange;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sourceforge.jindolf.corelib.VillageState;

/**
 * 各国の村一覧HTMLをパースし、村一覧通知を受け取るためのハンドラ。
 *
 * <p>パース終了時には村一覧リストが完成する。
 */
class VillageListHandler extends HtmlAdapter{

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private final List<VillageRecord> villageRecords = new LinkedList<>();


    /**
     * コンストラクタ。
     */
    VillageListHandler() {
        super();
        return;
    }


    /**
     * URLクエリー文字列から特定キーの値を得る。
     *
     * <p>クエリーの書式例：「{@literal a=b&c=d&e=f}」この場合キーcの値はd
     *
     * @param key キー
     * @param allQuery クエリー文字列
     * @return キーの値。見つからなければnull
     */
    static String getValueFromCGIQueries(String key,
                                                String allQuery){
        String result = null;

        String[] queries = allQuery.split("\\Q&\\E");

        for(String pair : queries){
            if(pair == null) continue;
            String[] namevalue = pair.split("\\Q=\\E");
            if(namevalue == null) continue;
            if(namevalue.length != 2) continue;
            String name  = namevalue[0];
            String value = namevalue[1];
            if(name == null) continue;
            if( name.equals(key) ){
                result = value;
                if(result == null) continue;
                if(result.length() <= 0) continue;
                break;
            }
        }

        return result;
    }

    /**
     * AタグのHREF属性値からクエリー部を抽出する。
     *
     * <p>「{@literal &amp;}」は「{@literal &}」に解釈される。
     *
     * @param hrefValue HREF属性値
     * @return クエリー文字列。見つからなければnull。
     * @see <a href="https://www.w3.org/TR/html401/appendix/notes.html#h-B.2.2">
     * HTML 4.01 B.2.2
     * </a>
     */
    static String getRawQueryFromHREF(CharSequence hrefValue){
        if(hrefValue == null) return null;

        // HTML 4.01 B.2.2 rule
        String pureHREF = hrefValue.toString().replace("&amp;", "&");

        URI uri;
        try{
            uri = new URI(pureHREF);
        }catch(URISyntaxException e){
            LOGGER.log(Level.WARNING,
                    "不正なURI[{0}]を検出しました", hrefValue);
            return null;
        }

        String rawQuery = uri.getRawQuery();

        return rawQuery;
    }

    /**
     * HTMLのAタグ内HREF属性値から村IDを得る。
     *
     * @param hrefValue HREF値
     * @return 村ID。見つからなければnull。
     */
    static String getVillageIDFromHREF(CharSequence hrefValue){
        String rawQuery = getRawQueryFromHREF(hrefValue);
        if(rawQuery == null) return null;

        String villageID = getValueFromCGIQueries("vid", rawQuery);
        if(villageID == null) return null;
        if(villageID.length() <= 0) return null;

        return villageID;
    }


    /**
     * パース結果の村一覧を返す。
     *
     * @return 村一覧
     */
    List<VillageRecord> getVillageRecords(){
        return this.villageRecords;
    }

    /**
     * リセットを行う。
     *
     * <p>村一覧リストは空になる。
     */
    void reset() {
        this.villageRecords.clear();
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>パース開始通知を受け、村一覧リストを初期化する。
     *
     * @param content {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void startParse(DecodedContent content) throws HtmlParseException {
        reset();
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>ページ自動判定の結果の通知を受け、
     * パース対象HTMLがトップページでも村一覧ページでもなければ
     * 例外を投げパースを中止させる。
     *
     * @param type {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc} 意図しないページが来た。
     */
    @Override
    public void pageType(PageType type) throws HtmlParseException {
        if(        type != PageType.VILLAGELIST_PAGE
                && type != PageType.TOP_PAGE ){
            throw new HtmlParseException("トップページか村一覧ページが必要です。");
        }
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>村URL出現の通知を受け、村一覧リストに村を追加する。
     *
     * @param content {@inheritDoc}
     * @param anchorRange {@inheritDoc}
     * @param villageRange {@inheritDoc}
     * @param hour {@inheritDoc}
     * @param minute {@inheritDoc}
     * @param villageState {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void villageRecord(DecodedContent content,
                              SeqRange anchorRange,
                              SeqRange villageRange,
                              int hour, int minute,
                              VillageState villageState)
            throws HtmlParseException {
        CharSequence href = anchorRange.sliceSequence(content);
        String villageID = getVillageIDFromHREF(href);
        if(villageID == null || villageID.length() <= 0){
            LOGGER.log(Level.WARNING,
                    "認識できないURL[{0}]に遭遇しました。", href);
            return;
        }

        CharSequence fullVillageName = villageRange.sliceSequence(content);

        VillageRecord record =
                new VillageRecord(villageID,
                                  fullVillageName.toString(),
                                  villageState );

        this.villageRecords.add(record);

        return;
    }

}
