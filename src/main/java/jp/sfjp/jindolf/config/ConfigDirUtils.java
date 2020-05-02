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

        String preErrMessage =
                "設定格納ディレクトリ<br/>"
                + getCenteredFileName(absPath)
                + "の作成に失敗しました。";
        if( ! isImplicitPath ){
            preErrMessage =
                    "<code>"
                    + CmdOption.OPT_CONFDIR
                    + "</code>&nbsp;オプション"
                    + "で指定された、<br/>"
                    + preErrMessage;
        }

        Path existsAncestor = FileUtils.findExistsAncestor(absPath);
        if(existsAncestor == null){
            abortNoRoot(absPath, preErrMessage);
        }else if( ! Files.isWritable(existsAncestor) ){
            abortCantWriteAncestor(existsAncestor, preErrMessage);
        }

        String prompt =
                "設定ファイル格納ディレクトリ<br/>"
                + getCenteredFileName(absPath)
                + "を作成します。";
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

        checkAccessibility(absPath);

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
        ConfigDirUtils.checkAccessibility(imgCacheDir);

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
     * 設定ディレクトリ操作の
     * 共通エラーメッセージ確認ダイアログを表示する。
     *
     * <p>閉じるまで待つ。
     *
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
     *
     * <p>閉じるまで待つ。
     *
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
     *
     * <p>閉じるまで待つ。
     *
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
     * 設定ディレクトリのルートファイルシステムもしくはドライブレターに
     * アクセスできないエラーをダイアログに提示し、VM終了する。
     *
     * @param path 設定ディレクトリ
     * @param preMessage メッセージ前半
     */
    private static void abortNoRoot(Path path, String preMessage){
        Path root = path.getRoot();
        showErrorMessage(
                "<html>"
                + preMessage + "<br/>"
                + getCenteredFileName(root)
                + "を用意する方法が不明です。<br/>"
                + "起動を中止します。<br/>"
                + MSG_POST
                + "</html>" );
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
        showErrorMessage(
                "<html>"
                + preMessage + "<br/>"
                + getCenteredFileName(existsAncestor)
                + "への書き込みができないため、"
                + "処理の続行は不可能です。<br/>"
                + "起動を中止します。<br/>"
                + MSG_POST
                + "</html>" );
        abort();
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
        String message =
                "<html>"
                + preMessage + "<br/>"
                + "このディレクトリを今から<br/>"
                + getCenteredFileName(existsAncestor)
                + "に作成して構いませんか？<br/>"
                + "このディレクトリ名は、後からいつでもヘルプウィンドウで<br/>"
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
                + "設定ディレクトリの作成をせずに起動を中止します。<br/>"
                + MSG_POST
                + "</html>" );
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
        showErrorMessage(
                "<html>"
                + "設定ディレクトリ<br/>"
                + getCenteredFileName(path)
                + "の作成に失敗しました。"
                + "起動を中止します。<br/>"
                + MSG_POST
                + "</html>" );
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
        showErrorMessage(
                "<html>"
                + "設定ディレクトリ<br/>"
                + getCenteredFileName(path)
                + "へのアクセスができません。"
                + "起動を中止します。<br/>"
                + "このディレクトリへのアクセス権を調整し"
                + "読み書きできるようにしてください。<br/>"
                + MSG_POST
                + "</html>" );
        abort();
        return;
    }

    /**
     * ファイルに書き込めないエラーをダイアログで提示し、VM終了する。
     *
     * @param file 書き込めなかったファイル
     */
    private static void abortCantWrite(Path file){
        showErrorMessage(
                "<html>"
                + "ファイル<br/>"
                + getCenteredFileName(file)
                + "への書き込みができません。"
                + "起動を中止します。<br/>"
                + "</html>" );
        abort();
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

    /**
     * 設定ディレクトリがアクセス可能でなければ
     * エラーダイアログを出してVM終了する。
     *
     * @param confDir 設定ディレクトリ
     */
    public static void checkAccessibility(Path confDir){
        if( ! FileUtils.isAccessibleDirectory(confDir) ){
            abortCantAccessConfigDir(confDir);
        }

        return;
    }

    /**
     * センタリングされたファイル名表示のHTML表記を出力する。
     *
     * @param path ファイル
     * @return HTML表記
     */
    public static String getCenteredFileName(Path path){
        return "<center>[&nbsp;"
                + FileUtils.getHtmledFileName(path)
                + "&nbsp;]</center>"
                + "<br/>";
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
                showInfoMessage(
                        "<html>"
                        + "設定ディレクトリを使わずに起動を続行します。<br/>"
                        + "今回、各種設定の読み込み・保存はできません。<br/>"
                        + "<code>"
                        + CmdOption.OPT_NOCONF
                        + "</code> オプション"
                        + "を使うとこの警告は出なくなります。"
                        + "</html>");
                break;
            }else if(lockPane.isRadioForce()){
                lock.forceRemove();
                if(lock.isExistsFile()){
                    showErrorMessage(
                            "<html>"
                            + "ロックファイルの強制解除に失敗しました。<br/>"
                            + "他に動いているJindolf"
                            + "が見つからないのであれば、<br/>"
                            + "なんとかしてロックファイル<br/>"
                            + getCenteredFileName(lockFile.toPath())
                            + "を削除してください。<br/>"
                            + "起動を中止します。"
                            + "</html>");
                    abort();
                    break;
                }
                lock.tryLock();
                if(lock.isFileOwner()) break;
                showErrorMessage(
                        "<html>"
                        + "ロックファイル<br/>"
                        + getCenteredFileName(lockFile.toPath())
                        + "を確保することができません。<br/>"
                        + "起動を中止します。"
                        + "</html>");
                abort();
                break;
            }
        }

        lockDialog.dispose();

        return;
    }

}