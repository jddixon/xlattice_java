/* TestDocument.java */
package org.xlattice.corexml.om;

import junit.framework.*;
import org.xlattice.corexml.CoreXmlTestCase;

/**
 * @author Jim Dixon
 */

public class TestDocument extends CoreXmlTestCase {

    private Document doc;
    private Element  root;

    public TestDocument (String name) {
        super(name);
    }

    public void setUp () {
        doc  = null;
        root = null;
    }

    public void tearDown() {
    }
 
    public void testBooleans() {
        Document x = new Document();
        assertNotNull(x);
        assertFalse(x.isAttr());
        assertFalse(x.isComment());
        assertTrue (x.isDocument());
        assertFalse(x.isDocType());
        assertFalse(x.isElement());
        assertFalse(x.isProcessingInstruction());
        assertFalse(x.isText());
    }
    public void testEmptyDoc() {
        doc = new Document();
        assertNotNull(doc);
        assertEquals (Document.DEFAULT_XML_DECL, doc.toXml());
        assertEquals ("1.0", doc.getVersion());
        assertEquals ("UTF-8", doc.getEncoding());

        assertNull(doc.getElementNode());
        try {
            doc.setDocument(new Document());
            fail("succeeded in setting document's document!");
        } catch (IllegalStateException ise) { /* expected */ }
    }
    /**
     * XXX MUCH MORE TESTING NECESSARY
     */
    
    /**
     * XXX UNSATISFACTORY: the order in which namespaces are printed
     * is actually unpredictable.
     *
     * XXX MORE SIGNIFICANT: there is no way to associate namespaces
     * with a document.  They are actually associated with the root
     * element, which may have unexpected consequences if the document
     * is transformed or the root element replaced.
     */
    public void testAddingNamespaces() {
        doc  = new Document();
        root = new Element("abc");
        root.addNamespace("c", "http://org.xlattice.xgo/core");
        root.addNamespace("x", "http://org.xlattice.xgo/xml");
        doc.getNodeList().append(root);
        // BUG XXX code permits next line without the preceding!
        // XXX that is, root need not be in NodeList XXX
        doc.setElementNode (root);
        // XXX ORDER OF NAMESPACE DECLARATIONS IS UNPREDICTABLE:
        // have to fiddle with this to get it to succeed
        String expected = Document.DEFAULT_XML_DECL + "<abc"
                + " xmlns:c=\"http://org.xlattice.xgo/core\"" 
                + " xmlns:x=\"http://org.xlattice.xgo/xml\""
                + "/>\n";
        assertSameSerialization (expected, doc.toXml());

    }
    public void testOneElDoc() {
           
    }
}
