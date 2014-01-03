/* Format.java */
package org.xlattice.projmgr.antOM;

/**
 * Format.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Format {

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _pattern;
    private String     _property;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Format () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getPattern() {
        return _pattern;
    }
    public void setPattern(String value) {
        _pattern = value;
    }
    public String getProperty() {
        return _property;
    }
    public void setProperty(String value) {
        _property = value;
    }
}
