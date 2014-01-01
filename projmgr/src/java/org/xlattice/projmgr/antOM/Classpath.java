/* Classpath.java */
package org.xlattice.projmgr.antOM;

/**
 * Classpath.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Classpath implements JavadocElm,JavacElm,JunitElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _location;
    private String     _path;
    private PathlikeElm _pathlikeElm;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Classpath () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getLocation() {
        return _location;
    }
    public void setLocation(String value) {
        _location = value;
    }
    public String getPath() {
        return _path;
    }
    public void setPath(String value) {
        _path = value;
    }
    public PathlikeElm getPathlikeElm() {
        return _pathlikeElm;
    }
    public void setPathlikeElm(PathlikeElm value) {
        _pathlikeElm = value;
    }
}
