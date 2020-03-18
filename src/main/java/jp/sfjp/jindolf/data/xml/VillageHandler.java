/*
 * village handler
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sfjp.jindolf.data.Avatar;
import jp.sfjp.jindolf.data.CoreData;
import jp.sfjp.jindolf.data.InterPlay;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.data.Nominated;
import jp.sfjp.jindolf.data.Period;
import jp.sfjp.jindolf.data.Player;
import jp.sfjp.jindolf.data.SysEvent;
import jp.sfjp.jindolf.data.Talk;
import jp.sfjp.jindolf.data.Topic;
import jp.sfjp.jindolf.data.Village;
import jp.sourceforge.jindolf.corelib.Destiny;
import jp.sourceforge.jindolf.corelib.EventFamily;
import jp.sourceforge.jindolf.corelib.GameRole;
import jp.sourceforge.jindolf.corelib.PeriodType;
import jp.sourceforge.jindolf.corelib.SysEventType;
import jp.sourceforge.jindolf.corelib.TalkType;
import jp.sourceforge.jindolf.corelib.VillageState;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * VillageのXMLパーサ本体。
 *
 * <p>パース中の各種コンテキストを保持する。
 */
public class VillageHandler implements ContentHandler{

    private static final String NS_JINARCHIVE =
            "http://jindolf.sourceforge.jp/xml/ns/501";


    private String nsPfx;

    private Land land;
    private Village village;
    private Period period;

    private Talk talk;
    private int talkNo;

    private SysEvent sysEvent;

    private boolean inLine;
    private final StringBuilder content = new StringBuilder(250);

    private final Map<String, Avatar> idAvatarMap = new HashMap<>();
    private final List<Avatar> avatarList = new LinkedList<>();
    private final List<Player> playerList = new LinkedList<>();
    private final List<Nominated> nominatedList = new LinkedList<>();
    private final List<InterPlay> interPlayList = new LinkedList<>();

    private Map<String, ElemTag> qNameMap = ElemTag.getQNameMap("");


    /**
     * constructor.
     */
    public VillageHandler(){
        super();
        return;
    }


    /**
     * 属性値を得る。
     *
     * @param atts 属性集合
     * @param attrName 属性名
     * @return 属性値。なければnull。
     */
    private static String attrValue(Attributes atts, String attrName){
        String result = atts.getValue("", attrName);
        return result;
    }


    /**
     * decode ElemTag.
     *
     * @param uri URI of namespace
     * @param localName local name
     * @param qName Qname
     * @return
     */
    private ElemTag decodeElemTag(String uri,
                                  String localName,
                                  String qName){
        ElemTag result = this.qNameMap.get(qName);
        return result;
    }

    /**
     * パースした結果のVillageを返す。
     *
     * @return 村。
     */
    public Village getVillage(){
        return this.village;
    }

    /**
     * パース開始前の準備。
     */
    private void resetBefore(){
        this.idAvatarMap.clear();
        this.content.setLength(0);
        this.avatarList.clear();
        this.playerList.clear();
        this.nominatedList.clear();
        this.interPlayList.clear();
        this.inLine = false;
        return;
    }

    /**
     * パース開始後の後始末。
     */
    private void resetAfter(){
        this.idAvatarMap.clear();
        this.content.setLength(0);
        this.avatarList.clear();
        this.playerList.clear();
        this.nominatedList.clear();
        this.interPlayList.clear();
        this.nsPfx = null;
        this.land = null;
        this.period = null;
        return;
    }

    /**
     * village要素開始の受信。
     *
     * @param atts 属性
     */
    private void startVillage(Attributes atts){
        String landId = attrValue(atts, "landId");
        String vid    = attrValue(atts, "vid");
        String name   = attrValue(atts, "fullName");
        String state  = attrValue(atts, "state");

        this.land = CoreData.getLandDefList().stream()
                .filter(landDef -> landDef.getLandId().equals(landId))
                .map(landDef -> new Land(landDef))
                .findFirst().get();

        this.village = new Village(this.land, vid, name);

        this.village.setState(VillageState.GAMEOVER);

        this.talkNo = 0;
        this.period = null;

        return;
    }

    /**
     * avatar要素開始の受信。
     *
     * @param atts 属性
     */
    private void startAvatar(Attributes atts){
        String avatarId    = attrValue(atts, "avatarId");
        String fullName    = attrValue(atts, "fullName");
        String shortName   = attrValue(atts, "shortName");
        String faceIconUri = attrValue(atts, "faceIconURI");

        Avatar avatar = Avatar.getPredefinedAvatar(fullName);

        this.village.addAvatar(avatar);
        this.idAvatarMap.put(avatarId, avatar);

        return;
    }

