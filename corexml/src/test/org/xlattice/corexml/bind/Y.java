/* Y.java */
package org.xlattice.corexml.bind;

/**
 * Test class.
 *
 * @author Jim Dixon
 */
public class Y                              implements Marker {
    private int     attr4;
    private String  valueY;

    public Y () {
        // nothing to do
    }
    public int getAttr4() {
        return attr4;
    }
    public void setAttr4 (int s) { 
        attr4 = s;
    }
    public String getValueY() {
        return valueY;
    }
    public void setValueY (String s) {
        valueY = s;
    }
} 
