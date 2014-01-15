/* TestCmdFiles.java */
package org.xlattice.projmgr;

import java.io.Writer;
import java.io.StringWriter;
import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.corexml.CoreXmlTestCase;

public class TestCmdFiles extends CoreXmlTestCase {

    Project project;
    Dependency [] deps;


    public TestCmdFiles (String name) {
        super(name);
    }
    public void setUp() {
        project = new Project();
        project.setLibDir("../lib");
        deps    = new Dependency[5];
        deps[0] = new Dependency();
        deps[1] = new Dependency();
        deps[2] = new Dependency();
        deps[3] = new Dependency();
        deps[4] = new Dependency();
     
        // version should be 1.5.4
        deps[0].setGroupId("ant");
        deps[0].setArtifactId("ant");
        deps[0].setUrl("http://jakarta.apache.org/ant/");
        
        // version should be 1.5.4
        deps[1].setGroupId("ant");
        deps[1].setArtifactId("optional");
        deps[1].setUrl("http://jakarta.apache.org/ant/");
        
        deps[2].setGroupId("junit");
        deps[2].setArtifactId("junit");
        deps[2].setVersion("3.8.1");
        deps[2].setUrl("http://www.junit.org/");
        
        deps[3].setGroupId("xpp3");
        deps[3].setArtifactId("xpp3");
        deps[3].setVersion("1.1.3.4.C");
        deps[3].setUrl(
  "http://www.extreme.indiana.edu/dist/java-repository/xpp3/distributions/");
        
        deps[4].setGroupId("xlattice");
        deps[4].setArtifactId("util");
        deps[4].setVersion("0.1");
        deps[4].setUrl("http://www.xlattice.org/");

        for (int i = 0 ; i <= 4; i++)
            project.addDependency(deps[i]);
    }
    public void tearDown() {
    }
    public void testEmptyProject()                  throws Exception {
        assertNotNull(project);
        assertNotNull(project.getLibDir());
        CmdFiles cmdGen = new CmdFiles(project);
        assertNotNull(cmdGen);
        assertEquals("ant", cmdGen.getAntDep().getArtifactId());
        assertEquals("optional", cmdGen.getOptionalDep().getArtifactId());
        assertEquals("junit", cmdGen.getJunitDep().getGroupId());
        assertEquals(2, cmdGen.getOtherDeps().size());
    }
}
