/*
 * environment information
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    /** 最大ヒープメモリ。 */
    public static final long MAX_MEMORY;

    private static final SortedMap<String, String> PROPERTY_MAP;
    private static final SortedMap<String, String> ENVIRONMENT_MAP;

    private static final List<String> CLASSPATHS;

    private static final String[] PROPNAMES = {
        "os.name",
        "os.version",
        "os.arch",
        "java.vendor",
        "java.version",
        "java.class.path",
    };

    private static final String[] ENVNAMES = {
        "LANG",
        "DISPLAY",
        //"PATH",
        //"TEMP",
        //"USER",
    };

    private static final String FORM_MEM =
            "最大ヒープメモリ量: {0,number} bytes\n";
    private static final String INDENT = "\u0020\u0020";
    private static final char NL = '\n';

    static{
        Runtime runtime = Runtime.getRuntime();
        MAX_MEMORY = runtime.maxMemory();

        ENVIRONMENT_MAP = buildEnvMap();

        PROPERTY_MAP = buildPropMap();
        OS_NAME      = PROPERTY_MAP.get("os.name");
        OS_VERSION   = PROPERTY_MAP.get("os.version");
        OS_ARCH      = PROPERTY_MAP.get("os.arch");
        JAVA_VENDOR  = PROPERTY_MAP.get("java.vendor");
        JAVA_VERSION = PROPERTY_MAP.get("java.version");

        String classpath = PROPERTY_MAP.get("java.class.path");
        CLASSPATHS = buildClassPathList(classpath);
    }


    /**
     * 隠れコンストラクタ。
     */
    private EnvInfo(){
        assert false;
    }


    /**
     * 主要環境変数マップを作成する。
     *
     * @return 主要環境変数マップ
     */
    private static SortedMap<String, String> buildEnvMap(){
        SortedMap<String, String> envmap = new TreeMap<>();

        for(String name : ENVNAMES){
            String val;
            try{
                val = System.getenv(name);
            }catch(SecurityException e){
                continue;
            }
            if(val == null) continue;
            envmap.put(name, val);
        }

        SortedMap<String, String> result;
        result = Collections.unmodifiableSortedMap(envmap);

        return result;
    }

    /**
     * 主要システムプロパティ値マップを作成する。
     *
     * @return 主要システムプロパティ値マップ
     */
    private static SortedMap<String, String> buildPropMap(){
        SortedMap<String, String> propmap = new TreeMap<>();

        for(String name : PROPNAMES){
            String val;
            try{
                val = System.getProperty(name);
            }catch(SecurityException e){
                continue;
            }
            if(val == null) continue;
            propmap.put(name, val);
        }

        SortedMap<String, String> result;
        result = Collections.unmodifiableSortedMap(propmap);

        return result;
    }

    /**
     * クラスパスリストを作成する。
     *
     * @param classpath 連結クラスパス値
     * @return クラスパスリスト
     */
    private static List<String> buildClassPathList(String classpath){
        String[] pathArray;
        if(classpath != null){
            pathArray = classpath.split(File.pathSeparator);
        }else{
            pathArray = new String[0];
        }

        List<String> result;
        result = Arrays.asList(pathArray);
        result = Collections.unmodifiableList(result);

        return result;
    }

    /**
     * VM詳細情報を文字列化する。
     * @return VM詳細情報
     */
    public static String getVMInfo(){
        StringBuilder result = new StringBuilder();

        String memform = MessageFormat.format(FORM_MEM, MAX_MEMORY);
        result.append(memform).append(NL);

        result.append(getSysPropInfo()).append(NL);
        result.append(getEnvInfo()).append(NL);
        result.append(getClassPathInfo()).append(NL);

        return result.toString();
    }

    /**
     * システムプロパティ要覧を返す。
     *
     * <p>java.class.pathの値は除く。
     *
     * @return システムプロパティ要覧
     */
    private static CharSequence getSysPropInfo(){
        StringBuilder result = new StringBuilder();
        result.append("主要システムプロパティ:\n");

        PROPERTY_MAP.entrySet().stream()
                .filter(entry -> ! entry.getKey().equals("java.class.path"))
                .forEachOrdered(entry -> {
                    result.append(INDENT);
                    result.append(entry.getKey());
                    result.append('=');
                    result.append(entry.getValue());
                    result.append(NL);
                });

        return result;
    }

    /**
     * 環境変数要覧を返す。
     *
     * @return 環境変数要覧
     */
    private static CharSequence getEnvInfo(){
        StringBuilder result = new StringBuilder("主要環境変数:\n");

        ENVIRONMENT_MAP.entrySet().stream()
                .forEachOrdered(entry -> {
                    result.append(INDENT);
                    result.append(entry.getKey());
                    result.append('=');
                    result.append(entry.getValue());
                    result.append(NL);
                });

        return result;
    }

    /**
     * クラスパス情報要覧を返す。
     *
     * @return クラスパス情報要覧
     */
    private static CharSequence getClassPathInfo(){
        StringBuilder result = new StringBuilder("クラスパス:\n");

        CLASSPATHS.stream().forEachOrdered(path -> {
            result.append(INDENT).append(path).append(NL);
        });

        return result;
    }

}
