/*
 * font previewer
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 * フォントプレビュー用コンポーネント。
 *
 * <p>発言表示部と同じビジュアルを再現する必要のため、GlyphDrawで描画する。
 */
@SuppressWarnings("serial")
public class FontPreviewer extends JComponent {

    private static final int MARGIN = 5;

    private final GlyphDraw draw;
    private FontInfo fontInfo;


    /**
     * コンストラクタ。
     * @param source 文字列
     * @param fontInfo フォント設定
     */
    public FontPreviewer(CharSequence source,
                         FontInfo fontInfo ){
        super();

        this.fontInfo = fontInfo;
        this.draw = new GlyphDraw(source, this.fontInfo);
        this.draw.setFontInfo(this.fontInfo);

        this.draw.setPos(MARGIN, MARGIN);

        this.draw.setColor(Color.BLACK);
        setBackground(Color.WHITE);

        updateBounds();

        return;
    }

    /**
     * サイズ更新。
     */
    private void updateBounds(){
        Rectangle bounds = this.draw.setWidth(Integer.MAX_VALUE);
        Dimension dimension = new Dimension(bounds.width  + MARGIN * 2,
                                            bounds.height + MARGIN * 2 );

        setPreferredSize(dimension);
        revalidate();
        repaint();

        return;
    }

    /**
     * フォント設定の変更。
     * @param newFontInfo フォント設定
     */
    public void setFontInfo(FontInfo newFontInfo){
        this.fontInfo = newFontInfo;
        this.draw.setFontInfo(this.fontInfo);

        updateBounds();

        return;
    }

    /**
     * {@inheritDoc}
     * 文字列の描画。
     * @param g {@inheritDoc}
     */
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        this.draw.paint(g2d);
        return;
    }

}
