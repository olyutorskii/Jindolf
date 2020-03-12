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

    private static final Map<String, PeriodType> periodTypeMap =
            new HashMap<>();

    static{
        periodTypeMap.put("prologue", PeriodType.PROLOGUE);
        periodTypeMap.put("progress", PeriodType.PROGRESS);
        periodTypeMap.put("epilogue", PeriodType.EPILOGUE);
    }


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


    /**
     * constructor.
     */
    public VillageHandler(){
        super();
        return;
    }


    /**
     * 会話種別をデコードする。
     *
     * @param type 属性値
     * @return 会話種別
     */
    private static TalkType decodeTalkType(String type){
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
                throw new AssertionError();
        }

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
        String landId = atts.getValue("", "landId");
        String vid    = atts.getValue("", "vid");
        String name   = atts.getValue("", "fullName");
        String state  = atts.getValue("", "state");

        this.land = CoreData.getLandDefList().stream()
                .filter(landDef -> landDef.getLandId().equals(landId))
                .map(landDef -> new Land(landDef))
                .findFirst().get();

        this.village = new Village(this.land, vid, name);
        this.village.setState(VillageState.GAMEOVER);
        this.talkNo = 0;
        return;
    }

    /**
     * avatar要素開始の受信。
     *
     * @param atts 属性
     */
    private void startAvatar(Attributes atts){
        String avatarId = atts.getValue("", "avatarId");
        String fullName = atts.getValue("", "fullName");
        String shortName = atts.getValue("", "shortName");
        String faceIconUri = atts.getValue("", "faceIconURI");

        Avatar avatar = Avatar.getPredefinedAvatar(fullName);

        this.village.addAvatar(avatar);
        this.idAvatarMap.put(avatarId, avatar);

        System.out.println(avatar);

        return;
    }

    /**
     * period要素開始の受信。
     *
     * @param atts 属性
     */
    private void startPeriod(Attributes atts){
        String typeAttr = atts.getValue("", "type");
        String dayAttr = atts.getValue("", "day");

        PeriodType periodType = periodTypeMap.get(typeAttr);
        int day = Integer.parseInt(dayAttr);

        this.period = new Period(this.village, periodType, day);
        int periodIdx = this.village.getPeriodSize();
        this.village.setPeriod(periodIdx, this.period);

        return;
    }

    /**
     * talk要素開始の受信。
     *
     * @param atts 属性
     */
    private void startTalk(Attributes atts){
        String type = atts.getValue("", "type");
        String avatarId = atts.getValue("", "avatarId");
        String xname = atts.getValue("", "xname");
        String time = atts.getValue("", "time");
        this.talkAvatar = this.idAvatarMap.get(avatarId);
        this.talkType = decodeTalkType(type);
        this.messageId = xname;
        this.content.setLength(0);
        this.talkTime = time;
    }

    /**
     * talk要素終了の受信。
     */
    private void endTalk(){
        int no = 0;
        if(this.talkType == TalkType.PUBLIC){
            no = this.talkNo++;
        }

        int d0;
        int d1;
        d1 = this.talkTime.charAt(0) - '0';
        d0 = this.talkTime.charAt(1) - '0';
        int hour = d1 * 10 + d0;
        d1 = this.talkTime.charAt(3) - '0';
        d0 = this.talkTime.charAt(4) - '0';
        int minute = d1 * 10 + d0;

        Talk talk = new Talk(
                this.period,
                this.talkType, this.talkAvatar,
                no, this.messageId,
                hour, minute,
                this.content.toString()
        );

        this.period.addTopic(talk);

        this.talkType = null;
        this.talkAvatar = null;
        this.messageId = null;
        this.content.setLength(0);
        this.talkTime = null;
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
        if( ! NS_JINARCHIVE.equals(uri)){
            return;
        }

        switch(localName){
            case "village":
                startVillage(atts);
                break;
            case "avatar":
                startAvatar(atts);
                break;
            case "period":
                startPeriod(atts);
                break;
            case "talk":
                startTalk(atts);
                break;
            case "li":
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
        if( ! NS_JINARCHIVE.equals(uri)){
            return;
        }
        switch(localName){
            case "talk":
                endTalk();
                break;
            case "li":
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
