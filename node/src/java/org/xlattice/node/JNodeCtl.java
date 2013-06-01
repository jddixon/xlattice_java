/* JNodeCtl.java */
package org.xlattice.node;

/**
 * Control block used by Runner to manage a JNode running in a separate
 * JVM but on the same host.
 * 
 * This could almost certainly subclass CtlBlock, with CNodeCtl as a
 * sibling subclass.
 *
 * @author Jim Dixon
 */

public class JNodeCtl {


    // XXX need control channel = UDP port through which we send 
    // xl messages

    // XXX need at least an int representing the node's state

    public JNodeCtl() {
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
