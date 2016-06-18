/*
 * Hon5
 *
 * License : The MIT License
 * Copyright(c) 2016 olyutorskii
 */

package jp.sfjp.jindolf.dxchg;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.data.Village;
import jp.sfjp.jindolf.net.ServerAccess;

/**
 * ホの字5 溝の口ランキング(Hon5)に関する諸々。
 *
 * @see <a href="http://hon5.com/jinro/">キャスト紹介表ジェネレータ</a>
 * @see <a href="http://hon5.com/about.php">ホの字５製作委員会</a>
 */
public final class Hon5{

    private static final String URL_HON5CAST = "http://hon5.com/jinro/";


    /**
     * 隠しコンストラクタ。
     */
    private Hon5(){
        assert false;
        throw new AssertionError();
    }


    /**
     * キャスト紹介ジェネレータ出力のURLを得る。
     * @param village 村
     * @return ジェネレータ出力URL
     */
    public static String getCastGeneratorUrl(Village village){
        Land land = village.getParentLand();
        ServerAccess server = land.getServerAccess();
        URL villageUrl = server.getVillageURL(village);

        String result = getCastGeneratorUrl(villageUrl);

        return result;
    }

    /**
     * キャスト紹介ジェネレータ出力のURLを得る。
     * @param villageUrl 人狼BBSの村アドレス。
     * @return ジェネレータ出力URL
     */
    public static String getCastGeneratorUrl(URL villageUrl){
        String vUrlTxt = villageUrl.toString();

        String vcode;
        try{
            vcode = URLEncoder.encode(vUrlTxt, "UTF-8");
        }catch(UnsupportedEncodingException e){
            assert false;
            return null;
        }

        StringBuilder url = new StringBuilder();
        url.append(URL_HON5CAST);
        url.append("?u=");
        url.append(vcode);
        url.append("&s=1");

        String urlTxt = url.toString();
        return urlTxt;
    }

}
