/* A.java */
package org.xlattice.corexml.bind;

/**
 * Test class.
 *
 * @author Jim Dixon
 */
public class A {
    private String field1;
    private String field2;
    private boolean bPresent;
    private String fieldC;
    private D      theD;
    private String fieldG;

    public A () {
        // nothing to do
    }
    public String getField1() {
        return field1;
    }
    public void setField1 (String s) { 
        field1 = s;
    }
    public String getField2() {
        return field2;
    }
    public void setField2 (String s) {
        field2 = s;
    }
    public boolean isBPresent() {
        return bPresent;
    }
    public void setBPresent(boolean b) {
        bPresent = b;
    }
    
    public String getFieldC() {
        return fieldC;
    }
    public void setFieldC (String s) {
        fieldC = s;
    } 
    public D getTheD() {
        return theD;
    }
    public void setTheD(D d) {
        theD = d;
    }
    /** get the CDATA field */
    public String getFieldG() {
        return fieldG;
    } 
    /** set the CDATA field */
    public void setFieldG(String s) {
        fieldG = s;
    }
} 
