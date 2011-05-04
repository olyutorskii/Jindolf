/*
 * anchor
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 発言アンカー。
 */
public final class Anchor{

    private static final int EPILOGUEDAY = 99;
    private static final Pattern ANCHOR_PATTERN;

    static{
        String spchar = "\u0020\u3000\\t";
        String sp = "[" +spchar+ "]";
        String sp_n = "(?:" + sp + "|" + "(?:\\Q&nbsp;\\E)" + ")*?";

        String day =   // TODO 「昨日」なども含めるか？
                "("
                    +"(?:"
                        +    "(プロ(?:ローグ)?)"
                        +"|"+"(エピ(?:ローグ)?)"
                        +"|"+"(?:"
                                + "([1-9１-９]?[0-9０-９])"
                                +sp_n+ "(?:[dDｄＤ]|(?:日目?))"
                            +")"
                    +")" +"[\\-\\[\\(/_－ー―［＿]?" +sp_n
                +")?";
        String ampm =
                "("
                    +"(?:"
                        +    "((?:[aAａＡ][\\.．]?[mMｍＭ][\\.．]?)|(?:午前))"
                        +"|"+"((?:[pPｐＰ][\\.．]?[mMｍＭ][\\.．]?)|(?:午後))"
                    +")" +sp_n
                +")?";
        String hhmm =
                "(?:"+
                    "("
                        +"([0-2０-２]?[0-9０-９])"
                            +sp_n+ "[:;：；]?" +sp_n
                        +"([0-5０-５][0-9０-９])"
                    +")"
                        +"|"
                    +"("
                        +"([0-2０-２]?[0-9０-９])"
                            +sp_n+ "時" +sp_n
                        +"([0-5０-５]?[0-9０-９])"
                            +sp_n+ "分"
                    +")"
                +")";

        String talkNum =
                "(?:>>([1-9][0-9]{0,8}))";

        ANCHOR_PATTERN = Pattern.compile(day + ampm + hhmm +"|"+ talkNum,
                                         Pattern.DOTALL);
    }


    private final CharSequence source;
    private final int startPos;
    private final int endPos;
    private final int day;
    private final int hour;
    private final int minute;
    private final int talkNo;


    /**
     * アンカーのコンストラクタ。
     * @param source アンカーが含まれる文字列
     * @param startPos アンカーの始まる位置
     * @param endPos アンカーの終わる位置
     * @param talkNo 公開発言番号
     */
    private Anchor(CharSequence source, int startPos, int endPos,
                    int talkNo ){
        super();

        if(talkNo <= 0) throw new IllegalArgumentException();

        this.source = source;
        this.startPos = startPos;
        this.endPos = endPos;
        this.day = -1;
        this.hour = -1;
        this.minute = -1;
        this.talkNo = talkNo;

        return;
    }

    /**
     * アンカーのコンストラクタ。
     * @param source アンカーが含まれる文字列
     * @param startPos アンカーの始まる位置
     * @param endPos アンカーの終わる位置
     * @param day 日
     * @param hour 時間(0-23)
     * @param minute 分(0-59)
     */
    private Anchor(CharSequence source, int startPos, int endPos,
                    int day, int hour, int minute                 ){
        super();

        this.source = source;
        this.startPos = startPos;
        this.endPos = endPos;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.talkNo = -1;

        return;
    }


    /**
     * 与えられた範囲指定文字列からアンカーを抽出する。
     * @param source 検索対象文字列
     * @param regionStart 範囲開始位置
     * @param regionEnd 範囲終了位置
     * @param currentDay 相対日付の基本となる日
     * @return アンカー
     */
    public static Anchor getAnchor(CharSequence source,
                                    int regionStart,
                                    int regionEnd,
                                    int currentDay      ){
        Matcher matcher = ANCHOR_PATTERN.matcher(source);
        matcher.region(regionStart, regionEnd);

        if( ! matcher.find() ) return null;

        Anchor anchor = getAnchorFromMatched(source, matcher, currentDay);

        return anchor;
    }

    /**
     * 与えられた文字列から全アンカーを抽出する。
     * @param source 検索対象文字列
     * @param currentDay 相対日付の基本となる日
     * @return アンカーのリスト（出現順）
     */
    public static List<Anchor> getAnchorList(CharSequence source,
                                               int currentDay      ){
        List<Anchor> result = new LinkedList<Anchor>();

        Matcher matcher = ANCHOR_PATTERN.matcher(source);
        int regionEnd = source.length();

        while(matcher.find()){
            Anchor anchor = getAnchorFromMatched(source, matcher, currentDay);
            result.add(anchor);
            int regionStart = matcher.end();
            matcher.region(regionStart, regionEnd);
        }

        return result;
    }

