/* ThreeLevelMapping.java */
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

/**
 * Data for testing Mapping and related classes.
 */
public abstract class ThreeLevelMapping extends CoreXmlTestCase {

    
    // TEST CLASSES ///////////////////////////////////////
    protected static final String CLASS_A_NAME=
                            "org.xlattice.corexml.bind.A";
    protected static final String CLASS_D_NAME=
                            "org.xlattice.corexml.bind.D";
    protected static final String CLASS_F_NAME=
                            "org.xlattice.corexml.bind.F";

    // TEST XML DATA //////////////////////////////////////
    String cdata     = "this is CDATA content";

    String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    String xmlComment= "<!-- usual mindless comment-->\n";
    String xmlStart  = "<tagA attr1=\"value1\" attr2=\"value2\" >\n";
    String xmlTagB   = "  <tagB/>\n";
    String xmlTagC   = "  <tagC>valueC</tagC>\n";
    String xmlStartD = "  <tagD attr3=\"value3\">\n";
    String xmlTagE   = "    <tagE>valueE</tagE>\n";
    String xmlFsStart= "    <tagFs>\n";
    String xmlF0     = "      <tagF>valueF0</tagF>\n";
    String xmlF1     = "      <tagF>valueF1</tagF>\n";
    String xmlF2     = "      <tagF>valueF2</tagF>\n";
    String xmlF3     = "      <tagF>valueF3</tagF>\n";
    String xmlF4     = "      <tagF>valueF4</tagF>\n";
    String xmlFsEnd  = "    </tagFs>\n";
    String xmlTextD  = "    TextD\n";
    String xmlEndD   = "  </tagD>\n";
    String xmlStartG = "  <tagG>\n";
    String xmlCDATA  = "    <![CDATA[" + cdata + "]]>\n";
    String xmlEndG   = "  </tagG>\n";
    String xmlEnd    = "</tagA>\n";

    String xmlForTwoAttrMapping = xmlHeader + xmlComment + xmlStart + xmlEnd;
    // a bit of a kludge, because the OM code doesn't generate a separate
    // close tag
    String uncommentedXmlForTwoAttr = xmlHeader + 
            "<tagA attr1=\"value1\" attr2=\"value2\" />\n";

    String xmlForSimplerMapping = xmlHeader + xmlComment + xmlStart 
                    + xmlTagB + xmlTagC + xmlStartG + xmlCDATA + xmlEndG 
                    + xmlEnd;
    /** note that the CDATA wrapper gets stripped off */
    String uncommentedXmlForSimplerMapping = xmlHeader + xmlStart 
                    + xmlTagB + xmlTagC + xmlStartG + cdata + xmlEndG 
                    + xmlEnd;
   
    String xmlForTwoLevelMapping = xmlHeader + xmlComment + xmlStart 
                    + xmlTagB + xmlTagC + xmlStartD + xmlTagE 
                    + xmlTextD + xmlEndD
                    + xmlStartG + xmlCDATA + xmlEndG 
                    + xmlEnd;
    /** the comment disappears and the CDATA wrapper gets stripped off */
    String uncommentedXmlForTwoLevelMapping = xmlHeader + xmlStart 
                    + xmlTagB + xmlTagC + xmlStartD + xmlTagE 
                    + xmlTextD + xmlEndD
                    + xmlStartG + cdata + xmlEndG 
                    + xmlEnd;
    
    String xmlForFullMapping = xmlHeader + xmlComment + xmlStart 
                    + xmlTagB + xmlTagC + xmlStartD + xmlTagE 
                    + xmlFsStart + xmlF0 + xmlF1 + xmlF2 
                                 + xmlF3 + xmlF4 + xmlFsEnd
                    + xmlTextD + xmlEndD
                    + xmlStartG + xmlCDATA + xmlEndG 
                    + xmlEnd;
    /** the comment disappears and the CDATA wrapper gets stripped off */
    String uncommentedXmlForFullMapping = xmlHeader + xmlStart 
                    + xmlTagB + xmlTagC + xmlStartD + xmlTagE 
                    + xmlFsStart + xmlF0 + xmlF1 + xmlF2 
                                 + xmlF3 + xmlF4 + xmlFsEnd
                    + xmlTextD + xmlEndD
                    + xmlStartG + cdata + xmlEndG 
                    + xmlEnd;
    
    // TEST CASE CONSTRUCTOR //////////////////////////////
    public ThreeLevelMapping (String name) {
        super(name);
    }

