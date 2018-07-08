/*
 * WolfBBS
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.dxchg;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.sfjp.jindolf.ResourceManager;
import jp.sfjp.jindolf.data.Avatar;
import jp.sfjp.jindolf.data.Village;
import jp.sourceforge.jindolf.corelib.Destiny;
import jp.sourceforge.jindolf.corelib.GameRole;

/**
 * まちゅ氏運営のまとめサイト(wolfbbs)に関する諸々。
 *
 * PukiWikiベース。
 *
 * @see <a href="https://wolfbbs.jp/">まとめサイト</a>
 * @see <a href="https://pukiwiki.osdn.jp/">PukiWiki</a>
 */
public final class WolfBBS{

    /** PukiWikiコメント行。 */
    public static final String COMMENTLINE;

    private static final String WIKICHAR = "#&[]()<>+-*:|~/,'%?";
    private static final Pattern WIKINAME_PATTERN =
            Pattern.compile("[A-Z][a-z]+([A-Z])[a-z]+");

    private static final String FACEICONSET =
            "resources/wolfbbs/faceIconSet.properties";
    private static final String ORDER_PREFIX = "iconset.order.";
    private static final List<FaceIconSet> FACEICONSET_LIST =
            new LinkedList<>();

    private static final Charset CHARSET_EUC = Charset.forName("EUC-JP");

    private static final String WOLFBBS_URL = "http://wolfbbs.jp/";

    private static final Color COLOR_INNOCENT = new Color(0xb7bad3);
    private static final Color COLOR_WOLF     = new Color(0xe0b8b8);
    private static final Color COLOR_HAMSTER  = new Color(0xb9d0be);
    private static final Color COLOR_DEAD     = new Color(0xaaaaaa);
    private static final Color COLOR_ALIVE    = new Color(0xffffff);

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    static{
        try{
            loadFaceIconSet();
        }catch(FileNotFoundException e){
            throw new ExceptionInInitializerError(e);
        }

        StringBuilder wikicomment = new StringBuilder();
        wikicomment.append("// ");
        while(wikicomment.length() < 72){
            wikicomment.append('=');
        }
        wikicomment.append('\n');
        COMMENTLINE = wikicomment.toString();
    }


    /**
     * 隠しコンストラクタ。
     */
    private WolfBBS(){
        assert false;
        throw new AssertionError();
    }


    /**
     * アイコンセットのロード。
     *
     * @throws FileNotFoundException リソースが不明
     */
    private static void loadFaceIconSet() throws FileNotFoundException {
        Properties properties = ResourceManager.getProperties(FACEICONSET);
        if(properties == null){
            LOGGER.severe("顔アイコンセットの読み込みに失敗しました");
            throw new FileNotFoundException();
        }

        loadFaceIconSet(properties);

        return;
    }

    /**
     * アイコンセットのロード。
     *
     * @param properties プロパティ
     * @throws FileNotFoundException リソースが不明
     */
    private static void loadFaceIconSet(Properties properties)
            throws FileNotFoundException {
        String codeCheck = properties.getProperty("codeCheck");
        if(    codeCheck == null
            || codeCheck.length() != 1
            || codeCheck.charAt(0) != '\u72fc'){  // 「狼」
            LOGGER.severe(
                    "顔アイコンセットプロパティファイルの"
                    +"文字コードがおかしいようです。"
                    +"native2ascii は正しく適用しましたか？");
            throw new FileNotFoundException();
        }

        Set<Object> keySet = properties.keySet();

        SortedSet<Integer> orderSet = new TreeSet<>();
        for(Object keyObj : keySet){
            if(keyObj == null) continue;
            String key = keyObj.toString();
            if( ! key.startsWith(ORDER_PREFIX) ) continue;
            key = key.replace(ORDER_PREFIX, "");
            Integer order;
            try{
                order = Integer.valueOf(key);
            }catch(NumberFormatException e){
                continue;
            }
            orderSet.add(order);
        }

        for(Integer orderNum : orderSet){
            String setName = properties.getProperty(ORDER_PREFIX + orderNum);
            FaceIconSet iconSet = loadFaceIconSet(properties, setName);
            FACEICONSET_LIST.add(iconSet);
        }

        return;
    }

