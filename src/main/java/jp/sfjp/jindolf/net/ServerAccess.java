/*
 * manage HTTP access
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.net;

import io.bitbucket.olyutorskii.jiocema.DecodeBreakException;
import io.bitbucket.olyutorskii.jiocema.DecodeNotifier;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import jp.osdn.jindolf.parser.content.ContentBuilder;
import jp.osdn.jindolf.parser.content.ContentBuilderSJ;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.osdn.jindolf.parser.content.SjisNotifier;
import jp.sfjp.jindolf.data.Period;
import jp.sfjp.jindolf.data.Village;

/**
 * 国ごとの人狼BBSサーバとの通信を一手に引き受ける。
 *
 * <p>受信対象はHTMLと各種アイコンJPEG画像。
 *
 * <p>プロクシサーバを介したHTTP通信を管理する。
 *
 * <p>国ごとに文字コードを保持し、HTML文書のデコードに用いられる。
 *
 * <p>画像(40種強)はキャッシュ管理が行われる。
 *
 * <p>※ 2020-02現在、進行中の村は存在しないゆえ、
 * Cookie認証処理は削除された。
 *
 * <p>最後にHTTP受信が行われた時刻を保持する。
 */
public class ServerAccess{

    private static final int BUFLEN_CONTENT = 200 * 1024;

    private static final String USER_AGENT = HttpUtils.getUserAgentName();
    private static final String JINRO_CGI = "./index.rb";

    private static final
            Map<String, SoftReference<BufferedImage>> IMAGE_CACHE;
    private static final Object CACHE_LOCK = new Object();

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    static{
        IMAGE_CACHE = new HashMap<>();
    }


    private final URL baseURL;

    private final Charset charset;
    private final boolean isSJIS;
    private final boolean isUTF8;

    private Proxy proxy = Proxy.NO_PROXY;

    private long lastServerMs;
    private long lastLocalMs;
    private long lastSystemMs;


    /**
     * 人狼BBSサーバとの接続管理を生成する。
     *
     * <p>この時点ではまだ通信は行われない。
     *
     * @param baseURL 国別のベースURL
     * @param charset 国のCharset
     * @throws IllegalArgumentException 不正なURL
     */
    public ServerAccess(URL baseURL, Charset charset)
            throws IllegalArgumentException{
        super();

        this.baseURL = baseURL;
        this.charset = charset;

        String charsetName = this.charset.name();
        if("Shift_JIS".equalsIgnoreCase(charsetName)){
            this.isSJIS = true;
            this.isUTF8 = false;
        }else if("UTF-8".equalsIgnoreCase(charsetName)){
            this.isSJIS = false;
            this.isUTF8 = true;
        }else{
            throw new IllegalArgumentException(charsetName);
        }
        assert this.isSJIS ^ this.isUTF8;

        return;
    }


    /**
     * 画像キャッシュを検索する。
     *
     * <p>キーは画像URL文字列。
     *
     * <p>ソフト参照オブジェクトの解放などにより、
     * キャッシュの状況は変化する。
     *
     * @param key キー
     * @return キャッシュされた画像。キャッシュされていなければnull。
     */
    private static BufferedImage getImageCache(String key){
        if(key == null) return null;

        BufferedImage image;

        // atomic get and remove
        synchronized(CACHE_LOCK){
            SoftReference<BufferedImage> ref = IMAGE_CACHE.get(key);
            if(ref == null) return null;

            image = ref.get();
            if(image == null){
                IMAGE_CACHE.remove(key);
            }
        }

        return image;
    }

    /**
     * キャッシュに画像を登録する。
     *
     * <p>キーは画像URL文字列。
     *
     * @param key キー
     * @param image キャッシュしたい画像。
     */
    private static void putImageCache(String key, BufferedImage image){
        if(key == null || image == null) return;

        // atomic get and put
        synchronized(CACHE_LOCK){
            if(getImageCache(key) != null) return;
            SoftReference<BufferedImage> ref =
                    new SoftReference<>(image);
            IMAGE_CACHE.put(key, ref);
        }

        return;
    }


    /**
     * HTTP通信に使われるProxyを返す。
     *
     * @return HTTP-Proxy
     */
    public Proxy getProxy(){
        return this.proxy;
    }

    /**
     * HTTP通信に使われるProxyを設定する。
     *
     * @param proxy HTTP-Proxy。nullならProxyなしと解釈される。
     */
    public void setProxy(Proxy proxy){
        if(proxy == null) this.proxy = Proxy.NO_PROXY;
        else              this.proxy = proxy;
        return;
    }

