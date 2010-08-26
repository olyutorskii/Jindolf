/*
 * JSON parse error information
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: JsParseException.java 900 2009-11-16 15:48:22Z olyutorskii $
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
