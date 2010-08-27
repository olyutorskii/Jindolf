/*
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf.json;

import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class JsObjectTest {

    public JsObjectTest() {
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
     * Test of parseObject method, of class JsObject.
     */
    @Test
    public void testParseObject() throws Exception{
        System.out.println("parseObject");

        JsonReader reader;
        JsObject object;

        reader = new JsonReader(new StringReader("{}"));
        object = JsObject.parseObject(reader);
        assertEquals(0, object.size());

        reader = new JsonReader(new StringReader("{\"A\":true}"));
        object = JsObject.parseObject(reader);
        assertEquals(1, object.size());
        assertEquals(JsBoolean.TRUE, object.getPair("A").getValue());

        reader = new JsonReader(new StringReader("{\"A\":true,\"B\":false}"));
        object = JsObject.parseObject(reader);
        assertEquals(2, object.size());
        assertEquals(JsBoolean.TRUE, object.getPair("A").getValue());
        assertEquals(JsBoolean.FALSE, object.getPair("B").getValue());

        reader = new JsonReader(new StringReader("\n{\r\"A\"\t: true,\"B\":false\n}\r"));
        object = JsObject.parseObject(reader);
        assertEquals(2, object.size());
        assertEquals(JsBoolean.TRUE, object.getPair("A").getValue());
        assertEquals(JsBoolean.FALSE, object.getPair("B").getValue());

        try{
            reader = new JsonReader(new StringReader("{,}"));
            object = JsObject.parseObject(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        try{
            reader = new JsonReader(new StringReader("{true,}"));
            object = JsObject.parseObject(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        try{
            reader = new JsonReader(new StringReader("{true"));
            object = JsObject.parseObject(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        try{
            reader = new JsonReader(new StringReader("true}"));
            object = JsObject.parseObject(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        return;
    }

    /**
     * Test of putValue method, of class JsObject.
     */
    @Test
    public void testPutGetValue(){
        System.out.println("putValue");

        JsObject object = new JsObject();
        assertEquals(0, object.size());

        object.putValue("x", JsNull.NULL);
        assertEquals(1, object.size());
        assertEquals(JsNull.NULL, object.getValue("x"));
        assertEquals(null, object.getValue("y"));

        object.putValue("y", JsBoolean.TRUE);
        assertEquals(2, object.size());
        assertEquals(JsBoolean.TRUE, object.getValue("y"));

        object.putValue("x", JsBoolean.FALSE);
        assertEquals(2, object.size());
        assertEquals(JsBoolean.FALSE, object.getValue("x"));

        return;
    }

    /**
     * Test of putPair method, of class JsObject.
     */
    @Test
    public void testPutGetPair(){
        System.out.println("putPair");

        JsObject object = new JsObject();
        assertEquals(0, object.size());

        JsPair pair = new JsPair("x", JsNull.NULL);
        object.putPair(pair);
        assertEquals(1, object.size());
        assertEquals(JsNull.NULL, object.getValue("x"));

        JsPair pair2 = object.getPair("x");
        assertNotNull(pair2);
        assertEquals("x", pair2.getName());
        assertEquals(JsNull.NULL, pair2.getValue());

        assertNull(object.getPair("y"));

        return;
    }

    /**
     * Test of clear method, of class JsObject.
     */
    @Test
    public void testClear(){
        System.out.println("clear");

        JsObject object = new JsObject();
        assertEquals(0, object.size());

        object.putValue("x", JsNull.NULL);
        assertEquals(1, object.size());

        object.clear();
        assertEquals(0, object.size());

        return;
    }

    /**
     * Test of remove method, of class JsObject.
     */
    @Test
    public void testRemove(){
        System.out.println("remove");

        JsObject object = new JsObject();

        object.putValue("x", JsNull.NULL);
        assertEquals(1, object.size());

        assertNotNull(object.getValue("x"));

        assertEquals(JsNull.NULL, object.remove("x"));
        assertEquals(0, object.size());
        assertNull(object.getValue("x"));

        assertNull(object.remove("y"));

        return;
    }

    /**
     * Test of nameSet method, of class JsObject.
     */
    @Test
    public void testNameSet(){
        System.out.println("nameSet");

        JsObject object = new JsObject();
        Set<String> set;

        set = object.nameSet();
        assertEquals(0, set.size());

        object.putValue("y", JsNull.NULL);
        object.putValue("z", JsNull.NULL);
        object.putValue("x", JsNull.NULL);

        set = object.nameSet();
        assertEquals(3, set.size());
        Object[] names = set.toArray();

        assertEquals("x", names[0]);
        assertEquals("y", names[1]);
        assertEquals("z", names[2]);

        return;
    }

    /**
     * Test of getPairList method, of class JsObject.
     */
    @Test
    public void testGetPairList(){
        System.out.println("getPairList");

        JsObject object = new JsObject();
        List<JsPair> list;

        list = object.getPairList();
        assertEquals(0, list.size());

        object.putValue("y", JsNull.NULL);
        object.putValue("z", JsBoolean.TRUE);
        object.putValue("x", JsBoolean.FALSE);

        list = object.getPairList();
        assertEquals(3, list.size());

        assertEquals("x", list.get(0).getName());
        assertEquals("y", list.get(1).getName());
        assertEquals("z", list.get(2).getName());
        assertEquals(JsBoolean.FALSE, list.get(0).getValue());
        assertEquals(JsNull.NULL, list.get(1).getValue());
        assertEquals(JsBoolean.TRUE, list.get(2).getValue());

        return;
    }

    /**
     * Test of iterator method, of class JsObject.
     */
    @Test
    public void testIterator(){
        System.out.println("iterator");

        JsObject object = new JsObject();
        object.putValue("y", JsBoolean.FALSE);
        object.putValue("x", JsBoolean.TRUE);

        Iterator<JsPair> it = object.iterator();

        assertTrue(it.hasNext());
        assertEquals(JsBoolean.TRUE, it.next().getValue());
        assertTrue(it.hasNext());
        assertEquals(JsBoolean.FALSE, it.next().getValue());
        assertFalse(it.hasNext());

        return;
    }

    /**
     * Test of size method, of class JsObject.
     */
    @Test
    public void testSize(){
        System.out.println("size");

        JsObject object = new JsObject();
        assertEquals(0, object.size());
        object.putValue("x", JsBoolean.TRUE);
        assertEquals(1, object.size());
        object.putValue("y", JsBoolean.FALSE);
        assertEquals(2, object.size());

        return;
    }

    /**
     * Test of hashCode method, of class JsObject.
     */
    @Test
    public void testHashCode(){
        System.out.println("hashCode");

        JsObject obj1 = new JsObject();
        JsObject obj2 = new JsObject();
        assertEquals(obj1.hashCode(), obj2.hashCode());

        obj1.putValue("x", new JsNumber("99"));
        obj2.putValue("x", new JsNumber("99"));
        assertEquals(obj1.hashCode(), obj2.hashCode());

        return;
    }

    /**
     * Test of equals method, of class JsObject.
     */
    @Test
    public void testEquals(){
        System.out.println("equals");

        JsObject obj1 = new JsObject();
        JsObject obj2 = new JsObject();
        assertTrue(obj1.equals(obj2));

        obj1.putValue("x", new JsNumber("99"));
        obj2.putValue("x", new JsNumber("99"));
        assertTrue(obj1.equals(obj2));

        obj1.putValue("x", new JsNumber("99"));
        obj2.putValue("x", new JsNumber("999"));
        assertFalse(obj1.equals(obj2));

        assertFalse(obj1.equals(null));

        return;
    }

    /**
     * Test of toString method, of class JsObject.
     */
    @Test
    public void testToString(){
        System.out.println("toString");

        JsObject object = new JsObject();

        assertEquals("{}", object.toString());

        object.putValue("x", JsBoolean.TRUE);
        assertEquals("{\"x\":true}", object.toString());

        object.putValue("y", JsBoolean.FALSE);
        assertEquals("{\"x\":true,\"y\":false}", object.toString());

        object.putValue("z", new JsObject());
        assertEquals("{\"x\":true,\"y\":false,\"z\":{}}", object.toString());

        return;
    }

    /**
     * Test of traverse method, of class JsObject.
     */
    @Test
    public void testTraverse() throws Exception{
        System.out.println("traverse");

        JsObject obj = new JsObject();
        JsValue val1 = new JsNumber("12");
        JsValue val2 = new JsString("AB");
        obj.putValue("x", val1);
        obj.putValue("y", val2);

        final List<Object> visited = new LinkedList<Object>();

        try{
            obj.traverse(new ValueVisitor(){
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

        assertEquals(6, visited.size());
        assertEquals(obj, visited.get(0));
        assertEquals("x", visited.get(1));
        assertEquals(val1, visited.get(2));
        assertEquals("y", visited.get(3));
        assertEquals(val2, visited.get(4));
        assertEquals(obj, visited.get(5));

        return;
    }

    /**
     * Test of hasChanged method, of class JsObject.
     */
    @Test
    public void testHasChanged(){
        System.out.println("hasChanged");

        JsObject obj = new JsObject();
        assertFalse(obj.hasChanged());

        obj.putValue("x", JsNull.NULL);
        assertTrue(obj.hasChanged());

        obj.setUnchanged();
        assertFalse(obj.hasChanged());

        JsObject child = new JsObject();
        obj.putValue("y", child);
        obj.setUnchanged();
        assertFalse(obj.hasChanged());

        child.putValue("z", JsBoolean.TRUE);
        assertTrue(obj.hasChanged());
        obj.setUnchanged();
        assertFalse(obj.hasChanged());

        return;
    }

    /**
     * Test of setUnchanged method, of class JsObject.
     */
    @Test
    public void testSetUnchanged(){
        System.out.println("setUnchanged");

        JsObject obj = new JsObject();
        JsObject child = new JsObject();
        obj.putValue("x", child);

        child.putValue("y", JsNull.NULL);
        assertTrue(child.hasChanged());

        obj.setUnchanged();
        assertFalse(child.hasChanged());

        return;
    }

}