    /**
     * period要素開始の受信。
     *
     * @param atts 属性
     */
    private void startPeriod(Attributes atts) throws SAXException{
        String typeAttr = attrValue(atts, "type");
        String dayAttr  = attrValue(atts, "day");

        PeriodType periodType = XmlDecoder.decodePeriodType(typeAttr);
        int day = Integer.parseInt(dayAttr);

        this.period = new Period(this.village, periodType, day);

        // append tail
        int periodIdx = this.village.getPeriodSize();
        this.village.setPeriod(periodIdx, this.period);

        return;
    }

    /**
     * period要素終了の受信。
     */
    private void endPeriod(){
        this.period.setFullOpen(true);
        return;
    }

    /**
     * talk要素開始の受信。
     *
     * @param atts 属性
     */
    private void startTalk(Attributes atts) throws SAXException{
        String type     = attrValue(atts, "type");
        String avatarId = attrValue(atts, "avatarId");
        String xname    = attrValue(atts, "xname");
        String time     = attrValue(atts, "time");

        TalkType talkType = XmlDecoder.decodeTalkType(type);
        Avatar talkAvatar = this.idAvatarMap.get(avatarId);
        int hour   = XmlDecoder.decodeHour  (time);
        int minute = XmlDecoder.decodeMinute(time);

        this.talk = new Talk(
                this.period,
                talkType, talkAvatar,
                0, xname,
                hour, minute,
                ""
        );

        this.content.setLength(0);

        return;
    }

    /**
     * talk要素終了の受信。
     */
    private void endTalk(){
        int no = 0;
        if(this.talk.getTalkType() == TalkType.PUBLIC){
            no = ++this.talkNo;
        }

        String dialog = this.content.toString();
        this.content.setLength(0);

        this.talk.setTalkNo(no);
        this.talk.setDialog(dialog);

        this.period.addTopic(this.talk);

        return;
    }

    /**
     * li要素開始の受信。
     *
     * @param atts 属性
     */
    private void startLi(Attributes atts){
        this.inLine = true;
        if(this.content.length() > 0){
            this.content.append('\n');
        }
        return;
    }

    /**
     * li要素終了の受信。
     */
    private void endLi(){
        this.inLine = false;
        return;
    }

    /**
     * playerList要素開始を受信する。
     *
     * @param atts 属性
     */
    private void startPlayerList(Attributes atts){
        this.playerList.clear();
        return;
    }

    /**
     * playerList要素終了を受信する。
     */
    private void endPlayerList(){
        this.sysEvent.addPlayerList(this.playerList);
        this.playerList.clear();
        return;
    }

    /**
     * execution要素開始を受信する。
     *
     * @param atts 属性
     */
    private void startExecution(Attributes atts){
        String victimId = attrValue(atts, "victim");

        if(victimId != null){
            Avatar victim = this.idAvatarMap.get(victimId);
            List<Avatar> single = Collections.singletonList(victim);
            this.sysEvent.addAvatarList(single);
        }

        return;
    }

    /**
     * execution要素終了を受信する。
     */
    private void endExecution(){
        this.sysEvent.addNominatedList(this.nominatedList);
        this.nominatedList.clear();
        return;
    }

    /**
     * counting要素終了を受信する。
     */
    private void endCounting(){
        this.sysEvent.addInterPlayList(this.interPlayList);
        this.interPlayList.clear();
        return;
    }

    /**
     * counting2要素終了を受信する。
     */
    private void endCounting2(){
        endCounting();
        return;
    }

    /**
     * murdered要素終了を受信する。
     */
    private void endMurdered(){
        this.sysEvent.addAvatarList(this.avatarList);
        this.avatarList.clear();
        return;
    }

    /**
     * survivor要素終了を受信する。
     */
    private void endSurvivor(){
        this.sysEvent.addAvatarList(this.avatarList);
        this.avatarList.clear();
        return;
    }

    /**
     * suddenDeath要素開始を受信する。
     *
     * @param atts 属性
     */
    private void startSuddenDeath(Attributes atts){
        String avatarId = attrValue(atts, "avatarId");
        Avatar avatar = this.idAvatarMap.get(avatarId);
        List<Avatar> single = Collections.singletonList(avatar);
        this.sysEvent.addAvatarList(single);
        return;
    }

