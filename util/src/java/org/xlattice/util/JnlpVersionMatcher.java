/* JnlpVersionMatcher.java */
package org.xlattice.util;

/**
 * Specifier for a set of JNLP-compatible version numbers and 
 * the type of matches permitted.  JNLP (Webstart, JSR056)
 * version numbers consist of a number of parts delimited by
 * any of a set of separators (dot, dash, or underscore: ".-_").
 * The parts themselves may consist of any character not in the
 * set of delimiters.
 * <p>
 * Each possible match is specified as a JNLP version number with 
 * an optional modifier, star (*) or plus (+), indicating the type 
 * of match permitted.  If there is no modifier, the match must be 
 * exact.  Separator characters are not considered in comparisons,
 * so <code>1-2_3</code> is an exact match for <code>1.2.3</code>.
 * <p>
 * If the + modifier is present, any version number which is an
 * exact match for or sorts after the version number in the pattern
 * will succeed.  In making this comparison between version numbers
 * with differing numbers of parts, the shorter version number is
 * in effect padded out with zeroes.  Numeric comparison is used
 * if possible, but if either of the pair of parts being compared contains
 * non-digit characters, the two parts are compared lexigraphically.
 * Given these considerations, <code>4.1</code> is an exact match
 * for <code>4.1.0.0</code>,sorts before <code>4.A</code>, and 
 * sorts after <code>4./</code>, because the character '/' precedes
 * the character '1' in sort order whereas 'A' follows it.
 * <p>
 * If the * modifier is present, version numbers match if they are
 * identical or if they have different numbers of parts but the
 * initial parts of the longer version number are identical to all
 * of the corresponding parts of the shorter version number.
 * <p>
 * Within the version matcher initialization string, version numbers
 * are separated by spaces.  There must be no delimiter between the
 * version number and modifier (*+), if a modifier is present.
 * <p>
 * As an example:
 * <pre>
 *   1.2.3 4_5_6* 7-8-9+
 * </pre>
 * would match <code>1-2-3</code> (exactly), <code>4.5.6.7</code> 
 * (as a partial match), or <code>10_7</code> (because it sorts after 
 * <code>7.8.9</code>.
 * 
 * @author Jim Dixon
 */
public class JnlpVersionMatcher {

    /** exact match between version numbers requird */
    public static final int EXACT     = 0;
    /** version number matches or sorts after the current version number*/
    public static final int OR_BETTER = 1;
    /** partial match; requires exact match but only on parts specified */
    public static final int QMARK     = 2;
    /** set of candidate version numbers to match */
    private final JnlpVersion [] versions;
    /** type for each candidate version (EXACT, OR_BETTER, QMARK) */
    private final int [] matchTypes;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create a matcher for JNLP (Webstart, JSR 56) versions.  This
     * is a space-separated list of JNLP version numbers, each with
     * optional specifiers (+ or *). Leading and trailing whitespace is 
     * ignored and any series of one or more spaces or tabs is treated
     * as a single delimitor; both are extensions to the JNLP specs.
     * 
     * @param s String containing one or more JNLP version descriptors.  
     */
    public JnlpVersionMatcher (String s) {
        String trimmed;
        if (s == null || (trimmed = s.trim()).equals(""))
            throw new IllegalArgumentException ( 
                    "null or empty version list");
        // split around any sequences of spaces or tabs 
        //   into constituent version strings
        String[] elements = trimmed.split("[ \\t]+");
        int count = elements.length;
        versions = new JnlpVersion[count];
        matchTypes    = new int[count];
        for (int i = 0; i < count; i++) {
            String elm = elements[i];
            if (elm.length() < 1)
                throw new IllegalArgumentException (
                        "version matcher element is empty");
            char lastChar = elm.charAt(elm.length() - 1);
            if (lastChar == '+')
                matchTypes[i] = OR_BETTER;
            else if (lastChar == '*')
                matchTypes[i] = QMARK;
            else 
                matchTypes[i] = EXACT;
            if (matchTypes[i] == EXACT)
                versions[i] = new JnlpVersion (elm);
            else {
                if (elm.length() < 2)
                    throw new IllegalArgumentException (
                        "version matcher element contains only modifier");
                versions[i] = new JnlpVersion (
                                elm.substring (0, elm.length() - 1));
            }
        }
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /**
     * Return the version part of the Nth version element.  This is 
     * the JNLP version number, without any optional modifier (+ or *).
     *
     * @param n zero-based index
     * @return  the JNLP version of the Nth element
     */
    public JnlpVersion getVersion (int n) {
        return versions[n];
    }
    /**
     * Returns the type of matching (the modifier) for the Nth version 
     * matcher.  This is one of EXACT, OR_BETTER, QMARK, for exact, 
     * exact or better, and partial matches respectively.
     * 
     * @return matching type for the Nth version
     */
    public int getType (int n) {
        return matchTypes[n];
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Whether there is a match between the candidate version number
     * and ANY of the elements of this matcher.  Each of the matcher
     * elements is checked in turn.  If any matches, <code>true</code> 
     * is returned immediately.
     *
     * @param v JNLP version number being tested for a match
     */
    public boolean match (JnlpVersion v) {
        for (int i = 0; i < versions.length; i++) {
            int type = matchTypes[i];
            if (type == EXACT && versions[i].compareTo(v) == 0)
                return true;
            else if (type == OR_BETTER && versions[i].compareTo(v) <= 0)
                return true;
            else if (type == QMARK && versions[i].partialMatch(v))
                return true;
        }
        return false;
    }
    /**
     * Whether there is a match between a JNLP version number 
     * specified as a String and ANY of the elements of this
     * matcher.
     *
     * @param s JNLP version number in Strng form
     */
    public boolean match (String s) {
        return match (new JnlpVersion(s));
    }
    /**
     * @return the number of JnlpVersions in the matcher
     */
    public int size() {
        return versions.length;
    }
}
