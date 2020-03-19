/*
 * dialogs in game
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import jp.sourceforge.jindolf.corelib.TalkType;

/**
 * プレイヤーの発言。
 */
public class Talk implements Topic{

    private static final String MEINICHI_LAST =
            "！\u0020今日がお前の命日だ！";

    private static final Map<TalkType, String> COLOR_MAP;

    static{
        COLOR_MAP = new EnumMap<>(TalkType.class);
        COLOR_MAP.put(TalkType.PUBLIC,   "白");
        COLOR_MAP.put(TalkType.PRIVATE,  "灰");
        COLOR_MAP.put(TalkType.WOLFONLY, "赤");
        COLOR_MAP.put(TalkType.GRAVE,    "青");
    }


    private final Period homePeriod;
    private final TalkType talkType;
    private final Avatar avatar;
    private final String messageID;
    private final int hour;
    private final int minute;
    private int charNum;
    private int talkNo;
    private CharSequence dialog;
    private int count = -1;


    /**
     * Talkの生成。
     *
     * @param homePeriod 発言元Period
     * @param talkType 発言種別
     * @param avatar Avatar
     * @param talkNo 公開発言番号。公開発言でないなら0以下の値を指定。
     * @param messageID メッセージID
     * @param hour 発言時
     * @param minute 発言分
     * @param dialog 会話データ
     */
    public Talk(Period homePeriod,
                 TalkType talkType,
                 Avatar avatar,
                 int talkNo,
                 String messageID,
                 int hour, int minute,
                 CharSequence dialog ){
        if(    homePeriod == null
            || talkType   == null
            || avatar     == null
            || messageID  == null
            || dialog     == null ) throw new NullPointerException();
        if(hour   < 0 || 23 < hour  ) throw new IllegalArgumentException();
        if(minute < 0 || 59 < minute) throw new IllegalArgumentException();
        if(talkType != TalkType.PUBLIC){
            if(0 < talkNo) throw new IllegalArgumentException();
        }

        this.homePeriod = homePeriod;
        this.talkType = talkType;
        this.avatar = avatar;
        this.talkNo = talkNo;
        this.messageID = messageID;
        this.hour = hour;
        this.minute = minute;
        this.dialog = dialog;

        this.charNum = this.dialog.length();

        return;
    }


    /**
     * 会話種別から色名への変換を行う。
     *
     * @param type 会話種別
     * @return 色名
     */
    public static String encodeColorName(TalkType type){
        Objects.requireNonNull(type);
        String result = COLOR_MAP.get(type);
        return result;
    }

