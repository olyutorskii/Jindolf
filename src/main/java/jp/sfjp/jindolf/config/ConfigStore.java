/*
 * config store
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.sourceforge.jovsonz.JsComposition;
import jp.sourceforge.jovsonz.JsObject;
import jp.sourceforge.jovsonz.JsParseException;
import jp.sourceforge.jovsonz.JsTypes;
import jp.sourceforge.jovsonz.JsVisitException;
import jp.sourceforge.jovsonz.Json;

/**
 * 各種設定の永続化関連。
 */
public class ConfigStore {

    /** 検索履歴ファイル。 */
    public static final File HIST_FILE = new File("searchHistory.json");
    /** ネットワーク設定ファイル。 */
    public static final File NETCONFIG_FILE = new File("netconfig.json");
    /** 台詞表示設定ファイル。 */
    public static final File TALKCONFIG_FILE = new File("talkconfig.json");
    /** ローカル画像格納ディレクトリ。 */
    public static final Path LOCALIMG_DIR = Paths.get("img");
    /** ローカル画像設定ファイル。 */
    public static final Path LOCALIMGCONFIG_PATH =
            Paths.get("avatarCache.json");

    private static final String LOCKFILE = "lock";

    private static final Charset CHARSET_JSON = StandardCharsets.UTF_8;

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    private boolean useStoreFile;
    private boolean isImplicitPath;
    private File configDir;


    /**
     * コンストラクタ。
     *
     * @param useStoreFile 設定ディレクトリ内への
     *     セーブデータ機能を使うならtrue
     * @param isImplicitPath 起動コマンドラインから指定された
     *     設定ディレクトリの場合false
     * @param configDirPath 設定ディレクトリ。
     *     設定ディレクトリを使わない場合は無視される。
     */
    public ConfigStore(boolean useStoreFile,
                       boolean isImplicitPath,
                       File configDirPath ){
        super();

        this.useStoreFile = useStoreFile;

        if(this.useStoreFile){
            this.isImplicitPath = isImplicitPath;
            this.configDir = configDirPath;
        }else{
            this.isImplicitPath = true;
            this.configDir = null;
        }

        return;
    }


    /**
     * 設定ディレクトリを使うか否か判定する。
     *
     * @return 設定ディレクトリを使うならtrue。
     */
    public boolean useStoreFile(){
        return this.useStoreFile;
    }

    /**
     * 設定ディレクトリを返す。
     *
     * @return 設定ディレクトリ。設定ディレクトリを使わない場合はnull
     */
    public File getConfigDir(){
        return this.configDir;
    }

    /**
     * ローカル画像格納ディレクトリを返す。
     *
     * @return 格納ディレクトリ。格納ディレクトリを使わない場合はnull
     */
    public Path getLocalImgDir(){
        if(this.configDir == null) return null;

        Path configPath = this.configDir.toPath();
        Path result = configPath.resolve(LOCALIMG_DIR);

        return result;
    }

    /**
     * 設定ディレクトリの存在を確認し、なければ作る。
     *
     * <p>設定ディレクトリを使わない場合は何もしない。
     */
    public void prepareConfigDir(){
        if( ! this.useStoreFile ) return;

        if( ! this.configDir.exists() ){
            File created =
                ConfigFile.buildConfigDirectory(this.configDir,
                                                this.isImplicitPath );
            ConfigFile.checkAccessibility(created);
        }else{
            ConfigFile.checkAccessibility(this.configDir);
        }

        File imgDir = new File(this.configDir, "img");
        if( ! imgDir.exists()){
            ConfigFile.buildImageCacheDir(imgDir);
        }

        return;
    }

    /**
     * ロックファイルの取得を試みる。
     *
     * <p>ロックに失敗したが処理を続行する場合、
     * 設定ディレクトリは使わないものとして続行する。
     */
    public void tryLock(){
        if( ! this.useStoreFile ) return;

        File lockFile = new File(this.configDir, LOCKFILE);
        InterVMLock lock = new InterVMLock(lockFile);

        lock.tryLock();

        if( ! lock.isFileOwner() ){
            ConfigFile.confirmLockError(lock);
            if( ! lock.isFileOwner() ){
                this.useStoreFile = false;
                this.isImplicitPath = true;
                this.configDir = null;
            }
        }

        return;
    }

