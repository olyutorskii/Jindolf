/*
 * Inter Process Communication with Web browser
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

/**
 * Webブラウザとのプロセス間通信。
 * java.awt.Desktopの代用品。
 * JRE1.6でしか使えない。がしかし、JRE1.5でもコンパイル＆ロード可能。
 * ※参照： java.awt.Desktop
 */
public final class WebIPC{

    private static final Class<?> DESKTOP_KLASS;
    private static final Method   METHOD_ISDESKTOPSUPPORTED;
    private static final Method   METHOD_GETDESKTOP;
    private static final Method   METHOD_ISSUPPORTED;
    private static final Method   METHOD_BROWSE;

    private static final Class<?> DESKTOP_ACTION_KLASS;
    private static final Enum<?>  BROWSE_ENUM;

    static{
        DESKTOP_KLASS       = getClass("java.awt.Desktop");
        DESKTOP_ACTION_KLASS = getInnerClass(DESKTOP_KLASS,
                                           "java.awt.Desktop.Action");

        METHOD_ISDESKTOPSUPPORTED = getMethod("isDesktopSupported");
        METHOD_GETDESKTOP         = getMethod("getDesktop");
        METHOD_ISSUPPORTED        = getMethod("isSupported",
                                             DESKTOP_ACTION_KLASS);
        METHOD_BROWSE             = getMethod("browse",
                                             URI.class);

        BROWSE_ENUM = getEnumMember(DESKTOP_ACTION_KLASS, "BROWSE");
    }


    private final Object desktop;


    /**
     * 見えないコンストラクタ。
     * @throws HeadlessException GUI環境が未接続
     * @throws UnsupportedOperationException 未サポート
     */
    private WebIPC()
            throws HeadlessException,
                   UnsupportedOperationException {
        super();

        try{
            this.desktop = METHOD_GETDESKTOP.invoke(null, (Object[]) null);
        }catch(InvocationTargetException e){
            Throwable targetException = e.getTargetException();

            if(targetException instanceof RuntimeException){
                throw (RuntimeException) targetException;
            }
            if(targetException instanceof Error){
                throw (Error) targetException;
            }

            AssertionError thw = new AssertionError();
            thw.initCause(e);
            throw thw;
        }catch(IllegalAccessException e){
            AssertionError thw = new AssertionError();
            thw.initCause(e);
            throw thw;
        }

        return;
    }


    /**
     * クラス名からClassインスタンスを探す。
     * @param klassName クラス名
     * @return Classインスタンス。クラスが見つからなければnull。
     */
    private static Class<?> getClass(String klassName){
        Class<?> result;
        try{
            result = Class.forName(klassName);
        }catch(ClassNotFoundException e){
            result = null;
        }
        return result;
    }

    /**
     * 内部クラス名からClassインスタンスを探す。
     * @param parent 囲む親クラス。
     * @param canonical 内部クラスのカノニカル名
     * @return Classインスタンス。クラスが見つからなければnull。
     */
    private static Class<?> getInnerClass(Class<?> parent,
                                            String canonical){
        if(parent == null) return null;

        Class<?> result = null;

        Class<?>[] innerKlasses = parent.getClasses();
        for(Class<?> klass : innerKlasses){
            if(klass.getCanonicalName().equals(canonical)){
                result = klass;
                break;
            }
        }

        return result;
    }

    /**
     * Desktopクラスのメソッド名からMethodインスタンスを探す。
     * @param methodName メソッド名
     * @param paramTypes 引数型並び
     * @return Methodインスタンス。見つからなければnull。
     */
    private static Method getMethod(String methodName,
                                     Class<?>... paramTypes){
        if(DESKTOP_KLASS == null) return null;

        Method result;

        try{
            result = DESKTOP_KLASS.getMethod(methodName, paramTypes);
        }catch(NoSuchMethodException e){
            result = null;
        }

        return result;
    }

    /**
     * Enumのメンバを探す。
     * @param parent Enumの型
     * @param memberName メンバ名
     * @return Enumインスタンス。見つからなければnull。
     */
    private static Enum<?> getEnumMember(Class<?> parent,
                                           String memberName){
        if(parent == null) return null;

        Field field;
        try{
            field = parent.getField(memberName);
        }catch(NoSuchFieldException e){
            return null;
        }

        Object value;
        try{
            value = field.get(null);
        }catch(IllegalAccessException e){
            return null;
        }catch(IllegalArgumentException e){
            return null;
        }

        if( ! (value instanceof Enum) ) return null;

        return (Enum<?>) value;
    }

