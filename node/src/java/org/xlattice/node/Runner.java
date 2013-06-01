/* Runner.java */
package org.xlattice.node;

import java.io.IOException;
import java.util.ArrayList;

/**
 * XXX NEEDS A PER-INSTANCE LOG
 *
 * @author Jim Dixon
 */

public class Runner                             extends TNode {

    protected final ArrayList tnodes;
    protected final ArrayList jnodes;
    protected final ArrayList cnodes;

    public Runner()                             throws IOException {

        tnodes = new ArrayList();
        jnodes = new ArrayList();
        cnodes = new ArrayList();

    }

    // PROPERTIES ///////////////////////////////////////////////////
    public int sizeTNodes() { return tnodes.size(); }
    public void addTNode( TNode t ) {
        if (t == null) 
            throw new IllegalArgumentException("null TNode");
        tnodes.add(t);
    }
    public TNode getTNode(int n) {
        return (TNode) tnodes.get(n);
    }
    
    public int sizeJNodes() { return jnodes.size(); }
    public void addJNode( JNodeCtl j ) {
        if (j == null) 
            throw new IllegalArgumentException("null JNodeCtl");
        jnodes.add(j);
    }
    public JNodeCtl getJNode(int n) {
        return (JNodeCtl) jnodes.get(n);
    }
   
    
    public int sizeCNodes() { return cnodes.size(); }
    public void addCNode( CNodeCtl t ) {
        if (t == null) 
            throw new IllegalArgumentException("null CNodeCtl");
        cnodes.add(t);
    }
    public CNodeCtl getCNode(int n) {
        return (CNodeCtl) cnodes.get(n);
    }


    // COMMAND LINE INTERFACE ///////////////////////////////////////

    public static void main(String[] args) {


    }

}
