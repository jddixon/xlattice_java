/* Dependency.java */
package org.xlattice.projmgr;

/** 
 * Loosely based on Maven's notion of a dependency.
 *
 * @author Jim Dixon
 */
class Dependency {
    private String groupId;             // project group, eg xlattice
    private String artifactId;          // eg projmgr
    private String version;             // eg "3.1a"
    private String type = "jar";        // or plugin or ejb or ...
    private String url;                 // used to fetch 

    Dependency() { }

    private void checkId (String name, String what) {
        if (!ProjMgr.isWellFormedId(name))
            throw new IllegalStateException("badly formed " + what 
                    + " in Dependency");
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public String getGroupId() {
        return groupId;
    }
    public Dependency setGroupId(String s) {
        checkId(s, "groupId");
        groupId = s;
        return this;
    }
    public String getArtifactId() {
        return artifactId;
    }
    public Dependency setArtifactId(String s) {
        checkId(s, "artifactId");
        artifactId = s;
        return this;
    }
    public String getVersion () {
        return version;
    }
    /**
     * External libraries need not necessarily follow XLattice 
     * version conventions, so version number "well-formedness"
     * cannot be checked.
     */
    public Dependency setVersion (String s) {
        if (s == null)
            s = "";
        version = s;
        return this;
    }
    public String getType() {
        return type;
    }
    public Dependency setType(String s) {
        checkId(s, "type");
        type = s;
        return this;
    }
    public String getUrl() {
        return url;
    }
    /**
     * XXX MUST CHECK FOR VALID URL 
     */
    public Dependency setUrl (String s) {
        url = s;
        return this;
    }
    // NODE /////////////////////////////////////////////////////////
    public String toXml() {
        if (groupId == null)
            groupId = artifactId;
        return new StringBuffer("<dependency>\n")
            .append("<groupId>").append(groupId).append("</groupId\n")
            .append("<artifactId>").append(artifactId).append("</artifactId>\n")
            .append("<version>").append(version).append("</version>\n")
            .append("<type>").append(type).append("</type>\n")
            .append("<url>").append(url).append("</url>\n")
            .append("</dependency>\n").toString();
    }
    /** 
     * Whether we can reasonably assume that the Dependency object
     * is well-formed.  The presumption is that if the value has been
     * set at all it is OK.
     */
    public boolean isWellFormed() {
        // XXX allow groupId == null but not groupID.equals("") ?
        return 
            (groupId != null)   && (!groupId.equals(""))    &&
            (artifactId != null)&& (!artifactId.equals("")) &&
            (version != null)   && (!version.equals(""))    &&
            (type != null)      && (!type.equals(""))       &&
            (url != null)       && (!url.equals(""));
    }
} 
