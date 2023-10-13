/*
 */

package jp.sfjp.jindolf.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class AnchorTest {

    public AnchorTest() {
    }

    /**
     * Test of parseInt method, of class Anchor.
     */
    @Test
    public void testParseInt_3args_1() {
        System.out.println("parseInt");

        int result;
        Matcher matcher;
        Pattern pattern;
        String input = "ABC123PQR456XYZ";

        pattern = Pattern.compile("([0-9]+)[A-Z]*([0-9]+)");
        matcher = pattern.matcher(input);

        assertTrue(matcher.find());

        result = Anchor.parseInt(input, matcher, 1);
        assertEquals(123, result);

        result = Anchor.parseInt(input, matcher, 2);
        assertEquals(456, result);

        try{
            Anchor.parseInt(null, matcher, 1);
            fail();
        }catch(NullPointerException e){
        }

        try{
            Anchor.parseInt(input, null, 1);
            fail();
        }catch(NullPointerException e){
        }

        return;
    }

    /**
     * Test of parseInt method, of class Anchor.
     */
    @Test
    public void testParseInt_3args_2() {
        System.out.println("parseInt");

        int result;

        try{
            Anchor.parseInt(null, 1, 3);
            fail();
        }catch(NullPointerException e){
        }

        result = Anchor.parseInt("1234567", 2, 5);
        assertEquals(345, result);

        result = Anchor.parseInt("1234567", 2, 3);
        assertEquals(3, result);

        result = Anchor.parseInt("1234567", 2, 2);
        assertEquals(0, result);

        result = Anchor.parseInt("1234567", 2, 1);
        assertEquals(0, result);

        result = Anchor.parseInt("1234567", 0, 0);
        assertEquals(0, result);

        try{
            Anchor.parseInt("1234567", 2, 999);
            fail();
        }catch(StringIndexOutOfBoundsException e){
        }

        try{
            Anchor.parseInt("1234567", -1, 5);
            fail();
        }catch(StringIndexOutOfBoundsException e){
        }

        return;
    }

}
