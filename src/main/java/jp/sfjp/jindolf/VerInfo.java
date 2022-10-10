/*
 * version information
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf;

import java.text.MessageFormat;
import java.util.Properties;

/**
 * バージョンその他アプリに関する各種情報。
 */
public final class VerInfo {

    /** タイトル。 */
    public static final String TITLE;
    /** バージョン。 */
    public static final String VERSION;
    /** 作者名。 */
    public static final String AUTHOR;
    /** 著作権表記。 */
    public static final String COPYRIGHT;
    /** ライセンス表記。 */
    public static final String LICENSE;
    /** 連絡先。 */
    public static final String CONTACT;
    /** 初出。 */
    public static final String INCEPTION;
    /** その他、何でも書きたいこと。 */
    public static final String COMMENT;
    /** クレジット。 */
    public static final String ID;

    private static final String RES_VERDEF = "resources/version.properties";

    private static final String PFX_TITLE     = "pkg-title.";
    private static final String PFX_VERSION   = "pkg-version.";
    private static final String PFX_AUTHOR    = "pkg-author.";
    private static final String PFX_LICENSE   = "pkg-license.";
    private static final String PFX_CONTACT   = "pkg-contact.";
    private static final String PFX_INCEPTION = "pkg-inception.";
    private static final String PFX_COMMENT   = "pkg-comment.";

    static{
        Properties verProp = ResourceManager.getProperties(RES_VERDEF);
        if(verProp == null) verProp = new Properties();

        TITLE     = getPackageInfo(verProp, PFX_TITLE,     "Jindolf");
        VERSION   = getPackageInfo(verProp, PFX_VERSION,   "0.0.1");
        AUTHOR    = getPackageInfo(verProp, PFX_AUTHOR,    "nobody");
        LICENSE   = getPackageInfo(verProp, PFX_LICENSE,   "Unknown");
        CONTACT   = getPackageInfo(verProp, PFX_CONTACT,   "Where?");
        INCEPTION = getPackageInfo(verProp, PFX_INCEPTION, "2008");
        COMMENT   = getPackageInfo(verProp, PFX_COMMENT,   "");

        COPYRIGHT = MessageFormat.format(
                "Copyright(c) {0} {1}",
                INCEPTION, AUTHOR );

        ID = MessageFormat.format(
                "{0} Ver.{1} {2} ({3})",
                TITLE, VERSION, COPYRIGHT, LICENSE );
    }


    /**
     * 隠しコンストラクタ。
     */
    private VerInfo(){
        assert false;
    }


    /**
     * プロパティからルートパッケージのパッケージ情報を取得する。
     *
     * @param prop プロパティ
     * @param prefix 接頭辞
     * @param defValue 見つからなかった場合のデフォルト値
     * @return パッケージ情報
     */
    static String getPackageInfo(Properties prop,
                                   String prefix,
                                   String defValue ){
        String result = getPackageInfo(prop, prefix,
                                       ResourceManager.DEF_ROOT_PACKAGE,
                                       defValue );
        return result;
    }

    /**
     * プロパティからパッケージに紐づけられたパッケージ情報を取得する。
     *
     * @param prop プロパティ
     * @param prefix 接頭辞
     * @param pkg 任意のパッケージ
     * @param defValue 見つからなかった場合のデフォルト値
     * @return パッケージ情報
     */
    static String getPackageInfo(Properties prop,
                                   String prefix,
                                   Package pkg,
                                   String defValue ){
        String propKeyName = prefix + pkg.getName();
        String result = prop.getProperty(propKeyName, defValue);

        // ignore Maven macro filtering
        if(isMavenMacro(result)){
            result = defValue;
        }

        return result;
    }

    /**
     * 文字列がMavenのマクロフィルタ置換子でありうるか判定する。
     *
     * <p>「${」で始まり「}」で終わる文字列を置換子とみなす。
     *
     * <p>マクロ展開結果がさらに偶然引っかかった場合はあきらめる。
     *
     * @param text 文字列
     * @return 置換子でありうるならtrue
     */
    public static boolean isMavenMacro(String text){
        if(text.startsWith("$" + "{") && text.endsWith("}")){
            return true;
        }
        return false;
    }

    /**
     * ウィンドウタイトル名を生成する。
     *
     * <p>各ウィンドウタイトルには、他のアプリとの区別のため
     * アプリ名が付加される。
     *
     * @param base タイトル基本部
     * @return アプリ名が付加されたウィンドウタイトル。
     */
    public static String getFrameTitle(String base){
        StringBuilder result = new StringBuilder();

        if(base != null){
            result.append(base).append("\u0020-\u0020");
        }
        result.append(TITLE);

        String message = result.toString();
        return message;
    }

    /**
     * About画面用メッセージを生成する。
     * @return About画面用メッセージ
     */
    public static String getAboutMessage(){
        StringBuilder result = new StringBuilder();

        result.append(MessageFormat.format(
                  "{0}\u0020\u0020\u0020Version {1}\n"
                + "{2}\n"
                + "ライセンス: {3}\n"
                + "連絡先: {4}",
                TITLE, VERSION, COPYRIGHT, LICENSE, CONTACT )
        );

        if(COMMENT.length() > 0){
            result.append('\n').append(COMMENT);
        }

        String message = result.toString();
        return message;
    }

}