    /**
     * 国のベースURLを返す。
     *
     * @return ベースURL
     */
    public URL getBaseURL(){
        return this.baseURL;
    }

    /**
     * 与えられたクエリーとCGIのURLから新たにURLを合成する。
     *
     * @param query ?から始まるクエリー
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
     * 指定された村の最新PeriodのHTMLデータのURLを取得する。
     *
     * @param village 村
     * @return URL
     */
    public URL getVillageURL(Village village){
        String query = village.getCGIQuery();
        URL url = getQueryURL(query);
        return url;
    }

    /**
     * 指定されたPeriodのHTMLデータのURLを取得する。
     *
     * @param period 日
     * @return URL
     */
    public URL getPeriodURL(Period period){
        String query = period.getCGIQuery();
        URL url = getQueryURL(query);
        return url;
    }

    /**
     * エンコーディングされた入力ストリームからHTML文字列を受信する。
     *
     * @param istream 入力ストリーム
     * @return 文字列
     * @throws java.io.IOException 入出力エラー（おそらくネットワーク関連）
     */
    public DecodedContent downloadHTMLStream(InputStream istream)
            throws IOException{
        DecodeNotifier decoder;
        ContentBuilder builder;
        if(this.isSJIS){
            decoder = new SjisNotifier();
            builder = new ContentBuilderSJ(BUFLEN_CONTENT);
        }else if(this.isUTF8){
            decoder = new DecodeNotifier(this.charset.newDecoder());
            builder = new ContentBuilder(BUFLEN_CONTENT);
        }else{
            assert false;
            return null;
        }
        decoder.setCharDecodeListener(builder);

        // TODO デコーダをインスタンス変数にできないか。
        // TODO DecodedContentのキャッシュ管理。

        try{
            decoder.decode(istream);
        }catch(DecodeBreakException e){
            return null;
        }

        return builder.getContent();
    }

    /**
     * 与えられたURLを用いてHTMLデータを取得する。
     *
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

        DecodedContent html;
        try(InputStream is = TallyInputStream.getInputStream(connection)){
            html = downloadHTMLStream(is);
        }

        connection.disconnect();

        HtmlSequence hseq = new HtmlSequence(url, datems, html);

        return hseq;
    }

    /**
     * 与えられたクエリーを用いてHTMLデータを取得する。
     *
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
     * トップページのHTMLデータを取得する。
     *
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    public HtmlSequence getHTMLTopPage() throws IOException{
        return downloadHTML("");
    }

    /**
     * 国に含まれる村一覧HTMLデータを取得する。
     *
     * <p>wolf国には存在しない。
     *
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    public HtmlSequence getHTMLLandList() throws IOException{
        return downloadHTML("?cmd=log");
    }

    /**
     * 指定された村のPeriod一覧のHTMLデータを取得する。
     *
     * <p>現在ゲーム進行中の村にも可能。
     * ※ 古国では使えないよ！
     *
     * @param village 村
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    public HtmlSequence getHTMLBoneHead(Village village) throws IOException{
        String query = village.getCGIQuery();
        return downloadHTML(query + "&meslog=");
    }

    /**
     * 指定された村の最新PeriodのHTMLデータをロードする。
     *
     * <p>既にGAMEOVERの村ではPeriod一覧のHTMLデータとなる。
     *
     * @param village 村
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    public HtmlSequence getHTMLVillage(Village village) throws IOException{
        URL url = getVillageURL(village);
        return downloadHTML(url);
    }

    /**
     * 指定されたPeriodのHTMLデータをロードする。
     *
     * @param period Period
     * @return HTMLデータ
     * @throws java.io.IOException ネットワークエラー
     */
    public HtmlSequence getHTMLPeriod(Period period) throws IOException{
        URL url = getPeriodURL(period);
        return downloadHTML(url);
    }

    /**
     * 絶対または相対URLの指すパーマネントなイメージ画像をダウンロードする。
     *
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

        String urlTxt = absolute.toString();
        BufferedImage image;
        image = getImageCache(urlTxt);
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

        try(InputStream is = TallyInputStream.getInputStream(connection)){
            image = ImageIO.read(is);
        }

        connection.disconnect();

        putImageCache(urlTxt, image);

        return image;
    }

    /**
     * 最終アクセス時刻を更新する。
     *
     * @param connection HTTP接続
     * @return リソース送信時刻
     */
    public long updateLastAccess(HttpURLConnection connection){
        this.lastServerMs = connection.getDate();
        this.lastLocalMs = System.currentTimeMillis();
        this.lastSystemMs = System.nanoTime() / (1000 * 1000);
        return this.lastServerMs;
    }

}
