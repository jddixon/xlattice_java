/* NCName.java */
package org.xlattice.corexml.expr;

/**
 * Token representing a non-colonized XML name used as a prefix.  
 *
 * @author Jim Dixon
 */
public class NCName implements Token {

    /** the NCname; it will represent a prefix and contain no colons */
    private String name;

    /**
     * @throws NullPointerException if name is null
     */
    public NCName (String s) {
        if (s == null) 
            throw new NullPointerException("null name");
        name = s;
    }

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the NCName (prefix) */
    public String getName() {
        return name;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    public String toString() {
        return name;
    }
}
