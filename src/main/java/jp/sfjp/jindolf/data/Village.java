/*
 * Village
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.sfjp.jindolf.net.HtmlSequence;
import jp.sfjp.jindolf.net.ServerAccess;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.LandState;
import jp.sourceforge.jindolf.corelib.PeriodType;
import jp.sourceforge.jindolf.corelib.VillageState;
import jp.sourceforge.jindolf.parser.DecodedContent;
import jp.sourceforge.jindolf.parser.HtmlAdapter;
import jp.sourceforge.jindolf.parser.HtmlParseException;
import jp.sourceforge.jindolf.parser.HtmlParser;
import jp.sourceforge.jindolf.parser.PageType;
import jp.sourceforge.jindolf.parser.SeqRange;

/**
 * いわゆる「村」。
 */
public class Village implements Comparable<Village> {

    private static final int GID_MIN = 3;

    private static final Comparator<Village> VILLAGE_COMPARATOR =
            new VillageComparator();

    private static final HtmlParser PARSER = new HtmlParser();
    private static final VillageHeadHandler HANDLER =
            new VillageHeadHandler();

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    static{
        PARSER.setBasicHandler   (HANDLER);
        PARSER.setSysEventHandler(HANDLER);
        PARSER.setTalkHandler    (HANDLER);
    }


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


    /**
     * Villageを生成する。
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
     * 村同士を比較するためのComparatorを返す。
     * @return Comparatorインスタンス
     */
    public static Comparator<Village> comparator(){
        return VILLAGE_COMPARATOR;
    }

    /**
     * 人狼BBSサーバからPeriod一覧情報が含まれたHTMLを取得し、
     * Periodリストを更新する。
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

    /**
     * 所属する国を返す。
     * @return 村の所属する国（Land）
     */
    public Land getParentLand(){
        return this.parentLand;
    }

    /**
     * 村のID文字列を返す。
     * @return 村ID
     */
    public String getVillageID(){
        return this.villageID;
    }

    /**
     * 村のID数値を返す。
     * @return 村ID
     */
    public int getVillageIDNum(){
        return this.villageIDNum;
    }

    /**
     * 村の名前を返す。
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
     * @return 村の長い名前
     */
    public String getVillageFullName(){
        return this.villageName;
    }

    /**
     * 村の状態を返す。
     * @return 村の状態
     */
    public VillageState getState(){
        return this.state;
    }

    /**
     * 村の状態を設定する。
     * @param state 村の状態
     */
    public void setState(VillageState state){
        this.state = state;
        return;
    }

    /**
     * プロローグを返す。
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
     * @param day 日付
     * @return Period
     */
    public Period getProgress(int day){
        for(Period period : this.periodList){
            if(   period.isProgress()
               && period.getDay() == day ) return period;
        }
        return null;
    }

    /**
     * PROGRESS状態のPeriodの総数を返す。
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
     * プロローグやエピローグへのアクセスも可能。
     * @param day Periodインデックス
     * @return Period
     */
    public Period getPeriod(int day){
        return this.periodList.get(day);
    }

    /**
     * 指定されたアンカーの対象のPeriodを返す。
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
     * @return Period総数
     */
    public int getPeriodSize(){
        return this.periodList.size();
    }

    /**
     * Periodへのリストを返す。
     * @return Periodのリスト。
     */
    public List<Period> getPeriodList(){
        return this.unmodList;
    }

    /**
     * 指定した名前で村に登録されているAvatarを返す。
     * @param fullName Avatarの名前
     * @return Avatar
     */
    public Avatar getAvatar(String fullName){
        // TODO CharSequenceにできない？
        Avatar avatar;

        avatar = Avatar.getPredefinedAvatar(fullName);
        if( avatar != null ){
            preloadAvatarFace(avatar);
            return avatar;
        }

        avatar = this.avatarMap.get(fullName);
        if( avatar != null ){
            preloadAvatarFace(avatar);
            return avatar;
        }

        return null;
    }

    /**
     * Avatarの顔画像を事前にロードする。
     * @param avatar Avatar
     */
    private void preloadAvatarFace(Avatar avatar){
        if(this.faceImageMap.get(avatar) != null) return;

        Land land = getParentLand();
        LandDef landDef = land.getLandDef();

        String template = landDef.getFaceURITemplate();
        int serialNo = avatar.getIdNum();
        String uri = MessageFormat.format(template, serialNo);

        BufferedImage image = land.downloadImage(uri);
        if(image == null) image = GUIUtils.getNoImage();

        this.faceImageMap.put(avatar, image);

        return;
    }

