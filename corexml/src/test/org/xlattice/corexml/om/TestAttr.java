/* TestAttr.java */
package org.xlattice.corexml.om;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestAttr extends TestCase {

    private Attr attr;

    public TestAttr (String name) {
        super(name);
    }

    public void setUp () {
        attr = null;
    }

    public void tearDown() {
    }
 
    public void testBooleans() {
        Attr x = new Attr("abc","def");
        assertNotNull(x);
        assertTrue (x.isAttr());
        assertFalse(x.isComment());
        assertFalse(x.isDocument());
        assertFalse(x.isDocType());
        assertFalse(x.isElement());
        assertFalse(x.isProcessingInstruction());
        assertFalse(x.isText());
    }
    public void testEmptyDoc() {
        attr = new Attr("georgieBoy", "the big guy");
        assertNotNull(attr);
        assertEquals("georgieBoy", attr.getName());
        assertEquals("the big guy", attr.getValue());
        assertEquals(" georgieBoy=\"the big guy\"", attr.toXml());
    }
    public void testPrefixedAttrs() {
        attr = new Attr("a", "b", "c");
        assertEquals(" a:b=\"c\"", attr.toXml());
    }
}
