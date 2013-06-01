/* TestCdata.java */
package org.xlattice.corexml.om;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestCdata extends TestCase {

    private Cdata cdata;

    public TestCdata (String name) {
        super(name);
    }

    public void setUp () {
        cdata = null;
    }

    public void tearDown() {
    }
 
    public void testBooleans() {
        Cdata x = new Cdata("abc");
        assertNotNull(x);
        assertFalse(x.isAttr());
        assertTrue (x.isCdata());
        assertFalse(x.isComment());
        assertFalse(x.isDocument());
        assertFalse(x.isDocType());
        assertFalse(x.isElement());
        assertFalse(x.isProcessingInstruction());
        assertTrue (x.isText());
    }
    public void testSimpleCDATA() {
        cdata = new Cdata("the big guy");
        assertNotNull(cdata);
        assertEquals("the big guy", cdata.getText());
        assertEquals("<![CDATA[the big guy]]>\n", cdata.toXml());
    }
}
