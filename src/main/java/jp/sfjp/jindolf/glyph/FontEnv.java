/*
 * font environment
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * フォント環境に関する情報あれこれをバックグラウンドで収集する。
 *
 * <ul>
 * <li>与えられた選択肢から利用可能なフォントを一つ選ぶこと
 * <li>任意の文字列を表示可能な全フォントを列挙すること
 * </ul>
 *
 * <p>この二つをバックグラウンドで非同期に収集する。
 *
 * <p>各種フォント環境収集メソッドの遅さをカバーするのが実装目的。
 */
public class FontEnv {

    /**
     * デフォルトのフォント環境。
     */
    public static final FontEnv DEFAULT;

    /** フォントファミリ選択肢。 */
    private static final String[] INIT_FAMILY_NAMES = {
        "Hiragino Kaku Gothic Pro",  // for MacOS X
        "Hiragino Kaku Gothic Std",
        "Osaka",
        "MS PGothic",                // for WinXP
        "MS Gothic",
        "IPAMonaPGothic",
        // TODO X11用のおすすめは？
    };

    /** JIS X0208:1990 表示確認用文字列。 */
    private static final String JPCHECK_CODE = "あ凜熙峠ゑアｱヴヰ┼ЖΩ9A";

    private static final int POOL_SZ = 2;
    private static final int STRIDE = 15;

    static{
        DEFAULT = new FontEnv(JPCHECK_CODE, INIT_FAMILY_NAMES);
    }


    private final String proveChars;
    private final List<String> fontFamilyList;

    private final Future<List<String>> listLoadFuture;
    private final Future<String>       fontSelectFuture;


    /**
     * コンストラクタ。
     *
     * <p>完了と同時に裏でフォント情報収集タスクが走る。
     *
     * @param proveChars 表示判定用文字列
     * @param fontFamilyList フォント候補
     * @throws NullPointerException 引数がnull
     */
    public FontEnv(String proveChars, List<String> fontFamilyList)
            throws NullPointerException {
        super();

        if(proveChars == null || fontFamilyList == null){
            throw new NullPointerException();
        }

        this.proveChars = proveChars;
        this.fontFamilyList = fontFamilyList;

        ExecutorService service = Executors.newFixedThreadPool(POOL_SZ);

        Callable<List<String>> loadTask = new FontListLoader();
        this.listLoadFuture = service.submit(loadTask);

        Callable<String> selectTask = new FontSelector();
        this.fontSelectFuture = service.submit(selectTask);

        service.shutdown();

        return;
    }

    /**
     * コンストラクタ。
     *
     * <p>完了と同時に裏でフォント情報収集タスクが走る。
     *
     * @param proveChars 表示判定用文字列
     * @param fontFamilyList フォント候補
     * @throws NullPointerException 引数がnull
     */
    public FontEnv(String proveChars, String ... fontFamilyList)
            throws NullPointerException {
        this(proveChars, Arrays.asList(fontFamilyList));
        return;
    }


    /**
     * 自発的なスケジューリングを促す。
     */
    @SuppressWarnings("CallToThreadYield")
    protected static void selfYield(){
        Thread.yield();
        return;
    }

    /**
     * 指定文字列が表示可能なフォントファミリ集合を生成する。
     *
     * <p>結構実行時間がかかるかも(数千ms)。乱用禁物。
     *
     * @param checkChars テスト対象の文字が含まれた文字列
     * @return フォント集合
     */
    protected static Collection<String> createFontSet(String checkChars){
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] allFonts = ge.getAllFonts();

        selfYield();

        Collection<String> result = new HashSet<>();
        int ct = 0;
        for(Font font : allFonts){
            if(++ct % STRIDE == 0) selfYield();

            String familyName = font.getFamily();
            if(result.contains(familyName)) continue;
            if(font.canDisplayUpTo(checkChars) >= 0) continue;

            result.add(familyName);
        }

        return result;
    }

    /**
     * システムに存在する有効なファミリ名か判定する。
     *
     * @param familyName フォントファミリ名。
     * @return 存在する有効なファミリ名ならtrue
     */
    protected static boolean isValidFamilyName(String familyName){
        int style = 0x00 | Font.PLAIN;
        int size = 1;
        Font dummyFont = new Font(familyName, style, size);

        String dummyFamilyName = dummyFont.getFamily(Locale.ROOT);
        if(dummyFamilyName.equals(familyName)) return true;

        String dummyLocalFamilyName = dummyFont.getFamily();
        if(dummyLocalFamilyName.equals(familyName)) return true;

        return false;
    }

    /**
     * 複数の候補から利用可能なフォントを一つ選び、生成する。
     *
     * <p>候補から適当なファミリが見つからなかったら"Dialog"が選択される。
     *
     * @param fontList フォントファミリ名候補
     * @return フォント
     */
    protected static String availableFontFamily(Iterable<String> fontList){
        String defaultFamilyName = Font.DIALOG;
        for(String familyName : fontList){
            if(isValidFamilyName(familyName)){
                defaultFamilyName = familyName;
                break;
            }
        }

        return defaultFamilyName;
    }


    /**
     * フォントファミリー名のリストを返す。
     *
     * @return フォントファミリー名のリスト
     * @throws IllegalStateException 収集タスクに異常が発生
     */
    public List<String> getFontFamilyList() throws IllegalStateException {
        List<String> result;

        try{
            result = this.listLoadFuture.get();
        }catch(ExecutionException | InterruptedException e){
            throw new IllegalStateException(e);
        }

        return result;
    }

    /**
     * 利用可能なフォントファミリ名を返す。
     *
     * @return フォントファミリー名
     * @throws IllegalStateException 収集タスクに異常が発生
     */
    public String selectFontFamily() throws IllegalStateException {
        String result;

        try{
            result = this.fontSelectFuture.get();
        }catch(ExecutionException | InterruptedException e){
            throw new IllegalStateException(e);
        }

        return result;
    }


    /**
     * フォントリスト収集タスク。
     */
    protected final class FontListLoader implements Callable<List<String>> {

        /**
         * コンストラクタ。
         */
        private FontListLoader(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         *
         * @return {@inheritDoc}
         */
        @Override
        public List<String> call(){
            Collection<String> fontSet =
                    createFontSet(FontEnv.this.proveChars);
            selfYield();

            List<String> result = new ArrayList<>(fontSet);
            Collections.sort(result);
            selfYield();

            result = Collections.unmodifiableList(result);

            return result;
        }
    }

    /**
     * フォント選択タスク。
     */
    protected final class FontSelector implements Callable<String> {

        /**
         * コンストラクタ。
         */
        private FontSelector(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         *
         * @return {@inheritDoc}
         */
        @Override
        public String call(){
            String result = availableFontFamily(FontEnv.this.fontFamilyList);
            return result;
        }
    }

}
