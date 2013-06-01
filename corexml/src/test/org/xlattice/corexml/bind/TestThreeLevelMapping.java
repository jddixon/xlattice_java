/* TestThreeLevelMapping.java */
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
public class TestThreeLevelMapping extends ThreeLevelMapping {

    public TestThreeLevelMapping (String name) {
        super(name);
    }

    public void setUp () {
        map       = null;  seq          = null;
        subMapD   = null;  dSeq         = null;
        attrBagD  = null;
        collector = null;  
        subMapF   = null;  fSeq         = null;
    }

    public void tearDown() {
    }

    /** 
     * Test Mapping has no bindings. 
     */
    public void testEmptyMapping()            throws Exception {
        buildEmptyMapping();
        assertEquals ("tagA", map.getName());
        assertEquals (CLASS_A_NAME, map.getClassName());
        assertEquals (0, seq.size());
        Document doc = new XmlParser(new StringReader(xmlForFullMapping))
                            .read();
        // XXX NOW FAILS HERE "no binding for attribute attr1"
//      Object o = map.apply(doc);
//      assertNotNull(o);
//      assertTrue (o instanceof A);
//      A a = (A)o;
        // single instance of A should have been created, no fields
        // should have been set
    }
    /** Test Mapping has only attributes on top level element bound */
    public void testTwoAttrMapping()            throws Exception {
        buildTwoAttrMapping();
        assertEquals ("tagA", map.getName());
        assertEquals (CLASS_A_NAME, map.getClassName());
        assertEquals (2, attrBag.size());
        assertEquals (0, seq.size());
        Document doc = new XmlParser(new StringReader(xmlForFullMapping))
                            .read();
        Object o = map.apply(doc);
        assertNotNull(o);
        assertTrue (o instanceof A);
        A a = (A)o;
        assertEquals("value1", a.getField1());
        assertEquals("value2", a.getField2());
    } 
    public void testSimplerMapping()            throws Exception {
        buildSimplerMapping();
        assertEquals ("tagA", map.getName());
        assertEquals (CLASS_A_NAME, map.getClassName());
        assertEquals (2, attrBag.size());
        assertEquals (3, seq.size());
        Document doc = new XmlParser(new StringReader(xmlForSimplerMapping))
                            .read();
        Object o = map.apply(doc);
        assertNotNull(o);
        assertTrue (o instanceof A);
        A a = (A)o;
        assertEquals("value1", a.getField1());
        assertEquals("value2", a.getField2());
        assertTrue(a.isBPresent());
        assertEquals("valueC", a.getFieldC());
        assertEquals(cdata,    a.getFieldG());
    } 
    public void testBasics()                    throws Exception {
        buildFullMapping();
        assertEquals ("tagA", map.getName());
        assertEquals (CLASS_A_NAME, map.getClassName());
        assertEquals (2, attrBag.size());
        assertEquals (4, seq.size());

        assertEquals ("tagD", subMapD.getName());
        assertEquals (1, attrBagD.size());
        assertEquals (3, dSeq.size());
        
        assertEquals ("tagFs", collector.getName());

        assertEquals ("tagF", subMapF.getName());
        assertEquals (1, fSeq.size());
    }
    public void testApplyingMapper()            throws Exception {
        buildFullMapping();
        assertEquals (A.class, map.getClazz());
        Document doc = new XmlParser(new StringReader(xmlForFullMapping))
                            .read();
        Object o = map.apply(doc);
        assertNotNull(o);
        assertTrue (o instanceof A);
        A a = (A)o;
        assertEquals("value1", a.getField1());
        assertEquals("value2", a.getField2());
        assertTrue(a.isBPresent());
        assertEquals("valueC", a.getFieldC());

        D theD = a.getTheD();
        assertNotNull(theD);
        assertEquals(5, theD.sizeF());
        assertEquals("valueF0", theD.getF(0).getFValue());
        assertEquals("valueF1", theD.getF(1).getFValue());
    }
}
