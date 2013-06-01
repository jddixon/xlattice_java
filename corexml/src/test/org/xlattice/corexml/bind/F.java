/* F.java */
package org.xlattice.corexml.bind;

/** 
 * Test class, a Java class corresponding an an XML element tagF 
 * which contains text, which corresponds to the fValue field in
 * the class.
 *
 * The Marker interface allows this to be used in testing the
 * Interface construct.
 *
 * @author Jim Dixon
 */
public class F                              implements Marker {
    String fValue;
    protected F () {}
    public String getFValue() {
        return fValue;
    }
    public void setFValue (String f) {
        fValue = f;
    }
} 
