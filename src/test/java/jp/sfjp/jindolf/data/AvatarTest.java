/*
 * Avatar Test
 *
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.data;

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
public class AvatarTest {

    public AvatarTest() {
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
     * Test of getPredefinedAvatarList method, of class Avatar.
     */
    @Test
    public void testGetPredefinedAvatarList(){
        System.out.println("getPredefinedAvatarList");
        List<Avatar> result = Avatar.getPredefinedAvatarList();
        assertNotNull(result);
        assertEquals(20, result.size());
        return;
    }

    /**
     * Test of getAvatarByFullname method, of class Avatar.
     */
    @Test
    public void testGetAvatarByFullname(){
        System.out.println("getPredefinedAvatar");
        Avatar result;
        result = Avatar.getAvatarByFullname("農夫 ヤコブ");
        assertNotNull(result);
        assertTrue(result.equals(result));
        return;
    }

    /**
     * Test of getFullName method, of class Avatar.
     */
    @Test
    public void testGetFullName(){
        System.out.println("getFullName");
        Avatar result = Avatar.getAvatarByFullname("農夫 ヤコブ");
        assertNotNull(result);
        assertEquals("農夫 ヤコブ", result.getFullName());
        return;
    }

    /**
     * Test of getJobTitle method, of class Avatar.
     */
    @Test
    public void testGetJobTitle(){
        System.out.println("getJobTitle");
        Avatar result = Avatar.getAvatarByFullname("農夫 ヤコブ");
        assertNotNull(result);
        assertEquals("農夫", result.getJobTitle());
        return;
    }

    /**
     * Test of getName method, of class Avatar.
     */
    @Test
    public void testGetName(){
        System.out.println("getName");
        Avatar result = Avatar.getAvatarByFullname("農夫 ヤコブ");
        assertNotNull(result);
        assertEquals("ヤコブ", result.getName());
        return;
    }

    /**
     * Test of getIdNum method, of class Avatar.
     */
    @Test
    public void testGetIdNum(){
        System.out.println("getIdNum");
        Avatar result = Avatar.getAvatarByFullname("農夫 ヤコブ");
        assertNotNull(result);
        assertEquals(15, result.getIdNum());
        return;
    }

    /**
     * Test of equals method, of class Avatar.
     */
    @Test
    public void testEquals(){
        System.out.println("equals");
        Avatar result = Avatar.getAvatarByFullname("農夫 ヤコブ");
        assertTrue(result.equals(result));
        return;
    }

    /**
     * Test of compareTo method, of class Avatar.
     */
    @Test
    public void testCompareTo(){
        System.out.println("compareTo");
        Avatar avatar1 = Avatar.getAvatarByFullname("農夫 ヤコブ");
        Avatar avatar2 = Avatar.getAvatarByFullname("シスター フリーデル");
        Avatar avatar3 = Avatar.getAvatarByFullname("羊飼い カタリナ");
        assertTrue(avatar1.compareTo(avatar2) < 0);
        assertTrue(avatar2.compareTo(avatar3) > 0);
        assertTrue(avatar2.compareTo(avatar2) == 0);
        return;
    }
}
