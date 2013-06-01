/* ThreeLevelData.java */
package org.xlattice.corexml.bind;

/**
 * Data for testing Mapping and related classes.
 *
 * @author Jim Dixon
 */
public interface ThreeLevelData {
    
    // TEST XML DATA //////////////////////////////////////
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
    String xmlTagG = "  <tagG>gee</tagG>\n";
    String xmlEnd    = "</tagA>\n";

    String xmlForTwoAttrMapping = xmlHeader + xmlComment + xmlStart + xmlEnd;
    // a bit of a kludge, because the OM code doesn't generate a separate
    // close tag
    String uncommentedXmlForTwoAttr = xmlHeader + 
            "<tagA attr1=\"value1\" attr2=\"value2\" />\n";

    String xmlForSimplerMapping = xmlHeader + xmlComment + xmlStart 
                    + xmlTagB + xmlTagC + xmlTagG 
                    + xmlEnd;
    String uncommentedXmlForSimplerMapping = xmlHeader + xmlStart 
                    + xmlTagB + xmlTagC + xmlTagG 
                    + xmlEnd;
   
    String xmlForTwoLevelMapping = xmlHeader + xmlComment + xmlStart 
                    + xmlTagB + xmlTagC + xmlStartD + xmlTagE 
                    + xmlTextD + xmlEndD
                    + xmlTagG 
                    + xmlEnd;
    String uncommentedXmlForTwoLevelMapping = xmlHeader + xmlStart 
                    + xmlTagB + xmlTagC + xmlStartD + xmlTagE 
                    + xmlTextD + xmlEndD
                    + xmlTagG 
                    + xmlEnd;
    
    String xmlForFullMapping = xmlHeader + xmlComment + xmlStart 
                    + xmlTagB + xmlTagC + xmlStartD + xmlTagE 
                    + xmlFsStart + xmlF0 + xmlF1 + xmlF2 + xmlF3 
                                                 + xmlF4 + xmlFsEnd
                    + xmlTextD + xmlEndD
                    + xmlTagG 
                    + xmlEnd;
    String uncommentedXmlForFullMapping = xmlHeader + xmlStart 
                    + xmlTagB + xmlTagC + xmlStartD + xmlTagE 
                    + xmlFsStart + xmlF0 + xmlF1 + xmlF2 + xmlF3 
                                                 + xmlF4 + xmlFsEnd
                    + xmlTextD + xmlEndD
                    + xmlTagG 
                    + xmlEnd;
    
}
