/* Javac.java */
package org.xlattice.projmgr.antOM;

/**
 * Javac.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Javac implements TargetElm{

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _debug;
    private String     _destdir;
    private String     _excludes;
    private String     _includes;
    private String     _optimize;
    private String     _srcdir;
    private JavacElm   _javacElm;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Javac () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getDebug() {
        return _debug;
    }
    public void setDebug(String value) {
        _debug = value;
    }
    public String getDestdir() {
        return _destdir;
    }
    public void setDestdir(String value) {
        _destdir = value;
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
    public String getOptimize() {
        return _optimize;
    }
    public void setOptimize(String value) {
        _optimize = value;
    }
    public String getSrcdir() {
        return _srcdir;
    }
    public void setSrcdir(String value) {
        _srcdir = value;
    }
    public JavacElm getJavacElm() {
        return _javacElm;
    }
    public void setJavacElm(JavacElm value) {
        _javacElm = value;
    }
}
