/*
 * village handler
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data.xml;

import java.util.HashMap;
import java.util.Map;
import jp.sfjp.jindolf.data.Avatar;
import jp.sfjp.jindolf.data.CoreData;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.data.Period;
import jp.sfjp.jindolf.data.Talk;
import jp.sfjp.jindolf.data.Village;
import jp.sourceforge.jindolf.corelib.PeriodType;
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

    private Avatar talkAvatar;
    private int talkNo;

    private TalkType talkType;
    private String messageId;
    private String talkTime;

    private boolean inLine;
    private final StringBuilder content = new StringBuilder(250);

    private final Map<String, Avatar> idAvatarMap = new HashMap<>();
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
     * @return 属性値
     */
    private static String attrValue(Attributes atts, String attrName){
        String result = atts.getValue("", attrName);
        return result;
    }

    /**
     * Period種別をデコードする。
     *
     * @param type 属性値
     * @return Period種別
     * @throws SAXException 不正な属性値
     */
    private static PeriodType decodePeriodType(String type)
            throws SAXException{
        PeriodType result;

        switch(type){
            case "prologue":
                result = PeriodType.PROLOGUE;
                break;
            case "progress":
                result = PeriodType.PROGRESS;
                break;
            case "epilogue":
                result = PeriodType.EPILOGUE;
                break;
            default:
                assert false;
                throw new SAXException("invalid period type:" + type);
        }

        return result;
    }

    /**
     * 会話種別をデコードする。
     *
     * @param type 属性値
     * @return 会話種別
     * @throws SAXException 不正な属性値
     */
    private static TalkType decodeTalkType(String type) throws SAXException{
        TalkType result;

        switch(type){
            case "public":
                result = TalkType.PUBLIC;
                break;
            case "wolf":
                result = TalkType.WOLFONLY;
                break;
            case "private":
                result = TalkType.PRIVATE;
                break;
            case "grave":
                result = TalkType.GRAVE;
                break;
            default:
                assert false;
                throw new SAXException("invalid talk type: " + type);
        }

        return result;
    }

    /**
     * hour値をデコードする。
     *
     * <p>例: 22:49:00+09:00 の 22
     *
     * @param txt 属性値
     * @return hour値
     */
    private static int decodeHour(String txt){
        int d1 = txt.charAt(0) - '0';
        int d0 = txt.charAt(1) - '0';
        int result = d1 * 10 + d0;
        return result;
    }

    /**
     * minute値をデコードする。
     *
     * <p>例: 22:49:00+09:00 の 49
     *
     * @param txt 属性値
     * @return minute値
     */
    private static int decodeMinute(String txt){
        int d1 = txt.charAt(3) - '0';
        int d0 = txt.charAt(4) - '0';
        int result = d1 * 10 + d0;
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
        this.inLine = false;
        return;
    }

    /**
     * パース開始後の後始末。
     */
    private void resetAfter(){
        this.idAvatarMap.clear();
        this.content.setLength(0);
        this.nsPfx = null;
        this.land = null;
        this.period = null;
        this.messageId = null;
        this.talkTime = null;
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

        PeriodType periodType = decodePeriodType(typeAttr);
        int day = Integer.parseInt(dayAttr);

        this.period = new Period(this.village, periodType, day);

        // append tail
        int periodIdx = this.village.getPeriodSize();
        this.village.setPeriod(periodIdx, this.period);

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

        this.talkAvatar = this.idAvatarMap.get(avatarId);
        this.talkType = decodeTalkType(type);
        this.messageId = xname;
        this.content.setLength(0);
        this.talkTime = time;

        return;
    }

    /**
     * talk要素終了の受信。
     */
    private void endTalk(){
        int no = 0;
        if(this.talkType == TalkType.PUBLIC){
            no = ++this.talkNo;
        }

        int hour   = decodeHour  (this.talkTime);
        int minute = decodeMinute(this.talkTime);

        String dialog = this.content.toString();

        Talk talk = new Talk(
                this.period,
                this.talkType, this.talkAvatar,
                no, this.messageId,
                hour, minute,
                dialog
        );

        this.period.addTopic(talk);

        this.talkType = null;
        this.talkAvatar = null;
        this.messageId = null;
        this.content.setLength(0);
        this.talkTime = null;

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

        switch(tag){
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
