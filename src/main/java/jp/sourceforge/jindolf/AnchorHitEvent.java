/*
 * anchor hit event
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: AnchorHitEvent.java 888 2009-11-04 06:23:35Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.Point;
import java.util.EventObject;

/**
 * 発言アンカーがクリックされたときのイベント。
 */
@SuppressWarnings("serial")
public class AnchorHitEvent extends EventObject{

    private final TalkDraw talkDraw;
    private final Anchor anchor;
    private final Point point;

    /**
     * コンストラクタ。
     * @param source イベント発生源
     * @param talkDraw 会話描画コンポーネント
     * @param anchor アンカー
     * @param point マウス座標
     */
    public AnchorHitEvent(Object source,
                            TalkDraw talkDraw, Anchor anchor, Point point){
        super(source);
        this.talkDraw = talkDraw;
        this.anchor = anchor;
        this.point = point;
        return;
    }

    /**
     * 会話描画コンポーネントを返す。
     * @return 会話描画コンポーネント
     */
    public TalkDraw getTalkDraw(){
        return this.talkDraw;
    }

    /**
     * アンカーを返す。
     * @return アンカー
     */
    public Anchor getAnchor(){
        return this.anchor;
    }

    /**
     * マウス座標を返す。
     * @return マウス座標
     */
    public Point getPoint(){
        return this.point;
    }

}
