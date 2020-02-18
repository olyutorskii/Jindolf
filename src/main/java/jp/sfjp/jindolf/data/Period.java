/*
 * daily period in village
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.osdn.jindolf.parser.HtmlParseException;
import jp.osdn.jindolf.parser.HtmlParser;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sfjp.jindolf.net.HtmlSequence;
import jp.sfjp.jindolf.net.ServerAccess;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.PeriodType;
import jp.sourceforge.jindolf.corelib.SysEventType;
import jp.sourceforge.jindolf.corelib.VillageState;

/**
 * いわゆる「日」。
 * 村の進行の一区切り。プロローグやエピローグも含まれる。
 *
 * <p>将来、24時間更新でなくなる可能性の考慮が必要。
 * 人気のないプロローグなどで、
 * 24時間以上の期間を持つPeriodが生成される可能性の考慮が必要。
 */
public class Period{
    // TODO Comparable も implement する？

    private static final HtmlParser PARSER = new HtmlParser();
    private static final PeriodHandler HANDLER =
            new PeriodHandler();

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    static{
        PARSER.setBasicHandler   (HANDLER);
        PARSER.setSysEventHandler(HANDLER);
        PARSER.setTalkHandler    (HANDLER);
    }

    private final Village homeVillage;
    private final PeriodType periodType;
    private final int day;
    private int limitHour;
    private int limitMinute;
    // TODO 更新月日も入れるべきか。
    private String loginName;
    private boolean isFullOpen = false;

    private final List<Topic> topicList = new LinkedList<>();
    private final List<Topic> unmodList =
            Collections.unmodifiableList(this.topicList);


    /**
     * この Period が進行中の村の最新日で、
     * 今まさに次々と発言が蓄積されているときは
     * true になる。
     * ※重要: Hot な Period は meslog クエリーを使ってダウンロードできない。
     */
    private boolean isHot;


    /**
     * Periodを生成する。
     * この段階では発言データのロードは行われない。
     * デフォルトで非Hot状態。
     * @param homeVillage 所属するVillage
     * @param periodType Period種別
     * @param day Period通番
     * @throws java.lang.NullPointerException 引数にnullが渡された場合。
     */
    public Period(Village homeVillage,
                   PeriodType periodType,
                   int day)
                   throws NullPointerException{
        this(homeVillage, periodType, day, false);
        return;
    }

    /**
     * Periodを生成する。
     * この段階では発言データのロードは行われない。
     * @param homeVillage 所属するVillage
     * @param periodType Period種別
     * @param day Period通番
     * @param isHot Hotか否か
     * @throws java.lang.NullPointerException 引数にnullが渡された場合。
     */
    private Period(Village homeVillage,
                    PeriodType periodType,
                    int day,
                    boolean isHot)
                    throws NullPointerException{
        if(    homeVillage == null
            || periodType  == null ) throw new NullPointerException();
        if(day < 0){
            throw new IllegalArgumentException("Period day is too small !");
        }
        switch(periodType){
        case PROLOGUE:
            assert day == 0;
            break;
        case PROGRESS:
        case EPILOGUE:
            assert day > 0;
            break;
        default:
            assert false;
            break;
        }

        this.homeVillage = homeVillage;
        this.periodType  = periodType;
        this.day         = day;

        unload();

        this.isHot = isHot;

        return;
    }


    /**
     * Periodを更新する。Topicのリストが更新される。
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
            period.isFullOpen = true;
        }else if(period.getType() != PeriodType.PROGRESS){
            period.isFullOpen = true;
        }else{
            period.isFullOpen = false;
        }

        HtmlSequence html = server.getHTMLPeriod(period);

        period.topicList.clear();

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

    /**
     * 所属する村を返す。
     * @return 村
     */
    public Village getVillage(){
        return this.homeVillage;
    }

    /**
     * Period種別を返す。
     * @return 種別
     */
    public PeriodType getType(){
        return this.periodType;
    }

