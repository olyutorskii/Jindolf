/*
 * baloon border
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * フキダシ風Border。
 */
public class BalloonBorder implements Border{

    private static final int RADIUS = 5;

    /**
     * 隙間が透明なフキダシ装飾を任意のコンポーネントに施す。
     * @param inner 装飾対象のコンポーネント
     * @return 装飾されたコンポーネント
     */
    public static JComponent decorateTransparentBorder(JComponent inner){
        JComponent result = new TransparentContainer(inner);

        Border border = new BalloonBorder();
        result.setBorder(border);

        return result;
    }

    /**
     * コンストラクタ。
     */
    public BalloonBorder(){
        super();
        return;
    }

    /**
     * {@inheritDoc}
     * @param comp {@inheritDoc}
     * @return {@inheritDoc}
     */
    public Insets getBorderInsets(Component comp){
        Insets insets = new Insets(RADIUS, RADIUS, RADIUS, RADIUS);
        return insets;
    }

    /**
     * {@inheritDoc}
     * 必ずfalseを返す(このBorderは透明)。
     * @return {@inheritDoc}
     */
    public boolean isBorderOpaque(){
        return false;
    }

    /**
     * {@inheritDoc}
     * @param comp {@inheritDoc}
     * @param g {@inheritDoc}
     * @param x {@inheritDoc}
     * @param y {@inheritDoc}
     * @param width {@inheritDoc}
     * @param height {@inheritDoc}
     */
    public void paintBorder(Component comp,
                              Graphics g,
                              int x, int y,
                              int width, int height ){
        final int diameter = RADIUS * 2;
        final int innerWidth  = width - diameter;
        final int innerHeight = height - diameter;

        Graphics2D g2d = (Graphics2D) g;

        Color bgColor = comp.getBackground();
        g2d.setColor(bgColor);

        Object antiAliaseHint =
                g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON );

        g2d.fillRect(x + RADIUS, y,
                   innerWidth, RADIUS);
        g2d.fillRect(x, y + RADIUS,
                   RADIUS, innerHeight);
        g2d.fillRect(x + RADIUS + innerWidth, y + RADIUS,
                   RADIUS, innerHeight);
        g2d.fillRect(x + RADIUS, y + RADIUS + innerHeight,
                   innerWidth, RADIUS);

        int right = 90;  // 90 degree right angle

        g2d.fillArc(x + innerWidth, y,
                  diameter, diameter, right * 0, right);
        g2d.fillArc(x, y,
                  diameter, diameter, right * 1, right);
        g2d.fillArc(x, y + innerHeight,
                  diameter, diameter, right * 2, right);
        g2d.fillArc(x + innerWidth, y + innerHeight,
                  diameter, diameter, right * 3, right);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliaseHint);

        return;
    }

    /**
     * 透明コンテナ。
     * 1つの子を持ち、背景色操作を委譲する。
     * つまりこのコンテナにBorderを設定すると子の背景色が反映される。
     */
    @SuppressWarnings("serial")
    private static class TransparentContainer extends JComponent{

        private final JComponent inner;

        /**
         * コンストラクタ。
         * @param inner 内部コンポーネント
         */
        public TransparentContainer(JComponent inner){
            super();

            this.inner = inner;

            setOpaque(false);

            LayoutManager layout = new BorderLayout();
            setLayout(layout);
            add(this.inner, BorderLayout.CENTER);

            return;
        }

        /**
         * {@inheritDoc}
         * 子の背景色を返す。
         * @return {@inheritDoc}
         */
        @Override
        public Color getBackground(){
            Color bg = this.inner.getBackground();
            return bg;
        }

        /**
         * {@inheritDoc}
         * 背景色指定をフックし、子の背景色を指定する。
         * @param bg {@inheritDoc}
         */
        @Override
        public void setBackground(Color bg){
            this.inner.setBackground(bg);
            return;
        }
    }

}
