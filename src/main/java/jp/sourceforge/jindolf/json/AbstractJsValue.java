/*
 * JSON abstract value
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
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
    @Override
    public void traverse(ValueVisitor visitor)
            throws JsVisitException{
        visitor.visitValue(this);
        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean hasChanged(){
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUnchanged(){
        return;
    }

}
