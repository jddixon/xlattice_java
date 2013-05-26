/* Term.java */
package org.xlattice.util.context;

import org.xlattice.Context;

/**
 * A context Term is part of an expression, a Literal or a Symbol,
 * where a literal is something that cannot be resolved further,
 * and a Symbol is a substring bracketed by ${ and }, which can
 * be resolved.
 *
 * @author Jim Dixon
 */
public interface Term {
    /**
     * Evaluate the term within the context.
     * @return what the term evaluates to as a String
     */
    public String resolve (Context ctx);
    /** @return offset of the first character of the term */
    public int from();
    /** @return offset of the first character beyond the term */
    public int to();
}
