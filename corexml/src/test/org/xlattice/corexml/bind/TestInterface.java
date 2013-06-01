/* TestInterface.java */
package org.xlattice.corexml.bind;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;

import junit.framework.*;

import org.xlattice.corexml.*;
import org.xlattice.corexml.om.*;

/**
 * Test corexml.bind.Interface, which is a Join which accepts
 * any of a number of elements, each of which maps into a distinct
 * Java class.  
 * 
 * XXX RENAME TestSixteenFold to TestInterface and drop this class.
 *
 * @author Jim Dixon
 */
public class TestInterface extends CoreXmlTestCase {

    // TEST CLASSES ///////////////////////////////////////
    // contains instances of Marker
    protected static final String C_W =
                            "org.xlattice.corexml.bind.W";
    protected static final String I_M =
                            "org.xlattice.corexml.bind.Marker";

    // F, X, Y, Z implement Marker
    protected static final String C_F =
                            "org.xlattice.corexml.bind.F";
    protected static final String C_X =
                            "org.xlattice.corexml.bind.X";
    protected static final String C_Y =
                            "org.xlattice.corexml.bind.Y";
    protected static final String C_Z =
                            "org.xlattice.corexml.bind.Z";

    // TEST XML DATA //////////////////////////////////////
    String xmlHeader  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    String xmlWrap    = "<tagW attr1=\"value1\" attr2=\"value2\" >\n";

    // F, X, Y, Z implement Marker
    String xmlF0     = "  <tagF>valueF0</tagF>\n";
    String xmlF1     = "  <tagF>valueF1</tagF>\n";
    String xmlF2     = "  <tagF>valueF2</tagF>\n";
    String xmlF3     = "  <tagF>valueF3</tagF>\n";
    String xmlF4     = "  <tagF>valueF4</tagF>\n";
    String xmlX   = "  <tagX attr3=\"value3\">valueX</tagX>\n";
    String xmlY   = "  <tagY attr4=\"14\">valueY</tagY>\n";
    String xmlZ   = "  <tagZ hyph-attr=\"false\">valueZ</tagZ>\n";
    
    String xmlEndWrap = "</tagW>\n";

    String oneOfEach  = xmlF0 + xmlX + xmlY + xmlZ;
    String many       = oneOfEach + xmlF1 + xmlF2 + xmlF3 + xmlF4;
    String wrappedXml = xmlHeader + xmlWrap + many + xmlEndWrap;

    // TEST CASE CONSTRUCTOR //////////////////////////////
    public TestInterface (String name) {
        super(name);
    }

    // TEST DATA STRUCTURES ///////////////////////////////
    protected Mapping        map;
    protected Interface      iface;
    protected SubMapping     subMapF;
    protected SubMapping     subMapX;
    protected SubMapping     subMapY;
    protected SubMapping     subMapZ;

    public void setUp()                     throws CoreXmlException {
        map   = new Mapping ("tagW", C_W);
        map.add( new AttrBinding("attr1") );
        map.add( new AttrBinding("attr2") );

        iface = new Interface("Marker", I_M, "marker");
        iface.repeats();
        
        subMapF = new SubMapping("tagF", C_F, "marker");
        subMapF.add( new TextBinding("fValue") );
        iface.add(subMapF);
    
        subMapX = new SubMapping("tagX", C_X, "marker");
        subMapX.add( new AttrBinding("attr3") );
        subMapX.add( new TextBinding("valueX") );
        iface.add(subMapX);

        subMapY = new SubMapping("tagY", C_Y, "marker");
        subMapY.add( new AttrBinding("attr4") );
        subMapY.add( new TextBinding("valueY") );
        iface.add(subMapY);

        subMapZ = new SubMapping("tagZ", C_Z, "marker");
        subMapZ.add( new AttrBinding("hyph-attr") );
        subMapZ.add( new TextBinding("valueZ") );
        iface.add(subMapZ);

        map.add(iface);
        map.join();
    }
    public void testBasics()                        throws Exception {
        assertNotNull(map);
        assertEquals("tagW", map.getName());
        assertEquals(C_W, map.getClassName());
        assertEquals(1, map.getOrdering().size());

        assertEquals("Marker", iface.getName());

        assertNotNull(subMapF);
        assertEquals("tagF", subMapF.getName());
        assertEquals(W.class, subMapF.getUpClazz());
        assertEquals(F.class,  subMapF.getClazz());

        assertNotNull(subMapF.getOrdering());
        assertEquals(1, subMapF.getOrdering().size());
        assertTrue ( F.class == subMapF.getOrdering().getClazz());
    }
    public void testApply()                         throws Exception {
        Document doc = new XmlParser (new StringReader(wrappedXml))
                        .read();
        Object o = map.apply(doc);
        assertNotNull(o);
        assertTrue (o instanceof W);
        W w = (W)o;
    }
    // XXX THIS TEST INCORRECTLY ASSUMES that the attributes will
    // be serialized in a particular order
    public void testGenerate()                      throws Exception {
        Document doc = new XmlParser (new StringReader(wrappedXml))
                        .read();
        W w = (W)map.apply(doc);
        Document doc2 = map.generate (w);

        assertSameSerialization(wrappedXml, doc2.toXml());
    }
}
