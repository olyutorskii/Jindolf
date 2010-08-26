/*
 * CodeX0208 Test
 *
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class CodeX0208Test {

    public CodeX0208Test() {
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
     * Test of isValid method, of class CodeX0208.
     */
    @Test
    public void testIsValid(){
        System.out.println("isValid");

        assertTrue (CodeX0208.isValid('A'));
        assertTrue (CodeX0208.isValid('狼'));
        assertFalse(CodeX0208.isValid('㍻'));

        return;
    }

}
