/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jp.sourceforge.jindolf.json;

import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class JsBooleanTest {

    public JsBooleanTest() {
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
     * Test of traverse method, of class JsBoolean.
     */
    @Test
    public void testTraverse(){
        System.out.println("traverse");

        try{
            JsBoolean.TRUE.traverse(new ValueVisitor(){
                int ct = 0;

                public void visitValue(JsValue value)
                        throws JsVisitException{
                    assertEquals(JsBoolean.TRUE, value);
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

        try{
            JsBoolean.FALSE.traverse(new ValueVisitor(){
                int ct = 0;

                public void visitValue(JsValue value)
                        throws JsVisitException{
                    assertEquals(JsBoolean.FALSE, value);
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
     * Test of hasChanged method, of class JsBoolean.
     */
    @Test
    public void testHasChanged(){
        System.out.println("hasChanged");

        assertFalse(JsBoolean.TRUE.hasChanged());
        JsBoolean.TRUE.setUnchanged();
        assertFalse(JsBoolean.TRUE.hasChanged());

        assertFalse(JsBoolean.FALSE.hasChanged());
        JsBoolean.FALSE.setUnchanged();
        assertFalse(JsBoolean.FALSE.hasChanged());

        return;
    }

    /**
     * Test of setUnchanged method, of class JsBoolean.
     */
    @Test
    public void testSetUnchanged(){
        System.out.println("setUnchanged");

        JsBoolean.TRUE.setUnchanged();
        assertFalse(JsBoolean.TRUE.hasChanged());

        JsBoolean.FALSE.setUnchanged();
        assertFalse(JsBoolean.FALSE.hasChanged());

        return;
    }

    /**
     * Test of valueOf method, of class JsBoolean.
     */
    @Test
    public void testValueOf(){
        System.out.println("valueOf");
        assertEquals(JsBoolean.TRUE, JsBoolean.valueOf(true));
        assertEquals(JsBoolean.FALSE, JsBoolean.valueOf(false));
        return;
    }

    /**
     * Test of booleanValue method, of class JsBoolean.
     */
    @Test
    public void testBooleanValue(){
        System.out.println("booleanValue");
        assertTrue(JsBoolean.TRUE.booleanValue());
        assertFalse(JsBoolean.FALSE.booleanValue());
        return;
    }

    /**
     * Test of isTrue method, of class JsBoolean.
     */
    @Test
    public void testIsTrue(){
        System.out.println("isTrue");
        assertTrue(JsBoolean.TRUE.isTrue());
        assertFalse(JsBoolean.FALSE.isTrue());
        return;
    }

    /**
     * Test of isFalse method, of class JsBoolean.
     */
    @Test
    public void testIsFalse(){
        System.out.println("isFalse");
        assertFalse(JsBoolean.TRUE.isFalse());
        assertTrue(JsBoolean.FALSE.isFalse());
        return;
    }

    /**
     * Test of hashCode method, of class JsBoolean.
     */
    @Test
    public void testHashCode(){
        System.out.println("hashCode");
        // NOTHING
        return;
    }

    /**
     * Test of equals method, of class JsBoolean.
     */
    @Test
    public void testEquals(){
        System.out.println("equals");

        assertTrue(JsBoolean.TRUE.equals(JsBoolean.TRUE));
        assertFalse(JsBoolean.TRUE.equals(JsBoolean.FALSE));
//        assertFalse(JsBoolean.TRUE.equals(JsNull.NULL));
        assertFalse(JsBoolean.TRUE.equals(null));

        assertFalse(JsBoolean.FALSE.equals(JsBoolean.TRUE));
        assertTrue(JsBoolean.FALSE.equals(JsBoolean.FALSE));
//        assertFalse(JsBoolean.TRUE.equals(JsNull.NULL));
        assertFalse(JsBoolean.FALSE.equals(null));

        return;
    }

    /**
     * Test of compareTo method, of class JsBoolean.
     */
    @Test
    public void testCompareTo(){
        System.out.println("compareTo");
        assertEquals(0, JsBoolean.TRUE.compareTo(JsBoolean.TRUE));
        assertEquals(0, JsBoolean.FALSE.compareTo(JsBoolean.FALSE));
        assertTrue(0 > JsBoolean.TRUE.compareTo(JsBoolean.FALSE));
        assertTrue(0 < JsBoolean.FALSE.compareTo(JsBoolean.TRUE));

        try{
            JsBoolean.TRUE.compareTo(null);
            fail();
        }catch(NullPointerException e){
            // NOTHING
        }

        try{
            JsBoolean.FALSE.compareTo(null);
            fail();
        }catch(NullPointerException e){
            // NOTHING
        }

        SortedSet<JsBoolean> set = new TreeSet<JsBoolean>();

        set.clear();
        set.add(JsBoolean.TRUE);
        set.add(JsBoolean.FALSE);
        assertEquals(JsBoolean.TRUE, set.first());
        assertEquals(JsBoolean.FALSE, set.last());
        set.clear();
        set.add(JsBoolean.FALSE);
        set.add(JsBoolean.TRUE);
        assertEquals(JsBoolean.TRUE, set.first());
        assertEquals(JsBoolean.FALSE, set.last());

        return;
    }

    /**
     * Test of toString method, of class JsBoolean.
     */
    @Test
    public void testToString(){
        System.out.println("toString");
        assertEquals("true", JsBoolean.TRUE.toString());
        assertEquals("false", JsBoolean.FALSE.toString());
        return;
    }

    /**
     * Test of toString method, of class JsBoolean.
     */
    @Test
    public void testEtc(){
        System.out.println("etc.");
        assertNotNull(JsBoolean.TRUE);
        assertNotNull(JsBoolean.FALSE);
        assertTrue(JsBoolean.TRUE instanceof JsBoolean);
        assertTrue(JsBoolean.FALSE instanceof JsBoolean);
        return;
    }

}
