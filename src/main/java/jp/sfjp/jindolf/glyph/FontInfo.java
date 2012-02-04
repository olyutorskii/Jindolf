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
import jp.sourceforge.jovsonz.JsBoolean;
import jp.sourceforge.jovsonz.JsNumber;
import jp.sourceforge.jovsonz.JsObject;
import jp.sourceforge.jovsonz.JsPair;
import jp.sourceforge.jovsonz.JsString;
import jp.sourceforge.jovsonz.JsValue;

/**
 * フォント描画に関する各種設定。
 */
public class FontInfo{

    /** デフォルトのフォント設定。 */
    public static final FontInfo DEFAULT_FONTINFO = new FontInfo();

    private static final String HASH_FAMILY     = "family";
    private static final String HASH_SIZE       = "size";
    private static final String HASH_ISBOLD     = "isBold";
    private static final String HASH_ISITALIC   = "isItalic";
    private static final String HASH_USEAA      = "useAntiAlias";
    private static final String HASH_FRACTIONAL = "useFractional";


    private final Font font;
    private final FontRenderContext context;


    /**
     * コンストラクタ。
     * デフォルトフォントとそれに適した描画属性が指定される。
     */
    public FontInfo(){
        this(FontUtils.createDefaultSpeechFont());
        return;
    }

    /**
     * コンストラクタ。
     * 描画設定はフォント属性に応じて自動的に調整される。
     * @param font フォント
     * @throws NullPointerException 引数がnull
     */
    public FontInfo(Font font)
            throws NullPointerException{
        this(font, createBestContext(font));
        return;
    }

    /**
     * コンストラクタ。
     * @param font フォント
     * @param context 描画設定
     * @throws NullPointerException 引数がnull
     */
    public FontInfo(Font font, FontRenderContext context)
            throws NullPointerException{
        super();
        if(font == null || context == null) throw new NullPointerException();
        this.font = font;
        this.context = context;
        return;
    }


    /**
     * フォントに応じた最適な描画設定を生成する。
     * <p>ビットマップフォントと推測されるときは
     * アンチエイリアスやサブピクセル補完を無効にする。
     * @param font フォント
     * @return 描画設定
     * @see FontUtils#guessBitmapFont(Font)
     */
    public static FontRenderContext createBestContext(Font font){
        boolean isAntiAliased         = true;
        boolean usesFractionalMetrics = true;
        if(FontUtils.guessBitmapFont(font)){
            isAntiAliased         = false;
            usesFractionalMetrics = false;
        }

        AffineTransform identity = ImtblAffineTx.IDENTITY;
        FontRenderContext result =
                new FontRenderContext(identity,
                                      isAntiAliased,
                                      usesFractionalMetrics);

        return result;
    }

    /**
     * フォント設定をJSON形式にエンコードする。
     * @param fontInfo フォント設定
     * @return JSON Object
     */
    public static JsObject buildJson(FontInfo fontInfo){
        Font font             = fontInfo.getFont();
        FontRenderContext frc = fontInfo.getFontRenderContext();

        JsPair family = new JsPair(HASH_FAMILY,
                                   FontUtils.getRootFamilyName(font) );
        JsPair size   = new JsPair(HASH_SIZE, font.getSize());
        JsPair bold   = new JsPair(HASH_ISBOLD, font.isBold());
        JsPair italic = new JsPair(HASH_ISITALIC, font.isItalic());
        JsPair aa     = new JsPair(HASH_USEAA, frc.isAntiAliased());
        JsPair frac   = new JsPair(HASH_FRACTIONAL,
                                   frc.usesFractionalMetrics() );

        JsObject result = new JsObject();
        result.putPair(family);
        result.putPair(size);
        result.putPair(bold);
        result.putPair(italic);
        result.putPair(aa);
        result.putPair(frac);

        return result;
    }

