/*
 * parser for Cookie-Date
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.net;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 人狼BBS用Cookie日付のパースを行う。
 * <p>デフォルトのロケールやタイムゾーンに依存しないよう設計される。
 * <p>人狼BBSのCookie期限表記はRFC2616で"rfc-1123-date"として定義される。
 * <p>例： 「Thu, 26 Jun 2008 06:44:34 GMT」
 * @see <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a>
 */
final class CookieDateParser{

    private static final String DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    private static final Locale LOCALE_ROOT = new Locale("__", "", "");
    private static final SimpleDateFormat PARSER;

    static{
        TimeZone zoneGMT = TimeZone.getTimeZone("GMT");
        Calendar gcal = new GregorianCalendar(zoneGMT, LOCALE_ROOT);
        DateFormatSymbols customSyms = buildSymbols();

        PARSER = new SimpleDateFormat(DATE_PATTERN, LOCALE_ROOT);
        PARSER.setTimeZone(zoneGMT);
        PARSER.setCalendar(gcal);
        PARSER.setDateFormatSymbols(customSyms);
        PARSER.setLenient(true);
    }


    /**
     * 隠しコンストラクタ。
     */
    private CookieDateParser(){
        assert false;
    }


    /**
     * 日付表記要素を定義する。
     * @return 日付表記要素
     */
    private static DateFormatSymbols buildSymbols(){
        DateFormatSymbols customSyms = new DateFormatSymbols(LOCALE_ROOT);

        String[] sweekdays = customSyms.getShortWeekdays();
        String[] smonths   = customSyms.getShortMonths();

        sweekdays[Calendar.SUNDAY   ] = "Sun";
        sweekdays[Calendar.MONDAY   ] = "Mon";
        sweekdays[Calendar.TUESDAY  ] = "Tue";
        sweekdays[Calendar.WEDNESDAY] = "Wed";
        sweekdays[Calendar.THURSDAY ] = "Thu";
        sweekdays[Calendar.FRIDAY   ] = "Fri";
        sweekdays[Calendar.SATURDAY ] = "Sat";

        smonths[Calendar.JANUARY  ] = "Jan";
        smonths[Calendar.FEBRUARY ] = "Feb";
        smonths[Calendar.MARCH    ] = "Mar";
        smonths[Calendar.APRIL    ] = "Apr";
        smonths[Calendar.MAY      ] = "May";
        smonths[Calendar.JUNE     ] = "Jun";
        smonths[Calendar.JULY     ] = "Jul";
        smonths[Calendar.AUGUST   ] = "Aug";
        smonths[Calendar.SEPTEMBER] = "Sep";
        smonths[Calendar.OCTOBER  ] = "Oct";
        smonths[Calendar.NOVEMBER ] = "Nov";
        smonths[Calendar.DECEMBER ] = "Dec";

        customSyms.setShortWeekdays(sweekdays);
        customSyms.setShortMonths(smonths);

        return customSyms;
    }

    /**
     * 日付文字列をパースする。
     * @param txt 文字列
     * @return エポック時刻(msec)。不正な文字列の場合は負の数を返す。
     */
    public static long parseToEpoch(String txt){
        Date date = parseToDate(txt);
        if(date == null) return -1L;

        long result = date.getTime();

        return result;
    }

    /**
     * 日付文字列をパースする。
     * @param txt 文字列
     * @return 日付。不正な文字列の場合はnullを返す。
     */
    public static Date parseToDate(String txt){
        Date result;

        try{
            synchronized(PARSER){
                result = PARSER.parse(txt);
            }
        }catch(ParseException e){
            return null;
        }

        return result;
    }

}
