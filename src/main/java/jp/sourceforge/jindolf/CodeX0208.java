/*
 * JIS X0208:1990 文字集合に関する諸々
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: CodeX0208.java 1002 2010-03-15 12:14:20Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.io.IOException;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * JIS X0208:1990 文字集合に関する諸々。
 * TODO G国がUTF-8化した今、このクラスは不要？
 */
public final class CodeX0208{

    private static final String RESOURCE_INVALIDCHAR =
            "resources/invalidX0208.txt";
    private static final char[] INVALID_CHAR_ARRAY = createInvalidCharArray();

    /**
     * ソートされた、禁止文字配列を生成する。
     * @return 禁止文字配列。
     */
    private static char[] createInvalidCharArray(){
        CharSequence source;
        try{
            source = Jindolf.loadResourceText(RESOURCE_INVALIDCHAR);
        }catch(IOException e){
            assert false;
            return null;
        }

        SortedSet<Character> charSet = new TreeSet<Character>();
        int sourceLength = source.length();
        for(int pos = 0; pos < sourceLength; pos++){
            char ch = source.charAt(pos);
            if(Character.isWhitespace(ch)) continue;
            charSet.add(ch);
        }

        char[] result = new char[charSet.size()];
        int pos = 0;
        for(char ch : charSet){
            result[pos++] = ch;
        }

        Arrays.sort(result);

        return result;
    }

    /**
     * 禁止文字か否か判定する。
     * @param ch 判定対象文字
     * @return 禁止ならfalse
     */
    public static boolean isValid(char ch){
        int index = Arrays.binarySearch(INVALID_CHAR_ARRAY, ch);
        if(index < 0) return true;
        return false;
    }

    /**
     * ダミーコンストラクタ。
     */
    private CodeX0208(){
        assert false;
        throw new AssertionError();
    }

    // TODO アラビア語やハングルやも弾きたい。
    // TODO JISエンコーダと区点チェックに作り直すか？
}
