/* Exclude.java */
package org.xlattice.projmgr.antOM;

/**
 * Exclude.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Exclude implements FileSetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _name;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Exclude () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getName() {
        return _name;
    }
    public void setName(String value) {
        _name = value;
    }
}
