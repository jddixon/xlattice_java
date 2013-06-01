/* ProcessingInstruction.java */
package org.xlattice.corexml.om;

/**
 * Class representing an XML processing instruction.
 * 
 * @author Jim Dixon
 */
public class ProcessingInstruction extends Node {

    /** target (language) */
    private String target;
    /** text of the instruction */
    private String text;
    
    /**
     * Create a node by specifying the target (language) and text
     * separately.
     */
    public ProcessingInstruction (String target, String text) {
        super();
        this.target = target;
        this.text   = text;
    }

    /** 
     * Create a node from an initialization string, guessing that
     * the first space separates the target from the text.
     *
     * XXX WILL FAIL IF THERE IS NO SUCH SPACE
     *
     */
    ProcessingInstruction (String comboText) {
        super();
        int spaceAt = comboText.indexOf(" ");
        this.target = comboText.substring(0, spaceAt);
        this.text   = comboText.substring(spaceAt + 1);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** @return a reference to the target of the PI */
    public String getTarget() {
        return target;
    }

    /** @return a reference to the text of the PI */
    public String getText() {
        return text;
    }
    // NODE METHODS /////////////////////////////////////////////////
    /** @return it's true, I'm a PI */
    public boolean isProcessingInstruction() {
        return true;
    }
    /**
     * Output properly bracketed PI content with a line separator,
     * without indenting.
     */
    public String toXml () {
        StringBuffer sb = new StringBuffer("<?")
            .append(target).append(" ").append(text).append("?>\n");
        return sb.toString();
    }
}
