/* EmptyElBinding.java */
package org.xlattice.corexml.bind;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.Node;

/**
 * Bind a subelement's presence in XML to the value of the named field in
 * the associated class.  That is, if the subelement is present in the
 * XML, then set the value of the appropriate boolean field in the 
 * associated object to <code>true.</code>
 * 
 * This allows XML writers to use empty elements like
 * <pre>
 *   &lt;married/&gt;
 * </pre>
 * to set boolean flags in associated Java objects.
 *
 * When generating XML from an object, if the bound field in the
 * object is false, the subelement will not be generated.
 *
 * If the <code>optional()</code> or <code>repeats()</code> modifiers
 * are present on this Binding in the Mapping definition, they 
 * will be ignored.  By definition, this binding is optional and may
 * not be repeated.
 *
 * @author Jim Dixon
 */
public class EmptyElBinding extends Binding {

    /** 
     * Create the binding.  If the named empty element is present, then
     * the setter will be called to set the boolean field in the 
     * associated class to true.
     *
     * This Join is always optional.
     * 
     * @param name      the tag in the optional empty element
     * @param fieldName the field which will be set to true
     */
    public EmptyElBinding (String name, String fieldName) {
        super (name, fieldName);
    }
    public EmptyElBinding (String name) {
        this (name, NameGenerator.dehyphenate(name));
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Called when the definition of a Mapping is complete.  Assigns
     * appropriate values to minOccur and maxOccur, and assigns the
     * parent class.
     *
     * @param clazz class that parent objects are drawn from
     */
    protected void join (Class clazz)       throws CoreXmlException {
        _setMinOccur(0);        // force a sensible
        _setMaxOccur(1);        //   pair of values ;-)
        super.join(clazz);
    }
    /** @return type for this Join */
    protected int joinType() {
        return EMPTYEL;
    }
    /**
     * On conversion from XML to Java objects, sets the field in 
     * the bound object to true if a subelement of this name is 
     * present.  
     *
     * @param node should be the element whose presence is being flagged
     * @param o    object whose boolean field will be set to true
     */
    protected void apply (Node node, Object o) throws CoreXmlException {
        Element elm = checkElmJoin(node, o);
        setField(o, "true");
    }

    /**
     * If the boolean field in the associated object is true, append
     * an empty element to the parent.  
     *
     * @param parent Element the empty subelement gets added to
     * @param o      the object whose boolean value is checked
     */
    protected void generate (Node parent, Object o) 
                                            throws CoreXmlException {
        Element elm = checkElmJoin(parent, o);
        if ( getField(o) == "true" ) 
            elm.addChild(new Element(name));
    }
}
