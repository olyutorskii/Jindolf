/*
 * lock file warning dialog
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.nio.file.Path;
import java.text.MessageFormat;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import jp.sfjp.jindolf.config.FileUtils;

/**
 * ロックエラー用ダイアログ。
 *
 * <ul>
 * <li>ロックの強制解除を試行
 * <li>リトライ
 * <li>設定ディレクトリを無視して続行
 * <li>起動中止
 * </ul>
 *
 * <p>の選択を利用者に求める。
 */
@SuppressWarnings("serial")
public final class LockErrorPane extends JOptionPane{

    private static final String FORM_MAIN =
            "<html>"
            + "設定ディレクトリのロックファイル<br/>"
            + "<center>[&nbsp;{0}&nbsp;]</center>"
            + "<br/>"
            + "のロックに失敗しました。<br/>"
            + "考えられる原因としては、<br/>"
            + "<ul>"
            + "<li>前回起動したJindolfの終了が正しく行われなかった"
            + "<li>今どこかで他のJindolfが動いている"
            + "</ul>"
            + "などが考えられます。<br/>"
            + "<hr/>"
            + "</html>";

    private static final String LABEL_CONTINUE =
            "設定ディレクトリを使わずに起動を続行";
    private static final String LABEL_RETRY =
            "再度ロック取得を試す";
    private static final String LABEL_FORCE =
            "<html>"
            + "ロックを強制解除<br/>"
            + " (※他のJindolfと設定ファイル書き込みが衝突するかも…)"
            + "</html>";

    private static final String LABEL_OK    = "OK";
    private static final String LABEL_ABORT = "起動中止";
    private static final String[] OPTIONS = {LABEL_OK, LABEL_ABORT};


    private final JRadioButton continueButton;
    private final JRadioButton retryButton;
    private final JRadioButton forceButton;


    /**
     * コンストラクタ。
     *
     * @param lockFile ロックに失敗したファイル
     */
    public LockErrorPane(Path lockFile){
        super();

        this.continueButton = new JRadioButton(LABEL_CONTINUE);
        this.retryButton    = new JRadioButton(LABEL_RETRY);
        this.forceButton    = new JRadioButton(LABEL_FORCE);

        ButtonGroup bgrp = new ButtonGroup();
        bgrp.add(this.continueButton);
        bgrp.add(this.retryButton);
        bgrp.add(this.forceButton);
        this.continueButton.setSelected(true);

        String lockName = FileUtils.getHtmledFileName(lockFile);
        String htmlMessage = MessageFormat.format(FORM_MAIN, lockName);

        Object[] msg = {
            htmlMessage,
            this.continueButton,
            this.retryButton,
            this.forceButton,
        };

        setMessage(msg);
        setOptions(OPTIONS);
        setMessageType(JOptionPane.ERROR_MESSAGE);

        return;
    }


    /**
     * 「起動中止」が選択されたか判定する。
     *
     * @param value ダイアログ結果
     * @return 「起動中止」が押されていたならtrue
     * @see JOptionPane#getValue()
     */
    public static boolean isAborted(Object value){
        boolean flag = LABEL_ABORT.equals(value);
        return flag;
    }


    /**
     * 「設定ディレクトリを無視して続行」が選択されたか判定する。
     *
     * @return 「無視して続行」が選択されていればtrue
     */
    public boolean isRadioContinue(){
        return this.continueButton.isSelected();
    }

    /**
     * 「リトライ」が選択されたか判定する。
     *
     * @return 「リトライ」が選択されていればtrue
     */
    public boolean isRadioRetry(){
        return this.retryButton.isSelected();
    }

    /**
     * 「強制解除」が選択されたか判定する。
     *
     * @return 「強制解除」が選択されていればtrue
     */
    public boolean isRadioForce(){
        return this.forceButton.isSelected();
    }

}
