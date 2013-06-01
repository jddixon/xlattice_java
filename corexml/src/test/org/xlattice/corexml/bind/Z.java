/* Z.java */
package org.xlattice.corexml.bind;

/**
 * Test class.
 *
 * @author Jim Dixon
 */
public class Z                              implements Marker {
    private boolean attr5;
    private String  valueZ;

    public Z () {
        // nothing to do
    }
    public boolean isHyphAttr() {
        return attr5;
    }
    public void setHyphAttr (boolean s) { 
        attr5 = s;
    }
    public String getValueZ() {
        return valueZ;
    }
    public void setValueZ (String s) {
        valueZ = s;
    }
} 
