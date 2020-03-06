/*
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
 */
public class AuthManagerTest {

    public AuthManagerTest() {
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
     * Test of encodeForm4Post method, of class AuthManager.
     */
    @Test
    public void testEncodeForm4Post_String() {
        System.out.println("encodeForm4Post");

        String result;

        result = AuthManager.encodeForm4Post("");
        assertEquals("", result);

        result = AuthManager.encodeForm4Post("abc");
        assertEquals("abc", result);

        result = AuthManager.encodeForm4Post("a c");
        assertEquals("a+c", result);

        result = AuthManager.encodeForm4Post("a\nc");
        assertEquals("a%0Ac", result);

        result = AuthManager.encodeForm4Post("a%c");
        assertEquals("a%25c", result);

        result = AuthManager.encodeForm4Post("a=c");
        assertEquals("a%3Dc", result);

        result = AuthManager.encodeForm4Post("a?c");
        assertEquals("a%3Fc", result);

        result = AuthManager.encodeForm4Post("a&c");
        assertEquals("a%26c", result);

        result = AuthManager.encodeForm4Post("a;c");
        assertEquals("a%3Bc", result);

        result = AuthManager.encodeForm4Post("a漢c");
        assertEquals("a%E6%BC%A2c", result);

        return;
    }

    /**
     * Test of encodeForm4Post method, of class AuthManager.
     */
    @Test
    public void testEncodeForm4Post_charArr() {
        System.out.println("encodeForm4Post");

        String result;

        result = AuthManager.encodeForm4Post(new char[]{});
        assertEquals("", result);

        result = AuthManager.encodeForm4Post(new char[]{'a', 'b', 'c'});
        assertEquals("abc", result);

        result = AuthManager.encodeForm4Post(new char[]{'a', ' ', 'c'});
        assertEquals("a+c", result);

        return;
    }

    /**
     * Test of buildLoginPostData method, of class AuthManager.
     */
    @Test
    public void testBuildLoginPostData() {
        System.out.println("buildLoginPostData");

        String result;

        result = AuthManager.buildLoginPostData("abc", new char[]{'x', 'y', 'z'});
        assertEquals("cmd=login&cgi_param=%26%23bottom&user_id=abc&password=xyz", result);

        result = AuthManager.buildLoginPostData("a c", new char[]{'x', ' ', 'z'});
        assertEquals("cmd=login&cgi_param=%26%23bottom&user_id=a+c&password=x+z", result);

        return;
    }

}
