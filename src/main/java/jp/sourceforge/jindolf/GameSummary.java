/*
 * Summarize game information
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: GameSummary.java 1028 2010-05-13 10:15:11Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jp.sourceforge.jindolf.corelib.Destiny;
import jp.sourceforge.jindolf.corelib.GameRole;
import jp.sourceforge.jindolf.corelib.SysEventType;
import jp.sourceforge.jindolf.corelib.Team;
import jp.sourceforge.jindolf.corelib.VillageState;

/**
 * 決着の付いたゲームのサマリを集計。
 */
public class GameSummary{

    /** キャスティング表示用Comparator。 */
    public static final Comparator<Player> COMPARATOR_CASTING =
            new CastingComparator();

    /**
     * プレイヤーのリストから役職バランス文字列を得る。
     * ex) "村村占霊狂狼"
     * @param players プレイヤーのリスト
     * @return 役職バランス文字列
     */
    public static String getRoleBalanceSequence(List<Player> players){
        List<GameRole> roleList = new LinkedList<GameRole>();
        for(Player player : players){
            GameRole role = player.getRole();
            roleList.add(role);
        }
        Collections.sort(roleList, GameRole.getPowerBalanceComparator());

        StringBuilder result = new StringBuilder();
        for(GameRole role : roleList){
            char ch = role.getShortName();
            result.append(ch);
        }

        return result.toString();
    }

    private final Map<Avatar, Player> playerMap =
            new HashMap<Avatar, Player>();
    private final List<Player> playerList =
            new LinkedList<Player>();
    private final Map<SysEventType, List<SysEvent>> eventMap =
            new EnumMap<SysEventType, List<SysEvent>>(SysEventType.class);

    private final Village village;

    // 勝者
    private Team winner;

    // 占い先集計
    private int ctScryVillage = 0;
    private int ctScryHamster = 0;
    private int ctScryMadman  = 0;
    private int ctScryWolf    = 0;

    // 護衛先集計
    private int ctGuardVillage   = 0;
    private int ctGuardHamster   = 0;
    private int ctGuardMadman    = 0;
    private int ctGuardWolf      = 0;
    private int ctGuardVillageGJ = 0;
    private int ctGuardHamsterGJ = 0;
    private int ctGuardMadmanGJ  = 0;
    private int ctGuardFakeGJ    = 0;

    // 発言時刻範囲
    private long talk1stTimeMs = -1;
    private long talkLastTimeMs = -1;

    /**
     * コンストラクタ。
     * @param village 村
     */
    public GameSummary(Village village){
        super();

        VillageState state = village.getState();
        if(   state != VillageState.EPILOGUE
           && state != VillageState.GAMEOVER){
            throw new IllegalStateException();
        }

        this.village = village;

        summarize();

        return;
    }

    /**
     * サマライズ処理。
     */
    private void summarize(){
        buildEventMap();

        summarizeTime();
        summarizeWinner();
        summarizePlayers();

        for(Period period : this.village.getPeriodList()){
            summarizePeriod(period);
        }

        summarizeJudge();
        summarizeGuard();

        return;
    }

    /**
     * SysEventの種別ごとに集計する。
     */
    private void buildEventMap(){
        for(SysEventType type : SysEventType.values()){
            List<SysEvent> eventList = new LinkedList<SysEvent>();
            this.eventMap.put(type, eventList);
        }

        for(Period period : this.village.getPeriodList()){
            for(Topic topic : period.getTopicList()){
                if( ! (topic instanceof SysEvent) ) continue;
                SysEvent event = (SysEvent) topic;
                SysEventType type = event.getSysEventType();
                List<SysEvent> eventList = this.eventMap.get(type);
                eventList.add(event);
            }
        }

        return;
    }

