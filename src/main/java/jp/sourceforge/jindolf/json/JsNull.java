/*
 * JSON null value
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf.json;

/**
 * JSON Null Value。
 * その実体はシングルトン
 */
public final class JsNull
        extends AbstractJsValue
        implements Comparable<JsNull> {

    /** ただ唯一のインスタンス。 */
    public static final JsNull NULL = new JsNull();

    /**
     * 隠しコンストラクタ。
     * 1回しか呼ばれないはず
     */
    private JsNull(){
        super();
        return;
    }

    /**
     * {@inheritDoc}
     * @param value {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(JsNull value){
        if(value == null) throw new NullPointerException();
        return 0;
    }

    /**
     * {@inheritDoc}
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj){
        if( ! (obj instanceof JsNull) ) return false;
        if(obj != this) return false;
        return true;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        return 7777;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        return "null";
    }

}
