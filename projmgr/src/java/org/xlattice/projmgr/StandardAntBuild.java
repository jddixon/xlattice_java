/* StandardAntBuild.java */
package org.xlattice.projmgr;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author Jim Dixon
 */

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.NodeList;
import org.xlattice.corexml.om.XmlParser;

public class StandardAntBuild extends AntBuild {

    /**
     * @param name         short name (id) of the project
     * @param version      project version 
     * @param properties   properties to be added to the Ant build.xml
     * @param dependencies jar files need by the project and where to get them
     */
    public StandardAntBuild (Project p) {
        super(p);
        haveSampleData = false;
        nodes.moveFrom(BuildElementFactory.makeSourceProperties(width))
             .moveFrom(BuildElementFactory.makeDocProperties(width))
             .moveFrom(BuildElementFactory.makeBuildProperties(width))
             .moveFrom(BuildElementFactory.makeDistProperties(width));
        NodeList initNodes = BuildElementFactory.makeInitSection(width);
        // XXX KLUDGE -- depends upon knowing exactly how long the list is!
        NodeList initTargetNodes = ((Element)initNodes.get(3)).getNodeList();
        nodes.moveFrom(initNodes);
        if (deps.length > 0)
             nodes.moveFrom(BuildElementFactory.makeGetDepsSection (
                                                    initTargetNodes,
                                                    deps, libdir, width));
        nodes.moveFrom(BuildElementFactory.makeCompileTarget(
                                                    deps, libdir, width))
             .moveFrom(BuildElementFactory.makeCompileTestsTarget(
                                                    deps, libdir, width));
        // this should not actually be here
        if (haveSampleData)
            nodes
             .moveFrom(BuildElementFactory.makeCompileSamplesTarget(
                                                    deps, libdir, width));
        nodes.moveFrom(BuildElementFactory.makeTestsTarget(width, deps,
                                                            haveSampleData))
             .moveFrom(BuildElementFactory.makeJarTarget(width))
             .moveFrom(BuildElementFactory.makeJavadocTarget(width, 
                                                      name, orgName, pkg))
             .moveFrom(BuildElementFactory.makeClean(width));
    }   
}