    /**
     * 勝者集計。
     */
    private void summarizeWinner(){
        List<SysEvent> eventList;

        eventList = this.eventMap.get(SysEventType.WINVILLAGE);
        if( ! eventList.isEmpty() ){
            this.winner = Team.VILLAGE;
        }

        eventList = this.eventMap.get(SysEventType.WINWOLF);
        if(  ! eventList.isEmpty() ){
            this.winner = Team.WOLF;
        }

        eventList = this.eventMap.get(SysEventType.WINHAMSTER);
        if(  ! eventList.isEmpty() ){
            this.winner = Team.HAMSTER;
        }

        if(this.winner == null) assert false;

        return;
    }

    /**
     * 参加者集計。
     */
    private void summarizePlayers(){
        List<SysEvent> eventList;

        List<Avatar>       avatarList;
        List<GameRole>     roleList;
        List<Integer>      integerList;
        List<CharSequence> textList;

        eventList = this.eventMap.get(SysEventType.ONSTAGE);
        for(SysEvent event : eventList){
            avatarList  = event.getAvatarList();
            integerList = event.getIntegerList();
            Avatar onstageAvatar = avatarList.get(0);
            Player onstagePlayer = registPlayer(onstageAvatar);
            onstagePlayer.setEntryNo(integerList.get(0));
        }

        eventList = this.eventMap.get(SysEventType.PLAYERLIST);
        assert eventList.size() == 1;
        SysEvent event = eventList.get(0);

        avatarList  = event.getAvatarList();
        roleList    = event.getRoleList();
        integerList = event.getIntegerList();
        textList    = event.getCharSequenceList();
        int avatarNum = avatarList.size();
        for(int idx = 0; idx < avatarNum; idx++){
            Avatar avatar = avatarList.get(idx);
            GameRole role = roleList.get(idx);
            CharSequence urlText = textList.get(idx * 2);
            CharSequence idName  = textList.get(idx * 2 + 1);
            int liveOrDead = integerList.get(idx);

            Player player = registPlayer(avatar);
            player.setRole(role);
            player.setUrlText(urlText.toString());
            player.setIdName(idName.toString());
            if(liveOrDead != 0){        // 生存
                player.setObitDay(-1);
                player.setDestiny(Destiny.ALIVE);
            }

            this.playerList.add(player);
        }

        return;
    }

    /**
     * Periodのサマライズ。
     * @param period Period
     */
    private void summarizePeriod(Period period){
        int day = period.getDay();
        for(Topic topic : period.getTopicList()){
            if(topic instanceof SysEvent){
                SysEvent sysEvent = (SysEvent) topic;
                summarizeDestiny(day, sysEvent);
            }
        }

        return;
    }

    /**
     * 各プレイヤー運命のサマライズ。
     * @param day 日
     * @param sysEvent システムイベント
     */
    private void summarizeDestiny(int day, SysEvent sysEvent){
        List<Avatar>  avatarList  = sysEvent.getAvatarList();
        List<Integer> integerList = sysEvent.getIntegerList();

        int avatarTotal = avatarList.size();
        Avatar lastAvatar = null;
        if(avatarTotal > 0) lastAvatar = avatarList.get(avatarTotal - 1);

        SysEventType eventType = sysEvent.getSysEventType();
        switch(eventType){
        case EXECUTION:  // G国のみ
            if(integerList.get(avatarTotal - 1) > 0) break;  // 処刑無し
            Player executedPl = registPlayer(lastAvatar);
            executedPl.setDestiny(Destiny.EXECUTED);
            executedPl.setObitDay(day);
            break;
        case SUDDENDEATH:
            Avatar suddenDeathAvatar = avatarList.get(0);
            Player suddenDeathPlayer = registPlayer(suddenDeathAvatar);
            suddenDeathPlayer.setDestiny(Destiny.SUDDENDEATH);
            suddenDeathPlayer.setObitDay(day);
            break;
        case COUNTING:  // G国COUNTING2は運命に関係なし
            if(avatarTotal % 2 == 0) break;  // 処刑無し
            Player executedPlayer = registPlayer(lastAvatar);
            executedPlayer.setDestiny(Destiny.EXECUTED);
            executedPlayer.setObitDay(day);
            break;
        case MURDERED:
            for(Avatar avatar : avatarList){
                Player player = registPlayer(avatar);
                player.setDestiny(Destiny.EATEN);
                player.setObitDay(day);
            }
            // TODO E国ハム溶け処理は後回し
            break;
        default:
            break;
        }

        return;
    }

