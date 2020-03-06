/*
 * HTTP utilities
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.sfjp.jindolf.VerInfo;
import jp.sfjp.jindolf.config.EnvInfo;

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

    private static final String THROUGHPUT_FORM =
            "{0,number,#,##0}Bytes {1,number,#,##0.0}{2}Bytes/sec";
    private static final String HTTP_FORM = "{0} {1} [{2} {3}] {4}";


    /**
     * 隠れコンストラクタ。
     */
    private HttpUtils(){
        super();
        assert false;
        throw new AssertionError();
    }


    /**
     * ネットワークのスループット報告用文字列を生成する。
     *
     * @param size 転送サイズ(バイト数)
     * @param nano 所要時間(ナノ秒)
     * @return スループット文字列。引数のいずれかが0以下なら空文字列
     */
    public static String throughput(long size, long nano){
        if(size <= 0 || nano <= 0) return "";

        double sec = ((double) nano) / (1000.0 * 1000.0 * 1000.0);
        double rate = ((double) size) / sec;

        String unit = "";
        if(rate >= 1500.0){
            rate /= 1000.0;
            unit = "K";
        }
        if(rate >= 1500.0){
            rate /= 1000.0;
            unit = "M";
        }

        String result =
                  MessageFormat.format(THROUGHPUT_FORM, size, rate, unit);

        return result;
    }

    /**
     * HTTPセッションの各種結果を文字列化する。
     *
     * @param conn HTTPコネクション
     * @param size 転送サイズ
     * @param nano 転送に要したナノ秒
     * @return セッション結果文字列。
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

        String result;
        result = MessageFormat.format(HTTP_FORM,
                                      method, url,
                                      responseCode, responseMessage,
                                      throughput );

        return result;
    }

    /**
     * HTTP UserAgent名を返す。
     *
     * @return UserAgent名
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.3">
     * User-Agent</a>
     */
    public static String getUserAgentName(){
        StringBuilder result = new StringBuilder();
        result.append(VerInfo.TITLE).append('/').append(VerInfo.VERSION);

        StringBuilder rawComment = new StringBuilder();
        Arrays.asList(
            EnvInfo.OS_NAME,
            EnvInfo.OS_VERSION,
            EnvInfo.OS_ARCH,
            EnvInfo.JAVA_VENDOR,
            EnvInfo.JAVA_VERSION
        ).stream().forEach(info -> {
            if(rawComment.length() > 0) rawComment.append(";\u0020");
            rawComment.append(info);
        });

        if(rawComment.length() > 0){
            CharSequence comment = escapeHttpComment(rawComment);
            result.append('\u0020').append(comment);
        }

        return result.toString();
    }

    /**
     * 与えられた文字列からHTTPコメントを生成する。
     *
     * @param comment コメント
     * @return HTTPコメント
     */
    public static String escapeHttpComment(CharSequence comment){
        String result = comment.toString();

        result = result.replaceAll("\\(", "\\\\(");
        result = result.replaceAll("\\)", "\\\\)");
        result = result.replaceAll("[\\u0000-\\u001f]", "?");
        result = result.replaceAll("[\\u007f-\\x{10ffff}]", "?");
        result = "(" + result + ")";

        return result;
    }

    /**
     * HTTP応答からCharsetを取得する。
     *
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
     *
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

}