    // TEST DATA STRUCTURES ///////////////////////////////
    protected Mapping        map;
    protected AttrBag        attrBag;
    protected Seq            seq;
    protected AttrBinding    attr1;
    protected EmptyElBinding emptyEl;
    protected SubElBinding   subEl;
    protected SubMapping     subMapD;
    protected AttrBag        attrBagD;
    protected Seq            dSeq;
    protected Collector      collector;
    protected SubMapping     subMapF;
    protected Seq            fSeq;
    protected SubElBinding   textG;     // CDATA

    public void buildEmptyMapping()   throws CoreXmlException {
        map = new Mapping ("tagA", CLASS_A_NAME);
        attrBag = map.getAttrBag();
        map.join();
        seq = (Seq) map.getOrdering();
    }
    public void buildTwoAttrMapping() throws CoreXmlException {
        map = new Mapping ("tagA", CLASS_A_NAME);
        attrBag = map.getAttrBag();
        attr1   = new AttrBinding   ("attr1", "field1");
        map.add(attr1);
        // anonymous binding 
        map.add(new AttrBinding   ("attr2", "field2"));
        map.join();

        seq = (Seq) map.getOrdering();
    }
    public void buildSimplerMapping() throws CoreXmlException {
        map = new Mapping ("tagA", CLASS_A_NAME);
        attrBag = map.getAttrBag();
        seq = (Seq) map.getOrdering(); // USED ONLY FOR TESTING
        
        attr1   = new AttrBinding   ("attr1", "field1");
        emptyEl = new EmptyElBinding("tagB", "bPresent");
        subEl   = new SubElBinding  ("tagC", "fieldC");
        map.add(attr1)
           .add(new AttrBinding     ("attr2", "field2"))
           .add(emptyEl)
           .add(subEl);

        // SKIP <tagD> and subelements

        textG = new SubElBinding("tagG", "fieldG");
        map.add(textG);
        map.join();
    }
    
    public void buildTwoLevelMapping() throws CoreXmlException {
        map = new Mapping ("tagA", CLASS_A_NAME);
        attrBag = map.getAttrBag();
        seq = ((Seq) map.getOrdering());
        map.add(new AttrBinding    ("attr1",   "field1"))
           .add(new AttrBinding    ("attr2",   "field2"))
           .add(new EmptyElBinding ("tagB",    "bPresent" ))
           .add(new SubElBinding   ("tagC",    "fieldC"   ));

        subMapD = new SubMapping("tagD", CLASS_D_NAME, "theD");
        attrBagD = subMapD.getAttrBag();
        map.add(subMapD);
        dSeq = ((Seq)subMapD.getOrdering());
        subMapD.add(new AttrBinding    ("attr3", "field3"))
               .add(new SubElBinding   ("tagE", "fieldE"))
               .add(new TextBinding("textField"));
        textG = new SubElBinding("tagG", "fieldG");
        map.add(textG);
        map.join();
    } 
    public void buildFullMapping() throws CoreXmlException {
        map = new Mapping ("tagA", CLASS_A_NAME);
        attrBag = map.getAttrBag();
        map.add(new AttrBinding    ("attr1",   "field1"))
           .add(new AttrBinding    ("attr2",   "field2"))
           .add(new EmptyElBinding ("tagB",    "bPresent" ))
           .add(new SubElBinding   ("tagC",    "fieldC"   ));

        subMapD = new SubMapping("tagD", CLASS_D_NAME, "theD");
        attrBagD = subMapD.getAttrBag();
        map.add(subMapD);
        subMapD.add(new AttrBinding    ("attr3", "field3"))
               .add(new SubElBinding   ("tagE", "fieldE"));
       
        // <tagFs> collector below tagD
        collector = new Collector("tagFs");
        subMapD.add(collector);
        // <tagF> items grouped below collector
        subMapF = new SubMapping("tagF", CLASS_F_NAME, "fCollection")
                            .setSetter("addF").setGetter("getF")
                            .setSizer("sizeF").repeats();
        collector.add(subMapF);
        subMapF.add(new TextBinding("fValue"));
        
        // last subelement in tagD
        subMapD.add(new TextBinding("textField"));
        // last subelement in tagA
        textG = new SubElBinding("tagG", "fieldG");
        map.add(textG);
        map.join();

        // the various orderings, for testing
        seq  = ((Seq)map.getOrdering());
        dSeq = ((Seq)subMapD.getOrdering());
        fSeq = ((Seq)subMapF.getOrdering());
    } 
}