    /**
     * 会話時刻のサマライズ。
     */
    private void summarizeTime(){
        for(Period period : this.village.getPeriodList()){
            for(Topic topic : period.getTopicList()){
                if( ! (topic instanceof Talk) ) continue;
                Talk talk = (Talk) topic;

                long epoch = talk.getTimeFromID();

                if(this.talk1stTimeMs  < 0) this.talk1stTimeMs  = epoch;
                if(this.talkLastTimeMs < 0) this.talkLastTimeMs = epoch;

                if(epoch < this.talk1stTimeMs ) this.talk1stTimeMs  = epoch;
                if(epoch > this.talkLastTimeMs) this.talkLastTimeMs = epoch;
            }
        }

        return;
    }

    /**
     * 占い師の活動を集計する。
     */
    private void summarizeJudge(){
        List<SysEvent> eventList = this.eventMap.get(SysEventType.JUDGE);

        for(SysEvent event : eventList){
            List<Avatar> avatarList  = event.getAvatarList();
            Avatar avatar = avatarList.get(1);
            Player seered = getPlayer(avatar);
            GameRole role = seered.getRole();
            switch(role){
            case WOLF:    this.ctScryWolf++;    break;
            case MADMAN:  this.ctScryMadman++;  break;
            case HAMSTER: this.ctScryHamster++; break;
            default:      this.ctScryVillage++; break;
            }
        }

        return;
    }

    /**
     * 占い師の活動を文字列化する。
     * @return 占い師の活動
     */
    public CharSequence dumpSeerActivity(){
        StringBuilder result = new StringBuilder();

        if(this.ctScryVillage > 0){
            result.append("村陣営を");
            result.append(this.ctScryVillage);
            result.append("回");
        }

        if(this.ctScryHamster > 0){
            if(result.length() > 0) result.append('、');
            result.append("ハムスターを");
            result.append(this.ctScryHamster);
            result.append("回");
        }

        if(this.ctScryMadman > 0){
            if(result.length() > 0) result.append('、');
            result.append("狂人を");
            result.append(this.ctScryMadman);
            result.append("回");
        }

        if(this.ctScryWolf > 0){
            if(result.length() > 0) result.append('、');
            result.append("人狼を");
            result.append(this.ctScryWolf);
            result.append("回");
        }

        if(result.length() <= 0) result.append("誰も占わなかった。");
        else                     result.append("占った。");

        CharSequence seq = WolfBBS.escapeWikiSyntax(result);

        return seq;
    }

    /**
     * 狩人の活動を集計する。
     */
    private void summarizeGuard(){
        List<SysEvent> eventList;

        eventList = this.eventMap.get(SysEventType.GUARD);
        for(SysEvent event : eventList){
            List<Avatar> avatarList = event.getAvatarList();
            Avatar avatar = avatarList.get(1);
            Player guarded = getPlayer(avatar);
            GameRole guardedRole = guarded.getRole();
            switch(guardedRole){
            case WOLF:    this.ctGuardWolf++;    break;
            case MADMAN:  this.ctGuardMadman++;  break;
            case HAMSTER: this.ctGuardHamster++; break;
            default:      this.ctGuardVillage++; break;
            }
        }

        for(Period period : this.village.getPeriodList()){
            summarizeGjPeriod(period);
        }

        return;
    }

