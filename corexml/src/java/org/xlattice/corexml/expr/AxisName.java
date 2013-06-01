/* AxisName.java */
package org.xlattice.corexml.expr;

/**
 * A token representing an XPath 1.0 axis name.
 * <p/>
 * NOTE that the String names use underscores (_) instead of dashes (-).
 *
 * @author Jim Dixon
 */
public class AxisName implements FixedNameToken {

    /** index into the table below */
    private final int type;

    public static final int ANCESTOR            =  0;
    public static final int ANCESTOR_OR_SELF    =  1;
    public static final int ATTRIBUTE           =  2;
    public static final int CHILD               =  3;
    public static final int DESCENDENT          =  4;
    public static final int DESCENDENT_OR_SELF  =  5;
    public static final int FOLLOWING           =  6;
    public static final int FOLLOWING_SIBLING   =  7;
    public static final int NAMESPACE           =  8;
    public static final int PARENT              =  9;
    public static final int PRECEDING           = 10;
    public static final int PRECEDING_SIBLING   = 11;
    public static final int SELF                = 12;
  
    /** @return whether an integer might represent a valid axis name */
    public static boolean indexInRange (int i) {
        return (i >= ANCESTOR) && (i <= SELF);
    }

    /** array of axis names in String form */
    public static final String [] NAMES = {
        "ancestor",     "ancestor-or-self",     "attribute",
        "child",        "descendent",           "descendent-or-self",
        "following",    "following-sibling",    "namespace",
        "parent",       "preceding",            "preceding-sibling",
        "self"};

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create an axis name, specifying its index.
     */
    public AxisName (int index) {
        if (index < 0 || index > SELF)
            throw new IllegalStateException("invalid axis index");
        type = index;
    }
    /**
     * Create an axis name, specifying the name.
     */
    public AxisName (String name) {
        type = getAxisNameIndex(name);
        if (type < 0)
            throw new IllegalStateException("no such axis name");
    }
    // STATIC METHODS ///////////////////////////////////////////////
    /** 
     * Return the index of the axis name or -1 if it is not a valid 
     * name.  This implementation is quite inefficient.  
     */
    public static int getAxisNameIndex(String name) {
        for (int i = 0; i <= SELF; i++) {
            if (name.equals(NAMES[i])) {
                return i;
            }
        }
        return -1;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the axis name index for this token */
    public int getIndex() {
        return type;
    }
    /** @return the axis name represented by this token */
    public String getName() {
        return NAMES[type];
    }
    // SERIALIZATION ////////////////////////////////////////////////
    /** @return a String useful in debugging */
    public String toString() {
        return new StringBuffer()
            .append("[axis:").append(NAMES[type]).append("]").toString();
    }
}
