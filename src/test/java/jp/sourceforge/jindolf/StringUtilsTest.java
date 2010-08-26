/*
 * StringUtils Test
 *
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
     * Test of parseInt method, of class StringUtils.
     */
    @Test
    public void testParseInt_3args_1(){
        System.out.println("parseInt");

        int result;
        Matcher matcher;
        Pattern pattern;
        String input = "ABC123PQR456XYZ";

        pattern = Pattern.compile("([0-9]+)[A-Z]*([0-9]+)");
        matcher = pattern.matcher(input);

        assertTrue(matcher.find());

        result = StringUtils.parseInt(input, matcher, 1);
        assertEquals(123, result);

        result = StringUtils.parseInt(input, matcher, 2);
        assertEquals(456, result);

        try{
            result = StringUtils.parseInt(null, matcher, 1);
            fail();
        }catch(NullPointerException e){
        }

        try{
            result = StringUtils.parseInt(input, null, 1);
            fail();
        }catch(NullPointerException e){
        }

        return;
    }

    /**
     * Test of parseInt method, of class StringUtils.
     */
    @Test
    public void testParseInt_CharSequence(){
        System.out.println("parseInt");

        int result;

        try{
            result = StringUtils.parseInt(null);
            fail();
        }catch(NullPointerException e){
        }

        result = StringUtils.parseInt("");
        assertEquals(0, result);

        result = StringUtils.parseInt("0");
        assertEquals(0, result);

        result = StringUtils.parseInt("999");
        assertEquals(999, result);

        result = StringUtils.parseInt("X");
        assertEquals(0, result);

        result = StringUtils.parseInt("-1");
        assertEquals(0, result);

        return;
    }

    /**
     * Test of parseInt method, of class StringUtils.
     */
    @Test
    public void testParseInt_3args_2(){
        System.out.println("parseInt");

        int result;

        try{
            result = StringUtils.parseInt(null, 1, 3);
            fail();
        }catch(NullPointerException e){
        }

        result = StringUtils.parseInt("1234567", 2, 5);
        assertEquals(345, result);

        result = StringUtils.parseInt("1234567", 2, 3);
        assertEquals(3, result);

        result = StringUtils.parseInt("1234567", 2, 2);
        assertEquals(0, result);

        result = StringUtils.parseInt("1234567", 2, 1);
        assertEquals(0, result);

        result = StringUtils.parseInt("1234567", 0, 0);
        assertEquals(0, result);

        try{
            result = StringUtils.parseInt("1234567", 2, 999);
            fail();
        }catch(StringIndexOutOfBoundsException e){
        }

        try{
            result = StringUtils.parseInt("1234567", -1, 5);
            fail();
        }catch(StringIndexOutOfBoundsException e){
        }

        return;
    }

    /**
     * Test of suppressString method, of class StringUtils.
     */
    @Test
    public void testSuppressString(){
        System.out.println("suppressString");

        CharSequence result;

        try{
            result = StringUtils.suppressString(null);
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
     * Test of isTerminated method, of class StringUtils.
     */
    @Test
    public void testIsTerminated(){
        System.out.println("isTerminated");

        try{
            StringUtils.isTerminated(null, null);
            fail();
        }catch(NullPointerException e){
        }

        try{
            StringUtils.isTerminated("A", null);
            fail();
        }catch(NullPointerException e){
        }

        try{
            StringUtils.isTerminated(null, "X");
            fail();
        }catch(NullPointerException e){
        }

        assertTrue(StringUtils.isTerminated("ABCXYZ", "XYZ"));
        assertTrue(StringUtils.isTerminated("ABCXYZ", ""));
        assertTrue(StringUtils.isTerminated("", ""));

        assertFalse(StringUtils.isTerminated("ABCXYZ", "PQR"));
        assertFalse(StringUtils.isTerminated("ABC", "ABCXYZ"));
        assertFalse(StringUtils.isTerminated("", "XYZ"));

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