    /**
     * 狩人GJの日ごとの集計。
     * @param period 日
     */
    private void summarizeGjPeriod(Period period){
        if(period.getDay() <= 2) return;

        boolean hasAssaultTried = period.hasAssaultTried();
        boolean hunterAlive = false;
        int wolfNum = 0;

        Set<Avatar> voters = period.getVoterSet();
        for(Avatar avatar : voters){
            Player player = getPlayer(avatar);
            switch(player.getRole()){
            case HUNTER: hunterAlive = true; break;
            case WOLF:   wolfNum++;          break;
            default:                         break;
            }
        }

        Avatar executed = period.getExecutedAvatar();
        if(executed != null){
            Player player = getPlayer(executed);
            switch(player.getRole()){
            case HUNTER: hunterAlive = false; break;
            case WOLF:   wolfNum--;           break;
            default:                          break;
            }
        }

        if( ! hunterAlive || wolfNum <= 0) return;

        SysEvent sysEvent;

        sysEvent = period.getTypedSysEvent(SysEventType.NOMURDER);
        if(sysEvent == null) return;

        sysEvent = period.getTypedSysEvent(SysEventType.GUARD);
        if(sysEvent == null) return;

        if(hasAssaultTried){
            Avatar guarded = sysEvent.getAvatarList().get(1);
            Player guardedPlayer = getPlayer(guarded);
            GameRole guardedRole = guardedPlayer.getRole();
            switch(guardedRole){
            case MADMAN:  this.ctGuardMadmanGJ++;   break;
            case HAMSTER: this.ctGuardHamsterGJ++;  break;
            default:      this.ctGuardVillageGJ++;  break;
            }
        }else{
            this.ctGuardFakeGJ++;   // 偽装GJ
        }

        return;
    }

    /**
     * 狩人の活動を文字列化する。
     * @return 狩人の活動
     */
    public CharSequence dumpHunterActivity(){
        StringBuilder result = new StringBuilder();

        String atLeast;
        if(this.ctGuardFakeGJ > 0) atLeast = "少なくとも";
        else                       atLeast = "";

        if(this.ctGuardVillage > 0){
            result.append(atLeast);
            result.append("村陣営を");
            result.append(this.ctGuardVillage);
            result.append("回護衛し");
            if(this.ctGuardVillageGJ > 0){
                result.append("GJを");
                result.append(this.ctGuardVillageGJ);
                result.append("回出した。");
            }else{
                result.append("た。");
            }
        }

        if(this.ctGuardHamster > 0){
            result.append(atLeast);
            result.append("ハムスターを");
            result.append(this.ctGuardHamster);
            result.append("回護衛し");
            if(this.ctGuardHamsterGJ > 0){
                result.append("GJを");
                result.append(this.ctGuardHamsterGJ);
                result.append("回出した。");
            }else{
                result.append("た。");
            }
        }

        if(this.ctGuardMadman > 0){
            result.append(atLeast);
            result.append("狂人を");
            result.append(this.ctGuardMadman);
            result.append("回護衛し");
            if(this.ctGuardMadmanGJ > 0){
                result.append("GJを");
                result.append(this.ctGuardMadmanGJ);
                result.append("回出した。");
            }else{
                result.append("た。");
            }
        }

        if(this.ctGuardWolf > 0){
            result.append(atLeast);
            result.append("人狼を");
            result.append(this.ctGuardWolf);
            result.append("回護衛した。");
        }

        if(this.ctGuardFakeGJ > 0){
            result.append("護衛先は不明ながら偽装GJが");
            result.append(this.ctGuardFakeGJ);
            result.append("回あった。");
        }

        if(result.length() <= 0) result.append("誰も護衛できなかった");

        CharSequence seq = WolfBBS.escapeWikiSyntax(result);

        return seq;
    }

