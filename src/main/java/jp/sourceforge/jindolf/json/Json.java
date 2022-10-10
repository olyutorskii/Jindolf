/*
 * JSON utilities
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: Json.java 914 2009-11-24 11:16:36Z olyutorskii $
 */

package jp.sourceforge.jindolf.json;

import java.io.IOException;
import java.io.Reader;

/**
 * JSON各種共通ユーティリティ。
 */
public final class Json{

    /**
     * JSON最上位構造から文字出力を開始する。
     * @param appout 出力先
     * @param value JSONのObjectかArray
     * @throws IOException 出力エラー
     * @throws IllegalArgumentException 出力対象がObjectでもArrayでもない。
     */
    public static void writeJsonTop(Appendable appout, JsValue value)
            throws IOException,
                   IllegalArgumentException {
        if( ! (value instanceof JsObject) && ! (value instanceof JsArray) ){
            throw new IllegalArgumentException();
        }

        JsonAppender appender = new JsonAppender(appout);

        try{
            value.traverse(appender);
        }catch(JsVisitException e){
            Throwable cause = e.getCause();
            if(cause instanceof IOException){
                throw (IOException) cause;
            }else if(cause instanceof RuntimeException){
                throw (RuntimeException) cause;
            }else if(cause instanceof Error){
                throw (Error) cause;
            }else{
                assert false;
                return;
            }
        }

        appender.flush();

        return;
    }

    /**
     * JSON規格のwhitespace文字を判定する。
     * @param ch 判定対象文字
     * @return whitespaceならtrue
     */
    public static boolean isWhitespace(char ch){
        if(ch == '\t'    ) return true;
        if(ch == '\r'    ) return true;
        if(ch == '\n'    ) return true;
        if(ch == '\u0020') return true;
        return false;
    }

    /**
     * whitespace文字を読み飛ばす。
     * @param reader 文字入力
     * @throws IOException 入力エラー
     */
    static void skipWhiteSpace(JsonReader reader)
            throws IOException{
        for(;;){
            int chData = reader.read();
            if(chData < '\u0000') break;
            if( ! isWhitespace((char)chData) ){
                reader.unread(chData);
                break;
            }
        }
        return;
    }

    /**
     * 各種定数(true,false,null)を文字ストリームから読み取る。
     * 長さ0の文字定数には無条件でfalseを返す。
     * @param reader 文字入力
     * @param text 文字定数
     * @return 文字定数が文字入力に現れればtrue。
     * 見つからないもしくはストリームの終わりに達したときはfalse
     * @throws IOException 入力エラー
     * @throws IllegalArgumentException 文字定数が長すぎる
     */
    static boolean parseConst(JsonReader reader,
                               CharSequence text)
            throws IOException,
                   IllegalArgumentException {
        int textLength = text.length();
        if(textLength <= 0) return false;
        if(textLength >= JsonReader.PUSHBACK_TOKENS){
            throw new IllegalArgumentException();
        }

        int[] backData = new int[textLength - 1];
        int readed = 0;

        for(;;){
            int chData = reader.read();
            if(chData != text.charAt(readed)){
                if(chData >= '\u0000') reader.unread(chData);
                for(int pos = readed - 1; pos >= 0; pos--){
                    reader.unread(backData[pos]);
                }
                break;
            }

            if(readed >= backData.length) return true;

            backData[readed++] = chData;
        }

        return false;
    }

    /**
     * JSONの各種Valueを文字ストリームから読み取る。
     * @param reader 文字入力
     * @return 各種Value
     * @throws IOException 入力エラー
     * @throws JsParseException パースエラー
     */
    public static JsValue parseValue(Reader reader)
            throws IOException, JsParseException{
        JsonReader jsreader;
        if(reader instanceof JsonReader){
            jsreader = (JsonReader) reader;
        }else{
            jsreader = new JsonReader(reader);
        }

        return parseValue(jsreader);
    }

    /**
     * JSONの各種Valueを文字ストリームから読み取る。
     * @param reader 文字入力
     * @return 各種Value。ストリームの終わりに達したときはnull
     * @throws IOException 入力エラー
     * @throws JsParseException パースエラー
     */
    static JsValue parseValue(JsonReader reader)
            throws IOException, JsParseException{
        skipWhiteSpace(reader);

        if(parseConst(reader, JsNull.NULL.toString())){
            return JsNull.NULL;
        }else if(parseConst(reader, JsBoolean.TRUE.toString())){
            return JsBoolean.TRUE;
        }else if(parseConst(reader, JsBoolean.FALSE.toString())){
            return JsBoolean.FALSE;
        }

        int head = reader.read();
        if(head < '\u0000') return null;

        if( head == '-' || ('0' <= head && head <= '9') ){
            reader.unread(head);
            return JsNumber.parseNumber(reader);
        }else if(head == '{'){
            reader.unread(head);
            return JsObject.parseObject(reader);
        }else if(head == '['){
            reader.unread(head);
            return JsArray.parseArray(reader);
        }else if(head == '"'){
            reader.unread(head);
            return JsString.parseString(reader);
        }

        throw new JsParseException();
    }

    /**
     * 隠しコンストラクタ。
     */
    private Json(){
        assert false;
        throw new AssertionError();
    }

}
