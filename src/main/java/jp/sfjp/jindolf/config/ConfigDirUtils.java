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
 *
 * <p>ファイル操作の異常系に関する、
 * ユーザとのインタラクション動作など。
 */
public final class ConfigDirUtils{

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private static final String TITLE_BUILDCONF =
            VerInfo.TITLE + "設定格納ディレクトリの設定";

    private static final Path FILE_README = Paths.get("README.txt");
    private static final Path FILE_AVATARJSON = Paths.get("avatarCache.json");

    private static final String RES_DIR = "resources";
    private static final String RES_README = RES_DIR + "/README.txt";
    private static final String RES_IMGDIR = RES_DIR + "/image";
    private static final String RES_AVATARJSON =
            RES_IMGDIR + "/avatarCache.json";

    private static final String MSG_POST =
            "<ul>"
            + "<li><code>" + CmdOption.OPT_CONFDIR + "</code>"
            + "&nbsp;オプション指定により、<br/>"
            + "任意の設定格納ディレクトリを指定することができます。<br/>"
            + "<li><code>" + CmdOption.OPT_NOCONF + "</code>"
            + "&nbsp;オプション指定により、<br/>"
            + "設定格納ディレクトリを使わずに起動することができます。<br/>"
            + "</ul>";
    private static final String MSG_NOCONF =
            "<html>"
            + "設定ディレクトリを使わずに起動を続行します。<br/>"
            + "今回、各種設定の読み込み・保存はできません。<br/>"
            + "<code>"
            + CmdOption.OPT_NOCONF
            + "</code> オプション"
            + "を使うとこの警告は出なくなります。"
            + "</html>";
    private static final String MSG_ABORT =
            "<html>"
            + "設定ディレクトリの作成をせずに起動を中止します。<br/>"
            + MSG_POST
            + "</html>";
    private static final String FORM_FAILRM =
            "<html>"
            + "ロックファイルの強制解除に失敗しました。<br/>"
            + "他に動いているJindolf"
            + "が見つからないのであれば、<br/>"
            + "なんとかしてロックファイル<br/>"
            + "{0}"
            + "を削除してください。<br/>"
            + "起動を中止します。"
            + "</html>";
    private static final String FORM_ILLLOCK =
            "<html>"
            + "ロックファイル<br/>"
            + "{0}"
            + "を確保することができません。<br/>"
            + "起動を中止します。"
            + "</html>";
    private static final String FORM_MKDIRFAIL =
            "<html>"
            + "ディレクトリ<br/>"
            + "{0}"
            + "の作成に失敗しました。"
            + "起動を中止します。<br/>"
            + MSG_POST
            + "</html>";
    private static final String FORM_ACCERR =
            "<html>"
            + "ディレクトリ<br/>"
            + "{0}"
            + "へのアクセスができません。"
            + "起動を中止します。<br/>"
            + "このディレクトリへのアクセス権を調整し"
            + "読み書きできるようにしてください。<br/>"
            + MSG_POST
            + "</html>";
    private static final String FORM_WRITEERR =
            "<html>"
            + "ファイル<br/>"
            + "{0}"
            + "への書き込みができません。"
            + "起動を中止します。<br/>"
            + "</html>";
    private static final String FORM_MKCONF =
            "<html>"
            + "設定ファイル格納ディレクトリ<br/>"
            + "{0}を作成します。<br/>"
            + "このディレクトリを今から作成して構いませんか？<br/>"
            + "このディレクトリ名は、後からいつでもヘルプウィンドウで<br/>"
            + "確認することができます。"
            + "</html>";

