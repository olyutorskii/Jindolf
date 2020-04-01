/*
 * Village
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.VillageState;

/**
 * いわゆる「村」。
 */
public class Village{

    private static final int GID_MIN = 3;


    private final Land parentLand;
    private final String villageID;
    private final int villageIDNum;
    private final String villageName;

    private final boolean isValid;

    private int limitMonth;
    private int limitDay;
    private int limitHour;
    private int limitMinute;

    private VillageState state = VillageState.UNKNOWN;

    private final LinkedList<Period> periodList = new LinkedList<>();
    private final List<Period> unmodList =
            Collections.unmodifiableList(this.periodList);

    private final Map<String, Avatar> avatarMap =
            new HashMap<>();

    private final Map<Avatar, BufferedImage> faceImageMap =
            new HashMap<>();
    private final Map<Avatar, BufferedImage> bodyImageMap =
            new HashMap<>();
    private final Map<Avatar, BufferedImage> faceMonoImageMap =
            new HashMap<>();
    private final Map<Avatar, BufferedImage> bodyMonoImageMap =
            new HashMap<>();

    private boolean isLocalArchive = false;


    /**
     * Villageを生成する。
     *
     * @param parentLand Villageの所属する国
     * @param villageID 村のID
     * @param villageName 村の名前
     */
    public Village(Land parentLand, String villageID, String villageName) {
        this.parentLand    = parentLand;
        this.villageID   = villageID.intern();
        this.villageIDNum = Integer.parseInt(this.villageID);
        this.villageName = villageName.intern();

        this.isValid = this.parentLand.getLandDef()
                       .isValidVillageId(this.villageIDNum);

        return;
    }


    /**
     * 所属する国を返す。
     *
     * @return 村の所属する国（Land）
     */
    public Land getParentLand(){
        return this.parentLand;
    }

    /**
     * 村のID文字列を返す。
     *
     * @return 村ID
     */
    public String getVillageID(){
        return this.villageID;
    }

    /**
     * 村のID数値を返す。
     *
     * @return 村ID
     */
    public int getVillageIDNum(){
        return this.villageIDNum;
    }

    /**
     * 村の名前を返す。
     *
     * @return 村の名前
     */
    public String getVillageName(){
        StringBuilder name = new StringBuilder();

        LandDef landDef = this.parentLand.getLandDef();
        String prefix = landDef.getLandPrefix();
        name.append(prefix);

        StringBuilder id = new StringBuilder(this.villageID);
        if(landDef.getLandId().equals("wolfg")){
            while(id.length() < GID_MIN){
                id.insert(0, '0');
            }
        }
        name.append(id);

        String result = name.toString();
        return result;
    }

    /**
     * 村の長い名前を返す。
     *
     * @return 村の長い名前
     */
    public String getVillageFullName(){
        return this.villageName;
    }

    /**
     * 村の状態を返す。
     *
     * @return 村の状態
     */
    public VillageState getState(){
        return this.state;
    }

    /**
     * 村の状態を設定する。
     *
     * @param state 村の状態
     */
    public void setState(VillageState state){
        this.state = state;
        return;
    }

    /**
     * 日程及び更新時刻を持っているか判定する。
     *
     * @return 日程が不明ならtrue
     */
    public boolean hasSchedule(){
        boolean result = ! this.periodList.isEmpty();
        return result;
    }

    /**
     * プロローグを返す。
     *
     * @return プロローグ
     */
    public Period getPrologue(){
        for(Period period : this.periodList){
            if(period.isPrologue()) return period;
        }
        return null;
    }

    /**
     * エピローグを返す。
     *
     * @return エピローグ
     */
    public Period getEpilogue(){
        for(Period period : this.periodList){
            if(period.isEpilogue()) return period;
        }
        return null;
    }

    /**
     * 指定された日付の進行日を返す。
     *
     * @param day 日付
     * @return Period
     */
    public Period getProgress(int day){
        for(Period period : this.periodList){
            if(    period.isProgress()
                && period.getDay() == day ) return period;
        }
        return null;
    }

    /**
     * PROGRESS状態のPeriodの総数を返す。
     *
     * @return PROGRESS状態のPeriod総数
     */
    public int getProgressDays(){
        int result = 0;
        for(Period period : this.periodList){
            if(period.isProgress()) result++;
        }
        return result;
    }

