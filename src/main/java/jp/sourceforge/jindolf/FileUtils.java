/*
 * file utilities
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Locale;

/**
 * 諸々のファイル操作ユーティリティ。
 * JRE1.6 API へのリフレクションアクセスを含む。
 */
public final class FileUtils{

    private static final String SCHEME_FILE = "file";

    /** JRE1.6のjava.io.File#setReadableに相当。 */
    private static final Method METHOD_SETREADABLE;
    /** JRE1.6のjava.io.File#setWritableに相当。 */
    private static final Method METHOD_SETWRITABLE;
    /** Locale.ROOT代替品。 */
    private static final Locale ROOT = new Locale("", "", "");

    static{
        Method method;
        int modifiers;

        try{
            method = File.class.getMethod(
                    "setReadable", Boolean.TYPE, Boolean.TYPE);
            modifiers = method.getModifiers();
            if( ! Modifier.isPublic(modifiers) ){
                method = null;
            }
        }catch(NoSuchMethodException e){
            method = null;
        }catch(SecurityException e){
            method = null;
        }
        METHOD_SETREADABLE = method;

        try{
            method = File.class.getMethod(
                    "setWritable", Boolean.TYPE, Boolean.TYPE);
            modifiers = method.getModifiers();
            if( ! Modifier.isPublic(modifiers) ){
                method = null;
            }
        }catch(NoSuchMethodException e){
            method = null;
        }catch(SecurityException e){
            method = null;
        }
        METHOD_SETWRITABLE = method;

        assert ! ( isMacOSXFs() && isWindowsOSFs() );
    }

    /**
     * なるべく自分にだけ許可を与え自分以外には許可を与えないように
     * ファイル属性を操作する。
     * @param method setReadableかsetWritableのいずれかのメソッド。
     * nullならなにもしない。
     * @param file 操作対象のファイル。
     * @return 成功すればtrue
     * @throws SecurityException セキュリティ上の許可が無い場合
     */
    private static boolean invokeOwnerOnly(Method method, File file)
            throws SecurityException{
        if(method == null) return false;
        if(file == null) throw new NullPointerException();

        Object result1;
        Object result2;
        try{
            result1 = method.invoke(file, false, false);
            result2 = method.invoke(file, true,  true);
        }catch(IllegalAccessException e){
            assert false;
            return false;
        }catch(IllegalArgumentException e){
            assert false;
            return false;
        }catch(ExceptionInInitializerError e){
            assert false;
            return false;
        }catch(InvocationTargetException e){
            Throwable cause = e.getCause();
            if(cause instanceof SecurityException){
                throw (SecurityException) cause;
            }else if(cause instanceof RuntimeException){
                throw (RuntimeException) cause;
            }else if(cause instanceof Error){
                throw (Error) cause;
            }else{
                assert false;
            }
            return false;
        }

        assert result1 instanceof Boolean;
        assert result2 instanceof Boolean;
        Boolean bresult1 = (Boolean) result1;
        Boolean bresult2 = (Boolean) result2;

        return bresult1 && bresult2;
    }

    /**
     * なるべく自分にだけ読み書き許可を与え
     * 自分以外には読み書き許可を与えないように
     * ファイル属性を操作する。
     * JRE1.6環境でなければなにもしない。
     * @param file 操作対象ファイル
     * @return 成功すればtrue
     * @throws SecurityException セキュリティ上の許可が無い場合
     */
    public static boolean setOwnerOnlyAccess(File file)
            throws SecurityException{
        boolean readresult  = invokeOwnerOnly(METHOD_SETREADABLE, file);
        boolean writeresult = invokeOwnerOnly(METHOD_SETWRITABLE, file);
        return readresult & writeresult;
    }

    /**
     * 任意の絶対パスの祖先の内、存在するもっとも近い祖先を返す。
     * @param file 任意の絶対パス
     * @return 存在するもっとも近い祖先。一つも存在しなければnull。
     * @throws IllegalArgumentException 引数が絶対パスでない
     */
    public static File findExistsAncestor(File file)
            throws IllegalArgumentException{
        if(file == null) return null;
        if( ! file.isAbsolute() ) throw new IllegalArgumentException();
        if(file.exists()) return file;
        File parent = file.getParentFile();
        return findExistsAncestor(parent);
    }

    /**
     * 任意の絶対パスのルートファイルシステムもしくはドライブレターを返す。
     * @param file 任意の絶対パス
     * @return ルートファイルシステムもしくはドライブレター
     * @throws IllegalArgumentException 引数が絶対パスでない
     */
    public static File findRootFile(File file)
            throws IllegalArgumentException{
        if( ! file.isAbsolute() ) throw new IllegalArgumentException();
        File parent = file.getParentFile();
        if(parent == null) return file;
        return findRootFile(parent);
    }

    /**
     * 相対パスの絶対パス化を試みる。
     * @param file 対象パス
     * @return 絶対パス。絶対化に失敗した場合は元の引数。
     */
    public static File supplyFullPath(File file){
        if(file.isAbsolute()) return file;

        File absFile;

        try{
            absFile = file.getAbsoluteFile();
        }catch(SecurityException e){
            return file;
        }

        return absFile;
    }

