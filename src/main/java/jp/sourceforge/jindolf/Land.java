/*
 * land
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.LandState;
import jp.sourceforge.jindolf.corelib.VillageState;
import jp.sourceforge.jindolf.parser.DecodedContent;
import jp.sourceforge.jindolf.parser.HtmlAdapter;
import jp.sourceforge.jindolf.parser.HtmlParseException;
import jp.sourceforge.jindolf.parser.HtmlParser;
import jp.sourceforge.jindolf.parser.PageType;
import jp.sourceforge.jindolf.parser.SeqRange;

/**
 * いわゆる「国」。
 */
public class Land {

    // 古国ID
    private static final String ID_VANILLAWOLF = "wolf";


    private final LandDef landDef;
    private final ServerAccess serverAccess;
    private final HtmlParser parser = new HtmlParser();
    private final VillageListHandler handler = new VillageListHandler();

    private final List<Village> villageList = new LinkedList<Village>();


    /**
     * コンストラクタ。
     * @param landDef 国定義
     * @throws java.lang.IllegalArgumentException 不正な国定義
     */
    public Land(LandDef landDef) throws IllegalArgumentException{
        super();

        this.landDef = landDef;

        URL url;
        try{
            url = this.landDef.getCgiURI().toURL();
        }catch(MalformedURLException e){
            throw new IllegalArgumentException(e);
        }
        this.serverAccess = new ServerAccess(url, this.landDef.getEncoding());

        this.parser.setBasicHandler(this.handler);

        return;
    }


    /**
     * クエリー文字列から特定キーの値を得る。
     * クエリーの書式例：「a=b&c=d&e=f」この場合キーcの値はd
     * @param key キー
     * @param allQuery クエリー
     * @return 値
     */
    public static String getValueFromCGIQueries(String key,
                                                   String allQuery){
        String result = null;

        String[] queries = allQuery.split("\\Q&\\E");

        for(String pair : queries){
            if(pair == null) continue;
            String[] namevalue = pair.split("\\Q=\\E");
            if(namevalue == null) continue;
            if(namevalue.length != 2) continue;
            String name  = namevalue[0];
            String value = namevalue[1];
            if(name == null) continue;
            if( name.equals(key) ){
                result = value;
                if(result == null) continue;
                if(result.length() <= 0) continue;
                break;
            }
        }

        return result;
    }

    /**
     * AタグのHREF属性値からクエリー部を抽出する。
     * 「{@literal &amp;}」は「{@literal &}」に解釈される。
     * @param hrefValue HREF属性値
     * @return クエリー文字列
     */
    public static String getRawQueryFromHREF(CharSequence hrefValue){
        if(hrefValue == null) return null;

        // HTML 4.01 B.2.2 rule
        String pureHREF = hrefValue.toString().replace("&amp;", "&");

        URI uri;
        try{
            uri = new URI(pureHREF);
        }catch(URISyntaxException e){
            Jindolf.logger().warn(
                     "不正なURI["
                    + hrefValue
                    + "]を検出しました");
            return null;
        }

        String rawQuery = uri.getRawQuery();

        return rawQuery;
    }

    /**
     * AタグのHREF属性値から村IDを得る。
     * @param hrefValue HREF値
     * @return village 村ID
     */
    public static String getVillageIDFromHREF(CharSequence hrefValue){
        String rawQuery = getRawQueryFromHREF(hrefValue);
        if(rawQuery == null) return null;

        String villageID = getValueFromCGIQueries("vid", rawQuery);
        if(villageID == null) return null;
        if(villageID.length() <= 0) return null;

        return villageID;
    }

    /**
     * 国定義を得る。
     * @return 国定義
     */
    public LandDef getLandDef(){
        return this.landDef;
    }

    /**
     * サーバ接続を返す。
     * @return ServerAccessインスタンス
     */
    public ServerAccess getServerAccess(){
        return this.serverAccess;
    }

    /**
     * 指定されたインデックス位置の村を返す。
     * @param index 0から始まるインデックス値
     * @return 村
     */
    public Village getVillage(int index){
        if(index < 0)                  return null;
        if(index >= getVillageCount()) return null;

        Village result = this.villageList.get(index);
        return result;
    }

    /**
     * 村の総数を返す。
     * @return 村の総数
     */
    public int getVillageCount(){
        int result = this.villageList.size();
        return result;
    }

    /**
     * 村のリストを返す。
     * @return 村のリスト
     */
    // TODO インスタンス変数でいいはず。
    public List<Village> getVillageList(){
        return Collections.unmodifiableList(this.villageList);
    }

    /**
     * 絶対または相対URLの指すパーマネントなイメージ画像をダウンロードする。
     * ※ A,B,D 国の顔アイコンは絶対パスらしい…。
     * @param imageURL 画像URL文字列
     * @return 画像イメージ
     */
    public BufferedImage downloadImage(String imageURL){
        ServerAccess server = getServerAccess();
        BufferedImage image;
        try{
            image = server.downloadImage(imageURL);
        }catch(IOException e){
            Jindolf.logger().warn(
                    "イメージ[" + imageURL + "]"
                    + "のダウンロードに失敗しました",
                    e );
            return null;
        }
        return image;
    }

    /**
     * 墓アイコンイメージを取得する。
     * @return 墓アイコンイメージ
     */
    public BufferedImage getGraveIconImage(){
        URI uri = getLandDef().getTombFaceIconURI();
        BufferedImage result = downloadImage(uri.toASCIIString());
        return result;
    }

