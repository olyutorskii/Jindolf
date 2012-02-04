/*
 * Jindolf main class
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;

/**
 * GUIとUnicode出力とリソースアクセスが解禁された
 * Jindolf スタートアップクラス。
 * <p>Jindolf_jre15の下請け。
 */
public final class JindolfGuiJp {

    private static final String TITLE_INVOKEDBL = "多重起動";
    private static final String MSG_INVOKEDBL =
            "ERROR : 二度目以降の起動がキャンセルされました。";

    /** 多重起動防止用セマフォ。 */
    private static final AtomicBoolean INVOKE_FLAG =
            new AtomicBoolean(false);


    /**
     * 隠しコンストラクタ。
     */
    private JindolfGuiJp(){
        super();
        assert false;
        return;
    }

    /**
     * JVM内で多重起動していないかチェックする。
     * <p>多重起動を確認した場合は、GUIにダイアログを出力する。
     * @return 多重起動していたらtrue
     */
    private static boolean hasInvokedCheck(){
        boolean successed = INVOKE_FLAG.compareAndSet(false, true);
        if(successed) return false;

        JOptionPane.showMessageDialog(null,
                                      MSG_INVOKEDBL,
                                      TITLE_INVOKEDBL,
                                      JOptionPane.ERROR_MESSAGE);

        return true;
    }

    /**
     * Jindolf のスタートアップエントリ。
     * <p>ここからGUIとUnicode出力とリソースアクセスが解禁される。
     * @param args コマンドライン引数
     */
    static void main(String... args){
        if(hasInvokedCheck()) return;

        JindolfOld.main(args);

        return;
    }

}
