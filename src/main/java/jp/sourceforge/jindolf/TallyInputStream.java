/*
 * InputStream associated with HttpURLConnection with counter
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * 読み込みバイト数を記録するHTTPコネクション由来のInputStream。
 * バッファリングも行う。
 */
public class TallyInputStream extends InputStream{

    private static final int BUFSIZE = 2 * 1024;

    /**
     * HTTPコネクションから入力ストリームを得る。
     * @param conn HTTPコネクション
     * @return 入力ストリーム
     * @throws java.io.IOException 入出力エラー
     */
    public static InputStream getInputStream(HttpURLConnection conn)
            throws IOException{
        return new TallyInputStream(conn);
    }

    private final HttpURLConnection conn;
    private final InputStream in;
    private long counter;
    private long nanoLap;
    private boolean hasClosed;

    /**
     * コンストラクタ。
     * @param conn HTTPコネクション
     * @throws java.io.IOException 入出力エラー
     */
    protected TallyInputStream(HttpURLConnection conn) throws IOException{
        super();

        this.conn = conn;
        this.counter = 0;
        this.nanoLap = 0;

        InputStream is;
        is = this.conn.getInputStream();
        is = new BufferedInputStream(is, BUFSIZE);
        this.in = is;

        this.hasClosed = false;

        return;
    }

    /**
     * 読み込みバイト数を返す。
     * @return 読み込みバイト数。
     */
    protected long getCount(){
        return this.counter;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    @Override
    public int available() throws IOException{
        int bytes = this.in.available();
        return bytes;
    }

    /**
     * {@inheritDoc}
     * 今までに読み込んだバイト数のスループットをログ出力する。
     * @throws java.io.IOException {@inheritDoc}
     */
    @Override
    public void close() throws IOException{
        if(this.hasClosed) return;

        this.in.close();

        long size = getCount();
        long span = System.nanoTime() - this.nanoLap;

        String message = HttpUtils.formatHttpStat(this.conn, size, span);
        Jindolf.logger().info(message);

        this.hasClosed = true;

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    public int read() throws IOException{
        if(this.counter <= 0) this.nanoLap = System.nanoTime();

        int byteData = this.in.read();
        if(byteData >= 0) this.counter++;

        return byteData;
    }

    /**
     * {@inheritDoc}
     *
     * @param buf {@inheritDoc}
     * @return {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    @Override
    public int read(byte[] buf) throws IOException{
        if(this.counter <= 0) this.nanoLap = System.nanoTime();

        int count = this.in.read(buf);
        if(count >= 0) this.counter += count;

        return count;
    }

    /**
     * {@inheritDoc}
     *
     * @param buf {@inheritDoc}
     * @param off {@inheritDoc}
     * @param len {@inheritDoc}
     * @return {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    @Override
    public int read(byte[] buf, int off, int len) throws IOException{
        if(this.counter <= 0) this.nanoLap = System.nanoTime();

        int count = this.in.read(buf, off, len);
        if(count >= 0) this.counter += count;

        return count;
    }

    /**
     * {@inheritDoc}
     *
     * @param n {@inheritDoc}
     * @return {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    @Override
    public long skip(long n) throws IOException{
        if(this.counter <= 0) this.nanoLap = System.nanoTime();

        long skipped = this.in.skip(n);
        this.counter += skipped;

        return skipped;
    }

}