    /**
     * counting要素開始を受信する。
     *
     * @param atts 属性
     */
    private void startCounting(Attributes atts){
        String victimId = attrValue(atts, "victim");
        if(victimId == null) return;
        Avatar victim = this.idAvatarMap.get(victimId);
        List<Avatar> single = Collections.singletonList(victim);
        this.sysEvent.addAvatarList(single);
        return;
    }

    /**
     * judge要素開始を受信する。
     *
     * @param atts 属性
     */
    private void startJudge(Attributes atts){
        startSimpleInterPlay(atts);
        return;
    }

    /**
     * guard要素開始を受信する。
     *
     * @param atts 属性
     */
    private void startGuard(Attributes atts){
        startSimpleInterPlay(atts);
        return;
    }

    /**
     * SystemEvent内の単一InterPlayを抽出する。
     *
     * @param atts 属性
     */
    private void startSimpleInterPlay(Attributes atts){
        String byWhomId = attrValue(atts, "byWhom");
        String targetId = attrValue(atts, "target");

        Avatar byWhom = this.idAvatarMap.get(byWhomId);
        Avatar target = this.idAvatarMap.get(targetId);

        InterPlay interPlay = new InterPlay(byWhom, target);
        List<InterPlay> single = Collections.singletonList(interPlay);

        this.sysEvent.addInterPlayList(single);

        return;
    }

    /**
     * SysEvent 開始の受信。
     *
     * @param type SysEvent種別
     */
    private void startSysEvent(ElemTag tag, Attributes atts){
        this.sysEvent = new SysEvent();

        SysEventType type = tag.getSystemEventType();
        EventFamily eventFamily = type.getEventFamily();
        this.sysEvent.setSysEventType(type);
        this.sysEvent.setEventFamily(eventFamily);

        if(this.sysEvent.getSysEventType() == SysEventType.ASSAULT){
            startAssault(atts);
        }else{
            switch(tag){
                case ONSTAGE:
                    startOnStage(atts);
                    break;
                case PLAYERLIST:
                    startPlayerList(atts);
                    break;
                case EXECUTION:
                    startExecution(atts);
                    break;
                case SUDDENDEATH:
                    startSuddenDeath(atts);
                    break;
                case COUNTING:
                    startCounting(atts);
                    break;
                case JUDGE:
                    startJudge(atts);
                    break;
                case GUARD:
                    startGuard(atts);
                    break;
                default:
                    break;
            }
        }

        this.content.setLength(0);

        return;
    }

    /**
     * SysEvent 終了の受信。
     *
     * @return パースしたSysEvent。
     */
    private void endSysEvent(){
        Topic topic;
        SysEventType eventType = this.sysEvent.getSysEventType();
        if(eventType == SysEventType.ASSAULT){
            endAssault();
            topic = this.talk;
        }else{
            switch(eventType){
                case PLAYERLIST:
                    endPlayerList();
                    break;
                case EXECUTION:
                    endExecution();
                    break;
                case COUNTING:
                    endCounting();
                    break;
                case COUNTING2:
                    endCounting2();
                    break;
                case MURDERED:
                    endMurdered();
                    break;
                case SURVIVOR:
                    endSurvivor();
                    break;
                default:
                    break;
            }

            DecodedContent decoded = new DecodedContent(this.content);
            this.sysEvent.setContent(decoded);

            topic = this.sysEvent;
        }

        this.period.addTopic(topic);

        this.content.setLength(0);
        this.sysEvent = null;

        return;
    }

    /**
     * assault要素開始の受信。
     *
     * @param atts 属性
     */
    private void startAssault(Attributes atts){
        String byWhom = attrValue(atts, "byWhom");
        String xname  = attrValue(atts, "xname");
        String time   = attrValue(atts, "time");

        Avatar talkAvatar = this.idAvatarMap.get(byWhom);

        int hour   = XmlDecoder.decodeHour  (time);
        int minute = XmlDecoder.decodeMinute(time);

        this.talk = new Talk(
                this.period,
                TalkType.WOLFONLY, talkAvatar,
                0, xname,
                hour, minute,
                ""
        );

        return;
    }

    /**
     * assault要素終了の受信。
     */
    private void endAssault(){
        String dialog = this.content.toString();
        this.content.setLength(0);

        this.talk.setDialog(dialog);

        return;
    }

    /**
     * onStage要素開始の受信。
     *
     * @param atts 属性
     */
    private void startOnStage(Attributes atts){
        String entryNo  = attrValue(atts, "entryNo");
        String avatarId = attrValue(atts, "avatarId");

        Avatar avatar = this.idAvatarMap.get(avatarId);
        int entry = Integer.parseInt(entryNo);

        Player player = new Player();
        player.setAvatar(avatar);
        player.setEntryNo(entry);

        this.sysEvent.addPlayerList(Collections.singletonList(player));

        return;
    }