    /**
     * Period通番を返す。
     * プロローグは常に0番。
     * n日目のゲーム進行日はn番
     * エピローグは最後のゲーム進行日+1番
     * @return Period通番
     */
    public int getDay(){
        return this.day;
    }

    /**
     * 更新時刻を設定する。
     *
     * @param hour 時
     * @param minute 分
     */
    public void setLimit(int hour, int minute){
        this.limitHour = hour;
        this.limitMinute = minute;
        return;
    }

    /**
     * 更新時刻の文字表記を返す。
     * @return 更新時刻の文字表記
     */
    public String getLimit(){
        StringBuilder result = new StringBuilder();

        if(this.limitHour < 10) result.append('0');
        result.append(this.limitHour).append(':');

        if(this.limitMinute < 10) result.append('0');
        result.append(this.limitMinute);

        return result.toString();
    }

    /**
     * Hotか否か返す。
     * @return Hotか否か
     */
    public boolean isHot(){
        return this.isHot;
    }

    /**
     * Hotか否か設定する。
     * @param isHotArg Hot指定
     */
    public void setHot(boolean isHotArg){
        this.isHot = isHotArg;
    }

    /**
     * プロローグか否か判定する。
     * @return プロローグならtrue
     */
    public boolean isPrologue(){
        if(getType() == PeriodType.PROLOGUE) return true;
        return false;
    }

    /**
     * エピローグか否か判定する。
     * @return エピローグならtrue
     */
    public boolean isEpilogue(){
        if(getType() == PeriodType.EPILOGUE) return true;
        return false;
    }

    /**
     * 進行日か否か判定する。
     * @return 進行日ならtrue
     */
    public boolean isProgress(){
        if(getType() == PeriodType.PROGRESS) return true;
        return false;
    }

    /**
     * このPeriodにアクセスするためのクエリーを生成する。
     * @return CGIに渡すクエリー
     */
    public String getCGIQuery(){
        StringBuilder result = new StringBuilder();

        Village village = getVillage();
        result.append(village.getCGIQuery());

        if(isHot()){
            result.append("&mes=all");   // 全表示指定
            return result.toString();
        }

        Land land = village.getParentLand();
        LandDef ldef = land.getLandDef();

        if(ldef.getLandId().equals("wolfg")){
            result.append("&meslog=");
            String dnum = "000" + (getDay() - 1);
            dnum = dnum.substring(dnum.length() - 3);
            switch(getType()){
            case PROLOGUE:
                result.append("000_ready");
                break;
            case PROGRESS:
                result.append(dnum).append("_progress");
                break;
            case EPILOGUE:
                result.append(dnum).append("_party");
                break;
            default:
                assert false;
                return null;
            }
        }else{
            result.append("&meslog=").append(village.getVillageID());
            switch(getType()){
            case PROLOGUE:
                result.append("_ready_0");
                break;
            case PROGRESS:
                result.append("_progress_").append(getDay() - 1);
                break;
            case EPILOGUE:
                result.append("_party_").append(getDay() - 1);
                break;
            default:
                assert false;
                return null;
            }
        }


        result.append("&mes=all");

        return result.toString();
    }

    /**
     * Periodに含まれるTopicのリストを返す。
     * このリストは上書き操作不能。
     * @return Topicのリスト
     */
    public List<Topic> getTopicList(){
        return this.unmodList;
    }

    /**
     * Topicのリスト内容を消す。
     */
    public void clearTopicList(){
        this.topicList.clear();
        return;
    }

    /**
     * Periodに含まれるTopicの総数を返す。
     * @return Topic総数
     */
    public int getTopics(){
        return this.topicList.size();
    }

    /**
     * Topicを追加する。
     * @param topic Topic
     * @throws java.lang.NullPointerException nullが渡された場合。
     */
    protected void addTopic(Topic topic) throws NullPointerException{
        if(topic == null) throw new NullPointerException();
        this.topicList.add(topic);
        return;
    }

