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
import java.util.Objects;
import jp.sfjp.jindolf.data.Avatar;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sourceforge.jindolf.corelib.LandDef;

/**
 * Avatarの顔、全身像、遺影、墓の画像をキャッシュ管理する。
 */
public class AvatarPics {

    private final Land land;

    private final Map<Avatar, BufferedImage> faceImageMap;
    private final Map<Avatar, BufferedImage> bodyImageMap;
    private final Map<Avatar, BufferedImage> faceMonoImageMap;
    private final Map<Avatar, BufferedImage> bodyMonoImageMap;

    private BufferedImage graveImage;
    private BufferedImage graveBodyImage;


    /**
     * Constructor.
     *
     * @param land 国
     */
    public AvatarPics(Land land){
        super();

        Objects.nonNull(land);
        this.land = land;

        this.faceImageMap     = new HashMap<>();
        this.bodyImageMap     = new HashMap<>();
        this.faceMonoImageMap = new HashMap<>();
        this.bodyMonoImageMap = new HashMap<>();

        this.graveImage     = null;
        this.graveBodyImage = null;

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
     * Avatarの顔イメージを設定する。
     *
     * @param avatar Avatar
     * @param image イメージ
     */
    public void setAvatarFaceImage(Avatar avatar, BufferedImage image){
        this.faceImageMap.remove(avatar);
        this.faceMonoImageMap.remove(avatar);
        this.faceImageMap.put(avatar, image);
        return;
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
     * Avatarの全身像イメージを設定する。
     *
     * @param avatar Avatar
     * @param image イメージ
     */
    public void setAvatarBodyImage(Avatar avatar, BufferedImage image){
        this.bodyImageMap.remove(avatar);
        this.bodyMonoImageMap.remove(avatar);
        this.bodyImageMap.put(avatar, image);
        return;
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
     * 国別の墓イメージを返す。
     *
     * @return 墓イメージ
     */
    public BufferedImage getGraveImage(){
        if(this.graveImage != null) return this.graveImage;

        BufferedImage result = this.land.getGraveIconImage();
        this.graveImage = result;

        return result;
    }

    /**
     * 墓イメージを設定する。
     *
     * @param image イメージ
     */
    public void setGraveImage(BufferedImage image){
        this.graveImage = image;
        return;
    }

    /**
     * 国別の墓イメージ(大)を返す。
     *
     * @return 墓イメージ(大)
     */
    public BufferedImage getGraveBodyImage(){
        if(this.graveBodyImage != null) return this.graveBodyImage;

        BufferedImage result = this.land.getGraveBodyImage();
        this.graveBodyImage = result;

        return result;
    }

    /**
     * 墓イメージ(大)を設定する。
     *
     * @param image イメージ
     */
    public void setGraveBodyImage(BufferedImage image){
        this.graveBodyImage = image;
        return;
    }

    /**
     * 全画像のキャッシュへの格納を試みる。
     */
    public void preload(){
        for(Avatar avatar : Avatar.getPredefinedAvatarList()){
            getAvatarFaceImage(avatar);
            getAvatarBodyImage(avatar);
            getAvatarFaceMonoImage(avatar);
            getAvatarBodyMonoImage(avatar);
            getGraveImage();
            getGraveBodyImage();
        }
    }

}
