/*
 */

package jp.sfjp.jindolf.data;

import jp.sourceforge.jindolf.corelib.TalkType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class TalkTest {

    public TalkTest() {
    }

    /**
     * Test of encodeColorName method, of class Talk.
     */
    @Test
    public void testEncodeColorName() {
        System.out.println("encodeColorName");

        assertEquals("白", Talk.encodeColorName(TalkType.PUBLIC));
        assertEquals("灰", Talk.encodeColorName(TalkType.PRIVATE));
        assertEquals("赤", Talk.encodeColorName(TalkType.WOLFONLY));
        assertEquals("青", Talk.encodeColorName(TalkType.GRAVE));

        try{
            Talk.encodeColorName(null);
            fail();
        }catch(NullPointerException e){
            assert true;
        }

        return;
    }

    /**
     * Test of isTerminated method, of class Talk.
     */
    @Test
    public void testIsTerminated() {
        System.out.println("isTerminated");

        try{
            Talk.isTerminated(null, null);
            fail();
        }catch(NullPointerException e){
            assert true;
        }

        try{
            Talk.isTerminated("A", null);
            fail();
        }catch(NullPointerException e){
            assert true;
        }

        try{
            Talk.isTerminated(null, "X");
            fail();
        }catch(NullPointerException e){
            assert true;
        }

        assertTrue(Talk.isTerminated("ABCXYZ", "XYZ"));
        assertTrue(Talk.isTerminated("ABCXYZ", "ABCXYZ"));
        assertTrue(Talk.isTerminated("ABCXYZ", ""));
        assertTrue(Talk.isTerminated("", ""));

        assertFalse(Talk.isTerminated("ABCXYZ", "PQR"));
        assertFalse(Talk.isTerminated("ABCXYZ", "1ABCXYZ"));
        assertFalse(Talk.isTerminated("ABC", "ABCXYZ"));
        assertFalse(Talk.isTerminated("", "XYZ"));

        return;
    }

}
