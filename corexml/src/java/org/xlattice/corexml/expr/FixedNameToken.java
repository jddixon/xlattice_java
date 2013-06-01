/* FixedNameToken.java */
package org.xlattice.corexml.expr;

/**
 * A token with an index into a table of fixed (well-known) names.
 *
 * @author Jim Dixon
 */
public interface FixedNameToken extends Token {

    /** @return the index associated with the token */
    public int getIndex();
    /** @return the String corresponding to the token's index */
    public String getName();
        
}
