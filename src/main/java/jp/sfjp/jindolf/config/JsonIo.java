/*
 * JSON I/O
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.sourceforge.jovsonz.JsComposition;
import jp.sourceforge.jovsonz.JsObject;
import jp.sourceforge.jovsonz.JsParseException;
import jp.sourceforge.jovsonz.JsTypes;
import jp.sourceforge.jovsonz.JsVisitException;
import jp.sourceforge.jovsonz.Json;

/**
 * JSONファイルの入出力。
 */
public class JsonIo {

    /** 検索履歴ファイル。 */
    public static final Path HIST_FILE = Paths.get("searchHistory.json");
    /** ネットワーク設定ファイル。 */
    public static final Path NETCONFIG_FILE = Paths.get("netconfig.json");
    /** 台詞表示設定ファイル。 */
    public static final Path TALKCONFIG_FILE = Paths.get("talkconfig.json");

    /** ローカル画像設定ファイル。 */
    public static final Path LOCALIMGCONFIG_PATH =
            Paths.get("avatarCache.json");


    private static final Charset CHARSET_JSON = StandardCharsets.UTF_8;
    private static final Logger LOGGER = Logger.getAnonymousLogger();


    private final ConfigStore configStore;


    /**
     * Constructor.
     *
     * @param configStore 設定ディレクトリ
     */
    public JsonIo(ConfigStore configStore){
        super();
        Objects.nonNull(configStore);
        this.configStore = configStore;
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
    public JsObject loadJsObject(Path file){
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
    public JsComposition<?> loadJson(Path file){
        Path absFile;
        if(file.isAbsolute()){
            absFile = file;
        }else{
            Path configDir = this.configStore.getConfigDir();
            if(configDir == null) return null;
            absFile = configDir.resolve(file);
            if( ! Files.exists(absFile) ) return null;
            if( ! absFile.isAbsolute() ) return null;
        }
        String absPath = absFile.toString();

        JsComposition<?> root;
        try(InputStream is = Files.newInputStream(absFile)){
            InputStream bis = new BufferedInputStream(is);
            root = loadJson(bis);
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
    public boolean saveJson(Path file, JsComposition<?> root){
        // TODO テンポラリファイルを用いたより安全なファイル更新
        Path configDir = this.configStore.getConfigDir();
        Path absFile = configDir.resolve(file);
        String absPath = absFile.toString();

        try{
            Files.deleteIfExists(absFile);
        }catch(IOException e){
            // NOTHING
            assert true;
        }

        try{
            Files.createFile(absFile);
        }catch(IOException e){
            LOGGER.log(Level.SEVERE,
                    "JSONファイル["
                    + absPath
                    + "]の新規生成ができません。", e);
            return false;
        }

        try(OutputStream os = Files.newOutputStream(absFile)){
            OutputStream bos = new BufferedOutputStream(os);
            saveJson(bos, root);
        }catch(IOException e){
            LOGGER.log(Level.SEVERE,
                    "JSONファイル["
                    + absPath
                    + "]の書き込み時に支障がありました。", e);
            return false;
        }catch(JsVisitException e){
            LOGGER.log(Level.SEVERE,
                    "JSONファイル["
                    + absPath
                    + "]の出力処理で支障がありました。", e);
            return false;
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
        Path path = ConfigStore.LOCALIMG_DIR.resolve(LOCALIMGCONFIG_PATH);
        JsObject result = loadJsObject(path);
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
