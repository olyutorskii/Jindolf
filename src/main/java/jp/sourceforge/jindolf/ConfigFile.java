/*
 * configuration file & directory
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: ConfigFile.java 928 2009-11-29 16:37:50Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import jp.sourceforge.jindolf.json.JsParseException;
import jp.sourceforge.jindolf.json.JsValue;
import jp.sourceforge.jindolf.json.Json;

/**
 * Jindolf設定格納ディレクトリに関するあれこれ。
 */
public final class ConfigFile{

    private static final String TITLE_BUILDCONF =
            Jindolf.TITLE + "設定格納ディレクトリの設定";

    private static final String JINCONF     = "Jindolf";
    private static final String JINCONF_DOT = ".jindolf";
    private static final String FILE_README = "README.txt";
    private static final Charset CHARSET_README = Charset.forName("UTF-8");
    private static final Charset CHARSET_JSON = Charset.forName("UTF-8");

    private static final String MSG_POST =
            "<ul>"
            + "<li><code>" + CmdOption.OPT_CONFDIR.toHyphened() + "</code>"
            + "&nbsp;オプション指定により、<br>"
            + "任意の設定格納ディレクトリを指定することができます。<br>"
            + "<li><code>" + CmdOption.OPT_NOCONF.toHyphened() + "</code>"
            + "&nbsp;オプション指定により、<br>"
            + "設定格納ディレクトリを使わずに起動することができます。<br>"
            + "</ul>";


    /**
     * 設定格納ディレクトリのセットアップ。
     * @return 設定格納ディレクトリ
     */
    public static File setupConfigDirectory(){
        AppSetting setting = Jindolf.getAppSetting();
        File configPath;

        if( ! setting.useConfigPath() ){
            configPath = null;
        }else{
            String optName;
            if(setting.getConfigPath() != null){
                configPath = setting.getConfigPath();
                optName = CmdOption.OPT_CONFDIR.toHyphened();
            }else{
                configPath = ConfigFile.getImplicitConfigDirectory();
                optName = null;
            }
            if( ! configPath.exists() ){
                configPath =
                        ConfigFile.buildConfigDirectory(configPath, optName);
            }
            ConfigFile.checkAccessibility(configPath);
        }

        setting.setConfigPath(configPath);

        return configPath;
    }

    /**
     * ロックファイルのセットアップ。
     * @return ロックオブジェクト
     */
    public static InterVMLock setupLockFile(){
        AppSetting setting = Jindolf.getAppSetting();

        File configPath = setting.getConfigPath();
        if(configPath == null) return null;

        File lockFile = new File(configPath, "lock");
        InterVMLock lock = new InterVMLock(lockFile);

        lock.tryLock();

        if( ! lock.isFileOwner() ){
            confirmLockError(lock);
            if( ! lock.isFileOwner() ){
                setting.setConfigPath(null);
                setting.setUseConfigPath(false);
            }
        }

        return lock;
    }

    /**
     * 暗黙的な設定格納ディレクトリを返す。
     * 起動元JARファイルと同じディレクトリに、
     * アクセス可能なディレクトリ"Jindolf"が
     * すでに存在していればそれを返す。
     * 起動元JARファイルおよび"Jindolf"が発見できなければ、
     * MacOSX環境の場合"~/Library/Application Support/Jindolf/"を返す。
     * Windows環境の場合"%USERPROFILE%\Jindolf\"を返す。
     * それ以外の環境(Linux,etc?)の場合"~/.jindolf/"を返す。
     * 返すディレクトリが存在しているか否か、
     * アクセス可能か否かは呼び出し元で判断せよ。
     * @return 設定格納ディレクトリ
     */
    public static File getImplicitConfigDirectory(){
        File result;

        File jarParent = FileUtils.getJarDirectory(Jindolf.class);
        if(jarParent != null && FileUtils.isAccessibleDirectory(jarParent)){
            result = new File(jarParent, JINCONF);
            if(FileUtils.isAccessibleDirectory(result)){
                return result;
            }
        }

        File appset = FileUtils.getAppSetDir();
        if(appset == null) return null;

        if(FileUtils.isMacOSXFs() || FileUtils.isWindowsOSFs()){
            result = new File(appset, JINCONF);
        }else{
            result = new File(appset, JINCONF_DOT);
        }

        return result;
    }

