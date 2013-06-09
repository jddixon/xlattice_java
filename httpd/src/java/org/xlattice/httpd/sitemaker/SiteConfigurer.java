/* SiteConfigurer.java */
package org.xlattice.httpd.sitemaker;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

/**
 * @author Jim Dixon
 */

import org.xlattice.CryptoException;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.bind.*;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.XmlParser;
//import org.xlattice.util.cmdline.Bindery;
import org.xlattice.util.cmdline.BooleanOpt;
import org.xlattice.util.cmdline.CmdLineOpt;

/**
 * Converts between a site configuration file in XML form and 
 * its object representation.
 * 
 * In its current incarnation, the configuration file is an XML
 * file following this form:
 * <pre>
 *   &lt;siteconfig&gt;
 *     &lt;nodeinfo dir="DIRNAME" master="MASTER_NAME"
 *                                nodekey = "NODE_DOMAIN_NAME /&gt;
 *     &lt;sites&gt;
 *       &lt;site&gt; name="SITE_A" dir="DIR_A"/&gt;
 *       &lt;site&gt; name="SITE_B" dir="DIR_B"/&gt;
 *       &lt;site&gt; name="SITE_C" dir="DIR_C"/&gt;
 *       ...
 *     &lt;/sites&gt;
 *     &lt;output dir="dirName"/&gt;
 *  &lt;/siteconfig&gt;
 * </pre>
 *
 * The <b>nodeinfo</b> line is mapped into a <code>NodeInfo</code>
 * object.
 *
 * Each of the <b>site</b> lines maps into a <code>SiteInfo</code>
 * object, giving its fully qualified domain name (like www.xlattice.org)
 * and the directory where the Web site's files are to be found.
 *
 * The <b>output</b> line specifies where the generated Web site is
 * to be written.  After SiteMaker is invoked, the output directory 
 * will contain an xlattice/ subdirectory.
 *
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class SiteConfigurer {

    // CONSTRUCTORS /////////////////////////////////////////////////
    private SiteConfigurer () {}
    
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Given xlattice.xml, the site configuration file, set up the
     * configuration for the site, a SiteConfig object.
     * 
     * @param reader an open site configuration file or String equivalent
     */
    public static SiteConfig configureSite (Reader reader) 
                throws CoreXmlException, CryptoException, IOException {
        Document doc  =  new XmlParser (reader).read();
        SiteConfig sc = (SiteConfig) getMap().apply(doc);
        reader.close();
        return sc;
    }
    // MAPPING //////////////////////////////////////////////////////
    private static Mapping map;
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
        // mapping between XML and NodeInfo class
        SubMapping nodeInfo = new SubMapping("nodeinfo", 
                                "org.xlattice.httpd.sitemaker.NodeInfo", 
                                "nodeInfo")
                                .add(new AttrBinding("dir"))
                                .add(new AttrBinding("master"))
                                .add(new AttrBinding("nodekey")
                                        .setGetter("getNodeKey")
                                        .setSetter("setNodeKey"));
       
        // descriptions of individual sites
        SubMapping site = new SubMapping ("site", 
                                "org.xlattice.httpd.sitemaker.SiteInfo", 
                                "site")
                                .add(new AttrBinding   ("name"))
                                .add(new AttrBinding   ("port"))
                                .add(new AttrBinding   ("dir"))
                                .repeats();

        // groups together site descriptions
        Collector sites = new Collector ("sites")
                                .add(site);
       
        // output directory
        SubMapping output = new SubMapping ("output",
                                "org.xlattice.httpd.sitemaker.OutputInfo",
                                "output")
                                .add(new AttrBinding ("dir"));
        // top element
        map = new Mapping ("siteconfig", 
                           "org.xlattice.httpd.sitemaker.SiteConfig")
                .add(nodeInfo)                                // RSA key
                .add(sites)
                .add(output);
        // we're done
        map.join();
        return map;
    }
}
