/*
 * configuration file & directory
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import jp.sfjp.jindolf.ResourceManager;
import jp.sfjp.jindolf.VerInfo;
import jp.sfjp.jindolf.view.LockErrorPane;

/**
 * Jindolf設定格納ディレクトリに関するあれこれ。
 */
public final class ConfigDirUtils{

    private static final String TITLE_BUILDCONF =
            VerInfo.TITLE + "設定格納ディレクトリの設定";

    private static final String JINCONF     = "Jindolf";
    private static final String JINCONF_DOT = ".jindolf";
    private static final String FILE_README = "README.txt";
    private static final Charset CHARSET_README = StandardCharsets.UTF_8;

    private static final String MSG_POST =
            "<ul>"
            + "<li><code>" + CmdOption.OPT_CONFDIR + "</code>"
            + "&nbsp;オプション指定により、<br/>"
            + "任意の設定格納ディレクトリを指定することができます。<br/>"
            + "<li><code>" + CmdOption.OPT_NOCONF + "</code>"
            + "&nbsp;オプション指定により、<br/>"
            + "設定格納ディレクトリを使わずに起動することができます。<br/>"
            + "</ul>";

    private static final int ERR_ABORT = 1;


    /**
     * 隠れコンストラクタ。
     */
    private ConfigDirUtils(){
        assert false;
    }


    /**
     * VMごとアプリを異常終了させる。
     *
     * <p>終了コードは1。
     */
    private static void abort(){
        System.exit(ERR_ABORT);
        assert false;
        return;
    }

    /**
     * ダイアログを表示し、閉じられるまで待つ。
     *
     * @param pane ダイアログの元となるペイン
     */
    private static void showDialog(JOptionPane pane){
        JDialog dialog = pane.createDialog(TITLE_BUILDCONF);
        dialog.setResizable(true);
        dialog.pack();

        dialog.setVisible(true);
        dialog.dispose();

        return;
    }

    /**
     * 設定ディレクトリ操作の
     * 共通エラーメッセージ確認ダイアログを表示する。
     *
     * <p>閉じるまで待つ。
     *
     * @param txt メッセージ
     */
    private static void showErrorMessage(String txt){
        JOptionPane pane;
        pane = new JOptionPane(txt, JOptionPane.ERROR_MESSAGE);
        showDialog(pane);
        return;
    }

    /**
     * 設定ディレクトリ操作の
     * 共通エラーメッセージ確認ダイアログを表示する。
     *
     * <p>閉じるまで待つ。
     *
     * @param seq メッセージtxt*/
    private static void showWarnMessage(String txt){
        JOptionPane pane;
        pane = new JOptionPane(txt, JOptionPane.WARNING_MESSAGE);
        showDialog(pane);
        return;
    }

    /**
     * 設定ディレクトリ操作の
     * 情報提示メッセージ確認ダイアログを表示する。
     *
     * <p>閉じるまで待つ。
     *
     * @param seq メッセージtxt*/
    private static void showInfoMessage(String txt){
        JOptionPane pane;
        pane = new JOptionPane(txt, JOptionPane.INFORMATION_MESSAGE);
        showDialog(pane);
        return;
    }

    /**
     * センタリングされたファイル名表示のHTML表記を出力する。
     *
     * @param path ファイル
     * @return HTML表記
     */
    public static String getCenteredFileName(Path path){
        String form = "<center>[&nbsp;{0}&nbsp;]</center><br/>";
        String fileName = FileUtils.getHtmledFileName(path);
        String result = MessageFormat.format(form, fileName);
        return result;
    }

    /**
     * 設定ディレクトリ生成をやめた操作への警告をダイアログで提示し、
     * VM終了する。
     */
    private static void abortQuitBuildConfigDir(){
        String msg =
                "<html>"
                + "設定ディレクトリの作成をせずに起動を中止します。<br/>"
                + MSG_POST
                + "</html>";

        showWarnMessage(msg);
        abort();

        return;
    }

    /**
     * 設定ディレクトリが生成できないエラーをダイアログで提示し、
     * VM終了する。
     *
     * @param path 生成できなかったディレクトリ
     */
    private static void abortCantBuildConfigDir(Path path){
        String form =
                "<html>"
                + "設定ディレクトリ<br/>"
                + "{0}"
                + "の作成に失敗しました。"
                + "起動を中止します。<br/>"
                + MSG_POST
                + "</html>";
        String fileName = getCenteredFileName(path);
        String msg = MessageFormat.format(form, fileName);

        showErrorMessage(msg);
        abort();

        return;
    }

