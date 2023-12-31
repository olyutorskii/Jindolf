/*
 * resource manager
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import jp.sfjp.jindolf.view.FlexiIcon;

/**
 * 各種リソースファイルの管理。
 *
 * <p>{@link Class}用と{@link ClassLoader}用とでは
 * 微妙に絶対リソース名の形式が異なることに留意せよ。
 *
 * <p>基本的に、リソースファイルへのアクセスにおける異常系は
 * リカバリの対象外とする。(ビルド工程の不手際扱い。)
 *
 * @see java.lang.Class#getResource
 * @see java.lang.ClassLoader#getResource
 */
public final class ResourceManager {

    /**
     * デフォルトで用いられるルートパッケージ。
     *
     * <p>相対リソース名の起点となる。
     */
    public static final Package DEF_ROOT_PACKAGE;

    private static final Class<?> ROOT_KLASS = Jindolf.class;

    private static final ClassLoader DEF_LOADER;

    private static final char PKG_SEPCHAR = '.';
    private static final char RES_SEPCHAR = '/';
    private static final String RES_SEPARATOR =
            Character.toString(RES_SEPCHAR);

    private static final Charset CS_UTF8 = StandardCharsets.UTF_8;

    private static final int BTN_SZ = 24;

    static{
        DEF_ROOT_PACKAGE = ROOT_KLASS.getPackage();
        DEF_LOADER = ROOT_KLASS.getClassLoader();
    }


    /**
     * 隠しコンストラクタ。
     */
    private ResourceManager(){
        assert false;
    }


    /**
     * リソース名が絶対パスか否か判定する。
     *
     * <p>リソース名が「/」で始まる場合、
     * {@link Class}用の絶対パスとみなされる。
     *
     * <p>「/」で始まるリソース名表記は{@link Class}用であって
     * {@link ClassLoader}では不要。
     *
     * @param resPath リソース名
     * @return 絶対パスならtrueを返す。
     * @see java.lang.Class#getResource
     * @see java.lang.ClassLoader#getResource
     */
    private static boolean isAbsoluteResourcePath(String resPath){
        if (resPath.startsWith(RES_SEPARATOR)) return true;
        return false;
    }

    /**
     * パッケージ情報を反映するリソース名前置詞を返す。
     *
     * <p>パッケージ名のセパレータ「.」は「/」に置き換えられる。
     * 無名パッケージの場合は長さゼロの空文字列を返す。
     * 無名パッケージを除き、リソース名前置詞は必ず「/」で終わる。
     *
     * <p>この前置詞は{@link ClassLoader}用であって
     * {@link Class}用ではないので、
     * 頭に「/」が付かない。
     *
     * <p>パッケージ「com.example.test」の
     * リソース名前置詞は「com/example/test/」
     *
     * @param pkg パッケージ設定。nullは無名パッケージと認識される。
     * @return リソース名前置詞。無名パッケージの場合は空文字列が返る。
     * @see java.lang.ClassLoader#getResource
     * @see java.lang.Class#getResource
     */
    private static String getResourcePrefix(Package pkg){
        if(pkg == null) return "";   // 無名パッケージ

        String pkgName = pkg.getName();
        String result = pkgName.replace(PKG_SEPCHAR, RES_SEPCHAR);
        if( ! result.isEmpty() ){
            result += RES_SEPARATOR;
        }

        assert result.charAt(0) != RES_SEPCHAR;

        return result;
    }

    /**
     * リソース名を用いて、
     * デフォルトのクラスローダとデフォルトのルートパッケージから
     * リソースのURLを取得する。
     *
     * @param resPath リソース名
     * @return リソースのURL。リソースが見つからなければnull。
     */
    public static URL getResource(String resPath){
        return getResource(DEF_ROOT_PACKAGE, resPath);
    }

    /**
     * 任意のルートパッケージと相対リソース名を用いて、
     * デフォルトのクラスローダからリソースのURLを取得する。
     *
     * @param rootPkg ルートパッケージ情報。
     *     「/」で始まる絶対リソース名が指定された場合は無視される。
     * @param resPath リソース名
     * @return リソースのURL。リソースが見つからなければnull。
     */
    private static URL getResource(Package rootPkg, String resPath){
        return getResource(DEF_LOADER, rootPkg, resPath);
    }

    /**
     * 任意のルートパッケージと相対リソース名を用いて、
     * 任意のクラスローダからリソースのURLを取得する。
     *
     * @param loader クラスローダ
     * @param rootPkg ルートパッケージ情報。
     *     「/」で始まる絶対リソース名が指定された場合は無視される。
     * @param resPath リソース名
     * @return リソースのURL。リソースが見つからなければnull。
     */
    private static URL getResource(ClassLoader loader,
                                   Package rootPkg,
                                   String resPath ){
        String fullName;
        if(isAbsoluteResourcePath(resPath)){
            // chop '/' heading
            fullName = resPath.substring(1);
        }else{
            String pfx = getResourcePrefix(rootPkg);
            fullName = pfx + resPath;
        }

        assert ! isAbsoluteResourcePath(fullName);

        URL result = loader.getResource(fullName);

        return result;
    }

