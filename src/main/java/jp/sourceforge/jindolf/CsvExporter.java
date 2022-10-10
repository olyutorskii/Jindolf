/*
 * CSV file exporter
 *
 * Copyright(c) 2009 olyutorskii
 * $Id: CsvExporter.java 953 2009-12-06 16:42:14Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import jp.sourceforge.jindolf.corelib.TalkType;

/**
 * 任意のPeriodの発言内容をCSVファイルへエクスポートする。
 * according to RFC4180 (text/csv)
 * @see <a href="http://www.ietf.org/rfc/rfc4180.txt">RFC4180</a>
 */
public final class CsvExporter{

    private static final String[] ENCNAMES = {
        "UTF-8",

        "ISO-2022-JP",
        "ISO-2022-JP-2",
        "ISO-2022-JP-3",
        "ISO-2022-JP-2004",

        "EUC-JP",
        "x-euc-jp-linux",
        "x-eucJP-Open",

        "Shift_JIS",
        "windows-31j",
        "x-MS932_0213",
        "x-SJIS_0213",
        "x-PCK",
    };
    private static final String JPCHECK =
          "[]09AZ"
        + "あんアンｱﾝゐゑヵヶヴヰヱヮ"
        + "亜瑤凜熙壷壺尭堯"
        + "峠"
        + "〒╋";
    private static final String CSVEXT = ".csv";
    private static final char CR = '\r';
    private static final char LF = '\n';
    private static final String CRLF = CR +""+ LF;
    private static final int BUFSIZ = 1024;

    private static final List<Charset> CHARSET_LIST = buildCharsetList();
    private static final FileFilter CSV_FILTER = new CsvFileFilter();
    private static final JComboBox encodeBox = new JComboBox();
    private static final JFileChooser chooser = buildChooser();
    // TODO staticなGUIパーツってどうなんだ…

    /**
     * Charsetが日本語エンコーダを持っているか確認する。
     * @param cs Charset
     * @return 日本語エンコーダを持っていればtrue
     */
    private static boolean hasJPencoder(Charset cs){
        if( ! cs.canEncode() ) return false;
        CharsetEncoder encoder = cs.newEncoder();
        try{
            if(encoder.canEncode(JPCHECK)) return true;
        }catch(Exception e){
            return false;
            // 一部JRE1.5系の「x-euc-jp-linux」エンコーディング実装には
            // canEncode()が例外を投げるバグがあるので、その対処。
        }
        return false;
    }

    /**
     * 日本語Charset一覧を生成する。
     * @return 日本語Charset一覧
     */
    private static List<Charset> buildCharsetList(){
        List<Charset> csList = new LinkedList<Charset>();
        for(String name : ENCNAMES){
            if( ! Charset.isSupported(name) ) continue;
            Charset cs = Charset.forName(name);

            if(csList.contains(cs)) continue;

            if( ! hasJPencoder(cs) ) continue;

            csList.add(cs);
        }

        Charset defcs = Charset.defaultCharset();
        if(   defcs.name().equals("windows-31j")
           && Charset.isSupported("Shift_JIS") ){
            defcs = Charset.forName("Shift_JIS");
        }

        if( hasJPencoder(defcs) || csList.size() <= 0 ){
            if(csList.contains(defcs)){
               csList.remove(defcs);
            }
            csList.add(0, defcs);
        }

        return csList;
    }

    /**
     * チューザーをビルドする。
     * @return チューザー
     */
    private static JFileChooser buildChooser(){
        JFileChooser result = new JFileChooser();

        result.setFileSelectionMode(JFileChooser.FILES_ONLY);
        result.setMultiSelectionEnabled(false);
        result.setFileHidingEnabled(true);

        result.setAcceptAllFileFilterUsed(true);

        result.setFileFilter(CSV_FILTER);

        JComponent accessory = buildAccessory();
        result.setAccessory(accessory);

        return result;
    }

