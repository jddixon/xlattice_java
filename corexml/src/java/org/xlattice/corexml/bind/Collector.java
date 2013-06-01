/* Collector.java */
package org.xlattice.corexml.bind;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.Node;
import org.xlattice.corexml.om.NodeList;
import org.xlattice.corexml.om.Text;

/**
 * Bind a set or list of Joins under a single tag without 
 * creating a Java object.  On input, the Collector's XML tag
 * is consumed and values are assigned to fields in the parent
 * class.  On output, the items in the Collector are wrapped in
 * the tag but values continue to be drawn from the same object.
 * 
 * This corresponds to common cases like 
 * <pre>
 * &lt;items&gt;
 *   &lt;item&gt; ... &lt;/item&gt;
 *   &lt;item&gt; ... &lt;/item&gt;
 *   ...
 * &lt;/items&gt;
 * </pre>
 *
 * The Collector does not have its own associated class.  When
 * converting from XML to object form, objects created by the 
 * list or set of Joins below the Collector are instead added 
 * to the Collector's parent's class, the class the Collector 
 * is joined to.  On conversion from Java objects to XML, the
 * presence of the Collector causes child elements to be wrapped
 * in the Collector's tag.
 *
 * @author Jim Dixon
 */
public class Collector extends Join {

    private Ordering ordering;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create the Collector, specifying the Element tag and 
     * whether the ordering under the collector is sequential.
     * 
     * @param tag        Name of the top level Element
     */
    public Collector (String tag, boolean isOrdered) {
        super (tag);
        if (isOrdered)
            ordering = new Seq();
        else
            throw new IllegalStateException (
                    "Bag Ordering is not yet supported");
    }
    /** 
     * Default constructor with Seq ordering. 
     */
    public Collector (String tag) {
        this(tag, true);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** mark the Collector as optional: it need not appear in the XML */
    public Collector optional () {
        _setMinOccur(0);
        return this;
    }
    // GETTER, SETTER, AND SIZER //////////////////////////
    /**
     * Assign a name to the getter method, overriding the default.
     *
     * @param s the name being assigned
     * @return  a reference to this object, to ease chaining
     */
    public Collector setGetter(String s) {
        _setGetter(s);
        return this;
    }
    /**
     * Assign a name to the setter method, overriding the default.
     *
     * @param s the name being assigned
     * @return  a reference to this object, to ease chaining
     */
    public Collector setSetter(String s) {
        _setSetter(s);
        return this;
    }
    /**
     * Assign a name to the sizer method, overriding the default.
     * The sizer method is used to determine the number of 
     * instances where maxOccur is greater than one.  
     *
     * @param s the name being assigned
     * @return  a reference to this, to ease chaining
     */
    public Collector setSizer(String s) {
        _setSizer(s);
        return this;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Join this Connector and its immediate descendents to its parent 
     * Class.  After a Mapping has been defined, a programm calls
     * Mapping.join(), which causes this method to be called.
     *
     * XXX SETTER/GETTER/SIZER METHODS CANNOT BE USED
     * 
     * @param clazz the parent Class
     */
    protected void join (Class clazz) throws CoreXmlException {
        if (maxOccur > 1) 
            throw new CoreXmlException (
                    "maxOccur is " + maxOccur
                    + " but must be 1 for Collector");
        setClazz(clazz);
        // XXX WON'T WORK UNLESS DEFAULT NAMES ASSIGNED
        // findMethods(clazz);
        ordering.join (clazz);
    }

    /**
     * Add a Join to the Collector's ordering.
     *
     * @param j Join to be added
     */
    public Collector add(Join j)    throws CoreXmlException {
        if (joined) 
            throw new IllegalStateException("Collector has been joined");
        ordering.add(j);
        return this;                // convenience, supports chaining
    }
    // JOIN INTERFACE ///////////////////////////////////////////////
    /** @return this Join type */
    protected int joinType() {
        return COLLECTOR;
    }
    /** 
     * Use this Collector to generate an object or object tree from
     * the XML Node, which must be an Element.  Values are drawn from
     * the element's attributes, its subelements, and so on, 
     * recursively, and are assigned to fields in the object and/or used
     * to create child objects.
     *
     * @param node XML Node from which values are drawn
     * @param o    object to which values, including children, are assigned
     */
    protected void apply (Node node, Object o) throws CoreXmlException {
        Element elm = checkElmJoin(node, o);
        ordering.apply (elm, o);
    }
    
    /**
     * Generate XML from an object using this Collector's ordering.
     * The XML output is wrapped in an element with the Collector's
     * tag (Element name).
     */
    protected void generate (Node parent, Object o) throws CoreXmlException {
        Element elm = checkElmJoin(parent, o);
        Element subElm = new Element(name);
        elm.addChild (subElm);
        ordering.generate (subElm, o);
    }
}
