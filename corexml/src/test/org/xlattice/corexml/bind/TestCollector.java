/* TestCollector.java */
package org.xlattice.corexml.bind;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.XmlParser;
/**
 * Data for testing Collector.
 *
 * XXX AS WRITTEN THIS DOES NOT CURRENTLY MAKE SENSE.
 */
public class TestCollector extends CoreXmlTestCase {

    // TEST CLASSES ///////////////////////////////////////
    protected static final String CLASS_FS_NAME=
                            "org.xlattice.corexml.bind.Fs";
    protected static final String CLASS_F_NAME=
                            "org.xlattice.corexml.bind.F";

    // TEST XML DATA //////////////////////////////////////
    String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    String xmlFsStart= "<Fs><moreFs>\n";
    String xmlF0     = "  <tagF>valueF0</tagF>\n";
    String xmlF1     = "  <tagF>valueF1</tagF>\n";
    String xmlF2     = "  <tagF>valueF2</tagF>\n";
    String xmlF3     = "  <tagF>valueF3</tagF>\n";
    String xmlF4     = "  <tagF>valueF4</tagF>\n";
    String xmlFsEnd  = "</moreFs></Fs>\n";
    String xmlForMapping = xmlHeader + xmlFsStart 
                                + xmlF0 + xmlF1 + xmlF2 + xmlF3 + xmlF4
                                + xmlFsEnd;
    // TEST CASE CONSTRUCTOR //////////////////////////////
    public TestCollector (String name) {
        super(name);
    }

    // TEST DATA STRUCTURES ///////////////////////////////
    protected Mapping        map;
    protected Collector      collector;
    protected SubMapping     subMapF;

    public void buildMapping() throws CoreXmlException {
        map = new Mapping ("Fs", CLASS_FS_NAME);
        collector = new Collector("moreFs");
        map.add(collector);
        subMapF = new SubMapping("tagF", CLASS_F_NAME, "fCollection")
                    .setSetter("addF").setGetter("getF")
                    .setSizer("sizeF").repeats();
        collector.add(subMapF);
        subMapF.add(new TextBinding("fValue"));
        map.join();
    }
    public void testBasics()                        throws Exception {
        buildMapping();
        assertNotNull(map);
        assertEquals("Fs", map.getName());
        assertEquals(CLASS_FS_NAME, map.getClassName());
        assertEquals(1, map.getOrdering().size());

        assertEquals("moreFs", collector.getName());

        assertNotNull(subMapF);
        assertEquals("tagF", subMapF.getName());
        assertEquals(Fs.class, subMapF.getUpClazz());
        assertEquals(F.class,  subMapF.getClazz());

        assertNotNull(subMapF.getOrdering());
        assertEquals(1, subMapF.getOrdering().size());
        assertTrue ( F.class == subMapF.getOrdering().getClazz());
    }
    public void testApply()                         throws Exception {
        buildMapping();
        Document doc = new XmlParser (new StringReader(xmlForMapping))
                        .read();
        Object o = map.apply(doc);
        assertNotNull(o);
        assertTrue (o instanceof Fs);
        Fs fs = (Fs)o;


    }
    public void testGenerate()                      throws Exception {
        buildMapping();
        Document doc = new XmlParser (new StringReader(xmlForMapping))
                        .read();
        Fs fs = (Fs)map.apply(doc);
        Document doc2 = map.generate (fs);

        assertSameSerialization(xmlForMapping, doc2.toXml());
    }
}
