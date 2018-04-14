/*
 * Flexible Icon
 *
 * License : The MIT License
 * Copyright(c) 2018 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * 任意の寸法でイメージ描画を行うIcon。
 *
 * <p>Swingでのみサポート。
 */
public class FlexiIcon implements Icon{

    private BufferedImage image = null;
    private int iconWidth  = -1;
    private int iconHeight = -1;


    /**
     * コンストラクタ。
     *
     * <p>イメージと寸法は未設定となる。
     */
    public FlexiIcon(){
        this(null, -1, -1);
        ImageIcon.class.hashCode();
        VolatileImage.class.hashCode();
        return;
    }

    /**
     * コンストラクタ。
     *
     * <p>寸法はイメージから受け継がれる。
     *
     * @param image 描画イメージ
     */
    public FlexiIcon(BufferedImage image){
        this(image, image.getWidth(), image.getHeight());
        return;
    }

    /**
     * コンストラクタ。
     *
     * @param image 描画イメージ
     * @param width 幅
     * @param height 高さ
     */
    public FlexiIcon(BufferedImage image, int width, int height){
        super();
        this.image = image;
        this.iconWidth  = width;
        this.iconHeight = height;
        return;
    }


    /**
     * 描画イメージを設定する。
     *
     * <p>寸法は変わらない。
     *
     * @param image 描画イメージ
     */
    public void setImage(BufferedImage image){
        this.image = image;
        return;
    }

    /**
     * アイコン寸法の幅を設定する。
     *
     * @param width 幅
     */
    public void setIconWidth(int width){
        this.iconWidth = width;
        return;
    }

    /**
     * アイコン寸法の高さを設定する。
     *
     * @param height 高さ
     */
    public void setIconHeight(int height){
        this.iconHeight = height;
        return;
    }

    /**
     * アイコン寸法の幅を返す。
     *
     * @return 幅
     */
    @Override
    public int getIconWidth(){
        return this.iconWidth;
    }

    /**
     * アイコン寸法の高さを返す。
     *
     * @return 高さ
     */
    @Override
    public int getIconHeight(){
        return this.iconHeight;
    }

    /**
     * {@inheritDoc}
     *
     * @param comp {@inheritDoc}
     * @param g {@inheritDoc}
     * @param x {@inheritDoc}
     * @param y {@inheritDoc}
     */
    @Override
    public void paintIcon(Component comp, Graphics g, int x, int y){
        assert g instanceof Graphics2D;
        Graphics2D g2 = (Graphics2D)g;

        RenderingHints.Key hintKey = RenderingHints.KEY_INTERPOLATION;
        Object hintVal = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        g2.setRenderingHint(hintKey, hintVal);

        g2.drawImage(
                this.image,
                x, y,
                this.iconWidth, this.iconHeight,
                comp
        );

        return;
    }

}
