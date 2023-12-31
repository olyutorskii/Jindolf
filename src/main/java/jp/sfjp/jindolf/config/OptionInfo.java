/*
 * option argument information
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * コマンドラインオプション情報。
 *
 * <p>public static void main()の引数から展開される。
 */
public class OptionInfo{

    /*
        ex) 1000x800
        ex) 1290x1024-256+128
    */
    private static final String REGEX_DIMNO =
            "([1-9][0-9]{0,5})";
    private static final String REGEX_SIGN =
            "(?:\\+|(\\-))";
    private static final String REGEX_LOCNO =
            REGEX_SIGN + REGEX_DIMNO;
    private static final String REGEX_GEOMETRY =
              REGEX_DIMNO + "x" + REGEX_DIMNO
            + "(?:" + REGEX_LOCNO + REGEX_LOCNO + ")?";
    private static final Pattern PATTERN_GEOMETRY =
            Pattern.compile(REGEX_GEOMETRY);

    private static final String ERRFORM_UKNOWN =
            "未定義の起動オプション[{0}]が指定されました。";
    private static final String ERRFORM_NOARG =
            "起動オプション[{0}]に引数がありません。";
    private static final String ERRFORM_GEOM =
              "起動オプション[{0}]のジオメトリ指定[{1}]が不正です。"
            + "WIDTHxHEIGHT[(+|-)XPOS(+|-)YPOS]の形式で指定してください";
    private static final String ERRFORM_BOOL =
              "起動オプション[{0}]の真偽指定[{1}]が不正です。"
            + "on, off, yes, no, true, falseのいずれかを指定してください。";
    private static final String ERRFORM_NONBOOL =
            "起動オプション[{0}]は真偽を指定するオプションではありません。";


    private Integer frameWidth  = null;
    private Integer frameHeight = null;
    private Integer frameXpos   = null;
    private Integer frameYpos   = null;

    private final List<String> invokeArgs = new LinkedList<>();
    private final List<CmdOption> optionList = new LinkedList<>();
    private final Map<CmdOption, Boolean> boolOptionMap =
            new EnumMap<>(CmdOption.class);
    private final Map<CmdOption, String> stringOptionMap =
            new EnumMap<>(CmdOption.class);


    /**
     * コンストラクタ。
     */
    protected OptionInfo(){
        super();
        return;
    }