    /**
     * チューザのアクセサリを生成する。
     * エンコード指定のコンボボックス。
     * @return アクセサリ
     */
    private static JComponent buildAccessory(){
        for(Charset cs : CHARSET_LIST){
            encodeBox.addItem(cs);
        }

        Border border = BorderFactory.createTitledBorder("出力エンコード");
        encodeBox.setBorder(border);

        JPanel accessory = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        accessory.setLayout(layout);

        constraints.insets = new Insets(3, 3, 3, 3);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        accessory.add(encodeBox, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        accessory.add(new JPanel(), constraints);  // dummy

        return accessory;
    }

    /**
     * ファイルに書き込めない／作れないエラー用のダイアログを表示する。
     * @param file 書き込もうとしたファイル。
     */
    private static void writeError(File file){
        Component parent = null;
        String title = "ファイル書き込みエラー";
        String message = "ファイル「" + file.toString() + "」\n"
                        +"に書き込むことができません。";

        JOptionPane.showMessageDialog(parent, message, title,
                                      JOptionPane.ERROR_MESSAGE );

        return;
    }

    /**
     * ファイル上書き確認ダイアログを表示する。
     * @param file 上書き対象ファイル
     * @return 上書きOKが指示されたらtrue
     */
    private static boolean confirmOverwrite(File file){
        Component parent = null;
        String title = "上書き確認";
        String message = "既存のファイル「" + file.toString() + "」\n"
                        +"を上書きしようとしています。続けますか？";

        int confirm = JOptionPane.showConfirmDialog(
                parent, message, title,
                JOptionPane.WARNING_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION );

        if(confirm == JOptionPane.OK_OPTION) return true;

        return false;
    }

    /**
     * チューザーのタイトルを設定する。
     * @param period エクスポート対象の日
     */
    private static void setTitle(Period period){
        Village village = period.getVillage();
        String villageName = village.getVillageName();
        String title = villageName + "村 " + period.getCaption();
        title += "の発言をCSVファイルへエクスポートします";
        chooser.setDialogTitle(title);
        return;
    }

    /**
     * エクスポート先ファイルの名前を生成する。
     * @param period エクスポート対象の日
     * @return エクスポートファイル名
     */
    private static String createUniqueFileName(Period period){
        Village village = period.getVillage();
        String villageName = village.getVillageName();

        String base = "JIN_" + villageName;

        switch(period.getType()){
        case PROLOGUE:
            base += "_Prologue";
            break;
        case EPILOGUE:
            base += "_Epilogue";
            break;
        case PROGRESS:
            base += "_Day";
            base += period.getDay();
            break;
        default:
            assert false;
            break;
        }

        File saveFile;
        String csvName;
        int serial = 1;
        do{
            csvName = base;
            if(serial > 1){
                csvName += "("+ serial +")";
            }
            serial++;
            csvName += CSVEXT;

            File current = chooser.getCurrentDirectory();
            saveFile = new File(current, csvName);
        }while(saveFile.exists());

        return csvName;
    }

    /**
     * Period情報をダンプする。
     * @param out 格納先
     * @param period ダンプ対象Period
     * @param topicFilter 発言フィルタ
     * @throws java.io.IOException 出力エラー
     */
    private static void dumpPeriod(Appendable out,
                                    Period period,
                                    TopicFilter topicFilter)
            throws IOException{
        String day = String.valueOf(period.getDay());

        List<Topic> topicList = period.getTopicList();
        for(Topic topic : topicList){
            if( ! (topic instanceof Talk) ) continue;
            Talk talk = (Talk) topic;
            if(talk.getTalkCount() <= 0) continue;

            if(topicFilter.isFiltered(talk)) continue;

            Avatar avatar = talk.getAvatar();

            String name = avatar.getName();
            int hour   = talk.getHour();
            int minute = talk.getMinute();
            TalkType type = talk.getTalkType();
            CharSequence dialog = talk.getDialog();

            out.append(name).append(',');

            out.append(day).append(',');

            out.append(Character.forDigit(hour / 10, 10));
            out.append(Character.forDigit(hour % 10, 10));
            out.append(':');
            out.append(Character.forDigit(minute / 10, 10));
            out.append(Character.forDigit(minute % 10, 10));
            out.append(',');

            switch(type){
            case PUBLIC:   out.append("say");     break;
            case PRIVATE:  out.append("think");   break;
            case WOLFONLY: out.append("whisper"); break;
            case GRAVE:    out.append("groan");   break;
            default: assert false;                break;
            }
            out.append(',');

            escapeCSV(out, dialog);
            out.append(CRLF);
        }

        return;
    }

    /**
     * ダイアログ操作に従いPeriodをエクスポートする。
     * @param period エクスポート対象のPeriod
     * @param topicFilter 発言フィルタ
     * @return エクスポートしたファイル
     */
    public static File exportPeriod(Period period, TopicFilter topicFilter){
        setTitle(period);

        String uniqName = createUniqueFileName(period);
        File uniqFile = new File(uniqName);
        chooser.setSelectedFile(uniqFile);

        int result = chooser.showSaveDialog(null);

        if(result != JFileChooser.APPROVE_OPTION) return null;

        File selected = chooser.getSelectedFile();

        if( ! hasExtent(selected.getName()) ){
            FileFilter filter = chooser.getFileFilter();
            if(filter == CSV_FILTER){
                String path = selected.getPath();
                path += CSVEXT;
                selected = new File(path);
            }
        }

        if(selected.exists()){
            if( ! selected.isFile() || ! selected.canWrite() ){
                writeError(selected);
                return null;
            }
            boolean confirmed = confirmOverwrite(selected);
            if( ! confirmed ) return null;
        }else{
            boolean created;
            try{
                created = selected.createNewFile();
            }catch(IOException e){
                writeError(selected);
                return null;
            }

            if( ! created ){
                boolean confirmed = confirmOverwrite(selected);
                if( ! confirmed ) return null;
            }
        }

        OutputStream os;
        try{
            os = new FileOutputStream(selected);
        }catch(FileNotFoundException e){
            writeError(selected);
            return null;
        }
        os = new BufferedOutputStream(os, BUFSIZ);

        Charset cs = (Charset)( encodeBox.getSelectedItem() );

        boolean hasIOError = false;
        Writer writer = new OutputStreamWriter(os, cs);
        try{
            dumpPeriod(writer, period, topicFilter);
        }catch(IOException e){
            hasIOError = true;
        }finally{
            try{
                writer.close();
            }catch(IOException e){
                hasIOError = true;
            }
        }
        if(hasIOError) writeError(selected);

        return selected;
    }

    /**
     * CSV用のエスケープシーケンス処理を行う。
     * RFC4180準拠。
     * @param app 格納先
     * @param seq エスケープシーケンス対象
     * @return appと同じもの
     * @throws java.io.IOException 出力エラー
     */
    public static Appendable escapeCSV(Appendable app, CharSequence seq)
            throws IOException{
        app.append('"');

        int length = seq.length();

        for(int pos = 0; pos < length; pos++){
            char ch = seq.charAt(pos);
            switch(ch){
            case '"':
                app.append("\"\"");
                continue;
            case '\n':
                app.append(CRLF);
                continue;
            default:
                app.append(ch);
                break;
            }
        }

        app.append('"');

        return app;
    }

    /**
     * ファイル名が任意の拡張子を持つか判定する。
     * 英字大小は同一視される。
     * 拡張子の前は必ず一文字以上何かがなければならない。
     * @param filename ファイル名
     * @param extent '.'で始まる拡張子文字列
     * @return 指定された拡張子を持つならtrue
     */
    public static boolean hasExtent(CharSequence filename,
                                     CharSequence extent ){
        int flength = filename.length();
        int elength = extent  .length();
        if(elength < 2) return false;
        if(flength <= elength) return false;

        if(filename.charAt(0) == '.') return false;

        int offset = flength - elength;
        assert offset > 0;

        for(int pos = 0; pos < elength; pos++){
            char ech = Character.toLowerCase(extent  .charAt(pos         ));
            char fch = Character.toLowerCase(filename.charAt(pos + offset));
            if(fch != ech) return false;
        }

        return true;
    }

    /**
     * パス名抜きのファイル名が拡張子を持つか判定する。
     * 先頭が.で始まるファイル名は拡張子を持たない。
     * 末尾が.で終わるファイル名は拡張子を持たない。
     * それ以外の.を含むファイル名は拡張子を持つとみなす。
     * @param filename パス名抜きのファイル名
     * @return 拡張子を持っていればtrue
     */
    public static boolean hasExtent(CharSequence filename){
        int length = filename.length();
        if(length < 3) return false;

        if(filename.charAt(0) == '.') return false;
        int lastPos = length - 1;
        if(filename.charAt(lastPos) == '.') return false;

        for(int pos = 1; pos <= lastPos - 1; pos++){
            char ch = filename.charAt(pos);
            if(ch == '.') return true;
        }

        return false;
    }

    /**
     * 隠しコンストラクタ。
     */
    private CsvExporter(){
        assert false;
        throw new AssertionError();
    }

    /**
     * CSVファイル表示用フィルタ。
     * 名前が「*.csv」の通常ファイルとディレクトリのみ表示させる。
     * ※ 表示の可否を問うものであって、選択の可否を問うものではない。
     */
    private static class CsvFileFilter extends FileFilter{

        /**
         * コンストラクタ。
         */
        public CsvFileFilter(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * @param file {@inheritDoc}
         * @return {@inheritDoc}
         */
        public boolean accept(File file){
            if(file.isDirectory()) return true;
            if( ! file.isFile() ) return false;

            if( ! hasExtent(file.getName(), CSVEXT) ) return false;

            return true;
        }

        /**
         * {@inheritDoc}
         * @return {@inheritDoc}
         */
        public String getDescription(){
            return "CSVファイル (*.csv)";
        }
    }

    // TODO SecurityExceptionの捕捉
    // 書き込み中のファイルロック
}
