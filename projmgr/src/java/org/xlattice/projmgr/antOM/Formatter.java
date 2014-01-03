/* Formatter.java */
package org.xlattice.projmgr.antOM;

/**
 * Formatter.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Formatter implements JunitElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _classname;
    private String     _type;
    private String     _extension;
    private boolean    _usefile;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Formatter () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getClassname() {
        return _classname;
    }
    public void setClassname(String value) {
        _classname = value;
    }
    public String getType() {
        return _type;
    }
    public void setType(String value) {
        _type = value;
    }
    public String getExtension() {
        return _extension;
    }
    public void setExtension(String value) {
        _extension = value;
    }
    public boolean getUsefile() {
        return _usefile;
    }
    public void setUsefile(boolean value) {
        _usefile = value;
    }
}
