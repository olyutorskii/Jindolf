/*
 * CookieDateParser Test
 *
 * Copyright(c) 2012 olyutorskii
 */
package jp.sfjp.jindolf.net;

import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class CookieDateParserTest {

    public CookieDateParserTest() {
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
     * Test of parseToEpoch method, of class CookieDateParser.
     */
    @Test
    public void testParseToEpoch() {
        System.out.println("parseToEpoch");

        long result;

        result = CookieDateParser
                .parseToEpoch("Thu, 1 Jan 1970 00:00:00 GMT");
        assertEquals(0L, result);

        result = CookieDateParser
                .parseToEpoch("Thu, 26 Jun 2008 06:44:34 GMT");
        assertEquals(1214462674000L, result);

        result = CookieDateParser
                .parseToEpoch("Thu, 26 Jun 2008 06:44:34 JST");
        assertEquals(1214430274000L, result);

        result = CookieDateParser
                .parseToEpoch("ThuX, 26 Jun 2008 06:44:34 JST");
        assertTrue(result < 0L);

        return;
    }

    /**
     * Test of parseToDate method, of class CookieDateParser.
     */
    @Test
    public void testParseToDate() {
        System.out.println("parseToDate");

        Date result;

        result = CookieDateParser
                .parseToDate("Thu, 1 Jan 1970 00:00:00 GMT");
        assertEquals(new Date(0L), result);

        result = CookieDateParser
                .parseToDate("Thu, 26 Jun 2008 06:44:34 GMT");
        assertEquals(new Date(1214462674000L), result);

        result = CookieDateParser
                .parseToDate("ThuX, 26 Jun 2008 06:44:34 GMT");
        assertNull(result);

        return;
    }

}
