/*
 * JSON number value
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: JsNumber.java 915 2009-11-24 11:19:48Z olyutorskii $
 */

package jp.sourceforge.jindolf.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * JSON 数値Value。
 * 10を基数としたjava.math.BigDecimalを実装ベースとする
 * IEEE754浮動小数ではない。
 */
public class JsNumber
        extends AbstractJsValue
        implements Comparable<JsNumber> {

    /**
     * 文字ストリームから符号付きの数字並びを読み込む。
     * +符号は読み飛ばされる。
     * 冒頭の連続する0はそのまま読まれる。
     * @param reader 文字入力
     * @param app 出力先
     * @param allowZeroTrail 冒頭の2つ以上連続するゼロを許すならtrue
     * @return 引数と同じ出力先
     * @throws IOException 入出力エラー
     * @throws JsParseException パースエラーもしくは入力終了
     */
    private static Appendable appendDigitText(JsonReader reader,
                                                Appendable app,
                                                boolean allowZeroTrail)
            throws IOException, JsParseException{
        int chData;

        chData = reader.read();
        if     (chData < '\u0000') throw new JsParseException();
        else if(chData == '-') app.append('-');
        else if(chData != '+') reader.unread(chData);

        boolean hasAppended = false;
        boolean zeroStarted = false;
        for(;;){
            chData = reader.read();
            if(chData < '\u0000') throw new JsParseException();

            if('0' <= chData && chData <= '9'){
                app.append((char)chData);

                if(zeroStarted && ! allowZeroTrail){
                    throw new JsParseException();
                }

                if(chData == '0' &&  ! hasAppended ){
                    zeroStarted = true;
                }

                hasAppended = true;
            }else{
                if( ! hasAppended ) throw new JsParseException();
                reader.unread(chData);
                break;
            }
        }

        return app;
    }

    /**
     * 文字ストリームから符号付きの数字並びを読み込む。
     * +符号はパースエラーとなる。
     * @param reader 文字入力
     * @param app 出力先
     * @return 引数と同じ出力先
     * @throws IOException 入出力エラー
     * @throws JsParseException パースエラーもしくは入力終了
     */
    private static Appendable appendIntegerPart(JsonReader reader,
                                                  Appendable app )
            throws IOException, JsParseException{
        int chData;

        chData = reader.read();
        if(chData < '\u0000') throw new JsParseException();
        if(chData == '+') throw new JsParseException();
        reader.unread(chData);

        appendDigitText(reader, app, false);

        return app;
    }

    /**
     * 文字ストリームから「.」で始まる小数部を読み込む。
     * 小数部がなければなにもせずに戻る。
     * @param reader 文字入力
     * @param app 出力先
     * @return 引数と同じ出力先
     * @throws IOException 入出力エラー
     * @throws JsParseException パースエラーもしくは入力終了
     */
    private static Appendable appendFractionPart(JsonReader reader,
                                                    Appendable app )
            throws IOException, JsParseException{
        int chData;

        chData = reader.read();
        if(chData < '\u0000') throw new JsParseException();
        if(chData != '.'){
            reader.unread(chData);
            return app;
        }

        app.append(".");

        boolean hasAppended = false;
        for(;;){
            chData = reader.read();
            if(chData < '\u0000') throw new JsParseException();

            if('0' <= chData && chData <= '9'){
                app.append((char)chData);
                hasAppended = true;
            }else{
                if( ! hasAppended ) throw new JsParseException();
                reader.unread(chData);
                break;
            }
        }

        return app;
    }

    /**
     * 文字ストリームから「e」もしくは「E」で始まる指数部を読み込む。
     * 指数部がなければなにもせずに戻る。
     * @param reader 文字入力
     * @param app 出力先
     * @return 引数と同じ出力先
     * @throws IOException 入出力エラー
     * @throws JsParseException パースエラーもしくは入力終了
     */
    private static Appendable appendExpPart(JsonReader reader,
                                              Appendable app )
            throws IOException, JsParseException{
        int chData;

        chData = reader.read();
        if(chData < '\u0000') throw new JsParseException();
        if(chData != 'e' && chData != 'E'){
            reader.unread(chData);
            return app;
        }

        app.append('E');

        appendDigitText(reader, app, true);

        return app;
    }

    /**
     * 文字ストリームからJSON数値Valueを読み込む。
     * @param reader 文字入力
     * @return 数値Value
     * @throws IOException 入力エラー
     * @throws JsParseException パースエラーもしくは入力終了
     */
    static JsNumber parseNumber(JsonReader reader)
            throws IOException, JsParseException{
        Json.skipWhiteSpace(reader);

        StringBuilder numText = new StringBuilder();
        appendIntegerPart (reader, numText);
        appendFractionPart(reader, numText);
        appendExpPart     (reader, numText);
        JsNumber result = new JsNumber(numText);

        return result;
    }

    private BigDecimal decimal;

    /**
     * コンストラクタ。
     * @param val 初期数値
     */
    public JsNumber(long val){
        this(BigDecimal.valueOf(val));
        return;
    }

    /**
     * コンストラクタ。
     * @param val 初期数値
     */
    public JsNumber(double val){
        this(BigDecimal.valueOf(val));
        return;
    }

    /**
     * コンストラクタ。
     * @param val 初期数値
     */
    public JsNumber(BigInteger val){
        this(new BigDecimal(val));
        return;
    }

    /**
     * コンストラクタ。
     * 書式はjava.math.BigDecinal#BigDecimal(String)に準ずる。
     * @param val 初期数値の文字列表記
     * @throws NumberFormatException 不正な数値表記
     */
    public JsNumber(CharSequence val) throws NumberFormatException{
        this(new BigDecimal(val.toString()));
        return;
    }

    /**
     * コンストラクタ。
     * @param val 初期数値
     * @throws NullPointerException 引数がnull
     */
    public JsNumber(BigDecimal val) throws NullPointerException{
        super();
        if(val == null) throw new NullPointerException();
        this.decimal = val;
        return;
    }

    /**
     * BigDecimal型の数値を返す。
     * @return BigDecimal型数値
     */
    public BigDecimal getBigDecimal(){
        return this.decimal;
    }

    /**
     * int型の数値を返す。
     * @return int型数値
     */
    public int intValue(){
        return this.decimal.intValue();
    }

    /**
     * long型の数値を返す。
     * @return long型数値
     */
    public long longValue(){
        return this.decimal.longValue();
    }

    /**
     * float型の数値を返す。
     * @return float型数値
     */
    public float floatValue(){
        return this.decimal.floatValue();
    }

    /**
     * double型の数値を返す。
     * @return double型数値
     */
    public double doubleValue(){
        return this.decimal.doubleValue();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        return this.decimal.hashCode();
    }

    /**
     * {@inheritDoc}
     * 「1.2」と「0.12E+1」など、スケールの一致しない値は異なる値と見なされる。
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if(this == obj) return true;
        if( ! (obj instanceof JsNumber) ) return false;
        JsNumber number = (JsNumber) obj;
        return this.decimal.equals(number.decimal);
    }

    /**
     * {@inheritDoc}
     * 「1.2」と「0.12E+1」など、スケールが異なっても値が同じであれば
     * 等しいと見なされる。
     * @param value {@inheritDoc}
     * @return {@inheritDoc}
     */
    public int compareTo(JsNumber value){
        if(this == value) return 0;
        return this.decimal.compareTo(value.decimal);
    }

    /**
     * {@inheritDoc}
     * java.math.BigDecimal#toString()に準ずる。
     * ※ JSON規格のパーサで解釈できるはず。
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        return this.decimal.toString();
    }

}
