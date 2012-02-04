/*
 * 文字列選択インタフェース
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.awt.Point;
import java.io.IOException;

/**
 * ドラッグ操作で文字列選択が可能な「何か」。
 */
public interface Selectable{

    /**
     * ドラッグ処理を行う。
     * @param fromPt ドラッグ開始位置
     * @param toPt 現在のドラッグ位置
     */
    void drag(Point fromPt, Point toPt);

    /**
     * 受け取った文字列に選択文字列を追加する。
     * @param appendable 追加対象文字列
     * @return 引数と同じインスタンス
     * @throws java.io.IOException ※ 出ないはず
     */
    Appendable appendSelected(Appendable appendable)
            throws IOException;

    /**
     * 選択範囲の解除。
     */
    void clearSelect();

}
