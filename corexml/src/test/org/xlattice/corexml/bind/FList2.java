/* FList2.java */
package org.xlattice.corexml.bind;

import java.util.ArrayList;

/**
 * Store references to instances of class F in a list.
 *
 * @author Jim Dixon
 */
public class FList2 {
    private ArrayList fCollection;
    
    public FList2() { 
        fCollection = new ArrayList();
    }
    public void addF ( F f ) {
        fCollection.add(f);
    }
    public F getF (int n) {
        return (F) fCollection.get(n);
    }
    public int sizeF() {
        return fCollection.size();
    }
} 
