/*
 * CmdOption test
 *
 * Copyright 2012 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class CmdOptionTest {

    public CmdOptionTest() {
    }

    /**
     * Test of values method, of class CmdOption.
     */
    @Test
    public void testValues() {
        System.out.println("values");

        CmdOption[] values = CmdOption.values();

        assertEquals(11, values.length);

        List<CmdOption> list = Arrays.asList(values);

        List<CmdOption> testList = new LinkedList<>();
        testList.add(CmdOption.OPT_HELP);
        testList.add(CmdOption.OPT_VERSION);
        testList.add(CmdOption.OPT_BOLDMETAL);
        testList.add(CmdOption.OPT_GEOMETRY);
        testList.add(CmdOption.OPT_VMINFO);
        testList.add(CmdOption.OPT_CONSOLELOG);
        testList.add(CmdOption.OPT_INITFONT);
        testList.add(CmdOption.OPT_ANTIALIAS);
        testList.add(CmdOption.OPT_FRACTIONAL);
        testList.add(CmdOption.OPT_CONFDIR);
        testList.add(CmdOption.OPT_NOCONF);

        assertTrue(list.containsAll(testList));
        assertTrue(testList.containsAll(list));
        assertEquals(testList.size(), list.size());

        return;
    }

    /**
     * Test of valueOf method, of class CmdOption.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");

        CmdOption expResult;
        CmdOption result;

        expResult = CmdOption.OPT_HELP;
        result = CmdOption.valueOf("OPT_HELP");
        assertEquals(expResult, result);

        try{
            CmdOption.valueOf("X");
            fail();
        }catch(IllegalArgumentException e){
            // GOOD
        }

        return;
    }

    /**
     * Test of getHelpText method, of class CmdOption.
     */
    @Test
    public void testGetHelpText() {
        System.out.println("getHelpText");

        CharSequence result = CmdOption.getHelpText();

        assertNotNull(result);
        assertTrue(result.length() > 0);
        assertTrue(result.toString().endsWith("\n"));

        return;
    }

    /**
     * Test of parseCmdOption method, of class CmdOption.
     */
    @Test
    public void testParseCmdOption() {
        System.out.println("parseCmdOption");

        assertNull(CmdOption.parseCmdOption(""));
        assertNull(CmdOption.parseCmdOption("X"));

        assertEquals(CmdOption.OPT_HELP, CmdOption.parseCmdOption("-help"));
        assertEquals(CmdOption.OPT_HELP, CmdOption.parseCmdOption("-?"));

        assertEquals(CmdOption.OPT_NOCONF,
                     CmdOption.parseCmdOption("-noconfdir"));

        return;
    }

    /**
     * Test of matches method, of class CmdOption.
     */
    @Test
    public void testMatches() {
        System.out.println("matches");

        assertFalse(CmdOption.OPT_HELP.matches(""));
        assertFalse(CmdOption.OPT_HELP.matches("help"));

        assertTrue(CmdOption.OPT_HELP.matches("-help"));
        assertTrue(CmdOption.OPT_HELP.matches("-h"));
        assertTrue(CmdOption.OPT_HELP.matches("--help"));
        assertTrue(CmdOption.OPT_HELP.matches("-?"));

        assertTrue(CmdOption.OPT_VERSION.matches("-version"));
        assertTrue(CmdOption.OPT_BOLDMETAL.matches("-boldMetal"));
        assertTrue(CmdOption.OPT_GEOMETRY.matches("-geometry"));
        assertTrue(CmdOption.OPT_VMINFO.matches("-vminfo"));
        assertTrue(CmdOption.OPT_CONSOLELOG.matches("-consolelog"));
        assertTrue(CmdOption.OPT_INITFONT.matches("-initfont"));
        assertTrue(CmdOption.OPT_ANTIALIAS.matches("-antialias"));
        assertTrue(CmdOption.OPT_FRACTIONAL.matches("-fractional"));
        assertTrue(CmdOption.OPT_CONFDIR.matches("-confdir"));
        assertTrue(CmdOption.OPT_NOCONF.matches("-noconfdir"));

        return;
    }

    /**
     * Test of isIndepOption method, of class CmdOption.
     */
    @Test
    public void testIsIndepOption() {
        System.out.println("isIndepOption");

        for(CmdOption opt : CmdOption.values()){
            switch(opt){
            case OPT_HELP:
            case OPT_VERSION:
            case OPT_VMINFO:
            case OPT_BOLDMETAL:
            case OPT_CONSOLELOG:
            case OPT_NOCONF:
                assertTrue(opt.isIndepOption());
                break;
            default:
                assertFalse(opt.isIndepOption());
                break;
            }
        }

        return;
    }

    /**
     * Test of isBooleanOption method, of class CmdOption.
     */
    @Test
    public void testIsBooleanOption() {
        System.out.println("isBooleanOption");

        for(CmdOption opt : CmdOption.values()){
            switch(opt){
            case OPT_ANTIALIAS:
            case OPT_FRACTIONAL:
                assertTrue(opt.isBooleanOption());
                break;
            default:
                assertFalse(opt.isBooleanOption());
                break;
            }
        }

        return;
    }

    /**
     * Test of toString method, of class CmdOption.
     */
    @Test
    public void testToString() {
        System.out.println("toString");

        assertEquals("-help", CmdOption.OPT_HELP.toString());
        assertEquals("-version", CmdOption.OPT_VERSION.toString());
        assertEquals("-boldMetal", CmdOption.OPT_BOLDMETAL.toString());
        assertEquals("-geometry", CmdOption.OPT_GEOMETRY.toString());
        assertEquals("-vminfo", CmdOption.OPT_VMINFO.toString());
        assertEquals("-consolelog", CmdOption.OPT_CONSOLELOG.toString());
        assertEquals("-initfont", CmdOption.OPT_INITFONT.toString());
        assertEquals("-antialias", CmdOption.OPT_ANTIALIAS.toString());
        assertEquals("-fractional", CmdOption.OPT_FRACTIONAL.toString());
        assertEquals("-confdir", CmdOption.OPT_CONFDIR.toString());
        assertEquals("-noconfdir", CmdOption.OPT_NOCONF.toString());

        return;
    }
}
