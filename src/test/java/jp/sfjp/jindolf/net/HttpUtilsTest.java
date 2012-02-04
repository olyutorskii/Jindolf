/*
 * HttpUtils Test
 *
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.net;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 *
 */
public class HttpUtilsTest {

    public HttpUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getHTMLCharset method, of class HttpUtils.
     */
    @Test
    public void getHTMLCharset() {
        System.out.println("getHTMLCharset");
        String contentType;

        contentType = "text/html;charset = Shift_JIS";
        String result = HttpUtils.getHTMLCharset(contentType);
        assertEquals("Shift_JIS", result);

        contentType = "text/html ; charset=Shift_JIS ; a = b  ; d=\"xyz\"  ";
        result = HttpUtils.getHTMLCharset(contentType);
        assertEquals("Shift_JIS", result);

        return;
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

        return;
    }
}