    /**
     * 処刑概観を文字列化する。
     * @return 文字列化した処刑概観
     */
    public CharSequence dumpExecutionInfo(){
        StringBuilder result = new StringBuilder();

        int exeWolf = 0;
        int exeMad = 0;
        int exeVillage = 0;
        for(Player player : this.playerList){
            Destiny destiny = player.getDestiny();
            if(destiny != Destiny.EXECUTED) continue;
            GameRole role = player.getRole();
            switch(role){
            case WOLF:   exeWolf++;    break;
            case MADMAN: exeMad++;     break;
            default:     exeVillage++; break;
            }
        }

        if(exeVillage > 0){
            result.append("▼村陣営×").append(exeVillage).append("回");
        }
        if(exeMad > 0){
            if(result.length() > 0) result.append("、");
            result.append("▼狂×").append(exeMad).append("回");
        }
        if(exeWolf > 0){
            if(result.length() > 0) result.append("、");
            result.append("▼狼×").append(exeWolf).append("回");
        }
        if(result.length() <= 0) result.append("なし");

        CharSequence seq = WolfBBS.escapeWikiSyntax(result);

        return seq;
    }

    /**
     * 襲撃概観を文字列化する。
     * @return 文字列化した襲撃概観
     */
    public CharSequence dumpAssaultInfo(){
        StringBuilder result = new StringBuilder();

        int eatMad = 0;
        int eatVillage = 0;
        for(Player player : this.playerList){
            if(player.getAvatar() == Avatar.AVATAR_GERD){
                result.append("▲ゲルト");
                continue;
            }
            Destiny destiny = player.getDestiny();
            if(destiny != Destiny.EATEN) continue;
            GameRole role = player.getRole();
            switch(role){
            case MADMAN: eatMad++;     break;
            default:     eatVillage++; break;
            }
        }

        if(eatVillage > 0){
            if(result.length() > 0) result.append("、");
            result.append("▲村陣営×").append(eatVillage).append("回");
        }
        if(eatMad > 0){
            if(result.length() > 0) result.append("、");
            result.append("▲狂×").append(eatMad).append("回");
        }

        if(result.length() <= 0) result.append("襲撃なし");

        CharSequence seq = WolfBBS.escapeWikiSyntax(result);

        return seq;
    }

    /**
     * まとめサイト用投票Boxを生成する。
     * @return 投票BoxのWikiテキスト
     */
    public CharSequence dumpVoteBox(){
        StringBuilder wikiText = new StringBuilder();

        for(Player player : getCastingPlayerList()){
            Avatar avatar = player.getAvatar();
            if(avatar == Avatar.AVATAR_GERD) continue;
            GameRole role = player.getRole();
            CharSequence fullName = avatar.getFullName();
            CharSequence roleName = role.getRoleName();
            StringBuilder line = new StringBuilder();
            line.append("[").append(roleName).append("] ").append(fullName);
            if(wikiText.length() > 0) wikiText.append(',');
            wikiText.append(WolfBBS.escapeWikiSyntax(line));
            wikiText.append("[0]");
        }

        wikiText.insert(0, "#vote(").append(")\n");

        return wikiText;
    }

