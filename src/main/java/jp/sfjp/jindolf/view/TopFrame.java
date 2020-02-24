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
import java.awt.Cursor;
import java.awt.LayoutManager;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * メインアプリウィンドウ。
 *
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
     *
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
     *
     * <p>このグラスペインは、可視化されている間、
     * キーボード入力とマウス入力を無視する。
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

    /**
     * ビジー状態の設定を行う。
     *
     * <p>ヘビーなタスク実行をアピールするために、
     * プログレスバーとカーソルの設定を行う。
     *
     * <p>ビジー中のマウス操作、キーボード入力は
     * 全てグラブされるため無視される。
     *
     * @param isBusy trueならプログレスバーのアニメ開始&amp;WAITカーソル。
     * falseなら停止&amp;通常カーソル。
     */
    public void setBusy(boolean isBusy){
        Cursor cursor;
        if(isBusy){
            cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        }else{
            cursor = Cursor.getDefaultCursor();
        }

        Component glassPane = getGlassPane();
        glassPane.setCursor(cursor);

        this.topView.setBusy(isBusy);

        glassPane.setVisible(isBusy);

        return;
    }

}
