/*
 * HTML sequence
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.net;

import java.net.URL;
import jp.osdn.jindolf.parser.content.DecodedContent;

/**
 * HTML本文。
 * 任意のDecodedContentをラップする。
 * 由来となるURLと受信時刻を含む。
 */
// TODO Dateも含めたい
public class HtmlSequence implements CharSequence{

    private final URL url;
    private final long datems;
    private final DecodedContent html;

    /**
     * コンストラクタ。
     *
     * @param url 由来のURL
     * @param datems 受信時刻(エポックミリ秒)
     * @param html HTML本文
     * @throws java.lang.NullPointerException 引数がnull
     */
    public HtmlSequence(URL url, long datems, DecodedContent html)
        throws NullPointerException{
        if(url == null || html == null){
            throw new NullPointerException();
        }
        this.url = url;
        this.datems = datems;
        this.html = html;
        return;
    }

    /**
     * URLを返す。
     *
     * @return URL
     */
    public URL getURL(){
        return this.url;
    }

    /**
     * 受信時刻を返す。
     * 単位はエポック時からのミリ秒。
     *
     * @return 受信時刻
     */
    public long getDateMs(){
        return this.datems;
    }

    /**
     * HTML文字列を返す。
     *
     * @return HTML文字列
     */
    public DecodedContent getContent(){
        return this.html;
    }

    /**
     * {@inheritDoc}
     *
     * @param index {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public char charAt(int index){
        return this.html.charAt(index);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int length(){
        return this.html.length();
    }

    /**
     * {@inheritDoc}
     *
     * @param start {@inheritDoc}
     * @param end {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public CharSequence subSequence(int start, int end){
        return this.html.subSequence(start, end);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        return this.html.toString();
    }

}
