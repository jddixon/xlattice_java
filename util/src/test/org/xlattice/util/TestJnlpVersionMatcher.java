/* TestJnlpVersionMatcher.java */
package org.xlattice.util;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestJnlpVersionMatcher extends TestCase {

    private JnlpVersionMatcher matcher;

    public TestJnlpVersionMatcher (String name) {
        super(name);
    }

    public void setUp () {
        matcher = null;
    }

    public void testEmpty() {
        String nullString = null;
        try {
            matcher = new JnlpVersionMatcher(nullString);
            fail("didn't catch null version string");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            matcher = new JnlpVersionMatcher("");
            fail("didn't catch empty version string");
        } catch (IllegalArgumentException iae) { /* success */ }
        try {
            matcher = new JnlpVersionMatcher(" \t ");
            fail("didn't catch whitespace-only version string");
        } catch (IllegalArgumentException iae) { /* success */ }
    }
    public void testSingletons() {
        matcher = new JnlpVersionMatcher("1.2");
        assertNotNull(matcher);
        assertEquals (1, matcher.size());
        assertEquals (JnlpVersionMatcher.EXACT, matcher.getType(0));
        assertEquals (0, new JnlpVersion("1.2")
                                .compareTo(matcher.getVersion(0)));

        matcher = new JnlpVersionMatcher("1-2+");
        assertNotNull(matcher);
        assertEquals (1, matcher.size());
        assertEquals (JnlpVersionMatcher.OR_BETTER, matcher.getType(0));
        assertEquals (0, new JnlpVersion("1.2")
                                .compareTo(matcher.getVersion(0)));

        matcher = new JnlpVersionMatcher("1-2.4*");
        assertNotNull(matcher);
        assertEquals (1, matcher.size());
        assertEquals (JnlpVersionMatcher.QMARK, matcher.getType(0));
        assertEquals (0, new JnlpVersion("1.2_4")
                                .compareTo(matcher.getVersion(0)));
    }
    /**
     * Verify that leading and trailing whitespace is ignored and
     * internal sequences of spaces and tabs constitute delimiters
     * between version numbers.
     */
    public void testNTuples() {
        matcher = new JnlpVersionMatcher("  1.2 \t3.4+   5.6.7* ");
        assertNotNull(matcher);
        assertEquals (3, matcher.size());
        assertEquals (JnlpVersionMatcher.EXACT, matcher.getType(0));
        assertEquals (0, new JnlpVersion("1.2")
                                .compareTo(matcher.getVersion(0)));
        assertEquals (JnlpVersionMatcher.OR_BETTER, matcher.getType(1));
        assertEquals (0, new JnlpVersion("3-4")
                                .compareTo(matcher.getVersion(1)));
        assertEquals (JnlpVersionMatcher.QMARK, matcher.getType(2));
        assertEquals (0, new JnlpVersion("5_6-7")
                                .compareTo(matcher.getVersion(2)));
   }
   public void testExactMatches() {
        matcher = new JnlpVersionMatcher("  1.2 ");
        assertTrue ( matcher.match("1-2") );
        assertTrue ( matcher.match("1-2.0.0") );
        assertFalse( matcher.match("1-2-a") );
        
        matcher = new JnlpVersionMatcher("  a.b 1-7 a.g");
        assertTrue ( matcher.match("001_007") );
        assertTrue ( matcher.match("a.g.0.0") );
   }
   public void testOrBetterMatches () {
        matcher = new JnlpVersionMatcher(" \t3.4+   ");
        assertTrue ( matcher.match("3.4") );
        assertTrue ( matcher.match("3.4a") );
        assertTrue ( matcher.match("3.04-a") );
        assertTrue ( matcher.match("3.005.4") );
        assertTrue ( matcher.match("4") );
        assertTrue ( matcher.match("A") );

        matcher = new JnlpVersionMatcher(" 1.2 \t3.4+   ");
        assertTrue ( matcher.match("1-02") );
        assertFalse( matcher.match("3.3") );
        assertTrue ( matcher.match("03.5.4") );
        assertTrue ( matcher.match("7Q") );
   }
   public void testPartialMatches () {
        matcher = new JnlpVersionMatcher(" 5.6.7* ");
        assertFalse( matcher.match("5.6") );
        assertTrue ( matcher.match("5.6.7") );
        assertTrue ( matcher.match("5.6.7.z") );
   }
}