    /**
     * まとめサイト用キャスト表を生成する。
     * @param iconSet 顔アイコンセット
     * @return キャスト表のWikiテキスト
     */
    public CharSequence dumpCastingBoard(FaceIconSet iconSet){
        StringBuilder wikiText = new StringBuilder();

        String vName = this.village.getVillageFullName();
        String generator = Jindolf.TITLE + " Ver." + Jindolf.VERSION;
        String author = iconSet.getAuthor() + "氏"
                       +" [ "+iconSet.getUrlText()+" ]";

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("// ↓キャスト表開始\n");
        wikiText.append("//        Village : " + vName + "\n");
        wikiText.append("//        Generator : " + generator + "\n");
        wikiText.append("//        アイコン作者 : " + author + '\n');
        wikiText.append("// ※アイコン画像の著作財産権保持者"
                       +"および画像サーバ運営者から\n");
        wikiText.append("// 新しい意向が示された場合、"
                       +"そちらを最優先で尊重してください。\n");
        wikiText.append(WolfBBS.COMMENTLINE);

        wikiText.append("|配役")
                .append("|参加者")
                .append("|役職")
                .append("|運命")
                .append("|その活躍")
                .append("|h")
                .append('\n');
        wikiText.append(WolfBBS.COMMENTLINE);

        for(Player player : getCastingPlayerList()){
            Avatar avatar   = player.getAvatar();
            GameRole role   = player.getRole();
            Destiny destiny = player.getDestiny();
            int obitDay     = player.getObitDay();
            String name     = player.getIdName();
            String urlText  = player.getUrlText();
            if(urlText == null) urlText = "";
            urlText = urlText.replace("~", "%7e");
            urlText = urlText.replace(" ", "%20");
            try{
                URL url = new URL(urlText);
                URI uri = url.toURI();
                urlText = uri.toASCIIString();
            }catch(MalformedURLException e){
                // NOTHING
            }catch(URISyntaxException e){
                // NOTHING
            }
            // PukiWikiではURL内の&のエスケープは不要?

            wikiText.append("// ========== ");
            wikiText.append(name + " acts as [" + avatar.getName() + "]");
            wikiText.append(" ==========\n");

            String teamColor =  "BGCOLOR("
                              + WolfBBS.getTeamWikiColor(role)
                              + "):";

            String avatarIcon = iconSet.getAvatarIconWiki(avatar);

            wikiText.append('|').append(teamColor);
            wikiText.append(avatarIcon).append("&br;");

            wikiText.append("[[").append(avatar.getName()).append("]]");

            wikiText.append('|').append(teamColor);
            wikiText.append("[[").append(WolfBBS.escapeWikiBracket(name));
            if(urlText != null && urlText.length() > 0){
                wikiText.append('>').append(urlText);
            }
            wikiText.append("]]");

            wikiText.append('|').append(teamColor);
            wikiText.append(WolfBBS.getRoleIconWiki(role));
            wikiText.append("&br;");
            wikiText.append("[[");
            wikiText.append(role.getRoleName());
            wikiText.append("]]");

            String destinyColor = WolfBBS.getDestinyColorWiki(destiny);
            wikiText.append('|');
            wikiText.append("BGCOLOR(").append(destinyColor).append("):");
            if(destiny == Destiny.ALIVE){
                wikiText.append("最後まで&br;生存");
            }else{
                wikiText.append(obitDay).append("日目").append("&br;");
                wikiText.append(destiny.getMessage());
            }

            wikiText.append('|');
            wikiText.append(avatar.getJobTitle()).append('。');

            if(avatar == Avatar.AVATAR_GERD){
                wikiText.append("寝てばかりいた。");
            }else if(role == GameRole.HUNTER){
                CharSequence report = dumpHunterActivity();
                wikiText.append(report);
            }else if(role == GameRole.SEER){
                CharSequence report = dumpSeerActivity();
                wikiText.append(report);
            }

            wikiText.append("|\n");

        }

        wikiText.append("|>|>|>|>|");
        wikiText.append("RIGHT:");
        wikiText.append("顔アイコン提供 : [[");
        wikiText.append(WolfBBS.escapeWikiBracket(iconSet.getAuthor()));
        wikiText.append(">" + iconSet.getUrlText());
        wikiText.append("]]氏");
        wikiText.append("|\n");

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("// ↑キャスト表ここまで\n");
        wikiText.append(WolfBBS.COMMENTLINE);

        return wikiText;
    }

