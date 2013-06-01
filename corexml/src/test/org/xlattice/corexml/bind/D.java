/* D.java */
package org.xlattice.corexml.bind;

import java.util.ArrayList;

/**
 * Test class.
 *
 * @author Jim Dixon
 */
public class D {
    private String field3;
    private String fieldE;
    private ArrayList fCollection;
    private String textField;
    
    protected D() { 
        fCollection = new ArrayList();
    }

    public String getField3() {
        return field3;
    }
    public void setField3(String s) {
        field3 = s;
    }
    public String getFieldE() {
        return fieldE;
    }
    public void setFieldE(String s) {
        fieldE = s;
    }
    public String getTextField() {
        return textField;
    }
    public void setTextField(String s) {
        textField = s;
    }
    // the collection /////////////////////////////////
    public void addF ( F f ) {
        fCollection.add(f);
    }
    public F getF (int n) {
        // RANGE CHECK
        return (F) fCollection.get(n);
    }
    public int sizeF() {
        return fCollection.size();
    }
} 