    /**
     * 文字列とそのMatcherからアンカーを抽出する。
     * @param source 検索対象文字列
     * @param matcher Matcher
     * @param currentDay 相対日付の基本となる日
     * @return アンカー
     */
    private static Anchor getAnchorFromMatched(CharSequence source,
                                                  Matcher matcher,
                                                  int currentDay){
        int startPos = matcher.start();
        int endPos   = matcher.end();

        /* G国アンカー */
        if(matcher.start(14) < matcher.end(14)){
            int talkNo = StringUtils.parseInt(source, matcher, 14);
            Anchor anchor = new Anchor(source, startPos, endPos, talkNo);
            return anchor;
        }

        int day = currentDay;
        if(matcher.start(1) < matcher.end(1)){
            if(matcher.start(2) < matcher.end(2)){ // prologue
                day = 0;
            }else if(matcher.start(3) < matcher.end(3)){ // epilogue
                day = EPILOGUEDAY;
            }else if(matcher.start(4) < matcher.end(4)){  // etc) "6d"
                day = StringUtils.parseInt(source, matcher, 4);
            }else{
                assert false;
                return null;
            }
        }

        boolean isPM = false;
        if(matcher.start(5) < matcher.end(5)){
            if(matcher.start(6) < matcher.end(6)){        // AM
                isPM = false;
            }else if(matcher.start(7) < matcher.end(7)){  // PM
                isPM = true;
            }else{
                assert false;
                return null;
            }
        }

        int hourGroup;
        int minuteGroup;
        if(matcher.start(8) < matcher.end(8)){   // hhmm hmm hh:mm
            hourGroup = 9;
            minuteGroup = 10;
        }else if(matcher.start(11) < matcher.end(11)){   // h時m分
            hourGroup = 12;
            minuteGroup = 13;
        }else{
            assert false;
            return null;
        }
        int hour   = StringUtils.parseInt(source, matcher, hourGroup);
        int minute = StringUtils.parseInt(source, matcher, minuteGroup);

        if(isPM && hour < 12) hour += 12;
        hour %= 24;
        // 午後12:34は午後00:34になる

        // TODO 3d25:30 は 3d01:30 か 4d01:30 どちらにすべきか？
        // とりあえず前者

        Anchor anchor = new Anchor(source, startPos, endPos,
                                   day, hour, minute);

        return anchor;
    }

    /**
     * アンカーの含まれる文字列を返す。
     * @return アンカーの含まれる文字列
     */
    public CharSequence getSource(){
        return this.source;
    }

    /**
     * アンカーの開始位置を返す。
     * @return アンカー開始位置
     */
    public int getStartPos(){
        return this.startPos;
    }

    /**
     * アンカーの終了位置を返す。
     * @return アンカー終了位置
     */
    public int getEndPos(){
        return this.endPos;
    }

    /**
     * アンカーの示す日付を返す。
     * @return 日付
     */
    public int getDay(){
        return this.day;
    }

    /**
     * アンカーの示す時刻を返す。
     * @return 時刻(0-23)
     */
    public int getHour(){
        return this.hour;
    }

    /**
     * アンカーの示す分を返す。
     * @return 分(0-59)
     */
    public int getMinute(){
        return this.minute;
    }

    /**
     * アンカーの示す公開発言番号を返す。
     * @return 公開発言番号。公開発言番号でない場合は0以下の値。
     */
    public int getTalkNo(){
        return this.talkNo;
    }

    /**
     * このアンカーが公開発言番号による物か判定する。
     * @return 公開発言番号由来であるならtrue
     */
    public boolean hasTalkNo(){
        return 0 < this.talkNo;
    }

    /**
     * 明示的なエピローグへのアンカーか判定する。
     * @return 明示的なエピローグへのアンカーならtrue
     */
    public boolean isEpilogueDay(){
        if(this.day >= EPILOGUEDAY) return true;
        return false;
    }

    /**
     * アンカーの文字列表記を返す。
     * 出典：まとめサイトの用語集
     * @return アンカーの文字列表記
     */
    @Override
    public String toString(){
        /* G国表記 */
        if(hasTalkNo()){
            return ">>" + this.talkNo;
        }

        StringBuilder result = new StringBuilder();

        result.append(getDay()).append('d');

        int anchorHour = getHour();
        if(anchorHour < 10) result.append('0');
        result.append(anchorHour).append(':');

        int anchorMinute = getMinute();
        if(anchorMinute < 10) result.append('0');
        result.append(anchorMinute);

        return result.toString();
    }

}
