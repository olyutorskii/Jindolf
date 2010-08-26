/*
 * command line options
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * コマンドラインオプションの列挙。
 */
public enum CmdOption{

    /** ヘルプ。 */
    OPT_HELP("help", "h", "-help", "?"),
    /** 版数表示。 */
    OPT_VERSION("version"),
    /** UI文字制御。 */
    OPT_BOLDMETAL("boldMetal"),
    /** スプラッシュ制御。 */
    OPT_NOSPLASH("nosplash"),
    /** ウィンドウ位置指定。 */
    OPT_GEOMETRY("geometry"),
    /** 実行環境出力。 */
    OPT_VMINFO("vminfo"),
    /** コンソールログ。 */
    OPT_CONSOLELOG("consolelog"),
    /** フォント指定。 */
    OPT_INITFONT("initfont"),
    /** アンチエイリアス。 */
    OPT_ANTIALIAS("antialias"),
    /** サブピクセル制御。 */
    OPT_FRACTIONAL("fractional"),
    /** 設定格納ディレクトリ指定。 */
    OPT_CONFDIR("confdir"),
    /** 設定格納ディレクトリ不使用。 */
    OPT_NOCONF("noconfdir"),
    ;

    /**
     * オプション名に合致するEnumを返す。
     * @param seq ハイフン付きオプション名
     * @return 合致したEnum。どれとも合致しなければnull
     */
    public static CmdOption parseCmdOption(CharSequence seq){
        for(CmdOption option : values()){
            if(option.matchHyphened(seq)) return option;
        }
        return null;
    }

    /**
     * 単体で意味をなすオプションか判定する。
     * @param option オプション
     * @return 単体で意味をなすならtrue
     */
    public static boolean isIndepOption(CmdOption option){
        switch(option){
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
     * @param option オプション
     * @return 真偽指定を一つ必要とするオプションならtrue
     */
    public static boolean isBooleanOption(CmdOption option){
        switch(option){
        case OPT_ANTIALIAS:
        case OPT_FRACTIONAL:
            return true;
        default:
            break;
        }

        return false;
    }

    /**
     * ヘルプメッセージ（オプションの説明）を返す。
     * @return ヘルプメッセージ
     */
    public static CharSequence getHelpText(){
        CharSequence helpText;

        try{
            helpText = Jindolf.loadResourceText("resources/help.txt");
        }catch(IOException e){
            helpText = "";
        }

        return helpText;
    }

    private final List<String> nameList = new LinkedList<String>();

    /**
     * コンストラクタ。
     * @param names 頭のハイフンを除いたオプション名の一覧
     */
    private CmdOption(CharSequence ... names){
        if(names == null) throw new NullPointerException();
        if(names.length <= 0) throw new IllegalArgumentException();

        for(CharSequence name : names){
            if(name == null) throw new NullPointerException();
            this.nameList.add(name.toString().intern());
        }

        return;
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

    /**
     * 頭のハイフンが付いたオプション名を返す。
     * オプション名が複数指定されていた場合は最初のオプション名
     * @return オプション名
     */
    public String toHyphened(){
        return "-" + toString();
    }

    /**
     * 任意のオプション文字列がこのオプションに合致するか判定する。
     * @param option ハイフンの付いたオプション文字列
     * @return 合致すればtrue
     */
    public boolean matchHyphened(CharSequence option){
        if(option == null) return false;

        for(String name : this.nameList){
            String hyphened = "-" + name;
            if(hyphened.equals(option.toString())) return true;
        }

        return false;
    }

}