    /**
     * 設定ディレクトリへアクセスできないエラーをダイアログで提示し、
     * VM終了する。
     *
     * @param path アクセスできないディレクトリ
     */
    private static void abortCantAccessConfigDir(Path path){
        String form =
                "<html>"
                + "設定ディレクトリ<br/>"
                + "{0}"
                + "へのアクセスができません。"
                + "起動を中止します。<br/>"
                + "このディレクトリへのアクセス権を調整し"
                + "読み書きできるようにしてください。<br/>"
                + MSG_POST
                + "</html>";
        String fileName = getCenteredFileName(path);
        String msg = MessageFormat.format(form, fileName);

        showErrorMessage(msg);
        abort();

        return;
    }

    /**
     * ファイルに書き込めないエラーをダイアログで提示し、VM終了する。
     *
     * @param path 書き込めなかったファイル
     */
    private static void abortCantWrite(Path path){
        String form =
                "<html>"
                + "ファイル<br/>"
                + "{0}"
                + "への書き込みができません。"
                + "起動を中止します。<br/>"
                + "</html>";
        String fileName = getCenteredFileName(path);
        String msg = MessageFormat.format(form, fileName);

        showErrorMessage(msg);
        abort();

        return;
    }

    /**
     * 設定ディレクトリのルートファイルシステムもしくはドライブレターに
     * アクセスできないエラーをダイアログに提示し、VM終了する。
     *
     * @param path 設定ディレクトリ
     * @param preMessage メッセージ前半
     */
    private static void abortNoRoot(Path path, String preMessage){
        String form =
                "<html>"
                + "{0}<br/>"
                + "{1}を用意する方法が不明です。<br/>"
                + "起動を中止します。<br/>"
                + MSG_POST
                + "</html>";

        Path root = path.getRoot();
        String fileName = getCenteredFileName(root);
        String msg = MessageFormat.format(form, preMessage, fileName);

        showErrorMessage(msg);
        abort();

        return;
    }

    /**
     * 設定ディレクトリの祖先に書き込めないエラーをダイアログで提示し、
     * VM終了する。
     *
     * @param existsAncestor 存在するもっとも近い祖先
     * @param preMessage メッセージ前半
     */
    private static void abortCantWriteAncestor(Path existsAncestor,
                                               String preMessage ){
        String form =
                "<html>"
                + "{0}<br/>"
                + "{1}への書き込みができないため、"
                + "処理の続行は不可能です。<br/>"
                + "起動を中止します。<br/>"
                + MSG_POST
                + "</html>";

        String fileName = getCenteredFileName(existsAncestor);
        String msg = MessageFormat.format(form, preMessage, fileName);

        showErrorMessage(msg);
        abort();

        return;
    }

    /**
     * 設定ディレクトリがアクセス可能でなければ
     * エラーダイアログを出してVM終了する。
     *
     * @param confDir 設定ディレクトリ
     */
    public static void checkDirPerm(Path confDir){
        if( ! FileUtils.isAccessibleDirectory(confDir) ){
            abortCantAccessConfigDir(confDir);
        }

        return;
    }

    /**
     * 暗黙的な設定格納ディレクトリを返す。
     *
     * <ul>
     *
     * <li>起動元JARファイルと同じディレクトリに、
     * アクセス可能なディレクトリ"Jindolf"が
     * すでに存在していればそれを返す。
     *
     * <li>起動元JARファイルおよび"Jindolf"が発見できなければ、
     * MacOSX環境の場合"~/Library/Application Support/Jindolf/"を返す。
     * Windows環境の場合"%USERPROFILE%\Jindolf\"を返す。
     *
     * <li>それ以外の環境(Linux,etc?)の場合"~/.jindolf/"を返す。
     *
     * </ul>
     *
     * <p>返すディレクトリが存在しているか否か、
     * アクセス可能か否かは呼び出し元で判断せよ。
     *
     * @return 設定格納ディレクトリ
     */
    public static Path getImplicitConfigDirectory(){
        Path result;

        Path jarParent = FileUtils.getJarDirectory();
        if(jarParent != null && FileUtils.isAccessibleDirectory(jarParent)){
            Path confPath = jarParent.resolve(JINCONF);
            if(FileUtils.isAccessibleDirectory(confPath)){
                return confPath;
            }
        }

        Path appset = FileUtils.getAppSetDir();
        if(appset == null) return null;

        if(FileUtils.isMacOSXFs() || FileUtils.isWindowsOSFs()){
            result = appset.resolve(JINCONF);
        }else{
            result = appset.resolve(JINCONF_DOT);
        }

        return result;
    }

