/*
 * village info handler
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data.html;

import java.util.logging.Logger;
import jp.osdn.jindolf.parser.HtmlAdapter;
import jp.osdn.jindolf.parser.HtmlParseException;
import jp.osdn.jindolf.parser.PageType;
import jp.osdn.jindolf.parser.SeqRange;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.data.Period;
import jp.sfjp.jindolf.data.Village;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.LandState;
import jp.sourceforge.jindolf.corelib.PeriodType;
import jp.sourceforge.jindolf.corelib.VillageState;


/**
 * 各村のHTMLをパースし、村情報や日程の通知を受け取るためのハンドラ。
 *
 * <p>パース終了時には、
 * あらかじめ指定したVillageインスタンスに
 * 更新時刻などの村情報が適切に更新される。
 *
 * <p>日程は空Periodのリストに反映されるが各Periodのロードはまだ行われない。
 *
 * <p>※人狼BBS:G国におけるG2087村のエピローグが終了した段階で、
 * 人狼BBSは過去ログの提供しか行っていない。
 * だがこのクラスには進行中の村をパースするための冗長な処理が若干残っている。
 */
class VillageInfoHandler extends HtmlAdapter {

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    private Village village = null;

    private boolean hasPrologue;
    private boolean hasProgress;
    private boolean hasEpilogue;

    private boolean hasDone;
    private int maxProgress;


    /**
     * コンストラクタ。
     */
    VillageInfoHandler(){
        super();
        return;
    }


    /**
     * 更新対象の村インスタンスを設定する。
     *
     * @param village 村インスタンス
     */
    public void setVillage(Village village){
        this.village = village;
        reset();
        return;
    }

    /**
     * 各種進行コンテキストのリセットを行う。
     */
    public void reset() {
        this.hasPrologue = false;
        this.hasProgress = false;
        this.hasEpilogue = false;
        this.hasDone = false;
        this.maxProgress = 0;
        return;
    }

    /**
     * パース結果から村の状態を算出する。
     *
     * @return 村の状態
     */
    public VillageState getVillageState() {
        if(this.hasDone){
            return VillageState.GAMEOVER;
        }else if(this.hasEpilogue){
            return VillageState.EPILOGUE;
        }else if(this.hasProgress){
            return VillageState.PROGRESS;
        }else if(this.hasPrologue){
            return VillageState.PROLOGUE;
        }

        return VillageState.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     *
     * @param content {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void startParse(DecodedContent content)
            throws HtmlParseException {
        reset();
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>HTML自動判定の結果が村の日程ページでなければ例外を投げ、
     * パースを中止する。
     *
     * @param type {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc} 意図しないページが来た。
     */
    @Override
    public void pageType(PageType type) throws HtmlParseException {
        if(type != PageType.PERIOD_PAGE){
            throw new HtmlParseException("日ページが必要です。");
        }
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>更新時刻の通知を受け取る。
     * 更新時刻はVillageインスタンスへ反映される。
     *
     * @param month {@inheritDoc}
     * @param day {@inheritDoc}
     * @param hour {@inheritDoc}
     * @param minute {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void commitTime(int month, int day, int hour, int minute)
            throws HtmlParseException {
        this.village.setLimit(month, day, hour, minute);
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>日程ページから各Period(日)へのリンクHTML出現の通知を受け取る。
     * Villageインスタンスの進行状況へ反映される。
     *
     * @param content {@inheritDoc}
     * @param anchorRange {@inheritDoc}
     * @param periodType {@inheritDoc}
     * @param day {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void periodLink(DecodedContent content,
                           SeqRange anchorRange,
                           PeriodType periodType,
                           int day)
            throws HtmlParseException {
        if(periodType == null){
            this.hasDone = true;
            return;
        }

        switch(periodType){
        case PROLOGUE:
            this.hasPrologue = true;
            break;
        case PROGRESS:
            this.hasProgress = true;
            this.maxProgress = day;
            break;
        case EPILOGUE:
            this.hasEpilogue = true;
            break;
        default:
            assert false;
            break;
        }

        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>パース終了時の処理を行う。
     *
     * <p>村としての体裁に矛盾が検出されると、
     * 例外を投げパースを中断する。
     *
     * <p>村の進行に従い空Periodのリストを生成する。
     *
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void endParse() throws HtmlParseException {
        VillageState villageState = getVillageState();
        if(villageState == VillageState.UNKNOWN){
            this.village.setState(villageState);
            LOGGER.warning("村の状況を読み取れません");
            return;
        }

        Land land = this.village.getParentLand();
        LandDef landDef = land.getLandDef();
        LandState landState = landDef.getLandState();

        if(landState == LandState.ACTIVE){
            this.village.setState(villageState);
        }else{
            this.village.setState(VillageState.GAMEOVER);
        }

        modifyPeriodList();

        return;
    }

    /**
     * 抽出したPeriod別リンク情報に伴い空Periodリストを準備する。
     *
     * <p>まだPeriodデータのロードは行われない。
     *
     * <p>ゲーム進行中の村で更新時刻をまたいで更新が行われた場合、
     * 既存のPeriodリストが伸張する場合がある。
     */
    private void modifyPeriodList() {
        Period lastPeriod = null;
        if(this.hasPrologue){
            Period prologue = this.village.getPrologue();
            if(prologue == null){
                lastPeriod =
                        new Period(this.village,
                                   PeriodType.PROLOGUE,
                                   0 );
                this.village.setPeriod(0, lastPeriod);
            }else{
                lastPeriod = prologue;
            }
        }

        if(this.hasProgress){
            for(int day = 1; day <= this.maxProgress; day++){
                Period progress = this.village.getProgress(day);
                if(progress == null){
                    lastPeriod =
                            new Period(this.village,
                                       PeriodType.PROGRESS,
                                       day );
                    this.village.setPeriod(day, lastPeriod);
                }else{
                    lastPeriod = progress;
                }
            }
        }

        if(this.hasEpilogue){
            Period epilogue = this.village.getEpilogue();
            if(epilogue == null){
                lastPeriod =
                        new Period(this.village,
                                   PeriodType.EPILOGUE,
                                   this.maxProgress + 1 );
                this.village.setPeriod(this.maxProgress + 1, lastPeriod);
            } else {
                lastPeriod = epilogue;
            }
        }

        assert this.village.getPeriodSize() > 0;
        assert lastPeriod != null;

        if(this.village.getState() != VillageState.GAMEOVER){
            lastPeriod.setHot(true);
        }

        return;
    }

}
