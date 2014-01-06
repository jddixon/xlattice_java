/* Sysproperty.java */
package org.xlattice.projmgr.antOM;

/**
 * Sysproperty.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Sysproperty implements JunitElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _key;
    private String     _value;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Sysproperty () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getKey() {
        return _key;
    }
    public void setKey(String value) {
        _key = value;
    }
    public String getValue() {
        return _value;
    }
    public void setValue(String value) {
        _value = value;
    }
}
