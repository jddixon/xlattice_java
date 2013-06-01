/* W.java */
package org.xlattice.corexml.bind;

import java.util.ArrayList;

/**
 * Test class with any number of Marker members.
 *
 * @author Jim Dixon
 */
public class W {
    private ArrayList<Marker> markers;
    private String attr1;
    private String attr2;

    protected W() { 
        markers = new ArrayList<Marker>();
    }
    public void addMarker ( Marker f ) {
        markers.add(f);
    }
    public Marker getMarker (int n) {
        return markers.get(n);
    }
    public int sizeMarkers() {
        return markers.size();
    }
    public String getAttr1 () { return attr1; }
    public String getAttr2 () { return attr2; }
    public void setAttr1(String s) { attr1 = s; }
    public void setAttr2(String s) { attr2 = s; }
} 
