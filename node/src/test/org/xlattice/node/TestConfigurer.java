/* TestConfigurer.java */
package org.xlattice.node;

import java.io.StringReader;
import junit.framework.*;

import org.xlattice.NodeID;
import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.bind.Mapping;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.XmlParser;
import org.xlattice.util.Base64Coder;

/**
 * @author Jim Dixon
 */

public class TestConfigurer extends CoreXmlTestCase {
    
    private Base64Coder coder = new Base64Coder();
                           // ....|....1....|....2
    private byte[] idBytes = "this gets encoded as".getBytes();
    private byte[] pBytes  = "1234".getBytes();
    private byte[] qBytes  = "5678".getBytes();
    private byte[] dBytes  = "90ab".getBytes();
    private byte[] eBytes  = "cdef".getBytes();
    private String nodeID, p, q, d, e;

    // test configuration file
    String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    String start     = "<node>\n";
    String id;       // calculated
    String rsa;      //           in constructor
    String latStart  = "  <overlays>\n";
    String httpd     = "    <overlay dir=\"httpd\""
                     + "      class=\"org.xlattice.Httpd\" />\n";
    String memcache  = "    <overlay dir=\"memcache\""
                     + "      class=\"org.xlattice.lattice.MemCache\" />\n";
    String latEnd    = "  </overlays>\n";
    String xmlEnd    = "</node>\n";
    String xmlNoDefaults, xmlWithoutID, xmlWithoutRSA;

    public TestConfigurer (String name) {
        super(name);
        if (idBytes.length > 20) {
            byte [] copy = idBytes;
            idBytes = new byte[20];
            for (int i = 0; i < 20; i++)
                idBytes[i] = copy[i];
        }
        nodeID = coder.encode(idBytes);
        p  = coder.encode(pBytes);
        q  = coder.encode(qBytes);
        d  = coder.encode(dBytes);
        e  = coder.encode(eBytes);

        id        = "  <id>" + nodeID + "</id>\n";
        rsa       = "  <rsa>\n"
                     + "    <p>" + p + "</p><q>" + q + "</q>\n"
                     + "    <d>" + d + "</d><e>" + e + "</e>\n"
                     + "  </rsa>\n";
        xmlNoDefaults = xmlHeader + start + id + rsa + latStart
                             + httpd + memcache
                           + latEnd + xmlEnd;
        xmlWithoutID  =  xmlHeader + start + rsa  + latStart
                             + httpd + memcache
                           + latEnd + xmlEnd;
        xmlWithoutRSA =  xmlHeader + start + id  + latStart
                             + httpd + memcache
                           + latEnd + xmlEnd;
    }

    public void setUp () {
    }
   
    /**
     * Use the xlattice.xml mapping to translate the sample configuration
     * from XML into an object tree and then back again into a second 
     * XML configuration.  If the two documents are the same, we should
     * be happy.
     */
    public void testMappingNoDefaults ()        throws Exception {
        Mapping map  = Configurer.buildMapping();
        Document doc =  new XmlParser (new StringReader(xmlNoDefaults))
                            .read();
        // java.lang.LinkageError:
        // Class org/xlattice/corexml/om/Document violates loader constraints
        Object o     = map.apply(doc);
        NodeConfig nc = (NodeConfig)o;
       
        // RSA Key
        assertNotNull(nc.getKey());

        // verify NodeID picked up correctly
        byte[] id = nc.getNodeID().value();
        assertNotNull(id);
        for (int i = 0; i < 20; i++)
            assertEquals( idBytes[i], id[i]);
        
        assertEquals (2, nc.sizeOverlay());
        OverlayConfig lc0 = nc.getOverlay(0);
        assertNotNull(lc0);
        assertEquals("httpd", lc0.getDir());
        assertEquals("org.xlattice.Httpd", lc0.getClassName());
        
        OverlayConfig lc1 = nc.getOverlay(1);
        assertEquals("memcache", lc1.getDir());
        assertEquals("org.xlattice.lattice.MemCache", lc1.getClassName());

        Document doc2 = map.generate (nc);
        assertSameSerialization(xmlNoDefaults, doc2.toXml());
    }  
    public void testMappingWithoutRSA ()        throws Exception {
        Mapping map  = Configurer.buildMapping();
        Document doc =  new XmlParser (new StringReader(xmlWithoutRSA))
                            .read();
        Object o     = map.apply(doc);
        NodeConfig nc = (NodeConfig)o;
   
        // we have not supplied a mapping
        assertNull(nc.getKey()); 
        
        assertEquals (2, nc.sizeOverlay());
        OverlayConfig lc0 = nc.getOverlay(0);
        assertNotNull(lc0);
        assertEquals("httpd", lc0.getDir());
        assertEquals("org.xlattice.Httpd", lc0.getClassName());
        
        OverlayConfig lc1 = nc.getOverlay(1);
        assertEquals("memcache", lc1.getDir());
        assertEquals("org.xlattice.lattice.MemCache", lc1.getClassName());
        
        Document doc2 = map.generate (nc);
        assertSameSerialization(xmlWithoutRSA, doc2.toXml());
    } 
    public void testMappingWithoutNodeID ()     throws Exception {
        Mapping map  = Configurer.buildMapping();
        Document doc =  new XmlParser (new StringReader(xmlWithoutID))
                            .read();
        Object o     = map.apply(doc);
        NodeConfig nc = (NodeConfig)o;
   
        // we have supplied a mapping
        assertNotNull(nc.getKey()); 
        // ... but no NodeID
        assertNull(nc.getNodeID());

        assertEquals (2, nc.sizeOverlay());
        OverlayConfig lc0 = nc.getOverlay(0);
        assertNotNull(lc0);
        assertEquals("httpd", lc0.getDir());
        assertEquals("org.xlattice.Httpd", lc0.getClassName());
        
        OverlayConfig lc1 = nc.getOverlay(1);
        assertEquals("memcache", lc1.getDir());
        assertEquals("org.xlattice.lattice.MemCache", lc1.getClassName());
    }
    
}
