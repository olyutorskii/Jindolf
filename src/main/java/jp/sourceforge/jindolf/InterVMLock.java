/*
 * inter-VM file locking
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ロックファイルを用いたVM間ロックオブジェクト。
 * 大昔のNFSではうまく動かないかも。
 * 一度でもロックに成功したロックファイルはVM終了時に消されてしまうので注意。
 */
public class InterVMLock{

    /** 所持するロックオブジェクト一覧。 */
    private static final Set<InterVMLock> ownedLockSet =
            Collections.synchronizedSet(new HashSet<InterVMLock>());

    static{
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(){
            @Override public void run(){ clearOwnedLockSet(); }
        });
    }


    private final File lockFile;
    private boolean isFileOwner = false;
    private FileOutputStream stream = null;


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
     * 所持するロックオブジェクト一覧への登録。
     * @param lock 登録するロックオブジェクト
     */
    private static void addOwnedLock(InterVMLock lock){
        synchronized(ownedLockSet){
            if( ! lock.isFileOwner() ) return;
            ownedLockSet.add(lock);
            lock.getLockFile().deleteOnExit();
        }
        return;
    }

    /**
     * 所持するロックオブジェクト一覧からの脱退。
     * @param lock 脱退対象のロックオブジェクト
     */
    private static void removeOwnedLock(InterVMLock lock){
        synchronized(ownedLockSet){
            ownedLockSet.remove(lock);
        }
        return;
    }

    /**
     * 所持するロックオブジェクトすべてを解放しロックファイルを削除する。
     */
    private static void clearOwnedLockSet(){
        synchronized(ownedLockSet){
            for(InterVMLock lock : ownedLockSet){
                if( ! lock.isFileOwner() ) continue;
                lock.release();
                try{
                    lock.getLockFile().delete();
                }catch(SecurityException e){
                    // NOTHING
                }
            }
            ownedLockSet.clear();
        }
        return;
    }

    /**
     * ロック対象のファイルを返す。
     * @return ロック対象ファイル
     */
    public File getLockFile(){
        return this.lockFile;
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
     * このオブジェクトがロックファイルの作者であるか判定する。
     * @return 作者ならtrue
     */
    public synchronized boolean isFileOwner(){
        return this.isFileOwner;
    }

    /**
     * ロックファイルのオープン中のストリームを返す。
     * ※ 排他制御目的のリソースなので、
     * 勝手に書き込んだりクローズしたりしないように。
     * @return オープン中のストリーム。オープンしてなければnull
     */
    protected synchronized FileOutputStream getOpenedStream(){
        if(isFileOwner()) return this.stream;
        return null;
    }

    /**
     * ロックファイルの強制削除を試みる。
     * @return 強制削除に成功すればtrue
     */
    public synchronized boolean forceRemove(){
        if(isFileOwner()) release();

        if( ! isExistsFile() ) return true;

        try{
            boolean result = this.lockFile.delete();
            if( ! result ) return false;
        }catch(SecurityException e){
            return false;
        }

        if(isExistsFile()) return false;

        return true;
    }

    /**
     * ロックファイルを生成する。
     * 生成されるロックファイルはVM終了時に削除されるよう登録される。
     * このメソッド実行中にVM終了が重なると、
     * ロックファイルが正しく削除されない場合がありうる。
     * @return 成功すればtrue
     */
    protected synchronized boolean touchLockFile(){
        boolean result = false;
        try{
            result = this.lockFile.createNewFile();
        }catch(IOException e){
            // NOTHING
        }catch(SecurityException e){
            // NOTHING
        }
        if(result == false){
            return false;
        }

        try{
            this.isFileOwner = true;
            this.stream = new FileOutputStream(this.lockFile);
        }catch(FileNotFoundException e){
            assert false;
            this.isFileOwner = false;
            this.stream = null;
            try{
                this.lockFile.delete();
            }catch(SecurityException e2){
                // NOTHING
            }
            return false;
        }

        addOwnedLock(this);

        return true;
    }

    /**
     * ロックを試みる。
     * このメソッドはブロックしない。
     * @return すでにロック済みもしくはロックに成功すればtrue
     */
    public synchronized boolean tryLock(){
        if( isFileOwner() ) return true;

        if(isExistsFile()) return false;
        if(touchLockFile() != true) return false;

        return true;
    }

    /**
     * ロックを解除する。
     * 自分が作者であるロックファイルは閉じられ削除される。
     * 削除に失敗しても無視。
     */
    public synchronized void release(){
        if( ! isFileOwner() ) return;

        try{
            this.stream.close();
        }catch(IOException e){
            // NOTHING
        }finally{
            this.stream = null;
            try{
                this.lockFile.delete();
            }catch(SecurityException e){
                // NOTHING
            }finally{
                removeOwnedLock(this);
                this.isFileOwner = false;
            }
        }

        return;
    }

}
