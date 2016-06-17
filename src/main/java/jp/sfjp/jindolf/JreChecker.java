/*
 * JRE checker
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf;

import java.awt.Frame;
import java.io.PrintStream;
import javax.swing.JOptionPane;

/**
 * JRE互換性のチェックを行う。
 * <p>JREのバージョンと、
 * JREに含まれるjava.langパッケージ仕様のバージョンは互換性があるものとする。
 * <p>なるべく昔のJREでも動くように。
 * 少なくとも1.2以降。できれば1.0以降。
 * 原則、1.3以降の新設ライブラリは利用禁止。
 * <p>なるべく昔のコンパイラでもコンパイルできるように。
 * 少なくとも1.2以降。できれば1.0以降。
 * 原則、assert文、総称ジェネリクス、アノテーションは禁止。
 * <p>できればBasic-Latinに入らない文字(日本語)の出力も禁止。
 * <p>ランタイム存在検査用のクラスは、ロードや初期化が軽いほうが望ましい。
 * そしていずれJindolfの実行に必要なものであるのが望ましい。
 * もしそうでない場合でも、
 * Jindolfに関係ないクラスの連鎖ロードが起きにくいほうが望ましい。
 */
public final class JreChecker {

    /** Jindolfが実行時に必要とするJREの版。 */
    public static final String REQUIRED_JRE_VER = "1.7";

    /** 互換性エラーの終了コード。 */
    public static final int EXIT_CODE_INCOMPAT_JRE = 1;

    private static final PrintStream STDERR = System.err;

    private static final String DIALOG_TITLE =
            "JRE Incompatibility detected...";

    private static final int MAX_LINE = 40;
    private static final int DEF_LINES = 5;
    private static final int INIT_SBUF = 100;


    /**
     * 隠しコンストラクタ。
     * <p><code>assert false;</code> 書きたいけど書いちゃだめ。
     */
    private JreChecker(){
        super();
        return;
    }


    /**
     * クラス名に相当するクラスがロードできるか判定する。
     * @param klassName FQDNなクラス名
     * @return ロードできたらtrue
     */
    public static boolean hasClass(String klassName){
        boolean result;

        try{
            Class.forName(klassName); // 1.2Laterな3引数版メソッドは利用禁止
            result = true;
        }catch(ClassNotFoundException e){
            result = false;
        }

        return result;
    }

    /**
     * JRE 1.1 相当のランタイムライブラリが提供されているか判定する。
     * @return 提供されているならtrue
     * @see java.io.Serializable
     */
    public static boolean has11Runtime(){
        boolean result = hasClass("java.io.Serializable");
        return result;
    }

    /**
     * JRE 1.2 相当のランタイムライブラリが提供されているか判定する。
     * @return 提供されているならtrue
     * @see java.util.Iterator
     */
    public static boolean has12Runtime(){
        boolean result;
        if(has11Runtime()) result = hasClass("java.util.Iterator");
        else               result = false;
        return result;
    }

    /**
     * JRE 1.3 相当のランタイムライブラリが提供されているか判定する。
     * @return 提供されているならtrue
     * @see java.util.TimerTask
     */
    public static boolean has13Runtime(){
        boolean result;
        if(has12Runtime()) result = hasClass("java.util.TimerTask");
        else               result = false;
        return result;
    }

    /**
     * JRE 1.4 相当のランタイムライブラリが提供されているか判定する。
     * @return 提供されているならtrue
     * @see java.lang.CharSequence
     */
    public static boolean has14Runtime(){
        boolean result;
        if(has13Runtime()) result = hasClass("java.lang.CharSequence");
        else               result = false;
        return result;
    }

    /**
     * JRE 1.5 相当のランタイムライブラリが提供されているか判定する。
     * @return 提供されているならtrue
     * @see java.lang.Appendable
     */
    public static boolean has15Runtime(){
        boolean result;
        if(has14Runtime()) result = hasClass("java.lang.Appendable");
        else               result = false;
        return result;
    }

    /**
     * JRE 1.6 相当のランタイムライブラリが提供されているか判定する。
     * @return 提供されているならtrue
     * @see java.util.Deque
     */
    public static boolean has16Runtime(){
        boolean result;
        if(has15Runtime()) result = hasClass("java.util.Deque");
        else               result = false;
        return result;
    }

