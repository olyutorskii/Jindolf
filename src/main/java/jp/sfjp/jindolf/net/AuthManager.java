/*
 * manage authentification info
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.net;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Cookieを用いた人狼BBSサーバとの認証管理を行う。
 *
 * <p>2012-10現在、サポートするのはG国のみ。
 *
 * <p>2012-10より、Cookie "uniqID" の送出も必要になった模様。
 */
public class AuthManager{

    /** ログアウト用のPOSTデータ。 */
    public static final String POST_LOGOUT;

    private static final String COOKIE_LOGIN = "login";
    private static final String ENC_POST = "UTF-8";
    private static final String PARAM_REDIR;

    private static final CookieManager COOKIE_MANAGER;

    static{
        PARAM_REDIR = "cgi_param=" + encodeForm4Post("&#bottom");
        POST_LOGOUT = "cmd=logout" + '&' + PARAM_REDIR;

        COOKIE_MANAGER = new CookieManager();
        CookieHandler.setDefault(COOKIE_MANAGER);
    }


    private final URI baseURI;


    /**
     * コンストラクタ。
     * @param bbsUri 人狼BBSサーバURI
     * @throws NullPointerException 引数がnull
     */
    public AuthManager(URI bbsUri) throws NullPointerException{
        if(bbsUri == null) throw new NullPointerException();
        this.baseURI = bbsUri;
        return;
    }

    /**
     * コンストラクタ。
     * @param bbsUrl 人狼BBSサーバURL
     * @throws NullPointerException 引数がnull
     * @throws IllegalArgumentException 不正な書式
     */
    public AuthManager(URL bbsUrl)
            throws NullPointerException, IllegalArgumentException{
        this(urlToUri(bbsUrl));
        return;
    }


    /**
     * URLからURIへ変換する。
     * 書式に関する例外は非チェック例外へ変換される。
     * @param url URL
     * @return URI
     * @throws NullPointerException 引数がnull
     * @throws IllegalArgumentException 不正な書式
     */
    private static URI urlToUri(URL url)
            throws NullPointerException, IllegalArgumentException{
        if(url == null) throw new NullPointerException();

        URI uri;
        try{
            uri = url.toURI();
        }catch(URISyntaxException e){
            throw new IllegalArgumentException(e);
        }

        return uri;
    }

    /**
     * 与えられた文字列に対し
     * 「application/x-www-form-urlencoded」符号化を行う。
     *
     * <p>この符号化はHTTPのPOSTメソッドで必要になる。
     * この処理は、一般的なPC用Webブラウザにおける、
     * HTML文書のFORMタグに伴うsubmit処理を模倣する。
     *
     * <p>生成文字列はUS-ASCIIの範疇に収まる。はず。
     *
     * @param formData 元の文字列
     * @return 符号化された文字列
     * @see java.net.URLEncoder
     * @see
     * <a href="http://tools.ietf.org/html/rfc1866#section-8.2.1">
     * RFC1866 8.2.1
     * </a>
     */
    public static String encodeForm4Post(String formData){
        if(formData == null){
            return null;
        }

        String result;
        try{
            result = URLEncoder.encode(formData, ENC_POST);
        }catch(UnsupportedEncodingException e){
            assert false;
            result = null;
        }

        return result;
    }

    /**
     * 配列版{@link #encodeForm4Post(java.lang.String)}。
     * @param formData 元の文字列
     * @return 符号化された文字列
     * @see #encodeForm4Post(java.lang.String)
     */
    public static String encodeForm4Post(char[] formData){
        return encodeForm4Post(new String(formData));
    }

    /**
     * ログイン用POSTデータを生成する。
     * @param userID 人狼BBSアカウント名
     * @param password パスワード
     * @return POSTデータ
     */
    public static String buildLoginPostData(String userID, char[] password){
        String id = encodeForm4Post(userID);
        if(id == null || id.isEmpty()){
            return null;
        }

        String pw = encodeForm4Post(password);
        if(pw == null || pw.isEmpty()){
            return null;
        }

        StringBuilder postData = new StringBuilder();
        postData.append("cmd=login");
        postData.append('&').append(PARAM_REDIR);
        postData.append('&').append("user_id=").append(id);
        postData.append('&').append("password=").append(pw);

        String result = postData.toString();
        return result;
    }


    /**
     * 人狼BBSサーバのベースURIを返す。
     * @return ベースURI
     */
    public URI getBaseURI(){
        return this.baseURI;
    }

    /**
     * 人狼BBSサーバ管轄下の全Cookieを列挙する。
     * 他サーバ由来のCookie群との区別はCookieドメイン名で判断される。
     * @param cookieStore Cookie記憶域
     * @return 人狼BBSサーバ管轄下のCookieのリスト
     */
    public List<HttpCookie> getCookieList(CookieStore cookieStore){
        List<HttpCookie> cookieList = cookieStore.get(this.baseURI);
        return cookieList;
    }

    /**
     * 認証Cookieを取得する。
     *
     * <p>G国での認証Cookie名は"login"。
     *
     * <p>※ 2012-10より"uniqID"も増えた模様だが判定には使わない。
     *
     * @param cookieStore Cookie記憶域
     * @return 認証Cookie。認証された状態に無いときはnull。
     */
    public HttpCookie getAuthCookie(CookieStore cookieStore){
        List<HttpCookie> cookieList = getCookieList(cookieStore);
        for(HttpCookie cookie : cookieList){
            String cookieName = cookie.getName();
            if(COOKIE_LOGIN.equals(cookieName)) return cookie;
        }

        return null;
    }

    /**
     * 現在ログイン中か否か判別する。
     * @return ログイン中ならtrue
     */
    public boolean hasLoggedIn(){
        assert COOKIE_MANAGER == CookieHandler.getDefault();
        CookieStore cookieStore = COOKIE_MANAGER.getCookieStore();

        HttpCookie authCookie = getAuthCookie(cookieStore);
        if(authCookie == null){
            clearAuthentication();
            return false;
        }

        return true;
    }

    /**
     * 認証情報をクリアする。
     */
    public void clearAuthentication(){
        assert COOKIE_MANAGER == CookieHandler.getDefault();
        CookieStore cookieStore = COOKIE_MANAGER.getCookieStore();

        HttpCookie authCookie = getAuthCookie(cookieStore);
        if(authCookie != null){
            cookieStore.remove(this.baseURI, authCookie);
        }

        return;
    }

}