    /**
     * Avatarを村に登録する。
     * @param avatar Avatar
     */
    // 未知のAvatar出現時の処理が不完全
    public void addAvatar(Avatar avatar){
        if(avatar == null) return;
        String fullName = avatar.getFullName();
        this.avatarMap.put(fullName, avatar);

        preloadAvatarFace(avatar);

        return;
    }

    /**
     * 村に登録されたAvatarの顔イメージを返す。
     * @param avatar Avatar
     * @return 顔イメージ
     */
    // TODO 失敗したらプロローグを強制読み込みして再トライしたい
    public BufferedImage getAvatarFaceImage(Avatar avatar){
        return this.faceImageMap.get(avatar);
    }

    /**
     * 村に登録されたAvatarの全身像イメージを返す。
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
        String uri = MessageFormat.format(template, serialNo);

        result = land.downloadImage(uri);
        if(result == null) result = GUIUtils.getNoImage();

        this.bodyImageMap.put(avatar, result);

        return result;
    }

    /**
     * 村に登録されたAvatarのモノクロ顔イメージを返す。
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
     * @return 墓イメージ
     */
    public BufferedImage getGraveImage(){
        BufferedImage result = getParentLand().getGraveIconImage();
        return result;
    }

    /**
     * 国に登録された墓イメージ(大)を返す。
     * @return 墓イメージ(大)
     */
    public BufferedImage getGraveBodyImage(){
        BufferedImage result = getParentLand().getGraveBodyImage();
        return result;
    }

    /**
     * 村にアクセスするためのCGIクエリーを返す。
     * @return CGIクエリー
     */
    public CharSequence getCGIQuery(){
        StringBuilder result = new StringBuilder();
        result.append("?vid=").append(getVillageID());
        return result;
    }

    /**
     * 次回更新月を返す。
     * @return 更新月(1-12)
     */
    public int getLimitMonth(){
        return this.limitMonth;
    }

    /**
     * 次回更新日を返す。
     * @return 更新日(1-31)
     */
    public int getLimitDay(){
        return this.limitDay;
    }

    /**
     * 次回更新時を返す。
     * @return 更新時(0-23)
     */
    public int getLimitHour(){
        return this.limitHour;
    }

    /**
     * 次回更新分を返す。
     * @return 更新分(0-59)
     */
    public int getLimitMinute(){
        return this.limitMinute;
    }

    /**
     * 有効な村か否か判定する。
     * @return 無効な村ならfalse
     */
    public boolean isValid(){
        return this.isValid;
    }

