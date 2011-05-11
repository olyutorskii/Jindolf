/*
 * 矩形領域テキスト描画抽象クラス
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.text.CharacterIterator;

/**
 * TextRowの実装を助けるクラス。
 */
public abstract class AbstractTextRow implements TextRow{

    /** 描画領域矩形。 */
    protected final Rectangle bounds = new Rectangle();
    /** フォント指定。 */
    protected FontInfo fontInfo;

    private boolean visible = true;

    /**
     * コンストラクタ。
     */
    protected AbstractTextRow(){
        this(FontInfo.DEFAULT_FONTINFO);
        return;
    }

    /**
     * コンストラクタ。
     * @param fontInfo フォント設定
     */
    protected AbstractTextRow(FontInfo fontInfo){
        this.fontInfo = fontInfo;
        return;
    }

    /**
     * {@inheritDoc}
     * @param newWidth {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Rectangle setWidth(int newWidth){
        this.bounds.width = newWidth;
        recalcBounds();
        return this.bounds;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Rectangle getBounds(){
        return this.bounds;
    }

    /**
     * {@inheritDoc}
     * @param xPos {@inheritDoc}
     * @param yPos {@inheritDoc}
     */
    @Override
    public void setPos(int xPos, int yPos){
        this.bounds.x = xPos;
        this.bounds.y = yPos;
        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getWidth(){
        return this.bounds.width;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getHeight(){
        return this.bounds.height;
    }

    /**
     * {@inheritDoc}
     * @param fontInfo {@inheritDoc}
     */
    @Override
    public void setFontInfo(FontInfo fontInfo){
        this.fontInfo = fontInfo;
        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isVisible(){
        return this.visible;
    }

    /**
     * {@inheritDoc}
     * @param visible {@inheritDoc}
     */
    @Override
    public void setVisible(boolean visible){
        this.visible = visible;
        return;
    }

    /**
     * 文字列からグリフ集合を生成する。
     * @param iterator 文字列
     * @return グリフ集合
     */
    public GlyphVector createGlyphVector(CharacterIterator iterator){
        return this.fontInfo.createGlyphVector(iterator);
    }

    /**
     * 文字列からグリフ集合を生成する。
     * @param seq 文字列
     * @return グリフ集合
     */
    public GlyphVector createGlyphVector(CharSequence seq){
        CharacterIterator iterator;
        iterator = new SequenceCharacterIterator(seq);
        return this.fontInfo.createGlyphVector(iterator);
    }

    /**
     * 文字列からグリフ集合を生成する。
     * @param seq 文字列
     * @param from 開始位置
     * @param to 終了位置
     * @return グリフ集合
     */
    public GlyphVector createGlyphVector(CharSequence seq,
                                           int from, int to ){
        CharacterIterator iterator;
        iterator = new SequenceCharacterIterator(seq, from, to);
        return this.fontInfo.createGlyphVector(iterator);
    }

}
