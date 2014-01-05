/* Property.java */
package org.xlattice.projmgr.antOM;

/**
 * Property.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Property implements ProjectElm,TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _name;
    private String     _value;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Property () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getName() {
        return _name;
    }
    public void setName(String value) {
        _name = value;
    }
    public String getValue() {
        return _value;
    }
    public void setValue(String value) {
        _value = value;
    }
}
