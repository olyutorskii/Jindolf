/*
 * command line options
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.util.Arrays;
import java.util.List;
import jp.sfjp.jindolf.ResourceManager;

/**
 * コマンドラインオプションの列挙。
 */
public enum CmdOption {

    /** ヘルプ。 */
    OPT_HELP("-help", "-h", "--help", "-?"),
    /** 版数表示。 */
    OPT_VERSION("-version"),
    /** UI文字制御。 */
    OPT_BOLDMETAL("-boldMetal"),
    /** スプラッシュ制御。 */
    OPT_NOSPLASH("-nosplash"),
    /** ウィンドウ位置指定。 */
    OPT_GEOMETRY("-geometry"),
    /** 実行環境出力。 */
    OPT_VMINFO("-vminfo"),
    /** コンソールログ。 */
    OPT_CONSOLELOG("-consolelog"),
    /** フォント指定。 */
    OPT_INITFONT("-initfont"),
    /** アンチエイリアス。 */
    OPT_ANTIALIAS("-antialias"),
    /** サブピクセル制御。 */
    OPT_FRACTIONAL("-fractional"),
    /** 設定格納ディレクトリ指定。 */
    OPT_CONFDIR("-confdir"),
    /** 設定格納ディレクトリ不使用。 */
    OPT_NOCONF("-noconfdir"),
    ;


    private static final String RES_HELPTEXT = "resources/help.txt";


    private final List<String> nameList;


    /**
     * コンストラクタ。
     * @param names 頭のハイフンを除いたオプション名の一覧
     */
    private CmdOption(String ... names){
        assert names.length > 0;
        this.nameList = Arrays.asList(names);
        return;
    }


    /**
     * ヘルプメッセージ（オプションの説明）を返す。
     * @return ヘルプメッセージ
     */
    public static CharSequence getHelpText(){
        CharSequence helpText =
            ResourceManager.getTextFile(RES_HELPTEXT);

        return helpText;
    }

    /**
     * オプション名に合致するEnumを返す。
     * @param arg 個別のコマンドライン引数
     * @return 合致したEnum。どれとも合致しなければnull
     */
    public static CmdOption parseCmdOption(String arg){
        for(CmdOption option : values()){
            if(option.matches(arg)) return option;
        }
        return null;
    }

    /**
     * 任意のオプション文字列がこのオプションに合致するか判定する。
     * @param option ハイフンの付いたオプション文字列
     * @return 合致すればtrue
     */
    public boolean matches(String option){
        for(String name : this.nameList){
            if(option.equals(name)) return true;
        }

        return false;
    }

    /**
     * 単体で意味をなすオプションか判定する。
     * @return 単体で意味をなすならtrue
     */
    public boolean isIndepOption(){
        switch(this){
        case OPT_HELP:
        case OPT_VERSION:
        case OPT_VMINFO:
        case OPT_BOLDMETAL:
        case OPT_NOSPLASH:
        case OPT_CONSOLELOG:
        case OPT_NOCONF:
            return true;
        default:
            break;
        }

        return false;
    }

    /**
     * 真偽指定を一つ必要とするオプションか判定する。
     * @return 真偽指定を一つ必要とするオプションならtrue
     */
    public boolean isBooleanOption(){
        switch(this){
        case OPT_ANTIALIAS:
        case OPT_FRACTIONAL:
            return true;
        default:
            break;
        }

        return false;
    }

    /**
     * 頭のハイフンを除いたオプション名を返す。
     * オプション名が複数指定されていた場合は最初のオプション名
     * @return オプション名
     */
    @Override
    public String toString(){
        return this.nameList.get(0);
    }

}
