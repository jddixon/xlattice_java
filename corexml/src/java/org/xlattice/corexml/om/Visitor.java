/* Visitor.java */
package org.xlattice.corexml.om;

/**
 * Interface for classes implementing the Visitor pattern.
 * 
 * @author Jim Dixon
 */
public interface Visitor {

    /**
     * Action taken by a visitor on arriving at a Node.
     */
    public void onEntry (Node n) throws RuntimeException;

    /**
     * Action taken by the visitor on leaving the Node.
     */
    public void onExit (Node n)  throws RuntimeException;
}
