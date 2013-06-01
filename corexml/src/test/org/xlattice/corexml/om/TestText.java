/* TestText.java */
package org.xlattice.corexml.om;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestText extends TestCase {

    private Text text;

    public TestText (String name) {
        super(name);
    }

    public void setUp () {
        text = null;
    }

    public void tearDown() {
    }
 
    public void testBooleans() {
        Text x = new Text("abc");
        assertNotNull(x);
        assertFalse(x.isAttr());
        assertFalse(x.isComment());
        assertFalse(x.isDocument());
        assertFalse(x.isDocType());
        assertFalse(x.isElement());
        assertFalse(x.isProcessingInstruction());
        assertTrue (x.isText());
    }
    public void testSingleText() {
        text = new Text("the big guy");
        assertNotNull(text);
        assertEquals("the big guy", text.getText());
        assertEquals("the big guy", text.toXml());
    }
}
