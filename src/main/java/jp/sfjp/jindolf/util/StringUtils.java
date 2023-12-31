/*
 * string utilities
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.util;

/**
 * 文字列ユーティリティクラス。
 */
public final class StringUtils{

    private static final int SUPLEN = 5;


    /**
     * ダミーコンストラクタ。
     */
    private StringUtils(){
        super();
        assert false;
        throw new AssertionError();
    }


    /**
     * 長い文字列を三点リーダで省略する。
     * 「abcdefg」→「abc…efg」
     * @param str 文字列
     * @return 省略した文字列
     */
    public static CharSequence suppressString(CharSequence str){
        String result = str.toString();
        result = result.replaceAll("[\u0020\\t\\n\\r\u3000]", "");
        if(result.length() <= SUPLEN * 2) return result;
        int len = result.length();
        String head = result.substring(0, SUPLEN);
        String tail = result.substring(len - SUPLEN, len);
        result = head + "…" + tail;
        return result;
    }

    /**
     * サブシーケンス同士を比較する。
     * @param seq1 サブシーケンス1
     * @param start1 開始インデックス1
     * @param end1 終了インデックス1
     * @param seq2 サブシーケンス2
     * @param start2 開始インデックス2
     * @param end2 終了インデックス2
     * @return サブシーケンス1の方が小さければ負、大きければ正、等しければ0
     * @throws IndexOutOfBoundsException 不正なインデックス指定
     * @see CharSequence#subSequence(int,int)
     */
    public static int compareSubSequence(
            CharSequence seq1, int start1, int end1,
            CharSequence seq2, int start2, int end2 )
            throws IndexOutOfBoundsException{
        int pos1 = start1;
        int pos2 = start2;

        for(;;){
            if(pos1 >= end1) break;
            if(pos2 >= end2) break;
            char ch1 = seq1.charAt(pos1);
            char ch2 = seq2.charAt(pos2);
            int diff = ch1 - ch2;
            if(diff != 0) return diff;
            pos1++;
            pos2++;
        }

        int length1 = end1 - start1;
        int length2 = end2 - start2;

        if(length1 == length2) return 0;
        if(length1 <  length2) return -1;
        else                   return +1;
    }

    /**
     * 文字シーケンスとサブシーケンスを比較する。
     * @param seq1 文字シーケンス
     * @param seq2 サブシーケンス
     * @param start2 開始インデックス
     * @param end2 終了インデックス
     * @return 文字シーケンスの方が小さければ負、大きければ正、等しければ0
     * @throws IndexOutOfBoundsException 不正なインデックス指定
     * @see CharSequence#subSequence(int,int)
     */
    public static int compareSubSequence(
            CharSequence seq1,
            CharSequence seq2, int start2, int end2 )
            throws IndexOutOfBoundsException{
        int result = compareSubSequence(seq1,      0, seq1.length(),
                                        seq2, start2,          end2 );
        return result;
    }

    // TODO 文字エンコーダ・デコーダ処理の一本化。
    // TODO 文字エンコーダ・デコーダのカスタム化。「～」対策など。
}