    /**
     * まだ存在しない設定格納ディレクトリを新規に作成する。
     *
     * <p>エラーがあればダイアログ提示とともにVM終了する。
     *
     * @param confPath 設定格納ディレクトリ
     * @param isImplicitPath ディレクトリが暗黙的に指定されたものならtrue。
     * @return 新規に作成した設定格納ディレクトリ
     * @throws IllegalArgumentException すでにそのディレクトリは存在する。
     */
    public static Path buildConfigDirectory(Path confPath,
                                            boolean isImplicitPath )
            throws IllegalArgumentException{
        if(Files.exists(confPath)) throw new IllegalArgumentException();

        Path absPath = confPath.toAbsolutePath();

        String optlead;
        if(isImplicitPath){
            optlead = "";
        }else{
            optlead =
                    "<code>"
                    + CmdOption.OPT_CONFDIR
                    + "</code>&nbsp;オプション"
                    + "で指定された、<br/>";
        }

        String fileName = getCenteredFileName(absPath);

        String form =
                "{0}設定格納ディレクトリ<br/>"
                + "{1}の作成に失敗しました。";
        String preErrMessage = MessageFormat.format(form, optlead, fileName);

        Path existsAncestor = FileUtils.findExistsAncestor(absPath);
        if(existsAncestor == null){
            abortNoRoot(absPath, preErrMessage);
        }else if( ! Files.isWritable(existsAncestor) ){
            abortCantWriteAncestor(existsAncestor, preErrMessage);
        }

        String promptForm =
                "設定ファイル格納ディレクトリ<br/>"
                + "{0}を作成します。";
        String dirName = getCenteredFileName(absPath);
        String prompt = MessageFormat.format(promptForm, dirName);
        boolean confirmed = confirmBuildConfigDir(existsAncestor, prompt);
        if( ! confirmed ){
            abortQuitBuildConfigDir();
        }

        boolean success;
        try{
            Files.createDirectories(absPath);
            success = true;
        }catch(IOException | SecurityException e){
            success = false;
        }

        if( ! success || ! Files.exists(absPath) ){
            abortCantBuildConfigDir(absPath);
        }

        // FileUtils.setOwnerOnlyAccess(absPath);

        checkDirPerm(absPath);

        touchReadme(absPath);

        return absPath;
    }

    /**
     * ローカル画像キャッシュディレクトリを作る。
     *
     * <p>作られたディレクトリ内に
     * ファイルavatarCache.jsonが作られる。
     *
     * @param imgCacheDir ローカル画像キャッシュディレクトリ
     */
    public static void buildImageCacheDir(Path imgCacheDir){
        if(Files.exists(imgCacheDir)) return;

        String jsonRes = "resources/image/avatarCache.json";
        InputStream is = ResourceManager.getResourceAsStream(jsonRes);
        if(is == null) return;

        try{
            Files.createDirectories(imgCacheDir);
        }catch(IOException e){
            // NOTHING
        }
        ConfigDirUtils.checkDirPerm(imgCacheDir);

        Path cachePath = imgCacheDir;
        Path jsonLeaf = Paths.get("avatarCache.json");
        Path path = cachePath.resolve(jsonLeaf);
        try{
            Files.copy(is, path);
        }catch(IOException e){
            abortCantAccessConfigDir(path);
        }

        return;
    }

