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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * <p>2020-03現在、既知のAvatarは最大20種類で固定。
 * Z国含め未知のAvatarが追加されるケースは今後考慮しない。
 */
public class Avatar implements Comparable<Avatar> {

    /** ゲルト。 */
    public static final Avatar AVATAR_GERD;

    private static final List<Avatar>        AVATAR_LIST;
    private static final Map<String, Avatar> AVATAR_FN_MAP;
    private static final Map<String, Avatar> AVATAR_ID_MAP;

    private static final Pattern AVATAR_PATTERN;

    static{
        List<PreDefAvatar>  predefs = CoreData.getPreDefAvatarList();
        AVATAR_LIST = buildAvatarList(predefs);

        AVATAR_FN_MAP = new HashMap<>();
        AVATAR_ID_MAP = new HashMap<>();
        AVATAR_LIST.forEach(avatar -> {
            String fullName = avatar.getFullName();
            String avatarId = avatar.getIdentifier();
            AVATAR_FN_MAP.put(fullName, avatar);
            AVATAR_ID_MAP.put(avatarId, avatar);
        });

        StringBuilder avatarGroupRegex = new StringBuilder();
        AVATAR_LIST.stream().map((avatar) ->
            avatar.getFullName()
        ).forEachOrdered((fullName) -> {
            if(avatarGroupRegex.length() > 0){
                avatarGroupRegex.append('|');
            }
            avatarGroupRegex.append('(')
                    .append(Pattern.quote(fullName))
                    .append(')');
        });
        AVATAR_PATTERN = Pattern.compile(avatarGroupRegex.toString());

        AVATAR_GERD = getAvatarByFullname("楽天家 ゲルト");

        assert AVATAR_LIST instanceof RandomAccess;
        assert AVATAR_GERD != null;
    }


    private final String name;
    private final String jobTitle;
    private final String fullName;
    private final int idNum;
    private final String identifier;
    private final int hashNum;


    /**
     * constructor.
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
        this.name = name.intern();
        this.jobTitle = jobTitle.intern();
        this.idNum = idNum;
        this.identifier = identifier.intern();

        this.fullName = (this.jobTitle + " " + this.name).intern();

        this.hashNum = this.fullName.hashCode() ^ this.idNum;

        return;
    }

    /**
     * constructor.
     *
     * @param fullName フルネーム
     */
    // TODO 当面は呼ばれないはず。Z国とか向け。
    public Avatar(String fullName){
        this.fullName = fullName.intern();
        this.idNum = -1;

        String[] tokens = this.fullName.split("\\p{Blank}+", 2);
        if(tokens.length == 1){
            this.jobTitle = null;
            this.name = this.fullName;
        }else if(tokens.length == 2){
            this.jobTitle = tokens[0].intern();
            this.name = tokens[1].intern();
        }else{
            this.jobTitle = null;
            this.name = null;
            assert false;
        }

        this.identifier = "???".intern();

        this.hashNum = this.fullName.hashCode() ^ this.idNum;

        return;
    }


    /**
     * 定義済みAvatarのAvatarリストを生成する。
     *
     * @param predefs 定義済みAvatar元データ群
     * @return ソートされた定義済みAvatarのリスト
     */
    private static List<Avatar> buildAvatarList(List<PreDefAvatar> predefs){
        List<Avatar> result;

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
    // TODO 20キャラ程度ならListをなめる方が早いか？
    public static Avatar getAvatarByFullname(String fullNameArg){
        return AVATAR_FN_MAP.get(fullNameArg);
    }

    /**
     * フルネームに合致するAvatarを返す。
     *
     * @param fullNameArg Avatarのフルネーム
     * @return Avatar。フルネームが一致するAvatarが無ければnull
     */
    public static Avatar getAvatarByFullname(CharSequence fullNameArg){
        for(Avatar avatar : AVATAR_LIST){
            String avatarName = avatar.getFullName();
            if(avatarName.contentEquals(fullNameArg)){
                return avatar;
            }
        }
        return null;
    }

    /**
     * IDに合致するAvatarを返す。
     *
     * @param avatarId AvatarのID
     * @return Avatar。IDが一致するAvatarが無ければnull
     */
    // TODO 20キャラ程度ならListをなめる方が早いか？
    public static Avatar getAvatarById(String avatarId){
        return AVATAR_ID_MAP.get(avatarId);
    }

    /**
     * 定義済みAvatar名に一致しないか調べる。
     *
     * @param matcher マッチャ
     * @return 一致したAvatar。一致しなければnull。
     */
    public static Avatar lookingAtAvatar(Matcher matcher){
        matcher.usePattern(AVATAR_PATTERN);

        if( ! matcher.lookingAt() ) return null;
        int groupCt = matcher.groupCount();
        for(int group = 1; group <= groupCt; group++){
            if(matcher.start(group) >= 0){
                Avatar avatar = AVATAR_LIST.get(group - 1);
                return avatar;
            }
        }

        return null;
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
     * 識別文字列を返す。
     *
     * @return 識別文字列
     */
    public String getIdentifier(){
        return this.identifier;
    }

    /**
     * {@inheritDoc}
     *
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }

        if( ! (obj instanceof Avatar) ){
            return false;
        }
        Avatar other = (Avatar) obj;

        boolean nameMatch = this.fullName.equals(other.fullName);
        boolean idMatch = this.idNum == other.idNum;

        if(nameMatch && idMatch) return true;

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        return this.hashNum;
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
     * 通し番号順に順序づける。
     * @param avatar {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(Avatar avatar){
        if(avatar == null) return +1;
        return this.idNum - avatar.idNum;
    }

}
