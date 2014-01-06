/* TarFileSet.java */
package org.xlattice.projmgr.antOM;

/**
 * TarFileSet.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class TarFileSet implements PathlikeElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _dir;
    private String     _excludes;
    private String     _file;
    private String     _includes;
    private FileSetElm _fileSetElm;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public TarFileSet () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getDir() {
        return _dir;
    }
    public void setDir(String value) {
        _dir = value;
    }
    public String getExcludes() {
        return _excludes;
    }
    public void setExcludes(String value) {
        _excludes = value;
    }
    public String getFile() {
        return _file;
    }
    public void setFile(String value) {
        _file = value;
    }
    public String getIncludes() {
        return _includes;
    }
    public void setIncludes(String value) {
        _includes = value;
    }
    public FileSetElm getFileSetElm() {
        return _fileSetElm;
    }
    public void setFileSetElm(FileSetElm value) {
        _fileSetElm = value;
    }
}
