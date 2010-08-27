/*
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf.json;

import java.io.StringReader;
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
public class JsStringTest {

    public JsStringTest() {
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
     * Test of parseHexChar method, of class JsString.
     */
    @Test
    public void testParseHexChar() throws Exception{
        System.out.println("parseHexChar");

        JsonReader reader;
        char ch;

        reader = new JsonReader(new StringReader("0000"));
        ch = JsString.parseHexChar(reader);
        assertEquals('\u0000', ch);

        reader = new JsonReader(new StringReader("ffff"));
        ch = JsString.parseHexChar(reader);
        assertEquals('\uffff', ch);

        reader = new JsonReader(new StringReader("FFFF"));
        ch = JsString.parseHexChar(reader);
        assertEquals('\uffff', ch);

        reader = new JsonReader(new StringReader("dead"));
        ch = JsString.parseHexChar(reader);
        assertEquals('\udead', ch);

        reader = new JsonReader(new StringReader("abcde"));
        ch = JsString.parseHexChar(reader);
        assertEquals('\uabcd', ch);

        try{
            reader = new JsonReader(new StringReader("000,"));
            ch = JsString.parseHexChar(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        return;
    }

    /**
     * Test of parseString method, of class JsString.
     */
    @Test
    public void testParseString() throws Exception{
        System.out.println("parseString");

        JsonReader reader;
        JsString string;

        reader = new JsonReader(new StringReader("\"abc\""));
        string = JsString.parseString(reader);
        assertEquals("abc", string.toRawString());

        reader = new JsonReader(new StringReader("\"あいう\""));
        string = JsString.parseString(reader);
        assertEquals("あいう", string.toRawString());

        reader = new JsonReader(new StringReader("\"\\\"\\\\\\/\""));
        string = JsString.parseString(reader);
        assertEquals("\"\\/", string.toRawString());

        reader = new JsonReader(new StringReader("\"\\b\\f\\n\\r\\t\""));
        string = JsString.parseString(reader);
        assertEquals("\b\f\n\r\t", string.toRawString());

        reader = new JsonReader(new StringReader("\"\\uabcd\\uCDEF\""));
        string = JsString.parseString(reader);
        assertEquals("\uabcd\ucdef", string.toRawString());

        try{
            reader = new JsonReader(new StringReader("abc\""));
            string = JsString.parseString(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        try{
            reader = new JsonReader(new StringReader("\"abc"));
            string = JsString.parseString(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        return;
    }

    /**
     * Test of writeText method, of class JsString.
     */
    @Test
    public void testWriteText() throws Exception{
        System.out.println("writeText");

        Appendable appout;
        JsString string;

        appout = new StringBuilder();
        string = new JsString();
        JsString.writeText(appout, string);
        assertEquals("\"\"", appout.toString());

        appout = new StringBuilder();
        string = new JsString("abc");
        JsString.writeText(appout, string);
        assertEquals("\"abc\"", appout.toString());

        appout = new StringBuilder();
        string = new JsString("\"");
        JsString.writeText(appout, string);
        assertEquals("\"\\\"\"", appout.toString());

        appout = new StringBuilder();
        string = new JsString("\\");
        JsString.writeText(appout, string);
        assertEquals("\"\\\\\"", appout.toString());

        appout = new StringBuilder();
        string = new JsString("/");
        JsString.writeText(appout, string);
        assertEquals("\"\\/\"", appout.toString());

        appout = new StringBuilder();
        string = new JsString("\b");
        JsString.writeText(appout, string);
        assertEquals("\"\\b\"", appout.toString());

        appout = new StringBuilder();
        string = new JsString("\f");
        JsString.writeText(appout, string);
        assertEquals("\"\\f\"", appout.toString());

        appout = new StringBuilder();
        string = new JsString("\n");
        JsString.writeText(appout, string);
        assertEquals("\"\\n\"", appout.toString());

        appout = new StringBuilder();
        string = new JsString("\r");
        JsString.writeText(appout, string);
        assertEquals("\"\\r\"", appout.toString());

        appout = new StringBuilder();
        string = new JsString("\t");
        JsString.writeText(appout, string);
        assertEquals("\"\\t\"", appout.toString());

        appout = new StringBuilder();
        string = new JsString("\u0001");
        JsString.writeText(appout, string);
        assertEquals("\"\\u0001\"", appout.toString());

        appout = new StringBuilder();
        string = new JsString("あ");
        JsString.writeText(appout, string);
        assertEquals("\"あ\"", appout.toString());

        return;
    }

    /**
     * Test of traverse method, of class JsString.
     */
    @Test
    public void testTraverse(){
        System.out.println("traverse");

        JsString string = new JsString("A");

        try{
            string.traverse(new ValueVisitor(){
                int ct = 0;

                public void visitValue(JsValue value)
                        throws JsVisitException{
                    assertEquals(new JsString("A"), value);
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
     * Test of hasChanged method, of class JsString.
     */
    @Test
    public void testHasChanged(){
        System.out.println("hasChanged");

        JsString string = new JsString("A");

        assertFalse(string.hasChanged());
        string.setUnchanged();
        assertFalse(string.hasChanged());

        return;
    }

    /**
     * Test of setUnchanged method, of class JsString.
     */
    @Test
    public void testSetUnchanged(){
        System.out.println("setUnchanged");

        JsString string = new JsString("A");

        string.setUnchanged();
        assertFalse(string.hasChanged());

        return;
    }

    /**
     * Test of charAt method, of class JsString.
     */
    @Test
    public void testCharAt(){
        System.out.println("charAt");

        JsString string;

        string = new JsString("abcde");
        assertEquals('b', string.charAt(1));

        try{
            string.charAt(999);
            fail();
        }catch(IndexOutOfBoundsException e){
            // NOTHING
        }

        return;
    }

    /**
     * Test of length method, of class JsString.
     */
    @Test
    public void testLength(){
        System.out.println("length");

        assertEquals(0, new JsString().length());
        assertEquals(0, new JsString("").length());
        assertEquals(1, new JsString("A").length());
        assertEquals(2, new JsString("AB").length());
        assertEquals(3, new JsString("A\"B").length());

        return;
    }

    /**
     * Test of subSequence method, of class JsString.
     */
    @Test
    public void testSubSequence(){
        System.out.println("subSequence");

        JsString string;

        string = new JsString("abcde");
        assertEquals("bcd", string.subSequence(1, 4).toString());
        assertEquals("", string.subSequence(1, 1).toString());

        try{
            string.subSequence(1,999);
            fail();
        }catch(IndexOutOfBoundsException e){
            // NOTHING
        }

        return;
    }

    /**
     * Test of hashCode method, of class JsString.
     */
    @Test
    public void testHashCode(){
        System.out.println("hashCode");
        assertEquals(new JsString("A").hashCode(), new JsString("A").hashCode());
        return;
    }

    /**
     * Test of equals method, of class JsString.
     */
    @Test
    public void testEquals(){
        System.out.println("equals");

        assertTrue(new JsString("A").equals(new JsString("A")));
        assertFalse(new JsString("A").equals(new JsString("a")));
        assertFalse(new JsString("A").equals(null));

        return;
    }

    /**
     * Test of compareTo method, of class JsString.
     */
    @Test
    public void testCompareTo(){
        System.out.println("compareTo");

        assertTrue(0 == new JsString("A").compareTo(new JsString("A")));
        assertTrue(0 > new JsString("A").compareTo(new JsString("a")));
        assertTrue(0 < new JsString("a").compareTo(new JsString("A")));
        assertTrue(0 < new JsString("A").compareTo(null));

        SortedSet<JsString> set = new TreeSet<JsString>();

        set.clear();
        set.add(new JsString("A"));
        set.add(new JsString("a"));
        assertEquals(new JsString("A"), set.first());
        assertEquals(new JsString("a"), set.last());

        set.clear();
        set.add(new JsString("a"));
        set.add(new JsString("A"));
        assertEquals(new JsString("A"), set.first());
        assertEquals(new JsString("a"), set.last());

        return;
    }

    /**
     * Test of toString method, of class JsString.
     */
    @Test
    public void testToString(){
        System.out.println("toString");

        assertEquals("\"\"", new JsString("").toString());
        assertEquals("\"abc\"", new JsString("abc").toString());
        assertEquals("\"\\\"\"", new JsString("\"").toString());
        assertEquals("\"\\\\\"", new JsString("\\").toString());
        assertEquals("\"\\/\"", new JsString("/").toString());
        assertEquals("\"\\b\"", new JsString("\b").toString());
        assertEquals("\"\\f\"", new JsString("\f").toString());
        assertEquals("\"\\n\"", new JsString("\n").toString());
        assertEquals("\"\\r\"", new JsString("\r").toString());
        assertEquals("\"\\t\"", new JsString("\t").toString());
        assertEquals("\"\\u0001\"", new JsString("\u0001").toString());
        assertEquals("\"あ\"", new JsString("あ").toString());

        return;
    }

    /**
     * Test of toRawString method, of class JsString.
     */
    @Test
    public void testToRawString(){
        System.out.println("toRawString");

        assertEquals("", new JsString("").toRawString());
        assertEquals("abc", new JsString("abc").toRawString());
        assertEquals("\"", new JsString("\"").toRawString());
        assertEquals("\\", new JsString("\\").toRawString());
        assertEquals("/", new JsString("/").toRawString());
        assertEquals("\b", new JsString("\b").toRawString());
        assertEquals("\f", new JsString("\f").toRawString());
        assertEquals("\n", new JsString("\n").toRawString());
        assertEquals("\r", new JsString("\r").toRawString());
        assertEquals("\t", new JsString("\t").toRawString());
        assertEquals("\u0001", new JsString("\u0001").toRawString());
        assertEquals("あ", new JsString("あ").toRawString());

        return;
    }

}
