/* Zip.java */
package org.xlattice.projmgr.antOM;

/**
 * Zip.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Zip implements TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _basedir;
    private String     _destfile;
    private String     _excludes;
    private String     _includes;
    private FileSet    _fileSet;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Zip () {
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
    public String getIncludes() {
        return _includes;
    }
    public void setIncludes(String value) {
        _includes = value;
    }
    public FileSet getFileSet() {
        return _fileSet;
    }
    public void setFileSet(FileSet value) {
        _fileSet = value;
    }
}
