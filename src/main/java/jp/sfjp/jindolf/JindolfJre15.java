/*
 * Jindolf main class
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf;

import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

/**
 * JRE1.5の利用が解禁されたJindolfエントリ。
 * <p>起動クラスJindolfの下請けとしての機能が想定される。
 * <p>必ずしも必要ないが、異常系切り分けに有用な、
 * 実行環境やビルドの成否に関する各種診断を行う。
 */
public final class JindolfJre15 {

    /** exit code. */
    public static final int EXIT_CODE_HEADLESS = 1;
    /** exit code. */
    public static final int EXIT_CODE_BUILDENCO = 1;
    /** exit code. */
    public static final int EXIT_CODE_INVMANIFEST = 1;


    /**
     * 隠しコンストラクタ。
     */
    private JindolfJre15(){
        super();
        assert false;
        return;
    }


    /**
     * GUI環境の有無をチェックする。
     * GUI環境に接続できなければJVMを終了させる。
     */
    private static void checkGuiEnv(){
        if( ! GraphicsEnvironment.isHeadless() ) return;

        String dispEnv;
        try{
            dispEnv = System.getenv("DISPLAY");
        }catch(SecurityException e){
            dispEnv = null;
        }

        StringBuilder message = new StringBuilder();

        message.append("ERROR : GUI session failed.");

        if(dispEnv != null){
            message.append('\n')
                   .append("Environment DISPLAY [")
                   .append(dispEnv)
                   .append("]");
        }

        System.err.println(message);
        System.err.flush();

        System.exit(EXIT_CODE_HEADLESS);

        assert false;
        return;
    }

    /**
     * エラーダイアログの出力。
     * @param errmsg エラーメッセージ
     * @param title タイトル
     */
    private static void showErrorDialog(String errmsg, String title){
        JOptionPane.showMessageDialog(null,
                                      errmsg,
                                      title,
                                      JOptionPane.ERROR_MESSAGE);
        return;
    }

    /**
     * ビルド時のエンコーディングミスを判定する。
     * ※ 非Unicode系の開発環境を使いたい人は適当に無視してね。
     */
    private static void checkBuildError(){
        if(   '狼' == 0x72fc
           && '　' == 0x3000
           && '~'  == 0x007e
           && '\\' == 0x005c  // バックスラッシュ
           && '¥'  == 0x00a5  // 半角円通貨
           && '～' == 0xff5e
           && '�' == 0xfffd  // Unicode専用特殊文字
        ){
            return;
        }

        String errmsg =
                "ERROR : Invalid source-code encoding detected.\n"
                +"Let's check encoding with compiler & source-file.";

        showErrorDialog(errmsg, "Build failed");

        System.exit(EXIT_CODE_BUILDENCO);

        assert false;
        return;
    }

    /**
     * MANIFEST.MFパッケージ定義エラーの検出。
     */
    private static void checkPackageDefinition(){
        Package rootPkg = ResourceManager.DEF_ROOT_PACKAGE;
        String implTitle   = rootPkg.getImplementationTitle();
        String implVersion = rootPkg.getImplementationVersion();
        String implVendor  = rootPkg.getImplementationVendor();

        String title = "ビルドエラー";

        if(implTitle != null && ! VerInfo.TITLE.equals(implTitle) ){
            String errmsg = "パッケージ定義とタイトルが一致しません。"
                    +"["+ implTitle +"]≠["+ VerInfo.TITLE +"]";
            showErrorDialog(errmsg, title);
            System.exit(EXIT_CODE_INVMANIFEST);
            assert false;
        }

        if(implVersion != null && ! VerInfo.VERSION.equals(implVersion)  ){
            String errmsg = "パッケージ定義とバージョン番号が一致しません。"
                    +"["+ implVersion +"]≠["+ VerInfo.VERSION +"]";
            showErrorDialog(errmsg, title);
            System.exit(EXIT_CODE_INVMANIFEST);
            assert false;
        }

        if(implVendor != null && ! VerInfo.AUTHOR.equals(implVendor) ){
            String errmsg = "パッケージ定義とベンダが一致しません。"
                    +"["+ implVendor +"]≠["+ VerInfo.AUTHOR +"]";
            showErrorDialog(errmsg, title);
            System.exit(EXIT_CODE_INVMANIFEST);
            assert false;
        }

        return;
    }

    /**
     * Jindolf のスタートアップエントリ。
     * <p>ここからJRE1.5の利用が解禁される。
     * @param args コマンドライン引数
     */
    static void main(String... args){
        checkGuiEnv();
        // ここから異常系でのSwingGUI解禁

        checkBuildError();
        // ここからUnicode出力解禁。

        checkPackageDefinition();

        JindolfGuiJp.main(args);

        return;
    }

}
