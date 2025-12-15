/*
 * core data
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import jp.sfjp.jindolf.dxchg.XmlUtils;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.PreDefAvatar;
import org.xml.sax.SAXException;

/**
 * JinCore 各種固定データのロード。
 *
 * <p>I/Oはリソースへのアクセスを前提とし、異常系はリカバーしない。
 */
public final class CoreData{

    private static final List<LandDef> LIST_LANDDEF;
    private static final List<PreDefAvatar> LIST_PREDEFAVATAR;

    static{
        try{
            LIST_LANDDEF = buildLandDefList();
            LIST_PREDEFAVATAR = buildPreDefAvatarList();
        }catch(   IOException
                | URISyntaxException
                | ParserConfigurationException
                | SAXException e
        ){
            throw new ExceptionInInitializerError(e);
        }
    }


    /**
     * hidden constructor.
     */
    private CoreData(){
        assert false;
    }


    /**
     * load and build LandDef list.
     *
     * @return LandDef list
     * @throws IOException resource IO error
     * @throws URISyntaxException xml error
     * @throws ParserConfigurationException xml error
     * @throws SAXException xml error
     */
    private static List<LandDef> buildLandDefList()
            throws
            IOException,
            URISyntaxException,
            ParserConfigurationException,
            SAXException {
        DocumentBuilder builder = XmlUtils.createDocumentBuilder();
        List<LandDef> result = LandDef.buildLandDefList(builder);
        return result;
    }

    /**
     * load and build PreDefAvatar list.
     *
     * @return PreDefAvatar list
     * @throws IOException resource IO error
     * @throws URISyntaxException xml error
     * @throws ParserConfigurationException xml error
     * @throws SAXException xml error
     */
    private static List<PreDefAvatar> buildPreDefAvatarList()
            throws
            IOException,
            URISyntaxException,
            ParserConfigurationException,
            SAXException {
        DocumentBuilder builder = XmlUtils.createDocumentBuilder();
        List<PreDefAvatar> result =
                PreDefAvatar.buildPreDefAvatarList(builder);
        return result;

    }


    /**
     * return LandDef list.
     *
     * @return list
     */
    public static List<LandDef> getLandDefList(){
        return LIST_LANDDEF;
    }

    /**
     * return PreDefAvatar list.
     *
     * @return list
     */
    public static List<PreDefAvatar> getPreDefAvatarList(){
        return LIST_PREDEFAVATAR;
    }

}
