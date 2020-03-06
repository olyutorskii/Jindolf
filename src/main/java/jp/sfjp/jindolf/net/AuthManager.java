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
import java.util.Objects;

/**
 * Cookieを用いた人狼BBSサーバとの認証管理を行う。
 *
 * <p>Cookie管理はVM内で共有される。
 *
 * <p>2012-10現在、サポートするのはG国のみ。
 *
 * <p>2012-10より、Cookie "uniqID" の送出も必要になった模様。
 *
 * <p>2020-02現在、認証が必要なHTTP通信は発生しない。
 */
class AuthManager{

    /** ログアウト用のPOSTデータ。 */
    static final String POST_LOGOUT;

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
     *
     * @param bbsUri 人狼BBSサーバURI
     * @throws NullPointerException 引数がnull
     */
    AuthManager(URI bbsUri){
        Objects.nonNull(bbsUri);
        this.baseURI = bbsUri;
        return;
    }

    /**
     * コンストラクタ。
     *
     * @param bbsUrl 人狼BBSサーバURL
     * @throws NullPointerException 引数がnull
     * @throws IllegalArgumentException 不正なURL書式
     */
    AuthManager(URL bbsUrl)
            throws IllegalArgumentException{
        this(urlToUri(bbsUrl));
        return;
    }


    /**
     * URLからURIへ変換する。
     *
     * <p>書式に関する例外は
     * 非チェック例外{@link IllegalArgumentException}へ変換される。
     *
     * @param url URL
     * @return URI
     * @throws NullPointerException 引数がnull
     * @throws IllegalArgumentException 不正な書式
     */
    private static URI urlToUri(URL url)
            throws IllegalArgumentException{
        Objects.nonNull(url);

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
    protected static String encodeForm4Post(String formData){
        Objects.nonNull(formData);

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
     *
     * @param formData 元の文字列
     * @return 符号化された文字列
     * @see #encodeForm4Post(java.lang.String)
     */
    protected static String encodeForm4Post(char[] formData){
        String txt = new String(formData);
        String result = encodeForm4Post(txt);
        return result;
    }

    /**
     * ログイン用POSTデータを生成する。
     *
     * @param userID 人狼BBSアカウント名
     * @param password パスワード
     * @return POSTデータ
     */
    public static String buildLoginPostData(String userID, char[] password){
        Objects.nonNull(userID);
        Objects.nonNull(password);

        String id = encodeForm4Post(userID);
        String pw = encodeForm4Post(password);

        StringBuilder postData = new StringBuilder();
        postData.append("cmd=login");
        postData.append('&').append(PARAM_REDIR);
        postData.append('&').append("user_id=").append(id);
        postData.append('&').append("password=").append(pw);

        String result = postData.toString();
        return result;
    }


    /**
     * このサーバの認証Cookieを取得する。
     *
     * <p>G国での認証Cookie名は"login"。
     *
     * <p>※ 2012-10より"uniqID"も増えた模様だが判定には使わない。
     *
     * @param cookieStore Cookie記憶域
     * @return 認証Cookie。認証された状態に無いときはnull。
     */
    protected HttpCookie getAuthCookie(CookieStore cookieStore){
        List<HttpCookie> cookieList = cookieStore.get(this.baseURI);
        for(HttpCookie cookie : cookieList){
            String cookieName = cookie.getName();
            if(COOKIE_LOGIN.equals(cookieName)) return cookie;
        }

        return null;
    }

    /**
     * このサーバに現在ログイン中か否か認証状態により判別する。
     *
     * @return ログイン中ならtrue
     */
    boolean hasLoggedIn(){
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
     * このサーバの認証情報をクリアする。
     *
     * <p>以降、BBSサーバへ認証情報は送られなくなる。
     *
     * <p>ログアウトと同じ意味。
     */
    void clearAuthentication(){
        assert COOKIE_MANAGER == CookieHandler.getDefault();
        CookieStore cookieStore = COOKIE_MANAGER.getCookieStore();

        HttpCookie authCookie = getAuthCookie(cookieStore);
        if(authCookie != null){
            cookieStore.remove(this.baseURI, authCookie);
        }

        return;
    }

}
