/* Attr.java */
package org.xlattice.corexml.om;

/**
 * XML attribute, either a triplet (prefix, name, value) or a pair
 * (name, value).  The prefixed form is a 'colonized' string like 
 * "prefix:name", where <i>colonized</i> means 'containing a colon'.
 * As required by the XML standard, neither <code>prefix</code> nor 
 * <code>name</code> may contain a colon.  In the actual XML, the
 * value will be doubly (") or singly (') quoted.
 * 
 * @author Jim Dixon
 */
public class Attr extends Node {

    private String prefix;
    private String name;
    private String value;
   
    /**
     * Create an attribute.
     * 
     * @param prefix NCNAME (non-colonized name) identifying the namespace
     * @param name   attribute name, another NCNAME
     * @param value  the attribute's value
     */
    public Attr (String prefix, String name, String value) {
        super();
        this.prefix = prefix;
        this.name   = name;
        this.value  = value;
    }

    /**
     * Default constructor with null prefix.
     */
    public Attr(String name, String value) {
        this(null, name, value);
    }

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the prefix part of the name; may be null */
    public String getPrefix() {
        return prefix;
    }
    /** @return the unqualified name of the attribute */
    public String getName() {
        return name;
    }
    /** @return the value assigned to the attribute */
    public String getValue() {
        return value;
    }

    // VISITOR-RELATED///////////////////////////////////////////////
    /**
     * Method used by classes walking the XML document.
     *
     * @param v the Visitor walking the document
     */
    public void walkAll (Visitor v) {
        v.onEntry(this);
        v.onExit(this);
    } 
    // NODE METHODS /////////////////////////////////////////////////
    /** @return true; this node is an Attr */
    public boolean isAttr() {
        return true;
    }
    /**
     * Convert the Node to XML form.  If the prefix is null, it is
     * omitted.
     *
     * @return the attribute in XML form 
     */
    public String toXml () {
        StringBuffer sb = new StringBuffer(" ");
        if (prefix != null)
            sb.append(prefix).append(":");
        sb.append(name).append("=\"").append(value).append("\"");
        return sb.toString();
    }
}