    /**
     * まだ存在しない設定格納ディレクトリを新規に作成する。
     * エラーがあればダイアログ提示とともにVM終了する。
     * @param confPath 設定格納ディレクトリ
     * @param optName 設定を指定したオプション名。
     * 暗黙的に指示されたものならnullを渡すべし。
     * @return 新規に作成した設定格納ディレクトリ
     * @throws IllegalArgumentException すでにそのディレクトリは存在する。
     */
    public static File buildConfigDirectory(File confPath,
                                               String optName)
            throws IllegalArgumentException{
        if(confPath.exists()) throw new IllegalArgumentException();

        File absPath = FileUtils.supplyFullPath(confPath);

        String preErrMessage =
                "設定格納ディレクトリ<br>"
                + getCenteredFileName(absPath)
                + "の作成に失敗しました。";
        if(optName != null){
            preErrMessage =
                    "<code>" + optName + "</code>&nbsp;オプション"
                    + "で指定された、<br>"
                    + preErrMessage;
        }

        File existsAncestor = FileUtils.findExistsAncestor(absPath);
        if(existsAncestor == null){
            abortNoRoot(absPath, preErrMessage);
        }else if( ! existsAncestor.canWrite() ){
            abortCantWriteAncestor(existsAncestor, preErrMessage);
        }

        String prompt =
                "設定ファイル格納ディレクトリ<br>"
                + getCenteredFileName(absPath)
                + "を作成します。";
        boolean confirmed = confirmBuildConfigDir(existsAncestor, prompt);
        if( ! confirmed ){
            abortQuitBuildConfigDir();
        }

        boolean success;
        try{
            success = absPath.mkdirs();
        }catch(SecurityException e){
            success = false;
        }

        if( ! success || ! absPath.exists() ){
            abortCantBuildConfigDir(absPath);
        }

        FileUtils.setOwnerOnlyAccess(absPath);

        checkAccessibility(absPath);

        touchReadme(absPath);

        return absPath;
    }

    /**
     * 設定ディレクトリ操作の
     * 共通エラーメッセージ確認ダイアログを表示する。
     * 閉じるまで待つ。
     * @param seq メッセージ
     */
    private static void showErrorMessage(CharSequence seq){
        JOptionPane pane =
                new JOptionPane(seq.toString(),
                                JOptionPane.ERROR_MESSAGE);
        showDialog(pane);
        return;
    }

    /**
     * 設定ディレクトリ操作の
     * 共通エラーメッセージ確認ダイアログを表示する。
     * 閉じるまで待つ。
     * @param seq メッセージ
     */
    private static void showWarnMessage(CharSequence seq){
        JOptionPane pane =
                new JOptionPane(seq.toString(),
                                JOptionPane.WARNING_MESSAGE);
        showDialog(pane);
        return;
    }

    /**
     * 設定ディレクトリ操作の
     * 情報提示メッセージ確認ダイアログを表示する。
     * 閉じるまで待つ。
     * @param seq メッセージ
     */
    private static void showInfoMessage(CharSequence seq){
        JOptionPane pane =
                new JOptionPane(seq.toString(),
                                JOptionPane.INFORMATION_MESSAGE);
        showDialog(pane);
        return;
    }

    /**
     * ダイアログを表示し、閉じられるまで待つ。
     * @param pane ダイアログの元となるペイン
     */
    private static void showDialog(JOptionPane pane){
        JDialog dialog = pane.createDialog(null, TITLE_BUILDCONF);
        dialog.setResizable(true);
        dialog.pack();

        dialog.setVisible(true);
        dialog.dispose();

        return;
    }