    /**
     * 指定されたPeriodインデックスのPeriodを返す。
     *
     * <p>プロローグやエピローグへのアクセスも可能。
     *
     * @param day Periodインデックス
     * @return Period
     */
    public Period getPeriod(int day){
        return this.periodList.get(day);
    }

    /**
     * 指定されたアンカーの対象のPeriodを返す。
     *
     * @param anchor アンカー
     * @return Period
     */
    public Period getPeriod(Anchor anchor){
        Period anchorPeriod;

        if(anchor.isEpilogueDay()){
            anchorPeriod = getEpilogue();
            return anchorPeriod;
        }

        int anchorDay = anchor.getDay();
        anchorPeriod = getPeriod(anchorDay);

        return anchorPeriod;
    }

    /**
     * Period総数を返す。
     *
     * @return Period総数
     */
    public int getPeriodSize(){
        return this.periodList.size();
    }

    /**
     * Periodへのリストを返す。
     *
     * @return Periodのリスト。
     */
    public List<Period> getPeriodList(){
        return this.unmodList;
    }

    /**
     * 指定したフルネームで村に登録されているAvatarを返す。
     *
     * @param fullName Avatarの名前
     * @return Avatar
     */
    public Avatar getAvatar(String fullName){
        Avatar avatar = this.avatarMap.get(fullName);
        return avatar;
    }

    /**
     * Avatarを村に登録する。
     *
     * @param avatar Avatar
     */
    public void addAvatar(Avatar avatar){
        if(avatar == null) return;

        String fullName = avatar.getFullName();
        if(this.avatarMap.get(fullName) != null) return;

        this.avatarMap.put(fullName, avatar);
        return;
    }

    /**
     * 村に登録されたAvatarの顔イメージを返す。
     *
     * @param avatar Avatar
     * @return 顔イメージ
     */
    public BufferedImage getAvatarFaceImage(Avatar avatar){
        BufferedImage result;
        result = this.faceImageMap.get(avatar);
        if(result != null) return result;

        Land land = getParentLand();
        LandDef landDef = land.getLandDef();

        String template = landDef.getFaceURITemplate();
        int serialNo = avatar.getIdNum();
        result = getAvatarImage(template, serialNo);

        this.faceImageMap.put(avatar, result);

        return result;
    }

    /**
     * 村に登録されたAvatarの全身像イメージを返す。
     *
     * @param avatar Avatar
     * @return 全身イメージ
     */
    public BufferedImage getAvatarBodyImage(Avatar avatar){
        BufferedImage result;
        result = this.bodyImageMap.get(avatar);
        if(result != null) return result;

        Land land = getParentLand();
        LandDef landDef = land.getLandDef();

        String template = landDef.getBodyURITemplate();
        int serialNo = avatar.getIdNum();
        result = getAvatarImage(template, serialNo);

        this.bodyImageMap.put(avatar, result);

        return result;
    }

    /**
     * 各国URLテンプレートと通し番号から
     * イメージをダウンロードする。
     *
     * @param template テンプレート
     * @param serialNo Avatarの通し番号
     * @return 顔もしくは全身像イメージ
     */
    private BufferedImage getAvatarImage(String template, int serialNo){
        Land land = getParentLand();
        String uri = MessageFormat.format(template, serialNo);

        BufferedImage result;
        result = land.downloadImage(uri);
        if(result == null) result = GUIUtils.getNoImage();

        return result;
    }

    /**
     * 村に登録されたAvatarのモノクロ顔イメージを返す。
     *
     * @param avatar Avatar
     * @return 顔イメージ
     */
    public BufferedImage getAvatarFaceMonoImage(Avatar avatar){
        BufferedImage result;
        result = this.faceMonoImageMap.get(avatar);
        if(result == null){
            result = getAvatarFaceImage(avatar);
            result = GUIUtils.createMonoImage(result);
            this.faceMonoImageMap.put(avatar, result);
        }
        return result;
    }

    /**
     * 村に登録されたAvatarの全身像イメージを返す。
     *
     * @param avatar Avatar
     * @return 全身イメージ
     */
    public BufferedImage getAvatarBodyMonoImage(Avatar avatar){
        BufferedImage result;
        result = this.bodyMonoImageMap.get(avatar);
        if(result == null){
            result = getAvatarBodyImage(avatar);
            result = GUIUtils.createMonoImage(result);
            this.bodyMonoImageMap.put(avatar, result);
        }
        return result;
    }

