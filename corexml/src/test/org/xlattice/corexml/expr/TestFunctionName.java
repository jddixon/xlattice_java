/* TestFunctionName.java */
package org.xlattice.corexml.expr;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestFunctionName extends TestCase {

    public TestFunctionName (String name) {
        super(name);
    }
    public void setUp () { }

    public void tearDown() { }
 
    public void testMapping() {
        int count = FunctionName.NAMES.length;
        for (int i = 0; i < count; i++) {
            String name = FunctionName.NAMES[i];
            FunctionName fn = new FunctionName(name);
            assertEquals (name, fn.getName());
        }
    }
}