    /**
     * playerInfo要素開始を受信する。
     *
     * @param atts 属性
     */
    private void startPlayerInfo(Attributes atts) throws SAXException{
        String playerId = attrValue(atts, "playerId");
        String avatarId = attrValue(atts, "avatarId");
        String survive = attrValue(atts, "survive");
        String role = attrValue(atts, "role");
        String uri = attrValue(atts, "uri");

        Avatar avatar = this.idAvatarMap.get(avatarId);
        GameRole gameRole = XmlDecoder.decodeRole(role);
        if(uri == null) uri = "";

        Player player = new Player();

        player.setAvatar(avatar);
        player.setRole(gameRole);
        player.setIdName(playerId);
        player.setUrlText(uri);
        if("true".equals(survive) || "1".equals(survive)){
            player.setObitDay(-1);
            player.setDestiny(Destiny.ALIVE);
        }

        this.playerList.add(player);

        return;
    }

    /**
     * nominated要素開始の受信。
     *
     * @param atts 属性
     */
    private void startNominated(Attributes atts){
        String avatarId = attrValue(atts, "avatarId");
        String countTxt = attrValue(atts, "count");

        Avatar avatar = this.idAvatarMap.get(avatarId);
        int count = Integer.parseInt(countTxt);

        Nominated nominated = new Nominated(avatar, count);
        this.nominatedList.add(nominated);

        return;
    }

    /**
     * vote要素開始を受信する。
     *
     * @param atts 属性
     */
    private void startVote(Attributes atts){
        String byWhomId = attrValue(atts, "byWhom");
        String targetId = attrValue(atts, "target");

        Avatar byWhom = this.idAvatarMap.get(byWhomId);
        Avatar target = this.idAvatarMap.get(targetId);

        InterPlay interPlay = new InterPlay(byWhom, target);
        this.interPlayList.add(interPlay);

        return;
    }

    /**
     * avatarRef要素開始を受信する。
     *
     * @param atts 属性
     */
    private void startAvatarRef(Attributes atts){
        String avatarId = attrValue(atts, "avatarId");
        Avatar avatar = this.idAvatarMap.get(avatarId);
        this.avatarList.add(avatar);
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param locator {@inheritDoc}
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void startDocument() throws SAXException {
        resetBefore();
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        resetAfter();
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param prefix {@inheritDoc}
     * @param uri {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if(NS_JINARCHIVE.equals(uri)){
            this.nsPfx = prefix;
            this.qNameMap = ElemTag.getQNameMap(this.nsPfx);
        }
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param prefix {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void endPrefixMapping(String prefix)
            throws SAXException {
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param uri {@inheritDoc}
     * @param localName {@inheritDoc}
     * @param qName {@inheritDoc}
     * @param atts {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {
        ElemTag tag = decodeElemTag(uri, localName, qName);
        if(tag == null) return;

        if(tag.isSysEventTag()){
            startSysEvent(tag, atts);
            return;
        }

        switch(tag){
            case VILLAGE:
                startVillage(atts);
                break;
            case AVATAR:
                startAvatar(atts);
                break;
            case PERIOD:
                startPeriod(atts);
                break;
            case TALK:
                startTalk(atts);
                break;
            case LI:
                startLi(atts);
                break;
            case PLAYERINFO:
                startPlayerInfo(atts);
                break;
            case NOMINATED:
                startNominated(atts);
                break;
            case VOTE:
                startVote(atts);
            case AVATARREF:
                startAvatarRef(atts);
                break;
            default:
                break;
        }

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param uri {@inheritDoc}
     * @param localName {@inheritDoc}
     * @param qName {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        ElemTag tag = decodeElemTag(uri, localName, qName);
        if(tag == null) return;

        if(tag.isSysEventTag()){
            endSysEvent();
            return;
        }

        switch(tag){
            case PERIOD:
                endPeriod();
                break;
            case TALK:
                endTalk();
                break;
            case LI:
                endLi();
                break;
            default:
                break;
        }

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param ch {@inheritDoc}
     * @param start {@inheritDoc}
     * @param length {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if(!this.inLine) return;
        this.content.append(ch, start, length);
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param ch {@inheritDoc}
     * @param start {@inheritDoc}
     * @param length {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param target {@inheritDoc}
     * @param data {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param name {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        return;
    }

}
