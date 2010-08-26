/*
 * JSON string value
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: JsString.java 917 2009-11-24 13:17:07Z olyutorskii $
 */

package jp.sourceforge.jindolf.json;

import java.io.IOException;

/**
 * JSON 文字列Value。
 */
public class JsString
        extends AbstractJsValue
        implements CharSequence, Comparable<JsString> {

    /**
     * FFFF形式4桁で16進エスケープされた文字列を読み、
     * 1文字にデコードする。
     * @param reader 文字入力
     * @return 文字
     * @throws IOException 入力エラー
     * @throws JsParseException パースエラーもしくは入力終了
     */
    static char parseHexChar(JsonReader reader)
            throws IOException, JsParseException{
        int hex1 = reader.read();
        int hex2 = reader.read();
        int hex3 = reader.read();
        int hex4 = reader.read();
        if(hex4 < '\u0000') throw new JsParseException();

        char hex1Ch = (char) hex1;
        char hex2Ch = (char) hex2;
        char hex3Ch = (char) hex3;
        char hex4Ch = (char) hex4;

        int digit1 = Character.digit(hex1Ch, 16);
        int digit2 = Character.digit(hex2Ch, 16);
        int digit3 = Character.digit(hex3Ch, 16);
        int digit4 = Character.digit(hex4Ch, 16);

        if(digit1 < 0) throw new JsParseException();
        if(digit2 < 0) throw new JsParseException();
        if(digit3 < 0) throw new JsParseException();
        if(digit4 < 0) throw new JsParseException();

        int digit = 0;
        digit += digit1;
        digit <<= 4;
        digit += digit2;
        digit <<= 4;
        digit += digit3;
        digit <<= 4;
        digit += digit4;

        char result = (char) digit;

        return result;
    }

    /**
     * ダブルクォーテーションで囲まれた文字列を読み込む。
     * @param reader 文字入力
     * @return 文字列Value
     * @throws IOException 入力エラー
     * @throws JsParseException パースエラーもしくは入力終了
     */
    static JsString parseString(JsonReader reader)
            throws IOException, JsParseException{
        int chData;

        Json.skipWhiteSpace(reader);
        chData = reader.read();
        if(chData < '\u0000') return null;
        if(chData != '"') throw new JsParseException();

        StringBuilder text = new StringBuilder();

        for(;;){
            chData = reader.read();
            if(chData < '\u0000') throw new JsParseException();
            if(chData == '"') break;

            if(chData == '\\'){
                chData = reader.read();
                if(chData < '\u0000') throw new JsParseException();
                if     (chData == '"' ) text.append('"');
                else if(chData == '\\') text.append('\\');
                else if(chData == '/' ) text.append('/');
                else if(chData == 'b' ) text.append('\b');
                else if(chData == 'f' ) text.append('\f');
                else if(chData == 'n' ) text.append('\n');
                else if(chData == 'r' ) text.append('\r');
                else if(chData == 't' ) text.append('\t');
                else if(chData == 'u')  text.append(parseHexChar(reader));
                else                    throw new JsParseException();
            }else{
                text.append((char)chData);
            }
        }

        JsString result = new JsString(text);

        return result;
    }

    /**
     * JSON 文字列Value形式で文字列を出力する。
     * @param appout 文字出力
     * @param seq 文字列
     * @throws IOException 出力エラー
     */
    public static void writeText(Appendable appout, CharSequence seq)
            throws IOException{
        appout.append('"');

        int length = seq.length();
        for(int pos = 0; pos < length; pos++){
            char ch = seq.charAt(pos);

            switch(ch){
            case '"' : appout.append('\\').append('"');  break;
            case '\\': appout.append('\\').append('\\'); break;
            case '/' : appout.append('\\').append('/');  break;
            case '\b': appout.append('\\').append('b');  break;
            case '\f': appout.append('\\').append('f');  break;
            case '\n': appout.append('\\').append('n');  break;
            case '\r': appout.append('\\').append('r');  break;
            case '\t': appout.append('\\').append('t');  break;
            default:
                if(Character.isISOControl(ch)){
                    String hex = "0000" + Integer.toHexString(ch);
                    hex = hex.substring(hex.length() - 4);
                    appout.append("\\u").append(hex);
                }else{
                    appout.append(ch);
                }
                break;
            }
        }

        appout.append('"');

        return;
    }

    private final String text;

    /**
     * コンストラクタ。
     * 空文字が設定される。
     */
    public JsString(){
        this(null);
        return;
    }

    /**
     * コンストラクタ。
     * 引数はJSON書式ではない。
     * @param seq 文字列。nullなら空文字が設定される。
     */
    public JsString(CharSequence seq){
        super();
        if(seq == null){
            this.text = "";
        }else{
            this.text = seq.toString();
        }
        return;
    }

    /**
     * {@inheritDoc}
     * @param index {@inheritDoc}
     * @return {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public char charAt(int index)
            throws IndexOutOfBoundsException{
        return this.text.charAt(index);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public int length(){
        return this.text.length();
    }

    /**
     * {@inheritDoc}
     * @param start {@inheritDoc}
     * @param end {@inheritDoc}
     * @return {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public CharSequence subSequence(int start, int end)
            throws IndexOutOfBoundsException{
        return this.text.subSequence(start, end);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        return this.text.hashCode();
    }

    /**
     * {@inheritDoc}
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if(this == obj) return true;

        if( ! (obj instanceof JsString) ) return false;
        JsString string = (JsString) obj;

        return this.text.equals(string.text);
    }

    /**
     * {@inheritDoc}
     * @param value {@inheritDoc}
     * @return {@inheritDoc}
     */
    public int compareTo(JsString value){
        if(this == value) return 0;
        if(value == null) return +1;
        return this.text.compareTo(value.text);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        StringBuilder string = new StringBuilder();
        try{
            writeText(string, this.text);
        }catch(IOException e){
            assert false;
        }
        return string.toString();
    }

    /**
     * クォーテーションされていない生の文字列を返す。
     * @return 生の文字列
     */
    public String toRawString(){
        return this.text;
    }

}
