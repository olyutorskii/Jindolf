/*
 * anchor hit listener
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.util.EventListener;

/**
 * 発言アンカーがヒットしたときのリスナ。
 */
public interface AnchorHitListener extends EventListener{
    /**
     * アンカーがクリックされたときに呼び出される。
     * @param event イベント
     */
    void anchorHitted(AnchorHitEvent event);

}
