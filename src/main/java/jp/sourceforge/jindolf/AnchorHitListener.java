/*
 * anchor hit listener
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: AnchorHitListener.java 888 2009-11-04 06:23:35Z olyutorskii $
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