    /**
     * リソース名を用いて、
     * デフォルトのクラスローダとデフォルトのルートパッケージから
     * リソースの入力ストリームを取得する。
     *
     * @param resPath リソース名
     * @return リソースの入力ストリーム。リソースが見つからなければnull。
     */
    public static InputStream getResourceAsStream(String resPath){
        return getResourceAsStream(DEF_ROOT_PACKAGE, resPath);
    }

    /**
     * 任意のルートパッケージと相対リソース名を用いて、
     * デフォルトのクラスローダからリソースの入力ストリームを取得する。
     *
     * @param rootPkg ルートパッケージ情報。
     *     「/」で始まる絶対リソース名が指定された場合は無視される。
     * @param resPath リソース名
     * @return リソースの入力ストリーム。リソースが見つからなければnull。
     */
    private static InputStream getResourceAsStream(Package rootPkg,
                                                   String resPath ){
        return getResourceAsStream(DEF_LOADER, rootPkg, resPath);
    }

    /**
     * 任意のルートパッケージと相対リソース名を用いて、
     * 任意のクラスローダからリソースの入力ストリームを取得する。
     *
     * @param loader クラスローダ
     * @param rootPkg ルートパッケージ情報。
     *     「/」で始まる絶対リソース名が指定された場合は無視される。
     * @param resPath リソース名
     * @return リソースの入力ストリーム。リソースが見つからなければnull。
     */
    private static InputStream getResourceAsStream(ClassLoader loader,
                                                   Package rootPkg,
                                                   String resPath ){
        URL url = getResource(loader, rootPkg, resPath);
        if(url == null) return null;

        InputStream result;
        try{
            result = url.openStream();
        }catch (IOException e){
            result = null;
        }

        return result;
    }

    /**
     * リソース名を用いてイメージ画像を取得する。
     *
     * @param resPath 画像リソース名
     * @return イメージ画像。リソースが見つからなければnull。
     */
    public static BufferedImage getBufferedImage(String resPath){
        URL url = getResource(resPath);
        if(url == null) return null;

        BufferedImage result;
        try{
            result = ImageIO.read(url);
        }catch(IOException e){
            result = null;
        }

        return result;
    }

    /**
     * リソース名を用いてアイコン画像を取得する。
     *
     * @param resPath アイコン画像リソース名
     * @return アイコン画像。リソースが見つからなければnull。
     */
    public static ImageIcon getImageIcon(String resPath){
        URL url = getResource(resPath);
        if(url == null) return null;

        ImageIcon result = new ImageIcon(url);

        return result;
    }

    /**
     * リソース名を用いて正方形ボタン用アイコン画像を取得する。
     *
     * @param resPath アイコン画像リソース名
     * @return アイコン画像。リソースが見つからなければnull。
     */
    public static Icon getButtonIcon(String resPath){
        Icon result = getSquareIcon(resPath, BTN_SZ);
        return result;
    }

    /**
     * リソース名を用いて正方形アイコン画像を取得する。
     *
     * @param resPath アイコン画像リソース名
     * @param sz 正方形アイコン寸法
     * @return アイコン画像。リソースが見つからなければnull。
     */
    public static Icon getSquareIcon(String resPath, int sz){
        BufferedImage image = getBufferedImage(resPath);
        Icon result = new FlexiIcon(image, sz, sz);
        return result;
    }

    /**
     * リソース名を用いてプロパティを取得する。
     *
     * @param resPath プロパティファイルのリソース名
     * @return プロパティ。リソースが読み込めなければnull。
     */
    public static Properties getProperties(String resPath){
        InputStream is = getResourceAsStream(resPath);
        if(is == null) return null;

        Properties properties = new Properties();

        try(InputStream pis = new BufferedInputStream(is)){
            properties.load(pis);
        }catch(IOException e){
            properties = null;
        }

        return properties;
    }

    /**
     * リソース名を用いてUTF-8テキストファイルの内容を取得する。
     *
     * <p>「#」で始まる行はコメント行として無視される。
     *
     * @param resPath テキストファイルのリソース名
     * @return テキスト。リソースが読み込めなければnull。
     */
    public static String getTextFile(String resPath){
        InputStream is;
        is = getResourceAsStream(resPath);
        if(is == null) return null;
        is = new BufferedInputStream(is);

        Reader reader = new InputStreamReader(is, CS_UTF8);

        StringBuilder result = new StringBuilder();

        try(LineNumberReader lineReader = new LineNumberReader(reader)){
            for(;;){
                String line;
                line = lineReader.readLine();
                if(line == null) break;
                if(line.startsWith("#")) continue;
                result.append(line).append('\n');
            }
        }catch(IOException e){
            result = null;
        }

        if(result == null) return null;

        return result.toString();
    }

}
