/* FList1.java */
package org.xlattice.corexml.bind;

import java.util.ArrayList;

/**
 * Store String values in a list.
 *
 * @author Jim Dixon
 */
public class FList1 {
    private ArrayList fCollection;
    
    public FList1() { 
        fCollection = new ArrayList();
    }
    public void addF (String f) {
        fCollection.add(f);
    }
    public String getF (int n) {
        return (String) fCollection.get(n);
    }
    public int sizeF() {
        return fCollection.size();
    }
} 