    /**
     * 設定ディレクトリのルートファイルシステムもしくはドライブレターに
     * アクセスできないエラーをダイアログに提示し、VM終了する。
     * @param path 設定ディレクトリ
     * @param preMessage メッセージ前半
     */
    private static void abortNoRoot(File path, String preMessage){
        File root = FileUtils.findRootFile(path);
        showErrorMessage(
                "<html>"
                + preMessage + "<br>"
                + getCenteredFileName(root)
                + "を用意する方法が不明です。<br>"
                + "起動を中止します。<br>"
                + MSG_POST
                + "</html>" );
        Jindolf.exit(1);
        return;
    }

    /**
     * 設定ディレクトリの祖先に書き込めないエラーをダイアログで提示し、
     * VM終了する。
     * @param existsAncestor 存在するもっとも近い祖先
     * @param preMessage メッセージ前半
     */
    private static void abortCantWriteAncestor(File existsAncestor,
                                                  String preMessage ){
        showErrorMessage(
                "<html>"
                + preMessage + "<br>"
                + getCenteredFileName(existsAncestor)
                + "への書き込みができないため、"
                + "処理の続行は不可能です。<br>"
                + "起動を中止します。<br>"
                + MSG_POST
                + "</html>" );
        Jindolf.exit(1);
        return;
    }

    /**
     * 設定ディレクトリを新規に生成してよいかダイアログで問い合わせる。
     * @param existsAncestor 存在するもっとも近い祖先
     * @param preMessage メッセージ前半
     * @return 生成してよいと指示があればtrue
     */
    private static boolean confirmBuildConfigDir(File existsAncestor,
                                                    String preMessage){
        String message =
                "<html>"
                + preMessage + "<br>"
                + "このディレクトリを今から<br>"
                + getCenteredFileName(existsAncestor)
                + "に作成して構いませんか？<br>"
                + "このディレクトリ名は、後からいつでもヘルプウィンドウで<br>"
                + "確認することができます。"
                + "</html>";

        JOptionPane pane =
                new JOptionPane(message,
                                JOptionPane.QUESTION_MESSAGE,
                                JOptionPane.YES_NO_OPTION);

        showDialog(pane);

        Object result = pane.getValue();
        if(result == null) return false;
        else if( ! (result instanceof Integer) ) return false;

        int ival = (Integer) result;
        if(ival == JOptionPane.YES_OPTION) return true;

        return false;
    }

    /**
     * 設定ディレクトリ生成をやめた操作への警告をダイアログで提示し、
     * VM終了する。
     */
    private static void abortQuitBuildConfigDir(){
        showWarnMessage(
                "<html>"
                + "設定ディレクトリの作成をせずに起動を中止します。<br>"
                + MSG_POST
                + "</html>" );
        Jindolf.exit(1);
        return;
    }

    /**
     * 設定ディレクトリが生成できないエラーをダイアログで提示し、
     * VM終了する。
     * @param path 生成できなかったディレクトリ
     */
    private static void abortCantBuildConfigDir(File path){
        showErrorMessage(
                "<html>"
                + "設定ディレクトリ<br>"
                + getCenteredFileName(path)
                + "の作成に失敗しました。"
                + "起動を中止します。<br>"
                + MSG_POST
                + "</html>" );
        Jindolf.exit(1);
        return;
    }

    /**
     * 設定ディレクトリへアクセスできないエラーをダイアログで提示し、
     * VM終了する。
     * @param path アクセスできないディレクトリ
     */
    private static void abortCantAccessConfigDir(File path){
        showErrorMessage(
                "<html>"
                + "設定ディレクトリ<br>"
                + getCenteredFileName(path)
                + "へのアクセスができません。"
                + "起動を中止します。<br>"
                + "このディレクトリへのアクセス権を調整し"
                + "読み書きできるようにしてください。<br>"
                + MSG_POST
                + "</html>" );
        Jindolf.exit(1);
        return;
    }

    /**
     * ファイルに書き込めないエラーをダイアログで提示し、VM終了する。
     * @param file 書き込めなかったファイル
     */
    private static void abortCantWrite(File file){
        showErrorMessage(
                "<html>"
                + "ファイル<br>"
                + getCenteredFileName(file)
                + "への書き込みができません。"
                + "起動を中止します。<br>"
                + "</html>" );
        Jindolf.exit(1);
        return;
    }