    /**
     * 村詳細情報を出力する。
     * @return 村詳細情報
     */
    public CharSequence dumpVillageWiki(){
        StringBuilder wikiText = new StringBuilder();

        DateFormat dform =
                DateFormat.getDateTimeInstance(DateFormat.FULL,
                                               DateFormat.FULL);

        String vName = this.village.getVillageFullName();
        String generator = Jindolf.TITLE + " Ver." + Jindolf.VERSION;

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("// ↓村詳細開始\n");
        wikiText.append("//        Village : " + vName + "\n");
        wikiText.append("//        Generator : " + generator + "\n");

        wikiText.append("* 村の詳細\n");

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("- 勝者\n");
        Team winnerTeam = getWinnerTeam();
        String wonTeam = winnerTeam.getTeamName();
        wikiText.append(wonTeam).append('\n');

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("- エントリー開始時刻\n");
        Date date = get1stTalkDate();
        String talk1st = dform.format(date);
        wikiText.append(talk1st).append('\n');

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("- 参加人数\n");
        int avatarNum = countAvatarNum();
        String totalMember = "ゲルト + " + (avatarNum - 1) + "名 = "
                            + avatarNum + "名";
        wikiText.append(WolfBBS.escapeWikiSyntax(totalMember))
                .append('\n');

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("- 役職内訳\n");
        StringBuilder roleMsg = new StringBuilder();
        for(GameRole role : GameRole.values()){
            List<Player> players = getRoledPlayerList(role);
            String roleName = role.getRoleName();
            if(players.size() <= 0) continue;
            if(roleMsg.length() > 0) roleMsg.append('、');
            roleMsg.append(roleName)
                   .append(" × ")
                   .append(players.size())
                   .append("名");
        }
        wikiText.append(WolfBBS.escapeWikiSyntax(roleMsg)).append('\n');

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("- 処刑内訳\n");
        wikiText.append(dumpExecutionInfo()).append('\n');

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("- 襲撃内訳\n");
        wikiText.append(dumpAssaultInfo()).append('\n');

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("- 突然死\n");
        wikiText.append(countSuddenDeath()).append("名").append('\n');

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("- 人口推移\n");
        for(int day = 1; day < this.village.getPeriodSize(); day++){
            List<Player> players = getSurvivorList(day);
            CharSequence roleSeq =
                    GameSummary.getRoleBalanceSequence(players);
            String daySeq;
            Period period = this.village.getPeriod(day);
            daySeq = period.getCaption();
            wikiText.append('|')
                    .append(daySeq)
                    .append('|')
                    .append(roleSeq)
                    .append("|\n");
        }

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("- 占い師の成績\n");
        wikiText.append(dumpSeerActivity()).append('\n');

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("- 狩人の成績\n");
        wikiText.append(dumpHunterActivity()).append('\n');

        wikiText.append(WolfBBS.COMMENTLINE);
        wikiText.append("// ↑村詳細ここまで\n");
        wikiText.append(WolfBBS.COMMENTLINE);

        return wikiText;
    }

    /**
     * 最初の発言の時刻を得る。
     * @return 時刻
     */
    public Date get1stTalkDate(){
        return new Date(this.talk1stTimeMs);
    }

    /**
     * 最後の発言の時刻を得る。
     * @return 時刻
     */
    public Date getLastTalkDate(){
        return new Date(this.talkLastTimeMs);
    }

    /**
     * 指定した日の生存者一覧を得る。
     * @param day 日
     * @return 生存者一覧
     */
    public List<Player> getSurvivorList(int day){
        if(day < 0 || this.village.getPeriodSize() <= day){
            throw new IndexOutOfBoundsException();
        }

        List<Player> result = new LinkedList<Player>();

        Period period = this.village.getPeriod(day);

        if(   period.isPrologue()
           || (period.isProgress() && day == 1) ){
            result.addAll(this.playerList);
            return result;
        }

        if(period.isEpilogue()){
            for(Player player : this.playerList){
                if(player.getDestiny() == Destiny.ALIVE){
                    result.add(player);
                }
            }
            return result;
        }

        for(Topic topic : period.getTopicList()){
            if( ! (topic instanceof SysEvent) ) continue;
            SysEvent sysEvent = (SysEvent) topic;
            if(sysEvent.getSysEventType() == SysEventType.SURVIVOR){
                List<Avatar> avatarList = sysEvent.getAvatarList();
                for(Avatar avatar : avatarList){
                    Player player = getPlayer(avatar);
                    result.add(player);
                }
            }
        }

        return result;
    }

