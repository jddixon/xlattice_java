/* JnlpVersion.java */
package org.xlattice.util;

/**
 * Versions for Webstart (JNLP) as specified in Sun's JSR-056,
 * "Java Network Launching Protocol".  For compatibility with 
 * JNLP, XLattice version numbers should conform to JSR 56.  To
 * improve understandability, they should conform to the stricter 
 * syntax described in the next paragraphs.
 * <p>
 * JNLP version numbers are strings consisting of a number of 
 * parts (substrings) delimited by separators.  While a number of
 * separators must be recognized (any of ".-_"), only the dot ('.')
 * should be used.  Similarly, while this implementation will trim
 * off leading and trailing white space, in practice version numbers
 * should not have any.  
 *
 * The specification permits any non-separator character to be used
 * in the parts.  However, in practice, only digits and alphabetical
 * characters should be used.  Certainly whitespace characters and
 * non-printing characters should never be used in parts.
 *
 * Preferably all parts will be entirely numeric; if it seems 
 * necessary to use alphabetic characters, they should if at all 
 * possible confined to the last part.
 * 
 * @author Jim Dixon
 */
public class JnlpVersion {

    private final String version;   // sanitized
    private final String [] parts;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create a JNLP version from a String consisting of a number of
     * parts separated by dot, dash, or underscore (".-_") as 
     * required by JSR 56.  The initialization String is trimmed.
     * 
     * Syntactically the initializer is 
     *   part (separator part)* 
     * where the 'part' substrings consist of any characters other
     * than the separators.  JSR 56 does not prohibit modifiers,
     * star (*) or plus (+), from appearing in the parts, but in
     * practice they should be avoided.
     * 
     * @param s JNLP version in String form
     */
    public JnlpVersion (String s) {
        if (s == null || (version = s.trim()).equals(""))
            throw new IllegalArgumentException(
                    "version is null or empty String");
        if (version.indexOf(' ') >= 0 || version.indexOf('\t') >= 0)
            throw new IllegalArgumentException(
                    "embedded whitespace in version string");
        if (version.indexOf(' ') >= 0 || version.indexOf('\t') >= 0)
            throw new IllegalArgumentException(
                    "embedded whitespace in version string");
        parts = version.split("[\\._-]", Integer.MAX_VALUE);
        for (int i = 0; i < parts.length; i++)
            if (parts[i].equals("")) 
                throw new IllegalArgumentException(
                        "leading, trailing, or duplicate separator");
    }
    /**
     * Create a JNLP version number from an XLattice version number.
     * If the decimal part of the XLattice version number is negative,
     * it is ignored.  The build number is always ignored.
     */
    public JnlpVersion (org.xlattice.Version v) {
        this ( (v.getDecimal() >= 0) ? 
                new StringBuffer()
                .append(v.getMajor()).append(".")
                .append(v.getMinor()).append(".")
                .append(v.getDecimal()).toString() 
                :
                new StringBuffer()
                .append(v.getMajor()).append(".")
                .append(v.getMinor()).toString() 
        );

    }
    // PROPERTIES ///////////////////////////////////////////////////
    /**
     * Returns the Nth part of a JNLP version number.
     * 
     * @param n zero-based index into the parts
     * @return  Nth part of the version, as a String
     */
    public String get (int n) {
        return parts[n];
    }
    /**
     * The number of parts.
     * 
     * @return the number of parts in the version string
     */
    public int size () {
        return parts.length;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Compare two parts as numbers if possible, otherwise 
     * lexigraphically.  If the first argument sorts before the
     * second, a negative value is returned.  If they are the
     * same, zero is returned.  Otherwise a positive value is
     * returned.  The arguments should not contain delimiters
     * or separators, but there is no check on this.
     * 
     * @param lhs first value in comparison
     * @param rhs second value in comparison
     * @return    ordering as described above
     */
    protected static int compareParts (String lhs, String rhs) {
        try {
            int left  = Integer.parseInt(lhs);
            int right = Integer.parseInt(rhs);
            if (left < right) 
                return -1;
            if (left > right)
                return 1;
            return 0;
        } catch (NumberFormatException e) {
            return lhs.compareTo(rhs);
        }
    }
    /**
     * Compare this to another version numerically if possible, otherwise
     * lexigraphically.  The versions have been split into parts around
     * separators (dot, dash, underscore; ".-_") which are not themselves
     * considered in determining ordering. If one version string has more
     * parts than another, the shorter is in effect normalized by padding
     * it out with "0" parts.
     *
     * @param rhs JNLP version being compared to
     * @return    the ordering described under compareParts(String, String)
     */
    public int compareTo (JnlpVersion rhs) {
        if (rhs == null || rhs.size() == 0)
            throw new IllegalArgumentException ("null or empty argument");
        int argLen = rhs.size();
        // we want the shorter of the two
        int len = (argLen <= parts.length) ? argLen : parts.length;
        int i;
        int comparison;
        for (i = 0; i < len; i++) {
            comparison = compareParts (parts[i], rhs.get(i));
            if (comparison != 0)
                return comparison;
        }
        if (parts.length > argLen) {
            for ( ; i < parts.length; i++) {
                comparison = compareParts (parts[i], "0");
                if (comparison != 0)
                    return comparison;
            }
        } else if (parts.length < argLen) {
            for ( ; i < argLen; i++) {
                comparison = compareParts ("0", rhs.get(i));
                if (comparison != 0)
                    return comparison;
            }
        }
        return 0;
    }
    /**
     * Determine whether the candidate rhs version is an exact match
     * to this version, without normalizing beyond the length of 
     * this version.
     */
    public boolean partialMatch (JnlpVersion rhs) {
        if (rhs == null || rhs.size() == 0)
            return false;
        int argLen = rhs.size();
        // we want the shorter of the two
        int len = (argLen <= parts.length) ? argLen : parts.length;
        int i;
        int comparison;
        for (i = 0; i < len; i++) {
            if (compareParts (parts[i], rhs.get(i)) != 0)
                return false;
        }
        if (parts.length > argLen) {
            for ( ; i < parts.length; i++) {
                if (compareParts (parts[i], "0") != 0)
                    return false;
            }
        } 
        return true;
    }
    /**
     * Return the original String form of the version number.
     * Leading and trailing whitespace is trimmed off, but the
     * separators are unchanged.
     * 
     * @return string passed to the constructor, trimmed */
    public String toString() {
        return version;
    }
}
