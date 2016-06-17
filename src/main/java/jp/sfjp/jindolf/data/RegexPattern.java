/*
 * Regex pattern
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import jp.sourceforge.jovsonz.JsBoolean;
import jp.sourceforge.jovsonz.JsObject;
import jp.sourceforge.jovsonz.JsPair;
import jp.sourceforge.jovsonz.JsString;
import jp.sourceforge.jovsonz.JsValue;

/**
 * 正規表現。
 */
public class RegexPattern{

    /** 英字大小無視指定フラグ。 */
    public static final int IGNORECASEFLAG =
            0x00000000 | Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE;
    private static final String REGEX_DELIM = "[\\s\u3000]+"; // 空白(全角含)
    private static final String REGEX_CHAR = ".?+*\\$(|)[]{}^-&";


    private final String editSource;
    private final boolean isRegex;
    private final Pattern pattern;
    private final String comment;


    /**
     * コンストラクタ。
     *
     * @param editSource リテラル文字列または正規表現
     * @param isRegex 指定文字列が正規表現ならtrue。リテラルならfalse
     * @param flag 正規表現フラグ
     * @param comment コメント
     * @throws java.util.regex.PatternSyntaxException 正規表現がおかしい
     */
    public RegexPattern(String editSource,
                        boolean isRegex,
                        int flag,
                        String comment)
            throws PatternSyntaxException{
        super();
        if(editSource == null) throw new NullPointerException();

        this.isRegex    = isRegex;
        if(comment != null) this.comment = comment;
        else                this.comment = "";

        String regexExpr;
        if(this.isRegex){
            this.editSource = editSource;
            regexExpr = this.editSource;
        }else{
            String newSource = "";
            regexExpr = "";

            String[] tokens = editSource.split(REGEX_DELIM);
            for(String token : tokens){
                if(token == null || token.length() <= 0) continue;

                if(newSource.length() <= 0) newSource  =       token;
                else                        newSource += " " + token;

                String quoted = "(?:" + quote(token) + ")";
                if(regexExpr.length() <= 0) regexExpr  =       quoted;
                else                        regexExpr += "|" + quoted;
            }

            this.editSource = newSource;
        }

        this.pattern = Pattern.compile(regexExpr, flag);

        return;
    }

    /**
     * コンストラクタ。
     *
     * @param editSource リテラル文字列または正規表現
     * @param isRegex 指定文字列が正規表現ならtrue。リテラルならfalse
     * @param flag 正規表現フラグ
     * @throws java.util.regex.PatternSyntaxException 正規表現がおかしい
     */
    public RegexPattern(String editSource,
                        boolean isRegex,
                        int flag )
            throws PatternSyntaxException{
        this(editSource, isRegex, flag, " ");
        return;
    }


    /**
     * 正規表現とまぎらわしい字を含むか判定する。
     * @param seq 文字列
     * @return 紛らわしい字を含むならtrue
     */
    public static boolean hasRegexChar(CharSequence seq){
        int length = seq.length();
        for(int pt = 0; pt < length; pt++){
            char ch = seq.charAt(pt);
            if(REGEX_CHAR.indexOf(ch) >= 0) return true;
        }
        return false;
    }

    /**
     * 任意の文字列を必要に応じて正規表現シーケンス化する。
     * @param text 文字列
     * @return 引数と同じ内容の正規表現。必要がなければ引数そのまま
     */
    public static String quote(String text){
        if(hasRegexChar(text)){
            return Pattern.quote(text);
        }
        return text;
    }

    /**
     * JSON形式に変換する。
     * @param regex 正規表現
     * @return JSON Object
     */
    public static JsObject encodeJson(RegexPattern regex){
        JsObject result = new JsObject();

        int regexFlag = regex.getRegexFlag();
        boolean flagDotall     = (regexFlag & Pattern.DOTALL)           != 0;
        boolean flagMultiline  = (regexFlag & Pattern.MULTILINE)        != 0;
        boolean flagIgnoreCase = (regexFlag & IGNORECASEFLAG) != 0;

        JsPair source     = new JsPair("source",     regex.getEditSource());
        JsPair isRegex    = new JsPair("isRegex",    regex.isRegex());
        JsPair dotall     = new JsPair("dotall",     flagDotall);
        JsPair multiline  = new JsPair("multiline",  flagMultiline);
        JsPair ignorecase = new JsPair("ignorecase", flagIgnoreCase);
        JsPair comment    = new JsPair("comment",    regex.getComment());

        result.putPair(source);
        result.putPair(isRegex);
        result.putPair(dotall);
        result.putPair(multiline);
        result.putPair(ignorecase);
        result.putPair(comment);

        return result;
    }

    /**
     * JSON形式から復元する。
     * @param object JSON Object
     * @return 正規表現
     */
    public static RegexPattern decodeJson(JsObject object){
        JsValue value;

        String source;
        value = object.getValue("source");
        if(value instanceof JsString){
            source = ((JsString) value).toRawString();
        }else{
            source = "";
        }

        boolean isRegex;
        value = object.getValue("isRegex");
        if(value instanceof JsBoolean){
            isRegex = ((JsBoolean) value).booleanValue();
        }else{
            isRegex = false;
        }

        int regexFlag = 0x00000000;
        value = object.getValue("dotall");
        if(value instanceof JsBoolean){
            if(((JsBoolean) value).isTrue()){
                regexFlag |= Pattern.DOTALL;
            }
        }
        value = object.getValue("multiline");
        if(value instanceof JsBoolean){
            if(((JsBoolean) value).isTrue()){
                regexFlag |= Pattern.MULTILINE;
            }
        }
        value = object.getValue("ignorecase");
        if(value instanceof JsBoolean){
            if(((JsBoolean) value).isTrue()){
                regexFlag |= IGNORECASEFLAG;
            }
        }

        String comment;
        value = object.getValue("comment");
        if(value instanceof JsString){
            comment = ((JsString) value).toRawString();
        }else{
            comment = "";
        }

        RegexPattern result =
                new RegexPattern(source, isRegex, regexFlag, comment);

        return result;
    }


    /**
     * 元の入力文字列を返す。
     * @return 入力文字列
     */
    public String getEditSource(){
        return this.editSource;
    }

    /**
     * コメントを返す。
     * @return コメント
     */
    public String getComment(){
        return this.comment;
    }

    /**
     * 元の入力文字列が正規表現か否か返す。
     * @return 正規表現ならtrue
     */
    public boolean isRegex(){
        return this.isRegex;
    }

    /**
     * 正規表現フラグを返す。
     * @return 正規表現フラグ。
     * @see java.util.regex.Pattern#flags()
     */
    public int getRegexFlag(){
        return this.pattern.flags();
    }

    /**
     * コンパイルされた正規表現形式を返す。
     * @return コンパイルされた正規表現形式
     */
    public Pattern getPattern(){
        return this.pattern;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        return this.editSource;
    }

    /**
     * {@inheritDoc}
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj){
        if(obj == null){
            return false;
        }
        if( ! (obj instanceof RegexPattern) ){
            return false;
        }
        RegexPattern other = (RegexPattern) obj;

        String thisPattern = this.pattern.pattern();
        String otherPattern = other.pattern.pattern();

        if( ! thisPattern.equals(otherPattern) ) return false;

        if(this.pattern.flags() != other.pattern.flags()) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        int hash = this.pattern.pattern().hashCode();
        hash ^= this.pattern.flags();
        return hash;
    }

}