    /**
     * 指定されたディレクトリにREADMEファイルを生成する。
     * 生成できなければダイアログ表示とともにVM終了する。
     * @param path READMEの格納ディレクトリ
     */
    private static void touchReadme(File path){
        File file = new File(path, FILE_README);

        try{
            file.createNewFile();
        }catch(IOException e){
            abortCantAccessConfigDir(path);
        }

        PrintWriter writer = null;
        try{
            OutputStream ostream = new FileOutputStream(file);
            Writer owriter = new OutputStreamWriter(ostream, CHARSET_README);
            writer = new PrintWriter(owriter);
            writer.println(CHARSET_README.name() + " Japanese");
            writer.println(
                    "このディレクトリは、"
                    + "Jindolfの各種設定が格納されるディレクトリです。");
            writer.println(
                    "Jindolfの詳細は "
                    + "http://jindolf.sourceforge.jp/"
                    + " を参照してください。");
            writer.println(
                    "このディレクトリを"
                    + "「" + JINCONF + "」"
                    + "の名前で起動元JARファイルと"
                    + "同じ位置に");
            writer.println(
                    "コピーすれば、そちらの設定が優先して使われます。");
            writer.println(
                    "「lock」の名前を持つファイルはロックファイルです。");
        }catch(IOException e){
            abortCantWrite(file);
        }catch(SecurityException e){
            abortCantWrite(file);
        }finally{
            if(writer != null){
                writer.close();
            }
        }

        return;
    }

    /**
     * 設定ディレクトリがアクセス可能でなければ
     * エラーダイアログを出してVM終了する。
     * @param confDir 設定ディレクトリ
     */
    public static void checkAccessibility(File confDir){
        if( ! FileUtils.isAccessibleDirectory(confDir) ){
            abortCantAccessConfigDir(confDir);
        }

        return;
    }

    /**
     * センタリングされたファイル名表示のHTML表記を出力する。
     * @param path ファイル
     * @return HTML表記
     */
    public static String getCenteredFileName(File path){
        return "<center>[&nbsp;"
                + FileUtils.getHtmledFileName(path)
                + "&nbsp;]</center>"
                + "<br>";
    }

    /**
     * 隠れコンストラクタ。
     */
    private ConfigFile(){
        super();
        return;
    }

    /**
     * ロックエラーダイアログの表示。
     * 呼び出しから戻ってもまだロックオブジェクトが
     * ロックファイルのオーナーでない場合、
     * 今後設定ディレクトリは一切使わずに起動を続行するものとする。
     * ロックファイルの強制解除に失敗した場合はVM終了する。
     * @param lock エラーを起こしたロック
     */
    public static void confirmLockError(InterVMLock lock){
        LockErrorPane pane = new LockErrorPane(lock);
        JDialog dialog = pane.createDialog(null, TITLE_BUILDCONF);
        dialog.setResizable(true);
        dialog.pack();

        for(;;){
            dialog.setVisible(true);
            dialog.dispose();

            if(pane.isAborted() || pane.getValue() == null){
                Jindolf.exit(1);
                break;
            }else if(pane.isRadioRetry()){
                lock.tryLock();
                if(lock.isFileOwner()) break;
            }else if(pane.isRadioContinue()){
                showInfoMessage(
                        "<html>"
                        + "設定ディレクトリを使わずに起動を続行します。<br>"
                        + "今回、各種設定の読み込み・保存はできません。<br>"
                        + "<code>"
                        + CmdOption.OPT_NOCONF.toHyphened()
                        + "</code> オプション"
                        + "を使うとこの警告は出なくなります。"
                        + "</html>");
                break;
            }else if(pane.isRadioForce()){
                lock.forceRemove();
                if(lock.isExistsFile()){
                    showErrorMessage(
                            "<html>"
                            + "ロックファイルの強制解除に失敗しました。<br>"
                            + "他に動いているJindolf"
                            + "が見つからないのであれば、<br>"
                            + "なんとかしてロックファイル<br>"
                            + getCenteredFileName(lock.getLockFile())
                            + "を削除してください。<br>"
                            + "起動を中止します。"
                            + "</html>");
                    Jindolf.exit(1);
                    break;
                }
                lock.tryLock();
                if(lock.isFileOwner()) break;
                showErrorMessage(
                        "<html>"
                        + "ロックファイル<br>"
                        + getCenteredFileName(lock.getLockFile())
                        + "を確保することができません。<br>"
                        + "起動を中止します。"
                        + "</html>");
                Jindolf.exit(1);
                break;
            }
        }

        return;
    }

