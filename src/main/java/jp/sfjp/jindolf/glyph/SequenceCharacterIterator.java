/*
 * CharSequence CharacterIterator
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.text.CharacterIterator;

/**
 * CharSequenceをソースとするCharacterIterator。
 */
public class SequenceCharacterIterator
        implements CharacterIterator,
                   Cloneable {

    private CharSequence source;
    private final int cursorBegin;
    private final int cursorEnd;
    private final int cursorLength;
    private int cursorPos;

    /**
     * コンストラクタ。
     * @param source ソース文字列
     * @param cursorBegin カーソル開始位置
     * @param cursorEnd カーソル終了位置
     */
    public SequenceCharacterIterator(CharSequence source,
                                         int cursorBegin, int cursorEnd){
        super();

        if(cursorBegin > cursorEnd){
            throw new IllegalArgumentException();
        }
        if(cursorBegin < 0 || source.length() < cursorEnd){
            throw new IndexOutOfBoundsException();
        }

        this.source = source;
        this.cursorBegin = cursorBegin;
        this.cursorEnd   = cursorEnd;
        this.cursorLength = this.cursorEnd - this.cursorBegin;
        this.cursorPos = this.cursorBegin;

        return;
    }

    /**
     * コンストラクタ。
     * @param source ソース文字列
     */
    public SequenceCharacterIterator(CharSequence source){
        super();
        this.source = source;
        this.cursorBegin = 0;
        this.cursorEnd   = source.length();
        this.cursorLength = this.cursorEnd - this.cursorBegin;
        this.cursorPos = this.cursorBegin;
        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public char first(){
        this.cursorPos = this.cursorBegin;
        return current();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public char last(){
        this.cursorPos = this.cursorEnd - 1;
        return current();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public char current(){
        if(this.cursorLength <= 0 || this.cursorPos < this.cursorBegin){
            this.cursorPos = this.cursorBegin;
            return CharacterIterator.DONE;
        }
        if(this.cursorPos >= this.cursorEnd){
            this.cursorPos = this.cursorEnd;
            return CharacterIterator.DONE;
        }
        return this.source.charAt(this.cursorPos);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public char next(){
        this.cursorPos++;
        return current();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public char previous(){
        this.cursorPos--;
        return current();
    }

    /**
     * {@inheritDoc}
     * @param newPos {@inheritDoc}
     * @return {@inheritDoc}
     * @throws java.lang.IllegalArgumentException {@inheritDoc}
     */
    @Override
    public char setIndex(int newPos) throws IllegalArgumentException{
        if(newPos < this.cursorBegin || this.cursorEnd < newPos){
            throw new IllegalArgumentException();
        }
        this.cursorPos = newPos;
        return current();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getBeginIndex(){
        return this.cursorBegin;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getEndIndex(){
        return this.cursorEnd;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getIndex(){
        return this.cursorPos;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Object clone(){
        Object result;
        try{
            result = super.clone();
        }catch(CloneNotSupportedException e){
            assert false;
            return null;
        }

        SequenceCharacterIterator seq = (SequenceCharacterIterator) result;
        seq.source = this.source.toString();

        return seq;
    }

}
