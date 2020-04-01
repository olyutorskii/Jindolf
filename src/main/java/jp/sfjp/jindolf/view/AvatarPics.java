/*
 * Avatar image loader
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import jp.sfjp.jindolf.data.Avatar;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sourceforge.jindolf.corelib.LandDef;

/**
 * Avatarの顔、全身像、遺影、墓の画像をキャッシュ管理する。
 */
public class AvatarPics {

    private final Land land;

    private final Map<Avatar, BufferedImage> faceImageMap =
            new HashMap<>();
    private final Map<Avatar, BufferedImage> bodyImageMap =
            new HashMap<>();
    private final Map<Avatar, BufferedImage> faceMonoImageMap =
            new HashMap<>();
    private final Map<Avatar, BufferedImage> bodyMonoImageMap =
            new HashMap<>();


    /**
     * COnstructor.
     *
     * @param land 国
     */
    public AvatarPics(Land land){
        super();
        this.land = land;
        return;
    }


    /**
     * Avatarの顔イメージを返す。
     *
     * @param avatar Avatar
     * @return 顔イメージ
     */
    public BufferedImage getAvatarFaceImage(Avatar avatar){
        BufferedImage result;
        result = this.faceImageMap.get(avatar);
        if(result != null) return result;

        LandDef landDef = this.land.getLandDef();

        String template = landDef.getFaceURITemplate();
        int serialNo = avatar.getIdNum();
        result = loadAvatarImage(template, serialNo);

        this.faceImageMap.put(avatar, result);

        return result;
    }

    /**
     * Avatarの全身像イメージを返す。
     *
     * @param avatar Avatar
     * @return 全身イメージ
     */
    public BufferedImage getAvatarBodyImage(Avatar avatar){
        BufferedImage result;
        result = this.bodyImageMap.get(avatar);
        if(result != null) return result;

        LandDef landDef = this.land.getLandDef();

        String template = landDef.getBodyURITemplate();
        int serialNo = avatar.getIdNum();
        result = loadAvatarImage(template, serialNo);

        this.bodyImageMap.put(avatar, result);

        return result;
    }

    /**
     * 各国URLテンプレートと通し番号から
     * イメージをダウンロードする。
     *
     * @param template テンプレート
     * @param serialNo Avatarの通し番号
     * @return 顔もしくは全身像イメージ
     */
    private BufferedImage loadAvatarImage(String template, int serialNo){
        String uri = MessageFormat.format(template, serialNo);

        BufferedImage result;
        result = this.land.downloadImage(uri);
        if(result == null) result = GUIUtils.getNoImage();

        return result;
    }

    /**
     * Avatarのモノクロ顔イメージを返す。
     *
     * @param avatar Avatar
     * @return 顔イメージ
     */
    public BufferedImage getAvatarFaceMonoImage(Avatar avatar){
        BufferedImage result;
        result = this.faceMonoImageMap.get(avatar);
        if(result == null){
            result = getAvatarFaceImage(avatar);
            result = GUIUtils.createMonoImage(result);
            this.faceMonoImageMap.put(avatar, result);
        }
        return result;
    }

    /**
     * Avatarの全身像イメージを返す。
     *
     * @param avatar Avatar
     * @return 全身イメージ
     */
    public BufferedImage getAvatarBodyMonoImage(Avatar avatar){
        BufferedImage result;
        result = this.bodyMonoImageMap.get(avatar);
        if(result == null){
            result = getAvatarBodyImage(avatar);
            result = GUIUtils.createMonoImage(result);
            this.bodyMonoImageMap.put(avatar, result);
        }
        return result;
    }

    /**
     * 国に登録された墓イメージを返す。
     *
     * @return 墓イメージ
     */
    public BufferedImage getGraveImage(){
        BufferedImage result = this.land.getGraveIconImage();
        return result;
    }

    /**
     * 国に登録された墓イメージ(大)を返す。
     *
     * @return 墓イメージ(大)
     */
    public BufferedImage getGraveBodyImage(){
        BufferedImage result = this.land.getGraveBodyImage();
        return result;
    }

}
