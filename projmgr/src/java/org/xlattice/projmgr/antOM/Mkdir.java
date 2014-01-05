/* Mkdir.java */
package org.xlattice.projmgr.antOM;

/**
 * Mkdir.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Mkdir implements TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _dir;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Mkdir () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getDir() {
        return _dir;
    }
    public void setDir(String value) {
        _dir = value;
    }
}
