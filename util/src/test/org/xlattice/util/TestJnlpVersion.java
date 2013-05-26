/* TestJnlpVersion.java */
package org.xlattice.util;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestJnlpVersion extends TestCase {

    private JnlpVersion jv;
    private JnlpVersion jv2;
    
    public TestJnlpVersion (String name) {
        super(name);
    }

    public void setUp () {
        jv = null;
    }

    public void testEmpty() {
        String nullString = null;
        try {
            jv = new JnlpVersion(nullString);
            fail("didn't catch null version string");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            jv = new JnlpVersion("");
            fail("didn't catch empty version string");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            jv = new JnlpVersion(" \t ");
            fail("didn't catch whitespace-only version string");
        } catch (IllegalArgumentException iae) { /* success */ }
    }
    /**
     * toString() should return the trimmed form of the String 
     * passed to the constructor, with no changes to the 
     * separators.
     */
    public void testToString () {
        assertEquals ("4",   new JnlpVersion("\t 4").toString());
        assertEquals ("1.2", new JnlpVersion(" 1.2\t").toString());
        assertEquals ("11-22-33",
                             new JnlpVersion("11-22-33   ").toString());
        assertEquals ("77_8_99",
                             new JnlpVersion("  77_8_99  ").toString());
    }
    public void testSimpleNumeric () {
        jv = new JnlpVersion("1.4.3");
        assertNotNull (jv);
        assertEquals (3, jv.size());
        assertEquals ("1", jv.get(0));
        assertEquals ("4", jv.get(1));
        assertEquals ("3", jv.get(2));
    }
    public void testWhitespace() {
        try {
            jv = new JnlpVersion("a b");
            fail("didn't catch space within version string");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            jv = new JnlpVersion("a\tb");
            fail("didn't catch tab within version string");
        } catch (IllegalArgumentException iae) { /* success */ }
        
    }
    public void testBadSeparators() {
        try {
            jv = new JnlpVersion("_ab");
            fail("didn't catch leading separator");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            jv = new JnlpVersion("a--b");
            fail("didn't catch double separator");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            jv = new JnlpVersion("ab.");
            fail("didn't catch trailing separator");
        } catch (IllegalArgumentException iae) { /* success */ }

    }
    public void testIdentity() {
        // identity to self
        jv  = new JnlpVersion("1.4");
        assertEquals (0,  jv.compareTo(jv));
        // all separators are equal
        jv2 = new JnlpVersion("1_4");
        assertEquals (0,  jv.compareTo(jv2));
        jv2 = new JnlpVersion("1-4");
        assertEquals (0,  jv.compareTo(jv2));
        // normalization, leading zeroes ignored if numeric
        jv2 = new JnlpVersion("1-04_0.000");
        assertEquals (0,  jv.compareTo(jv2));
    }
    public void testSorting() {
        // if not numeric, leading zeros are important
        jv  = new JnlpVersion("1.a");
        jv2 = new JnlpVersion("1.0a");
        assertTrue (jv.compareTo(jv2) > 0);
        assertTrue (jv2.compareTo(jv) < 0);
    }
    public void testPartialMatches() {
        jv  = new JnlpVersion("1.a");
        jv2 = new JnlpVersion("1.a.7");
        assertTrue  (jv.partialMatch(jv2));
        assertFalse (jv2.partialMatch(jv));

        jv  = new JnlpVersion ("4");
        jv2 = new JnlpVersion ("4.0.0");
        assertTrue  (jv.partialMatch(jv2));
        assertTrue  (jv2.partialMatch(jv));
    }
    public void testXLatticeVersion() {
        Version v = new Version();
        assertEquals ("org.xlattice.util", v.getPackage());
        int major = v.getMajor();
        int minor = v.getMinor();
        jv = new JnlpVersion (v);
        // DECIMAL PART TO THE VERSION NUMBER
        assertEquals (3, jv.size());
        assertEquals (String.valueOf(major), jv.get(0));
        assertEquals (String.valueOf(minor), jv.get(1));
    }
}
