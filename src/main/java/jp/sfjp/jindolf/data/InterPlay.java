/*
 * interplay
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data;

/**
 * Avatar間の行為関係。
 *
 * <p>行為を行う者と行為の対象者から構成される。
 *
 * <p>処刑投票、襲撃など。
 */
public class InterPlay {

    private final Avatar byWhom;
    private final Avatar target;


    /**
     * constructor.
     *
     * @param byWhom 行為者
     * @param target 行為の対象
     */
    public InterPlay(Avatar byWhom, Avatar target){
        super();
        this.byWhom = byWhom;
        this.target = target;
        return;
    }


    /**
     * 行為者を返す。
     *
     * @return 行為者
     */
    public Avatar getByWhom(){
        return this.byWhom;
    }

    /**
     * 行為対象を返す。
     *
     * @return 行為対象
     */
    public Avatar getTarget(){
        return this.target;
    }

}
