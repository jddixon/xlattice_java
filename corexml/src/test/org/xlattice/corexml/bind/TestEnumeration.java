/* TestEnumeration.java */
package org.xlattice.corexml.bind;

import java.io.StringReader;

import junit.framework.*;

import org.xlattice.corexml.CoreXmlException;

/**
 * @author Jim Dixon
 */

public class TestEnumeration extends TestCase {

    // INSTANCE VAR /////////////////////////////////////////////////
    Mapping map;
    
    // TEST CASE CONSTRUCTOR ////////////////////////////////////////
    public TestEnumeration (String name) {
        super(name);
    }

    // TESTS ////////////////////////////////////////////////////////
    public void setUp () {
    }

    
    public void tearDown() {
        map = null;
    }

    public void testDoubleFixed()           throws CoreXmlException {
        try {
            map   = new Mapping ("w", "org.xlattice.corexml.bind.W");
            map.add( new AttrBinding("attr1") .fixed("foo") .fixed("bar") );
            map.add( new AttrBinding("attr2") .values("def:val1,val2,val3") );
            map.join();
            fail("didn't detect fixed occurs twice");
        } catch ( CoreXmlException ce ) { /* success */ }
    }
    public void testDoubleDefault()         throws CoreXmlException {
        try {
            map   = new Mapping ("w", "org.xlattice.corexml.bind.W");
            map.add( new AttrBinding("attr1") .fixed("foo") );
            map.add( new AttrBinding("attr2") .values("def:val1:val2,val3") );
            map.join();
            fail("didn't detect fixed occurs twice");
        } catch ( CoreXmlException ce ) { /* success */ }
    }
    
    String xmlHeader  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    
    public void testFoo ()                  throws CoreXmlException {
        map   = new Mapping ("w", "org.xlattice.corexml.bind.W");
        map.add( new AttrBinding("attr1") .fixed("foo") );
        map.add( new AttrBinding("attr2") .values("def:val1,val2,val3") );
        map.join();
    } 
}