    /**
     * Periodのキャプション文字列を返す。
     * 主な用途はタブ画面の耳のラベルなど。
     * @return キャプション文字列
     */
    public String getCaption(){
        String result;

        switch(getType()){
        case PROLOGUE:
            result = "プロローグ";
            break;
        case PROGRESS:
            result = getDay() + "日目";
            break;
        case EPILOGUE:
            result = "エピローグ";
            break;
        default:
            assert false;
            result = null;
            break;
        }

        return result;
    }

    /**
     * このPeriodをダウンロードしたときのログイン名を返す。
     * @return ログイン名。ログアウト中はnull。
     */
    public String getLoginName(){
        return this.loginName;
    }

    /**
     * ログイン名を設定する。
     * @param loginName ログイン名
     */
    public void setLoginName(String loginName){
        this.loginName = loginName;
        return;
    }

    /**
     * 公開発言番号にマッチする発言を返す。
     * @param talkNo 公開発言番号
     * @return 発言。見つからなければnull
     */
    public Talk getNumberedTalk(int talkNo){
        if(talkNo <= 0) throw new IllegalArgumentException();

        for(Topic topic : this.topicList){
            if( ! (topic instanceof Talk) ) continue;
            Talk talk = (Talk) topic;
            if(talkNo == talk.getTalkNo()) return talk;
        }

        return null;
    }

    /**
     * このPeriodの内容にゲーム進行上隠された部分がある可能性を判定する。
     * @return 隠れた要素がありうるならfalse
     */
    public boolean isFullOpen(){
        return this.isFullOpen;
    }

    /**
     * ロード済みか否かチェックする。
     * @return ロード済みならtrue
     */
    public boolean hasLoaded(){
        return getTopics() > 0;
    }

    /**
     * 発言データをアンロードする。
     */
    public void unload(){
        this.limitHour = 0;
        this.limitMinute = 0;
        this.loginName = null;
        this.isFullOpen = false;

        this.isHot = false;

        this.topicList.clear();

        return;
    }

    /**
     * 襲撃メッセージの有無を判定する。
     * 決着が付くまで非狼陣営には見えない。
     * 偽装GJでは狼にも見えない。
     * @return 襲撃メッセージがあればtrue
     */
    public boolean hasAssaultTried(){
        for(Topic topic : this.topicList){
            if(topic instanceof Talk){
                Talk talk = (Talk) topic;
                if(talk.getTalkCount() <= 0) return true;
            }else if(topic instanceof SysEvent){
                SysEvent sysEvent = (SysEvent) topic;
                SysEventType type = sysEvent.getSysEventType();
                if(type == SysEventType.ASSAULT) return true;
            }
        }

        return false;
    }

    /**
     * 処刑されたAvatarを返す。
     * @return 処刑されたAvatar。突然死などなんらかの理由でいない場合はnull
     */
    public Avatar getExecutedAvatar(){
        Avatar result = null;

        for(Topic topic : getTopicList()){
            if( ! (topic instanceof SysEvent) ) continue;
            SysEvent event = (SysEvent) topic;
            result = event.getExecutedAvatar();
            if(result != null) break;
        }

        return result;
    }

    /**
     * 投票に参加したAvatarの集合を返す。
     * @return 投票に参加したAvatarのSet
     */
    public Set<Avatar> getVoterSet(){
        Set<Avatar> result = new HashSet<>();

        for(Topic topic : getTopicList()){
            if( ! (topic instanceof SysEvent) ) continue;
            SysEvent event = (SysEvent) topic;
            result = event.getVoterSet(result);
        }

        return result;
    }

    /**
     * 任意のタイプのシステムイベントを返す。
     * 複数存在する場合、返すのは最初の一つだけ。
     * @param type イベントタイプ
     * @return システムイベント
     */
    public SysEvent getTypedSysEvent(SysEventType type){
        for(Topic topic : getTopicList()){
            if( ! (topic instanceof SysEvent) ) continue;
            SysEvent event = (SysEvent) topic;
            if(event.getSysEventType() == type) return event;
        }

        return null;
    }

}
