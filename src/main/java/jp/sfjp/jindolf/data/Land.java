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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.sfjp.jindolf.net.ServerAccess;
import jp.sourceforge.jindolf.corelib.LandDef;

/**
 * いわゆる「国」。
 *
 * <p>人狼BBSのサーバと1:1の概念。
 */
public class Land {

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    private final LandDef landDef;
    private final ServerAccess serverAccess;

    private final List<Village> villageList = new ArrayList<>(1000);


    /**
     * コンストラクタ。
     *
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

        return;
    }


    /**
     * 国定義を得る。
     *
     * @return 国定義
     */
    public LandDef getLandDef(){
        return this.landDef;
    }

    /**
     * サーバ接続を返す。
     *
     * @return ServerAccessインスタンス
     */
    public ServerAccess getServerAccess(){
        return this.serverAccess;
    }

    /**
     * 指定されたインデックス位置の村を返す。
     *
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
     *
     * @return 村の総数
     */
    public int getVillageCount(){
        int result = this.villageList.size();
        return result;
    }

    /**
     * 村のリストを返す。
     *
     * @return 村のリスト
     */
    // TODO インスタンス変数でいいはず。
    public List<Village> getVillageList(){
        return Collections.unmodifiableList(this.villageList);
    }

    /**
     * 絶対または相対URLの指すパーマネントなイメージ画像をダウンロードする。
     *
     * <p>※ A,B,D 国の顔アイコンは絶対パスらしい…。
     *
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
     *
     * @return 墓アイコンイメージ
     */
    public BufferedImage getGraveIconImage(){
        URI uri = getLandDef().getTombFaceIconURI();
        BufferedImage result = downloadImage(uri.toASCIIString());
        return result;
    }

    /**
     * 墓アイコンイメージ(大)を取得する。
     *
     * @return 墓アイコンイメージ(大)
     */
    public BufferedImage getGraveBodyImage(){
        URI uri = getLandDef().getTombBodyIconURI();
        BufferedImage result = downloadImage(uri.toASCIIString());
        return result;
    }

    /**
     * 村リストを更新する。
     *
     * @param vset ソート済みの村一覧
     */
    public void updateVillageList(List<Village> vset){
        // TODO 村リスト更新のイベントリスナがあると便利か？
        this.villageList.clear();
        this.villageList.addAll(vset);
        return;
    }

    /**
     * 国の文字列表現を返す。
     *
     * @return 文字列表現
     */
    @Override
    public String toString(){
        return getLandDef().getLandName();
    }

}
