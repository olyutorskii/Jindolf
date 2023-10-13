/*
 * OptionInfo test
 *
 * Copyright 2012 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class OptionInfoTest {

    public OptionInfoTest() {
    }

    /**
     * Test of parseOptions method, of class OptionInfo.
     */
    @Test
    public void testParseOptions() {
        System.out.println("parseOptions");

        OptionInfo result;

        result = OptionInfo.parseOptions();
        assertNotNull(result);

        result = OptionInfo.parseOptions(null, null);
        assertNotNull(result);
        assertTrue(result.getInvokeArgList().isEmpty());

        result = OptionInfo.parseOptions(new String[]{});
        assertNotNull(result);

        result = OptionInfo.parseOptions("-help");
        assertNotNull(result);

        try{
            OptionInfo.parseOptions("X");
            fail();
        }catch(IllegalArgumentException e){
            assertEquals("未定義の起動オプション[X]が指定されました。",
                         e.getMessage() );
        }

        try{
            OptionInfo.parseOptions("");
            fail();
        }catch(IllegalArgumentException e){
            assertEquals("未定義の起動オプション[]が指定されました。",
                         e.getMessage() );
        }

        return;
    }

    /**
     * Test of getInvokeArgList method, of class OptionInfo.
     */
    @Test
    public void testGetInvokeArgList() {
        System.out.println("getInvokeArgList");

        OptionInfo result;
        List<String> list;

        result = OptionInfo.parseOptions();
        list = result.getInvokeArgList();
        assertTrue(list.isEmpty());

        result = OptionInfo.parseOptions("-help");
        list = result.getInvokeArgList();
        assertEquals(1, list.size());
        assertEquals("-help", list.get(0));

        result = OptionInfo.parseOptions("-initfont", "on", "-help");
        list = result.getInvokeArgList();
        assertEquals(3, list.size());
        assertEquals("-initfont", list.get(0));
        assertEquals("on", list.get(1));
        assertEquals("-help", list.get(2));

        return;
    }

    /**
     * Test of hasOption method, of class OptionInfo.
     */
    @Test
    public void testHasOption() {
        System.out.println("hasOption");

        OptionInfo result;

        result = OptionInfo.parseOptions();
        assertFalse(result.hasOption(CmdOption.OPT_HELP));
        assertFalse(result.hasOption(CmdOption.OPT_INITFONT));

        result = OptionInfo.parseOptions("-help");
        assertTrue(result.hasOption(CmdOption.OPT_HELP));
        assertFalse(result.hasOption(CmdOption.OPT_INITFONT));

        result = OptionInfo.parseOptions("-initfont", "on", "-help");
        assertTrue(result.hasOption(CmdOption.OPT_HELP));
        assertTrue(result.hasOption(CmdOption.OPT_INITFONT));

        result = OptionInfo.parseOptions("-initfont", "off", "-help");
        assertTrue(result.hasOption(CmdOption.OPT_HELP));
        assertTrue(result.hasOption(CmdOption.OPT_INITFONT));

        return;
    }

    /**
     * Test of getBooleanArg method, of class OptionInfo.
     */
    @Test
    public void testGetBooleanArg() {
        System.out.println("getBooleanArg");

        OptionInfo result;

        try{
            OptionInfo.parseOptions("-antialias");
            fail();
        }catch(IllegalArgumentException e){
            String expMsg =
                    "起動オプション[-antialias]に引数がありません。";
            assertEquals(expMsg, e.getMessage());
        }

        try{
            OptionInfo.parseOptions("-fractional");
            fail();
        }catch(IllegalArgumentException e){
            String expMsg =
                    "起動オプション[-fractional]に引数がありません。";
            assertEquals(expMsg, e.getMessage());
        }

        result = OptionInfo.parseOptions("-help");
        try{
            result.getBooleanArg(CmdOption.OPT_HELP);
            fail();
        }catch(IllegalArgumentException e){
            String expMsg =
                      "起動オプション[-help]は"
                    + "真偽を指定するオプションではありません。";
            assertEquals(expMsg, e.getMessage());
        }

        result = OptionInfo.parseOptions();
        assertNull(result.getBooleanArg(CmdOption.OPT_ANTIALIAS));
        assertNull(result.getBooleanArg(CmdOption.OPT_FRACTIONAL));


        String[] flags;

        flags = new String[]{"on", "yes", "true", "ON", "YES", "TRUE"};
        for(String flag : flags){
            result = OptionInfo.parseOptions("-antialias", flag);
            assertTrue(result.getBooleanArg(CmdOption.OPT_ANTIALIAS));
            result = OptionInfo.parseOptions("-fractional", flag);
            assertTrue(result.getBooleanArg(CmdOption.OPT_FRACTIONAL));
        }

        flags = new String[]{"off", "no", "false", "OFF", "NO", "FALSE"};
        for(String flag : flags){
            result = OptionInfo.parseOptions("-antialias", flag);
            assertFalse(result.getBooleanArg(CmdOption.OPT_ANTIALIAS));
            result = OptionInfo.parseOptions("-fractional", flag);
            assertFalse(result.getBooleanArg(CmdOption.OPT_FRACTIONAL));
        }

        try{
            OptionInfo.parseOptions("-antialias", "X");
            fail();
        }catch(IllegalArgumentException e){
            String expMsg =
                    "起動オプション[-antialias]の真偽指定[X]が不正です。"
                    + "on, off, yes, no, true, false"
                    + "のいずれかを指定してください。";
            assertEquals(expMsg, e.getMessage());
        }

        try{
            OptionInfo.parseOptions("-fractional", "X");
            fail();
        }catch(IllegalArgumentException e){
            String expMsg =
                    "起動オプション[-fractional]の真偽指定[X]が不正です。"
                    + "on, off, yes, no, true, false"
                    + "のいずれかを指定してください。";
            assertEquals(expMsg, e.getMessage());
        }

        result = OptionInfo.parseOptions("-antialias", "on",
                                         "-antialias", "off" );
        assertFalse(result.getBooleanArg(CmdOption.OPT_ANTIALIAS));

        return;
    }

    /**
     * Test of getStringArg method, of class OptionInfo.
     */
    @Test
    public void testGetStringArg() {
        System.out.println("getStringArg");

        OptionInfo result;

        try{
            OptionInfo.parseOptions("-initfont");
            fail();
        }catch(IllegalArgumentException e){
            String expMsg =
                    "起動オプション[-initfont]に引数がありません。";
            assertEquals(expMsg, e.getMessage());
        }

        try{
            OptionInfo.parseOptions("-confdir");
            fail();
        }catch(IllegalArgumentException e){
            String expMsg =
                    "起動オプション[-confdir]に引数がありません。";
            assertEquals(expMsg, e.getMessage());
        }

        result = OptionInfo.parseOptions();
        assertNull(result.getStringArg(CmdOption.OPT_INITFONT));
        assertNull(result.getStringArg(CmdOption.OPT_CONFDIR));

        result = OptionInfo.parseOptions("-initfont", "Monospaced-PLAIN-16");
        assertEquals("Monospaced-PLAIN-16",
                     result.getStringArg(CmdOption.OPT_INITFONT));

        result = OptionInfo.parseOptions("-confdir", "/tmp/x");
        assertEquals("/tmp/x",
                     result.getStringArg(CmdOption.OPT_CONFDIR));

        result = OptionInfo.parseOptions("-confdir", "/tmp/x",
                                         "-confdir", "/tmp/y" );
        assertEquals("/tmp/y",
                     result.getStringArg(CmdOption.OPT_CONFDIR));

        return;
    }

    /**
     * Test of getExclusiveOption method, of class OptionInfo.
     */
    @Test
    public void testGetExclusiveOption() {
        System.out.println("getExclusiveOption");

        OptionInfo result;
        CmdOption opt;

        result = OptionInfo.parseOptions();
        opt = result.getExclusiveOption(CmdOption.OPT_CONFDIR,
                                        CmdOption.OPT_NOCONF );
        assertNull(opt);

        result = OptionInfo.parseOptions("-help");
        opt = result.getExclusiveOption(CmdOption.OPT_CONFDIR,
                                        CmdOption.OPT_NOCONF );
        assertNull(opt);

        result = OptionInfo.parseOptions("-confdir", "/tmp/x");
        opt = result.getExclusiveOption(CmdOption.OPT_CONFDIR,
                                        CmdOption.OPT_NOCONF );
        assertSame(CmdOption.OPT_CONFDIR, opt);

        result = OptionInfo.parseOptions("-noconfdir");
        opt = result.getExclusiveOption(CmdOption.OPT_CONFDIR,
                                        CmdOption.OPT_NOCONF );
        assertSame(CmdOption.OPT_NOCONF, opt);

        result = OptionInfo.parseOptions("-confdir", "/tmp/x",
                                         "-noconfdir" );
        opt = result.getExclusiveOption(CmdOption.OPT_CONFDIR,
                                        CmdOption.OPT_NOCONF );
        assertSame(CmdOption.OPT_NOCONF, opt);

        result = OptionInfo.parseOptions("-noconfdir",
                                         "-confdir", "/tmp/x" );
        opt = result.getExclusiveOption(CmdOption.OPT_CONFDIR,
                                        CmdOption.OPT_NOCONF );
        assertSame(CmdOption.OPT_CONFDIR, opt);

        return;
    }

    /**
     * Test of geometry option, of class OptionInfo.
     */
    @Test
    public void testGeometry() {
        System.out.println("initialFrameWidth");

        OptionInfo result;

        result = OptionInfo.parseOptions();
        assertNull(result.initialFrameWidth());
        assertNull(result.initialFrameHeight());
        assertNull(result.initialFrameXpos());
        assertNull(result.initialFrameYpos());

        try{
            OptionInfo.parseOptions("-geometry");
            fail();
        }catch(IllegalArgumentException e){
            String expMsg =
                    "起動オプション[-geometry]に引数がありません。";
            assertEquals(expMsg, e.getMessage());
        }

        try{
            OptionInfo.parseOptions("-geometry", "Q");
            fail();
        }catch(IllegalArgumentException e){
            String expMsg =
                      "起動オプション[-geometry]の"
                    + "ジオメトリ指定[Q]が不正です。"
                    + "WIDTHxHEIGHT[(+|-)XPOS(+|-)YPOS]"
                    + "の形式で指定してください";
            assertEquals(expMsg, e.getMessage());
        }

        result = OptionInfo.parseOptions("-geometry", "800x600");
        assertEquals(800, result.initialFrameWidth().intValue());
        assertEquals(600, result.initialFrameHeight().intValue());
        assertNull(result.initialFrameXpos());
        assertNull(result.initialFrameYpos());

        result = OptionInfo.parseOptions("-geometry", "800x600+100+200");
        assertEquals(800, result.initialFrameWidth().intValue());
        assertEquals(600, result.initialFrameHeight().intValue());
        assertEquals(100, result.initialFrameXpos().intValue());
        assertEquals(200, result.initialFrameYpos().intValue());

        result = OptionInfo.parseOptions("-geometry", "800x600-100-200");
        assertEquals(800, result.initialFrameWidth().intValue());
        assertEquals(600, result.initialFrameHeight().intValue());
        assertEquals(-100, result.initialFrameXpos().intValue());
        assertEquals(-200, result.initialFrameYpos().intValue());

        return;
    }

}
