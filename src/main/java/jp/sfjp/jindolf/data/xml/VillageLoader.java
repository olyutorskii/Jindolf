/*
 * village loader
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import jp.sfjp.jindolf.data.Village;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * JinArchiverなどでXMLファイルにアーカイブされた人狼BBSの村プレイ記録を
 * 読み取る。
 */
public class VillageLoader {

    private static final String F_DISALLOW_DOCTYPE_DECL =
            "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String F_EXTERNAL_GENERAL_ENTITIES =
            "http://xml.org/sax/features/external-general-entities";
    private static final String F_EXTERNAL_PARAMETER_ENTITIES =
            "http://xml.org/sax/features/external-parameter-entities";
    private static final String F_LOAD_EXTERNAL_DTD =
            "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    private static final String F_NAMESPACE =
            "http://xml.org/sax/features/namespaces";
    private static final String F_NAMESPACEPFX =
            "http://xml.org/sax/features/namespace-prefixes";


    /**
     * constructor.
     */
    private VillageLoader(){
        assert false;
        return;
    }


    /**
     * XMLファイルをパースする。
     *
     * @param xmlFile XMLファイル
     * @return 村
     * @throws IOException I/Oエラー
     * @throws SAXException XMLの形式エラー
     */
    public static Village parseVillage(File xmlFile)
            throws IOException, SAXException{
        Objects.nonNull(xmlFile);

        boolean isNormal;
        isNormal = xmlFile.isFile()
                && xmlFile.exists()
                && xmlFile.canRead();
        if(!isNormal){
            throw new IOException(xmlFile.getPath() + "を読み込むことができません");
        }

        Path path = xmlFile.toPath();

        Village result = parseVillage(path);
        return result;
    }

    /**
     * XMLファイルをパースする。
     *
     * @param path XMLファイルのPath
     * @return 村
     * @throws IOException I/Oエラー
     * @throws SAXException XMLの形式エラー
     */
    public static Village parseVillage(Path path)
            throws IOException, SAXException{
        Objects.nonNull(path);

        path = path.normalize();

        boolean isNormal;
        isNormal = Files.exists(path)
                && Files.isRegularFile(path)
                && Files.isReadable(path);
        if(!isNormal){
            throw new IOException(path.toString() + "を読み込むことができません");
        }

        Village result;
        try(InputStream is = pathToStream(path)){
            result = parseVillage(is);
        }

        return result;
    }

    /**
     * Pathから入力ストリームを得る。
     *
     * @param path Path
     * @return バッファリングされた入力ストリーム
     * @throws IOException I/Oエラー
     */
    private static InputStream pathToStream(Path path) throws IOException{
        InputStream is;
        is = Files.newInputStream(path);
        is = new BufferedInputStream(is, 4*1024);
        return is;
    }

    /**
     * XML入力をパースする。
     *
     * @param istream XML入力
     * @return 村
     * @throws IOException I/Oエラー
     * @throws SAXException XMLの形式エラー
     */
    public static Village parseVillage(InputStream istream)
            throws IOException, SAXException{
        InputSource isource = new InputSource(istream);
        Village result = parseVillage(isource);
        return result;
    }

    /**
     * XML入力をパースする。
     *
     * @param isource XML入力
     * @return 村
     * @throws IOException I/Oエラー
     * @throws SAXException XMLの形式エラー
     */
    public static Village parseVillage(InputSource isource)
            throws IOException, SAXException{
        XMLReader reader = buildReader();
        VillageHandler handler = new VillageHandler();
        reader.setContentHandler(handler);

        reader.parse(isource);

        Village result = handler.getVillage();
        return result;
    }

    /**
     * SAXパーサファクトリを生成する。
     *
     * <ul>
     * <li>XML名前空間機能は有効になる。
     * <li>DTDによる形式検証は無効となる。
     * <li>XIncludeによる差し込み機能は無効となる。
     * </ul>
     *
     * @param schema スキーマ
     * @return ファクトリ
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">
     *     XML External Entity Prevention Cheat Sheet</a>
     */
    private static SAXParserFactory buildFactory(Schema schema){
        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setNamespaceAware(true);
        factory.setValidating(false);
        factory.setXIncludeAware(false);

        try{
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            //factory.setFeature(F_DISALLOW_DOCTYPE_DECL, true);
            factory.setFeature(F_EXTERNAL_GENERAL_ENTITIES, false);
            factory.setFeature(F_EXTERNAL_PARAMETER_ENTITIES, false);
            factory.setFeature(F_LOAD_EXTERNAL_DTD, false);
            factory.setFeature(F_NAMESPACE, true);
            factory.setFeature(F_NAMESPACEPFX, true);
        }catch(   ParserConfigurationException
                | SAXNotRecognizedException
                | SAXNotSupportedException e ){
            assert false;
            throw new AssertionError(e);
        }

        factory.setSchema(schema);

        return factory;
    }

    /**
     * SAXパーサを生成する。
     *
     * @param schema スキーマ
     * @return SAXパーサ
     */
    private static SAXParser buildParser(Schema schema){
        SAXParserFactory factory = buildFactory(schema);

        SAXParser parser;
        try{
            parser = factory.newSAXParser();
        }catch(ParserConfigurationException | SAXException e){
            assert false;
            throw new AssertionError(e);
        }

        try{
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        }catch(SAXNotRecognizedException | SAXNotSupportedException e){
            assert false;
            throw new AssertionError(e);
        }

        return parser;
    }

    /**
     * XMLリーダを生成する。
     *
     * @return XMLリーダ
     */
    private static XMLReader buildReader(){
        SAXParser parser = buildParser((Schema)null);

        XMLReader reader;
        try{
            reader = parser.getXMLReader();
        }catch(SAXException e){
            assert false;
            throw new AssertionError(e);
        }

        reader.setEntityResolver(NoopEntityResolver.NOOP_RESOLVER);
        reader.setErrorHandler(BotherHandler.HANDLER);

        return reader;
    }

}
