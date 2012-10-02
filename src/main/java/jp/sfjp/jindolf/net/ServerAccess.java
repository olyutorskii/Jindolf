/*
 * manage HTTP access
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.net;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import jp.sfjp.jindolf.data.Period;
import jp.sfjp.jindolf.data.Village;
import jp.sourceforge.jindolf.parser.ContentBuilder;
import jp.sourceforge.jindolf.parser.ContentBuilderSJ;
import jp.sourceforge.jindolf.parser.ContentBuilderUCS2;
import jp.sourceforge.jindolf.parser.DecodeException;
import jp.sourceforge.jindolf.parser.DecodedContent;
import jp.sourceforge.jindolf.parser.SjisDecoder;
import jp.sourceforge.jindolf.parser.StreamDecoder;

/**
 * 国ごとの人狼BBSサーバとの通信を一手に引き受ける。
 */
public class ServerAccess{

    private static final String USER_AGENT = HttpUtils.getUserAgentName();
    private static final String JINRO_CGI = "./index.rb";
    private static final
            Map<String, SoftReference<BufferedImage>> IMAGE_CACHE;

    private static final Logger LOGGER = Logger.getAnonymousLogger();
    private static final String ENC_POST = "UTF-8";

    static{
        Map<String, SoftReference<BufferedImage>> cache =
                new HashMap<String, SoftReference<BufferedImage>>();
        IMAGE_CACHE = Collections.synchronizedMap(cache);
    }


    private final URL baseURL;
    private final AuthManager authManager;

    private final Charset charset;
    private Proxy proxy = Proxy.NO_PROXY;
    private long lastServerMs;
    private long lastLocalMs;
    private long lastSystemMs;


    /**
     * 人狼BBSサーバとの接続管理を生成する。
     * この時点ではまだ通信は行われない。
     * @param baseURL 国別のベースURL
     * @param charset 国のCharset
     * @throws IllegalArgumentException 不正なURL
     */
    public ServerAccess(URL baseURL, Charset charset)
            throws IllegalArgumentException{
        super();

        this.baseURL = baseURL;
        this.authManager = new AuthManager(this.baseURL);
        this.charset = charset;

        return;
    }


    /**
     * 画像キャッシュを検索する。
     * @param key キー
     * @return キャッシュされた画像。キャッシュされていなければnull。
     */
    private static BufferedImage getImageCache(String key){
        if(key == null) return null;

        BufferedImage image;

        synchronized(IMAGE_CACHE){
            SoftReference<BufferedImage> ref = IMAGE_CACHE.get(key);
            if(ref == null) return null;

            Object referent = ref.get();
            if(referent == null){
                IMAGE_CACHE.remove(key);
                return null;
            }

            image = (BufferedImage) referent;
        }

        return image;
    }

    /**
     * 画像キャッシュに登録する。
     * @param key キー
     * @param image キャッシュしたい画像。
     */
    private static void putImageCache(String key, BufferedImage image){
        if(key == null || image == null) return;

        synchronized(IMAGE_CACHE){
            if(getImageCache(key) != null) return;
            SoftReference<BufferedImage> ref =
                    new SoftReference<BufferedImage>(image);
            IMAGE_CACHE.put(key, ref);
        }

        return;
    }

    /**
     * HTTP-Proxyを返す。
     * @return HTTP-Proxy
     */
    public Proxy getProxy(){
        return this.proxy;
    }

    /**
     * HTTP-Proxyを設定する。
     * @param proxy HTTP-Proxy。nullならProxyなしと解釈される。
     */
    public void setProxy(Proxy proxy){
        if(proxy == null) this.proxy = Proxy.NO_PROXY;
        else              this.proxy = proxy;
        return;
    }

    /**
     * 国のベースURLを返す。
     * @return ベースURL
     */
    public URL getBaseURL(){
        return this.baseURL;
    }

