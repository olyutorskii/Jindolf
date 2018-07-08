/*
 * WolfBBS test
 *
 * Copyright 2018 olyutorskii
 */

package jp.sfjp.jindolf.dxchg;

import jp.sourceforge.jindolf.corelib.Destiny;
import jp.sourceforge.jindolf.corelib.GameRole;
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
     * Test of getTeamWikiColor method, of class WolfBBS.
     */
    @Test
    public void testGetTeamWikiColor() {
        System.out.println("getTeamWikiColor");

        String result;

        result = WolfBBS.getTeamWikiColor(GameRole.INNOCENT);
        assertEquals("#b7bad3", result);

        result = WolfBBS.getTeamWikiColor(GameRole.FRATER);
        assertEquals("#b7bad3", result);

        result = WolfBBS.getTeamWikiColor(GameRole.HUNTER);
        assertEquals("#b7bad3", result);

        result = WolfBBS.getTeamWikiColor(GameRole.SEER);
        assertEquals("#b7bad3", result);

        result = WolfBBS.getTeamWikiColor(GameRole.SHAMAN);
        assertEquals("#b7bad3", result);

        result = WolfBBS.getTeamWikiColor(GameRole.WOLF);
        assertEquals("#e0b8b8", result);

        result = WolfBBS.getTeamWikiColor(GameRole.MADMAN);
        assertEquals("#e0b8b8", result);

        result = WolfBBS.getTeamWikiColor(GameRole.HAMSTER);
        assertEquals("#b9d0be", result);

        return;
    }

    /**
     * Test of getDestinyColorWiki method, of class WolfBBS.
     */
    @Test
    public void testGetDestinyColorWiki() {
        System.out.println("getDestinyColorWiki");

        String result;

        result = WolfBBS.getDestinyColorWiki(Destiny.ALIVE);
        assertEquals("#ffffff", result);

        result = WolfBBS.getDestinyColorWiki(Destiny.DISSOLVE);
        assertEquals("#aaaaaa", result);

        result = WolfBBS.getDestinyColorWiki(Destiny.EATEN);
        assertEquals("#aaaaaa", result);

        result = WolfBBS.getDestinyColorWiki(Destiny.EXECUTED);
        assertEquals("#aaaaaa", result);

        result = WolfBBS.getDestinyColorWiki(Destiny.SUDDENDEATH);
        assertEquals("#aaaaaa", result);

        return;
    }

}