    /**
     * JRE 1.7 相当のランタイムライブラリが提供されているか判定する。
     * @return 提供されているならtrue
     * @see java.lang.AutoCloseable
     */
    public static boolean has17Runtime(){
        boolean result;
        if(has16Runtime()) result = hasClass("java.lang.AutoCloseable");
        else               result = false;
        return result;
    }

    // TODO JRE1.8 対応

    /**
     * JREもしくは<code>java.lang</code>パッケージの
     * 仕様バージョンを返す。
     * <ol>
     * <li>システムプロパティ<code>java.specification.version</code>
     * <li>システムプロパティ<code>java.version</code>
     * <li><code>java.lang</code>パッケージの仕様バージョン
     * </ol>の順でバージョンが求められる。
     * @return 仕様バージョン文字列。不明ならnull
     */
    public static String getLangPkgSpec(){
        String result;

        try{
            result = System.getProperty("java.specification.version");
        }catch(SecurityException e){
            result = null;
        }
        if(result != null) return result;

        try{
            result = System.getProperty("java.version");
        }catch(SecurityException e){
            result = null;
        }
        if(result != null) return result;

        Package javaLangPkg = java.lang.Object.class.getPackage();
        if(javaLangPkg == null) return null;

        result = javaLangPkg.getSpecificationVersion();
        return result;
    }

    /**
     * JREのインストール情報を返す。
     * システムプロパティ<code>java.home</code>の取得が試みられる。
     * @return インストール情報。不明ならnull
     */
    public static String getJreHome(){
        String result;

        try{
            result = System.getProperty("java.home");
        }catch(SecurityException e){
            result = null;
        }

        return result;
    }

    /**
     * 非互換エラーメッセージを組み立てる。
     * @return エラーメッセージ
     */
    public static String buildErrMessage(){
        // このクラスではStringBuilder禁止
        StringBuffer message = new StringBuffer(INIT_SBUF);

        message.append("ERROR : Java JRE ")
               .append(REQUIRED_JRE_VER)
               .append(" compatible or later required.");

        String specVer = getLangPkgSpec();
        if(specVer != null){
            message.append("\nbut\u0020")
                   .append(specVer)
                   .append("\u0020detected.");
        }

        String jreHome = getJreHome();
        if(jreHome != null){
            message.append("  [ ")
                   .append(jreHome)
                   .append(" ]");
        }

        return message.toString();
    }

    /**
     * 指定された文字数で行の長さを改行文字で揃える。
     * <p>サロゲートペアは無視される。
     * @param text 文字列
     * @param limit 行ごとの最大文字数
     * @return 改行済みの文字列
     */
    public static String alignLine(String text, int limit){
        // このクラスではStringBuilder禁止
        int textLength = text.length();
        StringBuffer message = new StringBuffer(textLength + DEF_LINES);

        int lineLength = 0;

        for(int idx = 0; idx < textLength; idx++){
            if(lineLength >= limit){
                message.append('\n');
                lineLength = 0;
            }

            char ch = text.charAt(idx);
            message.append(ch);

            if(ch == '\n') lineLength = 0;
            else           lineLength++;
        }

        String result = message.toString();
        return result;
    }

    /**
     * JRE環境をチェックする。(JRE1.7)
     * <p>もしJREの非互換性が検出されたらエラーメッセージを報告する。
     * @return 互換性があれば0、無ければ非0
     */
    public static int checkJre(){
        if(has17Runtime()) return 0;

        String message = buildErrMessage();
        STDERR.println(message);
        STDERR.flush();
        if(has12Runtime()){
            showErrorDialog(message);
        }

        return EXIT_CODE_INCOMPAT_JRE;
    }

    /**
     * Swingダイアログでエラーを報告する。
     * <p>ボタンを押すまでの間、実行はブロックされる。
     * <p>GUIに接続できなければ何か例外を投げるかもしれない。
     * @param text エラー文面
     */
    public static void showErrorDialog(String text){
        String aligned = alignLine(text, MAX_LINE);

        Frame parent = null;
        JOptionPane.showMessageDialog(
                parent,
                aligned, DIALOG_TITLE,
                JOptionPane.ERROR_MESSAGE );

        return;
    }

}
