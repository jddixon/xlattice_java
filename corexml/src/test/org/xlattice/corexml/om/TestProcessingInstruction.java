/* TestProcessingInstruction.java */
package org.xlattice.corexml.om;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestProcessingInstruction extends TestCase {

    private ProcessingInstruction pi;

    public TestProcessingInstruction (String name) {
        super(name);
    }

    public void setUp () {
        pi = null;
    }

    public void tearDown() {
    }
 
    public void testBooleans() {
        ProcessingInstruction x = new ProcessingInstruction("abc", "def");
        assertNotNull(x);
        assertFalse(x.isAttr());
        assertFalse(x.isComment());
        assertFalse(x.isDocument());
        assertFalse(x.isDocType());
        assertFalse(x.isElement());
        assertTrue (x.isProcessingInstruction());
        assertFalse(x.isText());
    }
    public void testSimplePI() {
        pi = new ProcessingInstruction("perl", "chomp;");
        assertNotNull(pi);
        assertEquals("perl",   pi.getTarget());
        assertEquals("chomp;", pi.getText());
        assertEquals("<?perl chomp;?>\n", pi.toXml());
    }
}
