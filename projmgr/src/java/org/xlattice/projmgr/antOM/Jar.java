/* Jar.java */
package org.xlattice.projmgr.antOM;

/**
 * Jar.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Jar implements TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _basedir;
    private String     _destfile;
    private String     _excludes;
    private String     _manifest;
    private PathlikeElm _pathlikeElm;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Jar () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getBasedir() {
        return _basedir;
    }
    public void setBasedir(String value) {
        _basedir = value;
    }
    public String getDestfile() {
        return _destfile;
    }
    public void setDestfile(String value) {
        _destfile = value;
    }
    public String getExcludes() {
        return _excludes;
    }
    public void setExcludes(String value) {
        _excludes = value;
    }
    public String getManifest() {
        return _manifest;
    }
    public void setManifest(String value) {
        _manifest = value;
    }
    public PathlikeElm getPathlikeElm() {
        return _pathlikeElm;
    }
    public void setPathlikeElm(PathlikeElm value) {
        _pathlikeElm = value;
    }
}
