/*
 * Village icon renderer for JTree
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import jp.sfjp.jindolf.ResourceManager;
import jp.sfjp.jindolf.data.Village;

/**
 * JTreeの村別アイコン表示。
 */
@SuppressWarnings("serial")
public class VillageIconRenderer extends DefaultTreeCellRenderer{

    private static final ImageIcon ICON_PROLOGUE;
    private static final ImageIcon ICON_PROGRESS;
    private static final ImageIcon ICON_EPILOGUE;
    private static final ImageIcon ICON_GAMEOVER;
    private static final ImageIcon ICON_INVALID;

    static{
        ICON_PROLOGUE =
            ResourceManager.getImageIcon("resources/image/vs_prologue.png");
        ICON_PROGRESS =
            ResourceManager.getImageIcon("resources/image/vs_progress.png");
        ICON_EPILOGUE =
            ResourceManager.getImageIcon("resources/image/vs_epilogue.png");
        ICON_GAMEOVER =
            ResourceManager.getImageIcon("resources/image/vs_gameover.png");
        ICON_INVALID =
            ResourceManager.getImageIcon("resources/image/vs_cross.png");
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
            ImageIcon icon = null;
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
