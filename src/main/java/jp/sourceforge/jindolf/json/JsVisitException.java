/*
 * JSON traverse error exception
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf.json;

/**
 * トラバース中断例外。
 */
@SuppressWarnings("serial")
public class JsVisitException extends Exception{

    /**
     * コンストラクタ。
     */
    public JsVisitException(){
        super();
        return;
    }

    /**
     * コンストラクタ。
     * @param th 原因となった例外
     */
    public JsVisitException(Throwable th){
        super(th);
        return;
    }

}