    /**
     * 設定ディレクトリ上のJSONファイルを読み込む。
     * @param file JSONファイルの相対パス
     * @return JSON objectまたはarray。
     * 設定ディレクトリを使わない設定、
     * もしくはJSONファイルが存在しない、
     * もしくは入力エラーがあればnull
     */
    public static JsValue loadJson(File file){
        AppSetting setting = Jindolf.getAppSetting();
        if( ! setting.useConfigPath() ) return null;

        File absFile;
        if(file.isAbsolute()){
            absFile = file;
        }else{
            File configPath = setting.getConfigPath();
            if(configPath == null) return null;
            absFile = new File(configPath, file.getPath());
            if( ! absFile.exists() ) return null;
            if( ! absFile.isAbsolute() ) return null;
        }

        InputStream istream;
        try{
            istream = new FileInputStream(absFile);
        }catch(FileNotFoundException e){
            assert false;
            return null;
        }
        istream = new BufferedInputStream(istream);

        Reader reader = new InputStreamReader(istream, CHARSET_JSON);

        JsValue value;
        try{
            value = Json.parseValue(reader);
        }catch(IOException e){
            Jindolf.logger().fatal(
                    "JSONファイル["
                    + absFile.getPath()
                    + "]の読み込み時に支障がありました。", e);
            return null;
        }catch(JsParseException e){
            Jindolf.logger().fatal(
                    "JSONファイル["
                    + absFile.getPath()
                    + "]の内容に不備があります。", e);
            return null;
        }finally{
            try{
                reader.close();
            }catch(IOException e){
                Jindolf.logger().fatal(
                        "JSONファイル["
                        + absFile.getPath()
                        + "]を閉じることができません。", e);
                return null;
            }
        }

        return value;
    }

    /**
     * 設定ディレクトリ上のJSONファイルに書き込む。
     * @param file JSONファイルの相対パス
     * @param value JSON objectまたはarray
     * @return 正しくセーブが行われればtrue。
     * 何らかの理由でセーブが完了できなければfalse
     */
    public static boolean saveJson(File file, JsValue value){
        AppSetting setting = Jindolf.getAppSetting();
        if( ! setting.useConfigPath() ) return false;
        File configPath = setting.getConfigPath();
        if(configPath == null) return false;

        // TODO テンポラリファイルを用いたより安全なファイル更新
        File absFile = new File(configPath, file.getPath());
        absFile.delete();
        try{
            if(absFile.createNewFile() != true) return false;
        }catch(IOException e){
            Jindolf.logger().fatal(
                    "JSONファイル["
                    + absFile.getPath()
                    + "]の新規生成ができません。", e);
            return false;
        }

        OutputStream ostream;
        try{
            ostream = new FileOutputStream(absFile);
        }catch(FileNotFoundException e){
            assert false;
            return false;
        }
        ostream = new BufferedOutputStream(ostream);
        Writer writer = new OutputStreamWriter(ostream, CHARSET_JSON);

        try{
            Json.writeJsonTop(writer, value);
        }catch(IOException e){
            Jindolf.logger().fatal(
                    "JSONファイル["
                    + absFile.getPath()
                    + "]の書き込み時に支障がありました。", e);
            return false;
        }finally{
            try{
                writer.close();
            }catch(IOException e){
                Jindolf.logger().fatal(
                        "JSONファイル["
                        + absFile.getPath()
                        + "]を閉じることができません。", e);
                return false;
            }
        }

        return true;
    }

