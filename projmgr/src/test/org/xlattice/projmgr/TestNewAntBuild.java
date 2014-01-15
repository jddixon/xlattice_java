/* TestNewAntBuild.java */
package org.xlattice.projmgr;

import java.io.StringReader;
import junit.framework.*;
import org.xlattice.projmgr.antOM.*;

/**
 * Test the classes that form the object model behind Ant's build
 * file, build.xml, and the data binding that enables build.xml to
 * be read and written.
 * 
 * @author Jim Dixon
 */

import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.bind.*;
import org.xlattice.corexml.om.*;
import org.xlattice.projmgr.antOM.*;

public class TestNewAntBuild                extends CoreXmlTestCase {

    /** a fragmented Ant build.xml file */
    public final String HEADER = Document.DEFAULT_XML_DECL;
    public final String PROJECT_OPEN =
        "<project default=\"test\" name=\"corexml\" basedir=\".\">";
   
    /*
     * stuff in junk.build.xml goes here
     */
    public final String PROJECT_CLOSE = "</project>";
   
    // INSTANCE VARIABLES ///////////////////////////////////////////
    private final Mapping map;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public TestNewAntBuild (String name)        throws Exception {
        super(name);
        map = AntBuildFile.getMap();
    }
    public void setUp() {
    }
    public void tearDown() {
    }
    public void testEmptyProject()              throws Exception {
        String emptyProjectXml = HEADER + PROJECT_OPEN + PROJECT_CLOSE;

        Document doc = new XmlParser (new StringReader(emptyProjectXml))
                        .read();
        Object o = map.apply(doc);
        assertNotNull(o);
        assertTrue (o instanceof org.xlattice.projmgr.antOM.Project);
        // name collision :-(
        org.xlattice.projmgr.antOM.Project proj 
                                = (org.xlattice.projmgr.antOM.Project)o;
        assertEquals (".",          proj.getBasedir());
        assertEquals ("test",       proj.getDefaultTarget());
        assertEquals ("corexml",    proj.getName());
        Document docOut = map.generate(proj);
        assertSameSerialization( emptyProjectXml,   docOut.toXml() );
    }
}