    /**
     * 国に登録された墓イメージを返す。
     *
     * @return 墓イメージ
     */
    public BufferedImage getGraveImage(){
        BufferedImage result = getParentLand().getGraveIconImage();
        return result;
    }

    /**
     * 国に登録された墓イメージ(大)を返す。
     *
     * @return 墓イメージ(大)
     */
    public BufferedImage getGraveBodyImage(){
        BufferedImage result = getParentLand().getGraveBodyImage();
        return result;
    }

    /**
     * 村にアクセスするためのCGIクエリーを返す。
     *
     * @return CGIクエリー
     */
    public String getCGIQuery(){
        StringBuilder result = new StringBuilder();
        result.append("?vid=").append(getVillageID());
        return result.toString();
    }

    /**
     * 次回更新時を設定する。
     *
     * @param month 月
     * @param day 日
     * @param hour 時
     * @param minute 分
     */
    public void setLimit(int month, int day, int hour, int minute){
        this.limitMonth = month;
        this.limitDay = day;
        this.limitHour = hour;
        this.limitMinute = minute;
        return;
    }

    /**
     * 次回更新月を返す。
     *
     * @return 更新月(1-12)
     */
    public int getLimitMonth(){
        return this.limitMonth;
    }

    /**
     * 次回更新日を返す。
     *
     * @return 更新日(1-31)
     */
    public int getLimitDay(){
        return this.limitDay;
    }

    /**
     * 次回更新時を返す。
     *
     * @return 更新時(0-23)
     */
    public int getLimitHour(){
        return this.limitHour;
    }

    /**
     * 次回更新分を返す。
     *
     * @return 更新分(0-59)
     */
    public int getLimitMinute(){
        return this.limitMinute;
    }

    /**
     * 有効な村か否か判定する。
     *
     * @return 無効な村ならfalse
     */
    public boolean isValid(){
        return this.isValid;
    }

    /**
     * Periodリストの指定したインデックスにPeriodを上書きする。
     *
     * <p>リストのサイズと同じインデックスを指定する事が許される。
     * その場合の動作はList.addと同じ。
     *
     * @param index Periodリストのインデックス。
     * @param period 上書きするPeriod
     * @throws java.lang.IndexOutOfBoundsException インデックスの指定がおかしい
     */
    public void setPeriod(int index, Period period)
            throws IndexOutOfBoundsException{
        int listSize = this.periodList.size();
        if(index == listSize){
            this.periodList.add(period);
        }else if(index < listSize){
            this.periodList.set(index, period);
        }else{
            throw new IndexOutOfBoundsException();
        }
        return;
    }

    /**
     * アンカーに一致する会話(Talk)のリストを取得する。
     *
     * @param anchor アンカー
     * @return Talkのリスト
     */
    public List<Talk> getTalkListFromAnchor(Anchor anchor){
        List<Talk> result = new LinkedList<>();

        /* G国アンカー対応 */
        if(anchor.hasTalkNo()){
            // 事前に全Periodの全会話がロードされているのが前提
            for(Period period : this.periodList){
                Talk talk = period.getNumberedTalk(anchor.getTalkNo());
                if(talk == null) continue;
                result.add(talk);
            }
            return result;
        }

        Period anchorPeriod = getPeriod(anchor);
        if(anchorPeriod == null) return result;

        // 事前にアンカー対象Periodの全会話がロードされているのが前提

        for(Topic topic : anchorPeriod.getTopicList()){
            if( ! (topic instanceof Talk) ) continue;
            Talk talk = (Talk) topic;
            if(talk.getHour()   != anchor.getHour()  ) continue;
            if(talk.getMinute() != anchor.getMinute()) continue;
            result.add(talk);
        }
        return result;
    }

    /**
     * 全Periodの発言データをアンロードする。
     */
    public void unloadPeriods(){
        for(Period period : this.periodList){
            period.unload();
        }
        return;
    }

    /**
     * この村がローカルなアーカイブに由来するものであるか判定する。
     *
     * @return ローカルなアーカイブによる村であればtrue
     */
    public boolean isLocalArchive(){
        return this.isLocalArchive;
    }

    /**
     * この村がローカルなアーカイブに由来するものであるか設定する。
     *
     * @param flag ローカルなアーカイブによる村であればtrue
     */
    public void setLocalArchive(boolean flag){
        this.isLocalArchive = flag;
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>村の文字列表現を返す。
     * 村の名前と等しい。
     *
     * @return 村の名前
     */
    @Override
    public String toString(){
        return getVillageFullName();
    }

}
