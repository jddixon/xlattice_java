/* Arg.java */
package org.xlattice.projmgr.antOM;

/**
 * Arg.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Arg implements CommandLineArgElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _file;
    private String     _path;
    private String     _value;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Arg () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getFile() {
        return _file;
    }
    public void setFile(String value) {
        _file = value;
    }
    public String getPath() {
        return _path;
    }
    public void setPath(String value) {
        _path = value;
    }
    public String getValue() {
        return _value;
    }
    public void setValue(String value) {
        _value = value;
    }
}
