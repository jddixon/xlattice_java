/* SiteInfo.java */
package org.xlattice.httpd.sitemaker;

/**
 * Container for information about individual Web sites, used
 * by SiteMaker.
 *
 * @author Jim Dixon
 */
public class SiteInfo {

    private String _name;
    private int    _port = 80;
    private String _dir;

    // CONSTRUCTOR //////////////////////////////////////////////////
    public SiteInfo () {}

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the (fully qualified domain) name of the Web site */
    public String getName() {
        return _name;
    }
    /**
     * @param name Web site name, usually a fully qualified domain name
     */
    public void setName(String name) {
        SiteConfig.checkNeitherNullNorEmpty(name);
        _name = name;
    }
    /**
     * @return IP port number
     */
    public int getPort() {
        return _port;
    } 
    /**
     * @param port number in the range 1..65535 
     */
    public void setPort (int port) {
        if (port < 1 || port > 65535)
            throw new IllegalArgumentException (
                    "port number out of range: " + port);
    }
    /** @return where site source files are */
    public String getDir() {
        return _dir;
    }
    /**
     * @param dir directory in which site source files are located
     */
    public void setDir(String dir) {
        SiteConfig.checkNeitherNullNorEmpty(dir);
        _dir = dir;
    }
}
