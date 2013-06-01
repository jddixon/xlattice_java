/* SubMapping.java */
package org.xlattice.corexml.bind;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//import org.xlattice.corexml.CoreXml;
import org.xlattice.corexml.CoreXmlException;
import static org.xlattice.corexml.bind.NameGenerator.*;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.Node;
import org.xlattice.corexml.om.NodeList;
import org.xlattice.corexml.om.Text;

/**
 * Create a mapping from an XML subelement into a Java class.
 * This is a three-way binding, associating the name (tag) of 
 * the XML element with a field in a Java (parent) class and 
 * the class instances of which will be attached to that field.
 * On converting from XML, when a SubMapping is encountered,
 * a child object is created and added to the parent class by
 * calling the setter for the field.  On converting from Java
 * objects to XML, a SubMapping Join causes the appropriate 
 * getter to be called on the current object, fetching a child
 * object, then creating a corresponding child Element, which
 * is attached to the current node in the XML output.  In both
 * cases, after this initial step, the SubMappings Joins are 
 * applied.
 *
 * A SubMapping has an ordering attached to it.  This defines
 * the set or list of Joins to be applied when using the 
 * SubMapping.
 *
 * The SubMapping may be optional, in which case it need not 
 * appear in the XML.  It is also possible to use it repeatedly.
 * On converting from XML, this means that repeated occurrences
 * of the element involved will each cause another instance of
 * the corresponding class to be created and assigned to the 
 * appropriate field in the current object.  On output, if the
 * SubMapping is repeated, then for each child object an 
 * element will be added to the XML and the SubMapping's Joins
 * then used recursively to assign values and subelements to that
 * element.
 *
 * @author Jim Dixon
 */
public class SubMapping extends Join {

    /** 
     * The field in the parent class that the child class will
     * be assigned to.
     */
    private final String fieldName;

    /**
     * The child class, the class that Joins in the SubMapping's
     * ordering get bound to.
     */
    private String className;
    /** 
     * The parent class, the class that the child class will hang
     * off of.
     */
    private Class  upClazz;

    /** the set of AttrBindings attached to this SubMapping */
    private AttrBag attrBag = new AttrBag();

