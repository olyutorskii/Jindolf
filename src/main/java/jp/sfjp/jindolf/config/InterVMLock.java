/*
 * inter-VM file locking
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ロックファイルを用いたVM間ロックオブジェクト。
 *
 * <p>大昔のNFSではうまく動かないかも。
 *
 * <p>一度でもロックに成功したロックファイルは、
 * VM終了時に消されてしまうので注意。
 */
public class InterVMLock{

    /** 所持するロックオブジェクト一覧。 */
    private static final Collection<InterVMLock> OWNEDLOCKSET =
            Collections.synchronizedCollection(
                new LinkedList<InterVMLock>()
            );
    private static final AtomicBoolean SHUTDOWNGOING =
            new AtomicBoolean(false);

    static{
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(){
            @Override
            public void run(){
                shutdown();
            }
        });
    }


    private final File lockFile;
    private boolean isFileOwner = false;
    private InputStream stream = null;
    private final Object thisLock = new Object();


    /**
     * コンストラクタ。
     * この時点ではまだロックファイルの存在は確認されない。
     * @param lockFile ロックファイル
     * @throws NullPointerException 引数がnull
     */
    public InterVMLock(File lockFile) throws NullPointerException{
        if(lockFile == null) throw new NullPointerException();
        this.lockFile = lockFile;
        return;
    }


    /**
     * 所持するロックオブジェクトすべてを解放しロックファイルを削除する。
     */
    private static void shutdown(){
        if( ! SHUTDOWNGOING.compareAndSet(false, true) ) return;

        synchronized(OWNEDLOCKSET){
            for(InterVMLock lock : OWNEDLOCKSET){
                lock.releaseImpl();
            }
            OWNEDLOCKSET.clear();
        }
        return;
    }

    /**
     * シャットダウン処理進行中or完了済みか否か判定する。
     * @return 進行中or完了済みならtrue
     */
    protected static boolean isShutdownGoing(){
        boolean going = SHUTDOWNGOING.get();
        return going;
    }


    /**
     * このオブジェクトがロックファイルの作者であるか判定する。
     * @return 作者ならtrue
     */
    public boolean isFileOwner(){
        boolean result = this.isFileOwner;
        return result;
    }

    /**
     * ロックファイルがディスク上に存在するか判定する。
     * @return 存在すればtrue
     */
    public boolean isExistsFile(){
        if(this.lockFile.exists()){
            return true;
        }
        return false;
    }

    /**
     * ロック対象のファイルを返す。
     *
     * <p>勝手に作ったり消したりしないように。
     *
     * @return ロック対象ファイル
     */
    public File getLockFile(){
        return this.lockFile;
    }

    /**
     * ロックファイルのオープン中のストリームを返す。
     * ※ 排他制御目的のリソースなので、
     * 勝手に読み込んだりクローズしたりしないように。
     * @return オープン中のストリーム。オープンしてなければnull
     */
    protected InputStream getOpenedStream(){
        InputStream result = null;

        synchronized(this.thisLock){
            if(this.isFileOwner){
                result = this.stream;
            }
        }

        return result;
    }

    /**
     * ロックファイルの強制削除を試みる。
     * @return 強制削除に成功すればtrue
     */
    public boolean forceRemove(){
        synchronized(this.thisLock){
            if(this.isFileOwner) release();

            if( ! isExistsFile() ) return true;

            try{
                boolean result = this.lockFile.delete();
                if( ! result ) return false;
            }catch(SecurityException e){
                return false;
            }

            if(isExistsFile()) return false;
        }

        return true;
    }

    /**
     * ロックを試みる。
     * このメソッドは実行をブロックしない。
     * @return すでにロック済みもしくはロックに成功すればtrue
     */
    public boolean tryLock(){
        if(isShutdownGoing()) return false;

        synchronized(this.thisLock){
            if(hasLockedByMe()) return true;
            if(touchLockFile()) return true;
        }

        return false;
    }

    /**
     * 自身によるロックに成功しているか判定する。
     * @return 自身によるロック中であればtrue
     */
    public boolean hasLockedByMe(){
        boolean result;
        synchronized(this.thisLock){
            if( ! this.isFileOwner ){
                result = false;
            }else if( ! this.lockFile.exists() ){
                this.isFileOwner = false;
                result = false;
            }else{
                result = true;
            }
        }
        return result;
    }

    /**
     * ロックファイルを生成する。{@link #tryLock()}の下請け。
     * 生成されるロックファイルはVM終了時に削除されるよう登録される。
     * このメソッド実行中にVM終了が重なると、
     * ロックファイルが正しく削除されない場合がありうる。
     * @return 成功すればtrue
     */
    protected boolean touchLockFile(){
        synchronized(this.thisLock){
            boolean created = false;
            try{
                created = this.lockFile.createNewFile();
            }catch(IOException e){
                assert true;   // IGNORE
            }catch(SecurityException e){
                assert true;   // IGNORE
            }finally{
                if(created){
                    this.isFileOwner = true;
                    this.lockFile.deleteOnExit();
                }else{
                    this.isFileOwner = false;
                }
            }

            if( ! created )  return false;

            try{
                this.stream = new FileInputStream(this.lockFile);
            }catch(FileNotFoundException e){
                this.isFileOwner = false;
                this.stream = null;
                try{
                    this.lockFile.delete();
                }catch(SecurityException e2){
                    assert true; // IGNORE
                }
                return false;
            }

            synchronized(OWNEDLOCKSET){
                OWNEDLOCKSET.add(this);
            }
        }

        return true;
    }

    /**
     * ロックを解除する。
     *
     * <p>自分が作者であるロックファイルは閉じられ削除される。
     *
     * <p>削除に失敗しても無視。
     *
     * <p>シャットダウン処理進行中の場合は何もしない。
     */
    public void release(){
        if(isShutdownGoing()) return;

        releaseImpl();

        synchronized(OWNEDLOCKSET){
            OWNEDLOCKSET.remove(this);
        }

        return;
    }

    /**
     * ロックを解除する。{@link #release()}の下請け。
     *
     * <p>自分が作者であるロックファイルは閉じられ削除される。
     *
     * <p>削除に失敗しても無視。
     *
     * <p>シャットダウン処理進行中か否かは無視される。
     */
    protected void releaseImpl(){
        synchronized(this.thisLock){
            if( ! this.isFileOwner ) return;

            try{
                this.stream.close();
            }catch(IOException e){
                assert true;   // IGNORE
            }finally{
                this.stream = null;
                try{
                    this.lockFile.delete();
                }catch(SecurityException e){
                    assert true;   // IGNORE
                }finally{
                    this.isFileOwner = false;
                }
            }
        }

        return;
    }

    // TODO {@link java.nio.channels.FileChannnel}によるロック機構の併用。

}
