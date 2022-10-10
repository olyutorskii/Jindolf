/*
 * Village icon renderer for JTree
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: VillageIconRenderer.java 888 2009-11-04 06:23:35Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.Component;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

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
        URL url;
        url = Jindolf.getResource("resources/image/prologue.png");
        ICON_PROLOGUE = new ImageIcon(url);
        url = Jindolf.getResource("resources/image/progress.png");
        ICON_PROGRESS = new ImageIcon(url);
        url = Jindolf.getResource("resources/image/epilogue.png");
        ICON_EPILOGUE = new ImageIcon(url);
        url = Jindolf.getResource("resources/image/gameover.png");
        ICON_GAMEOVER = new ImageIcon(url);
        url = Jindolf.getResource("resources/image/cross.png");
        ICON_INVALID = new ImageIcon(url);
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