    /** the set or list of Joins attached to this SubMapping */
    private Ordering ordering;
 
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create a submapping
     * @param tag          corresponding element in the XML file
     * @param className    class this element maps into
     * @param fieldName    field of the <b>parent</b> class
     * @param isOrdered    whether subelements must appear in the order given
     */
    public SubMapping ( String tag, String className, String fieldName, 
                        boolean isOrdered)  throws CoreXmlException {
        super (tag);
        this.fieldName  = fieldName;
        this.className  = className;

        if (isOrdered) 
            ordering = new Seq(); 
        else 
            ordering = new Bag();
        
    }
    public SubMapping (String tag, String className, String fieldName) 
                                            throws CoreXmlException {
        this(tag, className, fieldName, true);      // defaults to ordered
    }
    public SubMapping (String tag, String className)
                                            throws CoreXmlException {
        this(tag, className, dehyphenate(tag), true);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    protected AttrBag getAttrBag() {
        return attrBag;
    }
    protected String getClassName() {
        return className;
    }
    public SubMapping setMaxOccur (int n) {
        _setMaxOccur(n);
        return this;
    }
    public SubMapping setMinOccur (int n) {
        _setMinOccur(n);
        return this;
    }
    /**
     * The joined element or attribute need not appear in the XML
     * and should not be output if the field has its default value.
     *
     * @return reference to this, for chaining
     */
    public SubMapping optional() {
        return setMinOccur(0);
    }
    /**
     * The joined element may be repeated any number of times.
     *
     * @return reference to this, for chaining
     */
    public SubMapping repeats() {
        return setMaxOccur(Integer.MAX_VALUE);
    }
    /**
     * @return the ordering (Seq or Bag) of the SubMapping
     */
    protected Ordering getOrdering() {
        return ordering;
    }

    // GETTER, SETTER, AND SIZER //////////////////////////
    public SubMapping setGetter(String s) {
        _setGetter(s);
        return this;
    }
    public SubMapping setSetter(String s) {
        _setSetter(s);
        return this;
    }
    public SubMapping setSizer(String s) {
        _setSizer(s);
        return this;
    }
    /**
     * Return the class of the parent in this Mapping.  Undefined
     * until the <code>join()</code> call.
     *
     * @return the parent class
     */
    protected Class getUpClazz () {
        return upClazz;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Add a Join to the SubMapping's Ordering.  This is the 
     * essential step in defining a SubMapping.  If the ordering
     * is sequential (a Seq), Joins will be applied in exactly the
     * same order in which they are added.
     * 
     * @param j the Join to be added
     * @return  reference to this object, for chaining
     */
    public SubMapping add (Join j)          throws CoreXmlException {
        if (j.joinType() == Join.ATTRBINDING)
            attrBag.add(j);
        else
            ordering.add(j);
        return this;
    }
    /**
     * Join this SubMapping to its parent class and join the 
     * SubMapping's children to its own class; set method names
     * for getter, setter, and sizer.  This call is invoked 
     * recursively when a <code>Mapping.join()</code> call is made
     * as the last step in defining a Mapping.
     */
    protected void join (Class clazz)          throws CoreXmlException {
        // class that attribute and subelement values map into
        try {
            setClazz (Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new CoreXmlException ("can't find " + className);
        }
        upClazz = clazz;
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
        findMethods(upClazz); 
        // JOIN CHILDREN //////////////////////////////////
        attrBag.join(this.clazz);
        ordering.join(this.clazz);
    }
    /** return the type of the Join */
    protected int joinType() {
        return SUBMAPPING;
    }
    // APPLY ////////////////////////////////////////////////////////
    /** 
     * Use the SubMapping to create a Java object from values in 
     * XML.  Generally this will create a subtree of such objects
     * as a result of applying Joins in the SubMappings ordering.
     * 
     * @param node XML Element submapping is being applied to
     * @param o    parent object that the node (Element) is being bound to
     */
    protected void apply (Node node, Object o) throws CoreXmlException {
        if (!joined())
            throw new IllegalStateException(
                    "cannot apply binding, has not yet been joined");
        if (o.getClass() != upClazz )
            throw new IllegalArgumentException(
                    "binding for class " + upClazz.getName() 
                    + " being applied to instance of " 
                    + o.getClass().getName());
        if(! (node instanceof Element) ) 
            throw new IllegalArgumentException(
                "argument should be " + name + " Element but is " + node);
        Element elm = (Element) node;
        ///////////////////////////////////////////////////
        Object subObj = null;
        try {
            subObj = clazz.newInstance();
        } catch (InstantiationException ie) {
            throw new CoreXmlException(
                "cannot create object for submapping: " 
                + ie);
        } catch (IllegalAccessException ie) {
            throw new CoreXmlException(
                "cannot create object for submapping: " 
                + ie);
        } 
        ///////////////////////////////////////////////////
        // attach an instance of this subObject to the parent
        try {
            setter.invoke(o, new Object [] { subObj } ); 
        } catch (IllegalAccessException iae) {
            throw new CoreXmlException(
                    "invoking setter " + setter.getName() 
                    + " on " + elm.getName() + ": " + iae);
        } catch (IllegalArgumentException iae) {
            throw new CoreXmlException(
                    "invoking setter " + setter.getName() 
                    + " on " + elm.getName() + ": " + iae);
        } catch (InvocationTargetException ie) {
            throw new CoreXmlException(
                    "invoking setter " + setter.getName() 
                    + " on " + elm.getName() + ": " + ie);
        }
        attrBag.apply(elm, subObj);
        ordering.apply(elm, subObj);
    } 

    // GENERATE /////////////////////////////////////////////////////
    /**
     * Get an object from the appropriate field in its parent,
     * using the getter previously set.  This form is used when
     * maxOccur == 1, so an index does not have to be supplied.
     *
     * @param o parent object from which subobject is obtained
     * @return  the child object, an instance of the SubMapping's class
     */
    private Object getSubObject(Object o)  throws CoreXmlException {
        Object subObj;
        try {
            // Adding a (Object) cast eliminates a warning message
            // but results in two errors in Test3LevelGen, one in
            // testTwoLevelMapping(), one in testFullMapping(),
            // both for wrong number of arguments.  Cast to Object[]
            // fixes both.
            subObj = getter.invoke(o, (Object[]) null); 
        } catch (IllegalAccessException iae) {
            throw new CoreXmlException(
                    "invoking getter: " + iae);
        } catch (IllegalArgumentException iae) {
            throw new CoreXmlException(
                    "invoking getter: " + iae);
        } catch (InvocationTargetException ie) {
            throw new CoreXmlException(
                    "invoking getter: " + ie);
        } 
        return subObj;
    }
    /**
     * Get an object from the appropriate field in the parent,
     * using the getter and an index number.  This form is used
     * when maxOccur is greater than one, that is, when there
     * may be more than one object corresponding to this SubMapping.
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
     * Generate XML from an object or tree of objects using this
     * SubMapping.
     * 
     * @param parent XML node that we are adding subnodes to
     * @param o      instance of the class we get objects from
     */
    protected void generate (Node parent, Object o) 
                                            throws CoreXmlException {
        if (!joined())
            throw new IllegalStateException(
                    "cannot apply binding, has not yet been joined");
        if (o.getClass() != upClazz ) {
            throw new CoreXmlException(
                    "binding for class " + upClazz.getName() 
                    + " being applied to instance of " 
                    + o.getClass().getName());
        }
        if (!(parent instanceof Element))
            throw new IllegalArgumentException ("parent must be Element, is "
                    + parent);
        Element elm = (Element)parent;
        Element subElm;
       
        // generate the subelement(s) /////////////////////
        Object subObj;
        if (maxOccur == 1) {
            subObj = getSubObject(o);
            if (subObj == null) {
                if (minOccur == 0)      // if (optional) ...
                    return;
                else 
                    throw new CoreXmlException (
                            "missing instance of " + className);
            }
            subElm = new Element (name); 
            elm.addChild(subElm);
            attrBag.generate  (subElm, subObj);
            ordering.generate (subElm, subObj);    
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
                subElm = new Element (name); 
                elm.addChild(subElm);
                subObj = getSubObject(o, i); 
                attrBag.generate  (subElm, subObj);
                ordering.generate (subElm, subObj);
            } // GEEP
        }
    }
    /**
     * Generate XML from an object or tree of objects using this
     * SubMapping; used by Interface, which gets the (sub)objects to be
     * generated.
     * 
     * @param parent XML node that we are adding subnodes to
     * @param subObj object drawn from an instance of the class we get objects from
     */
    protected void generateSubObject (Node parent, Object subObj) 
                                            throws CoreXmlException {
        if (!joined())
            throw new IllegalStateException(
                    "cannot apply binding, has not yet been joined");
        if (!(parent instanceof Element))
            throw new IllegalArgumentException ("parent must be Element, is "
                    + parent);
        Element elm = (Element)parent;
        Element subElm;
       
        // generate the subelement(s) /////////////////////
        subElm = new Element (name); 
        elm.addChild(subElm);
        attrBag.generate  (subElm, subObj);
        ordering.generate (subElm, subObj);    
    }
}
