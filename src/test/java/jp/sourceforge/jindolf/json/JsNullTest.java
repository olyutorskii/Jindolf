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
public class JsNullTest {

    public JsNullTest() {
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
     * Test of etc of class JsNull.
     */
    @Test
    public void testEtc(){
        System.out.println("etc");
        assertNotNull(JsNull.NULL);
        assertTrue(JsNull.NULL instanceof JsNull);
        return;
    }

    /**
     * Test of hasChanged method, of class JsNull.
     */
    @Test
    public void testHasChanged(){
        System.out.println("hasChanged");
        assertFalse(JsNull.NULL.hasChanged());
        JsNull.NULL.setUnchanged();
        assertFalse(JsNull.NULL.hasChanged());
        return;
    }

    /**
     * Test of setUnchanged method, of class JsNull.
     */
    @Test
    public void testSetUnchanged(){
        System.out.println("setUnchanged");
        JsNull.NULL.setUnchanged();
        assertFalse(JsNull.NULL.hasChanged());
        return;
    }

    /**
     * Test of traverse method, of class JsNull.
     */
    @Test
    public void testTraverse(){
        System.out.println("traverse");
        try{
            JsNull.NULL.traverse(new ValueVisitor(){
                int ct = 0;

                public void visitValue(JsValue value)
                        throws JsVisitException{
                    assertEquals(JsNull.NULL, value);
                    assertTrue(this.ct++ <= 0);
                }

                public void visitPairName(String name)
                        throws JsVisitException{
                    throw new JsVisitException();
                }

                public void visitCollectionClose(JsValue composite)
                        throws JsVisitException{
                    throw new JsVisitException();
                }
            });
        }catch(JsVisitException e){
            fail();
        }
        return;
    }

    /**
     * Test of compareTo method, of class JsNull.
     */
    @Test
    public void testCompareTo(){
        System.out.println("compareTo");
        assertEquals(0, JsNull.NULL.compareTo(JsNull.NULL));
        try{
            JsNull.NULL.compareTo(null);
            fail();
        }catch(NullPointerException e){
            // NOTHING
        }
        return;
    }

    /**
     * Test of toString method, of class JsNull.
     */
    @Test
    public void testToString(){
        System.out.println("toString");
        assertEquals("null", JsNull.NULL.toString());
        return;
    }

}
