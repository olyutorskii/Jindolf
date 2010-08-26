/*
 * 矩形領域テキスト描画基本インタフェース
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: TextRow.java 959 2009-12-14 14:11:01Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * 矩形に複数行のテキストを配置・描画する「なにものか」を表すインタフェース。
 * この場合、「行」とは左から右へ水平方向にグリフを並べたもの。
 */
public interface TextRow extends Selectable{

    /**
     * 新しい幅を指定し、寸法の再計算、内部の再レイアウトを促す。
     * @param newWidth 新しいピクセル幅
     * @return 新しい寸法
     */
    Rectangle setWidth(int newWidth);

    /**
     * 現在の設定で寸法の再計算、内部の再レイアウトを促す。
     * @return 新しい寸法
     */
    Rectangle recalcBounds();

    /**
     * 描画領域の寸法を返す。
     * @return 描画領域の寸法
     */
    Rectangle getBounds();

    /**
     * 描画開始位置の指定。
     * @param xPos 描画開始位置のx座標
     * @param yPos 描画開始位置のy座標
     */
    void setPos(int xPos, int yPos);

    /**
     * 描画領域の寸法幅を返す。
     * @return 描画領域の寸法幅
     */
    int getWidth();

    /**
     * 描画領域の寸法高を返す。
     * @return 描画領域の寸法高
     */
    int getHeight();

    /**
     * フォント設定を変更する。
     * @param fontInfo フォント設定
     */
    void setFontInfo(FontInfo fontInfo);

    /**
     * 描画対象か否か判定する。
     * @return 描画対象ならtrue
     */
    boolean isVisible();

    /**
     * 描画対象か否か設定する。
     * @param visible 描画対象ならtrue
     */
    void setVisible(boolean visible);

    /**
     * 描画を行う。
     * @param g グラフィックコンテキスト
     */
    void paint(Graphics2D g);

}
