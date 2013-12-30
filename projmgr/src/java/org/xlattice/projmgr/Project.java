/* Project.java */
package org.xlattice.projmgr;

import java.util.ArrayList;

/**
 * Project descriptor - which should not be used unless isWellFormed()
 * returns true.
 *
 * This is loosely related to the Maven project descriptor; Maven used
 * to be used for generating XLattice project documentation.  Names
 * are generally the same.
 *
 * An instance of Project is a deserialization of the project.xml file
 * for a component in a hierarchically structured project like XLattice.
 * Therefore <code>projectName</code> is the name of the overall project, 
 * not the name of this particular component.  The component's short name
 * is instead found in <code>id</code> and the longer name for the 
 * component in <code>name</code>.
 * 
 * XXX The role of a parent Project is not clear.  It looks like the idea
 * was that projects should inherit from their parent Projects.  That is,
 * if getLogo() would return null and there is a parent, it instead returns
 * the value returned by parent.getLogo().  This is nowhere documented.
 *
 * @author Jim Dixon
 */
public class Project {
    /**
     * Follows the form "N.NN".  If fields are renamed the version
     * number must be bumped and a translator provided.
     */
    private String pomVersion = "0.1";  // the default
    /**
     * If not null, reference to parent Project.  In current
     * implementation, this field must be null in the parent.
     */
    private Project parent;
    private String  parentFile;

    /** project name, for example "xlattice" */
    private String projectName;

    /** short name for component, for example "projmgr" */
    private String  id;

    /** longer name for component, for example "ProjMgr" */
    private String  name;

    /** component version number, should follow the form N.NN or N.NN(a|b)N */
    private String  version;

    /** Typically ${parent}/lib */
    private String  libDir;

    /** Java package for project, eg org.xlattice.projmgr */
    private String  packageName;

    // BUILD-RELATED ////////////////////////////////////////////////
    /** list of .jar files used by this project */
    private ArrayList dependencies;

    // other items ignored

    // WEB-SITE GENERATION //////////////////////////////////////////
    /** short description of project; one sentence or so */
    private String  shortDescription;
    /** longer capsule description, roughly a paragraph */
    private String  description;
    /** path to project logo */
    private String  logo;
    /** year the project was started */
    private int     startYear = -1;

    // OWNING ORGANIZATION ////////////////////////////////
    /** Organization name */
    private String  orgName;
    /** organization's Web page */
    private String  orgUrl;
    /** organization's logo */
    private String orgLogo;

    // WEB SITE LOCATION //////////////////////////////////
    // COMMUNICATIONS /////////////////////////////////////
    // PEOPLE /////////////////////////////////////////////
    // ignored for now

