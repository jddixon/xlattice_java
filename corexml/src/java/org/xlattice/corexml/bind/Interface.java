/* Interface.java */
package org.xlattice.corexml.bind;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xlattice.corexml.CoreXmlException;
import static org.xlattice.corexml.bind.NameGenerator.*;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.Node;
import org.xlattice.corexml.om.NodeList;
import org.xlattice.corexml.om.Text;
import static org.xlattice.util.StringLib.*;

/**
 * On the XML side, allow any of a set of SubMappings to appear.
 * On the Java side, these correspond to classes sharing the same
 * interface.  At binding, the element name is used to determine
 * which class to bind to.
 * 
 * XXX All tags are forced to uppercase when checking whether they
 * XXX match an Interfaces submappings.
 *
 * @author Jim Dixon
 */
public class Interface extends Join {

    private String ifaceName;
    private String fieldName;
    
    private Map<String, SubMapping> tagToSub 
                                = new HashMap<String, SubMapping>();
    private Map<String, SubMapping> classNameToSub 
                                = new HashMap<String, SubMapping>();
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Set up an Interface, providing a short name for use in 
     * generating error messages.  This will usually be the bare 
     * name of the Java interface.
     * 
     * @param name      short name for the interface
     * @param ifaceName fully qualified name of the interface 
     * @param fieldName field in the <b>parent</b> class (singular)
     */
    public Interface (String name, String ifaceName, String fieldName) {
        super (name);       // BUT there is no corresponding tag

        if (ifaceName == null)
            throw new IllegalArgumentException( "null interface name");
        this.ifaceName = ifaceName;
        if (fieldName == null)
            throw new IllegalArgumentException("null field name");
        this.fieldName = fieldName;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** mark the Interface as optional: it need not appear in the XML */
    public Interface optional () {
        _setMinOccur(0);
        return this;
    }
    /**
     * The joined element may be repeated any number of times.
     *
     * @return reference to this, for chaining
     */
    public Interface repeats() {
        return setMaxOccur(Integer.MAX_VALUE);
    }
    public Interface setMaxOccur (int n) {
        _setMaxOccur(n);
        return this;
    }
    public Interface setMinOccur (int n) {
        _setMinOccur(n);
        return this;
    }
    /**
     * Return whether the tag maps to an implementor of the 
     * Interface, after forcing its first character to upper case.
     */
    public boolean tagMatch (String tag) {
        return tagToSub.containsKey(ucFirst(tag));
    }
    // GETTER, SETTER, AND SIZER //////////////////////////
    /**
     * Assign a name to the getter method, overriding the default.
     *
     * @param s the name being assigned
     * @return  a reference to this object, to ease chaining
     */
    public Interface setGetter(String s) {
        _setGetter(s);
        return this;
    }
    /**
     * Assign a name to the setter method, overriding the default.
     *
     * @param s the name being assigned
     * @return  a reference to this object, to ease chaining
     */
    public Interface setSetter(String s) {
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
    public Interface setSizer(String s) {
        _setSizer(s);
        return this;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Join anything under this Interface to its parent 
     * Class.  After a Mapping has been defined, a programm calls
     * Mapping.join(), which causes this method to be called.
     *
     * @param clazz the parent Class
     */
    protected void join (Class clazz) throws CoreXmlException {
        setClazz(clazz);
        // METHOD NAME, GETTER AND SETTER /////////////////
        if (getterName == null) {
            getterName  = getterName(fieldName);
        }
        if (setterName == null) {
            if (maxOccur > 1) {
                setterName  = adderName(fieldName);
            } else {
                setterName  = setterName(fieldName);
            }
        }
        if (maxOccur > 1 && sizerName == null) {
            sizerName  = sizerName(fieldName);
        } 
        findMethods(clazz);
        
        // it would be cheaper to iterate over tagToSub.values()
        Iterator<String> it = tagToSub.keySet().iterator();
        while (it.hasNext()) {
            String subMapName = it.next();
            SubMapping subMap = tagToSub.get(ucFirst(subMapName));
//          // XXX EXPERIMENT /////////////////////////////
            // this ensures that the SubMappings use the right 
            // name for the getter, if nothing else.
            // XXX However, this means that the SubMapping cannot 
            // XXX be reused.
            
            // maxOccur MUST be set before minOccur
            subMap.setMaxOccur( getMaxOccur() );
            subMap.setMinOccur( getMinOccur() );
//          // END ////////////////////////////////////////
            subMap.join(clazz);
         }
    }

    /**
     * Add a Join to the Interface's set.
     *
     * @param j Join to be added
     */
    public Interface add (Join j)   throws CoreXmlException {
        if (joined) 
            throw new IllegalStateException(
                    "Interface has already been joined");
        if (!(j instanceof SubMapping))
            throw new IllegalArgumentException(
                    "only SubMappings may appear under Interface: " + j);
        SubMapping sub = (SubMapping)j;
        String ucSubName = ucFirst(sub.getName());
        tagToSub.put( ucSubName, sub );
        classNameToSub.put( sub.getClassName(), sub);

        return this;                // convenience, supports chaining
    }
    // JOIN INTERFACE ///////////////////////////////////////////////
    /** @return this Join type */
    protected int joinType() {
        return INTERFACE;
    }
    // APPLY //////////////////////////////////////////////
    /** 
     * Use this Interface to generate an object or object tree from
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
        // ordering.apply (elm, o);
        String ucElmName = ucFirst(elm.getName());
        SubMapping sub = tagToSub.get(ucElmName);
        if (sub == null)
            throw new CoreXmlException(
                    "no join for " + ucElmName 
                            + " under interface " + getName());
        sub.apply(node, o);
    }
    // GENERATE /////////////////////////////////////////// 
    /**
     * Get an object from the appropriate field in its parent,
     * using the getter previously set.  This form is used when
     * maxOccur == 1, so an index does not have to be supplied.
     *
     * XXX THIS CODE LIFTED FROM SubMapping
     *
     * @param o parent object from which subobject is obtained
     * @return  the child object, an instance of the SubMapping's class
     */
    private Object getSubObject(Object o)  throws CoreXmlException {
        Object subObj;
        try {
            subObj = getter.invoke(o , (Object[]) null); 
        } catch (IllegalAccessException iae) {
            throw new CoreXmlException(
                    "invoking getter " + getterName + ": " + iae);
        } catch (IllegalArgumentException iae) {
            throw new CoreXmlException(
                    "invoking getter " + getterName + ": " + iae);
        } catch (InvocationTargetException ie) {
            throw new CoreXmlException(
                    "invoking getter " + getterName + ": " + ie);
        } 
        return subObj; 
    }
    /**
     * Get an object from the appropriate field in the parent,
     * using the getter and an index number.  This form is used
     * when maxOccur is greater than one, that is, when there
     * may be more than one object corresponding to this SubMapping.
     * 
     * XXX THIS CODE LIFTED FROM SubMapping
     *
     * @param o parent object from which subobject is obtained
     * @param n index; this is the Nth such call
     * @return  the child object, an instance of the SubMapping's class
     */
    private Object getSubObject(Object o, int n)  
                                            throws CoreXmlException {
        Object subObj;
        try {
            subObj = getter.invoke(o, 
                    (Object[]) new Integer[] { new Integer(n) }); 
        } catch (IllegalAccessException iae) {
            throw new CoreXmlException(
                    "invoking indexed getter: " + iae);
        } catch (IllegalArgumentException iae) {
            throw new CoreXmlException(
                    "invoking indexed getter: " + iae);
        } catch (InvocationTargetException ie) {
            throw new CoreXmlException(
                    "invoking indexed getter: " + ie);
        } 
        return subObj;
    }
    /**
     * Generate XML from an object which has a member matching Interface.
     *
     * @param parent  XML node that we are adding subnodes to
     * @param o       instance of the class that we get objects from
     */
    protected void generate (Node parent, Object o) throws CoreXmlException {
        if (!joined())
            throw new CoreXmlException(
                "cannot apply binding, Interface has not yet been joined");
        if (o.getClass() != clazz ) {
            throw new CoreXmlException(
                    "binding for class " + clazz.getName() 
                    + " being applied to instance of " 
                    + o.getClass().getName());
        }
        if (!(parent instanceof Element))
            throw new IllegalArgumentException ("parent must be Element, is "
                    + parent);
        if (o == null) 
            throw new IllegalArgumentException(
                    "cannot generate from null object");
        Element elm = (Element)parent;
        Element subElm;
        Object subObj;
        if (maxOccur == 1) {
            subObj = getSubObject(o);
            if (subObj == null) {
                if (minOccur == 0)      // if (optional) ...
                    return;
                else 
                    throw new CoreXmlException (
                        "non-zero minOccur but no instance of Interface " 
                        + name);
            }
            String objClazzName = subObj.getClass().getName();
            SubMapping subMap = classNameToSub
                                .get( subObj.getClass().getName() );
            if (subMap == null)
                throw new CoreXmlException(
                    "Interface " + name + " has no SubMapping matching " 
                    + objClazzName);
            subMap.generateSubObject(parent, subObj);
        } else {
            // XXX CONFUSED: if maxOccur == 1, two cases: 
            //   primitive and constituent class. In the latter case,
            //   need to set count to zero if class field is null.
            int count = size(o);
            if ((count == 0) && (minOccur == 0))
                return;
            if (count < minOccur)
                throw new CoreXmlException (
                    "minOccur is " + minOccur + " but only "
                    + count + " elements present");
            if (count > maxOccur) 
                throw new CoreXmlException (
                        "maxOccur is " + maxOccur + " but there are "
                        + count + " elements present");
            for (int i = 0; i < count; i++) {
                subObj = getSubObject(o, i);
                String objClazzName = subObj.getClass().getName();
                SubMapping subMap 
                        = classNameToSub.get( subObj.getClass().getName() );
                if (subMap == null)
                    throw new CoreXmlException(
                        "Interface " + name + " has no SubMapping matching " 
                        + objClazzName);
                subMap.generateSubObject(parent, subObj);
            } 
            
        }
        
    }

}
