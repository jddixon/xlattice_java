/* Tar.java */
package org.xlattice.projmgr.antOM;

/**
 * Tar.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Tar implements TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _basedir;
    private String     _destfile;
    private String     _excludes;
    private String     _longfile;
    private TarFileSet _tarFileSet;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Tar () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getBasedir() {
        return _basedir;
    }
    public void setBasedir(String value) {
        _basedir = value;
    }
    public String getDestfile() {
        return _destfile;
    }
    public void setDestfile(String value) {
        _destfile = value;
    }
    public String getExcludes() {
        return _excludes;
    }
    public void setExcludes(String value) {
        _excludes = value;
    }
    public String getLongfile() {
        return _longfile;
    }
    public void setLongfile(String value) {
        _longfile = value;
    }
    public TarFileSet getTarFileSet() {
        return _tarFileSet;
    }
    public void setTarFileSet(TarFileSet value) {
        _tarFileSet = value;
    }
}
