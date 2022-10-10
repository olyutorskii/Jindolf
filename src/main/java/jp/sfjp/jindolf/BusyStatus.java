/*
 * busy status manager
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.sfjp.jindolf.view.TopFrame;
import jp.sfjp.jindolf.view.TopView;

/**
 * ビジー状態の見た目を管理する。
 *
 * <p>ビジー状態を伴うタスクの管理も行う。
 *
 * <p>EDTで処理しきれない長時間タスクを実行している状況、
 * およびその間のGUI操作が抑止される期間をビジーと呼ぶ。
 */
public class BusyStatus {

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    private final TopFrame topFrame;
    private final Executor executor = Executors.newCachedThreadPool();

    private boolean isBusy = false;


    /**
     * コンストラクタ。
     *
     * @param topFrame TopFrameインスタンス
     */
    public BusyStatus(TopFrame topFrame){
        super();
        this.topFrame = topFrame;
        return;
    }


    /**
     * ビジー状態か否か返す。
     *
     * @return ビジーならtrue
     */
    public boolean isBusy(){
        return this.isBusy;
    }

    /**
     * ビジー状態を設定する。
     *
     * <p>ヘビーなタスク実行をアピールするために、
     * プログレスバーとカーソルの設定を行う。
     *
     * <p>ビジー中のトップフレームのマウス操作、キーボード入力は
     * 全てグラブされるため無視される。
     *
     * @param flag trueならプログレスバーのアニメ開始&amp;WAITカーソル。
     * falseなら停止&amp;通常カーソル。
     * @param msg フッタメッセージ。nullなら変更なし。
     */
    public void setBusy(boolean flag, String msg){
        if(EventQueue.isDispatchThread()){
            setBusyEdt(flag, msg);
            return;
        }

        try{
            EventQueue.invokeAndWait(() -> {
                setBusyEdt(flag, msg);
            });
        }catch(InterruptedException | InvocationTargetException e){
            LOGGER.log(Level.SEVERE, "ビジー処理で失敗", e);
        }

        return;
    }

    /**
     * ビジー状態を設定する。
     *
     * <p>原則EDTから呼ばねばならない。
     *
     * @param flag trueならビジー
     * @param msg メッセージ。nullなら変更なし。
     */
    public void setBusyEdt(boolean flag, String msg){
        assert EventQueue.isDispatchThread();

        this.isBusy = flag;

        this.topFrame.setBusy(this.isBusy);

        if(msg != null){
            TopView topView = this.topFrame.getTopView();
            topView.updateSysMessage(msg);
        }

        return;
    }

    /**
     * 軽量タスクをEDTで実行する。
     *
     * <p>タスク実行中はビジー状態となる。
     *
     * <p>軽量タスク実行中はイベントループが停止するので、
     * 入出力待ちを伴わなずに早急に終わるタスクでなければならない。
     *
     * @param task 軽量タスク
     * @param beforeMsg ビジー中ステータス文字列
     * @param afterMsg ビジー復帰時のステータス文字列
     */
    public void submitLightBusyTask(Runnable task,
                                    String beforeMsg,
                                    String afterMsg ){
        EventQueue.invokeLater(() -> {
            setBusy(true, beforeMsg);
        });

        EventQueue.invokeLater(task);

        EventQueue.invokeLater(() -> {
            setBusy(false, afterMsg);
        });

        return;
    }

    /**
     * 重量級タスクをEDTとは別のスレッドで実行する。
     *
     * <p>タスク実行中はビジー状態となる。
     *
     * @param heavyTask 重量級タスク
     * @param beforeMsg ビジー中ステータス文字列
     * @param afterMsg ビジー復帰時のステータス文字列
     */
    public void submitHeavyBusyTask(final Runnable heavyTask,
                                    final String beforeMsg,
                                    final String afterMsg ){
        setBusy(true, beforeMsg);

        EventQueue.invokeLater(() -> {
            fork(() -> {
                try{
                    heavyTask.run();
                }finally{
                    setBusy(false, afterMsg);
                }
            });
        });

        return;
    }

    /**
     * スレッドプールを用いて非EDTなタスクを投入する。
     *
     * @param task タスク
     */
    private void fork(Runnable task){
        this.executor.execute(task);
        return;
    }

}
