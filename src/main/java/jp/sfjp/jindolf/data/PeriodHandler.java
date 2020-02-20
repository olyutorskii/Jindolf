/*
 * period handler
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jp.osdn.jindolf.parser.EntityConverter;
import jp.osdn.jindolf.parser.HtmlAdapter;
import jp.osdn.jindolf.parser.HtmlParseException;
import jp.osdn.jindolf.parser.PageType;
import jp.osdn.jindolf.parser.SeqRange;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sourceforge.jindolf.corelib.EventFamily;
import jp.sourceforge.jindolf.corelib.GameRole;
import jp.sourceforge.jindolf.corelib.PeriodType;
import jp.sourceforge.jindolf.corelib.SysEventType;
import jp.sourceforge.jindolf.corelib.TalkType;
import jp.sourceforge.jindolf.corelib.Team;


/**
 * 各日(Period)のHTMLをパースし、
 * 会話やイベントの通知を受け取るためのハンドラ。
 *
 * <p>パース終了時には、
 * あらかじめ指定したPeriodインスタンスに
 * 会話やイベントのリストが適切に更新される。
 *
 * <p>各種ビューが対応するまでの間、Unicodeの非BMP面文字には代替文字で対処。
 *
 * <p>※ 人狼BBS:G国におけるG2087村のエピローグが終了した段階で、
 * 人狼BBSは過去ログの提供しか行っていない。
 * だがこのクラスには進行中の村の各日をパースするための
 * 冗長な処理が若干残っている。
 */
public class PeriodHandler extends HtmlAdapter {

    private static final int TALKTYPE_NUM = TalkType.values().length;

    private final EntityConverter converter =
            new EntityConverter(true);
    // TODO: 非BMP面文字に対応するまでの暫定措置

    /** 非別、Avatar別、会話種別の会話通し番号。 */
    private final Map<Avatar, int[]> countMap =
            new HashMap<>();

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
    private final List<Avatar> avatarList = new LinkedList<>();
    private final List<GameRole> roleList = new LinkedList<>();
    private final List<Integer> integerList = new LinkedList<>();
    private final List<CharSequence>  charseqList =
        new LinkedList<>();


    /**
     * コンストラクタ。
     */
    public PeriodHandler(){
        super();
        return;
    }


    /**
     * 更新対象のPeriodを設定する。
     *
     * @param period Period
     */
    public void setPeriod(Period period){
        this.period = period;
        return;
    }

    /**
     * フルネーム文字列からAvatarインスタンスを得る。
     *
     * <p>村に未登録のAvatarであればついでに登録される。
     *
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
     * パース中の各種コンテキストをリセットする。
     */
    public void reset(){
        this.countMap.clear();

        resetTalkContext();
        resetEventContext();

        return;
    }

