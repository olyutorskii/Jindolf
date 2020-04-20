/*
 * daily period in village
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.PeriodType;
import jp.sourceforge.jindolf.corelib.SysEventType;

/**
 * いわゆる「日」。
 * 村の進行の一区切り。プロローグやエピローグも含まれる。
 *
 * <p>将来、24時間更新でなくなる可能性の考慮が必要。
 * 人気のないプロローグなどで、
 * 24時間以上の期間を持つPeriodが生成される可能性の考慮が必要。
 */
public final class Period{
    // TODO Comparable も implement する？

    private final Village homeVillage;
    private final PeriodType periodType;
    private final int day;
    private int limitHour;
    private int limitMinute;
    // TODO 更新月日も入れるべきか。

    private final List<Topic> topicList = new LinkedList<>();
    private final List<Topic> unmodList =
            Collections.unmodifiableList(this.topicList);


    /**
     * Periodを生成する。
     *
     * <p>この段階では発言データのロードは行われない。
     *
     * @param homeVillage 所属するVillage
     * @param periodType Period種別
     * @param day Period通番
     * @throws java.lang.NullPointerException 引数にnullが渡された場合。
     */
    public Period(Village homeVillage,
                  PeriodType periodType,
                  int day){
        Objects.nonNull(homeVillage);
        Objects.nonNull(periodType);

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

        return;
    }


    /**
     * 所属する村を返す。
     *
     * @return 村
     */
    public Village getVillage(){
        return this.homeVillage;
    }

    /**
     * Period種別を返す。
     *
     * @return 種別
     */
    public PeriodType getType(){
        return this.periodType;
    }

    /**
     * Period通番を返す。
     *
     * <p>プロローグは常に0番。
     * n日目のゲーム進行日はn番。
     * エピローグは最後のゲーム進行日+1番。
     *
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
     *
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
     * プロローグか否か判定する。
     *
     * @return プロローグならtrue
     */
    public boolean isPrologue(){
        if(getType() == PeriodType.PROLOGUE) return true;
        return false;
    }

    /**
     * エピローグか否か判定する。
     *
     * @return エピローグならtrue
     */
    public boolean isEpilogue(){
        if(getType() == PeriodType.EPILOGUE) return true;
        return false;
    }

    /**
     * 進行日か否か判定する。
     *
     * @return 進行日ならtrue
     */
    public boolean isProgress(){
        if(getType() == PeriodType.PROGRESS) return true;
        return false;
    }

    /**
     * このPeriodにアクセスするためのクエリーを生成する。
     *
     * @return CGIに渡すクエリー
     */
    public String getCGIQuery(){
        StringBuilder result = new StringBuilder();

        Village village = getVillage();
        result.append(village.getCGIQuery());

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
     *
     * <p>このリストは上書き操作不能。
     *
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
     *
     * @return Topic総数
     */
    public int getTopics(){
        return this.topicList.size();
    }

    /**
     * Topicを追加する。
     *
     * @param topic Topic
     * @throws java.lang.NullPointerException nullが渡された場合。
     */
    public void addTopic(Topic topic) throws NullPointerException{
        if(topic == null) throw new NullPointerException();
        this.topicList.add(topic);
        return;
    }

    /**
     * Periodのキャプション文字列を返す。
     *
     * <p>主な用途はタブ画面の耳のラベルなど。
     *
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
     * 公開発言番号にマッチする発言を返す。
     *
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
     * ロード済みか否かチェックする。
     *
     * @return ロード済みならtrue
     */
    public boolean hasLoaded(){
        return ! this.topicList.isEmpty();
    }

    /**
     * 発言データをアンロードする。
     */
    public void unload(){
        this.limitHour = 0;
        this.limitMinute = 0;

        this.topicList.clear();

        return;
    }

    /**
     * 襲撃メッセージの有無を判定する。
     *
     * <p>決着が付くまで非狼陣営には見えない。
     * 偽装GJでは狼にも見えない。
     *
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
     *
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
     *
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
     *
     * <p>複数存在する場合、返すのは最初の一つだけ。
     *
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
