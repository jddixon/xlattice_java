/* SiteConfig.java */
package org.xlattice.httpd.sitemaker;

import org.xlattice.util.ArrayStack;

/**
 * Container for Web site configuration information, for use with
 * org.xlattice.httpd.SiteMaker.
 *
 * SiteMaker needs
 *   o to know the location of the node.info directory, the master
 *     key for the site, and the name of the node configuration
 *   o for each site:
 *     o the associated domain name
 *     o the directory where the source files are to be found
 *   o and the name of a directory where the xlattice/ subdirectory
 *     is to be written
 *
 * This class is suitable for collecting that information and for
 * use with org.xlattice.corexml.bind to load from and store to XML.
 * 
 *
 * @author Jim Dixon
 */
public class SiteConfig {

    // PRIVATE MEMBERS //////////////////////////////////////////////
    private NodeInfo _nodeInfo;
    /** holds SiteInfo instances */
    private ArrayStack _sites = new ArrayStack();
    private OutputInfo _output;
   
    // CONSTRUCTOR //////////////////////////////////////////////////
    public SiteConfig () {}

    // PROPERTIES ///////////////////////////////////////////////////
    public NodeInfo getNodeInfo() {
        return _nodeInfo;
    }
    public void setNodeInfo(NodeInfo nodeInfo) {
        if (nodeInfo == null) 
            throw new IllegalArgumentException("null NodeInfo element");
        _nodeInfo = nodeInfo;
    }
    
    public OutputInfo getOutput() {
        return _output;
    }
    public void setOutput(OutputInfo outputInfo) {
        if(outputInfo == null || outputInfo.equals(""))
            throw new IllegalArgumentException(
                    "null or empty directory name");
        _output = outputInfo;
    }

    public void addSite (SiteInfo site) {
        _sites.push(site);
    }
    public SiteInfo getSite(int n) {
        return (SiteInfo)_sites.peek(n);
    }
    // FIX 2011-08-23 
    public int sizeSites() {
        return sizeSites();
    }
    // END FIX
    public int sizeSite() {
        return _sites.size();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public static void checkNeitherNullNorEmpty (String s) {
        if (s == null || s.equals(""))
            throw new IllegalArgumentException (s);
    }
}
