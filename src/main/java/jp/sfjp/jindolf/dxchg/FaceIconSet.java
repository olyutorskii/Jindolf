/*
 * Face icon set
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.dxchg;

import java.util.HashMap;
import java.util.Map;
import jp.sfjp.jindolf.data.Avatar;

/**
 * 顔アイコンWiki表記のセット。
 */
public class FaceIconSet{

    private final String caption;
    private final String author;
    private final String urlText;
    private final Map<Avatar, String> wikiMap = new HashMap<>();

    /**
     * コンストラクタ。
     * @param caption 説明
     * @param author 作者
     * @param urlText URL文字列
     */
    public FaceIconSet(String caption, String author, String urlText){
        super();
        this.caption = caption;
        this.author = author;
        this.urlText = urlText;
        return;
    }

    /**
     * 説明文字列を得る。
     * @return 説明文字列
     */
    public String getCaption(){
        return this.caption;
    }

    /**
     * 作者名を得る。
     * @return 作者名
     */
    public String getAuthor(){
        return this.author;
    }

    /**
     * URL文字列を得る。
     * @return URL文字列
     */
    public String getUrlText(){
        return this.urlText;
    }

    /**
     * Avatarに対するWiki表記を登録する。
     * @param avatar Avatar
     * @param wiki Wiki表記
     */
    public void registIconWiki(Avatar avatar, String wiki){
        this.wikiMap.put(avatar, wiki);
        return;
    }

    /**
     * Avatarに対するWiki表記を取得する。
     * @param avatar Avatar
     * @return Wiki表記
     */
    public String getAvatarIconWiki(Avatar avatar){
        String wiki = this.wikiMap.get(avatar);
        return wiki;
    }

    /**
     * アイコンセットの文字列化。
     * コンボボックスアイテムの表記などで使われることを想定。
     * @return 文字列
     */
    @Override
    public String toString(){
        return getCaption();
    }

}
