/* AntComment.java */
package org.xlattice.projmgr.antOM;

/**
 * AntComment.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class AntComment implements ProjectElm,TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _text;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public AntComment () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getText() {
        return _text;
    }
    public void setText(String value) {
        _text = value;
    }
}
