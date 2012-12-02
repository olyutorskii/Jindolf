/*
 * Logging handler for Swing text component
 *
 * License : The MIT License
 * Copyright(c) 2011 olyutorskii
 */

package jp.sfjp.jindolf.log;

import java.awt.EventQueue;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Swingテキストコンポーネント用データモデル
 * {@link javax.swing.text.Document}
 * に出力する{@link java.util.logging.Handler}。
 * <p>スレッド間競合はEDTで解決される。
 * <p>一定の文字数を超えないよう、古い記録は消去される。
 */
public class SwingDocHandler extends Handler{

    private static final int DOCLIMIT = 100 * 1000; // 単位は文字
    private static final float CHOPRATIO = 0.9f;
    private static final int CHOPPEDLEN = (int)(DOCLIMIT * CHOPRATIO);

    static{
        assert DOCLIMIT > CHOPPEDLEN;
    }


    private final Document document;


    /**
     * ログハンドラの生成。
     * @param document ドキュメントモデル
     */
    public SwingDocHandler(Document document){
        super();

        this.document = document;

        Formatter formatter = new SimpleFormatter();
        setFormatter(formatter);

        return;
    }

    /**
     * ドキュメント末尾に文字列を追加する。
     * <p>EDTから呼ばなければならない。
     * @param logMessage 文字列
     */
    private void appendLog(String logMessage){
        try{
            this.document.insertString(this.document.getLength(),
                                       logMessage,
                                       (AttributeSet) null );
        }catch(BadLocationException e){
            assert false;
        }
        return;
    }

    /**
     * ドキュメント先頭部をチョップして最大長に納める。
     * <p>EDTから呼ばなければならない。
     */
    private void chopHead(){
        int docLength = this.document.getLength();
        if(docLength <= DOCLIMIT) return;

        int offset = docLength - CHOPPEDLEN;
        try{
            this.document.remove(0, offset);
        }catch(BadLocationException e){
            assert false;
        }

        return;
    }

    /**
     * {@inheritDoc}
     * @param record {@inheritDoc}
     */
    @Override
    public void publish(LogRecord record){
        if( ! isLoggable(record) ){
            return;
        }

        Formatter formatter = getFormatter();
        final String message = formatter.format(record);

        EventQueue.invokeLater(new Runnable(){
            @Override
            public void run(){
                appendLog(message);
                chopHead();
                return;
            }
        });

        return;
    }

    /**
     * {@inheritDoc}
     * （何もしない）。
     */
    @Override
    public void flush(){
        return;
    }

    /**
     * {@inheritDoc}
     * ログ受け入れを締め切る。
     */
    @Override
    public void close(){
        setLevel(Level.OFF);
        flush();
        return;
    }

}
