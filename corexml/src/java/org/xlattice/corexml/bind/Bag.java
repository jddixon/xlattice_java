/* Bag.java */
package org.xlattice.corexml.bind;

import java.util.HashMap;
import java.util.Iterator;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Element;

/**
 * Maintains an unordered set of Joins (bindings, submappings, etc).
 * This is one of two types of Orderings on a Mapping, SubMapping,
 * or Collector.  An Ordering consists of a set or sequence of Joins.
 * Each Join binds between an XML value (attribute value or
 * text of an element) and a field in an object.  A Bag is a set of 
 * such Joins.
 *
 * In the current implementation, Bags may be qualified by isOptional(),
 * repeats(), setMinOccur(n), and setMaxOccur(n) but the Joins below
 * the Bag may not be.  More correctly: any such qualifications will 
 * be ignored.  
 *
 * In this implementation, the following may appear in a Bag:
 * <ul>
 *   <li>SubMappings</li>
 *   <li>Collectors</li>
 *   <li>SubElBindings</li>
 *   <li>Interfaces</li>
 * </ul>
 * 
 * Bags function like Interfaces.  When a Bag encounters a tag in the
 * XML, it looks this up in a name-to-Join mapping and does the
 * apply() for that Join.  When serializing an object tree to XML, it
 * does the opposite, looking up the object name in an object-to-name
 * map and then invoking generate() for the Join.
 *
 * @author Jim Dixon
 */
public class Bag extends Ordering {
    
    private boolean hasTextBinding = false;
    /** the set of Joins, keyed by name */
    private final HashMap joins;

    /**
     * Create the Bag and with it an empty set of Joins.
     */
    public Bag () {
        joins = new HashMap();
    }
    public Join get (String name) throws CoreXmlException {
        if (name == null) 
            throw new NullPointerException("null name");
        // but name may be ""
        Object j = joins.get(name);
        if (j == null)
            throw new CoreXmlException ("no such join");
        return (Join) j;
    }
    // INTERFACE ORDERING ///////////////////////////////////////////
    /**
     * Add a Join to the set. TextBindings are nameless and so there 
     * may be at most one TextBinding in a Bag.
     *
     * @param j  Join being added to the collection
     */
    protected void add (Join j)                throws CoreXmlException {
        String name = j.getName();
        if (j instanceof TextBinding) {
            if (hasTextBinding) 
                throw new CoreXmlException(
                        "trying to add second TextBinding to Bag");
            else
                hasTextBinding = true;
        } else {
            if (joins.containsKey(name))
                throw new CoreXmlException(
                        "trying to add second Join with name " 
                        + name + " to Bag");
        }
        joins.put(name, j);
    }
    /**
     * Set the bound class for the Ordering and all descendents.
     */
    protected void join (Class clazz)          throws CoreXmlException {
        setClazz (clazz);
        Iterator it = joins.keySet().iterator();
        while (it.hasNext()) {
            Join j = (Join) (joins.get(it.next()));
            j.join(clazz);
        }
    }
    
    /** @return the number of Joins in the Bag */
    protected int size() {
        return joins.size();
    }
    /** 
     * Use the ordering to generate a set of values from the
     * children of an Element, recursing if possible.  The
     * values obtained will be used to set fields in the object, 
     * and/or instantiate child objects.
     *
     * @param elm Element whose children values come from
     * @param o   object in which values are assigned
     */
    protected void apply (Element elm, Object o)   
                                            throws CoreXmlException {
        throw new CoreXmlException ("not yet supported");
    }
    /** 
     * Use the ordering to create XML nodes.  The ordering is used
     * to draw values from the object and, if they are present, from
     * children of the object.  These values are used to set attributes
     * in the Element, subelement values, and so on recursively.
     *
     * @param elm Element to which values are assigned
     * @param o   object from which values are drawn
     */
    protected void generate (Element elm, Object o)
                                            throws CoreXmlException {
        throw new CoreXmlException ("not yet supported");
    }
}
    
