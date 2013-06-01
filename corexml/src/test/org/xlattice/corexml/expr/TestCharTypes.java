/* TestCharTypes.java */
package org.xlattice.corexml.expr;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestCharTypes extends TestCase {

    public TestCharTypes (String name) {
        super(name);
    }

    public void setUp () {
    }

    public void tearDown() {
    }
 
    public void testIsSpace() {
        assertTrue(CharTypes.isSpace(' '));
        assertTrue(CharTypes.isSpace('\n'));
        assertTrue(CharTypes.isSpace('\r'));
        assertTrue(CharTypes.isSpace('\t'));
    }
    // Character.isDigit(char) is wired into Java

    public void testNameChars() {
        assertTrue(CharTypes.startsName('_'));
        assertFalse(CharTypes.startsName('5'));
        assertFalse(CharTypes.startsName(':')); 
        assertFalse(CharTypes.startsName('5'));

        for (char c = 'a' ; c <= 'z'; c++)
            assertTrue(CharTypes.isNameChar(c));
        for (char c = 'A' ; c <= 'Z'; c++)
            assertTrue(CharTypes.isNameChar(c));
        for (char c = '0' ; c <= '9'; c++)
            assertTrue(CharTypes.isNameChar(c));
        assertTrue(CharTypes.isNameChar('_'));
        assertTrue(CharTypes.isNameChar('-'));
        assertFalse(CharTypes.isNameChar('.')); // non-compliant
        assertFalse(CharTypes.isNameChar(':')); 
        assertFalse(CharTypes.isNameChar('$'));
    }
}
