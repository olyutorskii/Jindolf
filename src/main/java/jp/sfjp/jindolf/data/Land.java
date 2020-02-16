/*
 * land
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.osdn.jindolf.parser.HtmlParseException;
import jp.osdn.jindolf.parser.HtmlParser;
import jp.osdn.jindolf.parser.content.DecodedContent;
import jp.sfjp.jindolf.net.HtmlSequence;
import jp.sfjp.jindolf.net.ServerAccess;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.LandState;
import jp.sourceforge.jindolf.corelib.VillageState;

/**
 * いわゆる「国」。
 */
public class Land {

    // 古国ID
    private static final String ID_VANILLAWOLF = "wolf";

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    private final LandDef landDef;
    private final ServerAccess serverAccess;
    private final HtmlParser parser = new HtmlParser();
    private final VillageListHandler handler = new VillageListHandler();

    private final List<Village> villageList = new LinkedList<>();


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
            LOGGER.log(Level.WARNING,
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
     * build Village list.
     *
     * @param records village records on HTML
     * @return village list
     */
    private List<Village> buildVillageList(List<VillageRecord> records){
        List<Village> result = new ArrayList<>(records.size());

        for(VillageRecord record : records){
            String id = record.getVillageId();
            String fullVillageName = record.getFullVillageName();

            VillageState status;
            if(this.landDef.getLandState() == LandState.HISTORICAL){
                status = VillageState.GAMEOVER;
            }else{
                status = record.getVillageStatus();
            }

            Village village = new Village(this, id, fullVillageName);
            village.setState(status);

            result.add(village);
        }

        return result;
    }

    /**
     * 村一覧情報をダウンロードする。
     * リスト元情報は国のトップページと村一覧ページ。
     * 古国の場合は村一覧にアクセスせずトップページのみ。
     * 古国以外で村建てをやめた国はトップページにアクセスしない。
     * 村リストはVillageの実装に従いソートされる。重複する村は排除。
     *
     * @return ソートされた村一覧
     * @throws java.io.IOException ネットワーク入出力の異常
     */
    public SortedSet<Village> downloadVillageList() throws IOException {
        LandDef thisLand = getLandDef();
        LandState state = thisLand.getLandState();
        boolean isVanillaWolf = thisLand.getLandId().equals(ID_VANILLAWOLF);

        ServerAccess server = getServerAccess();

        // たまに同じ村が複数回出現するので注意！
        SortedSet<Village> result = new TreeSet<>();

        // トップページ
        if(state.equals(LandState.ACTIVE) || isVanillaWolf){
            HtmlSequence html = server.getHTMLTopPage();
            DecodedContent content = html.getContent();

            try{
                this.parser.parseAutomatic(content);
            }catch(HtmlParseException e){
                LOGGER.log(Level.WARNING, "トップページを認識できない", e);
            }
            List<VillageRecord> recList = this.handler.getVillageRecords();

            List<Village> list = buildVillageList(recList);
            result.addAll(list);
        }

        // 村一覧ページ
        if( ! isVanillaWolf ){
            HtmlSequence html = server.getHTMLLandList();
            DecodedContent content = html.getContent();

            try{
                this.parser.parseAutomatic(content);
            }catch(HtmlParseException e){
                LOGGER.log(Level.WARNING, "村一覧ページを認識できない", e);
            }
            List<VillageRecord> recList = this.handler.getVillageRecords();

            List<Village> list = buildVillageList(recList);
            result.addAll(list);
        }

        this.parser.reset();
        this.handler.reset();

        return result;
    }

    /**
     * 村リストを更新する。
     * @param vset ソート済みの村一覧
     */
    public void updateVillageList(SortedSet<Village> vset){
        // TODO 村リスト更新のイベントリスナがあると便利か？
        this.villageList.clear();
        this.villageList.addAll(vset);
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

}
