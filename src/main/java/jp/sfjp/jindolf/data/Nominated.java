/*
 * nominated with execution info
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data;

/**
 * 処刑投票の処刑先別集計。
 */
public class Nominated {

    private final Avatar avatar;
    private final int count;


    /**
     * constructor.
     *
     * @param avatar 処刑希望先Avatar
     * @param count 処刑票数
     */
    public Nominated(Avatar avatar, int count){
        super();
        this.avatar = avatar;
        this.count = count;
        return;
    }

    /**
     * 処刑希望先Avatarを返す。
     *
     * @return Avatar
     */
    public Avatar getAvatar(){
        return this.avatar;
    }

    /**
     * 処刑票数を返す。
     *
     * @return 票数
     */
    public int getCount(){
        return this.count;
    }

}
