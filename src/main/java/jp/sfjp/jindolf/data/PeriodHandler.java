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
import jp.sfjp.jindolf.util.StringUtils;
import jp.sourceforge.jindolf.corelib.EventFamily;
import jp.sourceforge.jindolf.corelib.GameRole;
import jp.sourceforge.jindolf.corelib.PeriodType;
import jp.sourceforge.jindolf.corelib.SysEventType;
import jp.sourceforge.jindolf.corelib.TalkType;
import jp.sourceforge.jindolf.corelib.Team;

/**
 * Periodパース用ハンドラ。
 */
public class PeriodHandler extends HtmlAdapter {

    private static final int TALKTYPE_NUM = TalkType.values().length;

    private final EntityConverter converter =
            new EntityConverter(true);
    // TODO: SMP面文字に彩色対応するまでの暫定措置

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
        this.period.setLoginName(null);
        this.period.clearTopicList();
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

        this.period.setLoginName(loginName.toString());

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
        this.period.setLimit(hour, minute);
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

        if(    periodType == PeriodType.PROGRESS
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

        if(    this.sysEventType == SysEventType.MURDERED
            || this.sysEventType == SysEventType.NOMURDER ){
            for(Topic topic : this.period.getTopicList()){
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
