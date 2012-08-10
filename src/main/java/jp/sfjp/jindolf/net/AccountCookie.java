/*
 * account cookie
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.net;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 人狼BBSアカウント管理用のCookie。
 * <p>JRE1.6 java.net.HttpCookie の代用品。
 * <p>人狼BBSではCookieヘッダに"Set-Cookie"が用いられる。
 * (Set-Cookie2ではない)
 * <p>人狼BBSではCookieの寿命管理に"Expires"が用いられる。(Max-Ageではない)
 * <p>人狼BBSではセッション管理のCookie名に"login"が用いられる。
 * @see <a href="http://www.ietf.org/rfc/rfc6265.txt">RFC6265</a>
 */
class AccountCookie{          // TODO JRE 1.6対応とともにHttpCookieへ移行予定

    private static final String HEADER_COOKIE = "Set-Cookie";
    private static final String SEPARATOR = ";";
    private static final String COOKIE_PATH = "Path";
    private static final String COOKIE_EXPIRES = "Expires";
    private static final String BBS_IDENTITY = "login";


    private final String loginData;
    private final URI pathURI;
    private final long expireDate;

    /**
     * 認証クッキーの生成。
     * @param loginData 認証データ
     * @param path Cookieパス
     * @param expireDate expire日付
     * @throws java.lang.NullPointerException 引数がnull
     * @throws java.lang.IllegalArgumentException パスが変
     */
    public AccountCookie(String loginData, String path, long expireDate)
            throws NullPointerException, IllegalArgumentException{
        super();

        if(loginData == null || path == null){
            throw new NullPointerException();
        }

        this.loginData = loginData;
        try{
            this.pathURI = new URI(path);
        }catch(URISyntaxException e){
            throw new IllegalArgumentException(path, e);
        }
        this.expireDate = expireDate;

        return;
    }

    /**
     * 文字列両端に連続するWSP(空白もしくはタブ)を取り除く。
     * @param txt テキスト
     * @return 取り除いた結果。引数がnullならnull
     */
    static String chopWsp(String txt){
        if(txt == null) return null;

        int startPt = -1;
        int endPt   = -1;

        int len = txt.length();
        for(int idx = 0; idx < len; idx++){
            char ch = txt.charAt(idx);
            if(ch != '\u0020' &&  ch != '\t'){
                if(startPt < 0) startPt = idx;
                endPt = idx + 1;
            }
        }

        if(startPt < 0) startPt = 0;
        if(endPt   < 0) endPt   = 0;

        String result = txt.substring(startPt, endPt);

        return result;
    }

    /**
     * Cookie属性を名前と値に分割する。
     * '='が無い属性(例:Secure)は値がnullになり、=付き空文字列とは区別される。
     * 余分なWSP空白はトリミングされる。
     * @param pair '='を挟む名前と値のペア。
     * @return [0]名前 [1]値 ※空文字列の名前およびnullな値がありうる。
     */
    static String[] splitPair(String pair){
        String[] result = new String[2];

        String[] split = pair.split("=", 2);

        if(split.length >= 1){
            result[0] = chopWsp(split[0]);
        }

        if(split.length >= 2){
            result[1] = chopWsp(split[1]);
        }

        return result;
    }

    /**
     * 人狼BBS用ログインデータを抽出する。
     * Cookie名は"login"。
     * @param nameValue '='で区切られたCookie name-value構造
     * @return 人狼BBS用ログインデータ。見つからなければnull
     */
    private static String parseLoginData(String nameValue){
        String[] nvPair = splitPair(nameValue);
        String name  = nvPair[0];
        String value = nvPair[1];
        if(name.length() <= 0) return null;  // 名前なし
        if(value == null) return null;       // '=' なし

        if( ! BBS_IDENTITY.equals(name) ) return null;

        return value;
    }

    /**
     * 認証Cookieを抽出する。
     * @param cookieSource HTTPヘッダ 「Set-Cookie」の値
     * @return 認証Cookie
     */
    public static AccountCookie createCookie(String cookieSource){
        String[] cookiePart = cookieSource.split(SEPARATOR);

        String login = parseLoginData(cookiePart[0]);
        String path = null;
        String expires = null;

        int partNo = cookiePart.length;
        for(int idx = 0 + 1; idx < partNo; idx++){
            String pair = cookiePart[idx];
            String[] attr = splitPair(pair);
            String attrName  = attr[0];
            String attrValue = attr[1];

            if(COOKIE_PATH.equalsIgnoreCase(attrName)){
                path = attrValue;
            }else if(COOKIE_EXPIRES.equalsIgnoreCase(attrName)){
                expires = attrValue;
            }
        }

        if(login   == null) return null;
        if(path    == null) return null;
        if(expires == null) return null;

        long expTime = CookieDateParser.parseToEpoch(expires);
        if(expTime < 0L) return null;

        AccountCookie cookie = new AccountCookie(login, path, expTime);

        return cookie;
    }

    /**
     * 認証Cookieを抽出する。
     * @param connection HTTP接続
     * @return 認証Cookie
     */
    public static AccountCookie createCookie(HttpURLConnection connection){
        String cookieHeader = connection.getHeaderField(HEADER_COOKIE);
        if(cookieHeader == null) return null;
        AccountCookie cookie = createCookie(cookieHeader);
        return cookie;
    }

    /**
     * 認証データを返す。
     * 取り扱い注意！人狼BBSでは機密情報だよ！
     * @return 認証データ
     */
    public String getLoginData(){
        return this.loginData;
    }

    /**
     * Cookieパスを返す。
     * @return Cookieパス
     */
    public URI getPathURI(){
        return this.pathURI;
    }

    /**
     * Cookie期限をエポック時刻で返す。
     * @return Cookieが期限切れを起こす日時。(msec)
     */
    public long getExpiredTime(){
        return this.expireDate;
    }

    /**
     * Cookie期限が切れてないか判定する。
     * @param nowMs 比較時刻(msec)
     * @return 期限が切れていたらtrue
     */
    public boolean hasExpired(long nowMs){
        long expireMs = getExpiredTime();
        if(expireMs < nowMs) return true;
        return false;
    }

    /**
     * 現時点でCookie期限が切れてないか判定する。
     * @return 期限が切れていたらtrue
     */
    public boolean hasExpired(){
        long nowMs = System.currentTimeMillis();
        boolean result = hasExpired(nowMs);
        return result;
    }

}
