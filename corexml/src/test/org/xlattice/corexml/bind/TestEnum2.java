/* TestEnum2.java */
package org.xlattice.corexml.bind;

import java.io.StringReader;

import junit.framework.*;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

/**
 * @author Jim Dixon
 */

public class TestEnum2                      extends CoreXmlTestCase {

    // INSTANCE VAR /////////////////////////////////////////////////
    private Mapping map;
    private W w;
    
    // TEST CASE CONSTRUCTOR ////////////////////////////////////////
    public TestEnum2 (String name)          throws Exception {
        super(name);
        map   = new Mapping ("w", "org.xlattice.corexml.bind.W");
        map.add( new AttrBinding("attr1") .fixed("foo") );
        map.add( new AttrBinding("attr2") 
                .values("def:val1,val2,val3")
                .optional() );
        map.join();
    }

    // TESTS ////////////////////////////////////////////////////////
    public void setUp ()                    throws Exception {
        w = new W();
    }
    
    String xmlHeader  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    
    public void testApplyBothAttr()         throws Exception {
        String input  = xmlHeader + "<w attr1=\"foo\" attr2=\"val3\" />";
        w = (W)map.apply(new XmlParser (new StringReader(input)).read());
        assertEquals("foo",     w.getAttr1());
        assertEquals("val3",    w.getAttr2());
    }
    public void testApplyDefaulting()       throws Exception {
        String input  = xmlHeader + "<w attr1=\"foo\"  />";
        w = (W)map.apply(new XmlParser (new StringReader(input)).read());
        assertEquals("foo",     w.getAttr1());
        assertEquals("def",     w.getAttr2());
    }
    public void testGenWithBothAttr()       throws Exception {
        w.setAttr1("foo");
        w.setAttr2("val1");
        Document doc = map.generate(w);
        // XXX THIS TEST INCORRECTLY ASSUMES that the attributes will
        // be serialized in a particular order
//      assertSameSerialization(
//              xmlHeader + 
//              "<w attr1=\"foo\" attr2=\"val1\" />",
//              doc.toXml() );
//      XXX it would be better to fix assertSameSerialization XXX
        String serialization = doc.toXml();
        // NOTE there is no space before end of element AND newline at end :-(
        if (
            !(serialization.equals(
                    xmlHeader + "<w attr1=\"foo\" attr2=\"val1\"/>")) &&
            !(serialization.equals(
                    xmlHeader + "<w attr2=\"val1\" attr1=\"foo\"/>\n")) ) {
                // DEBUG
                System.out.println("serialization is " + serialization);
                // END
                fail();
        }
    } 
    public void testGenerateDefaulting()    throws Exception {
        w.setAttr1("foo");
        // attr2 has not been set
        Document doc = map.generate(w);
//      assertSameSerialization(
//              "attr2 didn't default correctly",
//              xmlHeader + 
//              "<w attr1=\"foo\" attr2=\"def\" />",
//              doc.toXml() );
        // XXX SAME FIX AS ABOVE
        String serialization = doc.toXml();
        if (!(serialization.equals(
                xmlHeader + "<w attr1=\"foo\" attr2=\"def\" />")) &&
            !(serialization.equals(
                xmlHeader + "<w attr2=\"def\" attr1=\"foo\"/>\n")) ) {
                // DEBUG
                System.out.println("serialization is " + serialization);
                // END
                fail("attr2 didn't default correctly");
        }
    }
    public void testApplyBadFixedValue()    throws Exception {
        String input  = xmlHeader + "<w attr1=\"bar\" attr2=\"val3\" />";
        try {
            w = (W)map.apply(new XmlParser (new StringReader(input)).read());
            fail("apply() didn't catch bad fixed value");
        } catch (CoreXmlException cxe) { /* success */ }
    }
    public void testGenerateBadFixedValue() throws Exception {
        w.setAttr1("bar");
        w.setAttr2("val1");
        try {
            Document doc = map.generate(w);
            fail("generate() didn't catch bad fixed value");
        } catch (CoreXmlException cxe) { /* success */ }
    }
}