    /**
     * プレイヤー一覧を得る。
     * 参加エントリー順
     * @return プレイヤーのリスト
     */
    public List<Player> getPlayerList(){
        List<Player> result = Collections.unmodifiableList(this.playerList);
        return result;
    }

    /**
     * キャスティング表用にソートされたプレイヤー一覧を得る。
     * @return プレイヤーのリスト
     */
    public List<Player> getCastingPlayerList(){
        List<Player> sortedPlayers =
                new LinkedList<Player>();
        sortedPlayers.addAll(this.playerList);
        Collections.sort(sortedPlayers, COMPARATOR_CASTING);
        return sortedPlayers;
    }

    /**
     * 指定された役職のプレイヤー一覧を得る。
     * @param role 役職
     * @return 役職に合致するプレイヤーのリスト
     */
    public List<Player> getRoledPlayerList(GameRole role){
        List<Player> result = new LinkedList<Player>();

        for(Player player : this.playerList){
            if(player.getRole() == role){
                result.add(player);
            }
        }

        return result;
    }

    /**
     * 勝利陣営を得る。
     * @return 勝利した陣営
     */
    public Team getWinnerTeam(){
        return this.winner;
    }

    /**
     * 突然死者数を得る。
     * @return 突然死者数
     */
    public int countSuddenDeath(){
        int suddenDeath = 0;
        for(Player player : this.playerList){
            if(player.getDestiny() == Destiny.SUDDENDEATH) suddenDeath++;
        }
        return suddenDeath;
    }

    /**
     * 参加プレイヤー総数を得る。
     * @return プレイヤー総数
     */
    public int countAvatarNum(){
        int playerNum = this.playerList.size();
        return playerNum;
    }

    /**
     * AvatarからPlayerを得る。
     * 参加していないAvatarならnullを返す。
     * @param avatar Avatar
     * @return Player
     */
    public final Player getPlayer(Avatar avatar){
        Player player = this.playerMap.get(avatar);
        return player;
    }

    /**
     * AvatarからPlayerを得る。
     * 無ければ新規に作る。
     * @param avatar Avatar
     * @return Player
     */
    private Player registPlayer(Avatar avatar){
        Player player = getPlayer(avatar);
        if(player == null){
            player = new Player();
            player.setAvatar(avatar);
            this.playerMap.put(avatar, player);
        }
        return player;
    }

    /**
     * プレイヤーのソート仕様の記述。
     * まとめサイトのキャスト表向け。
     */
    private static final class CastingComparator
            implements Comparator<Player> {

        /**
         * コンストラクタ。
         */
        private CastingComparator(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * @param p1 {@inheritDoc}
         * @param p2 {@inheritDoc}
         * @return {@inheritDoc}
         */
        public int compare(Player p1, Player p2){
            if(p1 == p2) return 0;
            if(p1 == null) return -1;
            if(p2 == null) return +1;

            // ゲルトが最前
            Avatar avatar1 = p1.getAvatar();
            Avatar avatar2 = p2.getAvatar();
            if(avatar1.equals(avatar2)) return 0;
            if(avatar1 == Avatar.AVATAR_GERD) return -1;
            if(avatar2 == Avatar.AVATAR_GERD) return +1;

            // 生存者は最後
            Destiny dest1 = p1.getDestiny();
            Destiny dest2 = p2.getDestiny();
            if(dest1 != dest2){
                if     (dest1 == Destiny.ALIVE) return +1;
                else if(dest2 == Destiny.ALIVE) return -1;
            }

            // 退場順
            int obitDay1 = p1.getObitDay();
            int obitDay2 = p2.getObitDay();
            if(obitDay1 > obitDay2) return +1;
            if(obitDay1 < obitDay2) return -1;

            // 運命順
            int destinyOrder = dest1.compareTo(dest2);
            if(destinyOrder != 0) return destinyOrder;

            // エントリー順
            int entryOrder = p1.getEntryNo() - p2.getEntryNo();

            return entryOrder;
        }
    }

    // TODO E国ハムスター対応
}