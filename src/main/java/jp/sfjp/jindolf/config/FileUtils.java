/*
 * file utilities
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Locale;

/**
 * 諸々のファイル操作ユーティリティ。
 */
public final class FileUtils{

    private static final Class<?> THISKLASS = FileUtils.class;

    private static final String LANG_JA = "ja";
    private static final String SYSPROP_OSNAME = "os.name";
    private static final String SCHEME_FILE = "file";
    private static final String ENTITY_YEN = "&yen;";

    static{
        assert ! ( isMacOSXFs() && isWindowsOSFs() );
        new FileUtils().hashCode();
    }


    /**
     * 隠しコンストラクタ。
     */
    private FileUtils(){
        super();
        assert this.getClass() == THISKLASS;
        return;
    }


    /**
     * 任意のディレクトリがアクセス可能な状態にあるか判定する。
     *
     * <p>アクセス可能の条件を満たすためには、与えられたパスが
     * <ul>
     * <li>存在し、
     * <li>かつディレクトリであり、
     * <li>かつ読み込み可能であり、
     * <li>かつ書き込み可能
     * </ul>
     * でなければならない。
     *
     * @param path 任意のディレクトリ
     * @return アクセス可能ならtrue
     */
    public static boolean isAccessibleDirectory(Path path){
        if(path == null) return false;

        boolean result =
                   Files.exists(path)
                && Files.isDirectory(path)
                && Files.isReadable(path)
                && Files.isWritable(path);

        return result;
    }

    /**
     * クラスのロード元のURLを返す。
     *
     * @param klass クラス
     * @return ロード元URL。不明ならnull
     */
    public static URL getClassSourceUrl(Class<?> klass){
        ProtectionDomain domain = klass.getProtectionDomain();
        if(domain == null) return null;

        CodeSource src = domain.getCodeSource();
        if(src == null) return null;

        URL location = src.getLocation();

        return location;
    }

    /**
     * クラスがローカルファイルからロードされたのであれば
     * そのファイルを返す。
     *
     * @param klass 任意のクラス
     * @return ロード元ファイル。見つからなければnull。
     */
    public static Path getClassSourcePath(Class<?> klass){
        URL location = getClassSourceUrl(klass);
        if(location == null) return null;

        String scheme = location.getProtocol();
        if( ! SCHEME_FILE.equalsIgnoreCase(scheme) ) return null;

        URI uri;
        try{
            uri = location.toURI();
        }catch(URISyntaxException e){
            assert false;
            return null;
        }

        Path result = Paths.get(uri);

        return result;
    }

    /**
     * すでに存在するJARファイルか判定する。
     *
     * <p>ファイルがすでに通常ファイルとしてローカルに存在し、
     * ファイル名の拡張子が「.jar」であれば真と判定される。
     *
     * @param path 任意のファイル
     * @return すでに存在するJARファイルであればtrue
     */
    public static boolean isExistsJarFile(Path path){
        if(path == null) return false;
        if( ! Files.exists(path) ) return false;
        if( ! Files.isRegularFile(path) ) return false;

        Path leaf = path.getFileName();
        assert leaf != null;
        String leafName = leaf.toString();
        boolean result = leafName.matches("^.+\\.[jJ][aA][rR]$");

        return result;
    }

    /**
     * クラスがローカルJARファイルからロードされたのであれば
     * その格納ディレクトリを返す。
     *
     * @param klass 任意のクラス
     * @return ロード元JARファイルの格納ディレクトリ。
     *     JARが見つからない、もしくはロード元がJARファイルでなければnull。
     */
    public static Path getJarDirectory(Class<?> klass){
        Path jarFile = getClassSourcePath(klass);
        if(jarFile == null) return null;

        if( ! isExistsJarFile(jarFile) ){
            return null;
        }

        return jarFile.getParent();
    }

    /**
     * このクラスがローカルJARファイルからロードされたのであれば
     * その格納ディレクトリを返す。
     *
     * @return ロード元JARファイルの格納ディレクトリ。
     *     JARが見つからない、もしくはロード元がJARファイルでなければnull。
     */
    public static Path getJarDirectory(){
        return getJarDirectory(THISKLASS);
    }

    /**
     * ホームディレクトリを得る。
     *
     * <p>システムプロパティuser.homeで示されたホームディレクトリを返す。
     *
     * @return ホームディレクトリ。
     */
    public static Path getHomeDirectory(){
        String homeProp = System.getProperty("user.home");
        Path result = Paths.get(homeProp);
        return result;
    }

    /**
     * MacOSX環境か否か判定する。
     *
     * @return MacOSX環境ならtrue
     */
    public static boolean isMacOSXFs(){
        if(File.separatorChar != '/') return false;

        String osName = System.getProperty(SYSPROP_OSNAME);
        if(osName == null) return false;

        osName = osName.toLowerCase(Locale.ROOT);

        boolean result = osName.startsWith("mac os x");
        return result;
    }

    /**
     * Windows環境か否か判定する。
     *
     * @return Windows環境ならtrue
     */
    public static boolean isWindowsOSFs(){
        if(File.separatorChar != '\\') return false;

        String osName = System.getProperty(SYSPROP_OSNAME);
        if(osName == null) return false;

        osName = osName.toLowerCase(Locale.ROOT);

        boolean result = osName.startsWith("windows");
        return result;
    }

    /**
     * ファイル名を表示するためのJLabel用HTML文字列を生成する。
     *
     * <p>Windows日本語環境では、バックスラッシュ記号が円通貨記号に置換される。
     *
     * @param path 対象ファイル
     * @return HTML文字列断片
     */
    public static String getHtmledFileName(Path path){
        String pathName = path.toString();

        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage();

        if( FileUtils.isWindowsOSFs() && lang.equals(LANG_JA) ){
            pathName = pathName.replace(File.separator, ENTITY_YEN);
        }

        return "<code>" + pathName + "</code>";
    }

}
