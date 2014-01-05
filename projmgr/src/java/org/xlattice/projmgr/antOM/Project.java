/* Project.java */
package org.xlattice.projmgr.antOM;

/**
 * Project.
 *
 * This is copyrighted material made available under the terms of
 * the XLattice License, which is included in this distribution as
 * LICENSE.txt and is also available at
 *   http://xlattice.sourceforge.net/community/license.html
 *
 * @author Jim Dixon
 */
public class Project {

    // STATIC VARIABLES /////////////////////////////////////////////
    private String     _basedir;
    private String     _defaultTarget;
    private String     _name;
    private ProjectElm _projectElm;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public Project () {
    }

    // ACCESS METHODS ///////////////////////////////////////////////
    public String getBasedir() {
        return _basedir;
    }
    public void setBasedir(String value) {
        _basedir = value;
    }
    public String getDefaultTarget() {
        return _defaultTarget;
    }
    public void setDefaultTarget(String value) {
        _defaultTarget = value;
    }
    public String getName() {
        return _name;
    }
    public void setName(String value) {
        _name = value;
    }
    public ProjectElm getProjectElm() {
        return _projectElm;
    }
    public void setProjectElm(ProjectElm value) {
        _projectElm = value;
    }
}
