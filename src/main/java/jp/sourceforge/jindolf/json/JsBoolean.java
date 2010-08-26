/*
 * JSON boolean value
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf.json;

/**
 * JSON 真偽Value。
 */
public final class JsBoolean
        extends AbstractJsValue
        implements Comparable<JsBoolean> {

    /** 真。 */
    public static final JsBoolean TRUE  = new JsBoolean();
    /** 偽。 */
    public static final JsBoolean FALSE = new JsBoolean();

    /**
     * boolean値から真偽Valueを返す。
     * @param bool boolean値
     * @return TRUEかFALSE
     */
    public static JsBoolean valueOf(boolean bool){
        if(bool) return TRUE;
        return FALSE;
    }

    /**
     * コンストラクタ。
     * 2回しか呼ばれないはず。
     */
    private JsBoolean(){
        super();
        return;
    }

    /**
     * boolean値を返す。
     * @return boolean値
     */
    public boolean booleanValue(){
        if(this == TRUE) return true;
        return false;
    }

    /**
     * 真か判定する。
     * @return 真ならtrue
     */
    public boolean isTrue(){
        if(this == TRUE) return true;
        return false;
    }

    /**
     * 偽か判定する。
     * @return 偽ならtrue
     */
    public boolean isFalse(){
        if(this == FALSE) return true;
        return false;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        if(this.isTrue()) return Boolean.TRUE.hashCode();
        return Boolean.FALSE.hashCode();
    }

    /**
     * {@inheritDoc}
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if(this == obj) return true;

        if( ! (obj instanceof JsBoolean) ) return false;

        return false;
    }

    /**
     * {@inheritDoc}
     * @param value {@inheritDoc}
     * @return {@inheritDoc}
     */
    public int compareTo(JsBoolean value){
        if(value == null) throw new NullPointerException();
        if(this == value) return 0;

        if     (this.isTrue()  && value.isFalse()) return -1;
        else if(this.isFalse() && value.isTrue() ) return +1;

        return 0;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        if(this.isTrue()) return "true";
        return "false";
    }

}
