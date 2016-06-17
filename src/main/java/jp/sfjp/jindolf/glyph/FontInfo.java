/*
 * font information
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.CharacterIterator;
import java.util.Locale;

/**
 * フォント描画に関する各種設定。
 */
public class FontInfo{

    /** デフォルトのフォント環境。 */
    public static final FontEnv DEFAULT_FONTENV;
    /** デフォルトのフォント設定。 */
    public static final FontInfo DEFAULT_FONTINFO;

    /** デフォルトのポイントサイズ。 */
    public static final int DEF_SIZE = 16;
    /** デフォルトのフォントスタイル。 */
    public static final int DEF_STYLE = 0x00 | Font.PLAIN;

    /** {@link java.util.Locale#ROOT}代替品。 */
    private static final Locale LOCALE_ROOT = new Locale("", "", "");
    /** MSリコー系日本語ベクトルフォント下限ポイントサイズ。 */
    private static final int MS_VEC_LIMIT = 24;
    /** 二重引用符。 */
    private static final char DQ = '"';


    static{
        DEFAULT_FONTENV = FontEnv.DEFAULT;
        DEFAULT_FONTINFO = new FontInfo();
    }


    // いずれのフィールドもnull値はデフォルト値の遅延評価フラグ
    private String familyName;
    private Font font;
    private FontRenderContext context;


    /**
     * コンストラクタ。
     * デフォルトフォントとそれに適した描画属性が指定される。
     */
    public FontInfo(){
        this((Font) null, (FontRenderContext) null);
        return;
    }

    /**
     * コンストラクタ。
     * @param font フォント
     * @param context 描画設定
     */
    public FontInfo(Font font, FontRenderContext context){
        super();
        this.familyName = null;
        this.font = font;
        this.context = context;
        return;
    }


    /**
     * マイクロソフト&amp;リコー(リョービイマジクス)系
     * 日本語ベクトルフォントか否か、ファミリ名で見当をつける。
     *
     * <p>日本語Windows同梱のMSゴシックやMS明朝などが対象。
     *
     * <p>メイリオは対象外。
     *
     * @param font フォント
     * @return 見当が付けばtrue
     */
    protected static boolean isMsRicohJpFont(Font font){
        String rootFamilyName = font.getFamily(LOCALE_ROOT);
        if(rootFamilyName.startsWith("MS")){
            if(rootFamilyName.contains("Gothic")) return true;
            if(rootFamilyName.contains("Mincho")) return true;
        }
        return false;
    }

    /**
     * ビットマップフォントか否か見当をつける。
     *
     * <p>判定基準はかなりアバウト。
     * 実用上、小さめのMSPゴシックを補足できればそれでいい。
     *
     * <p>ビットマップフォントには
     * アンチエイリアスやサブピクセルを使わないほうが
     * 見栄えがいいような気がする。
     *
     * @param font 判定対象フォント
     * @return ビットマップフォントらしかったらtrue
     */
    protected static boolean isBitmapFont(Font font){
        if(font.getSize() >= MS_VEC_LIMIT) return false;
        if(isMsRicohJpFont(font)) return true;
        return false;
    }

    /**
     * フォントに応じた最適な描画設定を生成する。
     *
     * <p>ビットマップフォントと推測されるときは
     * アンチエイリアスやサブピクセル補完を無効にする。
     *
     * @param font フォント
     * @return 描画設定
     */
    protected static FontRenderContext createBestContext(Font font){
        boolean isAntiAliased;
        boolean usesFractionalMetrics;

        if(isBitmapFont(font)){
            isAntiAliased         = false;
            usesFractionalMetrics = false;
        }else{
            isAntiAliased         = true;
            usesFractionalMetrics = true;
        }

        AffineTransform identity = ImtblAffineTx.IDENTITY;
        FontRenderContext result;
        result = new FontRenderContext(identity,
                                       isAntiAliased,
                                       usesFractionalMetrics );

        return result;
    }

    /**
     * ファミリ名を返す。
     * @return ファミリ名
     */
    private String getFamilyName(){
        if(this.familyName == null){
            if(this.font == null){
                this.familyName = DEFAULT_FONTENV.selectFontFamily();
            }else{
                // 再帰に注意
                this.familyName = getRootFamilyName();
            }
        }
        return this.familyName;
    }

    /**
     * フォントを返す。
     * @return フォント
     */
    public Font getFont(){
        if(this.font == null){
            String name = getFamilyName();
            this.font = new Font(name, DEF_STYLE, DEF_SIZE);
        }
        return this.font;
    }

