/*
 * AccountCookie Test
 *
 * Copyright(c) 2012 olyutorskii
 */
package jp.sfjp.jindolf.net;

import java.net.URI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class AccountCookieTest {

    public AccountCookieTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of chopWsp method, of class AccountCookie.
     */
    @Test
    public void testChopWsp_String() {
        System.out.println("chopWsp");

        String result;

        result = AccountCookie.chopWsp("  abc   ");
        assertEquals("abc", result);

        result = AccountCookie.chopWsp("abc");
        assertEquals("abc", result);

        result = AccountCookie.chopWsp("   abc");
        assertEquals("abc", result);

        result = AccountCookie.chopWsp("abc   ");
        assertEquals("abc", result);

        result = AccountCookie.chopWsp("a b c");
        assertEquals("a b c", result);

        result = AccountCookie.chopWsp("\t abc \t");
        assertEquals("abc", result);

        result = AccountCookie.chopWsp("");
        assertEquals("", result);

        result = AccountCookie.chopWsp("   ");
        assertEquals("", result);

        result = AccountCookie.chopWsp(null);
        assertNull(result);

        return;
    }

    /**
     * Test of splitPair method, of class AccountCookie.
     */
    @Test
    public void testSplitPair_String() {
        System.out.println("splitPair");

        String[] result;

        result = AccountCookie.splitPair("a=b");
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);

        result = AccountCookie.splitPair("=b");
        assertEquals(2, result.length);
        assertEquals("", result[0]);
        assertEquals("b", result[1]);

        result = AccountCookie.splitPair("a=");
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertEquals("", result[1]);

        result = AccountCookie.splitPair("=");
        assertEquals(2, result.length);
        assertEquals("", result[0]);
        assertEquals("", result[1]);

        result = AccountCookie.splitPair("==");
        assertEquals(2, result.length);
        assertEquals("", result[0]);
        assertEquals("=", result[1]);

        result = AccountCookie.splitPair("a===b");
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertEquals("==b", result[1]);

        result = AccountCookie.splitPair(" a = b ");
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);

        result = AccountCookie.splitPair("a");
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertNull(result[1]);

        result = AccountCookie.splitPair("");
        assertEquals(2, result.length);
        assertEquals("", result[0]);
        assertNull(result[1]);

        return;
    }

    /**
     * Test of createCookie method, of class AccountCookie.
     */
    @Test
    public void testCreateCookie_String() {
        System.out.println("createCookie");

        String cookieSource;
        AccountCookie result;

        cookieSource =
                  "login=%22XXX%22;"
                + " path=/;"
                + " expires=Sun, 09 Sep 2012 14:16:26 GMT";

        result = AccountCookie.createCookie(cookieSource);

        assertEquals("%22XXX%22", result.getLoginData());
        assertEquals(URI.create("/"), result.getPathURI());
        assertEquals(1347200186000L, result.getExpiredTime());

        assertFalse(result.hasExpired(1347200185999L));
        assertFalse(result.hasExpired(1347200186000L));
        assertTrue (result.hasExpired(1347200186001L));

        return;
    }

}
