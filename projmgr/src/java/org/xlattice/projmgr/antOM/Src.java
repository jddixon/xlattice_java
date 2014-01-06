/* Src.java */
package org.xlattice.projmgr.antOM;

/**
 * Src.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Src implements JavacElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private PathlikeElm _pathlikeElm;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Src () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public PathlikeElm getPathlikeElm() {
        return _pathlikeElm;
    }
    public void setPathlikeElm(PathlikeElm value) {
        _pathlikeElm = value;
    }
}
