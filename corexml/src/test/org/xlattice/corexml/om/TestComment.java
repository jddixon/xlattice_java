/* TestComment.java */
package org.xlattice.corexml.om;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestComment extends TestCase {

    private Comment comment;

    public TestComment (String name) {
        super(name);
    }

    public void setUp () {
        comment = null;
    }

    public void tearDown() {
    }
 
    public void testBooleans() {
        Comment x = new Comment("abc");
        assertNotNull(x);
        assertFalse(x.isAttr());
        assertTrue (x.isComment());
        assertFalse(x.isDocument());
        assertFalse(x.isDocType());
        assertFalse(x.isElement());
        assertFalse(x.isProcessingInstruction());
        assertFalse(x.isText());
    }
    public void testEmptyDoc() {
        comment = new Comment("the big guy");
        assertNotNull(comment);
        assertEquals("the big guy", comment.getText());
        assertEquals("<!-- the big guy -->\n", comment.toXml());
    }
}
