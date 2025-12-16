/*
 * XML utilities
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.dxchg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import jp.sourceforge.jindolf.corelib.XmlResource;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XML関連のユーティリティ。
 */
public final class XmlUtils{

    private static final ErrorHandler STRICT_HANDLER = new StrictHandler();
    private static final XmlResourceResolver RESOLVER =
            new XmlResourceResolver();


    /**
     * 隠しコンストラクタ。
     */
    private XmlUtils(){
        super();
        return;
    }


    /**
     * リゾルバ経由でリソースにアクセスし、
     * 共通スキーマによるバリデーションを行うためのDocumentBuilderを生成する。
     *
     * @return ビルダ
     * @throws URISyntaxException URIが不正
     * @throws IOException 入出力エラー
     * @throws ParserConfigurationException パーサ準備失敗
     * @throws SAXException スキーマが変
     */
    public static DocumentBuilder createDocumentBuilder()
            throws IOException,
                   URISyntaxException,
                   ParserConfigurationException,
                   SAXException {
        Schema xsdSchema = createCoreSchema(RESOLVER);
        DocumentBuilder builder = createBuilder(xsdSchema, RESOLVER);

        return builder;
    }

    /**
     * 共通コアスキーマを得る。
     * このスキーマはLSResourceResolverによるリダイレクトをサポートする。
     *
     * @param resolver リゾルバ
     * @return スキーマ
     * @throws URISyntaxException URIが不正
     * @throws IOException 入出力エラー
     * @throws SAXException スキーマが変
     */
    public static Schema createCoreSchema(LSResourceResolver resolver)
            throws URISyntaxException,
                   IOException,
                   SAXException {
        SchemaFactory xsdSchemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        xsdSchemaFactory.setResourceResolver(resolver);
        xsdSchemaFactory.setErrorHandler(STRICT_HANDLER);

        InputStream is = XmlResource.I_URL_COREXML.openStream();
        StreamSource source = new StreamSource(is);
        Schema schema = xsdSchemaFactory.newSchema(source);

        return schema;
    }

    /**
     * スキーマによる妥当性検証を兼用するDocumentBuilderを生成する。
     *
     * @param schema スキーマ
     * @param resolver リゾルバ
     * @return DOM用ビルダ
     * @throws ParserConfigurationException パーサ準備失敗
     */
    public static DocumentBuilder createBuilder(Schema schema,
                                                  EntityResolver resolver)
            throws ParserConfigurationException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        factory.setSchema(schema);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(STRICT_HANDLER);
        builder.setEntityResolver(resolver);

        return builder;
    }

    /**
     * スキーマ検証用の厳密なエラーハンドラ。
     */
    public static class StrictHandler implements ErrorHandler{

        /**
         * コンストラクタ。
         */
        public StrictHandler(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         *
         * @param e {@inheritDoc} エラー情報
         * @throws org.xml.sax.SAXException {@inheritDoc} 引数と同じ物
         */
        @Override
        public void error(SAXParseException e) throws SAXException{
            throw e;
        }

        /**
         * {@inheritDoc}
         *
         * @param e {@inheritDoc} エラー情報
         * @throws org.xml.sax.SAXException {@inheritDoc} 引数と同じ物
         */
        @Override
        public void fatalError(SAXParseException e) throws SAXException{
            throw e;
        }

        /**
         * {@inheritDoc}
         *
         * @param e {@inheritDoc} エラー情報
         * @throws org.xml.sax.SAXException {@inheritDoc} 引数と同じ物
         */
        @Override
        public void warning(SAXParseException e) throws SAXException{
            throw e;
        }

    }

}
