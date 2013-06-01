/* SubElBinding.java */
package org.xlattice.corexml.bind;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.Node;
import org.xlattice.corexml.om.NodeList;
import org.xlattice.corexml.om.Text;

/**
 * Bind a subelement value in XML to the value of the named field in
 * the associated class.  As the term is used here, 'subelement value'
 * means text enclosed by the opening and closing parts of the element,
 * such as 
 * <pre>
 *   &lt;height&gt; 182 cm &lt;/height&gt;
 * </pre>
 * <p>
 *
 * @author Jim Dixon
 */
public class SubElBinding extends Binding {

    /**
     * Create the binding between an XML element and a field in 
     * a Java object.
     * @param name      the tag on the element
     * @param fieldName the name of the field in the object
     */
    public SubElBinding (String name, String fieldName) {
        super (name, fieldName);
    }
    /**
     * By default, tag and field name are the same.
     */
    public SubElBinding (String name) {
        this (name, NameGenerator.dehyphenate(name));
    }
    // JOIN INTERFACE ///////////////////////////////////////////////
    /** @return the Join type index for a SubElBinding */
    protected int joinType() {
        return SUBEL;
    }
    /**
     * Bind any text found in the Element to field in the object.
     * If child nodes of any type other than Text are found, it is
     * an error.  
     *
     * XXX Whitespace needs to be trimmed, but with some care.
     */
    protected void apply (Node node, Object o) throws CoreXmlException {
        Element elm = checkElmJoin(node, o);
        NodeList nodes = elm.getNodeList();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nodes.size(); i++) {
            Node subNode = nodes.get(i);
            if (! (subNode instanceof Text) )
                throw new IllegalStateException (
                        "SubEl binding but child node not Text: " 
                        + subNode);
            sb.append( ((Text)subNode).getText() );
        }
        setField(o, sb.toString().trim());
    }
    /**
     * Add a subelement to the parent element.  The text value of 
     * the subelement is determined by the value of the bound field
     * in the associated Java class instance.
     *
     * @param parent Element the subelement gets added to
     * @param o      the object whose value is checked
     */
    protected void generate (Node parent, Object o) 
                                            throws CoreXmlException {
        checkElmJoin(parent, o)
            .addChild(new Element(name)
                    .addChild(new Text(getField(o))));
    }
}
