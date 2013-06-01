/* Talker.java */
package org.xlattice.corexml.om;

/** 
 * XXX FOR DEBUGGING; INCOMPLETE 
 *
 * @author Jim Dixon
 */
public class Talker implements Visitor {
    public Talker () {}
    public void onEntry (Node n) {
        if (n.isElement())
            System.out.println(" enter element " 
                    + ((Element)n).getName() );

        else if (n.isDocument())
            System.out.println(" entering document");
        else 
            System.out.println(" *UNKNOWN NODE TYPE*");
    }
    public void onExit (Node n) {
    }
} 