    /**
     * ある文字列の末尾が別の文字列に一致するか判定する。
     *
     * @param target 判定対象
     * @param term 末尾文字
     * @return 一致すればtrue
     * @throws java.lang.NullPointerException 引数がnull
     * @see String#endsWith(String)
     */
    static boolean isTerminated(CharSequence target,
                                CharSequence term)
            throws NullPointerException{
        Objects.requireNonNull(target);
        Objects.requireNonNull(term);

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
     * 発言が交わされたPeriodを返す。
     *
     * @return Period
     */
    public Period getPeriod(){
        return this.homePeriod;
    }

    /**
     * 発言種別を得る。
     *
     * @return 種別
     */
    public TalkType getTalkType(){
        return this.talkType;
    }

    /**
     * 墓下発言か否か判定する。
     *
     * @return 墓下発言ならtrue
     */
    public boolean isGrave(){
        return this.talkType == TalkType.GRAVE;
    }

    /**
     * 各Avatarの発言種別ごとにその日(Period)の累積発言回数を返す。
     *
     * <p>システム生成の襲撃予告の場合は負の値となる。
     *
     * @return 累積発言回数。
     */
    public int getTalkCount(){
        return this.count;
    }

    /**
     * 発言文字数を返す。
     *
     * <p>改行(\n)は1文字。
     *
     * @return 文字数
     */
    public int getTotalChars(){
        return this.charNum;
    }

    /**
     * 発言元Avatarを得る。
     *
     * @return 発言元Avatar
     */
    public Avatar getAvatar(){
        return this.avatar;
    }

    /**
     * 公開発言番号を取得する。
     *
     * <p>公開発言番号が割り振られてなければ0以下の値を返す。
     *
     * @return 公開発言番号
     */
    public int getTalkNo(){
        return this.talkNo;
    }

    /**
     * 公開発言番号を設定する。
     *
     * <p>0以下の値は公開発言番号を持たないと判断される。
     *
     * @param talkNo 公開発言番号
     */
    public void setTalkNo(int talkNo){
        this.talkNo = talkNo;
        return;
    }

    /**
     * 公開発言番号の有無を返す。
     *
     * @return 公開発言番号が割り当てられているならtrueを返す。
     */
    public boolean hasTalkNo(){
        if(0 < this.talkNo) return true;
        return false;
    }

    /**
     * メッセージIDを取得する。
     *
     * @return メッセージID
     */
    public String getMessageID(){
        return this.messageID;
    }

    /**
     * メッセージIDからエポック秒(ms)に変換する。
     *
     * @return GMT 1970-01-01 00:00:00 からのエポック秒(ms)
     */
    public long getTimeFromID(){
        String epoch = this.messageID.replace("mes", "");
        long result = Long.parseLong(epoch) * 1000;
        return result;
    }

    /**
     * 発言時を取得する。
     *
     * @return 発言時
     */
    public int getHour(){
        return this.hour;
    }

    /**
     * 発言分を取得する。
     *
     * @return 発言分
     */
    public int getMinute(){
        return this.minute;
    }

    /**
     * 会話データを取得する。
     *
     * @return 会話データ
     */
    public CharSequence getDialog(){
        return this.dialog;
    }

    /**
     * 会話データを設定する。
     *
     * @param seq 会話データ
     */
    public void setDialog(CharSequence seq){
        this.dialog = seq;
        this.charNum = this.dialog.length();
        return;
    }

    /**
     * 発言種別ごとの発言回数を設定する。
     *
     * <p>システム生成の襲撃予告では負の値を入れれば良い。
     *
     * @param count 発言回数
     */
    public void setCount(int count){
        this.count = count;
        return;
    }

    /**
     * この会話を識別するためのアンカー文字列を生成する。
     *
     * <p>例えば「3d09:56」など。
     *
     * @return アンカー文字列
     */
    public String getAnchorNotation(){
        int day = this.homePeriod.getDay();

        String hstr = "0"+this.hour;
        hstr = hstr.substring(hstr.length() - 2);
        String mstr = "0"+this.minute;
        mstr = mstr.substring(mstr.length() - 2);

        return day + "d" + hstr + ":" + mstr;
    }

    /**
     * この会話を識別するためのG国用アンカー文字列を発言番号から生成する。
     *
     * <p>例えば「{@literal >>172}」など。
     *
     * @return アンカー文字列。発言番号がなければ空文字列。
     */
    public String getAnchorNotation_G(){
        if( ! hasTalkNo() ) return "";
        return ">>" + this.talkNo;
    }

    /**
     * 会話テキスト本文が襲撃予告たりうるか判定する。
     *
     * <p>Period開始時の襲撃予告の文面はシステムが生成する文書であり、
     * 狼プレイヤーの投稿に由来しない。
     *
     * <p>「！ 今日がお前の命日だ！」で終わる赤ログは
     * 襲撃予告の可能性がある。
     *
     * <p>
     * {@link jp.sourceforge.jindolf.corelib.SysEventType#MURDERED}
     * もしくは
     * {@link jp.sourceforge.jindolf.corelib.SysEventType#NOMURDER}
     * の前に該当する赤ログが出現すれば、それは襲撃予告と断定して良い。
     *
     * @return 襲撃予告のテキストの可能性があるならtrue
     */
    public boolean isMurderNotice(){
        boolean isWolf;
        isWolf = this.talkType == TalkType.WOLFONLY;
        if( ! isWolf) return false;

        boolean meinichida;
        meinichida = isTerminated(getDialog(), MEINICHI_LAST);
        if( ! meinichida) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>会話のString表現を返す。
     * 実体参照やHTMLタグも含まれる。
     *
     * @return 会話のString表現
     */
    @Override
    public String toString(){
        String fullName = this.avatar.getFullName();

        String verb;
        switch (this.talkType) {
        case PUBLIC:
            verb=" says ";
            break;
        case PRIVATE:
            verb=" think ";
            break;
        case WOLFONLY:
            verb=" howl ";
            break;
        case GRAVE:
            verb=" groan ";
            break;
        default:
            assert false;
            return null;
        }

        StringBuilder result = new StringBuilder();
        result.append(fullName).append(verb).append(this.dialog);

        return result.toString();
    }

}