    /**
     * アイコンセットのロード。
     *
     * @param properties プロパティ
     * @param setName アイコンセット名
     * @return アイコンセット
     */
    private static FaceIconSet loadFaceIconSet(Properties properties,
                                          String setName){
        String author  = properties.getProperty(setName + ".author");
        String caption = properties.getProperty(setName + ".caption");
        String urlText = properties.getProperty(setName + ".url");

        FaceIconSet iconSet = new FaceIconSet(caption, author, urlText);

        List<Avatar> avatarList = Avatar.getPredefinedAvatarList();
        for(Avatar avatar : avatarList){
            String identifier = avatar.getIdentifier();
            String key = setName + ".iconWiki." + identifier;
            String wiki = properties.getProperty(key);
            iconSet.registIconWiki(avatar, wiki);
        }

        return iconSet;
    }

    /**
     * 顔アイコンセットのリストを取得する。
     *
     * @return 顔アイコンセットのリスト
     */
    public static List<FaceIconSet> getFaceIconSetList(){
        List<FaceIconSet> result =
                Collections.unmodifiableList(FACEICONSET_LIST);
        return result;
    }

    /**
     * 任意の文字がWikiの特殊キャラクタか否か判定する。
     *
     * @param ch 文字
     * @return 特殊キャラクタならtrue
     */
    public static boolean isWikiChar(char ch){
        if(WIKICHAR.indexOf(ch) < 0) return false;
        return true;
    }

    /**
     * Wiki特殊文字を数値参照文字でエスケープする。
     *
     * @param seq Wiki特殊文字を含むかもしれない文字列。
     * @return エスケープされた文字列
     */
    public static CharSequence escapeWikiChar(CharSequence seq){
        StringBuilder result = new StringBuilder();

        int seqLength = seq.length();
        for(int pos = 0; pos < seqLength; pos++){
            char ch = seq.charAt(pos);
            if(isWikiChar(ch)){
                try{
                    appendNumCharRef(result, ch);
                }catch(IOException e){
                    assert false;
                    return null;
                }
            }else{
                result.append(ch);
            }
        }

        return result;
    }

    /**
     * WikiNameを数値参照文字でエスケープする。
     *
     * @param seq WikiNameを含むかもしれない文字列
     * @return エスケープされた文字列。
     * @see <a href="https://pukiwiki.osdn.jp/?WikiName">WikiName</a>
     */
    public static CharSequence escapeWikiName(CharSequence seq){
        StringBuilder result = null;
        Matcher matcher = WIKINAME_PATTERN.matcher(seq);

        int pos = 0;
        while(matcher.find(pos)){
            int matchStart = matcher.start();
            int matchEnd   = matcher.end();
            int capStart = matcher.start(1);
            int capEnd   = matcher.end(1);

            if(result == null) result = new StringBuilder();
            result.append(seq, pos, matchStart);
            result.append(seq, matchStart, capStart);
            try{
                appendNumCharRef(result, seq.charAt(capStart));
            }catch(IOException e){
                assert false;
                return null;
            }
            result.append(seq, capEnd, matchEnd);

            pos = matchEnd;
        }

        if(pos == 0 || result == null) return seq;

        result.append(seq, pos, seq.length());

        return result;
    }

    /**
     * 任意の文字列をWiki表記へ変換する。
     *
     * @param seq 任意の文字列
     * @return Wiki用表記
     */
    public static CharSequence escapeWikiSyntax(CharSequence seq){
        CharSequence result = seq;
        result = escapeWikiChar(result);   // この順番は大事
        result = escapeWikiName(result);
        // TODO さらにURLとメールアドレスのエスケープも
        return result;
    }

