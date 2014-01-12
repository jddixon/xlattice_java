/* TestProjectReader.java */
package org.xlattice.projmgr;

import java.io.StringReader;
import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

public class TestProjectReader extends CoreXmlTestCase {

    private ProjectReader pr;
    private StringReader  reader;
    private Document      doc;
    private Project       project;
    private Dependency [] deps;
  
    private String  id      ;
    private String  libdir  ;
    private String  name    ;
    private String  orgName ;
    private String  orgLogo ;
    private String  orgUrl  ;
    private Project parent  ;
    private String  pkg     ;
    private int     startYear;
    private String  version ;
    
    private final static String startTag = "<project>";
    private final static String parentTag (String parent) {
        // DEBUG
        System.out.println("invoking parentTag(\"" + parent + "\")");
        // END
        return new StringBuffer("<extends>").append(parent)
                        .append("</extends>").toString();
    }
    private final static String descTag(String desc) {
        return new StringBuffer("<desc>").append(desc)
                        .append("</desc>").toString();
    }
    private final static String idTag(String id) {
        return new StringBuffer("<id>").append(id)
                        .append("</id>").toString();
    }
    private final static String libDirTag(String libDir) {
        return new StringBuffer("<libDir>").append(libDir)
                        .append("</libDir>").toString();
    }
    private final static String nameTag(String name) {
        return new StringBuffer("<name>").append(name)
                        .append("</name>").toString();
    }
    private String makeOrgXml(String n, String l, String u) {
        return new StringBuffer("<organization>")
            .append("<name>").append(n).append("</name>")
            .append("<logo>").append(l).append("</logo>")
            .append("<url>").append(u).append("</url>")
            .append("</organization>").toString();
    }
    private final static String pkgTag(String pkg) {
        return new StringBuffer("<package>").append(pkg)
                        .append("</package>").toString();
    }
    private final static String startYearTag(int startYear) {
        return new StringBuffer("<inceptionYear>").append(startYear)
                        .append("</inceptionYear>").toString();
    }
    private final static String versionTag(String version) {
        return new StringBuffer("<version>").append(version)
                        .append("</version>").toString();
    }
    private final static String endTag   = "</project>";
   
    private String makeDepXml(String gId, String aId, String v, String url) {
        return new StringBuffer("<dependency>")
            .append("<groupId>")    .append(gId)    .append("</groupId>")
            .append("<artifactId>") .append(aId)    .append("</artifactId>")
            .append("<version>")    .append(v)      .append("</version>")
            .append("<url>")        .append(url)    .append("</url>")
            .append("</dependency>").toString();
    }
    public TestProjectReader (String name) {
        super(name);
    }
    public void setUp() {
        reader  = null;
        pr      = null;
        doc     = null;
        project = null;
        deps    = null;
        
        id       = "joe";
        libdir   = "../lib";
        name     = "Big Joe";
        orgName  = "The Mob";
        orgLogo  = "theFist.jpg";
        orgUrl   = "www.theMob.co.uk";
        parent   = null;
        pkg      = "org.xlattice.mob";
        startYear= 2001;
        version  = "1.42a7";
    }
    public void tearDown() {
    }
    public void testEmptyProject() throws Exception {
        reader  = new StringReader("");
        pr      = new ProjectReader(reader);
        project = pr.read();
        assertFalse(project.isWellFormed());
        Dependency[] d = project.getDependencies();
        assertNotNull(d);
        assertEquals(0, d.length);
    }
    public final String PARENT_FILE_NAME = 
                        "src/test/org/xlattice/projmgr/parent-project.xml";
    public String makeXml(boolean withDeps, boolean withParent) {
        StringBuffer sb = new StringBuffer(startTag);
        if (withParent) {
            sb.append(parentTag(PARENT_FILE_NAME));
        }
        sb.append(idTag(id))
          .append(nameTag(name))
          .append(versionTag(version))
          .append(libDirTag(libdir))
          .append(pkgTag(pkg))
          .append(makeOrgXml(orgName, orgLogo, orgUrl))
          .append(startYearTag(startYear));
        if (withDeps) {
            sb.append("<dependencies>");
            for (int i = 0; i < 4; i++) 
                sb.append(makeDepXml(
                            "gId" + i, "aId" + i, "0." + i, "url" + i) );
            sb.append("</dependencies>");
        }
        return sb.append(endTag).toString();
    }
    public void testNoDepProject()          throws Exception {
        reader  = new StringReader(makeXml(false, false));
        pr      = new ProjectReader(reader);
        project = pr.read();
        assertNotNull(project);
        assertTrue(project.isWellFormed());
        Dependency[] d = project.getDependencies();
        assertNotNull(d);
        assertEquals(0, d.length);
        assertEquals(id, project.getId());
        assertEquals(libdir, project.getLibDir());
        assertEquals(name, project.getName());
        assertEquals(orgName, project.getOrgName());
        assertEquals(pkg, project.getPackageName());
        assertEquals(startYear, project.getStartYear());
        assertEquals(version, project.getVersion());
    } 
    public void testWithDeps()              throws Exception {
        pr      = new ProjectReader(new StringReader(makeXml(true, false)));
        project = pr.read();
        assertNotNull(project);
        assertTrue(project.isWellFormed());
        Dependency[] d = project.getDependencies();
        assertNotNull(d);
        assertEquals(4, d.length);
        for (int i = 0; i < 4; i++) {
            assertEquals("gId" + i, d[i].getGroupId());
            assertEquals("aId" + i, d[i].getArtifactId());
            assertEquals("0."  + i, d[i].getVersion());
            assertEquals("jar",     d[i].getType());
            assertEquals("url" + i, d[i].getUrl());
        }
        // DEBUG
        System.out.println("project with dependencies:\n"
                         + "--------------------------");
        System.out.println( project.toXml() );
        // END
    }
    /**
     * XXX This 
     */
    public void testWithParent()            throws Exception {
        pr      = new ProjectReader(new StringReader(makeXml(true, true)));
        project = pr.read();
        // DEBUG
        // XXX SERIALIZATION IS INCORRECT: <description> tag isn't closed
        System.out.println("project with dependencies and parent:\n"
                         + "-------------------------------------");
        System.out.println( project.toXml() );
        // END
        
        assertTrue(project.isWellFormed());
        assertNotNull(project.getParent());
        assertEquals(PARENT_FILE_NAME,  project.getParentFile());

        Dependency[] d = project.getDependencies();
        assertNotNull(d);

        assertEquals(4, d.length);
        // XXX WORKING HERE.  It looks like the idea was that 
        // projects should inherit from their parent Projects.  That
        // is, if getLogo() would return null and there is a parent, 
        // it instead returns the value returned by parent.getLogo().
        // This is nowhere documented.
//      assertNotNull(project.getLogo());
//      assertEquals("/images/xlattice.jpg", project.getLogo());
    }
}