    /**
     * 任意のディレクトリがアクセス可能な状態にあるか判定する。
     * アクセス可能の条件を満たすためには、与えられたパスが
     * 存在し、
     * かつディレクトリであり、
     * かつ読み込み可能であり、
     * かつ書き込み可能
     * でなければならない。
     * @param path 任意のディレクトリ
     * @return アクセス可能ならtrue
     */
    public static boolean isAccessibleDirectory(File path){
        if(path == null) return false;

        if( ! path.exists() )      return false;
        if( ! path.isDirectory() ) return false;
        if( ! path.canRead() )     return false;
        if( ! path.canWrite() )    return false;

        return true;
    }

    /**
     * クラスがローカルファイルからロードされたのであれば
     * そのファイルを返す。
     * @param klass 任意のクラス
     * @return ロード元ファイル。見つからなければnull。
     */
    public static File getClassSourceFile(Class<?> klass){
        ProtectionDomain domain;
        try{
            domain = klass.getProtectionDomain();
        }catch(SecurityException e){
            return null;
        }

        CodeSource src = domain.getCodeSource();

        URL location = src.getLocation();
        String scheme = location.getProtocol();
        if( ! scheme.equals(SCHEME_FILE) ) return null;

        URI uri;
        try{
            uri = location.toURI();
        }catch(URISyntaxException e){
            assert false;
            return null;
        }

        File file = new File(uri);

        return file;
    }

    /**
     * すでに存在するJARファイルか判定する。
     * @param file 任意のファイル
     * @return すでに存在するJARファイルであればtrue
     */
    public static boolean isExistsJarFile(File file){
        if(file == null) return false;
        if( ! file.exists() ) return false;
        if( ! file.isFile() ) return false;

        String name = file.getName();
        if( ! name.matches("^.+\\.[jJ][aA][rR]$") ) return false;

        // TODO ファイル先頭マジックナンバーのテストも必要？

        return true;
    }

    /**
     * クラスがローカルJARファイルからロードされたのであれば
     * その格納ディレクトリを返す。
     * @param klass 任意のクラス
     * @return ロード元JARファイルの格納ディレクトリ。
     * JARが見つからない、もしくはロード元がJARファイルでなければnull。
     */
    public static File getJarDirectory(Class<?> klass){
        File jarFile = getClassSourceFile(klass);
        if(jarFile == null) return null;

        if( ! isExistsJarFile(jarFile) ){
            return null;
        }

        return jarFile.getParentFile();
    }

    /**
     * ホームディレクトリを得る。
     * システムプロパティuser.homeで示されたホームディレクトリを返す。
     * @return ホームディレクトリ。何らかの事情でnullを返す場合もあり。
     */
    public static File getHomeDirectory(){
        String homeProp;
        try{
            homeProp = System.getProperty("user.home");
        }catch(SecurityException e){
            return null;
        }

        File homeFile = new File(homeProp);

        return homeFile;
    }

    /**
     * MacOSX環境か否か判定する。
     * @return MacOSX環境ならtrue
     */
    public static boolean isMacOSXFs(){
        if(File.separatorChar != '/') return false;

        String osName;
        try{
            osName = System.getProperty("os.name");
        }catch(SecurityException e){
            return false;
        }

        if(osName == null) return false;

        osName = osName.toLowerCase(ROOT);

        if(osName.startsWith("mac os x")){
            return true;
        }

        return false;
    }

    /**
     * Windows環境か否か判定する。
     * @return Windows環境ならtrue
     */
    public static boolean isWindowsOSFs(){
        if(File.separatorChar != '\\') return false;

        String osName;
        try{
            osName = System.getProperty("os.name");
        }catch(SecurityException e){
            return false;
        }

        if(osName == null) return false;

        osName = osName.toLowerCase(ROOT);

        if(osName.startsWith("windows")){
            return true;
        }

        return false;
    }

    /**
     * アプリケーション設定ディレクトリを返す。
     * 存在の有無、アクセスの可否は関知しない。
     * @return アプリケーション設定ディレクトリ
     */
    public static File getAppSetDir(){
        File home = getHomeDirectory();
        if(home == null) return null;

        File result = home;

        if(isMacOSXFs()){
            result = new File(result, "Library");
            result = new File(result, "Application Support");
        }

        // TODO Win環境での%APPDATA%サポート

        return result;
    }

    /**
     * ファイル名を表示するためのJLabel用HTML文字列を生成する。
     * Windows日本語環境では、バックスラッシュ記号が円通貨記号に置換される。
     * @param file 対象ファイル
     * @return HTML文字列断片
     */
    public static String getHtmledFileName(File file){
        String pathName = file.getPath();

        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage();

        if( FileUtils.isWindowsOSFs() && lang.equals("ja") ){
            pathName = pathName.replace(File.separator, "&yen;");
        }

        return "<code>" + pathName + "</code>";
    }

    /**
     * 隠しコンストラクタ。
     */
    private FileUtils(){
        assert false;
        throw new AssertionError();
    }

}
