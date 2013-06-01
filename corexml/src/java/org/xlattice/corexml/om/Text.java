/* Text.java */
package org.xlattice.corexml.om;

/**
 * A Node containing text in String format.
 *
 * @author Jim Dixon
 */
public class Text extends Node {

    protected String text;      // CDATA subclass has access
   
    /**
     * Create the node, initializing its value.
     *
     * XXX the text should be XML-escaped.
     *
     * @param text String value
     */
    public Text (String text) {
        super();
        this.text = text;
    }

    // PROPERTIES ///////////////////////////////////////////////////
    public String getText() {
        return text;
    }
    /** UNTESTED */
    public void setText(String s) {
        // XXX SHOULD ESCAPE XML 
        text = s;
    }
    // NODE METHODS /////////////////////////////////////////////////
    /** CDATASection overrides */
    public boolean isCdata () {
        return false;       // default
    }
    public boolean isText() {
        return true;
    }
    /** 
     * XXX Possibly preliminary version.
     */
    public String toString() {
        return new StringBuffer().append("[Text:'").append(text)
                                 .append("']").toString();
    }
        
    public String toXml () {
        return text;
    }
}
