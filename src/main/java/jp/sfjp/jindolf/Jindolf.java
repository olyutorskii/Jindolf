/*
 * Jindolf main class
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf;

/**
 * Jindolf スタートアップクラス。
 * <p>JRE実行系の互換性検査を主目的とする。
 * <p>このクラスではJRE1.0互換なランタイムライブラリのみが利用できる。
 * <p>このクラスはJRE1.0でもコンパイルできなければならない。
 * アプリ開始はstaticメソッド{@link #main(String[])}呼び出しから。
 */
public final class Jindolf {

    /**
     * 隠しコンストラクタ。
     */
    private Jindolf(){
        super();
        return;
    }

    /**
     * Jindolf のスタートアップエントリ。
     * @param args コマンドライン引数
     */
    public static void main(String[] args){
        JreChecker.checkJre();

        JindolfJre15.main(args);

        return;
    }

}
