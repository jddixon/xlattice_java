/* Fs.java */
package org.xlattice.corexml.bind;

import java.util.ArrayList;

/**
 * Test class.
 *
 * @author Jim Dixon
 */
public class Fs {
    private ArrayList fCollection;
    
    protected Fs() { 
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