    /**
     * 描画属性を返す。
     * @return 描画属性
     */
    public FontRenderContext getFontRenderContext(){
        if(this.context == null){
            Font thisFont = getFont();
            this.context = createBestContext(thisFont);
        }
        return this.context;
    }

    /**
     * アンチエイリアス機能を使うか判定する。
     * @return アンチエイリアス機能を使うならtrue
     */
    public boolean isAntiAliased(){
        FontRenderContext frc = getFontRenderContext();
        boolean result = frc.isAntiAliased();
        return result;
    }

    /**
     * サブピクセル精度を使うか判定する。
     * @return サブピクセル精度を使うならtrue
     */
    public boolean usesFractionalMetrics(){
        FontRenderContext frc = getFontRenderContext();
        boolean result = frc.usesFractionalMetrics();
        return result;
    }

    /**
     * フォントの最大寸法を返す。
     * @return 最大寸法
     * @see java.awt.Font#getMaxCharBounds(FontRenderContext)
     */
    public Rectangle getMaxCharBounds(){
        Font thisFont = getFont();
        FontRenderContext frc = getFontRenderContext();
        Rectangle2D r2d = thisFont.getMaxCharBounds(frc);
        Rectangle rect = r2d.getBounds();
        return rect;
    }

    /**
     * フォントのみ異なる設定を派生させる。
     * @param newFont 新フォント
     * @return 新設定
     */
    public FontInfo deriveFont(Font newFont){
        FontInfo result = new FontInfo(newFont, this.context);
        return result;
    }

    /**
     * 描画属性のみ異なる設定を派生させる。
     * @param newContext 新描画設定
     * @return 新設定
     */
    public FontInfo deriveRenderContext(FontRenderContext newContext){
        FontInfo result = new FontInfo(this.font, newContext);
        return result;
    }

    /**
     * 描画属性のみ異なる設定を派生させる。
     * @param isAntiAliases アンチエイリアス設定
     * @param useFractional サブピクセル精度設定
     * @return 新設定
     */
    public FontInfo deriveRenderContext(boolean isAntiAliases,
                                           boolean useFractional ){
        AffineTransform tx;
        if(this.context == null){
            tx = ImtblAffineTx.IDENTITY;
        }else{
            tx = this.context.getTransform();
        }
        FontRenderContext newContext =
                new FontRenderContext(tx, isAntiAliases, useFractional);
        return deriveRenderContext(newContext);
    }

    /**
     * 文字列からグリフ集合を生成する。
     * @param iterator 文字列
     * @return グリフ集合
     */
    public GlyphVector createGlyphVector(CharacterIterator iterator){
        Font thisFont = getFont();
        FontRenderContext frc = getFontRenderContext();
        GlyphVector glyph = thisFont.createGlyphVector(frc, iterator);
        return glyph;
    }

    /**
     * ロケール中立なフォントファミリ名を返す。
     * JRE1.5対策
     * @return ファミリ名
     * @see Font#getFamily(Locale)
     */
    public String getRootFamilyName(){
        Font thisFont = getFont();
        String result = thisFont.getFamily(LOCALE_ROOT);
        return result;
    }

    /**
     * Font#decode()用の名前を返す。
     * 空白が含まれる場合は二重引用符で囲まれる。
     * @return {@link java.awt.Font#decode(String)}用の名前
     * @see java.awt.Font#decode(String)
     */
    public String getFontDecodeName(){
        StringBuilder result = new StringBuilder();

        String name = getRootFamilyName();

        Font thisFont = getFont();
        StringBuilder style = new StringBuilder();
        if(thisFont.isBold())   style.append("BOLD");
        if(thisFont.isItalic()) style.append("ITALIC");
        if(style.length() <= 0) style.append("PLAIN");

        int fontSize = thisFont.getSize();

        result.append(name)
              .append('-').append(style)
              .append('-').append(fontSize);

        if(   result.indexOf("\u0020") >= 0
           || result.indexOf("\u3000") >= 0 ){
            result.insert(0, DQ).append(DQ);
        }

        return result.toString();
    }

    /**
     * {@inheritDoc}
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj){
        if( ! (obj instanceof FontInfo) ) return false;
        FontInfo target = (FontInfo) obj;

        Font thisFont = getFont();
        Font targetFont = target.getFont();
        if( ! (thisFont.equals(targetFont)) ){
            return false;
        }

        FontRenderContext thisContext = getFontRenderContext();
        FontRenderContext targetContext = target.getFontRenderContext();
        if( ! (thisContext.equals(targetContext)) ){
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        int hashFont = getFont().hashCode();
        int hashContext = getFontRenderContext().hashCode();
        return hashFont ^ hashContext;
    }

}
