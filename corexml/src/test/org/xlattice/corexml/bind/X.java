/* X.java */
package org.xlattice.corexml.bind;

/**
 * Test class.
 *
 * @author Jim Dixon
 */
public class X                              implements Marker {
    private String  attr3;
    private String  valueX;

    public X () {
        // nothing to do
    }
    public String getAttr3() {
        return attr3;
    }
    public void setAttr3 (String s) { 
        attr3 = s;
    }
    public String getValueX() {
        return valueX;
    }
    public void setValueX (String s) {
        valueX = s;
    }
} 
