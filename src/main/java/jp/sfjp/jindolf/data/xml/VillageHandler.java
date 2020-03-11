/*
 * village handler
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data.xml;

import jp.sfjp.jindolf.data.Village;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * VillageのXMLパーサ本体。
 */
public class VillageHandler implements ContentHandler{

    /**
     * constructor.
     */
    public VillageHandler(){
        super();
        return;
    }


    /**
     * パースした結果のVillageを返す。
     *
     * @return 村
     */
    public Village getVillage(){
        return null;
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
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
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
        //System.out.println(prefix);
        //System.out.println(uri);
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
        //System.out.println(uri);
        //System.out.println(localName);
        //System.out.println(qName);
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
