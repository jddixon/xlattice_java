/* ProjectReader.java */
package org.xlattice.projmgr;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.xlattice.Context;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.expr.Expr;
import org.xlattice.corexml.expr.ExprParser;
import org.xlattice.corexml.expr.NodeSet;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.XmlParser;

/**
 * Uses XPath to read project.xml, generating a Project data structure,
 * optionally including a parent Project.
 * 
 * @author Jim Dixon
 */
public class ProjectReader {

    private final XmlParser cxp;
    private final Project   p;
    private final Project   myChild;
    private Document  doc;
    private boolean   recursed;
                    
    /**
     * 
     */
    public ProjectReader (Reader reader) throws CoreXmlException {
        this(reader, null);
    }
    public ProjectReader (Reader reader, Project child) 
                                        throws CoreXmlException {
        myChild = child;
        cxp = new XmlParser(reader);
        p   = new Project();
        recursed = myChild != null;
        // DEBUG
        if (recursed) 
            System.out.println("ProjectReader: RECURSING");
        // END
    }

    /**
     * Convert the configuration file to XML, then walk the OM tree
     * to populate a Project instance, and Dependency objects.
     * The conversion uses XPath expressions.
     *
     * @throws IOException if there is an error reading the file 
     */
    public Project read () throws CoreXmlException, IOException {
        doc = cxp.read();
        Expr result;
        NodeSet nodes = null;
       
        ExprParser parser = new ExprParser(doc, new Context());

        nodes = (NodeSet)parser.eval("/project/extends");
        if (nodes != null && nodes.size() > 0) {
            if (recursed) 
                throw new CoreXmlException(
                        "project.xml files cannot recurse more than once");
            String parentFile = parser.string(nodes);
            p.setParentFile(parentFile);
            // DEBUG
            System.out.println("ABOUT TO READ PARENT PROJECT FILE " 
                    + parentFile);
            // END
            try {
                ProjectReader recursingReader = new ProjectReader(
                        new FileReader(parentFile), p);
                Project parent = recursingReader.read();
                p.setParent(parent);
                
            } catch (IOException ioe) {
                throw new CoreXmlException (
                    "error reading parent project.xml: " + ioe);
            }
        }
        nodes  = (NodeSet)parser.eval("/project/projectName");
        if (nodes.size() > 0)
            p.setProjectName( parser.string(nodes) );

        nodes  = (NodeSet)parser.eval("/project/id");
        if (nodes.size() > 0)
            p.setId( parser.string(nodes) );

        nodes  = (NodeSet)parser.eval("/project/name");
        if (nodes.size() > 0)
            p.setName( parser.string(nodes) );

        nodes  = (NodeSet)parser.eval("/project/version");
        if (nodes.size() > 0)
            p.setVersion( parser.string(nodes) );

        nodes  = (NodeSet)parser.eval("/project/libDir");
        if (nodes.size() > 0)
            p.setLibDir( parser.string(nodes) );

        nodes  = (NodeSet)parser.eval("/project/package");
        if (nodes.size() > 0)
            p.setPackageName( parser.string(nodes) );

        // BUILD-RELATED ////////////////////////////////////////////
        nodes = (NodeSet)parser.eval("/project/dependencies/dependency");
        int count = nodes.size();
        for (int i = 0; i < count; i++) {
            Dependency d = new Dependency ();
            String queryBase = new StringBuffer()
                .append("/project/dependencies/dependency[")
                .append(i + 1).append("]/").toString();

            // groupId, artifactId, version, type, url
            d.setGroupId(parser.string(
                    (NodeSet) parser.eval(queryBase + "groupId"))); 
            d.setArtifactId(parser.string(
                    (NodeSet) parser.eval(queryBase + "artifactId"))); 
            d.setVersion(parser.string(
                    (NodeSet) parser.eval(queryBase + "version"))); 
            
            nodes = (NodeSet) parser.eval(queryBase + "type");
            if (nodes != null && nodes.size() > 0)
                d.setType(parser.string(nodes));

            d.setUrl(parser.string(
                    (NodeSet) parser.eval(queryBase + "url"))); 

            p.addDependency(d);
            
        }
        
        // WEB SITE GENERATION //////////////////////////////////////
        
        // project description ////////////////////////////
        nodes  = (NodeSet)parser.eval("/project/description");
        if (nodes.size() > 0)
            p.setDescription( parser.string(nodes) );
        
        nodes  = (NodeSet)parser.eval("/project/shortDescription");
        if (nodes.size() > 0)
            p.setShortDescription( parser.string(nodes) );
        
        nodes  = (NodeSet)parser.eval("/project/logo");
        if (nodes.size() > 0)
            p.setLogo( parser.string(nodes) );

        // can get a NumberFormatException here
        nodes = (NodeSet)parser.eval("/project/inceptionYear");
        if (nodes.size() > 0)
            p.setStartYear( Integer.parseInt(parser.string(nodes)) );
      
        // owning organization ////////////////////////////
        nodes  = (NodeSet)parser.eval("/project/organization/name");
        if (nodes.size() > 0)
            p.setOrgName( parser.string(nodes) );

        nodes  = (NodeSet)parser.eval("/project/organization/url");
        if (nodes.size() > 0)
            p.setOrgUrl( parser.string(nodes) );

        nodes  = (NodeSet)parser.eval("/project/organization/logo");
        if (nodes.size() > 0)
            p.setOrgLogo( parser.string(nodes) );

        return p;
    }
}
