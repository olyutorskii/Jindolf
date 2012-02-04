/*
 * font utilities
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * フォントユーティリティ。
 */
public final class FontUtils{

    /** Font.DIALOG代替品。 */
    public static final String FAMILY_DIALOG = "Dialog";
    /** デフォルトのポイントサイズ。 */
    public static final int DEF_SIZE = 16;

    // Locale.ROOT代替品。@see Locale#ROOT
    private static final Locale LOCALE_ROOT = new Locale("", "", "");

    private static final int MS_BMP_LIMIT = 24;

    private static final String[] INIT_FAMILY_NAMES = {
        "Hiragino Kaku Gothic Pro",  // for MacOS X
        "Hiragino Kaku Gothic Std",
        "Osaka",
        "MS PGothic",                // for WinXP
        "MS Gothic",
        "IPAMonaPGothic",
        // TODO X11用のおすすめは？
    };

    /** JIS0208:1990 チェック用。 */
    private static final String JPCHECK_CODE = "あ凜熙峠ゑアｱヴヰ┼ЖΩ9A";

    /** 二重引用符。 */
    private static final char DQ = '"';


    /**
     * 隠れコンストラクタ。
     */
    private FontUtils(){
        assert false;
    }


    /**
     * システムに存在する有効なファミリ名か判定する。
     * @param familyName フォントファミリ名。
     * @return 存在する有効なファミリ名ならtrue
     */
    public static boolean isValidFamilyName(String familyName){
        int style = 0x00 | Font.PLAIN;
        int size = 1;
        Font dummyFont = new Font(familyName, style, size);

        String dummyFamilyName      = getRootFamilyName(dummyFont);
        String dummyLocalFamilyName = dummyFont.getFamily();
        if(dummyFamilyName     .equals(familyName)) return true;
        if(dummyLocalFamilyName.equals(familyName)) return true;

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
        int size = DEF_SIZE;
        Font result = new Font(defaultFamilyName, style, size);

        return result;
    }

    /**
     * ソートされたフォントファミリ一覧表を生成する。
     * <p>JISX0208:1990相当が表示できないファミリは弾かれる。
     * <p>結構実行時間がかかるかも(数千ms)。乱用禁物。
     * @return フォント一覧
     */
    public static List<String> createFontList(){
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] allFonts = ge.getAllFonts();

        Set<String> checked = new HashSet<String>();
        for(Font font : allFonts){
            String familyName = font.getFamily();
            if(checked.contains(familyName)) continue;
            if(font.canDisplayUpTo(JPCHECK_CODE) >= 0) continue;
            checked.add(familyName);
        }
        List<String> result = new ArrayList<String>(checked);
        Collections.sort(result);

        return result;
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
        if(   font.getSize() < MS_BMP_LIMIT
           && familyName.startsWith("MS")
           && (   familyName.contains("Gothic")
               || familyName.contains("Mincho")    ) ){
            return true;
        }
        return false;
    }

    /**
     * Font#decode()用の名前を返す。
     * 空白が含まれる場合は二重引用符で囲まれる。
     * @param font フォント
     * @return Font#decode()用の名前
     * @see Font#decode(String)
     */
    public static String getFontDecodeName(Font font){
        StringBuilder result = new StringBuilder();

        String familyName = getRootFamilyName(font);

        StringBuilder style = new StringBuilder();
        if(font.isBold())       style.append("BOLD");
        if(font.isItalic())     style.append("ITALIC");
        if(style.length() <= 0) style.append("PLAIN");

        int fontSize = font.getSize();

        result.append(familyName)
              .append('-').append(style)
              .append('-').append(fontSize);

        if(   result.indexOf("\u0020") >= 0
           || result.indexOf("\u3000") >= 0 ){
            result.insert(0, DQ).append(DQ);
        }

        return result.toString();
    }

    /**
     * ロケール中立なフォントファミリ名を返す。
     * JRE1.5対策
     * @param font フォント
     * @return ファミリ名
     * @see Font#getFamily(Locale)
     */
    public static String getRootFamilyName(Font font){
        return font.getFamily(LOCALE_ROOT);
    }

}