    /**
     * Periodリストの指定したインデックスにPeriodを上書きする。
     * リストのサイズと同じインデックスを指定する事が許される。
     * その場合の動作はList.addと同じ。
     * @param index Periodリストのインデックス。
     * @param period 上書きするPeriod
     * @throws java.lang.IndexOutOfBoundsException インデックスの指定がおかしい
     */
    private void setPeriod(int index, Period period)
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
     * @param anchor アンカー
     * @return Talkのリスト
     * @throws java.io.IOException おそらくネットワークエラー
     */
    public List<Talk> getTalkListFromAnchor(Anchor anchor)
            throws IOException{
        List<Talk> result = new LinkedList<>();

        /* G国アンカー対応 */
        if(anchor.hasTalkNo()){
            // 事前に全Periodがロードされているのが前提
            for(Period period : this.periodList){
                Talk talk = period.getNumberedTalk(anchor.getTalkNo());
                if(talk == null) continue;
                result.add(talk);
            }
            return result;
        }

        Period anchorPeriod = getPeriod(anchor);
        if(anchorPeriod == null) return result;

        Period.parsePeriod(anchorPeriod, false);

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
     * {@inheritDoc}
     * 二つの村を順序付ける。
     * @param village {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(Village village){
        int cmpResult = VILLAGE_COMPARATOR.compare(this, village);
        return cmpResult;
    }

    /**
     * {@inheritDoc}
     * 二つの村が等しいか調べる。
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if( ! (obj instanceof Village) ) return false;
        Village village = (Village) obj;

        if( getParentLand() != village.getParentLand() ) return false;

        int cmpResult = compareTo(village);
        if(cmpResult == 0) return true;
        return false;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        int homeHash = getParentLand().hashCode();
        int vidHash = getVillageID().hashCode();
        int result = homeHash ^ vidHash;
        return result;
    }

    /**
     * {@inheritDoc}
     * 村の文字列表現を返す。
     * 村の名前と等しい。
     * @return 村の名前
     */
    @Override
    public String toString(){
        return getVillageFullName();
    }


    /**
     * Period一覧取得用ハンドラ。
     */
    private static class VillageHeadHandler extends HtmlAdapter{

        private Village village = null;

        private boolean hasPrologue;
        private boolean hasProgress;
        private boolean hasEpilogue;
        private boolean hasDone;
        private int maxProgress;

        /**
         * コンストラクタ。
         */
        public VillageHeadHandler(){
            super();
            return;
        }

        /**
         * 更新対象の村を設定する。
         * @param village 村
         */
        public void setVillage(Village village){
            this.village = village;
            return;
        }

        /**
         * リセットを行う。
         */
        public void reset(){
            this.hasPrologue = false;
            this.hasProgress = false;
            this.hasEpilogue = false;
            this.hasDone = false;
            this.maxProgress = 0;
            return;
        }

        /**
         * パース結果から村の状態を算出する。
         * @return 村の状態
         */
        public VillageState getVillageState(){
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
         * @param content {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void startParse(DecodedContent content)
                throws HtmlParseException{
            reset();
            return;
        }

        /**
         * {@inheritDoc}
         * 自動判定の結果が日ページでなければ例外を投げる。
         * @param type {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc} 意図しないページが来た。
         */
        @Override
        public void pageType(PageType type) throws HtmlParseException{
            if(type != PageType.PERIOD_PAGE){
                throw new HtmlParseException(
                        "日ページが必要です。");
            }
            return;
        }

        /**
         * {@inheritDoc}
         * @param month {@inheritDoc}
         * @param day {@inheritDoc}
         * @param hour {@inheritDoc}
         * @param minute {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void commitTime(int month, int day,
                                int hour, int minute)
                throws HtmlParseException{
            this.village.limitMonth  = month;
            this.village.limitDay    = day;
            this.village.limitHour   = hour;
            this.village.limitMinute = minute;

            return;
        }

        /**
         * {@inheritDoc}
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
                throws HtmlParseException{
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
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void endParse() throws HtmlParseException{
            Land land = this.village.getParentLand();
            LandDef landDef = land.getLandDef();
            LandState landState = landDef.getLandState();

            VillageState villageState = getVillageState();
            if(villageState == VillageState.UNKNOWN){
                this.village.setState(villageState);
                this.village.periodList.clear();
                LOGGER.warning("村の状況を読み取れません");
                return;
            }

            if(landState == LandState.ACTIVE){
                this.village.setState(villageState);
            }else{
                this.village.setState(VillageState.GAMEOVER);
            }

            modifyPeriodList();

            return;
        }

        /**
         * 抽出したリンク情報に伴いPeriodリストを更新する。
         * まだPeriodデータのロードは行われない。
         * ゲーム進行中の村で更新時刻をまたいで更新が行われた場合、
         * 既存のPeriodリストが伸張する場合がある。
         */
        private void modifyPeriodList(){
            Period lastPeriod = null;

            if(this.hasPrologue){
                Period prologue = this.village.getPrologue();
                if(prologue == null){
                    lastPeriod = new Period(this.village,
                                            PeriodType.PROLOGUE, 0);
                    this.village.setPeriod(0, lastPeriod);
                }else{
                    lastPeriod = prologue;
                }
            }

            if(this.hasProgress){
                for(int day = 1; day <= this.maxProgress; day++){
                    Period progress = this.village.getProgress(day);
                    if(progress == null){
                        lastPeriod = new Period(this.village,
                                                PeriodType.PROGRESS, day);
                        this.village.setPeriod(day, lastPeriod);
                    }else{
                        lastPeriod = progress;
                    }
                }
            }

            if(this.hasEpilogue){
                Period epilogue = this.village.getEpilogue();
                if(epilogue == null){
                    lastPeriod = new Period(this.village,
                                            PeriodType.EPILOGUE,
                                            this.maxProgress +1);
                    this.village.setPeriod(this.maxProgress +1, lastPeriod);
                }else{
                    lastPeriod = epilogue;
                }
            }

            assert this.village.getPeriodSize() > 0;
            assert lastPeriod != null;

            // 念のためチョップ。
            // リロードで村が縮むわけないじゃん。みんな大げさだなあ
            while(this.village.periodList.getLast() != lastPeriod){
                this.village.periodList.removeLast();
            }

            if(this.village.getState() != VillageState.GAMEOVER){
                lastPeriod.setHot(true);
            }

            return;
        }

    }


    /**
     * 村同士を比較するためのComparator。
     */
    private static class VillageComparator implements Comparator<Village> {

        /**
         * コンストラクタ。
         */
        public VillageComparator(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * @param v1 {@inheritDoc}
         * @param v2 {@inheritDoc}
         * @return {@inheritDoc}
         */
        @Override
        public int compare(Village v1, Village v2){
            int v1Num;
            if(v1 == null) v1Num = Integer.MIN_VALUE;
            else           v1Num = v1.getVillageIDNum();

            int v2Num;
            if(v2 == null) v2Num = Integer.MIN_VALUE;
            else           v2Num = v2.getVillageIDNum();

            return v1Num - v2Num;
        }

    }

}