    /**
     * 墓アイコンイメージ(大)を取得する。
     * @return 墓アイコンイメージ(大)
     */
    public BufferedImage getGraveBodyImage(){
        URI uri = getLandDef().getTombBodyIconURI();
        BufferedImage result = downloadImage(uri.toASCIIString());
        return result;
    }

    /**
     * 村リストを更新する。
     * 元情報は国のトップページと村一覧ページ。
     * 古国の場合は村一覧にアクセスせずトップページのみ。
     * 古国以外に村建てをやめた国はトップページにアクセスしない。
     * 村リストはVillageの実装に従いソートされる。重複する村は排除。
     * @throws java.io.IOException ネットワーク入出力の異常
     */
    public void updateVillageList() throws IOException{
        LandDef thisLand = getLandDef();
        LandState state = thisLand.getLandState();
        boolean isVanillaWolf = thisLand.getLandId().equals(ID_VANILLAWOLF);

        ServerAccess server = getServerAccess();

        // たまに同じ村が複数回出現するので注意！
        SortedSet<Village> vset = new TreeSet<Village>();

        // トップページ
        if(state.equals(LandState.ACTIVE) || isVanillaWolf){
            HtmlSequence html = server.getHTMLTopPage();
            DecodedContent content = html.getContent();
            try{
                this.parser.parseAutomatic(content);
            }catch(HtmlParseException e){
                Jindolf.logger().warn("トップページを認識できない", e);
            }
            List<Village> list = this.handler.getVillageList();
            if(list != null){
                vset.addAll(list);
            }
        }

        // 村一覧ページ
        if( ! isVanillaWolf ){
            HtmlSequence html = server.getHTMLLandList();
            DecodedContent content = html.getContent();
            try{
                this.parser.parseAutomatic(content);
            }catch(HtmlParseException e){
                Jindolf.logger().warn("村一覧ページを認識できない", e);
            }
            List<Village> list = this.handler.getVillageList();
            if(list != null){
                vset.addAll(list);
            }
        }

        // TODO 村リスト更新のイベントリスナがあると便利か？
        this.villageList.clear();
        this.villageList.addAll(vset);

        this.parser.reset();
        this.handler.reset();

        return;
    }

    /**
     * 国の文字列表現を返す。
     * @return 文字列表現
     */
    @Override
    public String toString(){
        return getLandDef().getLandName();
    }

    /**
     * 村一覧取得用ハンドラ。
     */
    private class VillageListHandler extends HtmlAdapter{

        private List<Village> villageList = null;

        /**
         * コンストラクタ。
         */
        public VillageListHandler(){
            super();
            return;
        }

        /**
         * 村一覧を返す。
         * 再度パースを行うまで呼んではいけない。
         * @return 村一覧
         * @throws IllegalStateException パース前に呼び出された。
         * あるいはパース後すでにリセットされている。
         */
        public List<Village> getVillageList() throws IllegalStateException{
            if(this.villageList == null){
                throw new IllegalStateException("パースが必要です。");
            }

            List<Village> result = this.villageList;

            return result;
        }

        /**
         * リセットを行う。
         * 村一覧は空になる。
         */
        public void reset(){
            this.villageList = null;
            return;
        }

        /**
         * {@inheritDoc}
         * 村一覧リストが初期化される。
         * @param content {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void startParse(DecodedContent content)
                throws HtmlParseException{
            reset();
            this.villageList = new LinkedList<Village>();
            return;
        }

        /**
         * {@inheritDoc}
         * 自動判定の結果がトップページでも村一覧ページでもなければ
         * 例外を投げる。
         * @param type {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc} 意図しないページが来た。
         */
        @Override
        public void pageType(PageType type) throws HtmlParseException{
            if(   type != PageType.VILLAGELIST_PAGE
               && type != PageType.TOP_PAGE ){
                throw new HtmlParseException(
                        "トップページか村一覧ページが必要です。");
            }
            return;
        }

        /**
         * {@inheritDoc}
         * @param content {@inheritDoc}
         * @param anchorRange {@inheritDoc}
         * @param villageRange {@inheritDoc}
         * @param hour {@inheritDoc}
         * @param minute {@inheritDoc}
         * @param villageState {@inheritDoc}
         * @throws HtmlParseException {@inheritDoc}
         */
        @Override
        public void villageRecord(DecodedContent content,
                                    SeqRange anchorRange,
                                    SeqRange villageRange,
                                    int hour, int minute,
                                    VillageState villageState)
                throws HtmlParseException{
            LandDef landdef = getLandDef();
            LandState landState = landdef.getLandState();

            CharSequence href = anchorRange.sliceSequence(content);
            String villageID = getVillageIDFromHREF(href);
            if(   villageID == null
               || villageID.length() <= 0 ){
                Jindolf.logger().warn(
                        "認識できないURL[" + href + "]に遭遇しました。");
                 return;
            }

            CharSequence fullVillageName =
                    villageRange.sliceSequence(content);

            // TODO 既に出来ているかもしれないVillageを再度作るのは無駄？
            Village village = new Village(Land.this,
                                          villageID,
                                          fullVillageName.toString() );

            if(landState == LandState.HISTORICAL){
                village.setState(VillageState.GAMEOVER);
            }else{
                village.setState(villageState);
            }

            this.villageList.add(village);

            return;
        }

    }

}
