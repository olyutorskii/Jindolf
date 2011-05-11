/*
 * font utilities
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Collections;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * フォントユーティリティ。
 */
public final class FontUtils{

    /** Font.DIALOG代替品。 */
    public static final String FAMILY_DIALOG = "Dialog";
    /** Locale.ROOT代替品。 */
    private static final Locale ROOT = new Locale("", "", "");

    private static final String[] INIT_FAMILY_NAMES = {
        "Hiragino Kaku Gothic Pro",  // for MacOS X
        "Hiragino Kaku Gothic Std",
        "Osaka",
        "MS PGothic",                // for WinXP
        "MS Gothic",
        // TODO X11用のおすすめは？
    };

    /** JIS0208:1990 チェック用。 */
    private static final String JPCHECK_CODE = "9Aあゑアｱヴヰ┼ЖΩ峠凜熙";


    /**
     * 隠れコンストラクタ。
     */
    private FontUtils(){
        assert false;
    }


    /**
     * システムに存在する有効なファミリ名か判定する。
     * @param family フォントファミリ名。
     * @return 存在する有効なファミリ名ならtrue
     */
    public static boolean isValidFamilyName(String family){
        int style = 0x00 | Font.PLAIN;
        int size = 1;
        Font dummyFont = new Font(family, style, size);

        String dummyFamily      = getRootFamilyName(dummyFont);
        String dummyLocalFamily = dummyFont.getFamily();
        if(dummyFamily     .equals(family)) return true;
        if(dummyLocalFamily.equals(family)) return true;

        return false;
    }

    /**
     * 発言用のデフォルトフォントを生成する。
     * 適当なファミリが見つからなかったら"Dialog"が選択される。
     * @return デフォルトフォント
     */
    public static Font createDefaultSpeechFont(){
        String defaultFamilyName = FAMILY_DIALOG;
        for(String familyName : INIT_FAMILY_NAMES){
            if(isValidFamilyName(familyName)){
                defaultFamilyName = familyName;
                break;
            }
        }

        int style = 0x00 | Font.PLAIN;
        int size = 16;
        Font result = new Font(defaultFamilyName, style, size);

        return result;
    }

    /**
     * ソートされたフォントファミリ一覧表を生成する。
     * JISX0208:1990相当が表示できないファミリは弾かれる。
     * 結構実行時間がかかるかも。乱用禁物。
     * @return フォント一覧
     */
    public static SortedSet<String> createFontSet(){
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();

        SortedSet<String> result = new TreeSet<String>();
        for(Font font : ge.getAllFonts()){
            if(font.canDisplayUpTo(JPCHECK_CODE) >= 0) continue;
            String familyName = font.getFamily();
            result.add(familyName.intern());
        }

        return Collections.unmodifiableSortedSet(result);
    }

    /**
     * ビットマップフォントか否か見当をつける。
     * ビットマップフォントにはアンチエイリアスやサブピクセルを使わないほうが
     * 見栄えがいいような気がする。
     * @param font 判定対象フォント
     * @return ビットマップフォントらしかったらtrue
     */
    public static boolean guessBitmapFont(Font font){
        String familyName = getRootFamilyName(font);
        if(   font.getSize() < 24
           && familyName.startsWith("MS")
           && (   familyName.contains("Gothic")
               || familyName.contains("Mincho")    ) ){
            return true;
        }
        return false;
    }

    /**
     * Font#decode()用の名前を返す。
     * @param font フォント
     * @return Font#decode()用の名前
     */
    public static String getFontDecodeName(Font font){
        StringBuilder result = new StringBuilder();

        StringBuilder style = new StringBuilder();
        if(font.isBold())   style.append("BOLD");
        if(font.isItalic()) style.append("ITALIC");
        if(style.length() <= 0) style.append("PLAIN");

        result.append(getRootFamilyName(font));
        result.append('-').append(style);
        result.append('-').append(font.getSize());

        if(   result.indexOf("\u0020") >= 0
           || result.indexOf("\u3000") >= 0 ){
            result.insert(0, '"').append('"');
        }

        return result.toString();
    }

    /**
     * ロケール中立なフォントファミリ名を返す。
     * JRE1.5対策
     * @param font フォント
     * @return ファミリ名
     */
    public static String getRootFamilyName(Font font){
        return font.getFamily(ROOT);
    }

}
