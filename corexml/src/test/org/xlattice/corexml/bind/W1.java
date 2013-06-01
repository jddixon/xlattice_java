/* W1.java */
package org.xlattice.corexml.bind;

import java.util.ArrayList;

/**
 * Test class. A version of W.java with either zero or one Marker
 * members.
 *
 * @author Jim Dixon
 */
public class W1                                 extends W {
    private Marker marker;
    private String attr1;
    private String attr2;

    protected W1() { 
    }
    public void setMarker ( Marker f ) {
        marker = f;
    }
    public Marker getMarker() {
        return marker;
    }
    public String getAttr1 () { return attr1; }
    public String getAttr2 () { return attr2; }
    public void setAttr1(String s) { attr1 = s; }
    public void setAttr2(String s) { attr2 = s; }
} 
