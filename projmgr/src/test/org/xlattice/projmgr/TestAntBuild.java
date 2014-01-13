/* TestAntBuild.java */
package org.xlattice.projmgr;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.corexml.om.*;

public class TestAntBuild extends TestCase {

    Project       project;
    Dependency [] deps;
    AntBuild      build;
    String        buildAsXml;
    Document      buildOm;

    public TestAntBuild (String name) {
        super(name);
    }
    public void setUp() {
        project = new Project();
        deps = new Dependency [4];
        for (int i = 0; i < deps.length; i++)
            deps[i] = new Dependency();
        deps[0].setGroupId("boys").setArtifactId("tom")  .setVersion("0.01");
        deps[1].setGroupId("boys").setArtifactId("dick") .setVersion("47.2a10").setType("txt");
        deps[2].setGroupId("boys").setArtifactId("harry").setVersion("9.1b12");
        deps[3].setGroupId("boys").setArtifactId("joe")  .setVersion("1.23");
        for (int i = 0; i < deps.length; i++){
            String gName = deps[i].getGroupId();
            String aName = deps[i].getArtifactId();
            deps[i].setUrl( "http://www." + gName + "Soft.com/stuff/" );
        }
        build   = null;
        buildAsXml = null;
        buildOm    = null;
    }
    public void tearDown() {
    }
    public void testStandardBuild() throws Exception {
        assertFalse(project.isWellFormed());
        
        project.setProjectName("OverallProject")    // may not be null
               // short and long component name
               .setId("shortName").setName("LongName")
               .setOrgName("Disorganization Inc").setPackageName("org.test")
               .setVersion("0.00a0").setStartYear(2004);
        assertTrue("project object is not well-formed", project.isWellFormed());
     
        project.setLibDir("${basedir}/../lib");

        for (int i = 0; i < deps.length; i++)
            project.addDependency(deps[i]);

        build = new StandardAntBuild(project);
        assertNotNull(build);
        
        buildAsXml = build.toXml();
        assertNotNull(buildAsXml);
        
        buildOm    = build.getDoc();
        assertNotNull(buildOm);
        assertTrue (buildOm instanceof Document);
        
        Element root = buildOm.getElementNode();
        assertNotNull(root);
        assertEquals("project", root.getName());

        // light entertainment
        // System.out.println(root.toXml());
    }
}
