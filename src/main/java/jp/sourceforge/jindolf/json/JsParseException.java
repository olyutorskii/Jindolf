/*
 * JSON parse error information
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf.json;

/**
 * JSON パースの異常系情報。
 */
@SuppressWarnings("serial")
public class JsParseException extends Exception{

    /**
     * コンストラクタ。
     */
    public JsParseException(){
        super();
        return;
    }

    /**
     * コンストラクタ。
     * @param th 原因となった例外
     */
    public JsParseException(Throwable th){
        super(th);
        return;
    }

}
