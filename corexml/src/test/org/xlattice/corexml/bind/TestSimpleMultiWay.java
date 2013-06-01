/* TestSimpleMultiWay.java */
package org.xlattice.corexml.bind;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.XmlParser;
/**
 * Data for testing Mapping and related classes.
 */
public class TestSimpleMultiWay extends CoreXmlTestCase {

    // TEST CLASSES ///////////////////////////////////////
    protected static final String CLASS_FLIST1_NAME=
                            "org.xlattice.corexml.bind.FList1";
    protected static final String CLASS_FLIST2_NAME=
                            "org.xlattice.corexml.bind.FList2";
    protected static final String CLASS_F_NAME=
                            "org.xlattice.corexml.bind.F";

    // TEST XML DATA //////////////////////////////////////
    String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    String xmlFsStart= "<Fs>\n";
    String xmlF0     = "  <tagF>valueF0</tagF>\n";
    String xmlF1     = "  <tagF>valueF1</tagF>\n";
    String xmlF2     = "  <tagF>valueF2</tagF>\n";
    String xmlF3     = "  <tagF>valueF3</tagF>\n";
    String xmlF4     = "  <tagF>valueF4</tagF>\n";
    String xmlFsEnd  = "</Fs>\n";
    String xmlForMapping = xmlHeader + xmlFsStart 
                                + xmlF0 + xmlF1 + xmlF2 + xmlF3 + xmlF4
                                + xmlFsEnd;
    // TEST CASE CONSTRUCTOR //////////////////////////////
    public TestSimpleMultiWay (String name) {
        super(name);
    }

    // TEST DATA STRUCTURES ///////////////////////////////
    protected Mapping        map;
    protected SubMapping     subMapF;

    // SINGLE-CLASS TESTS ///////////////////////////////////////////
    /**
     * Build a mapping that will store the F values in one class.
     */
//  public void buildMapping1() throws CoreXmlException {
//      map = new Mapping ("Fs", CLASS_FLIST1_NAME);
//      Collector collectFs = new Collector("Fs");  // to provide the <Fs>
//      map.add(collectFs);
//      Collector collectTagF  = new Collector("tagF") // provide the <tagF>
//          .setSetter("addF").setGetter("getF").setSizer("sizeF").repeats();
//      collectTagF.add(new TextBinding("fValue"));
//      collectFs.add(collectTagF);
//      map.join();
//  }
//  public void testBasics1()                       throws Exception {
//      buildMapping1();
//      assertNotNull(map);
//      assertEquals("Fs", map.getName());
//      assertEquals(CLASS_FLIST1_NAME, map.getClassName());
//      assertEquals(1, map.getOrdering().size());

//      assertNotNull(subMapF);
//      assertEquals("tagF", subMapF.getName());
//      assertEquals(FList1.class, subMapF.getUpClazz());
//      assertEquals(F.class,  subMapF.getClazz());

//      assertEquals ("getF",  subMapF._getGetterName());
//      assertEquals ("addF",  subMapF._getSetterName());
//      assertEquals ("sizeF", subMapF._getSizerName());
//      assertEquals (1, subMapF.getMinOccur());
//      assertEquals (Integer.MAX_VALUE, subMapF.getMaxOccur());

//      assertEquals(1, subMapF.getOrdering().size());
//      assertTrue ( F.class == subMapF.getOrdering().getClazz());
//  }
//  public void testApply1()                        throws Exception {
//      buildMapping1();
//      Document doc = new XmlParser (new StringReader(xmlForMapping))
//                      .read();
//      Object o = map.apply(doc);
//      assertNotNull(o);
//      assertTrue (o instanceof FList1);
//      FList1 fs = (FList1)o;
//      assertEquals (5, fs.sizeF());
//  }
//  public void testGenerate1()                     throws Exception {
//      buildMapping1();
//      Document doc  = new XmlParser (new StringReader(xmlForMapping))
//                      .read();
//      FList1 fList  = (FList1)map.apply(doc);
//      Document doc2 = map.generate (fList);

//      assertSameSerialization(xmlForMapping, doc2.toXml());
//  } // GEEP
    
    // TWO-CLASS TESTS //////////////////////////////////////////////
    /**
     * Build a mapping that will store the F values in two classes.
     */
    public void buildMapping2() throws CoreXmlException {
        map = new Mapping ("Fs", CLASS_FLIST2_NAME);
        subMapF = new SubMapping("tagF", CLASS_F_NAME, "fCollection");
        subMapF.setSetter("addF").setGetter("getF").setSizer("sizeF").repeats()
            .add(new TextBinding("fValue"));
        map.add(subMapF);
        map.join();
    }
    public void testBasics2()                       throws Exception {
        buildMapping2();
        assertNotNull(map);
        assertEquals("Fs", map.getName());
        assertEquals(CLASS_FLIST2_NAME, map.getClassName());
        assertEquals(1, map.getOrdering().size());

        assertNotNull(subMapF);
        assertEquals("tagF", subMapF.getName());
        assertEquals(FList2.class, subMapF.getUpClazz());
        assertEquals(F.class,  subMapF.getClazz());

        assertEquals ("getF",  subMapF._getGetterName());
        assertEquals ("addF",  subMapF._getSetterName());
        assertEquals ("sizeF", subMapF._getSizerName());
        assertEquals (1, subMapF.getMinOccur());
        assertEquals (Integer.MAX_VALUE, subMapF.getMaxOccur());

        assertEquals(1, subMapF.getOrdering().size());
        assertTrue ( F.class == subMapF.getOrdering().getClazz());
    }
    public void testApply2()                        throws Exception {
        buildMapping2();
        Document doc = new XmlParser (new StringReader(xmlForMapping))
                        .read();
        Object o = map.apply(doc);
        assertNotNull(o);
        assertTrue (o instanceof FList2);
        FList2 fs = (FList2)o;
        assertEquals (5, fs.sizeF());
    }
    public void testGenerate2()                     throws Exception {
        buildMapping2();
        Document doc  = new XmlParser (new StringReader(xmlForMapping))
                        .read();
        FList2 fList  = (FList2)map.apply(doc);
        Document doc2 = map.generate (fList);

        assertSameSerialization(xmlForMapping, doc2.toXml());
    }
}
