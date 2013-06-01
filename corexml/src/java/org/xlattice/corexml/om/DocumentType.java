/* DocumentType.java */
package org.xlattice.corexml.om;

/**
 * XXX Not implemented at this time.
 *
 * @author Jim Dixon
 */
public class DocumentType extends Node {

    private String name;        // XXX
    private String value;       // XXX
  
    /** XXX MAKES NO SENSE AT ALL */
    DocumentType (String name, String value) {
        super();
        this.value = value;
    }

    // PROPERTIES ///////////////////////////////////////////////////
    public String getValue() {
        return value;
    }

    // NODE METHODS /////////////////////////////////////////////////
    public boolean isDocType() {
        return true;
    }
    /** XXX MAKES NO SENSE AT ALL */
    public String toXml () {
        StringBuffer sb = new StringBuffer(" ")
            .append(name).append("=\"").append(value).append("\"");
        return sb.toString();
    }
}
