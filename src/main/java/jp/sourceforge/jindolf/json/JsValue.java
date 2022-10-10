/*
 * JSON value common interface
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: JsValue.java 899 2009-11-16 15:13:52Z olyutorskii $
 */

package jp.sourceforge.jindolf.json;

/**
 * JSON 各種Value共通インタフェース。
 */
public interface JsValue{

    /**
     * 深さ優先探索を行い各種構造の出現をビジターに通知する。
     * @param visitor ビジター
     * @throws JsVisitException トラバース中断。
     */
    void traverse(ValueVisitor visitor) throws JsVisitException;

    /**
     * このValueおよび子孫に変更があったか判定する。
     * @return 変更があればtrue
     */
    boolean hasChanged();

    /**
     * このValueおよび子孫に変更がなかったことにする。
     */
    void setUnchanged();

}