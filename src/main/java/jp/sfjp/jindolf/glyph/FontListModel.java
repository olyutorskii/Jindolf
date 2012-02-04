/*
 * font list model
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.awt.EventQueue;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 * フォントファミリ名一覧表示用リストのデータモデル。
 * <p>環境によってはフォントリストを完成させるのに
 * 数千msかかる場合があるので、その対策として非同期に一覧を読み込む。
 * <p>実際のリスト作成は裏で走るタスクにより行われ、
 * リスト完成の暁にはEDTによりリスナに通知される。
 * 一般的なリストモデルと同様、
 * 基本的にスレッド間競合の問題はEDTで解決すること。
 */
@SuppressWarnings("serial")
public class FontListModel extends AbstractListModel {

    private final List<String> familyList = new LinkedList<String>();

    private volatile boolean hasDone = false;

    /**
     * コンストラクタ。
     * <p>コンストラクタ完了と同時にリスト生成タスクが裏で走り始める。
     */
    public FontListModel(){
        super();

        Runnable task = createFillTask();
        startTask(task);

        return;
    }

    /**
     * フォントリスト埋めタスクを生成する。
     * @return タスク
     */
    private Runnable createFillTask(){
        Runnable task = new Runnable(){
            /** {@inheritDoc} */
            @Override
            @SuppressWarnings("CallToThreadYield")
            public void run(){
                Thread.yield();
                fillModel();
            }
        };

        return task;
    }

    /**
     * フォントファミリ名リストを設定する。
     * @param familyNames フォントファミリ名のリスト
     * @return リストの要素数
     */
    private int fillList(List<String> familyNames){
        this.familyList.addAll(familyNames);
        this.hasDone = true;
        int size = this.familyList.size();
        return size;
    }

    /**
     * フォントリストを収集しモデルに反映させる。
     */
    private void fillModel(){
        final List<String> fontList = FontUtils.createFontList();

        // スレッド間競合を避けるため、ここより先の処理はEDT任せ。
        EventQueue.invokeLater(new Runnable(){
            /** {@inheritDoc} */
            @Override
            public void run(){
                int size = fillList(fontList);
                if(size <= 0) return;

                int begin = 0;
                int end   = size - 1;
                fireContentsChanged(this, begin, end);

                return;
            }
        });

        return;
    }

    /**
     * フォントリスト埋めタスクを起動する。
     * @param task タスク
     */
    private void startTask(Runnable task){
        Thread thread = new Thread(task);
        thread.start();
        return;
    }

    /**
     * モデルが完成済みか否か判定する。
     * @return モデルが完成していればtrue
     */
    public boolean hasCompleted(){
        return this.hasDone;
    }

    /**
     * {@inheritDoc}
     * @param index {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Object getElementAt(int index){
        Object result = this.familyList.get(index);
        return result;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getSize(){
        int result = this.familyList.size();
        return result;
    }

}
