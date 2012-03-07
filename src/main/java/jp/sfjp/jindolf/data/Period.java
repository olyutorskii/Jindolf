/*
 * daily period in village
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.sfjp.jindolf.net.HtmlSequence;
import jp.sfjp.jindolf.net.ServerAccess;
import jp.sfjp.jindolf.util.StringUtils;
import jp.sourceforge.jindolf.corelib.EventFamily;
import jp.sourceforge.jindolf.corelib.GameRole;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.PeriodType;
import jp.sourceforge.jindolf.corelib.SysEventType;
import jp.sourceforge.jindolf.corelib.TalkType;
import jp.sourceforge.jindolf.corelib.Team;
import jp.sourceforge.jindolf.corelib.VillageState;
import jp.sourceforge.jindolf.parser.DecodedContent;
import jp.sourceforge.jindolf.parser.EntityConverter;
import jp.sourceforge.jindolf.parser.HtmlAdapter;
import jp.sourceforge.jindolf.parser.HtmlParseException;
import jp.sourceforge.jindolf.parser.HtmlParser;
import jp.sourceforge.jindolf.parser.PageType;
import jp.sourceforge.jindolf.parser.SeqRange;

/**
 * いわゆる「日」。
 * 村の進行の一区切り。プロローグやエピローグも含まれる。
 *
 * 将来、24時間更新でなくなる可能性の考慮が必要。
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

    private final List<Topic> topicList = new LinkedList<Topic>();
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
        if(   homeVillage == null
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
     * falseならまだ読み込んで無い時のみ読み込み。
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
        Set<Avatar> result = new HashSet<Avatar>();

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

    /**
     * Periodパース用ハンドラ。
     */
    private static class PeriodHandler extends HtmlAdapter{

        private static final int TALKTYPE_NUM = TalkType.values().length;

        private final EntityConverter converter =
                new EntityConverter();

        private final Map<Avatar, int[]> countMap =
                new HashMap<Avatar, int[]>();

        private Period period = null;

        private TalkType talkType;
        private Avatar avatar;
        private int talkNo;
        private String anchorId;
        private int talkHour;
        private int talkMinute;
        private DecodedContent talkContent = null;

        private EventFamily eventFamily;
        private SysEventType sysEventType;
        private DecodedContent eventContent = null;
        private final List<Avatar> avatarList = new LinkedList<Avatar>();
        private final List<GameRole> roleList = new LinkedList<GameRole>();
        private final List<Integer> integerList = new LinkedList<Integer>();
        private final List<CharSequence>  charseqList =
            new LinkedList<CharSequence>();

        /**
         * コンストラクタ。
         */
        public PeriodHandler(){
            super();
            return;
        }

        /**
         * パース結果を格納するPeriodを設定する。
         * @param period Period
         */
        public void setPeriod(Period period){
            this.period = period;
            return;
        }

        /**
         * 文字列断片からAvatarを得る。
         * 村に未登録のAvatarであればついでに登録される。
         * @param content 文字列
         * @param range 文字列内のAvatarフルネームを示す領域
         * @return Avatar
         */
        private Avatar toAvatar(DecodedContent content, SeqRange range){
            Village village = this.period.getVillage();
            String fullName = this.converter
                                  .convert(content, range)
                                  .toString();
            Avatar result = village.getAvatar(fullName);
            if(result == null){
                result = new Avatar(fullName);
                village.addAvatar(result);
            }

            return result;
        }

        /**
         * Avatar別、会話種ごとに発言回数をカウントする。
         * 1から始まる。
         * @param targetAvatar 対象Avatar
         * @param targetType 対象会話種
         * @return カウント数
         */
        private int countUp(Avatar targetAvatar, TalkType targetType){
            int[] countArray = this.countMap.get(targetAvatar);
            if(countArray == null){
                countArray = new int[TALKTYPE_NUM];
                this.countMap.put(targetAvatar, countArray);
            }
            int count = ++countArray[targetType.ordinal()];
            return count;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void startParse(DecodedContent content)
                throws HtmlParseException{
            this.period.loginName = null;
            this.period.topicList.clear();
            this.countMap.clear();
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param loginRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void loginName(DecodedContent content, SeqRange loginRange)
                throws HtmlParseException{
            DecodedContent loginName =
                    this.converter.convert(content, loginRange);

            this.period.loginName = loginName.toString();

            return;
        }

        /**
         * {@inheritDoc}
         * @param type {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void pageType(PageType type) throws HtmlParseException{
            if(type != PageType.PERIOD_PAGE){
                throw new HtmlParseException(
                        "意図しないページを読み込もうとしました。");
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
        public void commitTime(int month, int day, int hour, int minute)
                throws HtmlParseException{
            this.period.limitHour   = hour;
            this.period.limitMinute = minute;
            return;
        }

        /**
         * {@inheritDoc}
         * 自分へのリンクが無いかチェックする。
         * 自分へのリンクが見つかればこのPeriodを非Hotにする。
         * 自分へのリンクがあるということは、
         * 今読んでるHTMLは別のPeriodのために書かれたものということ。
         * 考えられる原因は、HotだったPeriodがゲーム進行に従い
         * Hotでなくなったこと。
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

            if(this.period.getType() != periodType) return;

            if(   periodType == PeriodType.PROGRESS
               && this.period.getDay() != day ){
                return;
            }

            if( ! anchorRange.isValid() ) return;

            this.period.setHot(false);

            return;
        }

        /**
         * {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void startTalk() throws HtmlParseException{
            this.talkType = null;
            this.avatar = null;
            this.talkNo = -1;
            this.anchorId = null;
            this.talkHour = -1;
            this.talkMinute = -1;
            this.talkContent = new DecodedContent(100 + 1);

            return;
        }

        /**
         * {@inheritDoc}
         * @param type {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void talkType(TalkType type)
                throws HtmlParseException{
            this.talkType = type;
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param avatarRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void talkAvatar(DecodedContent content, SeqRange avatarRange)
                throws HtmlParseException{
            this.avatar = toAvatar(content, avatarRange);
            return;
        }

        /**
         * {@inheritDoc}
         * @param hour {@inheritDoc}
         * @param minute {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void talkTime(int hour, int minute)
                throws HtmlParseException{
            this.talkHour = hour;
            this.talkMinute = minute;
            return;
        }

        /**
         * {@inheritDoc}
         * @param tno {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void talkNo(int tno) throws HtmlParseException{
            this.talkNo = tno;
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param idRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void talkId(DecodedContent content, SeqRange idRange)
                throws HtmlParseException{
            this.anchorId = content.subSequence(idRange.getStartPos(),
                                                idRange.getEndPos()   )
                                   .toString();
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param textRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void talkText(DecodedContent content, SeqRange textRange)
                throws HtmlParseException{
            this.converter.append(this.talkContent, content, textRange);
            return;
        }

        /**
         * {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void talkBreak()
                throws HtmlParseException{
            this.talkContent.append('\n');
            return;
        }

        /**
         * {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void endTalk() throws HtmlParseException{
            Talk talk = new Talk(this.period,
                                 this.talkType,
                                 this.avatar,
                                 this.talkNo,
                                 this.anchorId,
                                 this.talkHour, this.talkMinute,
                                 this.talkContent );

            int count = countUp(this.avatar, this.talkType);
            talk.setCount(count);

            this.period.addTopic(talk);

            this.talkType = null;
            this.avatar = null;
            this.talkNo = -1;
            this.anchorId = null;
            this.talkHour = -1;
            this.talkMinute = -1;
            this.talkContent = null;

            return;
        }

        /**
         * {@inheritDoc}
         * @param family {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void startSysEvent(EventFamily family)
                throws HtmlParseException{
            this.eventFamily = family;
            this.sysEventType = null;
            this.eventContent = new DecodedContent();
            this.avatarList.clear();
            this.roleList.clear();
            this.integerList.clear();
            this.charseqList.clear();
            return;
        }

        /**
         * {@inheritDoc}
         * @param type {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventType(SysEventType type)
                throws HtmlParseException{
            this.sysEventType = type;
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param contentRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventContent(DecodedContent content,
                                      SeqRange contentRange)
                throws HtmlParseException{
            this.converter.append(this.eventContent, content, contentRange);
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param anchorRange {@inheritDoc}
         * @param contentRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventContentAnchor(DecodedContent content,
                                             SeqRange anchorRange,
                                             SeqRange contentRange)
                throws HtmlParseException{
            this.converter.append(this.eventContent, content, contentRange);
            return;
        }

        /**
         * {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventContentBreak() throws HtmlParseException{
            this.eventContent.append('\n');
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param entryNo {@inheritDoc}
         * @param avatarRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventOnStage(DecodedContent content,
                                      int entryNo,
                                      SeqRange avatarRange)
                throws HtmlParseException{
            Avatar newAvatar = toAvatar(content, avatarRange);
            this.integerList.add(entryNo);
            this.avatarList.add(newAvatar);
            return;
        }

        /**
         * {@inheritDoc}
         * @param role {@inheritDoc}
         * @param num {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventOpenRole(GameRole role, int num)
                throws HtmlParseException{
            this.roleList.add(role);
            this.integerList.add(num);
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param avatarRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventMurdered(DecodedContent content,
                                       SeqRange avatarRange)
                throws HtmlParseException{
            Avatar murdered = toAvatar(content, avatarRange);
            this.avatarList.add(murdered);
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param avatarRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventSurvivor(DecodedContent content,
                                       SeqRange avatarRange)
                throws HtmlParseException{
            Avatar survivor = toAvatar(content, avatarRange);
            this.avatarList.add(survivor);
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param voteByRange {@inheritDoc}
         * @param voteToRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventCounting(DecodedContent content,
                                       SeqRange voteByRange,
                                       SeqRange voteToRange)
                throws HtmlParseException{
            if(voteByRange.isValid()){
                Avatar voteBy = toAvatar(content, voteByRange);
                this.avatarList.add(voteBy);
            }
            Avatar voteTo = toAvatar(content, voteToRange);
            this.avatarList.add(voteTo);
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param voteByRange {@inheritDoc}
         * @param voteToRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventCounting2(DecodedContent content,
                                        SeqRange voteByRange,
                                        SeqRange voteToRange)
                throws HtmlParseException{
            sysEventCounting(content, voteByRange, voteToRange);
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param avatarRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventSuddenDeath(DecodedContent content,
                                           SeqRange avatarRange)
                throws HtmlParseException{
            Avatar suddenDeath = toAvatar(content, avatarRange);
            this.avatarList.add(suddenDeath);
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param avatarRange {@inheritDoc}
         * @param anchorRange {@inheritDoc}
         * @param loginRange {@inheritDoc}
         * @param isLiving {@inheritDoc}
         * @param role {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventPlayerList(DecodedContent content,
                                          SeqRange avatarRange,
                                          SeqRange anchorRange,
                                          SeqRange loginRange,
                                          boolean isLiving,
                                          GameRole role )
                throws HtmlParseException{
            Avatar who = toAvatar(content, avatarRange);

            CharSequence anchor;
            if(anchorRange.isValid()){
                anchor = this.converter.convert(content, anchorRange);
            }else{
                anchor = "";
            }
            CharSequence account = this.converter
                                       .convert(content, loginRange);

            Integer liveOrDead;
            if(isLiving) liveOrDead = Integer.valueOf(1);
            else         liveOrDead = Integer.valueOf(0);

            this.avatarList.add(who);
            this.charseqList.add(anchor);
            this.charseqList.add(account);
            this.integerList.add(liveOrDead);
            this.roleList.add(role);

            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param avatarRange {@inheritDoc}
         * @param votes {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventExecution(DecodedContent content,
                                        SeqRange avatarRange,
                                        int votes )
                throws HtmlParseException{
            Avatar who = toAvatar(content, avatarRange);

            this.avatarList.add(who);
            this.integerList.add(votes);

            return;
        }

        /**
         * {@inheritDoc}
         * @param hour {@inheritDoc}
         * @param minute {@inheritDoc}
         * @param minLimit {@inheritDoc}
         * @param maxLimit {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventAskEntry(int hour, int minute,
                                       int minLimit, int maxLimit)
                throws HtmlParseException{
            this.integerList.add(hour * 60 + minute);
            this.integerList.add(minLimit);
            this.integerList.add(maxLimit);
            return;
        }

        /**
         * {@inheritDoc}
         * @param hour {@inheritDoc}
         * @param minute {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventAskCommit(int hour, int minute)
                throws HtmlParseException{
            this.integerList.add(hour * 60 + minute);
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param avatarRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventNoComment(DecodedContent content,
                                        SeqRange avatarRange)
                throws HtmlParseException{
            Avatar noComAvatar = toAvatar(content, avatarRange);
            this.avatarList.add(noComAvatar);
            return;
        }

        /**
         * {@inheritDoc}
         * @param winner {@inheritDoc}
         * @param hour {@inheritDoc}
         * @param minute {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventStayEpilogue(Team winner, int hour, int minute)
                throws HtmlParseException{
            GameRole role = null;

            switch(winner){
            case VILLAGE: role = GameRole.INNOCENT; break;
            case WOLF:    role = GameRole.WOLF;     break;
            case HAMSTER: role = GameRole.HAMSTER;  break;
            default: assert false; break;
            }

            this.roleList.add(role);
            this.integerList.add(hour * 60 + minute);

            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param guardByRange {@inheritDoc}
         * @param guardToRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventGuard(DecodedContent content,
                                    SeqRange guardByRange,
                                    SeqRange guardToRange)
                throws HtmlParseException{
            Avatar guardBy = toAvatar(content, guardByRange);
            Avatar guardTo = toAvatar(content, guardToRange);
            this.avatarList.add(guardBy);
            this.avatarList.add(guardTo);
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param judgeByRange {@inheritDoc}
         * @param judgeToRange {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void sysEventJudge(DecodedContent content,
                                    SeqRange judgeByRange,
                                    SeqRange judgeToRange)
                throws HtmlParseException{
            Avatar judgeBy = toAvatar(content, judgeByRange);
            Avatar judgeTo = toAvatar(content, judgeToRange);
            this.avatarList.add(judgeBy);
            this.avatarList.add(judgeTo);
            return;
        }

        /**
         * {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void endSysEvent() throws HtmlParseException{
            SysEvent event = new SysEvent();
            event.setEventFamily(this.eventFamily);
            event.setSysEventType(this.sysEventType);
            event.setContent(this.eventContent);
            event.addAvatarList(this.avatarList);
            event.addRoleList(this.roleList);
            event.addIntegerList(this.integerList);
            event.addCharSequenceList(this.charseqList);

            this.period.addTopic(event);

            if(   this.sysEventType == SysEventType.MURDERED
               || this.sysEventType == SysEventType.NOMURDER ){
                for(Topic topic : this.period.topicList){
                    if( ! (topic instanceof Talk) ) continue;
                    Talk talk = (Talk) topic;
                    if(talk.getTalkType() != TalkType.WOLFONLY) continue;
                    if( ! StringUtils
                         .isTerminated(talk.getDialog(),
                                       "！\u0020今日がお前の命日だ！") ){
                        continue;
                    }
                    talk.setCount(-1);
                    this.countMap.clear();
                }
            }

            this.eventFamily = null;
            this.sysEventType = null;
            this.eventContent = null;
            this.avatarList.clear();
            this.roleList.clear();
            this.integerList.clear();
            this.charseqList.clear();

            return;
        }

        /**
         * {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void endParse() throws HtmlParseException{
            return;
        }

        // TODO 村名のチェックは不要か？
    }

}
