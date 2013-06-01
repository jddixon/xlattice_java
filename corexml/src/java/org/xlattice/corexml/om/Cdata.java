/* Cdata.java */
package org.xlattice.corexml.om;

/**
 * An XML text node containing text which must not be subjected to
 * further interpretation by the XML processor.
 * 
 * @author Jim Dixon
 */
public class Cdata extends Text {

    public Cdata (String cdata) {
        super(cdata);
    }

    // PROPERTIES ///////////////////////////////////////////////////


    // NODE METHODS /////////////////////////////////////////////////
    /** 
     * This node is CDATA.
     */
    public boolean isCdata() {
        return true;
    }
    /** @return the text in a CDATA wrapper */ 
    public String toXml () {
        StringBuffer sb = new StringBuffer("<![CDATA[")
            .append(text).append("]]>\n");
        return sb.toString();
    }
}
