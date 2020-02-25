/*
 * StringUtils Test
 *
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class StringUtilsTest {

    public StringUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception{
    }

    @AfterClass
    public static void tearDownClass() throws Exception{
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of suppressString method, of class StringUtils.
     */
    @Test
    public void testSuppressString(){
        System.out.println("suppressString");

        CharSequence result;

        try{
            StringUtils.suppressString(null);
            fail();
        }catch(NullPointerException e){
        }

        result = StringUtils.suppressString("");
        assertEquals("", result);

        result = StringUtils.suppressString("ABCDE12345");
        assertEquals("ABCDE12345", result);

        result = StringUtils.suppressString("ABCDEF123456");
        assertEquals("ABCDE…23456", result);

        result = StringUtils.suppressString(" A\tBCDEF123　4\n5\r6");
        assertEquals("ABCDE…23456", result);

        return;
    }

    /**
     * Test of compareSubSequence method, of class StringUtils.
     */
    @Test
    public void testCompareSubSequence_6args(){
        System.out.println("compareSubSequence");

        int result;

        result = StringUtils.compareSubSequence("ABCDE",1,3,"ABCDE",1,3);
        assertTrue(result == 0);

        result = StringUtils.compareSubSequence("ABCDE",1,3,"ABXDE",1,3);
        assertTrue(result < 0);

        result = StringUtils.compareSubSequence("ABXDE",1,3,"ABCDE",1,3);
        assertTrue(result > 0);

        result = StringUtils.compareSubSequence("ABCDE",1,3,"ABCDE",2,4);
        assertTrue(result < 0);

        result = StringUtils.compareSubSequence("ABCDE",1,3,"#ABCDE",2,4);
        assertTrue(result == 0);

        result = StringUtils.compareSubSequence("ABCDE",1,3,"ABCDE",1,4);
        assertTrue(result < 0);

        result = StringUtils.compareSubSequence("ABCDE",1,4,"ABCDE",1,3);
        assertTrue(result > 0);

        return;
    }

    /**
     * Test of compareSubSequence method, of class StringUtils.
     */
    @Test
    public void testCompareSubSequence_4args(){
        System.out.println("compareSubSequence");

        int result;

        result = StringUtils.compareSubSequence("BCD","ABCDE",1,4);
        assertTrue(result == 0);

        result = StringUtils.compareSubSequence("BXD","ABCDE",1,4);
        assertTrue(result > 0);

        result = StringUtils.compareSubSequence("BCD","ABXDE",1,4);
        assertTrue(result < 0);

        return;
    }
}
