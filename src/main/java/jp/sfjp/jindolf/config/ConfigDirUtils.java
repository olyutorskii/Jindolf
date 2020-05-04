/*
 * configuration file & directory
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import jp.sfjp.jindolf.ResourceManager;
import jp.sfjp.jindolf.VerInfo;
import jp.sfjp.jindolf.view.LockErrorPane;

/**
 * Jindolf設定格納ディレクトリに関するあれこれ。
 */
public final class ConfigDirUtils{

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private static final String TITLE_BUILDCONF =
            VerInfo.TITLE + "設定格納ディレクトリの設定";

    private static final Path JINCONF     = Paths.get("Jindolf");
    private static final Path JINCONF_DOT = Paths.get(".jindolf");
    private static final Path FILE_README = Paths.get("README.txt");
    private static final Path FILE_AVATARJSON = Paths.get("avatarCache.json");

    private static final String RES_DIR = "resources";
    private static final String RES_README = RES_DIR + "/README.txt";
    private static final String RES_IMGDIR = RES_DIR + "/image";
    private static final String RES_AVATARJSON = RES_IMGDIR + "/avatarCache.json";

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
     * @param seq メッセージtxt
     */
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
     * @param seq メッセージtxt
     */
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
        assert false;

        return;
    }

    /**
     * ディレクトリが生成できないエラーをダイアログで提示し、
     * VM終了する。
     *
     * @param path 生成できなかったディレクトリ
     */
    private static void abortCantBuildDir(Path path){
        String form =
                "<html>"
                + "ディレクトリ<br/>"
                + "{0}"
                + "の作成に失敗しました。"
                + "起動を中止します。<br/>"
                + MSG_POST
                + "</html>";
        String fileName = getCenteredFileName(path);
        String msg = MessageFormat.format(form, fileName);

        showErrorMessage(msg);
        abort();
        assert false;

        return;
    }

    /**
     * ディレクトリへアクセスできないエラーをダイアログで提示し、
     * VM終了する。
     *
     * @param path アクセスできないディレクトリ
     */
    private static void abortCantAccessDir(Path path){
        String form =
                "<html>"
                + "ディレクトリ<br/>"
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
        assert false;

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
        assert false;

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
            abortCantAccessDir(confDir);
        }

        return;
    }

    /**
     * アプリケーション設定ディレクトリを返す。
     *
     * <p>存在の有無、アクセスの可否は関知しない。
     *
     * <p>WindowsやLinuxではホームディレクトリ。
     * Mac OS X ではさらにホームディレクトリの下の
     * "Library/Application Support/"
     *
     * @return アプリケーション設定ディレクトリ
     */
    public static Path getAppSetDir(){
        Path home = FileUtils.getHomeDirectory();

        Path result = home;

        if(FileUtils.isMacOSXFs()){
            result = result.resolve("Library");
            result = result.resolve("Application Support");
        }

        return result;
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
    public static Path getDefaultConfDirPath(){
        Path jarParent = FileUtils.getJarDirectory();
        if(FileUtils.isAccessibleDirectory(jarParent)){
            Path confPath = jarParent.resolve(JINCONF);
            if(FileUtils.isAccessibleDirectory(confPath)){
                return confPath;
            }
        }

        Path appset = getAppSetDir();

        Path result;
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
     * <p>既に存在すればなにもしない。
     *
     * @param confPath 設定格納ディレクトリ
     * @return 新規に作成した設定格納ディレクトリの絶対パス
     */
    public static Path buildConfDirPath(Path confPath){
        Path absPath = confPath.toAbsolutePath();
        if(Files.exists(absPath)) return absPath;

        boolean confirmed = confirmBuildConfigDir(absPath);
        if( ! confirmed ){
            abortQuitBuildConfigDir();
            assert false;
        }

        try{
            Files.createDirectories(absPath);
        }catch(IOException | SecurityException e){
            String msg = MessageFormat.format(
                    "{0}を生成できません", absPath.toString());
            LOGGER.log(Level.SEVERE, msg, e);
            abortCantBuildDir(absPath);
            assert false;
        }

        // FileUtils.setOwnerOnlyAccess(absPath);

        checkDirPerm(absPath);

        touchReadme(absPath);

        return absPath;
    }

    /**
     * 設定ディレクトリを新規に生成してよいかダイアログで問い合わせる。
     *
     * @param confDir 設定ディレクトリ
     * @return 生成してよいと指示があればtrue
     */
    private static boolean confirmBuildConfigDir(Path confDir){
        String form =
                "<html>"
                + "設定ファイル格納ディレクトリ<br/>"
                + "{0}を作成します。<br/>"
                + "このディレクトリを今から作成して構いませんか？<br/>"
                + "このディレクトリ名は、後からいつでもヘルプウィンドウで<br/>"
                + "確認することができます。"
                + "</html>";
        String confName = getCenteredFileName(confDir);
        String msg = MessageFormat.format(form, confName);

        JOptionPane pane;
        pane = new JOptionPane(msg,
                               JOptionPane.QUESTION_MESSAGE,
                               JOptionPane.YES_NO_OPTION);
        showDialog(pane);

        Object val = pane.getValue();
        if( ! (val instanceof Integer) ) return false;
        int ival = (int) val;
        boolean result = ival == JOptionPane.YES_OPTION;

        return result;
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
        Path absPath = imgCacheDir.toAbsolutePath();
        if(Files.exists(absPath)) return;

        try{
            Files.createDirectories(absPath);
        }catch(IOException e){
            String msg = MessageFormat.format(
                    "ディレクトリ{0}を生成できません", absPath.toString());
            LOGGER.log(Level.SEVERE, msg, e);
            abortCantBuildDir(absPath);
            assert false;
        }

        checkDirPerm(absPath);

        Path jsonPath = imgCacheDir.resolve(FILE_AVATARJSON);

        try(InputStream ris =
                ResourceManager.getResourceAsStream(RES_AVATARJSON)){
            InputStream is = new BufferedInputStream(ris);
            Files.copy(is, jsonPath);
        }catch(IOException e){
            String msg = MessageFormat.format(
                    "ファイル{0}を生成できませんでした", jsonPath.toString()
            );
            LOGGER.log(Level.SEVERE, msg, e);
            abortCantWrite(jsonPath);
            assert false;
        }

        return;
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
                assert false;
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
                    assert false;

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
                assert false;

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

        try(InputStream ris =
                ResourceManager.getResourceAsStream(RES_README)){
            InputStream is = new BufferedInputStream(ris);
            Files.copy(is, readme);
        }catch(IOException e){
            String msg = MessageFormat.format(
                    "{0}が生成できませんでした", readme.toString()
            );
            LOGGER.log(Level.SEVERE, msg, e);
            abortCantWrite(readme);
            assert false;
        }

        return;
    }

}
