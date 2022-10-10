/*
 * Top frame
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * メインアプリウィンドウ。
 * {@link TopView}をウィンドウ表示するための皮。
 */
@SuppressWarnings("serial")
public class TopFrame extends JFrame{

    private final TopView topView = new TopView();

    /**
     * コンストラクタ。
     */
    public TopFrame(){
        super();

        Container content = getContentPane();
        design(content);

        modifyGrassPane();

        return;
    }

    /**
     * レイアウトをデザインする。
     * @param container コンテナ
     */
    private void design(Container container){
        LayoutManager layout = new BorderLayout();
        container.setLayout(layout);
        container.add(this.topView, BorderLayout.CENTER);

        return;
    }

    /**
     * グラスペインのカスタマイズを行う。
     */
    private void modifyGrassPane(){
        Component glassPane = new JComponent() {};

        glassPane.addMouseListener(new MouseAdapter() {});
        glassPane.addKeyListener(new KeyAdapter() {});

        setGlassPane(glassPane);

        return;
    }

    /**
     * トップビューを返す。
     * @return トップビュー
     */
    public TopView getTopView(){
        return this.topView;
    }

}