    /**
     * ブラケットに入れる文字をエスケープする。
     *
     * @param seq 文字列。
     * @return エスケープされた文字列
     */
    public static CharSequence escapeWikiBracket(CharSequence seq){
        StringBuilder result = new StringBuilder();

        int seqLength = seq.length();
        for(int pos = 0; pos < seqLength; pos++){
            char ch = seq.charAt(pos);

            switch(ch){
            case '#': ch = '＃'; break;
            case '&': ch = '＆'; break;
            case '[': ch = '［'; break;
            case ']': ch = '］'; break;
            case '<': ch = '＜'; break;
            case '>': ch = '＞'; break;
            default: break;
            }

            result.append(ch);
        }

        int resultLength;

        while(result.length() > 0 && result.charAt(0) == '/'){
            result.deleteCharAt(0);
        }

        resultLength = result.length();
        for(int pos = resultLength - 1; pos >= 0; pos--){
            char ch = result.charAt(pos);
            if(ch != '/') break;
            result.deleteCharAt(pos);
        }

        resultLength = result.length();
        for(int pos = 1; pos < resultLength - 1; pos++){
            char ch = result.charAt(pos);
            if(ch == ':'){
                result.setCharAt(pos, '：');
            }
        }

        resultLength = result.length();
        if(resultLength == 1 && result.charAt(0) == ':'){
            result.setCharAt(0, '：');
        }

        return result;
    }

    /**
     * 数値参照文字に変換された文字を追加する。
     *
     * 例）{@literal 'D' => "&#x44;}"
     *
     * @param app 追加対象
     * @param ch 1文字
     * @return 引数と同じ
     * @throws java.io.IOException 入出力エラー。文字列の場合はありえない。
     */
    public static Appendable appendNumCharRef(Appendable app, char ch)
            throws IOException{
        app.append("&#x");

        int ival = ch;
        String hex = Integer.toHexString(ival);
        app.append(hex);

        app.append(';');

        return app;
    }

    /**
     * 任意の文字を数値参照文字列に変換する。
     *
     * 例）{@literal 'D' => "&#x44;"}
     *
     * @param ch 文字
     * @return 変換後の文字列
     */
    public static CharSequence toNumCharRef(char ch){
        StringBuilder result = new StringBuilder(8);
        try{
            appendNumCharRef(result, ch);
        }catch(IOException e){
            assert false;
            return null;
        }
        return result;
    }

    /**
     * ColorのRGB各成分をWikiカラー表記に変換する。
     *
     * α成分は無視される。
     *
     * @param color 色
     * @return Wikiカラー表記
     */
    public static String cnvWikiColor(Color color){
        int packRGB = color.getRGB();

        String txtRGB = Integer.toHexString(packRGB);
        String leadRGB = "00000000" + txtRGB;
        int chopLen = leadRGB.length() - 6;
        String fixed = leadRGB.substring(chopLen);
        String result = "#" + fixed;

        return result;
    }

    /**
     * 表の偶数行に色の変化を付ける。
     *
     * @param color 色
     * @return 変化した色
     */
    public static Color evenColor(Color color){
        int red   = color.getRed();
        int green = color.getGreen();
        int blue  = color.getBlue();

        float[] hsb = Color.RGBtoHSB(red, green, blue, null);
        float h = hsb[0];
        float s = hsb[1];
        float b = hsb[2];

        if(b < 0.5){
            b += 0.03;
        }else{
            b -= 0.03;
        }

        Color result = Color.getHSBColor(h, s, b);

        return result;
    }

    /**
     * 陣営の色を返す。
     *
     * @param role 役職
     * @return 色
     */
    public static Color getTeamColor(GameRole role){
        Color result;

        switch(role){
        case INNOCENT:
        case SEER:
        case SHAMAN:
        case HUNTER:
        case FRATER:
            result = COLOR_INNOCENT;
            break;
        case WOLF:
        case MADMAN:
            result = COLOR_WOLF;
            break;
        case HAMSTER:
            result = COLOR_HAMSTER;
            break;
        default:
            assert false;
            return null;
        }

        return result;
    }

