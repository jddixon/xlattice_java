/* AttrBinding.java */
package org.xlattice.corexml.bind;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Attr;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.Node;
import static org.xlattice.corexml.bind.NameGenerator.dehyphenate;

/**
 * Bind an attribute value in XML to the value of the named field in
 * the associated class.  Such bindings may be declared to be 
 * optional or, equivalently, the minimum occurrence (minOccur
 * parameter) may be set to zero, but no more than one instance may
 * occur.
 *
 * If a <code>repeats()</code> modifiers is present on this Binding 
 * in the Mapping definition, an exception will be thrown when the 
 * <code>join()</code> call is made.
 *
 * @author Jim Dixon
 */
public class AttrBinding extends Binding {

    private boolean isFixed;        // for fixed()
    private boolean hasDefault;     // for enum()
    private String[] values;        // for enum(); fixed() uses values[0]
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Bind the value of an attribute to a field in a class instance.
     *
     * @param attr       name of the attribute being bound
     * @param fieldName  field the attribute value is to be bound to
     */
    public AttrBinding (String attr, String fieldName) {
        super (attr, fieldName);
    }
    /**
     * By default the attribute and field names are the same.
     */
    public AttrBinding (String attr) {
        this (attr, dehyphenate(attr));
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public boolean hasDefault() {
        return hasDefault;
    }
    /**
     * Parse a list of values representing an enumeration of possible
     * attribute values.  The list is comma-separated, except that if
     * a default is being assigned, the first delimiter will be a 
     * colon (':').  The list should contain no white space.
     */
    public AttrBinding values(String valueList)     
                                            throws CoreXmlException {
        if (values != null)
            throw new CoreXmlException(
                    "attribute enumeration has already been set");
        if (valueList == null)
            throw new CoreXmlException(
                    "null enumeration list");
        String s = valueList.trim();
        if (s.equals(""))
            throw new CoreXmlException("empty enumeration list");
        String [] parts = s.split(":");
        if (parts.length > 2) {
            throw new CoreXmlException(
                    "too many colons in attribute enumberation " + s);
        } else if (parts.length == 2) {
            hasDefault = true;
            String [] otherValues = parts[1].split(",");
            values = new String[ 1 + otherValues.length ];
            values[0] = parts[0].trim();
            if (values[0].equals(""))
                throw new CoreXmlException(
                    "empty default in enumberation list '" + s + "'");
            for (int i = 0; i < otherValues.length; i++) {
                values[i + 1] = otherValues[i].trim();
                if (values[i + 1].equals(""))
                        throw new CoreXmlException(
                            "empty value in enumeration list '"
                            + s + "'");
            }
        } else {
            // no default value
            String[] rawValues = s.split(",");
            values = new String[ rawValues.length ];
            for (int i = 0; i < rawValues.length; i++) {
                values[i] = rawValues[i].trim();
                if (values[i].equals(""))
                    throw new CoreXmlException(
                        "empty value in enumeration list '" + s + "'");
            }
        }
        return this;
    }
    
    /**
     * Set the attribute to a fixed value.  On input (apply()), this means
     * that an exception will be thrown if the attribute has a value
     * other than this value.  On output, on a generate() call,  an 
     * exception will be thrown if the value in the object field is 
     * other than the fixed value.
     */
    protected AttrBinding fixed(String value)      
                                            throws CoreXmlException {
        if (value == null || value.equals(""))
            throw new CoreXmlException(
                    "cannot fix to null value");
        String s = value.trim();
        if (s == null || s.equals(""))
            throw new CoreXmlException(
                    "cannot fix to null value");
        if (isFixed)
            throw new CoreXmlException(
                    "attribute value has already been fixed");
        if (values != null)
            throw new CoreXmlException(
                    "cannot fix; enumeration has already been set");
        isFixed = true;
        values = new String[] { s };
        return this;
    } 

    // OTHER METHODS  ///////////////////////////////////////////////
    protected void join (Class clazz)       throws CoreXmlException {
        if (maxOccur > 1)
            throw new CoreXmlException(
                    "maxOccur is " + maxOccur 
                    + " but must be 1 for AttrBinding");
        super.join(clazz);
    }
    /** @return constant identifying this type of Join */
    protected int joinType() {
        return ATTRBINDING;
    }
    /**
     * Apply the binding, mapping the value of an attribute to a field
     * in an instance of a class.  XML attribute values are automatically
     * converted to and from the data type of the field.
     * 
     * @param node must be an attribute
     * @param o    object whose field will be bound to the attribute value
     */
    protected void apply (Node node, Object o) throws CoreXmlException {
        checkJoinObject(o);
        if(! (node instanceof Attr) )
            throw new IllegalArgumentException(
                    "should be binding to attribute " + name 
                    + " but actually binding to " + node);
        Attr attr = (Attr) node;
        if (! name.equals(attr.getName()) ) 
            throw new IllegalArgumentException (
                    "should be binding to attribute " + name 
                    + " but actually binding to " + attr.getName() );
        String val = attr.getValue();
        if (isFixed) {
            if (val == null || !val.equals(values[0]) )
                throw new CoreXmlException("attribute value is fixed to "
                    + values[0] + " but is actually " + val);
        } else if (values != null) {
            boolean found = false;
            for (int i = 0; i < values.length; i++)
                if (val.equals(values[i])) {
                    found = true; 
                    break;
                }
            if (!found)
                throw new CoreXmlException("attribute value " + val 
                        + " is not in enumeration");
        }
        setField (o, val);
    } 
    protected void applyDefault(Object o)   throws CoreXmlException{
        if (! hasDefault )
            throw new CoreXmlException(
                    "attribute " + name + " has no default to apply");
        setField (o, values[0]);
    }    
    /**
     * Reverse the binding, adding an attribute whose value is that 
     * of a field in an object to the named (parent) element.
     * 
     * @param parent element whose attribute/value is being set
     * @param o      object which is the source of the value
     */
    protected void generate (Node parent, Object o) 
                                            throws CoreXmlException {
        String val = getField(o);
        if (isFixed) {
            if (val == null || !val.equals(values[0]) )
                throw new CoreXmlException("attribute value is fixed to "
                    + values[0] + " but is actually " + val);
        } else if (val == null && hasDefault) {
            val = values[0];
        }
        Element elm = checkElmJoin(parent, o).addAttr(name, val);
    }
}