    private static final String LOG_MKDIRERR =
            "ディレクトリ{0}を生成できません";
    private static final String LOG_CREATEERR =
            "ファイル{0}を生成できません";
    private static final String LOG_RESCPY =
            "内部リソースから{0}へコピーが行われました。";

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
        JOptionPane pane = new JOptionPane(
                txt, JOptionPane.ERROR_MESSAGE);
        showDialog(pane);
        return;
    }

    /**
     * センタリングされたファイル名表示のHTML表記を出力する。
     *
     * @param path ファイル
     * @return HTML表記
     */
    private static String getCenteredFileName(Path path){
        String form = "<center>[&nbsp;{0}&nbsp;]</center><br/>";
        String fileName = FileUtils.getHtmledFileName(path);
        String result = MessageFormat.format(form, fileName);
        return result;
    }

    /**
     * ディレクトリが生成できないエラーをダイアログで提示し、
     * VM終了する。
     *
     * @param path 生成できなかったディレクトリ
     */
    private static void abortCantBuildDir(Path path){
        String fileName = getCenteredFileName(path);
        String msg = MessageFormat.format(FORM_MKDIRFAIL, fileName);

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
        String fileName = getCenteredFileName(path);
        String msg = MessageFormat.format(FORM_ACCERR, fileName);

        showErrorMessage(msg);
        abort();
        assert false;

        return;
    }

    /**
     * ディレクトリが生成できない異常系をログ出力する。
     *
     * @param dirPath 生成できなかったディレクトリ
     * @param cause 異常系原因
     */
    private static void logMkdirErr(Path dirPath, Throwable cause){
        String pathTxt = dirPath.toString();
        String msg = MessageFormat.format(LOG_MKDIRERR, pathTxt);
        LOGGER.log(Level.SEVERE, msg, cause);
        return;
    }

    /**
     * リソースからローカルファイルへコピーする。
     *
     * @param resource リソース名
     * @param dest ローカルファイル
     */
    private static void copyResource(String resource, Path dest){
        try(InputStream ris =
                ResourceManager.getResourceAsStream(resource)){
            InputStream is = new BufferedInputStream(ris);
            Files.copy(is, dest);
        }catch(IOException | SecurityException e){
            String destName = dest.toString();
            String logMsg = MessageFormat.format(LOG_CREATEERR, destName);
            LOGGER.log(Level.SEVERE, logMsg, e);

            String destHtml = getCenteredFileName(dest);
            String diagMsg = MessageFormat.format(FORM_WRITEERR, destHtml);
            showErrorMessage(diagMsg);
            abort();

            assert false;
        }

        String destName = dest.toString();
        String msg = MessageFormat.format(LOG_RESCPY, destName);
        LOGGER.info(msg);

        return;
    }

    /**
     * 設定ディレクトリがアクセス可能でなければ
     * エラーダイアログを出してVM終了する。
     *
     * @param confDir 設定ディレクトリ
     */
    private static void checkDirPerm(Path confDir){
        if( ! FileUtils.isAccessibleDirectory(confDir) ){
            abortCantAccessDir(confDir);
        }

        return;
    }

    /**
     * 設定ディレクトリの存在を確認し、なければ作る。
     *
     * <p>設定ディレクトリを使わない場合は何もしない。
     *
     * @param configStore 設定ディレクトリ情報
     */
    public static void prepareConfigDir(ConfigStore configStore){
        if( ! configStore.useStoreFile() ) return;

        Path conf = configStore.getConfigDir();
        if(Files.exists(conf)){
            checkDirPerm(conf);
        }else{
            buildConfDirPath(conf);
        }

        Path imgDir = configStore.getLocalImgDir();
        if(Files.exists(imgDir)){
            checkDirPerm(imgDir);
        }else{
            buildImageCacheDir(imgDir);
        }

        return;
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
    private static Path buildConfDirPath(Path confPath){
        assert confPath.isAbsolute();
        if(Files.exists(confPath)) return confPath;

        boolean confirmed = confirmBuildConfigDir(confPath);
        if( ! confirmed ){
            JOptionPane pane = new JOptionPane(
                    MSG_ABORT, JOptionPane.WARNING_MESSAGE);
            showDialog(pane);
            abort();
            assert false;
        }

        try{
            Files.createDirectories(confPath);
        }catch(IOException | SecurityException e){
            logMkdirErr(confPath, e);
            abortCantBuildDir(confPath);
            assert false;
        }

        // FileUtils.setOwnerOnlyAccess(absPath);

        checkDirPerm(confPath);

        Path readme = confPath.resolve(FILE_README);
        copyResource(RES_README, readme);

        return confPath;
    }

    /**
     * 設定ディレクトリを新規に生成してよいかダイアログで問い合わせる。
     *
     * @param confDir 設定ディレクトリ
     * @return 生成してよいと指示があればtrue
     */
    private static boolean confirmBuildConfigDir(Path confDir){
        String confName = getCenteredFileName(confDir);
        String msg = MessageFormat.format(FORM_MKCONF, confName);

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
    private static void buildImageCacheDir(Path imgCacheDir){
        assert imgCacheDir.isAbsolute();
        if(Files.exists(imgCacheDir)) return;

        try{
            Files.createDirectories(imgCacheDir);
        }catch(IOException | SecurityException e){
            logMkdirErr(imgCacheDir, e);
            abortCantBuildDir(imgCacheDir);
            assert false;
        }

        checkDirPerm(imgCacheDir);

        Path jsonPath = imgCacheDir.resolve(FILE_AVATARJSON);
        copyResource(RES_AVATARJSON, jsonPath);

        return;
    }

    /**
     * ロックファイルの取得を試みる。
     *
     * <p>ロックに失敗したが処理を続行する場合、
     * 設定ディレクトリは使わないものとして続行する。
     *
     * @param configStore 設定ディレクトリ情報
     */
    public static void tryLock(ConfigStore configStore){
        if( ! configStore.useStoreFile() ) return;

        Path lockPath = configStore.getLockFile();
        File lockFile = lockPath.toFile();
        InterVMLock lock = new InterVMLock(lockFile);

        lock.tryLock();

        if( ! lock.isFileOwner() ){
            confirmLockError(lock);
            if( ! lock.isFileOwner() ){
                configStore.setNoConf();
            }
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
    private static void confirmLockError(InterVMLock lock){
        File lockFile = lock.getLockFile();

        LockErrorPane lockPane = new LockErrorPane(lockFile.toPath());
        JDialog lockDialog = lockPane.createDialog(TITLE_BUILDCONF);

        lockDialog.setResizable(true);
        lockDialog.pack();

        do{
            lockDialog.setVisible(true);

            Object result = lockPane.getValue();
            boolean aborted = LockErrorPane.isAborted(result);
            boolean windowClosed = result == null;

            if(aborted || windowClosed){
                abort();
                assert false;
                break;
            }

            if(lockPane.isRadioRetry()){
                lock.tryLock();
            }else if(lockPane.isRadioContinue()){
                JOptionPane pane = new JOptionPane(
                        MSG_NOCONF, JOptionPane.INFORMATION_MESSAGE);
                showDialog(pane);
                break;
            }else if(lockPane.isRadioForce()){
                forceRemove(lock);
                break;
            }
        }while( ! lock.isFileOwner());

        lockDialog.dispose();

        return;
    }

    /**
     * ロックファイルの強制削除を試みる。
     *
     * <p>削除とそれに後続する再ロック取得に成功したときだけ制御を戻す。
     *
     * <p>削除できないまたは再ロックできない場合は、
     * 制御を戻さずVMごとアプリを終了する。
     *
     * @param lock ロック
     */
    private static void forceRemove(InterVMLock lock){
        File lockFile = lock.getLockFile();

        lock.forceRemove();
        if(lock.isExistsFile()){
            String fileName = getCenteredFileName(lockFile.toPath());
            String msg = MessageFormat.format(FORM_FAILRM, fileName);
            showErrorMessage(msg);
            abort();
            assert false;
            return;
        }

        lock.tryLock();
        if(lock.isFileOwner()) return;

        String fileName = getCenteredFileName(lockFile.toPath());
        String msg = MessageFormat.format(FORM_ILLLOCK, fileName);
        showErrorMessage(msg);
        abort();
        assert false;

        return;
    }

}
