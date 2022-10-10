/*
 * JSON abstract value
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: AbstractJsValue.java 900 2009-11-16 15:48:22Z olyutorskii $
 */

package jp.sourceforge.jindolf.json;

/**
 * JSON 各種Value共通実装。
 * 継承必須。
 */
public class AbstractJsValue implements JsValue{

    /**
     * コンストラクタ。
     */
    protected AbstractJsValue(){
        super();
        return;
    }

    /**
     * {@inheritDoc}
     * @param visitor {@inheritDoc}
     * @throws JsVisitException {@inheritDoc}
     */
    public void traverse(ValueVisitor visitor)
            throws JsVisitException{
        visitor.visitValue(this);
        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public boolean hasChanged(){
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void setUnchanged(){
        return;
    }

}
