/*
 * Player
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.data;

import jp.sourceforge.jindolf.corelib.Destiny;
import jp.sourceforge.jindolf.corelib.GameRole;

/**
 * プレイヤーに関する情報の集約。
 */
public class Player{

    private Avatar avatar;
    private GameRole role;
    private Destiny destiny;
    private int obitDay = -1;
    private String idName;
    private String urlText;
    private int entryNo = -1;

    /**
     * コンストラクタ。
     */
    public Player(){
        super();
        return;
    }

    /**
     * Avatar名を返す。
     * @return Avatar名
     */
    @Override
    public String toString(){
        if(this.avatar == null) return "?";
        return this.avatar.toString();
    }

    /**
     * Avatarを取得する。
     * @return Avatar
     */
    public Avatar getAvatar(){
        return avatar;
    }

    /**
     * Avatarをセットする。
     * @param avatar Avatar
     */
    public void setAvatar(Avatar avatar){
        this.avatar = avatar;
        return;
    }

    /**
     * 役割を取得する。
     * @return 役割
     */
    public GameRole getRole(){
        return role;
    }

    /**
     * 役割を設定する。
     * @param role 役割
     */
    public void setRole(GameRole role){
        this.role = role;
        return;
    }

    /**
     * 運命を取得する。
     * @return 運命
     */
    public Destiny getDestiny(){
        return destiny;
    }

    /**
     * 運命を設定する。
     * @param destiny 運命
     */
    public void setDestiny(Destiny destiny){
        this.destiny = destiny;
        return;
    }

    /**
     * 命日を取得する。
     * @return プロローグを0とする命日。死んでなければ負。
     */
    public int getObitDay(){
        return obitDay;
    }

    /**
     * 命日を設定する。
     * @param obitDay プロローグを0とする命日。死んでなければ負。
     */
    public void setObitDay(int obitDay){
        this.obitDay = obitDay;
        return;
    }

    /**
     * プレイヤーIDを取得する。
     * @return プレイヤーID
     */
    public String getIdName(){
        return idName;
    }

    /**
     * プレイヤーIDを設定する。
     * @param idName プレイヤーID
     */
    public void setIdName(String idName){
        this.idName = idName;
        return;
    }

    /**
     * URL文字列を取得する。
     * 必ずしもURLを満たす文字列ではないかもしれない。
     * @return URL文字列
     */
    public String getUrlText(){
        return urlText;
    }

    /**
     * URL文字列を設定する。
     * @param urlText URL文字列
     */
    public void setUrlText(String urlText){
        this.urlText = urlText;
        return;
    }

    /**
     * エントリーNo.を取得する。
     * @return エントリーNo.
     */
    public int getEntryNo(){
        return entryNo;
    }

    /**
     * エントリーNo.を設定する。
     * @param entryNo エントリーNo.
     */
    public void setEntryNo(int entryNo){
        this.entryNo = entryNo;
        return;
    }

    /**
     * プレイヤーの運命を文字列化する。
     * @return 文字列化した運命
     */
    public String getDestinyMessage(){
        StringBuilder destinyMessage = new StringBuilder();

        switch(this.destiny){
        case ALIVE:
            assert this.obitDay < 0;
            destinyMessage.append("最後まで生存");
            break;
        default:
            assert this.obitDay >= 0;
            destinyMessage.append(this.obitDay).append("日目に");
            destinyMessage.append(this.destiny.getMessage());
        }

        return destinyMessage.toString();
    }

}