    /**
     * ロックエラー用ダイアログ。
     * <ul>
     * <li>強制解除
     * <li>リトライ
     * <li>設定ディレクトリを無視
     * <li>起動中止
     * </ul>
     * の選択を利用者に求める。
     */
    @SuppressWarnings("serial")
    private static class LockErrorPane
            extends JOptionPane
            implements ActionListener{

        private final InterVMLock lock;

        private final JRadioButton continueButton =
                new JRadioButton("設定ディレクトリを使わずに起動を続行");
        private final JRadioButton retryButton =
                new JRadioButton("再度ロック取得を試す");
        private final JRadioButton forceButton =
                new JRadioButton(
                "<html>"
                + "ロックを強制解除<br>"
                + " (※他のJindolfと設定ファイル書き込みが衝突するかも…)"
                + "</html>");

        private final JButton okButton = new JButton("OK");
        private final JButton abortButton = new JButton("起動中止");

        private boolean aborted = false;

        /**
         * コンストラクタ。
         * @param lock 失敗したロック
         */
        public LockErrorPane(InterVMLock lock){
            super();

            this.lock = lock;

            String htmlMessage =
                    "<html>"
                    + "設定ディレクトリのロックファイル<br>"
                    + getCenteredFileName(this.lock.getLockFile())
                    + "のロックに失敗しました。<br>"
                    + "考えられる原因としては、<br>"
                    + "<ul>"
                    + "<li>前回起動したJindolfの終了が正しく行われなかった"
                    + "<li>今どこかで他のJindolfが動いている"
                    + "</ul>"
                    + "などが考えられます。<br>"
                    + "<hr>"
                    + "</html>";

            ButtonGroup bgrp = new ButtonGroup();
            bgrp.add(this.continueButton);
            bgrp.add(this.retryButton);
            bgrp.add(this.forceButton);
            this.continueButton.setSelected(true);

            Object[] msg = {
                htmlMessage,
                this.continueButton,
                this.retryButton,
                this.forceButton,
            };
            setMessage(msg);

            Object[] opts = {
                this.okButton,
                this.abortButton,
            };
            setOptions(opts);

            setMessageType(JOptionPane.ERROR_MESSAGE);

            this.okButton   .addActionListener(this);
            this.abortButton.addActionListener(this);

            return;
        }

        /**
         * 「設定ディレクトリを無視して続行」が選択されたか判定する。
         * @return 「無視して続行」が選択されていればtrue
         */
        public boolean isRadioContinue(){
            return this.continueButton.isSelected();
        }

        /**
         * 「リトライ」が選択されたか判定する。
         * @return 「リトライ」が選択されていればtrue
         */
        public boolean isRadioRetry(){
            return this.retryButton.isSelected();
        }

        /**
         * 「強制解除」が選択されたか判定する。
         * @return 「強制解除」が選択されていればtrue
         */
        public boolean isRadioForce(){
            return this.forceButton.isSelected();
        }

        /**
         * 「起動中止」が選択されたか判定する。
         * @return 「起動中止」が押されていたならtrue
         */
        public boolean isAborted(){
            return this.aborted;
        }

        /**
         * {@inheritDoc}
         * @param parentComponent {@inheritDoc}
         * @param title {@inheritDoc}
         * @return {@inheritDoc}
         * @throws HeadlessException {@inheritDoc}
         */
        @Override
        public JDialog createDialog(Component parentComponent,
                                      String title)
                throws HeadlessException{
            final JDialog dialog =
                    super.createDialog(parentComponent, title);

            ActionListener listener = new ActionListener(){
                public void actionPerformed(ActionEvent event){
                    dialog.setVisible(false);
                    return;
                }
            };

            this.okButton   .addActionListener(listener);
            this.abortButton.addActionListener(listener);

            return dialog;
        }

        /**
         * ボタン押下を受信する。
         * @param event イベント
         */
        public void actionPerformed(ActionEvent event){
            Object source = event.getSource();
            if(source == this.okButton) this.aborted = false;
            else                        this.aborted = true;
            return;
        }

    }

}
