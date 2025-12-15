/*
 * XML decoders
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data.xml;

import jp.sourceforge.jindolf.corelib.GameRole;
import jp.sourceforge.jindolf.corelib.PeriodType;
import jp.sourceforge.jindolf.corelib.TalkType;
import org.xml.sax.SAXException;

/**
 * XML values decoders.
 */
public final class XmlDecoder {

    /**
     * hidden constructor.
     */
    private XmlDecoder(){
        assert false;
    }


    /**
     * Period種別をデコードする。
     *
     * @param type 属性値
     * @return Period種別
     * @throws SAXException 不正な属性値
     */
    public static PeriodType decodePeriodType(String type)
            throws SAXException {
        PeriodType result;

        switch (type) {
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
    public static TalkType decodeTalkType(String type)
            throws SAXException {
        TalkType result;

        switch (type) {
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
    public static int decodeHour(String txt) {
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
    public static int decodeMinute(String txt) {
        int d1 = txt.charAt(3) - '0';
        int d0 = txt.charAt(4) - '0';
        int result = d1 * 10 + d0;
        return result;
    }

    /**
     * roleをデコードする。
     *
     * @param role role属性値
     * @return GameRole種別
     * @throws SAXException 不正な値
     */
    static GameRole decodeRole(String role) throws SAXException {
        GameRole result;

        switch (role) {
        case "innocent":
            result = GameRole.INNOCENT;
            break;
        case "wolf":
            result = GameRole.WOLF;
            break;
        case "seer":
            result = GameRole.SEER;
            break;
        case "shaman":
            result = GameRole.SHAMAN;
            break;
        case "madman":
            result = GameRole.MADMAN;
            break;
        case "hunter":
            result = GameRole.HUNTER;
            break;
        case "frater":
            result = GameRole.FRATER;
            break;
        case "hamster":
            result = GameRole.HAMSTER;
            break;
        default:
            assert false;
            throw new SAXException("invalid role: " + role);
        }

        return result;
    }

}
