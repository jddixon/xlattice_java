/* TextBinding.java */
package org.xlattice.corexml.bind;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.Node;
import org.xlattice.corexml.om.Text;

/**
 * Bind a text value in an XML element to the value of the named 
 * field in the associated class.  The field being bound to must
 * be of type String.
 *
 * If the <code>repeats()</code> modifier is present on this Binding 
 * in the Mapping definition, it will be ignored.  The <code>maxOccur</code>
 * parameter will always be 1.
 *
 * @author Jim Dixon
 */
public class TextBinding extends Binding {

    /** 
     * Create the binding.
     * @param fieldName name of the field in the associated class
     */
    public TextBinding (String fieldName) {
        super ("", fieldName);
    }

    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Called by recursion from the <code>Mapping.join()</code> call
     * after completing definition of the Mapping.
     * @param clazz parent in a field of which the text appears
     */
    protected void join (Class clazz)          throws CoreXmlException {
        maxOccur = 1;   // just ignore any change
        super.join(clazz);
    }
    /** @return the Join type index for a TextBinding */
    protected int joinType() {
        return TEXT;
    }
    /**
     * Use the text from an XML Text node to set a field in the 
     * bound object, the field being of type String.
     *
     * @param node the Text node
     * @param o    the object whose field is to be set
     */
    protected void apply (Node node, Object o) throws CoreXmlException {
        checkJoinObject(o);
        if (! (node instanceof Text) ) 
            throw new IllegalArgumentException (
                    "binding requires Text node but found: " + node);
        setField (o, ((Text)node).getText());
    }
    /**
     *
     * @param parent the Element whose NodeList the Text node is added to
     * @param o      the object whose value is checked
     */
    protected void generate (Node parent, Object o) 
                                            throws CoreXmlException {
        checkElmJoin(parent, o)
                .addChild(new Text(getField(o)));
    }
}