    /**
     * 与えられたクエリーとCGIのURLから新たにURLを合成する。
     * @param query クエリー
     * @return 新たなURL
     */
    protected URL getQueryURL(String query){
        if(query.length() >= 1 && query.charAt(0) != '?'){
            return null;
        }

        URL result;
        try{
            result = new URL(getBaseURL(), JINRO_CGI + query);
        }catch(MalformedURLException e){
            assert false;
            return null;
        }
        return result;
    }

    /**
     * エンコーディングされた入力ストリームから文字列を生成する。
     * @param istream 入力ストリーム
     * @return 文字列
     * @throws java.io.IOException 入出力エラー（おそらくネットワーク関連）
     */
    public DecodedContent downloadHTMLStream(InputStream istream)
            throws IOException{
        StreamDecoder decoder;
        ContentBuilder builder;
        if(this.charset.name().equalsIgnoreCase("Shift_JIS")){
            decoder = new SjisDecoder();
            builder = new ContentBuilderSJ(200 * 1024);
        }else if(this.charset.name().equalsIgnoreCase("UTF-8")){
            decoder = new StreamDecoder(this.charset.newDecoder());
            builder = new ContentBuilderUCS2(200 * 1024);
        }else{
            assert false;
            return null;
        }
        decoder.setDecodeHandler(builder);

        // TODO デコーダをインスタンス変数にできないか。
        // TODO DecodedContentのキャッシュ管理。

        try{
            decoder.decode(istream);
        }catch(DecodeException e){
            return null;
        }

        return builder.getContent();
    }

    /**
     * 与えられたクエリーを用いてHTMLデータを取得する。
     * @param query HTTP-GET クエリー
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    protected HtmlSequence downloadHTML(String query)
            throws IOException{
        URL url = getQueryURL(query);
        HtmlSequence result = downloadHTML(url);
        return result;
    }

    /**
     * 与えられたURLを用いてHTMLデータを取得する。
     * @param url URL
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    protected HtmlSequence downloadHTML(URL url)
            throws IOException{
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection(this.proxy);
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(false);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");

        connection.connect();

        long datems = updateLastAccess(connection);

        int responseCode = connection.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK){ // 200
            String logMessage =  "発言のダウンロードに失敗しました。";
            logMessage += HttpUtils.formatHttpStat(connection, 0, 0);
            LOGGER.warning(logMessage);
            return null;
        }

        String cs = HttpUtils.getHTMLCharset(connection);
        if(!cs.equalsIgnoreCase(this.charset.name())){
            return null;
        }

        InputStream stream = TallyInputStream.getInputStream(connection);
        DecodedContent html = downloadHTMLStream(stream);

        stream.close();
        connection.disconnect();

        HtmlSequence hseq = new HtmlSequence(url, datems, html);

        return hseq;
    }

    /**
     * 絶対または相対URLの指すパーマネントなイメージ画像をダウンロードする。
     * @param url 画像URL文字列
     * @return 画像イメージ
     * @throws java.io.IOException ネットワークエラー
     */
    public BufferedImage downloadImage(String url) throws IOException{
        URL absolute;
        try{
            URL base = getBaseURL();
            absolute = new URL(base, url);
        }catch(MalformedURLException e){
            assert false;
            return null;
        }

        BufferedImage image;
        image = getImageCache(absolute.toString());
        if(image != null) return image;

        HttpURLConnection connection =
                (HttpURLConnection) absolute.openConnection(this.proxy);
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setUseCaches(true);
        connection.setInstanceFollowRedirects(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");

        connection.connect();

        int responseCode       = connection.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK){
            String logMessage =  "イメージのダウンロードに失敗しました。";
            logMessage += HttpUtils.formatHttpStat(connection, 0, 0);
            LOGGER.warning(logMessage);
            return null;
        }

        InputStream stream = TallyInputStream.getInputStream(connection);
        image = ImageIO.read(stream);
        stream.close();

        connection.disconnect();

        putImageCache(absolute.toString(), image);

        return image;
    }

