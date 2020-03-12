/*
 * village XML file element tags
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * XMLファイルのタグ要素名デコーダ。
 */
public enum ElemTag {

    VILLAGE("village"),
    AVATAR("avatar"),
    PERIOD("period"),
    TALK("talk"),
    LI("li"),
    ;


    private final String name;


    /**
     * constructor.
     *
     * @param name element name
     */
    ElemTag(String name){
        this.name = name;
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

}
