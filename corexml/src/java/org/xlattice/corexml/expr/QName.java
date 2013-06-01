/* QName.java */
package org.xlattice.corexml.expr;

/**
 * Token representing an XML name which may be qualified.  If the name
 * is qualified, it is written as two NCNames separated by a colon
 * and stored as a pair of Strings, prefix and name.  If the prefix
 * is null or the empty String (""), it is stored as a null prefix
 * and the name and written as a single unqualified NCName.
 * 
 * @author Jim Dixon
 */
public class QName implements Token {

    private String prefix;      // null by default, of course
    private String localPart;

    /**
     * Create a token for a prefixless name.
     * 
     * @throws NullPointerException if name is null
     */
    public QName (String name) {
        this (null, name);
    }

    /**
     * Create a token, specifying both parts of the name.
     * 
     * @param prefix an NCName or may be null
     * @param name   local part of the name, an NCName
     * @throws NullPointerException if name is null
     */
    public QName (String prefix, String name) {
        if (prefix == "")
            this.prefix = null;
        else
            this.prefix = prefix;
        if (name == null) 
            throw new NullPointerException("null name");
        localPart = name;
    }

    // PROPERTIES ///////////////////////////////////////////////////
    public String getPrefix() {
        return prefix;
    }
    public String getLocalPart() {
        return localPart;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    public String getName () {
        StringBuffer sb = new StringBuffer();
        if (prefix != null && !prefix.equals(""))
            sb.append(prefix).append(".");
        return sb.append(localPart).toString();
        
    }
    public String toString() {
        return new StringBuffer().append("[qname:").append(getName())
                                 .append("]").toString();
    }
}
