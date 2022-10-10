/*
 * JSON object value
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: JsObject.java 914 2009-11-24 11:16:36Z olyutorskii $
 */

package jp.sourceforge.jindolf.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * JSON オブジェクト Value。
 */
public class JsObject
        extends AbstractJsValue
        implements Iterable<JsPair> {

    /**
     * JSON Objectを文字ストリームからパースする。
     * @param reader 文字入力
     * @return JSON Object。入力終了ならnull
     * @throws IOException 入力エラー
     * @throws JsParseException パースエラー
     */
    static JsObject parseObject(JsonReader reader)
            throws IOException, JsParseException{
        int chData;

        Json.skipWhiteSpace(reader);
        chData = reader.read();
        if(chData < '\u0000') return null;
        if(chData != '{') throw new JsParseException();

        JsObject result = new JsObject();

        for(;;){
            Json.skipWhiteSpace(reader);
            chData = reader.read();
            if(chData < '\u0000') throw new JsParseException();
            if(chData == '}') break;

            if(result.size() <= 0){
                reader.unread(chData);
            }else{
                if(chData != ',') throw new JsParseException();
                Json.skipWhiteSpace(reader);
            }

            JsString name = JsString.parseString(reader);
            if(name == null){
                throw new JsParseException();
            }

            Json.skipWhiteSpace(reader);
            chData = reader.read();
            if(chData < '\u0000') throw new JsParseException();
            if(chData != ':') throw new JsParseException();
            Json.skipWhiteSpace(reader);

            JsValue value = Json.parseValue(reader);
            if(value == null){
                throw new JsParseException();
            }

            result.putValue(name.toRawString(), value);
        }

        return result;
    }


    private final Map<String, JsValue> valueMap =
            new TreeMap<String, JsValue>();
    private boolean changed = false;


    /**
     * コンストラクタ。
     */
    public JsObject(){
        super();
        return;
    }

    /**
     * 名前とValueからpairを登録する。
     * @param name 名前
     * @param value Value
     * @return 旧Value。同じ内容のpairがすでに存在していたらnull
     * @throws NullPointerException 引数のいずれかがnull
     */
    public JsValue putValue(String name, JsValue value)
            throws NullPointerException{
        if(name  == null) throw new NullPointerException();
        if(value == null) throw new NullPointerException();

        JsValue oldValue = this.valueMap.get(name);
        if(value.equals(oldValue)) return null;

        JsValue old = this.valueMap.put(name, value);
        this.changed = true;
        return old;
    }

    /**
     * 名前からValueを取得する。
     * @param name 名前
     * @return 対応するValue。見つからなければnull
     */
    public JsValue getValue(String name){
        return this.valueMap.get(name);
    }

    /**
     * JSON pairを追加する。
     * @param pair JSON pair
     */
    public void putPair(JsPair pair){
        putValue(pair.getName(), pair.getValue());
        return;
    }

    /**
     * 名前からJSON pairを返す。
     * @param name 名前
     * @return JSON Pair。見つからなければnull
     */
    public JsPair getPair(String name){
        JsValue value = getValue(name);
        if(value == null) return null;

        return new JsPair(name, value);
    }

    /**
     * 空にする。
     */
    public void clear(){
        if(this.valueMap.size() > 0) this.changed = true;
        this.valueMap.clear();
        return;
    }

    /**
     * 指定した名前のpairを削除する。
     * @param name pairの名前
     * @return 消されたValue。該当するpairがなければnull
     */
    public JsValue remove(String name){
        JsValue old = this.valueMap.remove(name);
        if(old != null) this.changed = true;
        return old;
    }

    /**
     * 保持する全pairの名前の集合を返す。
     * @return すべての名前
     */
    public Set<String> nameSet(){
        return this.valueMap.keySet();
    }

    /**
     * pairのリストを返す。
     * 格納順は名前順。
     * @return pairリスト
     */
    public List<JsPair> getPairList(){
        List<JsPair> result = new ArrayList<JsPair>();

        for(String name : nameSet()){
            JsPair pair = getPair(name);
            result.add(pair);
        }

        return result;
    }

    /**
     * pairにアクセスするための反復子を提供する。
     * この反復子での削除作業はできない。
     * @return 反復子イテレータ
     */
    public Iterator<JsPair> iterator(){
        return getPairList().iterator();
    }

    /**
     * pair総数を返す。
     * @return pair総数
     */
    public int size(){
        return this.valueMap.size();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        return this.valueMap.hashCode();
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

        if( ! (obj instanceof JsObject) ) return false;
        JsObject composit = (JsObject) obj;

        return this.valueMap.equals(composit.valueMap);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        StringBuilder text = new StringBuilder();

        text.append("{");
        boolean hasElem = false;
        for(JsPair pair : this){
            if(hasElem) text.append(',');
            try{
                JsString.writeText(text, pair.getName());
            }catch(IOException e){
                assert false;
            }
            text.append(':')
                .append(pair.getValue());
            hasElem = true;
        }
        text.append("}");

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

        for(JsPair pair : this){
            String name   = pair.getName();
            JsValue value = pair.getValue();
            visitor.visitPairName(name);
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

        for(JsValue value : this.valueMap.values()){
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

        for(JsValue value : this.valueMap.values()){
            value.setUnchanged();
        }

        return;
    }

}
