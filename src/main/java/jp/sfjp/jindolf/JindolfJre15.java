/*
 * Jindolf main class
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;

/**
 * JRE1.5の利用が解禁されたJindolfエントリ。
 * <p>起動クラスJindolfの下請けとしての機能が想定される。
 * <p>必ずしも必要ないが、異常系切り分けに有用な、
 * 実行環境やビルドの成否に関する各種診断を行う。
 * <p>各種診断を通過した後、JindolfMainに制御を渡す。
 */
public final class JindolfJre15 {

    /** GUI環境に接続できないときの終了コード。 */
    public static final int EXIT_CODE_HEADLESS = 1;
    /** アプリのビルド工程の不備を自動検出した時の終了コード。 */
    public static final int EXIT_CODE_BUILDMISS = 1;
    /** VM内多重起動を自動検出した時の終了コード。 */
    public static final int EXIT_CODE_INVOKEDBL = 1;

    private static final PrintStream STDERR = System.err;

    private static final String ERRMSG_GUISESS =
            "ERROR : GUI session failed.";
    private static final String ERRMSG_DISPLAY =
            "{0}\nEnvironment: DISPLAY [{1}]";

    private static final String TITLE_SRCENC = "Build failed";
    private static final String ERRMSG_SRCENC =
              "ERROR : Invalid source-code encoding detected.\n"
            + "Let's check encoding with compiler & source-file.";

    private static final String TITLE_PKGERR = "ビルドエラー";
    private static final String ERRMSG_PKG =
            "パッケージ定義と{0}が一致しません。[{1}]≠[{2}]";

    private static final String TITLE_INVOKEDBL = "多重起動";
    private static final String ERRMSG_INVOKEDBL =
            "ERROR : 二度目以降の起動がキャンセルされました。";

    private static final char[][] ENCODING_TEST = {
        {'狼', 0x72fc},
        {'　', 0x3000},  // 全角スペース
        {'\\', 0x005c},  // バックスラッシュ
        {'¥',  0x00a5},  // 半角円通貨
        {'~',  0x007e},  // チルダ
        {'～', 0xff5e},  // 全角チルダ
        {'〜', 0x301c},  // 波ダッシュ
        {'�', 0xfffd},  // Unicode専用特殊文字
    };

    /** 多重起動防止用セマフォ。 */
    private static final AtomicBoolean INVOKE_FLAG =
            new AtomicBoolean(false);


    /**
     * 隠しコンストラクタ。
     */
    private JindolfJre15(){
        assert false;
    }


    /**
     * GUI環境の有無をチェックする。
     * GUI環境に接続できなければJVMを終了させる。
     * @return 成功すれば0。失敗したら0以外。
     */
    private static int proveGuiEnv(){
        if( ! GraphicsEnvironment.isHeadless() ) return 0;

        String errMsg = ERRMSG_GUISESS;

        String dispEnv;
        try{
            dispEnv = System.getenv("DISPLAY"); // for X11
        }catch(SecurityException e){
            dispEnv = null;
        }

        if(dispEnv != null){
            errMsg = MessageFormat.format(ERRMSG_DISPLAY, errMsg, dispEnv);
        }

        STDERR.println(errMsg);
        STDERR.flush();

        return EXIT_CODE_HEADLESS;
    }

    /**
     * エラーダイアログの出力。
     * @param errmsg エラーメッセージ
     * @param title タイトル
     */
    private static void showErrorDialog(String errmsg, String title){
        Frame parent = null;
        JOptionPane.showMessageDialog(parent,
                                      errmsg,
                                      title,
                                      JOptionPane.ERROR_MESSAGE);
        return;
    }

    /**
     * ビルド時のエンコーディングミスを判定する。
     * <p>※ 非Unicode系の開発環境を使いたい人は適当に無視してね。
     * @return 成功すれば0。失敗したら0以外。
     */
    private static int checkSourceEncoding(){
        boolean pass = true;
        for(char[] pair : ENCODING_TEST){
            char compiled = pair[0];
            char expected = pair[1];
            if(compiled != expected){
                pass = false;
                break;
            }
        }
        if(pass) return 0;

        String errmsg = ERRMSG_SRCENC;

        showErrorDialog(errmsg, TITLE_SRCENC);

        return EXIT_CODE_BUILDMISS;
    }

    /**
     * MANIFEST.MFパッケージ定義エラーの検出。
     * @return 成功すれば0。失敗したら0以外。
     */
    private static int checkPackageDefinition(){
        Package rootPkg = ResourceManager.DEF_ROOT_PACKAGE;
        String implTitle   = rootPkg.getImplementationTitle();
        String implVersion = rootPkg.getImplementationVersion();
        String implVendor  = rootPkg.getImplementationVendor();

        String title = TITLE_PKGERR;

        if(implTitle != null && ! VerInfo.TITLE.equals(implTitle) ){
            String errmsg = MessageFormat.format(
                    ERRMSG_PKG, "タイトル", implTitle, VerInfo.TITLE );
            showErrorDialog(errmsg, title);
            return EXIT_CODE_BUILDMISS;
        }

        if(implVersion != null && ! VerInfo.VERSION.equals(implVersion)  ){
            String errmsg = MessageFormat.format(
                    ERRMSG_PKG,
                    "バージョン番号", implVersion, VerInfo.VERSION );
            showErrorDialog(errmsg, title);
            return EXIT_CODE_BUILDMISS;
        }

        if(implVendor != null && ! VerInfo.AUTHOR.equals(implVendor) ){
            String errmsg = MessageFormat.format(
                    ERRMSG_PKG, "ベンダ", implVendor, VerInfo.AUTHOR );
            showErrorDialog(errmsg, title);
            return EXIT_CODE_BUILDMISS;
        }

        return 0;
    }

    /**
     * JVM内で多重起動していないかチェックする。
     * <p>多重起動を確認した場合は、GUIにダイアログを出力する。
     * @return 成功すれば0。失敗したら0以外。
     */
    private static int checkHasInvoked(){
        boolean successed = INVOKE_FLAG.compareAndSet(false, true);
        if(successed) return 0;

        showErrorDialog(ERRMSG_INVOKEDBL, TITLE_INVOKEDBL);

        return EXIT_CODE_INVOKEDBL;
    }

    /**
     * Jindolf のスタートアップエントリ。
     * <p>ここからJRE1.5の利用が解禁される。
     * <p>最終的に{@link JindolfMain}へ制御が渡される。
     * @param args コマンドライン引数
     * @return 起動に成功すれば0。失敗したら0以外。
     */
    public static int main(String... args){
        int exitCode;

        exitCode = proveGuiEnv();
        if(exitCode != 0) return exitCode;
        // ここから異常系でのみSwingGUI解禁

        exitCode = checkSourceEncoding();
        if(exitCode != 0) return exitCode;
        // ここから日本語を含むUnicode出力解禁。

        exitCode = checkPackageDefinition();
        if(exitCode != 0) return exitCode;

        exitCode = checkHasInvoked();
        if(exitCode != 0) return exitCode;

        exitCode = JindolfMain.main(args);

        return exitCode;
    }

}
