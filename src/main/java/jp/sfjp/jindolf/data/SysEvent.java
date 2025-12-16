/*
 * system event in game
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sourceforge.jindolf.corelib.EventFamily;
import jp.sourceforge.jindolf.corelib.GameRole;
import jp.sourceforge.jindolf.corelib.SysEventType;

/**
 * 人狼BBSシステムが生成する各種メッセージ。
 * Topicの具体化。
 */
public class SysEvent implements Topic{
    // TODO 狼の襲撃先表示は Talk か SysEvent どちらにしよう...

    private EventFamily eventFamily;
    private SysEventType sysEventType;
    private DecodedContent content;

    private final List<Avatar>   avatarList  = new LinkedList<>();
    private final List<GameRole> roleList    = new LinkedList<>();
    private final List<Integer>  integerList = new LinkedList<>();
    private final List<CharSequence>  charseqList =
            new LinkedList<>();
    /** for playerList and onStage. */
    private final List<Player> playerList = new LinkedList<>();
    /** for execution. */
    private final List<Nominated> nominatedList = new LinkedList<>();
    /** for vote, judge, counting, etc. */
    private final List<InterPlay> interPlayList = new LinkedList<>();


    /**
     * コンストラクタ。
     */
    public SysEvent(){
        super();
        return;
    }

    /**
     * イベントファミリを取得する。
     *
     * @return イベントファミリ
     */
    public EventFamily getEventFamily(){
        return this.eventFamily;
    }

    /**
     * イベントファミリを設定する。
     *
     * @param eventFamily イベントファミリ
     * @throws NullPointerException 引数がnull
     */
    public void setEventFamily(EventFamily eventFamily)
            throws NullPointerException{
        this.eventFamily = eventFamily;
        return;
    }

    /**
     * イベント種別を取得する。
     *
     * @return イベント種別
     */
    public SysEventType getSysEventType(){
        return this.sysEventType;
    }

    /**
     * イベント種別を設定する。
     *
     * @param type イベント種別
     * @throws NullPointerException 引数がnull
     */
    public void setSysEventType(SysEventType type)
            throws NullPointerException{
        if(type == null) throw new NullPointerException();
        this.sysEventType = type;
        return;
    }

    /**
     * イベントメッセージを取得する。
     *
     * @return イベントメッセージ
     */
    public DecodedContent getContent(){
        return this.content;
    }

    /**
     * イベントメッセージを設定する。
     *
     * @param content イベントメッセージ
     * @throws NullPointerException 引数がnull
     */
    public void setContent(DecodedContent content)
            throws NullPointerException{
        if(content == null) throw new NullPointerException();
        this.content = content;
        return;
    }

    /**
     * Avatarリストを取得する。
     *
     * @return Avatarリスト
     */
    public List<Avatar> getAvatarList(){
        List<Avatar> result = Collections.unmodifiableList(this.avatarList);
        return result;
    }

    /**
     * Roleリストを取得する。
     *
     * @return Roleリスト
     */
    public List<GameRole> getRoleList(){
        List<GameRole> result = Collections.unmodifiableList(this.roleList);
        return result;
    }

    /**
     * Integerリストを取得する。
     *
     * @return Integerリスト
     */
    public List<Integer> getIntegerList(){
        List<Integer> result = Collections.unmodifiableList(this.integerList);
        return result;
    }

    /**
     * CharSequenceリストを取得する。
     *
     * @return CharSequenceリスト
     */
    public List<CharSequence> getCharSequenceList(){
        List<CharSequence> result =
                Collections.unmodifiableList(this.charseqList);
        return result;
    }

    /**
     * Playerリストを返す。
     *
     * @return Playerリスト
     */
    public List<Player> getPlayerList(){
        List<Player> result =
                Collections.unmodifiableList(this.playerList);
        return result;
    }

    /**
     * Nominatedリストを返す。
     *
     * @return Nominatedリスト
     */
    public List<Nominated> getNominatedList(){
        List<Nominated> result =
                Collections.unmodifiableList(this.nominatedList);
        return result;
    }

    /**
     * InterPlayリストを返す。
     *
     * @return InterPlayリスト
     */
    public List<InterPlay> getInterPlayList(){
        List<InterPlay> result =
                Collections.unmodifiableList(this.interPlayList);
        return result;
    }

    /**
     * Avatar一覧を追加する。
     *
     * @param list Avatar一覧
     */
    public void addAvatarList(List<Avatar> list){
        this.avatarList.addAll(list);
        return;
    }

    /**
     * 役職一覧を追加する。
     *
     * @param list 役職一覧
     */
    public void addRoleList(List<GameRole> list){
        this.roleList.addAll(list);
        return;
    }

    /**
     * 数値一覧を追加する。
     *
     * @param list 数値一覧
     */
    public void addIntegerList(List<Integer> list){
        this.integerList.addAll(list);
        return;
    }

    /**
     * 文字列一覧を追加する。
     *
     * @param list 文字列一覧
     */
    public void addCharSequenceList(List<CharSequence> list){
        this.charseqList.addAll(list);
        return;
    }

    /**
     * Player一覧を追加する。
     *
     * @param list Player一覧
     */
    public void addPlayerList(List<Player> list){
        this.playerList.addAll(list);
        return;
    }

    /**
     * Nominated一覧を追加する。
     *
     * @param list Nominated一覧
     */
    public void addNominatedList(List<Nominated> list){
        this.nominatedList.addAll(list);
        return;
    }

    /**
     * InterPlay一覧を追加する。
     *
     * @param list InterPlay一覧
     */
    public void addInterPlayList(List<InterPlay> list){
        this.interPlayList.addAll(list);
        return;
    }

    /**
     * システムイベントを解析し、処刑されたAvatarを返す。
     * G国運用中の時点で、処刑者が出るのはCOUNTINGとEXECUTIONのみ。
     *
     * @return 処刑されたAvatar。いなければnull
     */
    public Avatar getExecutedAvatar(){
        Avatar result = null;

        switch(this.sysEventType){
        case COUNTING:
        case EXECUTION:
            if( ! this.avatarList.isEmpty()){
                result = this.avatarList.get(0);
            }
            break;
        case COUNTING2:
            // NOTHING
            break;
        default:
            break;
        }

        return result;
    }

    /**
     * 投票に参加したAvatarの集合を返す。
     * G国運用中の時点で、投票者が出るのはCOUNTINGとCOUNTING2のみ。
     *
     * @param set 結果格納先。nullなら自動的に確保される。
     * @return 投票に参加したAvatarのSet
     */
    public Set<Avatar> getVoterSet(Set<Avatar> set){
        Set<Avatar> result;
        if(set == null) result = new HashSet<>();
        else            result = set;

        if(    this.sysEventType != SysEventType.COUNTING
            && this.sysEventType != SysEventType.COUNTING2 ){
            return result;
        }

        Set<Avatar> voterSet = this.interPlayList.stream()
                .map(interPlay -> interPlay.getByWhom())
                .collect(Collectors.toSet());

        result.addAll(voterSet);

        return result;
    }

}