    /**
     * 各役職のアイコンWikiを返す。
     *
     * @param role 役職
     * @return アイコンWiki
     */
    public static String getRoleIconWiki(GameRole role){
        String result;

        switch(role){
        case INNOCENT:
            result = "&char(村人,nolink);";
            break;
        case WOLF:
            result = "&char(人狼,nolink);";
            break;
        case SEER:
            result = "&char(占い師,nolink);";
            break;
        case SHAMAN:
            result = "&char(霊能者,nolink);";
            break;
        case MADMAN:
            result = "&char(狂人,nolink);";
            break;
        case HUNTER:
            result = "&char(狩人,nolink);";
            break;
        case FRATER:
            result = "&char(共有者,nolink);";
            break;
        case HAMSTER:
            result = "&char(ハムスター人間,nolink);";
            break;
        default:
            assert false;
            result = "";
            break;
        }

        return result;
    }

    /**
     * 運命に対応する色を返す。
     *
     * @param destiny 運命
     * @return 色
     */
    public static Color getDestinyColor(Destiny destiny){
        Color result;
        if(destiny == Destiny.ALIVE) result = COLOR_ALIVE;
        else                         result = COLOR_DEAD;
        return result;
    }

    /**
     * そのまままとめサイトパス名に使えそうなシンプルな文字か判定する。
     *
     * @param ch 文字
     * @return まとめサイトパス名に使えそうならtrue
     */
    private static boolean isSimpleIdToken(char ch){
        if('0' <= ch && ch <= '9') return true;
        if('A' <= ch && ch <= 'Z') return true;
        if('a' <= ch && ch <= 'z') return true;
        if(ch == '-' || ch == '_') return true;
        return false;
    }

    /**
     * プレイヤーIDを構成する文字からパス名を組み立てる。
     *
     * @param seq パス名
     * @param ch 文字
     * @return 引数と同じもの
     */
    private static StringBuilder encodeId(StringBuilder seq, char ch){
        if(isSimpleIdToken(ch)){
            seq.append(ch);
            return seq;
        }

        CharBuffer cbuf = CharBuffer.allocate(1);
        cbuf.append(ch);
        cbuf.rewind();

        CharsetEncoder encoder = CHARSET_EUC.newEncoder();
        ByteBuffer bytebuf;
        try{
            bytebuf = encoder.encode(cbuf);
        }catch(CharacterCodingException e){
            seq.append('X');
            return seq;
        }

        int limit = bytebuf.limit();
        while(bytebuf.position() < limit){
            int iVal = bytebuf.get();
            if(iVal < 0) iVal += 0x0100;
            String hex = Integer.toHexString(iVal).toUpperCase(Locale.JAPAN);
            seq.append('%');
            if(hex.length() < 2) seq.append('0');
            seq.append(hex);
        }

        return seq;
    }

    /**
     * プレイヤーIDからパス名の一部を予測する。
     *
     * @param id プレイヤーID
     * @return .htmlを抜いたパス名
     */
    private static StringBuilder encodeId(CharSequence id){
        StringBuilder result = new StringBuilder();
        int length = id.length();
        for(int pt = 0; pt < length; pt++){
            char ch = id.charAt(pt);
            encodeId(result, ch);
        }
        return result;
    }

    /**
     * プレイヤーIDからまとめサイト上の個人ページを推測する。
     *
     * @param id プレイヤーID
     * @return 個人ページURL文字列
     */
    public static String encodeURLFromId(CharSequence id){
        CharSequence encodedId = encodeId(id);

        String result = WOLFBBS_URL + encodedId + ".html";

        return result;
    }

    /**
     * キャスト紹介ジェネレータ出力のURLを得る。
     *
     * @param village 村
     * @return ジェネレータ出力URL
     */
    public static String getCastGeneratorUrl(Village village){
        String villageName = village.getVillageName();

        StringBuilder txt = new StringBuilder();
        txt.append(WOLFBBS_URL);
        txt.append(villageName);
        txt.append("%C2%BC.html");

        String result = txt.toString();
        return result;
    }

}