    /**
     * JSONからフォントを復元。
     * @param obj JSON Object
     * @return フォント
     */
    private static Font decodeJsonFont(JsObject obj){
        JsValue value;

        Font font = null;
        value = obj.getValue(HASH_FAMILY);
        if(value instanceof JsString){
            JsString string = (JsString) value;
            Font decoded = Font.decode(string.toRawString());
            if(decoded != null){
                font = decoded;
            }
        }
        if(font == null){
            font = FontUtils.createDefaultSpeechFont();
        }

        boolean isBold   = false;
        boolean isItalic = false;

        value = obj.getValue(HASH_ISBOLD);
        if(value instanceof JsBoolean){
            JsBoolean bool = (JsBoolean) value;
            isBold = bool.booleanValue();
        }

        value = obj.getValue(HASH_ISITALIC);
        if(value instanceof JsBoolean){
            JsBoolean bool = (JsBoolean) value;
            isItalic = bool.booleanValue();
        }

        int style = Font.PLAIN;
        if(isBold)   style |= Font.BOLD;
        if(isItalic) style |= Font.ITALIC;

        int size = FontUtils.DEF_SIZE;
        value = obj.getValue(HASH_SIZE);
        if(value instanceof JsNumber){
            JsNumber number = (JsNumber) value;
            size = number.intValue();
        }

        Font derivedFont = font.deriveFont(style, (float)size);

        return derivedFont;
    }

    /**
     * JSONからフォント描画設定を復元。
     * @param obj JSON Object
     * @param font デフォルトフォント
     * @return フォント描画設定
     */
    private static FontRenderContext decodeJsonFrc(JsObject obj,
                                                   Font font ){
        FontRenderContext defFrc = createBestContext(font);
        boolean isAntiAlias   = defFrc.isAntiAliased();
        boolean useFractional = defFrc.usesFractionalMetrics();

        JsValue value;

        value = obj.getValue(HASH_USEAA);
        if(value instanceof JsBoolean){
            JsBoolean bool = (JsBoolean) value;
            isAntiAlias = bool.booleanValue();
        }

        value = obj.getValue(HASH_FRACTIONAL);
        if(value instanceof JsBoolean){
            JsBoolean bool = (JsBoolean) value;
            useFractional = bool.booleanValue();
        }

        FontRenderContext newFrc =
                new FontRenderContext(ImtblAffineTx.IDENTITY,
                                      isAntiAlias, useFractional );

        return newFrc;
    }

    /**
     * JSONからのフォント設定復元。
     * @param obj JSON Object
     * @return フォント設定
     */
    public static FontInfo decodeJson(JsObject obj){
        Font font = decodeJsonFont(obj);
        FontRenderContext frc = decodeJsonFrc(obj, font);

        FontInfo result = new FontInfo(font, frc);

        return result;
    }

    /**
     * フォントを返す。
     * @return フォント
     */
    public Font getFont(){
        return this.font;
    }

    /**
     * 描画属性を返す。
     * @return 描画属性
     */
    public FontRenderContext getFontRenderContext(){
        return this.context;
    }

    /**
     * アンチエイリアス機能を使うか判定する。
     * @return アンチエイリアス機能を使うならtrue
     */
    public boolean isAntiAliased(){
        boolean result = this.context.isAntiAliased();
        return result;
    }

    /**
     * サブピクセル精度を使うか判定する。
     * @return サブピクセル精度を使うならtrue
     */
    public boolean usesFractionalMetrics(){
        boolean result = this.context.usesFractionalMetrics();
        return result;
    }

    /**
     * フォントの最大寸法を返す。
     * @return 最大寸法
     * @see java.awt.Font#getMaxCharBounds(FontRenderContext)
     */
    public Rectangle getMaxCharBounds(){
        Rectangle2D r2d = this.font.getMaxCharBounds(this.context);
        Rectangle rect = r2d.getBounds();
        return rect;
    }

    /**
     * フォントのみ異なる設定を派生させる。
     * @param newFont 新フォント
     * @return 新設定
     */
    public FontInfo deriveFont(Font newFont){
        return new FontInfo(newFont, this.context);
    }

    /**
     * 描画属性のみ異なる設定を派生させる。
     * @param newContext 新描画設定
     * @return 新設定
     */
    public FontInfo deriveRenderContext(FontRenderContext newContext){
        return new FontInfo(this.font, newContext);
    }

    /**
     * 描画属性のみ異なる設定を派生させる。
     * @param isAntiAliases アンチエイリアス設定
     * @param useFractional サブピクセル精度設定
     * @return 新設定
     */
    public FontInfo deriveRenderContext(boolean isAntiAliases,
                                        boolean useFractional){
        AffineTransform tx = this.context.getTransform();
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
        GlyphVector glyph =
                this.font.createGlyphVector(this.context, iterator);
        return glyph;
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

        if( ! (this.font   .equals(target.font))    ) return false;
        if( ! (this.context.equals(target.context)) ) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        return this.font.hashCode() ^ this.context.hashCode();
    }

}
