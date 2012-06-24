/*
 * JreChecker test
 *
 * Copyright 2012 olyutorskii
 */

package jp.sfjp.jindolf;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 */
public class JreCheckerTest {

    public JreCheckerTest() {
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
     * Test of hasClass method, of class JreChecker.
     */
    @Test
    public void testHasClass() {
        System.out.println("hasClass");

        assertTrue(JreChecker.hasClass("java.lang.Object"));
        assertTrue(JreChecker.hasClass(this.getClass().getName()));
        assertFalse(JreChecker.hasClass("x.x.X"));

        return;
    }

    /**
     * Test of has11Runtime method, of class JreChecker.
     */
    @Test
    public void testHas11Runtime() {
        System.out.println("has11Runtime");
        assertTrue(JreChecker.has11Runtime());
        return;
    }

    /**
     * Test of has12Runtime method, of class JreChecker.
     */
    @Test
    public void testHas12Runtime() {
        System.out.println("has12Runtime");
        assertTrue(JreChecker.has12Runtime());
        return;
    }

    /**
     * Test of has13Runtime method, of class JreChecker.
     */
    @Test
    public void testHas13Runtime() {
        System.out.println("has13Runtime");
        assertTrue(JreChecker.has13Runtime());
        return;
    }

    /**
     * Test of has14Runtime method, of class JreChecker.
     */
    @Test
    public void testHas14Runtime() {
        System.out.println("has14Runtime");
        assertTrue(JreChecker.has14Runtime());
        return;
    }

    /**
     * Test of has15Runtime method, of class JreChecker.
     */
    @Test
    public void testHas15Runtime() {
        System.out.println("has15Runtime");
        assertTrue(JreChecker.has15Runtime());
        return;
    }

    /**
     * Test of has16Runtime method, of class JreChecker.
     */
    @Test
    public void testHas16Runtime() {
        System.out.println("has16Runtime");

        boolean result = JreChecker.has16Runtime();

        return;
    }

    /**
     * Test of getLangPkgSpec method, of class JreChecker.
     */
    @Test
    public void testGetLangPkgSpec() {
        System.out.println("getLangPkgSpec");

        String result = JreChecker.getLangPkgSpec();

        return;
    }

    /**
     * Test of getJreHome method, of class JreChecker.
     */
    @Test
    public void testGetJreHome() {
        System.out.println("getJreHome");

        String result = JreChecker.getJreHome();

        return;
    }

    /**
     * Test of buildErrMessage method, of class JreChecker.
     */
    @Test
    public void testBuildErrMessage() {
        System.out.println("buildErrMessage");

        String result = JreChecker.buildErrMessage();

        return;
    }

    /**
     * Test of alignLine method, of class JreChecker.
     */
    @Test
    public void testAlignLine() {
        System.out.println("alignLine");

        String result;

        result = JreChecker.alignLine("abc", 1);
        assertEquals("a\nb\nc", result);

        result = JreChecker.alignLine("abc", 2);
        assertEquals("ab\nc", result);

        result = JreChecker.alignLine("abc", 3);
        assertEquals("abc", result);

        result = JreChecker.alignLine("abc", 4);
        assertEquals("abc", result);

        result = JreChecker.alignLine("abc", 0);
        assertEquals("\na\nb\nc", result);

        result = JreChecker.alignLine("abc", -1);
        assertEquals("\na\nb\nc", result);

        result = JreChecker.alignLine("a\nbcde", 3);
        assertEquals("a\nbcd\ne", result);

        result = JreChecker.alignLine("", 3);
        assertEquals("", result);

        try{
            JreChecker.alignLine(null, 3);
            fail();
        }catch(NullPointerException e){
            assert true;
        }

        return;
    }

    /**
     * Test of checkJre method, of class JreChecker.
     */
    @Test
    public void testCheckJre() {
        System.out.println("checkJre");

        if(false){
            JreChecker.checkJre();
        }

        return;
    }

    /**
     * Test of showErrorDialog method, of class JreChecker.
     */
    @Test
    public void testShowErrorDialog() {
        System.out.println("showErrorDialog");

        if(false){
            JreChecker.showErrorDialog("abc");
        }

        return;
    }

}