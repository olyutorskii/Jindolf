/*
 * OutputStream associated with HttpURLConnection with counter
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * 書き込みバイト数をログ出力するHTTPコネクション由来のOutputStream。
 */
public class TallyOutputStream extends OutputStream{

    private static final int BUFSIZE = 512;

    /**
     * HTTPコネクションから出力ストリームを得る。
     * @param conn HTTPコネクション
     * @return 出力ストリーム
     * @throws java.io.IOException 入出力エラー
     */
    public static OutputStream getOutputStream(HttpURLConnection conn)
            throws IOException{
        return new TallyOutputStream(conn);
    }

    private final HttpURLConnection conn;
    private final OutputStream out;
    private long counter;
    private long nanoLap;

    /**
     * コンストラクタ。
     * @param conn HTTPコネクション
     * @throws java.io.IOException 入出力エラー
     */
    protected TallyOutputStream(HttpURLConnection conn)
            throws IOException{
        super();

        this.conn = conn;
        this.counter = 0;
        this.nanoLap = 0;

        OutputStream os;
        os = this.conn.getOutputStream();
        os = new BufferedOutputStream(os, BUFSIZE);
        this.out = os;

        return;
    }

    /**
     * 書き込みバイト数を返す。
     * @return 書き込みバイト数。
     */
    protected long getCount(){
        return this.counter;
    }

    /**
     * {@inheritDoc}
     * 今までに書き込んだバイト数のスループットをログ出力する。
     * @throws java.io.IOException {@inheritDoc}
     */
    @Override
    public void close() throws IOException{
        this.out.close();

        long size = getCount();
        long span = System.nanoTime() - this.nanoLap;

        String message = HttpUtils.formatHttpStat(this.conn, size, span);
        Jindolf.logger().info(message);

        return;
    }

    /**
     * {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    @Override
    public void flush() throws IOException{
        this.out.flush();
        return;
    }

    /**
     * {@inheritDoc}
     * @param b {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    @Override
    public void write(byte[] b) throws IOException{
        if(this.counter <= 0) this.nanoLap = System.nanoTime();

        this.out.write(b);
        this.counter += b.length;

        return;
    }

    /**
     * {@inheritDoc}
     * @param b {@inheritDoc}
     * @param off {@inheritDoc}
     * @param len {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException{
        if(this.counter <= 0) this.nanoLap = System.nanoTime();

        this.out.write(b, off, len);
        this.counter += len;

        return;
    }

    /**
     * {@inheritDoc}
     * @param b {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    public void write(int b) throws IOException{
        if(this.counter <= 0) this.nanoLap = System.nanoTime();

        this.out.write(b);
        this.counter++;

        return;
    }

}
