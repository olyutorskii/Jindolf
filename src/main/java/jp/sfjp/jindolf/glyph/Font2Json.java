/*
 * font <-> JSON exchanging
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import jp.sourceforge.jovsonz.JsBoolean;
import jp.sourceforge.jovsonz.JsNumber;
import jp.sourceforge.jovsonz.JsObject;
import jp.sourceforge.jovsonz.JsPair;
import jp.sourceforge.jovsonz.JsString;
import jp.sourceforge.jovsonz.JsValue;

/**
 * フォント情報とJSONとのデータ交換を行う。
 */
public final class Font2Json {

    private static final String HASH_FAMILY     = "family";
    private static final String HASH_SIZE       = "size";
    private static final String HASH_ISBOLD     = "isBold";
    private static final String HASH_ISITALIC   = "isItalic";
    private static final String HASH_USEAA      = "useAntiAlias";
    private static final String HASH_FRACTIONAL = "useFractional";

    /**
     * 隠しコンストラクタ。
     */
    private Font2Json(){
        assert false;
    }


    /**
     * フォント設定をJSON形式にエンコードする。
     *
     * @param fontInfo フォント設定
     * @return JSON Object
     */
    public static JsObject buildJson(FontInfo fontInfo){
        Font font             = fontInfo.getFont();
        FontRenderContext frc = fontInfo.getFontRenderContext();

        JsPair family = new JsPair(HASH_FAMILY,
                                   fontInfo.getRootFamilyName() );
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
     *
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
            font = FontInfo.DEFAULT_FONTINFO.getFont();
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

        int size = FontInfo.DEF_SIZE;
        value = obj.getValue(HASH_SIZE);
        if(value instanceof JsNumber){
            JsNumber number = (JsNumber) value;
            size = number.intValue();
        }

        Font derivedFont = font.deriveFont(style, (float) size);

        return derivedFont;
    }

    /**
     * JSONからフォント描画設定を復元。
     *
     * @param obj JSON Object
     * @param font デフォルトフォント
     * @return フォント描画設定
     */
    private static FontRenderContext decodeJsonFrc(JsObject obj,
                                                     Font font ){
        JsBoolean jsAntiAlias = null;
        JsBoolean jsFractional = null;

        boolean isAntiAlias = false;
        boolean useFractional = false;

        JsValue value;
        value = obj.getValue(HASH_USEAA);
        if(value instanceof JsBoolean){
            jsAntiAlias = (JsBoolean) value;
            isAntiAlias = jsAntiAlias.booleanValue();
        }

        value = obj.getValue(HASH_FRACTIONAL);
        if(value instanceof JsBoolean){
            jsFractional = (JsBoolean) value;
            useFractional = jsFractional.booleanValue();
        }

        if(jsAntiAlias == null || jsFractional == null){
            FontRenderContext defFrc = FontInfo.createBestContext(font);
            if(jsAntiAlias == null){
                isAntiAlias = defFrc.isAntiAliased();
            }
            if(jsFractional == null){
                useFractional = defFrc.usesFractionalMetrics();
            }
        }

        FontRenderContext newFrc =
                new FontRenderContext(ImtblAffineTx.IDENTITY,
                                      isAntiAlias, useFractional );

        return newFrc;
    }

    /**
     * JSONからのフォント設定復元。
     *
     * @param obj JSON Object
     * @return フォント設定
     */
    public static FontInfo decodeJson(JsObject obj){
        Font font = decodeJsonFont(obj);
        FontRenderContext frc = decodeJsonFrc(obj, font);

        FontInfo result = new FontInfo(font, frc);

        return result;
    }

}