    /**
     * 文字列が可変引数のいずれかと英字大小無視で等しいか判定する。
     *
     * @param text 文字列
     * @param names 文字列の可変引数
     * @return 等しい物があればtrue
     */
    private static boolean equalsIgnoreCase(String text, String ... names){
        for(String name : names){
            if(text.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    /**
     * 真偽二値をとるオプション解析の下請け。
     *
     * @param info オプション情報格納先
     * @param option オプション種別
     * @param optTxt オプション名文字列
     * @param onoff オプション引数
     * @throws IllegalArgumentException 引数の構文エラー
     */
    private static void parseBooleanSwitch(OptionInfo info,
                                           CmdOption option,
                                           String optTxt,
                                           String onoff )
            throws IllegalArgumentException{
        Boolean flag;

        if(equalsIgnoreCase(onoff, "on", "yes", "true")){
            flag = Boolean.TRUE;
        }else if(equalsIgnoreCase(onoff, "off", "no", "false")){
            flag = Boolean.FALSE;
        }else{
            String errmsg =
                    MessageFormat.format(ERRFORM_BOOL, optTxt, onoff);
            throw new IllegalArgumentException(errmsg);
        }

        info.boolOptionMap.put(option, flag);

        return;
    }

    /**
     * ウィンドウジオメトリオプション解析。
     *
     * <p>例) WIDTHxHEIGHT+XPOS+YPOS
     *
     * @param info オプション情報格納先
     * @param optTxt オプション名文字列
     * @param geometry オプション引数
     * @throws IllegalArgumentException 引数の構文エラー
     */
    private static void parseGeometry(OptionInfo info,
                                      String optTxt,
                                      String geometry )
            throws IllegalArgumentException{
        Matcher matcher = PATTERN_GEOMETRY.matcher(geometry);
        if( ! matcher.matches() ){
            String errmsg = MessageFormat.format(ERRFORM_GEOM,
                                                 optTxt, geometry);
            throw new IllegalArgumentException(errmsg);
        }

        int gpos = 1;
        String width  = matcher.group(gpos++);
        String height = matcher.group(gpos++);
        String xMinus = matcher.group(gpos++);
        String xPos   = matcher.group(gpos++);
        String yMinus = matcher.group(gpos++);
        String yPos   = matcher.group(gpos++);

        info.frameWidth  = Integer.parseInt(width);
        info.frameHeight = Integer.parseInt(height);

        if(xPos != null){
            info.frameXpos = Integer.parseInt(xPos);
            if(xMinus != null){
                info.frameXpos = -info.frameXpos;
            }
        }

        if(yPos != null){
            info.frameYpos = Integer.parseInt(yPos);
            if(yMinus != null){
                info.frameYpos = -info.frameYpos;
            }
        }

        return;
    }

    /**
     * 引数付きオプションを解析する。
     *
     * @param info オプション情報
     * @param optTxt オプション文字列
     * @param option オプション種別
     * @param iterator コマンドライン引数並び
     * @throws IllegalArgumentException オプションの引数がない
     */
    private static void parseOptionArg(OptionInfo info,
                                       String optTxt,
                                       CmdOption option,
                                       Iterator<String> iterator )
            throws IllegalArgumentException {
        if( ! iterator.hasNext()){
            String errMsg = MessageFormat.format(ERRFORM_NOARG, optTxt);
            throw new IllegalArgumentException(errMsg);
        }

        String nextArg = iterator.next();

        if(option == CmdOption.OPT_GEOMETRY){
            parseGeometry(info, optTxt, nextArg);
        }else if(option.isBooleanOption()){
            parseBooleanSwitch(info, option, optTxt, nextArg);
        }else if(   option == CmdOption.OPT_INITFONT
                 || option == CmdOption.OPT_CONFDIR ){
            info.stringOptionMap.put(option, nextArg);
        }else{
            assert false;
        }

        return;
    }

    /**
     * オプション文字列を解析する。
     *
     * @param args main()に渡されるオプション文字列
     * @return 解析済みのオプション情報。
     * @throws IllegalArgumentException 構文エラー
     */
    public static OptionInfo parseOptions(String ... args)
            throws IllegalArgumentException{
        OptionInfo result = new OptionInfo();

        for(String arg : args){
            if(arg == null) continue;
            result.invokeArgs.add(arg);
        }
        Iterator<String> iterator = result.invokeArgs.iterator();

        while(iterator.hasNext()){
            String arg = iterator.next();

            CmdOption option = CmdOption.parseCmdOption(arg);
            if(option == null){
                String errmsg = MessageFormat.format(ERRFORM_UKNOWN, arg);
                throw new IllegalArgumentException(errmsg);
            }
            result.optionList.add(option);

            if( ! option.isIndepOption() ){
                parseOptionArg(result, arg, option, iterator);
            }
        }

        return result;
    }


    /**
     * 全引数のリストを返す。
     *
     * @return 全引数のリスト
     */
    public List<String> getInvokeArgList(){
        return Collections.unmodifiableList(this.invokeArgs);
    }

    /**
     * オプションが指定されていたか否か判定する。
     *
     * @param option オプション
     * @return 指定されていたらtrue
     */
    public boolean hasOption(CmdOption option){
        boolean result = this.optionList.contains(option);
        return result;
    }

    /**
     * 真偽値をとるオプション値を返す。
     *
     * <p>複数回指定された場合は最後の値。
     *
     * @param option オプション
     * @return 真偽値。オプション指定がなかった場合はnull
     * @throws IllegalArgumentException 真偽値を取るオプションではない。
     */
    public Boolean getBooleanArg(CmdOption option)
            throws IllegalArgumentException{
        if( ! option.isBooleanOption() ){
            String errMsg =
                    MessageFormat.format(ERRFORM_NONBOOL, option.toString());
            throw new IllegalArgumentException(errMsg);
        }
        Boolean result = this.boolOptionMap.get(option);
        return result;
    }

    /**
     * 文字列引数をとるオプション値を返す。
     *
     * <p>複数回指定された場合は最後の値。
     *
     * @param option オプション
     * @return 文字列。オプション指定がなかった場合はnull
     */
    public String getStringArg(CmdOption option){
        String result = this.stringOptionMap.get(option);
        return result;
    }

    /**
     * 排他的オプションのいずれかが指定されたか判定する。
     *
     * <p>後から指定された方が有効となる。
     *
     * @param options 排他的オプション群
     * @return いずれかのオプション。どれも指定されなければnull
     */
    public CmdOption getExclusiveOption(CmdOption... options){
        CmdOption result = null;
        for(CmdOption option : this.optionList){
            for(CmdOption excOption : options){
                if(option == excOption){
                    result = option;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 初期のフレーム幅を返す。
     *
     * @return 初期のフレーム幅。オプション指定されてなければnull
     */
    public Integer initialFrameWidth(){
        return this.frameWidth;
    }

    /**
     * 初期のフレーム高を返す。
     *
     * @return 初期のフレーム高。オプション指定されてなければnull
     */
    public Integer initialFrameHeight(){
        return this.frameHeight;
    }

    /**
     * 初期のフレーム位置のX座標を返す。
     *
     * @return 初期のフレーム位置のX座標。オプション指定されてなければnull
     */
    public Integer initialFrameXpos(){
        return this.frameXpos;
    }

    /**
     * 初期のフレーム位置のY座標を返す。
     *
     * @return 初期のフレーム位置のY座標。オプション指定されてなければnull
     */
    public Integer initialFrameYpos(){
        return this.frameYpos;
    }

}