    /**
     * パース中の会話コンテキストをリセットする。
     */
    public void resetTalkContext(){
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
     * パース中のイベントコンテキストをリセットする。
     */
    public void resetEventContext(){
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
     *
     * @param content {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void startParse(DecodedContent content)
            throws HtmlParseException{
        reset();

        this.period.setLoginName(null);
        this.period.clearTopicList();

        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>各PeriodのHTML上部にあるログイン名が通知されたのなら、
     * それはPOSTやCookieを使ってのログインに成功したと言うこと。
     *
     * <p>ログイン名中の文字実体参照は展開される。
     *
     * <p>※ 2020-02現在、人狼BBS各国へのログインは無意味。
     *
     * @param content {@inheritDoc}
     * @param loginRange {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void loginName(DecodedContent content, SeqRange loginRange)
            throws HtmlParseException{
        DecodedContent loginName =
                this.converter.convert(content, loginRange);

        this.period.setLoginName(loginName.toString());

        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>受信したHTMLがPeriodページでないのならパースを中止する。
     *
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
     *
     * <p>月日の通知は無視される。
     *
     * @param month {@inheritDoc}
     * @param day {@inheritDoc}
     * @param hour {@inheritDoc}
     * @param minute {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void commitTime(int month, int day, int hour, int minute)
            throws HtmlParseException{
        this.period.setLimit(hour, minute);
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>このPeriodが進行中(Hot!)か否か判定する。
     *
     * <p>PeriodのHTML内に自分自身へのリンクが無いかチェックする。
     * 自分へのリンクが見つかればこのPeriodを非Hotにする。
     * 自分へのリンクがあるということは、
     * 今受信しているHTMLは別のPeriodから辿るために書かれたものということ。
     *
     * <p>原因としては、HotだったPeriodがゲーム進行に従い
     * Hotでなくなったことなどが考えられる。
     *
     * <p>各Periodの種別と日は、
     * 村情報受信を通じて事前に設定されていなければならない。
     *
     * <p>※ 2020-02現在、HotなPeriodを受信する機会はないはず。
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
                           int day )
            throws HtmlParseException{
        if(this.period.getType() != periodType) return;

        boolean isProgress = periodType == PeriodType.PROGRESS;
        boolean dayMatch = this.period.getDay() == day;
        if(isProgress && ! dayMatch){
            return;
        }

        if( ! anchorRange.isValid() ) return;

        this.period.setHot(false);

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void startTalk() throws HtmlParseException{
        resetTalkContext();
        this.talkContent = new DecodedContent(100 + 1);
        return;
    }

    /**
     * {@inheritDoc}
     *
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
     *
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
     *
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
     *
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
     *
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
     *
     * <p>会話中の文字実体参照は展開される。
     *
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
     *
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void talkBreak()
            throws HtmlParseException{
        this.talkContent.append('\n');
        return;
    }

    /**
     * 日別、Avatar別、会話種ごとに発言回数をインクリメントする。
     *
     * @param targetAvatar 対象Avatar
     * @param targetType 対象会話種
     * @return 現時点でのカウント数
     */
    private int countUp(Avatar targetAvatar, TalkType targetType){
        int[] countArray = this.countMap.get(targetAvatar);
        if(countArray == null){
            countArray = new int[TALKTYPE_NUM];
            this.countMap.put(targetAvatar, countArray);
        }

        int typeIdx = targetType.ordinal();
        int count = ++countArray[typeIdx];
        return count;
    }

    /**
     * {@inheritDoc}
     *
     * <p>パース中の各種コンテキストから会話を組み立て、
     * Periodに追加する。
     *
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

        resetTalkContext();

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param family {@inheritDoc}
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void startSysEvent(EventFamily family)
            throws HtmlParseException{
        resetEventContext();

        this.eventFamily = family;
        this.eventContent = new DecodedContent();

        return;
    }

    /**
     * {@inheritDoc}
     *
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
     *
     * <p>イベント文字列中の文字実体参照は展開される。
     *
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
     *
     * <p>イベント文内Aタグ内容の文字実体参照は展開される。
     * HREF属性値は無視される
     *
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
     *
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void sysEventContentBreak() throws HtmlParseException{
        this.eventContent.append('\n');
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Avatarリストの先頭にAvatarが、
     * intリストの先頭にエントリー番号が入る。
     *
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
     *
     * <p>役職者数開示に伴い役職リストとintリストに一件ずつ追加される。
     *
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
     *
     * <p>噛み及びハム溶けに伴いAvatarリストに1件ずつ追加される。
     *
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
     *
     * <p>生存者表示に伴いAvatarリストに1件ずつ追加される。
     *
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
     *
     * <p>G国以外での処刑に伴い、
     * 投票元と投票先の順でAvatarリストに追加される。
     *
     * <p>被処刑者がいればAvatarリストの最後に追加される。
     *
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
     *
     * <p>G国処刑に伴い、
     * 投票元と投票先の順でAvatarリストに追加される。
     *
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
     *
     * <p>Avatarリストの先頭に突然死者が入る。
     *
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
     *
     * <p>プレイヤー情報開示に伴い、
     * Avatarリストに1件、
     * 文字列リストにURLとプレイヤー名の2件、
     * intリストに生死(1or0)が1件、
     * Roleリストに役職が1件追加される。
     *
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
        if(isLiving) liveOrDead = 1;
        else         liveOrDead = 0;

        this.avatarList.add(who);
        this.charseqList.add(anchor);
        this.charseqList.add(account);
        this.integerList.add(liveOrDead);
        this.roleList.add(role);

        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>G国処刑に伴い、Avatarリストに投票先が1件、
     * intリストに得票数が1件追加される。
     * 最後に被処刑者がAvatarリストに1件、負の値がintリストに1件追加される。
     *
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
     *
     * <p>エントリー促しに伴い、
     * intリストに分数、最小メンバ数、最大メンバ数の3件が設定される。
     *
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
     *
     * <p>エントリー完了に伴い、分数をintリストに設定する。
     *
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
     *
     * <p>未発言者一覧に伴い、
     * 未発言者はAvatarリストへ1件ずつ追加される。
     *
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
     *
     * <p>決着発表に伴い、
     * Roleリストに勝者が1件、intリスト分数が1件設定される。
     *
     * <p>村勝利の場合は素村役職が用いられる。
     *
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
     *
     * <p>護衛に伴い、Avatarリストに護衛元1件と護衛先1件が設定される。
     *
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
     *
     * <p>占いに伴い、
     * 占い元が1件、占い先が1件Avatarリストに設定される。
     *
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
     *
     * <p>パースの完了した1件のイベントインスタンスを
     * Periodに追加する。
     *
     * <p>襲撃もしくは襲撃なしのイベントの前に、
     * 「今日がお前の命日だ！」で終わる赤ログが出現した場合、
     * 赤カウントに含めない。
     *
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

        boolean isMurderResult =
                   this.sysEventType == SysEventType.MURDERED
                || this.sysEventType == SysEventType.NOMURDER;

        if(isMurderResult){
            for(Topic topic : this.period.getTopicList()){
                if( ! (topic instanceof Talk) ) continue;
                Talk talk = (Talk) topic;
                if(talk.isMurderNotice()){
                    talk.setCount(-1);
                    this.countMap.clear();
                    break;
                }
            }
        }

        resetEventContext();

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @throws HtmlParseException {@inheritDoc}
     */
    @Override
    public void endParse() throws HtmlParseException{
        reset();
        return;
    }

}
