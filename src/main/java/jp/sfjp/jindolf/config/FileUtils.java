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
     * 任意の絶対パスの祖先の内、存在するもっとも近い祖先を返す。
     *
     * @param file 任意の絶対パス
     * @return 存在するもっとも近い祖先。一つも存在しなければnull。
     * @throws IllegalArgumentException 引数が絶対パスでない
     */
    public static Path findExistsAncestor(Path file)
            throws IllegalArgumentException{
        if(file == null) return null;
        if( ! file.isAbsolute() ) throw new IllegalArgumentException();
        if(Files.exists(file)) return file;
        Path parent = file.getParent();
        return findExistsAncestor(parent);
    }

    /**
     * 相対パスの絶対パス化を試みる。
     *
     * @param file 対象パス
     * @return 絶対パス。絶対化に失敗した場合は元の引数。
     */
    public static Path supplyFullPath(Path file){
        Path absFile;

        try{
            absFile = file.toAbsolutePath();
        }catch(SecurityException e){
            return file;
        }

        return absFile;
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
     * クラスがローカルファイルからロードされたのであれば
     * そのファイルを返す。
     *
     * @param klass 任意のクラス
     * @return ロード元ファイル。見つからなければnull。
     */
    public static Path getClassSourceFile(Class<?> klass){
        ProtectionDomain domain;
        try{
            domain = klass.getProtectionDomain();
        }catch(SecurityException e){
            return null;
        }

        CodeSource src = domain.getCodeSource();
        if(src == null) return null;

        URL location = src.getLocation();
        if(location == null) return null;

        String scheme = location.getProtocol();
        if( ! SCHEME_FILE.equals(scheme) ) return null;

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
     * @param path 任意のファイル
     * @return すでに存在するJARファイルであればtrue
     */
    public static boolean isExistsJarFile(Path path){
        if(path == null) return false;
        if( ! Files.exists(path) ) return false;
        if( ! Files.isRegularFile(path) ) return false;

        String name = path.getFileName().toString();
        boolean result = name.matches("^.+\\.[jJ][aA][rR]$");

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
        Path jarFile = getClassSourceFile(klass);
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
     * @return ホームディレクトリ。何らかの事情でnullを返す場合もあり。
     */
    public static Path getHomeDirectory(){
        String homeProp;
        try{
            homeProp = System.getProperty("user.home");
        }catch(SecurityException e){
            return null;
        }

        File homeFile = new File(homeProp);
        Path result = homeFile.toPath();

        return result;
    }

    /**
     * MacOSX環境か否か判定する。
     *
     * @return MacOSX環境ならtrue
     */
    public static boolean isMacOSXFs(){
        if(File.separatorChar != '/') return false;

        String osName;
        try{
            osName = System.getProperty(SYSPROP_OSNAME);
        }catch(SecurityException e){
            return false;
        }

        if(osName == null) return false;

        osName = osName.toLowerCase(Locale.ROOT);

        if(osName.startsWith("mac os x")){
            return true;
        }

        return false;
    }

    /**
     * Windows環境か否か判定する。
     *
     * @return Windows環境ならtrue
     */
    public static boolean isWindowsOSFs(){
        if(File.separatorChar != '\\') return false;

        String osName;
        try{
            osName = System.getProperty(SYSPROP_OSNAME);
        }catch(SecurityException e){
            return false;
        }

        if(osName == null) return false;

        osName = osName.toLowerCase(Locale.ROOT);

        if(osName.startsWith("windows")){
            return true;
        }

        return false;
    }

    /**
     * アプリケーション設定ディレクトリを返す。
     *
     * <p>存在の有無、アクセスの可否は関知しない。
     *
     * <p>WindowsやLinuxではホームディレクトリ。
     * Mac OS X ではさらにホームディレクトリの下の
     * "Library/Application Support/"
     *
     * @return アプリケーション設定ディレクトリ
     */
    public static Path getAppSetDir(){
        Path home = getHomeDirectory();
        if(home == null) return null;

        Path result = home;

        if(isMacOSXFs()){
            result = result.resolve("Library");
            result = result.resolve("Application Support");
        }

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