    /**
     * 指定された認証情報をPOSTする。
     * @param authData 認証情報
     * @return 認証情報が受け入れられたらtrue
     * @throws java.io.IOException ネットワークエラー
     */
    protected boolean postAuthData(String authData) throws IOException{
        URL url = getQueryURL("");
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection(this.proxy);
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        byte[] authBytes = authData.getBytes(ENC_POST);

        OutputStream os = TallyOutputStream.getOutputStream(connection);
        os.write(authBytes);
        os.flush();
        os.close();

        updateLastAccess(connection);

        connection.disconnect();

        if( ! this.authManager.hasLoggedIn() ){
            String logMessage =  "認証情報の送信に失敗しました。";
            LOGGER.warning(logMessage);
            return false;
        }

        LOGGER.info("正しく認証が行われました。");

        return true;
    }

    /**
     * トップページのHTMLデータを取得する。
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    public HtmlSequence getHTMLTopPage() throws IOException{
        return downloadHTML("");
    }

    /**
     * 国に含まれる村一覧HTMLデータを取得する。
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    public HtmlSequence getHTMLLandList() throws IOException{
        return downloadHTML("?cmd=log");
    }

    /**
     * 指定された村のPeriod一覧のHTMLデータを取得する。
     * 現在ゲーム進行中の村にも可能。
     * ※ 古国では使えないよ！
     * @param village 村
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    public HtmlSequence getHTMLBoneHead(Village village) throws IOException{
        String villageID = village.getVillageID();
        return downloadHTML("?vid=" + villageID + "&meslog=");
    }

    /**
     * 指定された村の最新PeriodのHTMLデータをロードする。
     * 既にGAMEOVERの村ではPeriod一覧のHTMLデータとなる。
     * @param village 村
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    public HtmlSequence getHTMLVillage(Village village) throws IOException{
        URL url = getVillageURL(village);
        return downloadHTML(url);
    }

    /**
     * 指定された村の最新PeriodのHTMLデータのURLを取得する。
     * @param village 村
     * @return URL
     */
    public URL getVillageURL(Village village){
        String villageID = village.getVillageID();
        URL url = getQueryURL("?vid=" + villageID);
        return url;
    }

    /**
     * 指定されたPeriodのHTMLデータをロードする。
     * @param period Period
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    public HtmlSequence getHTMLPeriod(Period period) throws IOException{
        URL url = getPeriodURL(period);
        return downloadHTML(url);
    }

    /**
     * 指定されたPeriodのHTMLデータのURLを取得する。
     * @param period 日
     * @return URL
     */
    public URL getPeriodURL(Period period){
        String query = period.getCGIQuery();
        URL url = getQueryURL(query);
        return url;
    }

    /**
     * 最終アクセス時刻を更新する。
     * @param connection HTTP接続
     * @return リソース送信時刻
     */
    public long updateLastAccess(HttpURLConnection connection){
        this.lastServerMs = connection.getDate();
        this.lastLocalMs = System.currentTimeMillis();
        this.lastSystemMs = System.nanoTime() / (1000 * 1000);
        return this.lastServerMs;
    }

    /**
     * 与えられたユーザIDとパスワードでログイン処理を行う。
     * @param userID ユーザID
     * @param password パスワード
     * @return ログインに成功すればtrue
     * @throws java.io.IOException ネットワークエラー
     */
    public final boolean login(String userID, char[] password)
            throws IOException{
        if(this.authManager.hasLoggedIn()){
            return true;
        }

        String postText = AuthManager.buildLoginPostData(userID, password);
        boolean result;
        try{
            result = postAuthData(postText);
        }catch(IOException e){
            this.authManager.clearAuthentication();
            throw e;
        }

        return result;
    }

    /**
     * ログアウト処理を行う。
     * @throws java.io.IOException ネットワーク入出力エラー
     */
    public void logout() throws IOException{
        if( ! this.authManager.hasLoggedIn() ){
            return;
        }

        try{
            postAuthData(AuthManager.POST_LOGOUT);
        }finally{
            this.authManager.clearAuthentication();
        }

        return;
    }
    // TODO シャットダウンフックでログアウトさせようかな…

    /**
     * ログイン中か否か判定する。
     * @return ログイン中ならtrue
     */
    public boolean hasLoggedIn(){
        boolean result = this.authManager.hasLoggedIn();
        return result;
    }

}