    /**
     * JRE1.6より提供されたjava.awt.Desktopが利用可能か判定する。
     * ※参照： java.awt.DesktopのisDesktopSupported()
     * @return Desktopが利用可能ならtrue。JRE1.5以前だとたぶんfalse。
     */
    public static boolean isDesktopSupported(){
        if(METHOD_ISDESKTOPSUPPORTED == null) return false;

        Object invokeResult;
        try{
            invokeResult = METHOD_ISDESKTOPSUPPORTED.invoke(null,
                                                            (Object[]) null);
        }catch(InvocationTargetException e){
            Throwable targetException = e.getTargetException();

            if(targetException instanceof RuntimeException){
                throw (RuntimeException) targetException;
            }else if(targetException instanceof Error){
                throw (Error) targetException;
            }

            AssertionError thw = new AssertionError();
            thw.initCause(e);
            throw thw;
        }catch(IllegalAccessException e){
            AssertionError thw = new AssertionError();
            thw.initCause(e);
            throw thw;
        }

        if( ! (invokeResult instanceof Boolean) ){
            assert false;
            return false;
        }

        boolean result = (Boolean) invokeResult;

        return result;
    }

    /**
     * WebIPCインスタンスを得る。
     * ※参照： java.awt.DesktopのgetDesktop()
     * @return インスタンス
     * @throws java.awt.HeadlessException スクリーンデバイスが見つからない
     * @throws java.lang.UnsupportedOperationException 未サポートの機能
     */
    public static WebIPC getWebIPC()
            throws HeadlessException,
                   UnsupportedOperationException {
        if(GraphicsEnvironment.isHeadless()) throw new HeadlessException();
        if( ! isDesktopSupported() ){
            throw new UnsupportedOperationException();
        }

        WebIPC webIPC = new WebIPC();

        return webIPC;
    }

    /**
     * Webブラウザに任意のURIを表示させる。
     * ※参照： java.awt.Desktopのbrowse(java.net.URI)
     * @param uri URI
     * @throws NullPointerException 引数がnull
     * @throws UnsupportedOperationException 未サポートの機能
     * @throws IOException ブラウザが見つからない
     * @throws SecurityException セキュリティ違反
     * @throws IllegalArgumentException URI形式が変
     */
    public void browse(URI uri)
            throws NullPointerException,
                   UnsupportedOperationException,
                   IOException,
                   SecurityException,
                   IllegalArgumentException {
        if(uri == null) throw new NullPointerException();
        if( ! isSupported(Action.BROWSE) ){
            throw new UnsupportedOperationException();
        }

        try{
            METHOD_BROWSE.invoke(this.desktop, uri);
        }catch(InvocationTargetException e){
            Throwable targetException = e.getTargetException();

            if(targetException instanceof IOException){
                throw (IOException) targetException;
            }
            if(targetException instanceof RuntimeException){
                throw (RuntimeException) targetException;
            }
            if(targetException instanceof Error){
                throw (Error) targetException;
            }

            AssertionError thw = new AssertionError();
            thw.initCause(e);
            throw thw;
        }catch(IllegalAccessException e){
            AssertionError thw = new AssertionError();
            thw.initCause(e);
            throw thw;
        }

        return;
    }

    /**
     * 指定した機能(アクション)がサポートされているか否か判定する。
     * ※参照： java.awt.Desktop#isSupported(java.awt.Desktop.Action)
     * @param action アクション
     * @return アクションがサポートされていればtrue。
     */
    public boolean isSupported(Action action){
        switch(action){
        case BROWSE:
            break;
        default:
            return false;
        }

        Object invokeResult;
        try{
            invokeResult = METHOD_ISSUPPORTED.invoke(this.desktop,
                                                     BROWSE_ENUM);
        }catch(InvocationTargetException e){
            Throwable targetException = e.getTargetException();

            if(targetException instanceof RuntimeException){
                throw (RuntimeException) targetException;
            }
            if(targetException instanceof Error){
                throw (Error) targetException;
            }

            AssertionError thw = new AssertionError();
            thw.initCause(e);
            throw thw;
        }catch(IllegalAccessException e){
            AssertionError thw = new AssertionError();
            thw.initCause(e);
            throw thw;
        }

        if( ! (invokeResult instanceof Boolean) ){
            assert false;
            return false;
        }

        boolean result = (Boolean) invokeResult;

        return result;
    }

    /**
     * 各種デスクトップアクション。
     * ※参照 java.awt.Desktop.Action
     */
    public static enum Action{
        /** Webブラウザでのブラウズ。 */
        BROWSE,
    //  EDIT,
    //  MAIL,
    //  OPEN,
    //  PRINT,
    }

}
