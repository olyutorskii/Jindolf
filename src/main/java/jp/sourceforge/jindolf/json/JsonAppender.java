/*
 * JSON string output
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: JsonAppender.java 919 2009-11-24 14:32:50Z olyutorskii $
 */

package jp.sourceforge.jindolf.json;

import java.io.Flushable;
import java.io.IOException;
import java.util.Stack;

/**
 * JSON文字出力用ビジター。
 * JSON Valueのトラバース時にこのビジターを指定すると、
 * 事前に用意した文字出力先にJSONフォーマットで出力される
 */
class JsonAppender
        implements ValueVisitor,
                   Flushable {

    private static final String NEWLINE = "\n";
    private static final String INDENT_UNIT = "\u0020\u0020";
    private static final String HASH_SEPARATOR = "\u0020:\u0020";
    private static final String ELEM_DELIMITOR = "\u0020,";


    private final Appendable appout;

    private final Stack<JsValue> valueStack = new Stack<JsValue>();
    private final Stack<Boolean> hasChildStack = new Stack<Boolean>();

    private boolean afterPairName = false;


    /**
     * コンストラクタ。
     * @param appout 出力先
     */
    public JsonAppender(Appendable appout){
        super();
        this.appout = appout;
        return;
    }

    /**
     * 1文字出力。
     * @param ch 文字
     * @throws IOException 出力エラー
     */
    protected void append(char ch) throws IOException{
        this.appout.append(ch);
        return;
    }

    /**
     * 文字列出力。
     * @param seq 文字列
     * @throws IOException 出力エラー
     */
    protected void append(CharSequence seq) throws IOException{
        this.appout.append(seq);
        return;
    }

    /**
     * 最後の改行を出力した後、可能であれば出力先をフラッシュする。
     * @throws IOException 出力エラー
     */
    public void flush() throws IOException{
        putNewLine();
        if(this.appout instanceof Flushable){
            ((Flushable)this.appout).flush();
        }
        return;
    }

    /**
     * 改行を出力する。
     * @throws IOException 出力エラー
     */
    protected void putNewLine()
            throws IOException{
        append(NEWLINE);
        return;
    }

    /**
     * インデントを出力する。
     * @throws IOException 出力エラー
     */
    protected void indentOut() throws IOException{
        int level = stackLength();
        for(int ct = 1; ct <= level; ct++){
            append(INDENT_UNIT);
        }
        return;
    }

    /**
     * 要素間区切りコンマを出力する。
     * JSONでは最後の要素の後にコンマを出力してはいけない。
     * @throws IOException 出力エラー
     */
    protected void putElemDelimitor()
            throws IOException{
        append(ELEM_DELIMITOR);
        return;
    }

    /**
     * pairの名前を出力する。
     * @param name pair名
     * @throws IOException 出力エラー
     */
    protected void putPairName(String name)
            throws IOException{
        JsString.writeText(this.appout, name);
        return;
    }

    /**
     * pair区切りコロンを出力する。
     * @throws IOException 出力エラー
     */
    protected void putPairSeparator()
            throws IOException{
        append(HASH_SEPARATOR);
        return;
    }

    /**
     * 一段ネストする。
     * @param value JSON Value
     * @throws IllegalArgumentException 引数がObjectでもArrayでもなかった
     */
    protected void pushValue(JsValue value)
            throws IllegalArgumentException{
        if( ! (value instanceof JsObject) && ! (value instanceof JsArray) ){
            throw new IllegalArgumentException();
        }

        this.valueStack.push(value);
        this.hasChildStack.push(false);

        return;
    }

    /**
     * ネストを一段解除する。
     * @return 最後にネストしていたValue
     */
    protected JsValue popValue(){
        this.hasChildStack.pop();
        return this.valueStack.pop();
    }

    /**
     * ネストのスタック段数を返す。
     * @return 段数
     */
    protected int stackLength(){
        return this.valueStack.size();
    }

    /**
     * ネスト後、一つでも子要素が出力されたか判定する。
     * @return 子要素が出力されていればtrue
     */
    protected boolean hasChildOut(){
        if(stackLength() <= 0) return false;
        return this.hasChildStack.peek();
    }

    /**
     * 現時点でのネストに対し、子要素が一つ以上出力済みであると設定する。
     */
    protected void setChildOut(){
        if(stackLength() <= 0) return;
        this.hasChildStack.pop();
        this.hasChildStack.push(true);
    }

    /**
     * {@inheritDoc}
     * Valueの出力を行う。
     * @param value {@inheritDoc}
     * @throws JsVisitException {@inheritDoc}
     */
    public void visitValue(JsValue value)
            throws JsVisitException{
        try{
            if( ! this.afterPairName ){
                if(hasChildOut()){
                    putElemDelimitor();
                }
                putNewLine();
                indentOut();
            }
            this.afterPairName = false;

            setChildOut();

            if(value instanceof JsObject){
                append('{');
                pushValue(value);
            }else if(value instanceof JsArray){
                append('[');
                pushValue(value);
            }else{
                append(value.toString());
            }
        }catch(IOException e){
            throw new JsVisitException(e);
        }

        return;
    }

    /**
     * {@inheritDoc}
     * pairの名前を出力する。
     * @param name {@inheritDoc}
     * @throws JsVisitException {@inheritDoc}
     */
    public void visitPairName(String name)
            throws JsVisitException{
        try{
            if(hasChildOut()){
                putElemDelimitor();
            }
            putNewLine();
            indentOut();
            putPairName(name);
            putPairSeparator();
        }catch(IOException e){
            throw new JsVisitException(e);
        }

        setChildOut();
        this.afterPairName = true;

        return;
    }

    /**
     * {@inheritDoc}
     * 閉じ括弧を出力する。
     * @param composite {@inheritDoc}
     * @throws JsVisitException {@inheritDoc}
     */
    public void visitCollectionClose(JsValue composite)
            throws JsVisitException{
        boolean hasChild = hasChildOut();

        JsValue value = popValue();

        try{
            if(hasChild){
                putNewLine();
                indentOut();
            }else{
                append('\u0020');
            }

            if(value instanceof JsObject){
                append('}');
            }else if(value instanceof JsArray){
                append(']');
            }else{
                assert false;
                throw new JsVisitException();
            }
        }catch(IOException e){
            throw new JsVisitException(e);
        }

        return;
    }

}
