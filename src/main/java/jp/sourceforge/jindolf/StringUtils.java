/*
 * string utilities
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.util.regex.Matcher;

/**
 * 文字列ユーティリティクラス。
 */
public final class StringUtils{

    private static final int SUPLEN = 5;

    /**
     * 正規表現にマッチした領域を数値化する。
     * @param seq 文字列
     * @param matcher Matcher
     * @param groupIndex 前方指定グループ番号
     * @return 数値
     * @throws IndexOutOfBoundsException 不正なグループ番号
     */
    public static int parseInt(CharSequence seq,
                                Matcher matcher,
                                int groupIndex     )
            throws IndexOutOfBoundsException {
        return parseInt(seq,
                        matcher.start(groupIndex),
                        matcher.end(groupIndex)   );
    }

    /**
     * 文字列を数値化する。
     * @param seq 文字列
     * @return 数値
     */
    public static int parseInt(CharSequence seq){
        return parseInt(seq, 0, seq.length());
    }

    /**
     * 部分文字列を数値化する。
     * @param seq 文字列
     * @param startPos 範囲開始位置
     * @param endPos 範囲終了位置
     * @return パースした数値
     * @throws IndexOutOfBoundsException 不正な位置指定
     */
    public static int parseInt(CharSequence seq, int startPos, int endPos)
            throws IndexOutOfBoundsException{
        int result = 0;

        for(int pos = startPos; pos < endPos; pos++){
            char ch = seq.charAt(pos);
            int digit = Character.digit(ch, 10);
            if(digit < 0) break;
            result *= 10;
            result += digit;
        }

        return result;
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
     * ある文字列の末尾が別の文字列に一致するか判定する。
     * @see String#endsWith(String)
     * @param target 判定対象
     * @param term 末尾文字
     * @return 一致すればtrue
     * @throws java.lang.NullPointerException 引数がnull
     */
    public static boolean isTerminated(CharSequence target,
                                         CharSequence term)
            throws NullPointerException{
        if(target == null || term == null) throw new NullPointerException();

        int targetLength = target.length();
        int termLength   = term  .length();

        int offset = targetLength - termLength;
        if(offset < 0) return false;

        for(int pos = 0; pos < termLength; pos++){
            char targetch = target.charAt(offset + pos);
            char termch   = term  .charAt(0      + pos);
            if(targetch != termch) return false;
        }

        return true;
    }

    /**
     * サブシーケンス同士を比較する。
     * @see CharSequence#subSequence(int,int)
     * @param seq1 サブシーケンス1
     * @param start1 開始インデックス1
     * @param end1 終了インデックス1
     * @param seq2 サブシーケンス2
     * @param start2 開始インデックス2
     * @param end2 終了インデックス2
     * @return サブシーケンス1の方が小さければ負、大きければ正、等しければ0
     * @throws IndexOutOfBoundsException 不正なインデックス指定
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
     * @see CharSequence#subSequence(int,int)
     * @param seq1 文字シーケンス
     * @param seq2 サブシーケンス
     * @param start2 開始インデックス
     * @param end2 終了インデックス
     * @return 文字シーケンスの方が小さければ負、大きければ正、等しければ0
     * @throws IndexOutOfBoundsException 不正なインデックス指定
     */
    public static int compareSubSequence(
            CharSequence seq1,
            CharSequence seq2, int start2, int end2 )
            throws IndexOutOfBoundsException{
        int result = compareSubSequence(seq1,      0, seq1.length(),
                                        seq2, start2,          end2 );
        return result;
    }

    /**
     * ダミーコンストラクタ。
     */
    private StringUtils(){
        super();
        assert false;
        throw new AssertionError();
    }

    // TODO 文字エンコーダ・デコーダ処理の一本化。
    // TODO 文字エンコーダ・デコーダのカスタム化。「～」対策など。
}
