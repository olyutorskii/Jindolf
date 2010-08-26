/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jp.sourceforge.jindolf.json;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class JsNumberTest {

    public JsNumberTest() {
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
     * Test of parseNumber method, of class JsNumber.
     */
    @Test
    public void testParseNumber() throws Exception{
        System.out.println("parseNumber");

        JsonReader reader;
        JsNumber number;

        try{
            reader = new JsonReader(new StringReader("0"));
            number = JsNumber.parseNumber(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        reader = new JsonReader(new StringReader("0,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("0", number.toString());

        reader = new JsonReader(new StringReader("\n\r\t\u00200,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("0", number.toString());

        reader = new JsonReader(new StringReader("-0,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("0", number.toString());

        reader = new JsonReader(new StringReader("12,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("12", number.toString());

        reader = new JsonReader(new StringReader("-12,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("-12", number.toString());

        try{
            reader = new JsonReader(new StringReader("+12,"));
            number = JsNumber.parseNumber(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        try{
            reader = new JsonReader(new StringReader("12.,"));
            number = JsNumber.parseNumber(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        reader = new JsonReader(new StringReader("12.34,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("12.34", number.toString());

        reader = new JsonReader(new StringReader("12.0,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("12.0", number.toString());

        reader = new JsonReader(new StringReader("12.00,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("12.00", number.toString());

        reader = new JsonReader(new StringReader("12.003,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("12.003", number.toString());

        reader = new JsonReader(new StringReader("12.0030,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("12.0030", number.toString());

        try{
            reader = new JsonReader(new StringReader("09,"));
            number = JsNumber.parseNumber(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        reader = new JsonReader(new StringReader("12e34,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("1.2E+35", number.toString());

        reader = new JsonReader(new StringReader("12E34,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("1.2E+35", number.toString());

        reader = new JsonReader(new StringReader("12e+34,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("1.2E+35", number.toString());

        reader = new JsonReader(new StringReader("12e-34,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("1.2E-33", number.toString());

        reader = new JsonReader(new StringReader("12e0034,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("1.2E+35", number.toString());

        try{
            reader = new JsonReader(new StringReader("12e,"));
            number = JsNumber.parseNumber(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        try{
            reader = new JsonReader(new StringReader("12e+,"));
            number = JsNumber.parseNumber(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        try{
            reader = new JsonReader(new StringReader("12e-,"));
            number = JsNumber.parseNumber(reader);
            fail();
        }catch(JsParseException e){
            // NOTHING
        }

        reader = new JsonReader(new StringReader("-12.34e-056,"));
        number = JsNumber.parseNumber(reader);
        assertEquals("-1.234E-55", number.toString());

        return;
    }

    /**
     * Test of constructor, of class JsNumber.
     */
    @Test
    public void testConstructors() throws Exception{
        System.out.println("constructor");

        JsNumber number;
        BigDecimal decimal;

        number = new JsNumber(99L);
        decimal = number.getBigDecimal();
        assertEquals(new BigInteger("99"), decimal.unscaledValue());
        assertEquals(0, decimal.scale());

        number = new JsNumber(99.0);
        decimal = number.getBigDecimal();
        assertEquals(new BigInteger("990"), decimal.unscaledValue());
        assertEquals(1, decimal.scale());

        number = new JsNumber(new BigInteger("99"));
        decimal = number.getBigDecimal();
        assertEquals(new BigInteger("99"), decimal.unscaledValue());
        assertEquals(0, decimal.scale());

        number = new JsNumber("99.9");
        decimal = number.getBigDecimal();
        assertEquals(new BigInteger("999"), decimal.unscaledValue());
        assertEquals(1, decimal.scale());

        number = new JsNumber(new BigDecimal("99.9"));
        decimal = number.getBigDecimal();
        assertEquals(new BigInteger("999"), decimal.unscaledValue());
        assertEquals(1, decimal.scale());

        return;
    }

    /**
     * Test of traverse method, of class JsNumber.
     */
    @Test
    public void testTraverse(){
        System.out.println("traverse");

        JsNumber number = new JsNumber("0");

        try{
            number.traverse(new ValueVisitor(){
                int ct = 0;

                public void visitValue(JsValue value)
                        throws JsVisitException{
                    assertEquals(new JsNumber("0"), value);
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
     * Test of hasChanged method, of class JsNumber.
     */
    @Test
    public void testHasChanged(){
        System.out.println("hasChanged");

        JsNumber number = new JsNumber("0");

        assertFalse(number.hasChanged());
        number.setUnchanged();
        assertFalse(number.hasChanged());

        return;
    }

    /**
     * Test of setUnchanged method, of class JsNumber.
     */
    @Test
    public void testSetUnchanged(){
        System.out.println("setUnchanged");

        JsNumber number = new JsNumber("0");

        number.setUnchanged();
        assertFalse(number.hasChanged());

        return;
    }

    /**
     * Test of getBigDecimal method, of class JsNumber.
     */
    @Test
    public void testGetBigDecimal(){
        System.out.println("getBigDecimal");

        JsNumber number = new JsNumber("-123.456e+1");
        BigDecimal decimal = number.getBigDecimal();

        assertEquals(new BigDecimal("-123.456e+1"), decimal);
        assertEquals(2, decimal.scale());
        assertEquals(new BigInteger("-123456"), decimal.unscaledValue());

        return;
    }

    /**
     * Test of intValue method, of class JsNumber.
     */
    @Test
    public void testIntValue(){
        System.out.println("intValue");

        assertEquals(0, new JsNumber("0").intValue());
        assertEquals(99, new JsNumber("99.9").intValue());
        assertEquals(-99, new JsNumber("-99.9").intValue());
        assertEquals(2147483647, new JsNumber("2147483647").intValue());

        return;
    }

    /**
     * Test of longValue method, of class JsNumber.
     */
    @Test
    public void testLongValue(){
        System.out.println("longValue");

        assertEquals(0L, new JsNumber("0").longValue());
        assertEquals(99L, new JsNumber("99.9").longValue());
        assertEquals(-99L, new JsNumber("-99.9").longValue());
        assertEquals(999999999999L, new JsNumber("999999999999").longValue());

        return;
    }

    /**
     * Test of floatValue method, of class JsNumber.
     */
    @Test
    public void testFloatValue(){
        System.out.println("floatValue");

        assertEquals(1.25f, new JsNumber("1.25").floatValue(), 0.0);
        assertEquals(1.25f, new JsNumber("125E-2").floatValue(), 0.0);

        return;
    }

    /**
     * Test of doubleValue method, of class JsNumber.
     */
    @Test
    public void testDoubleValue(){
        System.out.println("doubleValue");

        assertEquals(1.25, new JsNumber("1.25").doubleValue(), 0.0);
        assertEquals(1.25, new JsNumber("125E-2").doubleValue(), 0.0);

        return;
    }

    /**
     * Test of hashCode method, of class JsNumber.
     */
    @Test
    public void testHashCode(){
        System.out.println("hashCode");

        assertEquals(new JsNumber("1").hashCode(), new JsNumber("1").hashCode());
        assertEquals(new JsNumber("1.23").hashCode(), new JsNumber("123e-2").hashCode());

        return;
    }

    /**
     * Test of equals method, of class JsNumber.
     */
    @Test
    public void testEquals(){
        System.out.println("equals");

        assertTrue(new JsNumber("1").equals(new JsNumber("1")));
        assertFalse(new JsNumber("1").equals(new JsNumber("2")));
        assertFalse(new JsNumber("1").equals(null));

        assertTrue(new JsNumber("1.23").equals(new JsNumber("123e-2")));
        assertFalse(new JsNumber("1.0").equals(new JsNumber("1.00")));

        return;
    }

    /**
     * Test of compareTo method, of class JsNumber.
     */
    @Test
    public void testCompareTo(){
        System.out.println("compareTo");

        assertTrue(0 > new JsNumber("-1").compareTo(new JsNumber("1")));
        assertTrue(0 < new JsNumber("1").compareTo(new JsNumber("-1")));
        assertTrue(new JsNumber("1").compareTo(new JsNumber("1")) == 0);

        assertTrue(0 > new JsNumber("1").compareTo(new JsNumber("2")));
        assertTrue(0 < new JsNumber("9").compareTo(new JsNumber("8")));

        assertTrue(new JsNumber("1.23").compareTo(new JsNumber("123e-2")) == 0);
        assertTrue(new JsNumber("1.0").compareTo(new JsNumber("1.00")) == 0);

        return;
    }

    /**
     * Test of toString method, of class JsNumber.
     */
    @Test
    public void testToString(){
        System.out.println("toString");

        JsNumber number;

        number = new JsNumber("0");
        assertEquals("0", number.toString());
        number = new JsNumber("+0");
        assertEquals("0", number.toString());
        number = new JsNumber("-0");
        assertEquals("0", number.toString());

        number = new JsNumber("1");
        assertEquals("1", number.toString());
        number = new JsNumber("+1");
        assertEquals("1", number.toString());
        number = new JsNumber("-1");
        assertEquals("-1", number.toString());

        number = new JsNumber("0.0");
        assertEquals("0.0", number.toString());

        number = new JsNumber("1.0");
        assertEquals("1.0", number.toString());

        number = new JsNumber("1.00");
        assertEquals("1.00", number.toString());

        number = new JsNumber("0.1");
        assertEquals("0.1", number.toString());

        number = new JsNumber("0.10");
        assertEquals("0.10", number.toString());

        number = new JsNumber("0.000001");
        assertEquals("0.000001", number.toString());

        number = new JsNumber("0.0000001");
        assertEquals("1E-7", number.toString());

        number = new JsNumber("123e0");
        assertEquals("123", number.toString());

        number = new JsNumber("123e1");
        assertEquals("1.23E+3", number.toString());

        number = new JsNumber("123E1");
        assertEquals("1.23E+3", number.toString());

        number = new JsNumber("123e+1");
        assertEquals("1.23E+3", number.toString());

        number = new JsNumber("123e-1");
        assertEquals("12.3", number.toString());

        number = new JsNumber("123e-8");
        assertEquals("0.00000123", number.toString());

        number = new JsNumber("123e-9");
        assertEquals("1.23E-7", number.toString());

        return;
    }

}
