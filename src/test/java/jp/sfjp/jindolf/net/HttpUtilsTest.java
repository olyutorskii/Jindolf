/*
 * HttpUtils Test
 *
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 *
 */
public class HttpUtilsTest {

    public HttpUtilsTest() {
    }

    /**
     * Test of escapeHttpComment method, of class HttpUtils.
     */
    @Test
    public void testEscapeHttpComment(){
        System.out.println("escapeHttpComment");

        CharSequence comment;
        String expResult;
        String result;

        comment = "abc";
        expResult = "(abc)";
        result = HttpUtils.escapeHttpComment(comment);
        assertEquals(expResult, result);

        comment = "abc(pqr)xyz";
        expResult = "(abc\\(pqr\\)xyz)";
        result = HttpUtils.escapeHttpComment(comment);
        assertEquals(expResult, result);

        comment = "a\nb";
        expResult = "(a?b)";
        result = HttpUtils.escapeHttpComment(comment);
        assertEquals(expResult, result);

        comment = "a狼b";
        expResult = "(a?b)";
        result = HttpUtils.escapeHttpComment(comment);
        assertEquals(expResult, result);

        comment = "a🐑b";
        expResult = "(a?b)";
        result = HttpUtils.escapeHttpComment(comment);
        assertEquals(expResult, result);

        return;
    }

    /**
     * Test of throughput method, of class HttpUtils.
     */
    @Test
    public void testThroughput() {
        System.out.println("throughput");
        String result;

        result = HttpUtils.throughput(1000, 1000 * 1000 * 1000);
        assertEquals("1,000Bytes 1,000.0Bytes/sec", result);

        result = HttpUtils.throughput(1499, 1000 * 1000 * 1000);
        assertEquals("1,499Bytes 1,499.0Bytes/sec", result);

        result = HttpUtils.throughput(1500, 1000 * 1000 * 1000);
        assertEquals("1,500Bytes 1.5KBytes/sec", result);

        result = HttpUtils.throughput(1000, 2000 * 1000 * 1000);
        assertEquals("1,000Bytes 500.0Bytes/sec", result);

        result = HttpUtils.throughput(1499999, 1000 * 1000 * 1000);
        assertEquals("1,499,999Bytes 1,500.0KBytes/sec", result);

        result = HttpUtils.throughput(1500000, 1000 * 1000 * 1000);
        assertEquals("1,500,000Bytes 1.5MBytes/sec", result);

        result = HttpUtils.throughput(1, 1000 * 1000 * 1000);
        assertEquals("1Bytes 1.0Bytes/sec", result);

        result = HttpUtils.throughput(0, 1000 * 1000 * 1000);
        assertEquals("", result);

        return;
    }

    /**
     * Test of formatHttpStat method, of class HttpUtils.
     * @throws java.lang.Exception
     */
    @Test
    public void testFormatHttpStat() throws Exception{
        System.out.println("formatHttpStat");

        URL url = new URL("http://example.com");
        DummyConnection dummy = new DummyConnection(url);
        long onesec = 1000 * 1000 * 1000;
        String result;
        String expected;

        dummy.setRequestMethod("GET");
        dummy.responseCodeX = 200;
        dummy.responseMessageX = "OK";
        result = HttpUtils.formatHttpStat(dummy, 1000, onesec);
        expected = "GET http://example.com [200 OK]"
                + " 1,000Bytes 1,000.0Bytes/sec";
        assertEquals(expected, result);

        return;
    }

    /**
     * Test of getUserAgentName method, of class HttpUtils.
     */
    @Test
    public void testGetUserAgentName() {
        System.out.println("getUserAgentName");

        String result = HttpUtils.getUserAgentName();
        assertNotNull(result);

        return;
    }

    /**
     * Test of getHTMLCharset method, of class HttpUtils.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetHTMLCharset_URLConnection() throws Exception{
        System.out.println("getHTMLCharset");

        URL url = new URL("http://example.com");
        DummyConnection dummy = new DummyConnection(url);
        String result;

        result = HttpUtils.getHTMLCharset(dummy);
        assertNull(result);

        dummy.contentTypeX = "text/html;charset=Shift_JIS";
        result = HttpUtils.getHTMLCharset(dummy);
        assertEquals("Shift_JIS", result);

        return;
    }

    /**
     * Test of getHTMLCharset method, of class HttpUtils.
     */
    @Test
    public void testGetHTMLCharset_String() {
        System.out.println("getHTMLCharset");
        String contentType;

        String result;

        contentType = "text/html;charset=Shift_JIS";
        result = HttpUtils.getHTMLCharset(contentType);
        assertEquals("Shift_JIS", result);

        contentType = "text/html;charset=\";;;\"";
        result = HttpUtils.getHTMLCharset(contentType);
        assertEquals("\";;;\"", result);

        contentType = "text/html ; charset = Shift_JIS ; a = b  ; d=\"xyz\"  ";
        result = HttpUtils.getHTMLCharset(contentType);
        assertEquals("Shift_JIS", result);

        contentType = " text/html ;charset=Shift_JIS";
        result = HttpUtils.getHTMLCharset(contentType);
        assertEquals("Shift_JIS", result);

        contentType = "tex/html;charset=Shift_JIS";
        result = HttpUtils.getHTMLCharset(contentType);
        assertNull(result);

        contentType = "text/htm;charset=Shift_JIS";
        result = HttpUtils.getHTMLCharset(contentType);
        assertNull(result);

        contentType = "text / html;charset=Shift_JIS";
        result = HttpUtils.getHTMLCharset(contentType);
        assertNull(result);

        contentType = "text/html";
        result = HttpUtils.getHTMLCharset(contentType);
        assertNull(result);

        contentType = "text/html;";
        result = HttpUtils.getHTMLCharset(contentType);
        assertNull(result);

        contentType = "text/html;charse=Shift_JIS";
        result = HttpUtils.getHTMLCharset(contentType);
        assertNull(result);

        return;
    }

    private class DummyConnection extends HttpURLConnection{

        public int responseCodeX;
        public String responseMessageX;
        public String contentTypeX;

        public DummyConnection(URL u) {
            super(u);
        }

        @Override
        public int getResponseCode(){
            return this.responseCodeX;
        }

        @Override
        public String getResponseMessage(){
            return this.responseMessageX;
        }

        @Override
        public String getContentType(){
            return this.contentTypeX;
        }

        @Override
        public void connect() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void disconnect() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean usingProxy() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
