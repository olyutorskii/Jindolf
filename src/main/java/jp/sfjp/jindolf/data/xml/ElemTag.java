/*
 * village XML file element tags
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data.xml;

import java.util.HashMap;
import java.util.Map;
import jp.sourceforge.jindolf.corelib.SysEventType;

/**
 * XMLファイルのタグ要素名デコーダ。
 */
public enum ElemTag {

    VILLAGE("village"),
    AVATARLIST("avatarList"),
    AVATAR("avatar"),
    AVATARREF("avatarRef"),
    PERIOD("period"),

    TALK("talk"),
    LI("li"),
    RAWDATA("rawdata"),

    STARTENTRY("startEntry", SysEventType.STARTENTRY),
    ONSTAGE("onStage", SysEventType.ONSTAGE),
    STARTMIRROR("startMirror", SysEventType.STARTMIRROR),
    OPENROLE("openRole", SysEventType.OPENROLE),
    MURDERED("murdered", SysEventType.MURDERED),
    STARTASSAULT("startAssault", SysEventType.STARTASSAULT),
    SURVIVOR("survivor", SysEventType.SURVIVOR),
    COUNTING("counting", SysEventType.COUNTING),
    SUDDENDEATH("suddenDeath", SysEventType.SUDDENDEATH),
    NOMURDER("noMurder", SysEventType.NOMURDER),
    WINVILLAGE("winVillage", SysEventType.WINVILLAGE),
    WINWOLF("winWolf", SysEventType.WINWOLF),
    WINHAMSTER("winHamster", SysEventType.WINHAMSTER),
    PLAYERLIST("playerList", SysEventType.PLAYERLIST),
    PANIC("panic", SysEventType.PANIC),
    EXECUTION("execution", SysEventType.EXECUTION),
    VANISH("vanish", SysEventType.VANISH),
    CHECKOUT("checkout", SysEventType.CHECKOUT),
    SHORTMEMBER("shortMember", SysEventType.SHORTMEMBER),
    ASKENTRY("askEntry", SysEventType.ASKENTRY),
    ASKCOMMIT("askCommit", SysEventType.ASKCOMMIT),
    NOCOMMENT("noComment", SysEventType.NOCOMMENT),
    STAYEPILOGUE("stayEpilogue", SysEventType.STAYEPILOGUE),
    GAMEOVER("gameOver", SysEventType.GAMEOVER),
    JUDGE("judge", SysEventType.JUDGE),
    GUARD("guard", SysEventType.GUARD),
    COUNTING2("counting2", SysEventType.COUNTING2),
    ASSAULT("assault", SysEventType.ASSAULT),

    ROLEHEADS("roleHeads"),
    VOTE("vote"),
    PLAYERINFO("playerInfo"),
    NOMINATED("nominated"),
    ;


    private final String name;
    private final SysEventType sysEventType;


    /**
     * constructor.
     *
     * @param name element name
     * @param isSysEvent true if SysEvent
     */
    ElemTag(String name, SysEventType type){
        this.name = name;
        this.sysEventType = type;
        return;
    }

    /**
     * constructor.
     *
     * <p>It's not SysEvent.
     *
     * @param name element name
     */
    ElemTag(String name){
        this(name, null);
        return;
    }


    /**
     * get ElemTag map with name-space Prefixed key.
     *
     * @param pfx prefix
     * @return ElemTag
     */
    public static Map<String, ElemTag> getQNameMap(String pfx){
        Map<String, ElemTag> result = new HashMap<>();

        String lead;
        if(pfx.isEmpty()){
            lead = "";
        }else{
            lead = pfx + ":";
        }

        for(ElemTag tag : values()){
            String key = lead + tag.name;
            key = key.intern();
            result.put(key, tag);
        }

        return result;
    }


    /**
     * タグがSysEventか判定する。
     *
     * @return SysEventならtrue
     */
    public boolean isSysEventTag(){
        return this.sysEventType != null;
    }

    /**
     * return SysEventType.
     *
     * @return SysEventType. true if not SystemEvent.
     */
    public SysEventType getSystemEventType(){
        return this.sysEventType;
    }

}
