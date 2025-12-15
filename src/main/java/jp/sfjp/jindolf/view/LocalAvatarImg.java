/*
 * local avatar images
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jp.sfjp.jindolf.ResourceManager;
import jp.sfjp.jindolf.data.Avatar;

/**
 * 人狼BBSサーバにアクセスできなくなる将来に備えた代替イメージの諸々。
 *
 * <p>リソースに格納した代替Avatarイメージへのアクセスを提供する。
 *
 * <p>2020-04現在、凪庵氏作の新旧Avatarイメージは
 * 人狼BBSサーバ群より公衆送信中。
 *
 * @see <a href="https://ninjinix.com/">NINJINIX.COM</a>
 * @see <a href="https://yoroz.jp/">路地裏萬亭</a>
 */
public final class LocalAvatarImg {

    private static final String IMGDIR = "resources/image/avatar";
    private static final String TEMPLATE_FACE =
            IMGDIR + "/face{0,number,#00}.png";
    private static final String TEMPLATE_BODY =
            IMGDIR + "/body{0,number,#00}.png";
    private static final String RES_GRAVE     = IMGDIR + "/face99.png";
    private static final String RES_GRAVEBODY = IMGDIR + "/body99.png";

    private static final Map<String, BufferedImage> FACE_MAP;
    private static final Map<String, BufferedImage> BODY_MAP;

    private static final BufferedImage GRAVE_IMAGE;
    private static final BufferedImage GRAVEBODY_IMAGE;

    static{
        FACE_MAP = loadTemplateResImg(TEMPLATE_FACE);
        BODY_MAP = loadTemplateResImg(TEMPLATE_BODY);

        GRAVE_IMAGE     = ResourceManager.getBufferedImage(RES_GRAVE);
        GRAVEBODY_IMAGE = ResourceManager.getBufferedImage(RES_GRAVEBODY);
    }


    /**
     * Hidden constructor.
     */
    private LocalAvatarImg(){
        assert false;
    }


    /**
     * リソース名テンプレートにAvatarIdNumを適用して得られたリソースから
     * イメージを読み込む。
     *
     * @param resForm リソース名テンプレート
     * @return AvatarIdとリソースイメージからなるマップ
     */
    private static Map<String, BufferedImage> loadTemplateResImg(String resForm){
        Map<String, BufferedImage> result = new HashMap<>();

        Avatar.getPredefinedAvatarList().forEach(avatar -> {
            String avatarId = avatar.getIdentifier();
            int idNum = avatar.getIdNum();
            String res = MessageFormat.format(resForm, idNum);

            BufferedImage img = ResourceManager.getBufferedImage(res);
            assert img != null;
            result.put(avatarId, img);
        });

        return Collections.unmodifiableMap(result);
    }


    /**
     * Avatarの代替顔イメージを返す。
     *
     * @param avatarId AvatarId
     * @return 代替顔イメージ
     */
    public static BufferedImage getAvatarFaceImage(String avatarId){
        BufferedImage result = FACE_MAP.get(avatarId);
        return result;
    }

    /**
     * Avatarの代替全身像イメージを返す。
     *
     * @param avatarId AvatarId
     * @return 代替全身像イメージ
     */
    public static BufferedImage getAvatarBodyImage(String avatarId){
        BufferedImage result = BODY_MAP.get(avatarId);
        return result;
    }

    /**
     * 代替墓イメージを返す。
     *
     * @return 代替墓イメージ
     */
    public static BufferedImage getGraveImage(){
        return GRAVE_IMAGE;
    }

    /**
     * 代替墓イメージ(大)を返す。
     *
     * @return 代替墓イメージ(大)
     */
    public static BufferedImage getGraveBodyImage(){
        return GRAVEBODY_IMAGE;
    }

}
