/* TestXmlParser.java */
package org.xlattice.corexml.om;

import java.io.StringReader;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.CoreXmlException;

public class TestXmlParser extends CoreXmlTestCase {

    XmlParser cxp;

    // TEST XML DATA //////////////////////////////////////
    String cdata     = "this is CDATA content";

    String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    String aComment  = "<!-- usual mindless comment-->\n";
    String xmlStart  = "<tagA attr1=\"value1\" attr2=\"value2\" >\n";
    String xmlTagB   = "  <tagB/>\n";
    String xmlTagC   = "  <tagC>valueC</tagC>\n";
    String xmlStartD = "  <tagD attr3=\"value3\">\n";
    String xmlTagE   = "    <tagE>valueE</tagE>\n";
    String xmlFsStart= "    <tagFs>\n";
    String xmlF1     = "      <tagF>valueF1</tagF>\n";
    String xmlF2     = "      <tagF>valueF1</tagF>\n";
    String xmlFsEnd  = "    </tagFs>\n";
    String xmlTextD  = "    TextD\n";
    String xmlEndD   = "  </tagD>\n";
    // tagG indentation altered to get through tests 
    String xmlStartG = "<tagG>\n";
    String xmlCDATA  = "    <![CDATA[" + cdata + "]]>\n";
    String xmlEndG   = "</tagG>\n";
    String xmlEnd    = "</tagA>\n";

    String PerlPI    = "<?perl chomp;?>";
    
    String xmlForSimplerMapping = xmlHeader + aComment 
                    + xmlStart + xmlTagB + xmlTagC
                    + xmlStartG + xmlCDATA + xmlEndG 
                    + xmlEnd;
    
    String xmlForFullMapping = xmlHeader + aComment
                    + xmlStart + xmlTagB + xmlTagC
                    + xmlStartD + xmlTagE 
                    + xmlFsStart + xmlF1 + xmlF2 + xmlFsEnd
                    + xmlTextD + xmlEndD
                    + xmlStartG + xmlCDATA + xmlEndG 
                    + xmlEnd;
    
    public TestXmlParser (String name) {
        super(name);
    }

    public void setUp () {
        cxp = null;
    }

    public void tearDown() {
    }

