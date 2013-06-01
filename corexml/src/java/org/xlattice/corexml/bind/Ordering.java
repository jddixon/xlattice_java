/* Ordering.java */
package org.xlattice.corexml.bind;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Element;

/**
 * The set or sequence of Joins which bind XML element or 
 * attribute values with fields in an object which is an instance
 * of a bound class.  The Ordering may be sequential, in which 
 * case joined elements or attributes are matched in turn, or a
 * set, in which case attributes or elements may appear in any 
 * order in the XML.
 * 
 * Joins added to the Ordering may be either Bindings or SubMappings.
 * Bindings either associate an attribute with a field in an object
 * or associate the presence or value of a subelement.  SubMappings
 * map a child object to a parent.  A Collector is used to map any
 * number of child objects to a parent.
 * 
 * XXX Set Orderings (Bags) are not supported in the current 
 * XXX implementation.
 *
 * @author Jim Dixon
 */
public abstract class  Ordering {
    /** the Java Class associated with the Ordering */
    private Class clazz;

    /** No-arg constructor. */
    protected Ordering () { }
    
    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the Java Class associated with the Ordering */
    protected Class getClazz() {
        return clazz;
    }
    /**
     * Set the Java class associated with the Ordering.  Must be
     * called by subclasses.  XXX Must NOT be called by other
     * classes!
     */
    protected void setClazz (Class clazz) {
        this.clazz = clazz;
    }
    // ABSTRACT METHODS /////////////////////////////////////////////
    /** 
     * Add a Join to the Ordering.
     * @param j Join to be added
     */
    protected abstract void add (Join j)      throws CoreXmlException;
   
    /**
     * Set the joined class for all descendents.  Subclasses must
     * call Ordering.setClazz() and iterate over all descendents, 
     * calling their join(clazz) methods.
     * 
     * @param clazz Class that descendents are to be joined to.
     */
    protected abstract void join (Class clazz) throws CoreXmlException;
    
    /** @return a count of Joins in the Ordering */
    protected abstract int size();

    /** 
     * Apply the ordering to an XML element, using values of XML 
     * attributes and other Nodes to set fields in the object, 
     * create child objects, and so forth, recursively.
     *
     * @param elm Element from which values will be drawn
     * @param o   object to which values will be assigned
     */
    protected abstract void apply (Element elm, Object o) 
                                            throws CoreXmlException;
   
    /**
     * Use the Ordering to generate XML from a subtree of Java 
     * objects.  The Joins in the Ordering are applied in turn,
     * but in no predictable order.
     *
     * @param elm XML Element to which child Nodes will be attached
     * @param o   Java object from which values will be drawn
     */
    protected abstract void generate (Element elm, Object o)
                                            throws CoreXmlException;
}
