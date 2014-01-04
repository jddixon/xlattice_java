/* Javadoc.java */
package org.xlattice.projmgr.antOM;

/**
 * Javadoc.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Javadoc implements TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private boolean    _author;
    private String     _destdir;
    private boolean    _overview;
    private boolean    _package;
    private String     _packagenames;
    private boolean    _protected;
    private boolean    _public;
    private String     _sourcepath;
    private boolean    _use;
    private boolean    _version;
    private JavadocElm _javadocElm;
    private FileSet    _fileSet;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Javadoc () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public boolean getAuthor() {
        return _author;
    }
    public void setAuthor(boolean value) {
        _author = value;
    }
    public String getDestdir() {
        return _destdir;
    }
    public void setDestdir(String value) {
        _destdir = value;
    }
    public boolean getOverview() {
        return _overview;
    }
    public void setOverview(boolean value) {
        _overview = value;
    }
    public boolean getPackage() {
        return _package;
    }
    public void setPackage(boolean value) {
        _package = value;
    }
    public String getPackagenames() {
        return _packagenames;
    }
    public void setPackagenames(String value) {
        _packagenames = value;
    }
    public boolean getProtected() {
        return _protected;
    }
    public void setProtected(boolean value) {
        _protected = value;
    }
    public boolean getPublic() {
        return _public;
    }
    public void setPublic(boolean value) {
        _public = value;
    }
    public String getSourcepath() {
        return _sourcepath;
    }
    public void setSourcepath(String value) {
        _sourcepath = value;
    }
    public boolean getUse() {
        return _use;
    }
    public void setUse(boolean value) {
        _use = value;
    }
    public boolean getVersion() {
        return _version;
    }
    public void setVersion(boolean value) {
        _version = value;
    }
    public JavadocElm getJavadocElm() {
        return _javadocElm;
    }
    public void setJavadocElm(JavadocElm value) {
        _javadocElm = value;
    }
    public FileSet getFileSet() {
        return _fileSet;
    }
    public void setFileSet(FileSet value) {
        _fileSet = value;
    }
}
