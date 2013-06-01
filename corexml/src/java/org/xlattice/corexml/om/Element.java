/* Element.java */
package org.xlattice.corexml.om;

import java.util.Iterator;

/**
 * An XML element node.  This implementation specifies the element 
 * in terms of its prefix and name.  
 * <p/>
 * XXX Need to consider whether to store the Namespace (uri) rather
 * than the prefix; could resolve prefix in the constructor.  This
 * decision will have consequences.
 * <p/>
 * In this implementation an element always has its own attribute
 * list.
 * 
 * @author Jim Dixon
 */
public class Element extends Holder {

    private String prefix;
    private String name;
    private AttrList alist;
  
    /**
     * Create an XML element, given its prefix and name.  Both 
     * prefix and name should conformant to the XML specifications
     * and must not contain colons (that is, they must be NCNames).
     *
     * @param prefix NCName or null
     * @param name   NCName, must not be null
     */
    public Element (String prefix, String name) {
        super();
        this.prefix = prefix;
        this.name   = name;
        alist = new AttrList();
        alist.setHolder(this);
    }
    /**
     * Create an XML element, defaulting the prefix to null.
     */
    public Element (String name) {
        this(null, name);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the prefix, an NCName or null */
    public String getPrefix() {
        return prefix;
    }
    /** @return the element name, an NCName, which may not be null */
    public String getName() {
        return name;
    }
    /**
     * @return the attribute list - may be empty, may not be null
     */
    public AttrList getAttrList() {
        return alist;
    }
    // ATTRIBUTES ///////////////////////////////////////////////////
    /** 
     * Add an attribute to this element.
     * @param prefix to attribute name, may be null
     * @param name   the attribute name itself
     * @param value  the String value the attribute is set to
     * @return       a reference to this Element, to allow chaining
     */
    public Element addAttr (String prefix, String name, String value) {
        Attr attr = new Attr (prefix, name, value);
        alist.add(attr);
        return this;
    }
    /**
     * Add an element, defaulting its prefix to null.
     */
    public Element addAttr (String name, String value) {
        return addAttr (null, name, value);
    }
    /**
     * @param  n index of the parameter to be returned
     * @return the Nth attribute
     */
    public Attr getAttr (int n) {
        return alist.get(n);
    }
    // VISITOR-RELATED///////////////////////////////////////////////
    public void walkAttrs (Visitor v) {
        alist.walkAll(v);
    }
    // NODE METHODS /////////////////////////////////////////////////
    public boolean isElement   () {   
        return true; 
    }
    /**
     * Preliminary version, for debugging.
     * 
     * @return the element in string form, without its attributes
     */

    public String toString() {
        return new StringBuffer().append("[Element: tag:").append(name)
                                 .append(" ...]").toString();
    }
    /**
     * @return the element and its attributes in XML form, unindented
     */
    public String toXml() {
        StringBuffer sb = new StringBuffer("<");
        // conditionally output prefix
        sb.append(name);
        // conditionally output attributes
        int attrCount = alist.size(); 
        for (int i = 0; i < attrCount; i++)
            sb.append(alist.get(i).toXml() );
        // conditionally output ns2pf
        if (nsUris.size() > 0) {
            for (int i = 0; i < nsUris.size(); i++) {
                String ns = (String) nsUris.get(i);
                String p  = (String) ns2pf.get(ns);
                sb.append(" ");
                if (p == null) 
                    sb.append("xmlns=\"");
                else 
                    sb.append("xmlns:").append(p).append("=\"");
                sb.append(ns).append("\"");
            }
        }
        if (nodes.size() > 0) {
            // line separator 
            sb.append(">\n");
            // conditionally output body
            for (int i = 0; i < nodes.size(); i++) {
                sb.append(((Node)nodes.get(i)).toXml());
            }
            sb.append("</");
            // prefix
            sb.append(name).append(">\n");
        } else {
            // empty element
            sb.append("/>\n");
        }
        return sb.toString();
    }

}