    public void testEmptyDocument() throws Exception {
        cxp = new XmlParser(new StringReader(xmlHeader));
        Document doc  = cxp.read();
        assertNotNull(doc);
        assertEquals("UTF-8", doc.getEncoding());
        assertEquals("1.0",   doc.getVersion());
        // CHECK NAMESPACES, SHOULD BE EMPTY
        
        // check for children
        NodeList kids = doc.getNodeList();
        assertNotNull(kids);
        assertEquals(0, kids.size());
        // check for null root
        Element  root = doc.getElementNode();
        assertNull(root);
        assertEquals(Document.DEFAULT_XML_DECL, doc.toXml());
    }
    public void testSingleElementNoAttr() throws Exception {
        cxp = new XmlParser(new StringReader( 
                    xmlHeader + xmlStartG + xmlEndG));
        Document doc = cxp.read();
        assertNotNull(doc);
        assertEquals("UTF-8", doc.getEncoding());
        assertEquals("1.0",   doc.getVersion());
        Element  root = doc.getElementNode();  // CHECK IT
       
        // CHECK DOC NAMESPACES
        
        // CHECK CHILDREN
        NodeList kids = doc.getNodeList();
        // whitespace tagG whitespace
        assertEquals("wrong number of children!", 3, kids.size());
        assertTrue( (kids.get(0)).isText() );
        assertTrue( (kids.get(1)).isElement() );
        assertTrue( (kids.get(2)).isText() );

        Element kidG = (Element)kids.get(1);
        // CHECK ATTRIBUTES
        // CHECK NAMESPACES
        // CHECK CHILDREN
        NodeList grandchildren = kidG.getNodeList();
        assertEquals(1, grandchildren.size());
        assertTrue( (grandchildren.get(0)).isText() );
        
        String expected = Document.DEFAULT_XML_DECL + xmlStartG + xmlEndG ;
        assertSameSerialization(expected, doc.toXml());
    }
    public void testElementWithComments() throws Exception {
        cxp = new XmlParser(new StringReader( 
                    xmlHeader + xmlStartG 
                    + aComment + xmlCDATA + aComment + xmlCDATA 
                    + PerlPI + xmlEndG
                    ));
        Document doc = cxp.read();
        assertNotNull(doc);
        assertEquals("UTF-8", doc.getEncoding());
        assertEquals("1.0",   doc.getVersion());
       
        // CHECK DOC NAMESPACES
        
        // CHECK CHILDREN
        NodeList kids = doc.getNodeList();
        // whitespace tagG whitespace
        assertEquals("wrong number of children!", 3, kids.size());
        assertTrue( (kids.get(0)).isText() );
        assertTrue( (kids.get(1)).isElement() );
        assertTrue( (kids.get(2)).isText() );

        Element kidG = (Element)kids.get(1);
        Element root = doc.getElementNode();
        assertTrue (root == kidG);

        // CHECK ATTRIBUTES
        // CHECK NAMESPACES
        // CHECK CHILDREN
        NodeList grandchildren = kidG.getNodeList();
        assertEquals(10, grandchildren.size());
        assertTrue( (grandchildren.get(0)).isText() );
        assertTrue( (grandchildren.get(1)).isComment() );
        assertTrue( (grandchildren.get(2)).isText() );
        assertTrue( ((Text)grandchildren.get(3)).isCdata() );
        assertTrue( (grandchildren.get(4)).isText() );
        assertTrue( (grandchildren.get(5)).isComment() );
        assertTrue( (grandchildren.get(6)).isText() );
        assertTrue( ((Text)grandchildren.get(7)).isCdata() );
        assertTrue( (grandchildren.get(8)).isText() );
        assertTrue( (grandchildren.get(9)).isProcessingInstruction() );
        
        String expected = xmlHeader + xmlStartG 
                    + aComment + xmlCDATA + aComment + xmlCDATA + PerlPI
                    + xmlEndG;
        assertSameSerialization(expected, doc.toXml());
    }
    public void testFullMappingXml() throws Exception {
        cxp = new XmlParser(new StringReader(xmlForFullMapping));
        Document doc = cxp.read();
        assertNotNull(doc);
        assertEquals("UTF-8", doc.getEncoding());
        assertEquals("1.0",   doc.getVersion());
       
        // CHECK DOC NAMESPACES
        
        // CHECK CHILDREN

        // CHECK ATTRIBUTES
        // CHECK NAMESPACES
        // CHECK CHILDREN
        
        assertSameSerialization(xmlForFullMapping, doc.toXml());
    }
    // /////////////////////////////////////////////////////////////
    // IF NAMESPACES ARE DISABLED, THEY ARE SEEN BY THE PARSER AS
    // ATTRIBUTES.  IF THEY ARE ENABLED ... it's a bit tricky.  The
    // parser returns ALL of the namespaces visible at the current
    // depth, which will include those declared lower in the stack.
    // /////////////////////////////////////////////////////////////
    String xmlinner  = "  <tagB2 xmlns=\"http://xlattice.org/ns/default\""
                       +    " xmlns:c=\"http://xlattice.org/ns/core\"/>\n";
    public void testSimpleNamespaces() throws Exception {
        cxp = new XmlParser(new StringReader(xmlinner));
        Document doc = cxp.read();
        assertNotNull(doc);
        assertEquals("UTF-8", doc.getEncoding());
        assertEquals("1.0",   doc.getVersion());
        assertSameSerialization(xmlHeader + xmlinner, doc.toXml());
    }    
    String startWrapper = 
            "<wrapper xmlns:f=\"http://xlattice.org/ns/file\""
               +    " xmlns:x=\"http://xlattice.org/ns/xgo\">\n";
    String endWrapper   = "</wrapper>\n"; 
    String nestedNames  = startWrapper + xmlinner + endWrapper;
    
    public void testNestedNames() throws Exception {
        cxp = new XmlParser(new StringReader(nestedNames));
        Document doc = cxp.read();
        assertNotNull(doc);
        assertEquals("UTF-8", doc.getEncoding());
        assertEquals("1.0",   doc.getVersion());
        Element root = doc.getElementNode();
        assertNotNull(root);
        NodeList kids = doc.getNodeList();
        assertEquals("wrong number of children", 2, kids.size());

        // <wrapper>
        Node kid0 = kids.get(0);
        assertTrue (kid0.isElement());
        Node kid1 = kids.get(1);
        assertTrue  (kid1.isText());
        assertFalse (((Text)kid1).isCdata());
        // LOOK AT NAMESPACES ON <wrapper>
        // *** MISSING TESTS ***

        // <inner>
        Element wrapper = (Element)kid0;
        NodeList grandchildren = wrapper.getNodeList();
        assertNotNull(grandchildren);
        assertEquals("wrong number of grandchildren", 3, grandchildren.size());
        NodeList gkids = wrapper.getNodeList();
        Node gkid0 = gkids.get(0);
        Node gkid1 = gkids.get(1);
        Node gkid2 = gkids.get(2);
        assertTrue (gkid0.isText());
        assertTrue (gkid1.isElement());
        assertTrue (gkid2.isText());
        Element inner = (Element) gkid1;
        // LOOK AT NAMESPACES ON <inner>
        // *** MISSING TESTS ***

        assertSameSerialization(xmlHeader + nestedNames, doc.toXml());
    }    

}
