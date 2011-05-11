/*
 * JSON pair in object
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf.json;

import java.io.IOException;

/**
 * JSON オブジェクトValue内に列挙される、名前の付いたValueとの組。
 * 後での変更は不可能。
 */
public class JsPair{

    private final String name;
    private final JsValue value;

    /**
     * コンストラクタ。
     * @param name 名前
     * @param value JSON Value
     * @throws NullPointerException 名前もしくはValueがnull
     */
    public JsPair(String name, JsValue value)
            throws NullPointerException{
        super();

        if(name  == null || value == null) throw new NullPointerException();

        this.name = name;
        this.value = value;

        return;
    }

    /**
     * コンストラクタ。
     * @param name 名前
     * @param text 文字列
     * @throws NullPointerException 名前がnull
     */
    public JsPair(String name, CharSequence text)
            throws NullPointerException{
        this(name, (JsValue) new JsString(text) );
        return;
    }

    /**
     * コンストラクタ。
     * @param name 名前
     * @param bool 真偽
     * @throws NullPointerException 名前がnull
     */
    public JsPair(String name, boolean bool)
            throws NullPointerException{
        this(name, JsBoolean.valueOf(bool));
        return;
    }

    /**
     * コンストラクタ。
     * @param name 名前
     * @param number 数値
     * @throws NullPointerException 名前がnull
     */
    public JsPair(String name, long number)
            throws NullPointerException{
        this(name, new JsNumber(number));
        return;
    }

    /**
     * コンストラクタ。
     * @param name 名前
     * @param number 数値
     * @throws NullPointerException 名前がnull
     */
    public JsPair(String name, double number)
            throws NullPointerException{
        this(name, new JsNumber(number));
        return;
    }

    /**
     * 名前を返す。
     * @return 名前
     */
    public String getName(){
        return this.name;
    }

    /**
     * JSON Valueを返す。
     * @return JSON Value
     */
    public JsValue getValue(){
        return this.value;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        StringBuilder text = new StringBuilder();

        try{
            JsString.writeText(text, this.name);
        }catch(IOException e){
            assert false; // NEVER!
        }

        text.append(':')
            .append(this.value);

        return text.toString();
    }

}