    Project() {
        dependencies = new ArrayList();
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public String getPom() {
        return pomVersion;
    }
    public Project setPom (String s) {
        pomVersion = s;
        return this;
    }

    public Project getParent() {
        return parent;
    }
    /** does not appear in XML serialization */
    public Project setParent (Project s) {
        parent = s;
        return this;
    }
    public String getParentFile() {
        return parentFile;
    }
    public void setParentFile(String s) {
        if (s == null || s.equals(""))
            throw new IllegalArgumentException(
                    "null or empty parent file name");
        parentFile = s;
    }
    public String getProjectName() {
        return projectName;
    }
    public Project setProjectName(String s) {
        projectName = s;
        return this;
    }
    public String getId() {
        return id;
    }
    public Project setId (String s) {
        if (!ProjMgr.isWellFormedId(s))
            throw new IllegalStateException("badly formed ID");
        id = s;
        return this;
    }
    public String getName() {
        return name;
    }
    public Project setName(String s) {
        name = s;
        return this;
    }
    public String getVersion() {
        return version;
    }
    public Project setVersion(String s) {
        if (!ProjMgr.isWellFormedVersion(s))
            throw new IllegalStateException("badly formed version number");
        version = s;
        return this;
    }
    public String getLibDir() {
        return libDir;
    }
    public Project setLibDir(String libdir) {
        libDir = libdir;
        return this;
    }
    public String getPackageName () {
        return packageName;
    }
    public Project setPackageName(String s) {
        packageName = s;
        return this;
    }
    // BUILD-RELATED ////////////////////////////////////////////////
    public Project addDependency(Dependency d) {
        dependencies.add(d);
        return this;
    }
    public Dependency getDependency(int n) {
        return (Dependency) dependencies.get(n);
    }
    private static Dependency[] dummy = new Dependency[]{};
    public Dependency [] getDependencies() {
        return (Dependency[]) dependencies.toArray(dummy);
    }
    // WEB SITE GENERATION //////////////////////////////////////////

    // PROJECT DESCRIPTION ////////////////////////////////
    public String getDescription() {
        return description;
    }
    public Project setDescription(String s) {
        description = s;
        return this;
    }
    public String getShortDescription() {
        return shortDescription;
    }
    public Project setShortDescription(String s) {
        shortDescription = s;
        return this;
    }
    public String getLogo() {
        return logo;
    }
    public Project setLogo(String s) {
        logo = s;
        return this;
    }
    public int getStartYear() {
        return startYear;
    }
    public Project setStartYear(int year) {
        if ( year < 1990 || year > 2100)
            throw new IllegalArgumentException("impossible start year");
        startYear = year;
        return this;
    }

    // OWNING ORGANIZATION ////////////////////////////////
    public String getOrgName () {
        return orgName;
    }
    public Project setOrgName (String oname) {
        if (oname == null)
            throw new NullPointerException("null org name");
        // empty is OK
        orgName = oname;
        return this;
    }
    public String getOrgUrl () {
        return orgUrl;
    }
    public Project setOrgUrl (String oUrl) {
        if (oUrl == null)
            throw new NullPointerException("null org name");
        // empty is OK
        orgUrl = oUrl;
        return this;
    }
    public String getOrgLogo () {
        return orgLogo;
    }
    public Project setOrgLogo (String oLogo) {
        if (oLogo == null)
            throw new NullPointerException("null org name");
        // empty is OK
        orgLogo = oLogo;
        return this;
    }
    /////////////////////////////////////////////////////////////////
    /**
     * Minimal checks on correctness.  The presumption is that if
     * the values have been set at all they have been set correctly.
     *
     * XXX NEEDS TO BE RE-EXAMINED XXX
     *
     * @return whether the object appears to be well-formed
     */
    public boolean isWellFormed() {
        return (id      != null)     && (!id.equals(""))
           &&  (name    != null)     && (!name.equals(""))
           &&  (version != null)     && (!version.equals(""))
           &&  (packageName != null) && (!packageName.equals(""))
           &&  (orgName != null)     && (!orgName.equals(""))
           &&  (startYear > 0);
    }
    // NODE /////////////////////////////////////////////////////////
    public String toXml() {
        StringBuffer sb = new StringBuffer().append("<project>\n");
        if (parent != null)
            sb.append("<extends>").append(parentFile).append("</extends>");

        sb.append("<id>").append(id).append("</id>\n")
          .append("<name>").append(name).append("</name>\n")
          .append("<version>").append(version).append("</version>\n")
          .append("<libDir>").append(libDir).append("</libDir>\n")
          .append("<package>").append(packageName).append("</package>\n")
          // BUILD-RELATED ////////////////////////////////
          .append("<dependencies>\n");
        for (int i = 0; i < dependencies.size(); i++)
            sb.append(((Dependency)dependencies.get(i)).toXml());
        sb.append("</dependencies>\n")
          // PROJECT DESCRIPTION
          .append("<description\n").append(description)
                .append("</description>\n")
          .append("<shortDescription\n").append(shortDescription)
                .append("</shortDescription>\n")
          .append("<logo>").append(logo).append("</logo>\n")
          .append("<inceptionYear>").append(startYear)
                .append("</inceptionYear>\n")
          // OWNING ORGANIZATION
          .append("<organization>\n")
          .append("  <name>").append(orgName).append("</name>\n")
          .append("  <url>") .append(orgUrl) .append("</url>\n")
          .append("  <logo>").append(orgLogo).append("</logo>\n")
          .append("</organization>\n")
          ;
        sb.append("</project>\n");
        return sb.toString();
    }
}
