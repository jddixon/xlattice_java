/* TestProject.java */
package org.xlattice.projmgr;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

public class TestProject extends CoreXmlTestCase {

    Project project;

    Dependency [] deps;
    public TestProject (String name) {
        super(name);
    }
    public void setUp() {
        project = new Project();
        deps = new Dependency [4];
        for (int i = 0; i < deps.length; i++)
            deps[i] = new Dependency();
        deps[0].setGroupId("boys").setArtifactId("tom")    .setVersion("1.2");
        deps[1].setGroupId("boys").setArtifactId("dick");
        deps[2].setGroupId("boys").setArtifactId("harry");
        deps[3].setGroupId("boys").setArtifactId("joe");
    }
    public void tearDown() {
    }
    public void testEmptyProject() {
        assertFalse(project.isWellFormed());
        Dependency[] d = project.getDependencies();
        assertNotNull(d);
        assertEquals(0, d.length);
    }
    public void testTheSimpleStuff() {
        project.setId("test").setName("Test Project")
               .setOrgName("Disorganization Inc").setPackageName("org.test")
               .setStartYear(2004)
               .setVersion("0.00a0");
        assertTrue("project object not well-formed", project.isWellFormed());
        for (int i = 0; i < deps.length; i++)
            project.addDependency(deps[i]);
        for (int i = 0; i < deps.length; i++)
            assertTrue ( deps[i] == project.getDependency(i) );
        Dependency [] moreDeps = project.getDependencies();
        for (int i = 0; i < deps.length; i++)
            assertTrue ( deps[i] == moreDeps[i] );
    }
}
