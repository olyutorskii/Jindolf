/*
 * Jindolf main class
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf;

/**
 * Jindolf スタートアップクラス。
 * <p>起動用のmainエントリを提供する。
 * <p>JRE実行系の互換性検査を主目的とする。
 * <p>このクラスではJRE1.0互換なランタイムライブラリのみが利用できる。
 * このクラスはJDK1.0相当のコンパイラでもコンパイルできなければならない。
 * <p>アプリ開始はstaticメソッド{@link #main(String[])}呼び出しから。
 */
public final class Jindolf {

    /**
     * 隠しコンストラクタ。
     * <p><code>assert false;</code> 書きたいけど書いちゃだめ。
     */
    private Jindolf(){
        return;
    }


    /**
     * Jindolf のスタートアップエントリ。
     * <p>互換性検査が行われた後、
     * JRE1.5解除版エントリ{@link JindolfJre15}
     * に制御を渡す。
     * @param args コマンドライン引数
     */
    public static void main(String[] args){
        int exitCode;

        exitCode = JreChecker.checkJre();
        if(exitCode != 0) System.exit(exitCode);

        exitCode = JindolfJre15.main(args);
        if(exitCode != 0) System.exit(exitCode);

        // デーモンスレッドがいなければ、(アプリ画面が出ていなければ)
        // この後暗黙にSystem.exit(0)が行われる。はず。

        return;
    }

}
