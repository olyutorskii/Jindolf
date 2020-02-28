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
 * <p>各種ウィンドウシステムとの接点を管理する。
 * (ウィンドウ最小化UI、クローズUI、リサイズ操作、
 * ウィンドウタイトル、タスクバーアイコンなど)
 *
 * <p>メニューバーと{@link TopView}を自身のコンテナ上にレイアウトする。
 * アプリ画面本体の処理は{@link TopView}に委譲される。
 *
 * <p>アプリウィンドウ上のカーソル形状を管理する。
 * ヘビーな処理を行う間は砂時計アイコンになる。
 *
 * <p>glass paneの操作により、
 * ヘビーな処理中の各種アプリ操作(キーボード、マウス)をマスクする。
 *
 * <p>アプリによる各ウィンドウの親及び祖先となる。
 *
 * <p>各種モーダルダイアログの親となる。
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
     * <p>アプリウィンドウは常に透明なグラスペインに覆い尽くされている。
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
     * 実際のアプリ画面を担当する{@link TopView}を返す。
     *
     * @return トップビュー
     */
    public TopView getTopView(){
        return this.topView;
    }

    /**
     * アプリウィンドウ上のマウスカーソルのビジー状態を管理する。
     *
     * @param isBusy ビジーならtrue。
     */
    private void setCursorBusy(boolean isBusy){
        Cursor cursor;
        if(isBusy){
            cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        }else{
            cursor = Cursor.getDefaultCursor();
        }

        Component glassPane = getGlassPane();
        glassPane.setCursor(cursor);

        return;
    }

    /**
     * アプリウィンドウ上のマウス及びキー入力のグラブを管理する。
     *
     * <p>ビジー状態の場合、
     * アプリ画面上のマウス及びキー操作は全て事前にグラブされ無視される。
     *
     * @param isBusy ビジーならtrue
     */
    private void setUiMask(boolean isBusy){
        Component glassPane = getGlassPane();
        glassPane.setVisible(isBusy);
        return;
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
     * <p>プログラスバーの表示操作は{@link TopView}に委譲される。
     *
     * @param isBusy trueならプログレスバーのアニメ開始&amp;WAITカーソル。
     *     falseなら停止&amp;通常カーソル。
     */
    public void setBusy(boolean isBusy){
        setCursorBusy(isBusy);
        setUiMask(isBusy);
        this.topView.setBusy(isBusy);
        return;
    }

}