    /**
     * 設定ディレクトリを新規に生成してよいかダイアログで問い合わせる。
     *
     * @param existsAncestor 存在するもっとも近い祖先
     * @param preMessage メッセージ前半
     * @return 生成してよいと指示があればtrue
     */
    private static boolean confirmBuildConfigDir(Path existsAncestor,
                                                 String preMessage){
        String form =
                "<html>"
                + "{0}<br/>"
                + "このディレクトリを今から<br/>"
                + "{1}に作成して構いませんか？<br/>"
                + "このディレクトリ名は、後からいつでもヘルプウィンドウで<br/>"
                + "確認することができます。"
                + "</html>";
        String fileName = getCenteredFileName(existsAncestor);
        String msg = MessageFormat.format(form, preMessage, fileName);

        JOptionPane pane;
        pane = new JOptionPane(msg,
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
     * ロックエラーダイアログの表示。
     *
     * <p>呼び出しから戻ってもまだロックオブジェクトが
     * ロックファイルのオーナーでない場合、
     * 今後設定ディレクトリは一切使わずに起動を続行するものとする。
     *
     * <p>ロックファイルの強制解除に失敗した場合はVM終了する。
     *
     * @param lock エラーを起こしたロック
     */
    public static void confirmLockError(InterVMLock lock){
        File lockFile = lock.getLockFile();
        LockErrorPane lockPane = new LockErrorPane(lockFile.toPath());
        JDialog lockDialog = lockPane.createDialog(TITLE_BUILDCONF);
        lockDialog.setResizable(true);
        lockDialog.pack();

        for(;;){
            lockDialog.setVisible(true);

            Object result = lockPane.getValue();
            boolean aborted = LockErrorPane.isAborted(result);
            boolean windowClosed = result == null;

            if(aborted || windowClosed){
                abort();
                break;
            }else if(lockPane.isRadioRetry()){
                lock.tryLock();
                if(lock.isFileOwner()) break;
            }else if(lockPane.isRadioContinue()){
                String msg =
                        "<html>"
                        + "設定ディレクトリを使わずに起動を続行します。<br/>"
                        + "今回、各種設定の読み込み・保存はできません。<br/>"
                        + "<code>"
                        + CmdOption.OPT_NOCONF
                        + "</code> オプション"
                        + "を使うとこの警告は出なくなります。"
                        + "</html>";
                showInfoMessage(msg);
                break;
            }else if(lockPane.isRadioForce()){
                lock.forceRemove();
                if(lock.isExistsFile()){
                    String form =
                            "<html>"
                            + "ロックファイルの強制解除に失敗しました。<br/>"
                            + "他に動いているJindolf"
                            + "が見つからないのであれば、<br/>"
                            + "なんとかしてロックファイル<br/>"
                            + "{0}"
                            + "を削除してください。<br/>"
                            + "起動を中止します。"
                            + "</html>";
                    String fileName = getCenteredFileName(lockFile.toPath());
                    String msg = MessageFormat.format(form, fileName);

                    showErrorMessage(msg);
                    abort();

                    break;
                }
                lock.tryLock();
                if(lock.isFileOwner()) break;

                String form =
                        "<html>"
                        + "ロックファイル<br/>"
                        + "{0}"
                        + "を確保することができません。<br/>"
                        + "起動を中止します。"
                        + "</html>";
                String fileName = getCenteredFileName(lockFile.toPath());
                String msg = MessageFormat.format(form, fileName);

                showErrorMessage(msg);
                abort();

                break;
            }
        }

        lockDialog.dispose();

        return;
    }

    /**
     * 指定されたディレクトリにREADMEファイルを生成する。
     *
     * <p>生成できなければダイアログ表示とともにVM終了する。
     *
     * @param path READMEの格納ディレクトリ
     */
    private static void touchReadme(Path path){
        Path readme = path.resolve(FILE_README);

        try{
            Files.createFile(readme);
        }catch(IOException e){
            abortCantAccessConfigDir(readme);
        }

        PrintWriter writer = null;
        try{
            OutputStream ostream = Files.newOutputStream(readme);
            Writer owriter = new OutputStreamWriter(ostream, CHARSET_README);
            writer = new PrintWriter(owriter);
            writer.println(CHARSET_README.name() + " Japanese");
            writer.println(
                    "このディレクトリは、"
                    + "Jindolfの各種設定が格納されるディレクトリです。");
            writer.println(
                    "Jindolfの詳細は "
                    + "http://jindolf.osdn.jp/"
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
        }catch(IOException | SecurityException e){
            abortCantWrite(readme);
        }finally{
            if(writer != null){
                writer.close();
            }
        }

        return;
    }

}
