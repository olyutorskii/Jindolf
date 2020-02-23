/*
 * village list handler
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data.html;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static final String ERR_ILLEGALPAGE =
            "トップページか村一覧ページが必要です。";
    private static final String ERR_URI =
            "認識できないURL[{0}]に遭遇しました。";

    private static final Pattern REG_VID = Pattern.compile(
            "\\Qindex.rb?vid=\\E" + "([1-9][0-9]*)" + "\\Q&amp;\\E");


    private List<VillageRecord> villageRecords = new LinkedList<>();


    /**
     * コンストラクタ。
     */
    VillageListHandler() {
        super();
        this.villageRecords = new LinkedList<>();
        return;
    }


    /**
     * HTMLのAタグ内HREF属性値から村IDを得る。
     *
     * <p>G国も含め村IDは0以外から始まる1以上の10進数。
     *
     * @param hrefValue HREF属性値
     * @return 村ID。見つからなければnull。
     */
    static String parseVidFromHref(CharSequence hrefValue){
        Matcher matcher = REG_VID.matcher(hrefValue);
        boolean match = matcher.lookingAt();
        if(!match) return null;

        String result = matcher.group(1);
        assert result.length() > 0;

        return result;
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
        this.villageRecords = new LinkedList<>();
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
            throw new HtmlParseException(ERR_ILLEGALPAGE);
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
        String villageID = parseVidFromHref(href);
        if(villageID == null){
            LOGGER.log(Level.WARNING, ERR_URI, href);
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
