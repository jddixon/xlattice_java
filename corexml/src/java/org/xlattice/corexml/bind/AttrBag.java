/* AttrBag.java */
package org.xlattice.corexml.bind;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Attr;
import org.xlattice.corexml.om.AttrList;
import org.xlattice.corexml.om.Element;

/**
 * Maintains an unordered set of AttrBindings.
 *
 * @author Jim Dixon
 */
public class AttrBag extends Ordering {
    
    private boolean hasTextBinding = false;
    /** the set of AttrBindings, keyed by name */
    private final HashMap<String,AttrBinding> bindings;

    /**
     * Create the AttrBag and with it an empty set of AttrBindings.
     */
    public AttrBag () {
        bindings = new HashMap<String, AttrBinding>();
    }
    public Join get (String name) throws CoreXmlException {
        if (name == null) 
            throw new NullPointerException("null name");
        // but name may be ""
        AttrBinding j = bindings.get(name);
        if (j == null)
            throw new CoreXmlException ("no such AttrBinding");
        return j;
    }
    // INTERFACE ORDERING ///////////////////////////////////////////
    /**
     * Add an AttrBinding to the set. 
     *
     * @param j  Join being added to the collection
     */
    protected void add (Join j)                throws CoreXmlException {
        String name = j.getName();
        if( ! (j instanceof AttrBinding) )
            throw new IllegalArgumentException(
                    "Join is not an AttrBinding");
        if (bindings.containsKey(name))
            throw new CoreXmlException(
                    "trying to add second Join with name " 
                    + name + " to Bag");
        bindings.put(name, (AttrBinding)j);
    }
    /**
     * Set the bound class for the AttrBindings in AttrBag.
     */
    protected void join (Class clazz)          throws CoreXmlException {
        setClazz (clazz);
        Iterator it = bindings.keySet().iterator();
        while (it.hasNext()) {
            AttrBinding j = bindings.get(it.next());
            j.join(clazz);
        }
    }
    
    /** @return the number of AttrBindings in the Bag */
    protected int size() {
        return bindings.size();
    }
    /** 
     * Use the ordering to generate a set of values from the
     * attributes of an Element.  The values obtained will be used 
     * to set fields in the object. 
     *
     * @param elm Element whose children values come from
     * @param o   object in which values are assigned
     */
    protected void apply (Element elm, Object o)   
                                            throws CoreXmlException {
        AttrList    alist = elm.getAttrList();
        // XXX no support for prefixes

        // XXX THIS CODE OK IF XML HAS BEEN CHECKED AGAINST DTD
//      for (int j = 0; j < alist.size(); j++) {
//          Attr attr = alist.get(j);
//          String name = attr.getName();
//          join = bindings.get(name);
//          if (join == null)
//              throw new CoreXmlException(
//                      "no binding for attribute " + name);
//          join.apply(attr, o);
//      } 
    
        int bSize = bindings.size();
        Map<String,Boolean> attrFound = new HashMap<String,Boolean>(bSize);
        for (String bName: bindings.keySet())
            attrFound.put( bName, false);
        
        // PREFERRED FORM:
        // for (Attr attr: alist) {
        for (int j = 0; j < alist.size(); j++) {
            Attr attr = alist.get(j);
            String name = attr.getName();
            AttrBinding binding = bindings.get(name);
            if (binding == null)
                throw new CoreXmlException(
                        "element " + elm.getName()
                        + ": no binding for attribute " + name);
            binding.apply(attr, o);
            attrFound.put(name, true);
        }
        for (String bName: bindings.keySet()) {
            if (!attrFound.get(bName)) {
                AttrBinding binding = bindings.get(bName);
                int minOccur = binding.getMinOccur();
                if (minOccur != 0) {
                    throw new CoreXmlException (
                        "attribute is not optional but has not been set: " 
                        + bName);
                } else {
                    if (binding.hasDefault()) {
                        binding.applyDefault (o);
                    }
                }
            }
        }
    }
    /** 
     * Use the AttrBag to create XML nodes.  The ordering, the AttrBag,
     * is used to draw values from the object.  These values are used 
     * to set attributes in the Element.
     *
     * XXX A better implementation would support defaults and would
     * not generate XML where the current value is the default.
     * 
     * @param elm Element to which attributes are assigned
     * @param o   object from which values are drawn
     */
    protected void generate (Element elm, Object o)
                                            throws CoreXmlException {
        Iterator it = bindings.keySet().iterator();
        while (it.hasNext())
            bindings.get(it.next()).generate (elm, o);
    }
}
    
