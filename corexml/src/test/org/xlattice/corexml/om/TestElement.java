/* TestElement.java */
package org.xlattice.corexml.om;

import java.io.StringReader;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

import org.xlattice.corexml.CoreXmlTestCase;

public class TestElement extends CoreXmlTestCase {

    private Element element;

    public TestElement (String name) {
        super(name);
    }

    public void setUp () {
        element = null;
    }

    public void tearDown() {
    }

    public void testBooleans() {
        Element x = new Element("abc");
        assertNotNull(x);
        assertFalse(x.isAttr());
        assertFalse(x.isComment());
        assertFalse(x.isDocument());
        assertFalse(x.isDocType());
        assertTrue (x.isElement());
        assertFalse(x.isProcessingInstruction());
        assertFalse(x.isText());
    }
    public void testEmptyElement() {
        element = new Element("abc");
        assertNotNull(element);
        assertNull(element.getPrefix());
        assertNotNull(element.getName());
        assertEquals("abc", element.getName());
        assertSameSerialization("<abc/>\n", element.toXml());
    }
    /**
     * Test added 2006-06-21 after deserializing problem discovered.
     * Unfortunately, the problem doesn't show up here.
     */
    public final String data060621 = 
        Document.DEFAULT_XML_DECL
        + "<project>" 
        + "  <!-- project description -->"
        + "  <description>"
        + "    Dummy parent project."
        + "  </description>"
        + "  <shortDescription>"
        + "    dummy parent project"
        + "  </shortDescription>"
        + "</project>";

  
  public void test21060621 ()                 throws Exception {
        Document doc = new XmlParser(new StringReader(data060621)).read();
        String serialization = doc.toXml();
        // DEBUG
        System.out.println(serialization);
        // END
        assertSameSerialization( data060621, serialization );
    }
    // XXX TEST ATTRIBUTE LIST **********

    // XXX MUCH MORE TESTING NEEDED *****
}
