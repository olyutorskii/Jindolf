/*
 * environment information
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.io.File;
import java.text.NumberFormat;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 実行環境に関する各種情報。
 */
public final class EnvInfo{

    /** OS名。 */
    public static final String OS_NAME;
    /** OSバージョン。 */
    public static final String OS_VERSION;
    /** アーキテクチャ種別。 */
    public static final String OS_ARCH;
    /** Java実行系ベンダ。 */
    public static final String JAVA_VENDOR;
    /** Java実行形バージョン。 */
    public static final String JAVA_VERSION;

    private static final SortedMap<String, String> propertyMap =
            new TreeMap<String, String>();

    private static final SortedMap<String, String> environmentMap =
            new TreeMap<String, String>();

    private static final String[] classpaths;

    static{
        OS_NAME      = getSecureProperty("os.name");
        OS_VERSION   = getSecureProperty("os.version");
        OS_ARCH      = getSecureProperty("os.arch");
        JAVA_VENDOR  = getSecureProperty("java.vendor");
        JAVA_VERSION = getSecureProperty("java.version");

        getSecureEnvironment("LANG");
        getSecureEnvironment("DISPLAY");

        String classpath   = getSecureProperty("java.class.path");
        if(classpath != null){
            classpaths = classpath.split(File.pathSeparator);
        }else{
            classpaths = new String[0];
        }
    }


    /**
     * 隠れコンストラクタ。
     */
    private EnvInfo(){
        throw new AssertionError();
    }


    /**
     * 可能ならシステムプロパティを読み込む。
     * @param key キー
     * @return プロパティ値。セキュリティ上読み込み禁止の場合はnull。
     */
    private static String getSecureProperty(String key){
        String result;
        try{
            result = System.getProperty(key);
            if(result != null) propertyMap.put(key, result);
        }catch(SecurityException e){
            result = null;
        }
        return result;
    }

    /**
     * 可能なら環境変数を読み込む。
     * @param name 環境変数名
     * @return 環境変数値。セキュリティ上読み込み禁止の場合はnull。
     */
    private static String getSecureEnvironment(String name){
        String result;
        try{
            result = System.getenv(name);
            if(result != null) environmentMap.put(name, result);
        }catch(SecurityException e){
            result = null;
        }
        return result;
    }

    /**
     * VM詳細情報を文字列化する。
     * @return VM詳細情報
     */
    public static String getVMInfo(){
        StringBuilder result = new StringBuilder();
        NumberFormat nform = NumberFormat.getNumberInstance();

        result.append("最大ヒープメモリ量: "
                + nform.format(Jindolf.RUNTIME.maxMemory()) + " bytes\n");

        result.append("\n");

        result.append("起動時引数:\n");
        for(String arg : Jindolf.getOptionInfo().getInvokeArgList()){
            result.append("  ").append(arg).append("\n");
        }

        result.append("\n");

        result.append("主要システムプロパティ:\n");
        Set<String> propKeys = propertyMap.keySet();
        for(String propKey : propKeys){
            if(propKey.equals("java.class.path")) continue;
            String value = propertyMap.get(propKey);
            result.append("  ");
            result.append(propKey).append("=").append(value).append("\n");
        }

        result.append("\n");

        result.append("主要環境変数:\n");
        Set<String> envKeys = environmentMap.keySet();
        for(String envKey : envKeys){
            String value = environmentMap.get(envKey);
            result.append("  ");
            result.append(envKey).append("=").append(value).append("\n");
        }

        result.append("\n");

        result.append("クラスパス:\n");
        for(String path : classpaths){
            result.append("  ");
            result.append(path).append("\n");
        }

        result.append("\n");

        return result.toString();
    }

}
