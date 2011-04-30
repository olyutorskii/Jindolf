/*
 * JSON array value
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * JSON 配列Value。
 */
public class JsArray
        extends AbstractJsValue
        implements Iterable<JsValue> {

    private final List<JsValue> valueList = new ArrayList<JsValue>();
    private boolean changed = false;


    /**
     * コンストラクタ。
     */
    public JsArray(){
        super();
        return;
    }


    /**
     * JSON Arrayを文字ストリームからパースする。
     * @param reader 文字入力
     * @return JSON Array。入力終了ならnull
     * @throws IOException 入力エラー
     * @throws JsParseException パースエラー
     */
    static JsArray parseArray(JsonReader reader)
            throws IOException,
                   JsParseException {
        int chData;

        Json.skipWhiteSpace(reader);
        chData = reader.read();
        if(chData < '\u0000') return null;
        if(chData != '[') throw new JsParseException();

        JsArray result = new JsArray();

        for(;;){
            Json.skipWhiteSpace(reader);
            chData = reader.read();
            if(chData < '\u0000') throw new JsParseException();
            if(chData == ']') break;

            if(result.size() <= 0){
                reader.unread(chData);
            }else{
                if(chData != ',') throw new JsParseException();
                Json.skipWhiteSpace(reader);
            }

            JsValue value = Json.parseValue(reader);
            if(value == null){
                throw new JsParseException();
            }

            result.add(value);
        }

        return result;
    }


    /**
     * JSON Valueを追加する。
     * @param value JSON Value
     */
    public void add(JsValue value){
        this.valueList.add(value);
        this.changed = true;
        return;
    }

    /**
     * 指定された位置のValueを返す。
     * @param index 0で始まる位置
     * @return Value
     * @throws IndexOutOfBoundsException 不正な位置指定
     */
    public JsValue get(int index) throws IndexOutOfBoundsException{
        return this.valueList.get(index);
    }

    /**
     * 空にする。
     */
    public void clear(){
        if(this.valueList.size() > 0) this.changed = true;
        this.valueList.clear();
        return;
    }

    /**
     * JSON Valueを削除する。
     * @param value JSON Value
     * @return 既存のValueが削除されたならtrue
     */
    public boolean remove(JsValue value){
        boolean removed = this.valueList.remove(value);
        if(removed) this.changed = true;
        return removed;
    }

    /**
     * Value総数を返す。
     * @return 総数
     */
    public int size(){
        return this.valueList.size();
    }

    /**
     * Valueにアクセスするための反復子を提供する。
     * この反復子での削除作業はできない。
     * @return 反復子イテレータ
     */
    public Iterator<JsValue> iterator(){
        Collection<JsValue> unmodColl =
                Collections.unmodifiableCollection(this.valueList);
        return unmodColl.iterator();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        return this.valueList.hashCode();
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

        if( ! (obj instanceof JsArray) ) return false;
        JsArray array = (JsArray) obj;

        return this.valueList.equals(array.valueList);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        StringBuilder text = new StringBuilder();

        text.append("[");
        boolean hasElem = false;
        for(JsValue value : this.valueList){
            if(hasElem) text.append(',');
            text.append(value);
            hasElem = true;
        }
        text.append("]");

        return text.toString();
    }

    /**
     * {@inheritDoc}
     * @param visitor {@inheritDoc}
     * @throws JsVisitException {@inheritDoc}
     */
    @Override
    public void traverse(ValueVisitor visitor) throws JsVisitException{
        visitor.visitValue(this);

        for(JsValue value : this.valueList){
            value.traverse(visitor);
        }

        visitor.visitCollectionClose(this);

        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean hasChanged(){
        if(this.changed) return true;

        for(JsValue value : this.valueList){
            if(value.hasChanged()) return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUnchanged(){
        this.changed = false;

        for(JsValue value : this.valueList){
            value.setUnchanged();
        }

        return;
    }

}
