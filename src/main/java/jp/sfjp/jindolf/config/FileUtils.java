/*
 * file utilities
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.config;

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

    private static final Class<?> THISKLASS = FileUtils.class;

    private static final String LANG_JA = "ja";
    private static final String SYSPROP_OSNAME = "os.name";
    private static final String SCHEME_FILE = "file";
    private static final String ENTITY_YEN = "&yen;";

    /** JRE1.6の{@link java.io.File#setReadable(boolean,boolean)}に相当。 */
    private static final Method METHOD_SETREADABLE;
    /** JRE1.6の{@link java.io.File#setWritable(boolean,boolean)}に相当。 */
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
     * パーミッション操作メソッドの下請け。
     * リフレクション機構に関する異常系を吸収する。
     * @param method setReadableかsetWritableのいずれかの2引数版メソッド。
     * @param file 捜査対象のファイル
     * @param flag 操作フラグ
     * @param ownerOnly 対象は所有者のみか否か
     * @return メソッドの戻り値。成功すればtrue
     * @throws InvocationTargetException メソッドが発した異常系
     */
    private static boolean reflectFilePermOp(
            Method method, File file, boolean flag, boolean ownerOnly)
            throws InvocationTargetException {
        Object result;
        try{
            result = method.invoke(file, flag, ownerOnly);
        }catch(IllegalAccessException e){
            assert false;
            throw new AssertionError(e);
        }catch(IllegalArgumentException e){
            assert false;
            throw new AssertionError(e);
        }catch(ExceptionInInitializerError e){
            assert false;
            throw new AssertionError(e);
        }

        assert result instanceof Boolean;
        Boolean boolResult = (Boolean) result;

        return boolResult;
    }

    /**
     * パーミッション操作メソッドの下請け。
     * @param method setReadableかsetWritableのいずれかの2引数版メソッド。
     * @param file 操作対象のファイル。
     * @param flag 操作フラグ
     * @param ownerOnly 対象は所有者のみか否か
     * @return 成功すればtrue
     * @throws SecurityException セキュリティ上の許可が無い場合
     */
    private static boolean invokeFilePermOp(
            Method method, File file, boolean flag, boolean ownerOnly)
            throws SecurityException{
        boolean result;

        try{
            result = reflectFilePermOp(method, file, flag, ownerOnly);
        }catch(InvocationTargetException e){
            Throwable cause = e.getCause();
            if(cause instanceof SecurityException){
                throw (SecurityException) cause;
            }else if(cause instanceof RuntimeException){
                throw (RuntimeException) cause;
            }else if(cause instanceof Error){
                throw (Error) cause;
            }

            assert false;
            throw new AssertionError(e);
        }

        return result;
    }

    /**
     * ファイルの読み込みパーミッションを操作する。
     * JRE1.6の{@link java.io.File#setReadable(boolean,boolean)}
     * の代用品。
     *
     * <p>JRE1.6でなければなにもせずにfalseを返す。
     *
     * @param file ファイルorディレクトリ
     * @param flag 操作フラグ
     * @param ownerOnly 対象は所有者のみか否か
     * @return 成功すればtrue
     * @throws SecurityException セキュリティ上の許可が無い場合
     */
    public static boolean setReadable(
            File file, boolean flag, boolean ownerOnly )
            throws SecurityException {
        if(file == null) throw new NullPointerException();
        if(METHOD_SETREADABLE == null) return false;

        boolean result;
        result = invokeFilePermOp(METHOD_SETREADABLE, file, flag, ownerOnly);
        return result;
    }

    /**
     * ファイルの書き込みパーミッションを操作する。
     * JRE1.6の{@link java.io.File#setWritable(boolean,boolean)}
     * の代用品。
     *
     * <p>JRE1.6でなければなにもせずにfalseを返す。
     *
     * @param file ファイルorディレクトリ
     * @param flag 操作フラグ
     * @param ownerOnly 対象は所有者のみか否か
     * @return 成功すればtrue
     * @throws SecurityException セキュリティ上の許可が無い場合
     */
    public static boolean setWritable(
            File file, boolean flag, boolean ownerOnly )
            throws SecurityException {
        if(file == null) throw new NullPointerException();
        if(METHOD_SETWRITABLE == null) return false;

        boolean result;
        result = invokeFilePermOp(METHOD_SETWRITABLE, file, flag, ownerOnly);
        return result;
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
        boolean result = true;

        result &= setReadable(file, false, false);
        result &= setReadable(file, true,  true);

        result &= setWritable(file, false, false);
        result &= setWritable(file, true, true);

        return result;
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
     * <ul>
     * <li>存在し、
     * <li>かつディレクトリであり、
     * <li>かつ読み込み可能であり、
     * <li>かつ書き込み可能
     * </ul>
     * でなければならない。
     * @param path 任意のディレクトリ
     * @return アクセス可能ならtrue
     */
    public static boolean isAccessibleDirectory(File path){
        if(path == null) return false;

        boolean result = true;

        if     ( ! path.exists() )      result = false;
        else if( ! path.isDirectory() ) result = false;
        else if( ! path.canRead() )     result = false;
        else if( ! path.canWrite() )    result = false;

        return result;
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
     *     JARが見つからない、もしくはロード元がJARファイルでなければnull。
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
     * このクラスがローカルJARファイルからロードされたのであれば
     * その格納ディレクトリを返す。
     * @return ロード元JARファイルの格納ディレクトリ。
     *     JARが見つからない、もしくはロード元がJARファイルでなければnull。
     */
    public static File getJarDirectory(){
        return getJarDirectory(THISKLASS);
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
            osName = System.getProperty(SYSPROP_OSNAME);
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
            osName = System.getProperty(SYSPROP_OSNAME);
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
     *
     * <p>WindowsやLinuxではホームディレクトリ。
     * Mac OS X ではさらにホームディレクトリの下の
     * "Library/Application Support/"
     *
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

        if( FileUtils.isWindowsOSFs() && lang.equals(LANG_JA) ){
            pathName = pathName.replace(File.separator, ENTITY_YEN);
        }

        return "<code>" + pathName + "</code>";
    }

}
