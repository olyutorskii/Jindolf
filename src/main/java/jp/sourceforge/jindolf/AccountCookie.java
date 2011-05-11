/*
 * account cookie
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 人狼BBSアカウント管理用のCookie。
 * JRE1.6 HttpCookie の代用品。
 */
class AccountCookie{          // TODO JRE 1.6対応とともにHttpCookieへ移行予定

    // 人狼BBSのCookie期限表記例： 「Thu, 26 Jun 2008 06:44:34 GMT」
    private static final String DATE_FORM = "EEE, dd MMM yyyy HH:mm:ss z";
    private static final SimpleDateFormat FORMAT;

    static{
        Calendar calendar = new GregorianCalendar();
        TimeZone zoneGMT = TimeZone.getTimeZone("GMT");
        DateFormatSymbols customSyms = new DateFormatSymbols();
        String[] sweekdays = customSyms.getShortWeekdays();
        sweekdays[Calendar.SUNDAY] = "Sun";
        sweekdays[Calendar.MONDAY] = "Mon";
        sweekdays[Calendar.TUESDAY] = "Tue";
        sweekdays[Calendar.WEDNESDAY] = "Wed";
        sweekdays[Calendar.THURSDAY] = "Thu";
        sweekdays[Calendar.FRIDAY] = "Fri";
        sweekdays[Calendar.SATURDAY] = "Sat";
        customSyms.setShortWeekdays(sweekdays);
        String[] months = customSyms.getShortMonths();
        months[Calendar.JANUARY] = "Jan";
        months[Calendar.FEBRUARY] = "Feb";
        months[Calendar.MARCH] = "Mar";
        months[Calendar.APRIL] = "Apr";
        months[Calendar.MAY] = "May";
        months[Calendar.JUNE] = "Jun";
        months[Calendar.JULY] = "Jul";
        months[Calendar.AUGUST] = "Aug";
        months[Calendar.SEPTEMBER] = "Sep";
        months[Calendar.OCTOBER] = "Oct";
        months[Calendar.NOVEMBER] = "Nov";
        months[Calendar.DECEMBER] = "Dec";
        customSyms.setShortMonths(months);

        FORMAT = new SimpleDateFormat(DATE_FORM, Locale.JAPAN);
        FORMAT.setCalendar(calendar);
        FORMAT.setTimeZone(zoneGMT);
        FORMAT.setDateFormatSymbols(customSyms);
        FORMAT.setLenient(true);
    }

    private final String loginData;
    private final URI pathURI;
    private final Date expireDate;

    /**
     * 認証クッキーの生成。
     * @param loginData 認証データ
     * @param path Cookieパス
     * @param expireDate expire日付
     * @throws java.lang.NullPointerException 引数がnull
     * @throws java.lang.IllegalArgumentException パスが変
     */
    public AccountCookie(String loginData, String path, Date expireDate)
            throws NullPointerException, IllegalArgumentException{
        super();

        if(loginData == null || path == null || expireDate == null){
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
     * Cookie期限が切れてないか判定する。
     * @return 期限が切れていたらtrue
     */
    public boolean hasExpired(){
        long nowMs = System.currentTimeMillis();
        long expireMs = this.expireDate.getTime();
        if(expireMs < nowMs) return true;
        return false;
    }

    /**
     * Cookieパスを返す。
     * @return Cookieパス
     */
    public URI getPathURI(){
        return this.pathURI;
    }

    /**
     * 認証データを返す。
     * @return 認証データ
     */
    public String getLoginData(){
        return this.loginData;
    }

    /**
     * 認証Cookieを抽出する。
     * @param cookieSource HTTPヘッダ 「Cookie=」の値
     * @return 認証Cookie
     */
    public static AccountCookie createCookie(String cookieSource){
        String[] cookieParts = cookieSource.split("; ");
        if(cookieParts.length <= 0) return null;

        String login = null;
        String path = null;
        String expires = null;
        for(String part : cookieParts){
            String[] nmval = part.split("=", 2);
            if(nmval == null) continue;
            if(nmval.length != 2) continue;
            String name = nmval[0];
            String value = nmval[1];

            if(name.equals("login")){
                login = value;
            }else if(name.equals("path")){
                path = value;
            }else if(name.equals("expires")){
                expires = value;
            }
        }
        if(login == null || path == null || expires == null) return null;

        Date date;
        try{
            date = FORMAT.parse(expires);
        }catch(ParseException e){
            return null;
        }

        AccountCookie cookie = new AccountCookie(login, path, date);

        return cookie;
    }

    /**
     * 認証Cookieを抽出する。
     * @param connection HTTP接続
     * @return 認証Cookie
     */
    public static AccountCookie createCookie(HttpURLConnection connection){
        String cookieHeader = connection.getHeaderField("Set-Cookie");
        if(cookieHeader == null) return null;
        AccountCookie cookie = createCookie(cookieHeader);
        return cookie;
    }

    /**
     * 認証Cookieの文字列表記。
     * @return String文字列
     */
    @Override
    public String toString(){
        return this.loginData;
    }

}
