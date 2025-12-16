/*
 * Village icon renderer for JTree
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import jp.sfjp.jindolf.ResourceManager;
import jp.sfjp.jindolf.data.Village;

/**
 * JTreeの村別アイコン表示。
 */
@SuppressWarnings("serial")
public class VillageIconRenderer extends DefaultTreeCellRenderer{

    private static final Icon ICON_PROLOGUE;
    private static final Icon ICON_PROGRESS;
    private static final Icon ICON_EPILOGUE;
    private static final Icon ICON_GAMEOVER;
    private static final Icon ICON_INVALID;

    private static final int MARK_SZ = 16;

    static{
        ICON_PROLOGUE = ResourceManager
                .getSquareIcon("resources/image/vs_prologue.png", MARK_SZ);
        ICON_PROGRESS = ResourceManager
                .getSquareIcon("resources/image/vs_progress.png", MARK_SZ);
        ICON_EPILOGUE = ResourceManager
                .getSquareIcon("resources/image/vs_epilogue.png", MARK_SZ);
        ICON_GAMEOVER = ResourceManager
                .getSquareIcon("resources/image/vs_gameover.png", MARK_SZ);
        ICON_INVALID = ResourceManager
                .getSquareIcon("resources/image/vs_cross.png", MARK_SZ);
    }

    /**
     * コンストラクタ。
     */
    public VillageIconRenderer(){
        super();
        return;
    }

    /**
     * {@inheritDoc}
     * 村種別によってツリーリストアイコンを書き分ける。
     *
     * @param tree {@inheritDoc}
     * @param value {@inheritDoc}
     * @param sel {@inheritDoc}
     * @param expanded {@inheritDoc}
     * @param leaf {@inheritDoc}
     * @param row {@inheritDoc}
     * @param hasFocus {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus ){
        if(leaf && value instanceof Village){
            Village village = (Village) value;
            Icon icon = null;
            switch(village.getState()){
            case PROLOGUE: icon = ICON_PROLOGUE; break;
            case PROGRESS: icon = ICON_PROGRESS; break;
            case EPILOGUE: icon = ICON_EPILOGUE; break;
            case GAMEOVER: icon = ICON_GAMEOVER; break;
            default: assert false; break;
            }
            if( ! village.isValid()) icon = ICON_INVALID;
            setLeafIcon(icon);
        }

        Component comp =
                super
                .getTreeCellRendererComponent(
                    tree,
                    value,
                    sel,
                    expanded,
                    leaf,
                    row,
                    hasFocus
                );

        return comp;
    }

}
