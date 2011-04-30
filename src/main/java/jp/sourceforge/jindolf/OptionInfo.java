/*
 * option argument information
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

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
 * public static void main()の引数から展開される。
 */
public class OptionInfo{

    private static final Pattern PATTERN_GEOMETRY =
            Pattern.compile(
                 "([1-9][0-9]*)x([1-9][0-9]*)"
                +"(?:(\\+|\\-)([1-9][0-9]*)(\\+|\\-)([1-9][0-9]*))?"
                );


    private Integer frameWidth  = null;
    private Integer frameHeight = null;
    private Integer frameXpos = null;
    private Integer frameYpos = null;

    private final List<String> invokeArgs = new LinkedList<String>();
    private final List<CmdOption> optionList = new LinkedList<CmdOption>();
    private final Map<CmdOption, Boolean> boolOptionMap =
            new EnumMap<CmdOption, Boolean>(CmdOption.class);
    private final Map<CmdOption, String> stringOptionMap =
            new EnumMap<CmdOption, String>(CmdOption.class);


    /**
     * コンストラクタ。
     */
    protected OptionInfo(){
        super();
        return;
    }


    /**
     * オプション文字列を解析する。
     * @param args main()に渡されるオプション文字列
     * @return 解析済みのオプション情報。
     * @throws IllegalArgumentException 構文エラー
     */
    public static OptionInfo parseOptions(String[] args)
            throws IllegalArgumentException{
        OptionInfo result = new OptionInfo();

        result.invokeArgs.clear();
        for(String arg : args){
            if(arg == null) continue;
            if(arg.length() <= 0) continue;
            result.invokeArgs.add(arg);
        }
        Iterator<String> iterator = result.invokeArgs.iterator();

        while(iterator.hasNext()){
            String arg = iterator.next();

            CmdOption option = CmdOption.parseCmdOption(arg);
            if(option == null){
                throw new IllegalArgumentException(
                        "未定義の起動オプション["
                        + arg
                        + "]が指定されました。");
            }
            result.optionList.add(option);

            if(CmdOption.isIndepOption(option)){
                continue;
            }else if(CmdOption.isBooleanOption(option)){
                Boolean bool = parseBooleanSwitch(arg, iterator);
                result.boolOptionMap.put(option, bool);
                continue;
            }

            switch(option){
            case OPT_INITFONT:
            case OPT_CONFDIR:
                checkNextArg(arg, iterator);
                result.stringOptionMap.put(option, iterator.next());
                break;
            case OPT_GEOMETRY:
                checkNextArg(arg, iterator);
                String geometry = iterator.next();
                Matcher matcher = PATTERN_GEOMETRY.matcher(geometry);
                if( ! matcher.matches() ){
                    throw new IllegalArgumentException(
                            "起動オプション["
                            +arg
                            +"]の引数形式["
                            +geometry
                            +"]が不正です。" );
                }
                String width  = matcher.group(1);
                String height = matcher.group(2);
                String xSign  = matcher.group(3);
                String xPos   = matcher.group(4);
                String ySign  = matcher.group(5);
                String yPos   = matcher.group(6);
                try{
                    result.frameWidth  = Integer.parseInt(width);
                    result.frameHeight = Integer.parseInt(height);
                    if(xPos != null && xPos.length() > 0){
                        result.frameXpos = Integer.parseInt(xPos);
                        if(xSign.equals("-")){
                            result.frameXpos = -result.frameXpos;
                        }
                    }
                    if(yPos != null && yPos.length() > 0){
                        result.frameYpos = Integer.parseInt(yPos);
                        if(ySign.equals("-")){
                            result.frameYpos = -result.frameYpos;
                        }
                    }
                }catch(NumberFormatException e){
                    assert false;
                    throw new IllegalArgumentException(e);
                }

                break;
            default:
                assert false;
                break;
            }
        }

        return result;
    }

    /**
     * 真偽二値をとるオプション解析の下請け。
     * @param option オプション名
     * @param iterator 引数並び
     * @return 真偽
     * @throws IllegalArgumentException 構文エラー
     */
    private static Boolean parseBooleanSwitch(
            String option, Iterator<String> iterator )
                throws IllegalArgumentException{
        Boolean result;
        checkNextArg(option, iterator);
        String onoff = iterator.next();
        if(   onoff.compareToIgnoreCase("on"  ) == 0
           || onoff.compareToIgnoreCase("yes" ) == 0
           || onoff.compareToIgnoreCase("true") == 0){
            result = Boolean.TRUE;
        }else if(   onoff.compareToIgnoreCase("off"  ) == 0
                 || onoff.compareToIgnoreCase("no"   ) == 0
                 || onoff.compareToIgnoreCase("false") == 0){
            result = Boolean.FALSE;
        }else{
            throw new IllegalArgumentException(
                    "起動オプション["
                    +option
                    +"]の引数形式["
                    +onoff
                    +"]が不正です。"
                    +"on, off, yes, no, true, false"
                    +"のいずれかを指定してください。");
        }
        return result;
    }

    /**
     * 追加引数を持つオプションのチェック。
     * @param option オプション名
     * @param iterator 引数並び
     * @throws IllegalArgumentException 構文エラー
     */
    private static void checkNextArg(CharSequence option,
                                       Iterator<String> iterator )
                                       throws IllegalArgumentException{
        if( ! iterator.hasNext() ){
            throw new IllegalArgumentException(
                    "起動オプション["
                    +option
                    +"]に引数がありません。");
        }
        return;
    }


    /**
     * 全引数のリストを返す。
     * @return 全引数のリスト
     */
    public List<String> getInvokeArgList(){
        return Collections.unmodifiableList(this.invokeArgs);
    }

    /**
     * オプションが指定されていたか否か判定する。
     * @param option オプション
     * @return 指定されていたらtrue
     */
    public boolean hasOption(CmdOption option){
        if(this.optionList.contains(option)) return true;
        return false;
    }

    /**
     * 真偽値をとるオプション値を返す。
     * 複数回指定された場合は最後の値。
     * @param option オプション
     * @return 真偽値。オプション指定がなかった場合はnull
     * @throws IllegalArgumentException 真偽値を取るオプションではない。
     */
    public Boolean getBooleanArg(CmdOption option)
            throws IllegalArgumentException{
        if( ! CmdOption.isBooleanOption(option) ){
            throw new IllegalArgumentException();
        }
        Boolean result = this.boolOptionMap.get(option);
        return result;
    }

    /**
     * 文字列引数をとるオプション値を返す。
     * 複数回指定された場合は最後の値。
     * @param option オプション
     * @return 文字列。オプション指定がなかった場合はnull
     */
    public String getStringArg(CmdOption option){
        String result = this.stringOptionMap.get(option);
        return result;
    }

    /**
     * 排他的オプションのいずれかが指定されたか判定する。
     * 後から指定された方が有効となる。
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
     * @return 初期のフレーム幅。オプション指定されてなければnull
     */
    public Integer initialFrameWidth(){
        return this.frameWidth;
    }

    /**
     * 初期のフレーム高を返す。
     * @return 初期のフレーム高。オプション指定されてなければnull
     */
    public Integer initialFrameHeight(){
        return this.frameHeight;
    }

    /**
     * 初期のフレーム位置のX座標を返す。
     * @return 初期のフレーム位置のX座標。オプション指定されてなければnull
     */
    public Integer initialFrameXpos(){
        return this.frameXpos;
    }

    /**
     * 初期のフレーム位置のY座標を返す。
     * @return 初期のフレーム位置のY座標。オプション指定されてなければnull
     */
    public Integer initialFrameYpos(){
        return this.frameYpos;
    }

}
