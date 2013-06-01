/* Test3LevelGen.java */
package org.xlattice.corexml.bind;

import java.io.StringReader;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.*;

/**
 */
public class Test3LevelGen extends ThreeLevelMapping {

    public Test3LevelGen (String name) {
        super(name);
    }

    public void setUp () {
        map       = null;  seq          = null;
        subMapD   = null;  dSeq         = null;
        collector = null;  
        subMapF   = null;  fSeq         = null;
    }

    public void tearDown() {
    }

    /** Test Mapping has only attributes on top level element bound */
    public void testTwoAttrMapping()            throws Exception {
        buildTwoAttrMapping();
        assertEquals ("tagA", map.getName());
        assertEquals (CLASS_A_NAME, map.getClassName());
        assertEquals (2, attrBag.size());
        assertEquals (0, seq.size());
        Document doc = new XmlParser(new StringReader(xmlForTwoAttrMapping))
                            .read();
        A a = (A)map.apply(doc);
        Document doc2 = map.generate(a);
        NodeList nodes = doc2.getNodeList();
        assertEquals (1, nodes.size());
        assertSameSerialization (uncommentedXmlForTwoAttr, doc2.toXml());
    } 
    public void testSimplerMapping()            throws Exception {
        buildSimplerMapping();
        assertEquals ("tagA", map.getName());
        assertEquals (CLASS_A_NAME, map.getClassName());
        assertEquals (2, attrBag.size());
        assertEquals (3, seq.size());
        Document doc = new XmlParser(new StringReader(xmlForSimplerMapping))
                            .read();
        A a = (A)map.apply(doc);
        Document doc2 = map.generate(a);
        assertSameSerialization (uncommentedXmlForSimplerMapping, 
                                                        doc2.toXml());
    } 
    public void testTwoLevelMapping()                   throws Exception {
        buildTwoLevelMapping();
        Document doc = new XmlParser(new StringReader(xmlForTwoLevelMapping))
                            .read();
        A a = (A) map.apply(doc);
        Document doc2 = map.generate(a);
        assertSameSerialization (uncommentedXmlForTwoLevelMapping, 
                                                        doc2.toXml());
    } 
    // the new feature here is Collectors
    // XXX THIS TEST INCORRECTLY ASSUMES that the attributes will
    // be serialized in a particular order
    public void testFullMapping()                   throws Exception {
        buildFullMapping();
        Document doc = new XmlParser(new StringReader(xmlForFullMapping))
                            .read();
        A a = (A) map.apply(doc);
        Document doc2 = map.generate(a);
        assertSameSerialization (uncommentedXmlForFullMapping, 
                                                        doc2.toXml());
    }
}
