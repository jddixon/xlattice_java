/* NodeInfo.java */
package org.xlattice.httpd.sitemaker;

import java.io.File;

/**
 * Container allowing SiteMaker to collect information relating to
 * the node.info file to be used by the utility.
 *
 * @author Jim Dixon
 */
public class NodeInfo {

    /** directory the node.info file is located in */
    private String _dir;
    /** name of the Web site master key */
    private String _masterKeyName;
    /** name of the node configuration file */
    private String _nodeKeyName;

    public NodeInfo () {}

    // PROPERTIES ///////////////////////////////////////////////////
    /** 
     * @return directory the node.info file is located in 
     */
    public String getDir() {
        return _dir;
    }
    /**
     * @param dir name of the directory node.info is expected to be found in
     */
    public void setDir (String dir) {
        SiteConfig.checkNeitherNullNorEmpty(dir);
        if (!dir.endsWith(File.separator))
            dir = new StringBuffer(dir).append(File.separator).toString();
        _dir = dir;
    }
    /** 
     * @return name of the Web site master key 
     */
    public String getMaster() {
        return _masterKeyName;
    }
    /**
     * Sets the name of the master key; this is typically a fully
     * qualified domain name, such as master.xlattice.org.  The
     * master key may be used to sign configuration files for any
     * number of sites and nodes.
     *
     * @param name name of the master key for the site
     */
    public void setMaster(String name) {
        SiteConfig.checkNeitherNullNorEmpty(name);
        _masterKeyName = name;
    }
    /** 
     * @return name of the node configuration file 
     */
    public String getNodeKey() {
        return _nodeKeyName;
    }
    /**
     * Sets the name of the configuration file for the node; this 
     * may be a fully qualified domain name, such as www.xlattice.org.
     * This name can be used to retrieve a skeletal node configuration 
     * file from node.info (if one does not exist, it will be created.) 
     * 
     * @param name name of the master key for the site
     */
    public void setNodeKey(String name) {
        SiteConfig.checkNeitherNullNorEmpty(name);
        _nodeKeyName = name;
    }
}
