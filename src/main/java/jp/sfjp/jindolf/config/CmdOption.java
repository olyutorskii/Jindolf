/*
 * command line options
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import jp.sfjp.jindolf.ResourceManager;

/**
 * コマンドラインオプションの列挙。
 *
 * <p>1オプションは複数の別名を持ちうる。
 *
 * <p>1引数を持つオプションと持たないオプションは区別される。
 */
public enum CmdOption {

    /** ヘルプ。 */
    OPT_HELP("-help", "-h", "--help", "-?"),
    /** 版数表示。 */
    OPT_VERSION("-version"),
    /** UI文字制御。 */
    OPT_BOLDMETAL("-boldMetal"),
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


    private static final Collection<CmdOption> OPTS_INDEPENDENT =
            EnumSet.of(
            OPT_HELP,
            OPT_VERSION,
            OPT_VMINFO,
            OPT_BOLDMETAL,
            OPT_CONSOLELOG,
            OPT_NOCONF
            );
    private static final Collection<CmdOption> OPTS_BOOLEAN =
            EnumSet.of(
            OPT_ANTIALIAS,
            OPT_FRACTIONAL
            );

    private static final String RES_DIR = "resources";
    private static final String RES_HELPTEXT = RES_DIR + "/help.txt";


    private final List<String> nameList;


    /**
     * コンストラクタ。
     *
     * @param names オプション名の一覧
     */
    private CmdOption(String ... names){
        assert names.length > 0;
        this.nameList = Arrays.asList(names);
        return;
    }


    /**
     * ヘルプメッセージ（オプションの説明）を返す。
     *
     * @return ヘルプメッセージ
     */
    public static String getHelpText(){
        String helpText = ResourceManager.getTextFile(RES_HELPTEXT);
        return helpText;
    }

    /**
     * オプション名に合致するEnumを返す。
     *
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
     *
     * @param option ハイフンの付いたオプション文字列
     * @return 合致すればtrue
     */
    public boolean matches(String option){
        boolean result = this.nameList.contains(option);
        return result;
    }

    /**
     * 単体で意味をなすオプションか判定する。
     *
     * @return 単体で意味をなすならtrue
     */
    public boolean isIndepOption(){
        boolean result = OPTS_INDEPENDENT.contains(this);
        return result;
    }

    /**
     * 真偽指定を一つ必要とするオプションか判定する。
     *
     * @return 真偽指定を一つ必要とするオプションならtrue
     */
    public boolean isBooleanOption(){
        boolean result = OPTS_BOOLEAN.contains(this);
        return result;
    }

    /**
     * オプション名を返す。
     *
     * <p>オプション別名が複数指定されている場合は最初のオプション名
     *
     * @return オプション名
     */
    @Override
    public String toString(){
        String result = this.nameList.get(0);
        return result;
    }

}
