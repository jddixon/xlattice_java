/* Template.java */
package org.xlattice;

import org.xlattice.Context;

/**
 * XXX THIS DOES NOT CONFORM TO THE IMPLEMENTATION IN PROGRESS. 
 *
 * @author Jim Dixon
 */
public interface Template {

    /** The Template is an irreducible byte array. */
    public static final int TPL_BINARY  = 1;

    /** The Template is an irreducible String. */
    public static final int TPL_STRING  = 2;

    /** 
     * The Template is a String which should be looked up in the Context
     * but is otherwise irreducible.  That is, any ${..} constructs 
     * within the String will not be translated, they will be left
     * unchanged.
     */
    public static final int TPL_VAR     = 3;

    /** 
     * The Template consists of two Strings, the first of which should
     * resolve in the Context to an object and the second of which is 
     * the name of a field in the class of which the object is an instance.
     */
    public static final int TPL_ATTR    = 4;

    /**
     * The Template contains a reference to a Template.
     */
    public static final int TPL_REF     = 5;

    /**
     * The Template is a String which should resolve to a Template
     * in the Context.  That is, this Template is a runtime reference
     * to a Template by name.
     */
    public static final int TPL_BY_NAME = 6;

    /**
     * A Template mapping consisting of (1) a name referring to 
     * one or more values and (2) a reference to a Template.
     *
     * XXX The Template should have a single unbound variable.  
     */
    public static final int TPL_MAP     = 7;

    /**
     * A Template application, a data structure consisting of (1) a 
     * reference to the Template being applied and (2) a list of 
     * name-value pairs.
     *
     * XXX The number of NV pairs in the list should be equal to 
     * XXX the number of Template parameters, and the names should 
     * XXX match.
     */
    public static final int TPL_APPLY   = 8;
    
    /**
     * A conditional Template consisting of a String value <code>test</code>
     * and a reference to a Template.  If <code>test</code> resolves to 
     * <code>true</code> in the Context, the Template is resolved in the
     * Context.  If <code>test</code> is undefined or is defined but does
     * not resolve to <code>true</code> the Template is ignored.
     */
    public static final int TPL_IF      = 9;

    /**
     * A conditional Template consisting of a String value <code>test</code>
     * and references to two Templates.  If <code>test</code> resolves to
     * <code>true</code> in the Context, the first Template is resolved in
     * the Context.  Otherwise the second Template is resolved.
     */
    public static final int TPL_IF_ELSE = 10;

    /**
     * A Template consisting of a number of Templates.  An ordered set,
     * a sequence.
     */
    public static final int TPL_SEQ     = 11;
    
    /** @return one of the values specified above. */
    public int getType();

    /** 
     * Recursively reduce all constituent Templates in the Context to
     * Strings, concatenate them, and return the result.
     */
    public String toString(Context ctx);

    /** 
     * Recursively reduce all constituent Templates in the Context to
     * byte arrays, concatenate them, and return the result.
     */
    public byte[] getBytes(Context ctx);
}
