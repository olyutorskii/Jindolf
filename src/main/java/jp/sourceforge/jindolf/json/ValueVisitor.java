/*
 * JSON value visitor
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: ValueVisitor.java 900 2009-11-16 15:48:22Z olyutorskii $
 */

package jp.sourceforge.jindolf.json;

/**
 * Valueへのビジター共通インタフェース。
 */
public interface ValueVisitor{

    /**
     * Value登場の通知を受け取る。
     * @param value JSON Value
     * @throws JsVisitException トラバース中止
     */
    void visitValue(JsValue value) throws JsVisitException;

    /**
     * pair名登場の通知を受け取る。
     * @param name pair名
     * @throws JsVisitException トラバース中止
     */
    void visitPairName(String name) throws JsVisitException;

    /**
     * 括弧終了の通知を受け取る。
     * @param composite JSON Object か JSON Array
     * @throws JsVisitException トラバース中止
     */
    void visitCollectionClose(JsValue composite) throws JsVisitException;

}
