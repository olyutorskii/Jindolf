/*
 * WolfBBS test
 *
 * Copyright 2018 olyutorskii
 */

package jp.sfjp.jindolf.dxchg;

import java.awt.Color;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class WolfBBSTest {

    public WolfBBSTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of escapeWikiName method, of class WolfBBS.
     */
    @Test
    public void testEscapeWikiName() {
        System.out.println("escapeWikiName");

        CharSequence result;

        result = WolfBBS.escapeWikiName("aBc");
        assertEquals("aBc", result.toString());

        result = WolfBBS.escapeWikiName("AbC");
        assertEquals("AbC", result.toString());

        result = WolfBBS.escapeWikiName("AbCd");
        assertEquals("Ab&#x43;d", result.toString());

        result = WolfBBS.escapeWikiName("ABcDe");
        assertEquals("ABc&#x44;e", result.toString());

        result = WolfBBS.escapeWikiName("AbCdEfGh");
        assertEquals("Ab&#x43;dEf&#x47;h", result.toString());

        return;
    }

    /**
     * Test of cnvWikiColor method, of class WolfBBS.
     */
    @Test
    public void testCnvWikiColor() {
        System.out.println("cnvWikiColor");

        String result;

        result = WolfBBS.cnvWikiColor(new Color(0x00, 0x00, 0x00));
        assertEquals("#000000", result);

        result = WolfBBS.cnvWikiColor(new Color(0x12, 0x34, 0x56));
        assertEquals("#123456", result);

        result = WolfBBS.cnvWikiColor(new Color(0x01, 0x00, 0x00));
        assertEquals("#010000", result);

        result = WolfBBS.cnvWikiColor(new Color(0x00, 0x00, 0x01));
        assertEquals("#000001", result);

        result = WolfBBS.cnvWikiColor(new Color(0xff, 0xff, 0xff));
        assertEquals("#ffffff", result);

        result = WolfBBS.cnvWikiColor(new Color(0x12, 0x34, 0x56, 0x78));
        assertEquals("#123456", result);

        return;
    }

}
