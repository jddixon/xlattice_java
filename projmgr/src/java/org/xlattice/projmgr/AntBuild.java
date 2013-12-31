/* AntBuild.java */

package org.xlattice.projmgr;

import java.io.IOException;
import java.io.StringReader;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.NodeList;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.XmlParser;

/**
 * @author Jim Dixon
 */

public abstract class AntBuild {
    
    protected final Project project;
    /** copied from the Project object */ 
    protected final String projectName;
    protected final String id;
    protected final String libdir;
    protected final String name;
    protected final String orgName;
    protected final String pkg;
    protected final int    startYear;
    protected final String version;
    
    protected final Dependency[] deps;
    
    protected Document  doc;
    protected Element   root;
    protected NodeList nodes;
  
    /** SHOULD GET FROM project.xml */
    protected boolean haveSampleData = false;
    
    /** width of comments, not including the bracketing characters */
    protected int width = 65;

    protected AntBuild (Project p) {
        if (p == null)
            throw new NullPointerException("null project");
        project   = p;
        projectName = project.getProjectName();
        if (projectName == null || projectName.equals(""))
            throw new IllegalStateException("null or missing project name");
        id        = project.getId();            // short name for project
        if (id == null || id.equals(""))
            throw new IllegalStateException("null or missing component id");
        name      = project.getName();          // longer name
        if (name == null || name.equals(""))
            throw new IllegalStateException("null or missing component name");
        version   = project.getVersion();
        if (version == null || version.equals(""))
            throw new IllegalStateException("null or missing project version");
        libdir    = project.getLibDir();
        pkg       = project.getPackageName();
        if (pkg == null || pkg.equals(""))
            throw new IllegalStateException("null or missing project pkg");

        // BUILD-RELATED //////////////////////////////////
        deps      = project.getDependencies();

        // WEB SITE GENERATTION ///////////////////////////
        orgName   = project.getOrgName();
        startYear = project.getStartYear();
        

        doc = new Document();
        root = new Element("project")
                    .addAttr("default", "jar")
                    .addAttr("name",    id)
                    .addAttr("basedir", ".");
        doc.setElementNode(root);
        // XXX PROBLEM IN om
        doc.getNodeList().append(root);
        // XXX END PROBLEM
        nodes = root.getNodeList()
            .append(BuildElementFactory.makeProperty("name",    id)) 
            .append(BuildElementFactory.makeProperty("version", version))
            .append(BuildElementFactory.makeProperty("projectName",
                                                            projectName));
    }

    // PROPERTIES ///////////////////////////////////////////////////
    public Document getDoc() {
        return doc;
    }

    // NODE, so to speak ////////////////////////////////////////////
    public String toXml() {
        return doc.toXml();
    }
}
