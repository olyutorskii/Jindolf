/*
 * characters in village
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.stream.Collectors;
import jp.sourceforge.jindolf.corelib.PreDefAvatar;

/**
 * Avatar またの名をキャラクター。
 *
 * <p>ゲルトもAvatarである。
 * 墓石は「Avatarの状態」であってAvatarそのものではない。
 *
 * <p>プロローグが終わり参加プレイヤーが固定されるまでの間、
 * 複数のプレイヤーが同一Avatarを担当しうる。
 *
 * <p>Avatar同士は通し番号により一意に順序づけられる。
 *
 * <p>設計メモ: 未知のAvatar出現に備え、
 * {@link PreDefAvatar}と分離したクラスとして設計された。
 *
 * <p>2020-03現在、Avatarは20インスタンスで固定。
 * Z国含め未知のAvatarが追加されるケースは今後考慮しない。
 */
public class Avatar implements Comparable<Avatar> {

    /** ゲルト。 */
    public static final Avatar AVATAR_GERD;

    private static final List<Avatar>        AVATAR_LIST = buildAvatarList();
    private static final Map<String, Avatar> AVATAR_FN_MAP = new HashMap<>();
    private static final Map<String, Avatar> AVATAR_ID_MAP = new HashMap<>();


    static{
        AVATAR_LIST.forEach(avatar -> {
            String fullName = avatar.getFullName();
            String avatarId = avatar.getIdentifier();
            AVATAR_FN_MAP.put(fullName, avatar);
            AVATAR_ID_MAP.put(avatarId, avatar);
        });

        AVATAR_GERD = getAvatarById("gerd");

        assert AVATAR_LIST instanceof RandomAccess;
        assert AVATAR_GERD != null;
    }


    private final String name;
    private final String jobTitle;
    private final String fullName;
    private final int idNum;
    private final String identifier;


    /**
     * constructor.
     *
     * <p>全ての引数は他のインスタンスに対しユニークでなければならない。
     *
     * @param name 名前
     * @param jobTitle 職業名
     * @param idNum 通し番号
     * @param identifier 識別文字列
     */
    private Avatar(String name,
                   String jobTitle,
                   int idNum,
                   String identifier ){
        super();

        this.name = name.intern();
        this.jobTitle = jobTitle.intern();
        this.idNum = idNum;
        this.identifier = identifier.intern();

        this.fullName = (this.jobTitle + " " + this.name).intern();

        return;
    }


    /**
     * Avatarリストを生成する。
     *
     * <p>Avatarの全インスタンスはこのリストに含まれる。
     *
     * @return ソートされた定義済みAvatarのリスト
     */
    private static List<Avatar> buildAvatarList(){
        List<Avatar> result;

        List<PreDefAvatar>  predefs = CoreData.getPreDefAvatarList();
        result = predefs.stream()
                .map(preDefAvatar -> toAvatar(preDefAvatar))
                .sorted()
                .collect(Collectors.toList());

        result = Collections.unmodifiableList(result);

        return result;
    }

    /**
     * 定義済みAvatarからAvatarへの変換を行う。
     *
     * @param pre 定義済みAvatar
     * @return Avatar
     */
    private static Avatar toAvatar(PreDefAvatar pre){
        String shortName = pre.getShortName();
        String jobTitle  = pre.getJobTitle();
        int serialNo     = pre.getSerialNo();
        String avatarId  = pre.getAvatarId();

        Avatar result = new Avatar(shortName, jobTitle, serialNo, avatarId);
        return result;
    }

    /**
     * 定義済みAvatar群のリストを返す。
     *
     * @return Avatarのリスト
     */
    public static List<Avatar> getPredefinedAvatarList(){
        return AVATAR_LIST;
    }

    /**
     * フルネームに合致するAvatarを返す。
     *
     * @param fullNameArg Avatarのフルネーム
     * @return Avatar。フルネームが一致するAvatarが無ければnull
     */
    public static Avatar getAvatarByFullname(String fullNameArg){
        return AVATAR_FN_MAP.get(fullNameArg);
    }

    /**
     * IDに合致するAvatarを返す。
     *
     * @param avatarId AvatarのID
     * @return Avatar。IDが一致するAvatarが無ければnull
     */
    public static Avatar getAvatarById(String avatarId){
        return AVATAR_ID_MAP.get(avatarId);
    }


    /**
     * フルネームを取得する。
     *
     * @return フルネーム
     */
    public String getFullName(){
        return this.fullName;
    }

    /**
     * 職業名を取得する。
     *
     * @return 職業名
     */
    public String getJobTitle(){
        return this.jobTitle;
    }

    /**
     * 通常名を取得する。
     *
     * @return 通常名
     */
    public String getName(){
        return this.name;
    }

    /**
     * 通し番号を返す。
     *
     * @return 通し番号
     */
    public int getIdNum(){
        return this.idNum;
    }

    /**
     * AvatarID識別文字列を返す。
     *
     * @return 識別文字列
     */
    public String getIdentifier(){
        return this.identifier;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        return getFullName();
    }

    /**
     * {@inheritDoc}
     *
     * <p>通し番号順に順序づける。
     *
     * @param avatar {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(Avatar avatar){
        if(avatar == null) return +1;
        return this.idNum - avatar.idNum;
    }

}
