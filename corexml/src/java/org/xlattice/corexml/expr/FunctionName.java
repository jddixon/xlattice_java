/* FunctionName.java */
package org.xlattice.corexml.expr;

import java.util.HashMap;

/**
 * Tokens representing the names of standard XPath 1.0 library 
 * functions.
 *
 * @author Jim Dixon
 */
public class FunctionName implements FixedNameToken {

    private final int index;
    /////////////////////////////////////////
    // XXX ADD ARGCOUNT AND ARRAY OF TYPES //
    // XXX NOT YET IMPLEMENTED             //
    // //////////////////////////////////////
    private /* final */ int argCount;
    private /* final */ Class[] argTypes;   // Double.TYPE, etc

    private static final HashMap map;

    // CORE FUNCTIONS ///////////////////////////////////////////////    
    public static final String[] NAMES = {
        // NODE SET 
        "count",
        "id",
        "last",
        "local-name",
        "name",
        "namespace-uri",
        "position",
        // STRING 
        "concat",
        "contains",
        "normalize-space",
        "starts-with",
        "string",
        "string-length",
        "substring",
        "substring-before",
        "substring-after",
        "translate",
        // BOOLEAN
        "false",
        "lang",
        "not",
        "true",
        // NUMBER 
        "ceiling",
        "floor",
        "number",
        "round",
        "sum"
    };
    static {
        map = new HashMap(NAMES.length);
        for (int i = 0; i < NAMES.length; i++)
            map.put (NAMES[i], new Integer(i));
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    // Efficiency is not important here; these are only constructed once.
    /**
     * Given its index, construct a function name token.
     */
    public FunctionName (int n) {
        if (n < 0 || n >= NAMES.length)
            throw new IllegalStateException (
                                "function name index out of range");
        index = n;
    }
    /** 
     * Given its name in String from, construct a function name
     * token.
     */
    public FunctionName (String name) {
        Integer i = (Integer)map.get(name);
        if (i == null)
            throw new IllegalArgumentException("not a function name");
        index = i.intValue();
    }        
    // FIXED NAME TOKEN /////////////////////////////////////////////
    /** @return a reference to the array of function names */
    public static String [] getNames() {
        return NAMES;
    }
    /** @return the index of this function name */
    public int getIndex() {
        return index;
    }
    /** @return this function name as a String */
    public String getName() {
        return NAMES[index];
    }
    // SERIALIZATION ////////////////////////////////////////////////
    /** @return this token in a form suitable for debug messages */
    public String toString() {
        return new StringBuffer("[funcname:").append(NAMES[index])
                .append("]").toString();
    }
}
