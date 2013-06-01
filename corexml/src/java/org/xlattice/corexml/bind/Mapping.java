/* Mapping.java */
package org.xlattice.corexml.bind;

import org.xlattice.corexml.CoreXml;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.Node;
import org.xlattice.corexml.om.NodeList;
import org.xlattice.corexml.om.Text;

/**
 * Data structure describing the binding between an XML tree
 * (Document or Element) and a set of objects.  When the Mapping is
 * used to traverse XML, a set of objects will be created corresponding
 * to the XML content.  When the Mapping is used to traverse a set of
 * objects, an equivalent XML Document will be created in object model
 * (OM) form.
 * 
 * This is the top level descriptor in XLattice's data binding scheme.
 *
 * @author Jim Dixon
 */

public class Mapping {

    /** tag in root XML element */
    private final String rootTag;
    /** name of class it maps into */
    private final String className;
    /** the class the tag maps into */
    private final Class  clazz;
    
    /** the set of AttrBindings attached to this SubMapping */
    private AttrBag attrBag = new AttrBag();
    /** the set or list of tag/attribute name-to-field Joins */
    private final Ordering ordering;

    /** whether join() has been called */
    private boolean joined = false;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create a mapping between an XML fragment (Document or Element)
     * and a set of objects.  If the XML is a Document, its Element
     * node will be used in the Mapping.
     *
     * The mapping is defined in terms of a set or list of Joins.  Each 
     * Join relates an XML element or attribute value to a field in an
     * instance of a Java class, or specifies the class of the object
     * to be created if a tag is present in the XML.  
     *
     * The list or set of Joins is an Ordering.  If the Joins must 
     * appear in fixed order, the Ordering is a Seq (for Sequence).
     * If no order is specified, it is a Bag.  XXX BAGS ARE NOT 
     * XXX SUPPORTED IN THIS IMPLEMENTATION.
     *
     * The Ordering may either be used to generate sets of Java objects
     * (using the <code>apply</code> method) or to generate XML from
     * such sets of Java objects (using the <code>generate</code> 
     * method.
     * 
     * XXX Java throws InstantiationExceptions if the class named
     * XXX is an internal class.  The only fix found so far is to
     * XXX compile classes separately.  In other words, no dollar
     * XXX sign ($) may appear in the class name String.
     *
     * @param tag       the tag on the Element
     * @param name      the fully qualified name of the Class
     * @param isOrdered whether the XML must follow the same order
     *                    as the Mapping (defaults to true)
     */
    public Mapping (String tag, String name, boolean isOrdered)
                                            throws CoreXmlException {
        rootTag   = tag;
        className = name;
        try {
            clazz     = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new CoreXmlException ("can't find class " + className);
        }
        if (isOrdered)
            ordering  = new Seq();
        else
            ordering = new Bag();
        ordering.setClazz(clazz);
    }
    /**
     * Constructor which defaults to a fixed (Seq) Ordering.
     */
    public Mapping (String tag, String name)
                                            throws CoreXmlException {
        this (tag, name, true);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    protected AttrBag getAttrBag() {
        return attrBag;
    }
    /**
     * An instance of this class is the top-level Java object 
     * involved in the Mapping.
     * 
     * @return the top-level Class
     */
    protected Class getClazz() {
        return clazz;
    }
    /**
     * The name of the Mapping's top-level class.
     * 
     * @return the name of the Class involved in the Mapping
     */
    protected String getClassName() {
        return className;
    }
    /**
     * Get the Ordering, either a Bag (unordered) or a Seq, with
     * fixed order.  By default the Ordering is a Seq.
     *
     * XXX Only Seqs are supported in the current implementation.
     *
     * @return the Ordering for the Mapping
     */
    protected Ordering getOrdering() {
        return ordering;
    }
    /**
     * The name of the topmost Element involved in the Mapping.
     * This is the tag on the Element node of the Document.
     *
     * @return the tag on the root Element.
     */
    protected String getName() {
        return rootTag;
    }

    // OTHER METHODS ////////////////////////////////////////////////
    
    /**
     * Add a Join to the Mapping.  Mappings are defined by first
     * declaring the Mapping using one of the constructors above
     * and then adding Joins using this method.  If the Ordering
     * on the Join is a Seq (the default), then Joins are expected
     * to occur in the XML in exactly the order in which they are 
     * added.
     */
    public Mapping add (Join join)          throws CoreXmlException {
        if (join.joinType() == Join.ATTRBINDING)
            attrBag.add(join);
        else
            ordering.add(join);
        return this;
    }
    /**
     * Called after declaring all bindings.  This sets up the 
     * joins between descendents, enabling the use of the Mapping 
     * either in generating objects from XML or XML from objects.
     */
    public void join ()                     throws CoreXmlException {
        if (joined)
            throw new CoreXmlException("join() has already been called");
        joined = true;
        attrBag.join(clazz);
        ordering.join(clazz);
    }
    /**
     * @return the Join type index reserved for a Mapping
     */
    protected int joinType () {
        return Join.MAPPING;
    }
    /**
     * Apply the mapping to a Document, creating one or more Objects
     * and assigning values to fields in the Object(s), recursing 
     * through the Document to generate an object tree.
     *
     * @param doc the Document being processed
     * @return    a reference to the topmost object created
     */
    public Object apply (Document doc) throws CoreXmlException {
        if (doc == null)
            throw new NullPointerException(
                "cannot apply map to null document");
        Element root = doc.getElementNode();
        if (root == null) 
            throw new IllegalArgumentException(
                    "Document has no Element node");
        Object    o = null;
        Throwable e = null;
        try {
            o = clazz.newInstance();
        } catch (IllegalAccessException iae) {
            e = iae;
        } catch (InstantiationException ie) {
            e = ie;
        } 
        if (e != null) 
            throw new CoreXmlException ("error creating object: "
                    + e);
        attrBag.apply(root, o);
        ordering.apply(root, o);
        return o;
    }
    /**
     * Use the Mapping to generate an XML Document from an object.
     * The object is an instance of the Java class involved in the
     * Mapping, and will generally be the topmost object in a tree
     * of objects.
     *
     * @param o an instance of the Class involved in the Mapping
     * @return  an XML document in CoreXML object model form
     */
    public Document generate (Object o)     throws CoreXmlException {
        if (o == null)
            throw new NullPointerException (
                    "cannot generate from null object");
        if (o.getClass() != clazz) 
            throw new IllegalArgumentException(
                    "object is not an instance of " + className);
        Document doc   = new Document();
        Element elm = new Element (rootTag);
        doc.getNodeList().append(elm);
        doc.setElementNode(elm);        // XXX SHOULD NOT BE NECESSARY
        attrBag.generate (elm, o);
        ordering.generate (elm, o);
        return doc;
    }
}
