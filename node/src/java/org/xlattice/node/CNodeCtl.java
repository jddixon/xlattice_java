/* CNodeCtl.java */
package org.xlattice.node;

/**
 * Control block used by Runner to manage a CNode, an XLattice node
 * running on a host reachable by UDP (in this implementation).  Either
 * the runner or the CNode or both may be behind a NAT.
 * 
 * This could almost certainly subclass CtlBlock, with JNodeCtl as a
 * sibling subclass.
 *
 * @author Jim Dixon
 */

public class CNodeCtl {


    // XXX need control channel = NATtedAddress through which we send 
    // xl messages; this may be local or on the open Internet.

    // XXX need at least an int representing the node's state

    public CNodeCtl() {
        /* STUB */

    }

    // EQUALS, HASHCODE /////////////////////////////////////////////
    public boolean equals(Object o) {
        /* STUB */
        return o == this;
    }
    public int hashCode() {
        /* STUB */
        return -1;
    }
}
