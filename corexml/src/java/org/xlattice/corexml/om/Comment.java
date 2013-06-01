/* Comment.java */
package org.xlattice.corexml.om;

/**
 * A comment node, containing text which will not be rendered and may
 * be discarded.  Comments are contained within XML comment delimiters
 * <pre>
 *  &lt;!-- like this --&gt;
 * </pre>
 *
 * @author Jim Dixon
 */
public class Comment extends Node {

    /** the text of the comment */
    private String text;
    
    /**
     * Create a comment.  XXX Trim the text?
     */
    public Comment (String text) {
        super();
        this.text = text.trim();
    }

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the text contained within the comment delimiters */
    public String getText() {
        return text;
    }

    // NODE METHODS /////////////////////////////////////////////////
    /** @return true, this node is a comment */
    public boolean isComment () {
        return true;
    }
    /** @return the comment enclosed within XML comment delimiters */
    public String toXml () {
        StringBuffer sb = new StringBuffer("<!-- ")
            .append(text).append(" -->\n");
        return sb.toString();
    }
}
