/* Tstamp.java */
package org.xlattice.projmgr.antOM;

/**
 * Tstamp.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Tstamp implements TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private Format     _format;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Tstamp () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public Format getFormat() {
        return _format;
    }
    public void setFormat(Format value) {
        _format = value;
    }
}
