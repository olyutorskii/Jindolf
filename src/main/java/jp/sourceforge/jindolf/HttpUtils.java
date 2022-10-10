/*
 * HTTP utilities
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: HttpUtils.java 953 2009-12-06 16:42:14Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP関連のユーティリティ群。
 */
public final class HttpUtils{

    private static final String TOKEN_REGEX =
            "([^\\(\\)<>@,;:\\\"/\\[\\]\\?=\\{\\}\\p{Blank}\\p{Cntrl}]+)";
    private static final String MTYPE_REGEX =
            "[\\p{Blank}]*"
            + TOKEN_REGEX + "/" + TOKEN_REGEX
            + "[\\p{Blank}]*";
    private static final String PARAM_REGEX =
            "[\\p{Blank}]*;[\\p{Blank}]*"
            + TOKEN_REGEX
            + "[\\p{Blank}]*=[\\p{Blank}]*"
            + "(" + TOKEN_REGEX + "|" + "(\"[^\\p{Cntrl}\\\"]*\")" + ")";
    private static final Pattern MTYPE_PATTERN = Pattern.compile(MTYPE_REGEX);
    private static final Pattern ATTR_PATTERN  = Pattern.compile(PARAM_REGEX);

    private static final NumberFormat THROUGHPUT_FORMAT;
    private static final NumberFormat SIZE_FORMAT;

    static{
        THROUGHPUT_FORMAT = NumberFormat.getInstance();
        THROUGHPUT_FORMAT.setMaximumFractionDigits(1);
        THROUGHPUT_FORMAT.setMinimumFractionDigits(1);
        THROUGHPUT_FORMAT.setGroupingUsed(true);
        SIZE_FORMAT = NumberFormat.getInstance();
        SIZE_FORMAT.setGroupingUsed(true);
    }

    /**
     * ネットワークのスループット報告用文字列を生成する。
     * @param size 転送サイズ(バイト数)
     * @param nano 所要時間(ナノ秒)
     * @return スループット文字列
     */
    public static String throughput(long size, long nano){
        if(size <= 0 || nano <= 0) return "";

        double sec = ((double)nano) / (1000.0 * 1000.0 * 1000.0);
        double rate = ((double)size) / sec;

        String unit = "";
        if(rate >= 1500.0){
            rate /= 1000.0;
            unit = "K";
        }
        if(rate >= 1500.0){
            rate /= 1000.0;
            unit = "M";
        }

        String result =  SIZE_FORMAT.format(size) + "Bytes "
                       + THROUGHPUT_FORMAT.format(rate) + unit
                       + "Bytes/sec";
        return result;
    }

    /**
     * HTTPセッションの各種結果を文字列化する。
     * @param conn HTTPコネクション
     * @param size 転送サイズ
     * @param nano 転送に要したナノ秒
     * @return セッション結果
     */
    public static String formatHttpStat(HttpURLConnection conn,
                                          long size,
                                          long nano ){
        String method = conn.getRequestMethod();
        String url    = conn.getURL().toString();

        String responseCode;
        try{
            responseCode = String.valueOf(conn.getResponseCode());
        }catch(IOException e){
            responseCode = "???";
        }

        String responseMessage;
        try{
            responseMessage = conn.getResponseMessage();
        }catch(IOException e){
            responseMessage = "???";
        }

        String throughput = throughput(size, nano);

        String message =  method
                        + " " + url
                        + " [" + responseCode
                        + " " + responseMessage + "]"
                        + " " + throughput;

        return message;
    }

    /**
     * ユーザエージェント名を返す。
     * @return ユーザエージェント名
     */
    public static String getUserAgentName(){
        StringBuilder result = new StringBuilder();
        result.append(Jindolf.TITLE).append("/").append(Jindolf.VERSION);

        StringBuilder rawComment = new StringBuilder();
        if(EnvInfo.OS_NAME != null){
            if(rawComment.length() > 0) rawComment.append("; ");
            rawComment.append(EnvInfo.OS_NAME);
        }
        if(EnvInfo.OS_VERSION != null){
            if(rawComment.length() > 0) rawComment.append("; ");
            rawComment.append(EnvInfo.OS_VERSION);
        }
        if(EnvInfo.OS_ARCH != null){
            if(rawComment.length() > 0) rawComment.append("; ");
            rawComment.append(EnvInfo.OS_ARCH);
        }
        if(EnvInfo.JAVA_VENDOR != null){
            if(rawComment.length() > 0) rawComment.append("; ");
            rawComment.append(EnvInfo.JAVA_VENDOR);
        }
        if(EnvInfo.JAVA_VERSION != null){
            if(rawComment.length() > 0) rawComment.append("; ");
            rawComment.append(EnvInfo.JAVA_VERSION);
        }

        CharSequence comment = escapeHttpComment(rawComment);
        if(comment != null) result.append(" ").append(comment);

        return result.toString();
    }

    /**
     * 与えられた文字列からHTTPコメントを生成する。
     * @param comment コメント
     * @return HTTPコメント
     */
    public static String escapeHttpComment(CharSequence comment){
        if(comment == null) return null;
        if(comment.length() <= 0) return null;

        String result = comment.toString();
        result = result.replaceAll("\\(", "\\\\(");
        result = result.replaceAll("\\)", "\\\\)");
        result = result.replaceAll("[\\u0000-\\u001f]", "?");
        result = result.replaceAll("[\\u007f-\\uffff]", "?");
        result = "(" + result + ")";

        return result;
    }

    /**
     * HTTP応答からCharsetを取得する。
     * @param connection HTTP接続
     * @return Charset文字列
     */
    public static String getHTMLCharset(URLConnection connection){
        String contentType = connection.getContentType();
        if(contentType == null) return null;
        return getHTMLCharset(contentType);
    }

    /**
     * ContentTypeからCharsetを取得する。
     * @param contentType ContentType
     * @return Charset文字列
     */
    public static String getHTMLCharset(String contentType){
        Matcher matcher;
        boolean matchResult;
        int lastPos;

        matcher = MTYPE_PATTERN.matcher(contentType);
        matchResult = matcher.lookingAt();
        if(!matchResult) return null;
        lastPos = matcher.end();
        String type = matcher.group(1);
        String subtype = matcher.group(2);

        if(!type.equalsIgnoreCase("text")) return null;
        if(!subtype.equalsIgnoreCase("html")) return null;

        matcher.usePattern(ATTR_PATTERN);

        String charset = null;
        for(;;){
            matchResult = matcher.find(lastPos);
            if(!matchResult) break;
            lastPos = matcher.end();
            String attribute = matcher.group(1);
            String value = matcher.group(2);
            if(attribute.equalsIgnoreCase("charset")) charset = value;
        }
        return charset;
    }

    /**
     * 隠れコンストラクタ。
     */
    private HttpUtils(){
        super();
        assert false;
        throw new AssertionError();
    }

}