    /**
     * 設定ディレクトリ上のOBJECT型JSONファイルを読み込む。
     *
     * @param file JSONファイルの相対パス。
     * @return JSON object。
     *     設定ディレクトリを使わない設定、
     *     もしくはJSONファイルが存在しない、
     *     もしくはOBJECT型でなかった、
     *     もしくは入力エラーがあればnull
     */
    public JsObject loadJsObject(File file){
        JsComposition<?> root = loadJson(file);
        if(root == null || root.getJsTypes() != JsTypes.OBJECT) return null;
        JsObject result = (JsObject) root;
        return result;
    }

    /**
     * 設定ディレクトリ上のJSONファイルを読み込む。
     *
     * @param file JSONファイルの相対パス
     * @return JSON objectまたはarray。
     *     設定ディレクトリを使わない設定、
     *     もしくはJSONファイルが存在しない、
     *     もしくは入力エラーがあればnull
     */
    public JsComposition<?> loadJson(File file){
        if( ! this.useStoreFile ) return null;

        File absFile;
        if(file.isAbsolute()){
            absFile = file;
        }else{
            if(this.configDir == null) return null;
            absFile = new File(this.configDir, file.getPath());
            if( ! absFile.exists() ) return null;
            if( ! absFile.isAbsolute() ) return null;
        }
        String absPath = absFile.getPath();

        InputStream istream;
        try{
            istream = new FileInputStream(absFile);
        }catch(FileNotFoundException e){
            assert false;
            return null;
        }
        istream = new BufferedInputStream(istream);

        JsComposition<?> root;
        try{
            root = loadJson(istream);
        }catch(IOException e){
            LOGGER.log(Level.SEVERE,
                    "JSONファイル["
                    + absPath
                    + "]の読み込み時に支障がありました。", e);
            return null;
        }catch(JsParseException e){
            LOGGER.log(Level.SEVERE,
                    "JSONファイル["
                    + absPath
                    + "]の内容に不備があります。", e);
            return null;
        }finally{
            try{
                istream.close();
            }catch(IOException e){
                LOGGER.log(Level.SEVERE,
                        "JSONファイル["
                        + absPath
                        + "]を閉じることができません。", e);
                return null;
            }
        }

        return root;
    }

    /**
     * バイトストリーム上のJSONデータを読み込む。
     *
     * <p>バイトストリームはUTF-8と解釈される。
     *
     * @param is バイトストリーム
     * @return JSON objectまたはarray。
     * @throws IOException 入力エラー
     * @throws JsParseException 構文エラー
     */
    protected JsComposition<?> loadJson(InputStream is)
            throws IOException, JsParseException {
        Reader reader = new InputStreamReader(is, CHARSET_JSON);
        reader = new BufferedReader(reader);
        JsComposition<?> root = loadJson(reader);
        return root;
    }

    /**
     * 文字ストリーム上のJSONデータを読み込む。
     *
     * @param reader 文字ストリーム
     * @return JSON objectまたはarray。
     * @throws IOException 入力エラー
     * @throws JsParseException 構文エラー
     */
    protected JsComposition<?> loadJson(Reader reader)
            throws IOException, JsParseException {
        JsComposition<?> root = Json.parseJson(reader);
        return root;
    }

