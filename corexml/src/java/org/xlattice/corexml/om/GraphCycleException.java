/* GraphCycleException.java */
package org.xlattice.corexml.om;

/** 
 * Exception returned if a Document is found to contain graph
 * cycles, that is, if a node is found to be its own parent.
 *
 * @author Jim Dixon
 */
public class GraphCycleException extends RuntimeException {
    /** No-argument constructor. */
    public GraphCycleException () {
        super();
    }
    /** Constructor taking a single String argument. */
    public GraphCycleException (String msg) {
        super(msg);
    }
}
