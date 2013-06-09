/* TestSiteConfig.java */
package org.xlattice.httpd.sitemaker;

import java.io.StringReader;
import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.bind.Mapping;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.XmlParser;

/**
 *
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class TestSiteConfig extends CoreXmlTestCase {

    // XXX BUG IN xpp3? if a /> is incorrectly inserted at the end
    // of the second line, the parser accepts it (as text?) but then
    // org.xlattice.corexml.bind has a size of 3 on sites but throws
    // an index out of range error in get(2), saying that the size is 2.
    String testData =
          "<siteconfig>"
        +   "<nodeinfo dir=\"~\" master=\"master.xlattice.org\" "
        +                       "nodekey=\"ns1.xlattice.org\"/>"
        +   "<sites>"
        +     "<site name=\"www.xlattice.org\"   port=\"80\" dir=\"xlattice/target/docs/\"/>"
        +     "<site name=\"jxcl.xlattice.org\"  port=\"80\" dir=\"jxcl/target/docs/\"/>"
        +     "<site name=\"glass.xlattice.org\" port=\"80\" dir=\"glass/target/docs/\"/>"
        +   "</sites>"
        +   "<output dir=\"generated/\"/>"
        + "</siteconfig>";

    public TestSiteConfig (String name)         throws Exception{
        super(name);
    }
    public void testNoData()                    throws Exception {
        Mapping map  = SiteConfigurer.buildMapping();
        assertNotNull(map);
    }
    /*
    public void testWithData()                  throws Exception {
        Mapping map   = SiteConfigurer.buildMapping();
        Document doc  =  new XmlParser (new StringReader(testData))
                                         .read();
        Object o      = map.apply(doc);
        SiteConfig sc = (SiteConfig)o;
        NodeInfo ni   = sc.getNodeInfo();
        assertNotNull(ni);
        assertEquals ("master.xlattice.org",   ni.getMaster());
        assertEquals ("ns1.xlattice.org",      ni.getNodeKey());
        assertEquals (3, sc.sizeSite());
        SiteInfo si   = sc.getSite(0);
        assertEquals ("www.xlattice.org",      si.getName());
        assertEquals ("xlattice/target/docs/", si.getDir());
        OutputInfo oi = sc.getOutput();
        assertEquals("generated/", oi.getDir());
    }
    */
}
