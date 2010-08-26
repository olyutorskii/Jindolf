/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jp.sourceforge.jindolf.json;

import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class JsArrayTest {

    public JsArrayTest() {
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
     * Test of parseArray method, of class JsArray.
     */
    @Test
    public void testParseArray() throws Exception{
        System.out.println("parseArray");

        JsonReader reader;
        JsArray array;

        reader = new JsonReader(new StringReader("[]"));
        array = JsArray.parseArray(reader);
        assertEquals(0, array.size());

        reader = new JsonReader(new StringReader("[true]"));
        array = JsArray.parseArray(reader);
        assertEquals(1, array.size());
        assertEquals(JsBoolean.TRUE, array.get(0));

        reader = new JsonReader(new StringReader("[true,false]"));
        array = JsArray.parseArray(reader);
        assertEquals(2, array.size());
        assertEquals(JsBoolean.TRUE, array.get(0));
        assertEquals(JsBoolean.FALSE, array.get(1));

        reader = new JsonReader(new StringReader("\n[\rtrue\t, false\n]\r"));
        array = JsArray.parseArray(reader);
        assertEquals(2, array.size());
        assertEquals(JsBoolean.TRUE, array.get(0));
        assertEquals(JsBoolean.FALSE, array.get(1));

        try{
            reader = new JsonReader(new StringReader("[,]"));
            array = JsArray.parseArray(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        try{
            reader = new JsonReader(new StringReader("[true,]"));
            array = JsArray.parseArray(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        try{
            reader = new JsonReader(new StringReader("[true"));
            array = JsArray.parseArray(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        try{
            reader = new JsonReader(new StringReader("true]"));
            array = JsArray.parseArray(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        return;
    }

    /**
     * Test of add method, of class JsArray.
     */
    @Test
    public void testAdd(){
        System.out.println("add");

        JsArray array = new JsArray();

        JsNumber number = new JsNumber("1.23");
        assertEquals(0, array.size());
        array.add(number);
        assertEquals(1, array.size());
        array.add(number);
        assertEquals(2, array.size());

        return;
    }

    /**
     * Test of get method, of class JsArray.
     */
    @Test
    public void testGet(){
        System.out.println("get");

        JsArray array = new JsArray();

        JsValue val1 = new JsNumber("1.23");
        JsValue val2 = new JsString("abc");

        array.add(val1);
        array.add(val2);

        assertEquals(val1, array.get(0));
        assertEquals(val2, array.get(1));

        try{
            array.get(2);
            fail();
        }catch(IndexOutOfBoundsException e){
            // NOTHING
        }

        return;
    }

    /**
     * Test of clear method, of class JsArray.
     */
    @Test
    public void testClear(){
        System.out.println("clear");

        JsArray array = new JsArray();

        JsValue val1 = new JsNumber("1.23");
        JsValue val2 = new JsString("abc");

        array.add(val1);
        array.add(val2);
        assertEquals(2, array.size());

        array.clear();
        assertEquals(0, array.size());

        try{
            array.get(0);
            fail();
        }catch(IndexOutOfBoundsException e){
            // NOTHING
        }

        return;
    }

    /**
     * Test of remove method, of class JsArray.
     */
    @Test
    public void testRemove(){
        System.out.println("remove");

        JsArray array = new JsArray();

        JsValue val1 = new JsNumber("1.23");
        JsValue val2 = new JsString("abc");
        JsValue val3 = JsBoolean.TRUE;

        array.add(val1);
        array.add(val2);
        assertEquals(2, array.size());

        assertTrue(array.remove(val1));
        assertEquals(1, array.size());
        assertEquals(val2, array.get(0));

        assertFalse(array.remove(val3));
        assertEquals(1, array.size());

        return;
    }

    /**
     * Test of size method, of class JsArray.
     */
    @Test
    public void testSize(){
        System.out.println("size");

        JsArray array = new JsArray();
        assertEquals(0, array.size());

        JsValue val1 = new JsNumber("1.23");

        array.add(val1);
        assertEquals(1, array.size());

        return;
    }

    /**
     * Test of iterator method, of class JsArray.
     */
    @Test
    public void testIterator(){
        System.out.println("iterator");

        JsArray array = new JsArray();

        JsValue val1 = new JsNumber("1.23");
        JsValue val2 = new JsString("abc");

        array.add(val1);
        array.add(val2);

        Iterator<JsValue> it = array.iterator();

        assertTrue(it.hasNext());
        assertEquals(val1, it.next());

        assertTrue(it.hasNext());
        assertEquals(val2, it.next());

        assertFalse(it.hasNext());

        return;
    }

    /**
     * Test of hashCode method, of class JsArray.
     */
    @Test
    public void testHashCode(){
        System.out.println("hashCode");

        JsArray array1 = new JsArray();
        JsArray array2 = new JsArray();

        assertEquals(array1.hashCode(), array2.hashCode());

        array1.add(new JsString("abc"));
        array2.add(new JsString("abc"));

        assertEquals(array1.hashCode(), array2.hashCode());

        return;
    }

    /**
     * Test of equals method, of class JsArray.
     */
    @Test
    public void testEquals(){
        System.out.println("equals");

        JsArray array1 = new JsArray();
        JsArray array2 = new JsArray();

        assertTrue(array1.equals(array2));

        array1.add(new JsString("abc"));
        array2.add(new JsString("abc"));

        assertTrue(array1.equals(array2));

        array1.add(new JsString("xyz"));
        array2.add(new JsString("XYZ"));

        assertFalse(array1.equals(array2));

        assertFalse(array1.equals(null));

        return;
    }

    /**
     * Test of toString method, of class JsArray.
     */
    @Test
    public void testToString(){
        System.out.println("toString");

        JsArray array = new JsArray();

        assertEquals("[]", array.toString());

        array.add(JsBoolean.TRUE);
        assertEquals("[true]", array.toString());

        array.add(JsBoolean.FALSE);
        assertEquals("[true,false]", array.toString());

        array.add(new JsArray());
        assertEquals("[true,false,[]]", array.toString());

        return;
    }

    /**
     * Test of traverse method, of class JsArray.
     */
    @Test
    public void testTraverse() throws Exception{
        System.out.println("traverse");

        JsArray array = new JsArray();
        JsValue val1 = new JsNumber("12");
        JsValue val2 = new JsString("AB");
        array.add(val1);
        array.add(val2);

        final List<Object> visited = new LinkedList<Object>();

        try{
            array.traverse(new ValueVisitor(){
                public void visitValue(JsValue value)
                        throws JsVisitException{
                    visited.add(value);
                    return;
                }

                public void visitPairName(String name)
                        throws JsVisitException{
                    visited.add(name);
                    return;
                }

                public void visitCollectionClose(JsValue composite)
                        throws JsVisitException{
                    visited.add(composite);
                    return;
                }
            });
        }catch(JsVisitException e){
            fail();
        }

        assertEquals(4, visited.size());
        assertEquals(array, visited.get(0));
        assertEquals(val1, visited.get(1));
        assertEquals(val2, visited.get(2));
        assertEquals(array, visited.get(3));

        return;
    }

    /**
     * Test of hasChanged method, of class JsArray.
     */
    @Test
    public void testHasChanged(){
        System.out.println("hasChanged");

        JsArray array = new JsArray();
        assertFalse(array.hasChanged());

        array.add(new JsNumber("0"));
        assertTrue(array.hasChanged());

        array.setUnchanged();
        assertFalse(array.hasChanged());

        JsArray child = new JsArray();
        array.add(child);
        array.setUnchanged();
        assertFalse(array.hasChanged());

        child.add(JsNull.NULL);
        assertTrue(array.hasChanged());
        array.setUnchanged();
        assertFalse(array.hasChanged());

        return;
    }

    /**
     * Test of setUnchanged method, of class JsArray.
     */
    @Test
    public void testSetUnchanged(){
        System.out.println("setUnchanged");

        JsArray array = new JsArray();
        JsArray child = new JsArray();
        array.add(child);

        child.add(JsNull.NULL);
        assertTrue(child.hasChanged());

        array.setUnchanged();
        assertFalse(child.hasChanged());

        return;
    }

}