    /**
     * 設定ディレクトリ上のJSONファイルに書き込む。
     *
     * @param file JSONファイルの相対パス
     * @param root JSON objectまたはarray
     * @return 正しくセーブが行われればtrue。
     *     何らかの理由でセーブが完了できなければfalse
     */
    public boolean saveJson(File file, JsComposition<?> root){
        if( ! this.useStoreFile ) return false;

        // TODO テンポラリファイルを用いたより安全なファイル更新
        File absFile = new File(this.configDir, file.getPath());
        String absPath = absFile.getPath();

        absFile.delete();
        try{
            if(absFile.createNewFile() != true) return false;
        }catch(IOException e){
            LOGGER.log(Level.SEVERE,
                    "JSONファイル["
                    + absPath
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

        try{
            saveJson(ostream, root);
        }catch(JsVisitException e){
            LOGGER.log(Level.SEVERE,
                    "JSONファイル["
                    + absPath
                    + "]の出力処理で支障がありました。", e);
            return false;
        }catch(IOException e){
            LOGGER.log(Level.SEVERE,
                    "JSONファイル["
                    + absPath
                    + "]の書き込み時に支障がありました。", e);
            return false;
        }finally{
            try{
                ostream.close();
            }catch(IOException e){
                LOGGER.log(Level.SEVERE,
                        "JSONファイル["
                        + absPath
                        + "]を閉じることができません。", e);
                return false;
            }
        }

        return true;
    }

    /**
     * バイトストリームにJSONデータを書き込む。
     *
     * <p>バイトストリームはUTF-8と解釈される。
     *
     * @param os バイトストリーム出力
     * @param root JSON objectまたはarray
     * @throws IOException 出力エラー
     * @throws JsVisitException 構造エラー
     */
    protected void saveJson(OutputStream os, JsComposition<?> root)
            throws IOException, JsVisitException {
        Writer writer = new OutputStreamWriter(os, CHARSET_JSON);
        writer = new BufferedWriter(writer);
        saveJson(writer, root);
        return;
    }

    /**
     * 文字ストリームにJSONデータを書き込む。
     *
     * @param writer 文字ストリーム出力
     * @param root JSON objectまたはarray
     * @throws IOException 出力エラー
     * @throws JsVisitException 構造エラー
     */
    protected void saveJson(Writer writer, JsComposition<?> root)
            throws IOException, JsVisitException {
        Json.dumpJson(writer, root);
        return;
    }

    /**
     * 検索履歴ファイルを読み込む。
     *
     * @return 履歴データ。履歴を読まないもしくは読めない場合はnull
     */
    public JsObject loadHistoryConfig(){
        JsObject result = loadJsObject(HIST_FILE);
        return result;
    }

    /**
     * ネットワーク設定ファイルを読み込む。
     *
     * @return ネットワーク設定データ。
     *     設定を読まないもしくは読めない場合はnull
     */
    public JsObject loadNetConfig(){
        JsObject result = loadJsObject(NETCONFIG_FILE);
        return result;
    }

    /**
     * 台詞表示設定ファイルを読み込む。
     *
     * @return 台詞表示設定データ。
     *     設定を読まないもしくは読めない場合はnull
     */
    public JsObject loadTalkConfig(){
        JsObject result = loadJsObject(TALKCONFIG_FILE);
        return result;
    }

    /**
     * ローカル画像設定ファイルを読み込む。
     *
     * @return ローカル画像設定データ。
     *     設定を読まないもしくは読めない場合はnull
     */
    public JsObject loadLocalImgConfig(){
        Path path = LOCALIMG_DIR.resolve(LOCALIMGCONFIG_PATH);
        JsObject result = loadJsObject(path.toFile());
        return result;
    }

    /**
     * 検索履歴ファイルに書き込む。
     *
     * @param root 履歴データ
     * @return 書き込まなかったもしくは書き込めなかった場合はfalse
     */
    public boolean saveHistoryConfig(JsComposition<?> root){
        boolean result = saveJson(HIST_FILE, root);
        return result;
    }

    /**
     * ネットワーク設定ファイルに書き込む。
     *
     * @param root ネットワーク設定
     * @return 書き込まなかったもしくは書き込めなかった場合はfalse
     */
    public boolean saveNetConfig(JsComposition<?> root){
        boolean result = saveJson(NETCONFIG_FILE, root);
        return result;
    }

    /**
     * 台詞表示設定ファイルに書き込む。
     *
     * @param root 台詞表示設定
     * @return 書き込まなかったもしくは書き込めなかった場合はfalse
     */
    public boolean saveTalkConfig(JsComposition<?> root){
        boolean result = saveJson(TALKCONFIG_FILE, root);
        return result;
    }

}
