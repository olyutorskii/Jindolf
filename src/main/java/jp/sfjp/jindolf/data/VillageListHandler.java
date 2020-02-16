/*
 * village list handler
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import jp.osdn.jindolf.parser.HtmlAdapter;
import jp.osdn.jindolf.parser.HtmlParseException;
import jp.osdn.jindolf.parser.PageType;
import jp.osdn.jindolf.parser.SeqRange;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.LandState;
import jp.sourceforge.jindolf.corelib.VillageState;

/**
 * 村一覧取得用ハンドラ。
 */
class VillageListHandler extends HtmlAdapter {

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private List<Village> villageList = null;
    private final Land outer;

    /**
     * コンストラクタ。
     */
    public VillageListHandler(final Land outer) {
        super();
        this.outer = outer;
        return;
    }

    /**
     * 村一覧を返す。
     * 再度パースを行うまで呼んではいけない。
     * @return 村一覧
     * @throws IllegalStateException パース前に呼び出された。
     *     あるいはパース後すでにリセットされている。
     */
    public List<Village> getVillageList() throws IllegalStateException {
        if (this.villageList == null) {
            throw new IllegalStateException("パースが必要です。");
        }
        List<Village> result = this.villageList;
        return result;
    }

    /**
     * リセットを行う。
     * 村一覧は空になる。
     */
    public void reset() {
        this.villageList = null;
        return;
    }

    /**
     * {@inheritDoc}
     * 村一覧リストが初期化される。
     * @param content {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void startParse(DecodedContent content) throws HtmlParseException {
        reset();
        this.villageList = new LinkedList<>();
        return;
    }

    /**
     * {@inheritDoc}
     * 自動判定の結果がトップページでも村一覧ページでもなければ
     * 例外を投げる。
     * @param type {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc} 意図しないページが来た。
     */
    @Override
    public void pageType(PageType type) throws HtmlParseException {
        if (type != PageType.VILLAGELIST_PAGE && type != PageType.TOP_PAGE) {
            throw new HtmlParseException("トップページか村一覧ページが必要です。");
        }
        return;
    }

    /**
     * {@inheritDoc}
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
        LandDef landdef = outer.getLandDef();
        LandState landState = landdef.getLandState();
        CharSequence href = anchorRange.sliceSequence(content);
        String villageID = Land.getVillageIDFromHREF(href);
        if (villageID == null || villageID.length() <= 0) {
            LOGGER.warning("認識できないURL[" + href + "]に遭遇しました。");
            return;
        }
        CharSequence fullVillageName = villageRange.sliceSequence(content);
        // TODO 既に出来ているかもしれないVillageを再度作るのは無駄？
        Village village = new Village(outer, villageID, fullVillageName.toString());
        if (landState == LandState.HISTORICAL) {
            village.setState(VillageState.GAMEOVER);
        } else {
            village.setState(villageState);
        }
        this.villageList.add(village);
        return;
    }

}
