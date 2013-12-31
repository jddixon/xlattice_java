/* AntBuildFile.java */

package org.xlattice.projmgr;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.bind.*;
import org.xlattice.corexml.om.*;

/**
 * This will replace AntBuild.java
 *
 * @author Jim Dixon
 */
public class AntBuildFile {

    /** antOM class names, needed for mapping */
    public final static String CL_AntComment =
                    "org.xlattice.projmgr.antOM.AntComment";
    public final static String CL_Arg =
                    "org.xlattice.projmgr.antOM.Arg";
    public final static String CL_BatchTest =
                    "org.xlattice.projmgr.antOM.BatchTest";
    public final static String CL_Classpath =
                    "org.xlattice.projmgr.antOM.Classpath";
    public final static String CL_CommandLineArgElm =
                    "org.xlattice.projmgr.antOM.CommandLineArgElm";
    public final static String CL_Copy =
                    "org.xlattice.projmgr.antOM.Copy";
    public final static String CL_Delete =
                    "org.xlattice.projmgr.antOM.Delete";
    public final static String CL_Env =
                    "org.xlattice.projmgr.antOM.Env";
    public final static String CL_Exclude =
                    "org.xlattice.projmgr.antOM.Exclude";
    public final static String CL_ExecElm =
                    "org.xlattice.projmgr.antOM.ExecElm";
    public final static String CL_Exec =
                    "org.xlattice.projmgr.antOM.Exec";
    public final static String CL_FileSetElm =
                    "org.xlattice.projmgr.antOM.FileSetElm";
    public final static String CL_FileSet =
                    "org.xlattice.projmgr.antOM.FileSet";
    public final static String CL_Format =
                    "org.xlattice.projmgr.antOM.Format";
    public final static String CL_Formatter =
                    "org.xlattice.projmgr.antOM.Formatter";
    public final static String CL_Include =
                    "org.xlattice.projmgr.antOM.Include";
    public final static String CL_Jar =
                    "org.xlattice.projmgr.antOM.Jar";
    public final static String CL_JavacElm =
                    "org.xlattice.projmgr.antOM.JavacElm";
    public final static String CL_Javac =
                    "org.xlattice.projmgr.antOM.Javac";
    public final static String CL_JavadocElm =
                    "org.xlattice.projmgr.antOM.JavadocElm";
    public final static String CL_Javadoc =
                    "org.xlattice.projmgr.antOM.Javadoc";
    public final static String CL_JunitElm =
                    "org.xlattice.projmgr.antOM.JunitElm";
    public final static String CL_Junit =
                    "org.xlattice.projmgr.antOM.Junit";
    public final static String CL_Jvmarg =
                    "org.xlattice.projmgr.antOM.Jvmarg";
    public final static String CL_Mkdir =
                    "org.xlattice.projmgr.antOM.Mkdir";
    public final static String CL_Pathelement =
                    "org.xlattice.projmgr.antOM.Pathelement";
    public final static String CL_PathElement =
                    "org.xlattice.projmgr.antOM.PathElement";
    public final static String CL_PathlikeElm =
                    "org.xlattice.projmgr.antOM.PathlikeElm";
    public final static String CL_ProjectElm =
                    "org.xlattice.projmgr.antOM.ProjectElm";
    public final static String CL_Project =
                    "org.xlattice.projmgr.antOM.Project";
    public final static String CL_Property =
                    "org.xlattice.projmgr.antOM.Property";
    public final static String CL_Src =
                    "org.xlattice.projmgr.antOM.Src";
    public final static String CL_Sysproperty =
                    "org.xlattice.projmgr.antOM.Sysproperty";
    public final static String CL_TarFileSet =
                    "org.xlattice.projmgr.antOM.TarFileSet";
    public final static String CL_TargetElm =
                    "org.xlattice.projmgr.antOM.TargetElm";
    public final static String CL_Target =
                    "org.xlattice.projmgr.antOM.Target";
    public final static String CL_Tar =
                    "org.xlattice.projmgr.antOM.Tar";
    public final static String CL_Tstamp =
                    "org.xlattice.projmgr.antOM.Tstamp";
    public final static String CL_Zip =
                    "org.xlattice.projmgr.antOM.Zip";
    /** binding between XML and the antOM classes */
    private static Mapping map;


    // XML-AntOM MAPPING ////////////////////////////////////////////
    /**
     * @return a reference to the XML configuration file mapping
     */
    public static Mapping getMap()          throws CoreXmlException {
        if (map == null)
            buildMapping();
        return map;
    }
    /**
     * Construct the data binding that translates between the 
     * XML representation of the node's configuration and a set
     * of objects conveying the same information.
     */
    public static Mapping buildMapping () throws CoreXmlException {
        /* 
        // mapping between XML and RSAInfo class
        SubMapping subKey = new SubMapping("rsa", 
                                "org.xlattice.node.RSAInfo", "key")
                    .add(new SubElBinding   ("p"))
                    .add(new SubElBinding   ("q"))
                    .add(new SubElBinding   ("d"))
                    .add(new SubElBinding   ("e"))
                    .optional();
       
        // descriptions of individual overlays
        SubMapping overlay = new SubMapping ("overlay", 
                                "org.xlattice.node.OverlayConfig", "overlay")
                                .add(new AttrBinding("dir"))
                                .add(new AttrBinding("class")
                                        .setGetter("getClassName")
                                        .setSetter("setClassName")
                                        )
                                .repeats();
        // groups together overlay descriptions
        Collector overlays = new Collector ("overlays")
                    .add(overlay);
        */
        
        // top element ////////////////////////////////////
        map = new Mapping ("project", CL_Project)
                .add(new AttrBinding("default")
                        .setGetter("getDefaultTarget")
                        .setSetter("setDefaultTarget"))
                .add(new AttrBinding("name"))
                .add(new AttrBinding("basedir"))
                /*
                .add(new SubElBinding ("id").optional())    // the nodeID
                .add(subKey)                                // RSA key
                .add(overlays)
                */
                ;
        // we're done
        map.join();
        return map;
    }

}
