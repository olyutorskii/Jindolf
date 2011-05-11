/*
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf.json;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class JsPairTest {

    public JsPairTest() {
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
     * Test of getName method, of class JsPair.
     */
    @Test
    public void testGetName(){
        System.out.println("getName");

        JsPair pair;

        pair = new JsPair("", JsNull.NULL);
        assertEquals("", pair.getName());

        pair = new JsPair("a", JsNull.NULL);
        assertEquals("a", pair.getName());

        return;
    }

    /**
     * Test of getValue method, of class JsPair.
     */
    @Test
    public void testGetValue(){
        System.out.println("getValue");

        JsPair pair;

        pair = new JsPair("x", JsNull.NULL);
        assertEquals(JsNull.NULL, pair.getValue());

        pair = new JsPair("x", "abc");
        assertEquals(new JsString("abc"), pair.getValue());

        pair = new JsPair("x", true);
        assertEquals(JsBoolean.TRUE, pair.getValue());

        pair = new JsPair("x", false);
        assertEquals(JsBoolean.FALSE, pair.getValue());

        pair = new JsPair("x", 999999999999L);
        assertEquals(new JsNumber("999999999999"), pair.getValue());

        pair = new JsPair("x", 1.25);
        assertEquals(new JsNumber("1.25"), pair.getValue());

        return;
    }

    /**
     * Test of toString method, of class JsPair.
     */
    @Test
    public void testToString(){
        System.out.println("toString");

        JsPair pair;

        pair = new JsPair("x", JsNull.NULL);
        assertEquals("\"x\":null", pair.toString());

        pair = new JsPair("", JsNull.NULL);
        assertEquals("\"\":null", pair.toString());

        return;
    }

}
