/*
 * font information
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.text.CharacterIterator;
import jp.sourceforge.jindolf.json.JsBoolean;
import jp.sourceforge.jindolf.json.JsNumber;
import jp.sourceforge.jindolf.json.JsObject;
import jp.sourceforge.jindolf.json.JsPair;
import jp.sourceforge.jindolf.json.JsString;
import jp.sourceforge.jindolf.json.JsValue;

/**
 * フォント描画に関する各種設定。
 */
public class FontInfo{

    /** デフォルトのフォント設定。 */
    public static final FontInfo DEFAULT_FONTINFO = new FontInfo();

    private static final String HASH_FAMILY = "family";
    private static final String HASH_SIZE = "size";
    private static final String HASH_ISBOLD = "isBold";
    private static final String HASH_ISITALIC = "isItalic";
    private static final String HASH_USEAA = "useAntiAlias";
    private static final String HASH_FRACTIONAL = "useFractional";


    private Font font;
    private FontRenderContext context;


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
     * @param font フォント
     * @return 描画設定
     */
    public static FontRenderContext createBestContext(Font font){
        FontRenderContext result;

        AffineTransform identity = ImtblAffineTx.IDENTITY;
        if(FontUtils.guessBitmapFont(font)){
            result = new FontRenderContext(identity, false, false);
        }else{
            result = new FontRenderContext(identity, true, true);
        }

        return result;
    }

    /**
     * フォント設定をJSON形式にエンコードする。
     * @param fontInfo フォント設定
     * @return JSON Object
     */
    public static JsObject buildJson(FontInfo fontInfo){
        Font font = fontInfo.getFont();
        FontRenderContext frc = fontInfo.getFontRenderContext();
        JsPair type = new JsPair(HASH_FAMILY,
                                 FontUtils.getRootFamilyName(font) );
        JsPair size = new JsPair(HASH_SIZE, font.getSize());
        JsPair bold = new JsPair(HASH_ISBOLD, font.isBold());
        JsPair italic = new JsPair(HASH_ISITALIC, font.isItalic());
        JsPair host = new JsPair(HASH_USEAA, frc.isAntiAliased());
        JsPair port =
                new JsPair(HASH_FRACTIONAL, frc.usesFractionalMetrics());

        JsObject result = new JsObject();
        result.putPair(type);
        result.putPair(size);
        result.putPair(bold);
        result.putPair(italic);
        result.putPair(host);
        result.putPair(port);

        return result;
    }

    /**
     * JSONからのフォント設定復元。
     * @param obj JSON Object
     * @return フォント設定
     */
    public static FontInfo decodeJson(JsObject obj){
        JsValue value;

        Font newFont = FontUtils.createDefaultSpeechFont();
        FontRenderContext newFrc = createBestContext(newFont);
        int style = newFont.getStyle();

        value = obj.getValue(HASH_FAMILY);
        if(value instanceof JsString){
            JsString string = (JsString) value;
            Font decoded = Font.decode(string.toRawString());
            if(decoded != null){
                newFont = decoded;
            }
        }

        int size = newFont.getSize();
        value = obj.getValue(HASH_SIZE);
        if(value instanceof JsNumber){
            JsNumber number = (JsNumber) value;
            size = number.intValue();
        }

        boolean isBold = newFont.isBold();
        value = obj.getValue(HASH_ISBOLD);
        if(value instanceof JsBoolean){
            JsBoolean bool = (JsBoolean) value;
            isBold = bool.booleanValue();
        }
        if(isBold) style |= Font.BOLD;

        boolean isItalic = newFont.isItalic();
        value = obj.getValue(HASH_ISITALIC);
        if(value instanceof JsBoolean){
            JsBoolean bool = (JsBoolean) value;
            isItalic = bool.booleanValue();
        }
        if(isItalic) style |= Font.ITALIC;

        boolean isAntiAlias = newFrc.isAntiAliased();
        value = obj.getValue(HASH_USEAA);
        if(value instanceof JsBoolean){
            JsBoolean bool = (JsBoolean) value;
            isAntiAlias = bool.booleanValue();
        }

        boolean useFractional = newFrc.usesFractionalMetrics();
        value = obj.getValue(HASH_FRACTIONAL);
        if(value instanceof JsBoolean){
            JsBoolean bool = (JsBoolean) value;
            useFractional = bool.booleanValue();
        }

        newFont = newFont.deriveFont(style, (float)size);

        newFrc = new FontRenderContext(ImtblAffineTx.IDENTITY,
                                       isAntiAlias, useFractional);

        FontInfo result = new FontInfo(newFont, newFrc);

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
