/*
 * characters in village
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: Avatar.java 972 2009-12-26 05:05:15Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import jp.sourceforge.jindolf.corelib.PreDefAvatar;

/**
 * Avatar またの名をキャラクター。
 */
public class Avatar implements Comparable<Avatar> {

    private static final List<Avatar>        AVATAR_LIST;
    private static final Map<String, Avatar> AVATAR_MAP;

    private static final Pattern AVATAR_PATTERN;

    /** ゲルト。 */
    public static final Avatar AVATAR_GERD;

    static{
        List<PreDefAvatar>  predefs;
        try{
            DocumentBuilder builder = XmlUtils.createDocumentBuilder();
            predefs = PreDefAvatar.buildPreDefAvatarList(builder);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new ExceptionInInitializerError(e);
        }

        AVATAR_LIST = buildAvatarList(predefs);

        AVATAR_MAP = new HashMap<String, Avatar>();
        for(Avatar avatar : AVATAR_LIST){
            String fullName = avatar.getFullName();
            AVATAR_MAP.put(fullName, avatar);
        }

        StringBuilder avatarGroupRegex = new StringBuilder();
        for(Avatar avatar : AVATAR_LIST){
            String fullName = avatar.getFullName();
            if(avatarGroupRegex.length() > 0){
                avatarGroupRegex.append('|');
            }
            avatarGroupRegex.append('(')
                            .append(Pattern.quote(fullName))
                            .append(')');
        }
        AVATAR_PATTERN = Pattern.compile(avatarGroupRegex.toString());

        AVATAR_GERD = getPredefinedAvatar("楽天家 ゲルト");

        assert AVATAR_LIST instanceof RandomAccess;
        assert AVATAR_GERD != null;
    }

    /**
     * 定義済みAvatar群の生成。
     * @param predefs 定義済みAvatar元データ群
     * @return ソートされた定義済みAvatarのリスト
     */
    private static List<Avatar> buildAvatarList(List<PreDefAvatar> predefs){
        List<Avatar> result = new ArrayList<Avatar>(predefs.size());

        for(PreDefAvatar preDefAvatar : predefs){
            String shortName = preDefAvatar.getShortName();
            String jobTitle  = preDefAvatar.getJobTitle();
            int serialNo     = preDefAvatar.getSerialNo();
            String avatarId  = preDefAvatar.getAvatarId();
            Avatar avatar = new Avatar(shortName,
                                       jobTitle,
                                       serialNo,
                                       avatarId );
            result.add(avatar);
        }

        Collections.sort(result);
        result = Collections.unmodifiableList(result);

        return result;
    }

    /**
     * 定義済みAvatar群のリストを返す。
     * @return Avatarのリスト
     */
    public static List<Avatar> getPredefinedAvatarList(){
        return AVATAR_LIST;
    }

    /**
     * 定義済みAvatarを返す。
     * @param fullName Avatarのフルネーム
     * @return Avatar。フルネームが一致するAvatarが無ければnull
     */
    // TODO 20キャラ程度ならListをなめる方が早いか？
    public static Avatar getPredefinedAvatar(String fullName){
        return AVATAR_MAP.get(fullName);
    }

    /**
     * 定義済みAvatarを返す。
     * @param fullName Avatarのフルネーム
     * @return Avatar。フルネームが一致するAvatarが無ければnull
     */
    public static Avatar getPredefinedAvatar(CharSequence fullName){
        for(Avatar avatar : AVATAR_LIST){
            String avatarName = avatar.getFullName();
            if(avatarName.contentEquals(fullName)){
                return avatar;
            }
        }
        return null;
    }

    /**
     * 定義済みAvatar名に一致しないか調べる。
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

    private final String name;
    private final String jobTitle;
    private final String fullName;
    private final int idNum;
    private final String identifier;
    private final int hashNum;

    /**
     * Avatarを生成する。
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
     * Avatarを生成する。
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
     * フルネームを取得する。
     * @return フルネーム
     */
    public String getFullName(){
        return this.fullName;
    }

    /**
     * 職業名を取得する。
     * @return 職業名
     */
    public String getJobTitle(){
        return this.jobTitle;
    }

    /**
     * 通常名を取得する。
     * @return 通常名
     */
    public String getName(){
        return this.name;
    }

    /**
     * 通し番号を返す。
     * @return 通し番号
     */
    public int getIdNum(){
        return this.idNum;
    }

    /**
     * 識別文字列を返す。
     * @return 識別文字列
     */
    public String getIdentifier(){
        return this.identifier;
    }

    /**
     * {@inheritDoc}
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
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        return this.hashNum;
    }

    @Override
    public String toString(){
        return getFullName();
    }

    /**
     * {@inheritDoc}
     * 通し番号順に順序づける。
     * @param avatar {@inheritDoc}
     * @return {@inheritDoc}
     */
    public int compareTo(Avatar avatar){
        if(avatar == null) return +1;
        return this.idNum - avatar.idNum;
    }

}
