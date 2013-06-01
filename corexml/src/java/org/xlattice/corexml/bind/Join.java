/* Join.java */
package org.xlattice.corexml.bind;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.Node;

/**
 * Superclass of mappings between XML nodes and Java classes.  These
 * may be Bindings, which associate element and attribute values to
 * object fields; SubMappings, which associate elements with Java
 * class instances; or Collectors, which signal that a tag will appear
 * in the XML with no correspondence to an object field value or
 * instance.
 *
 * @author Jim Dixon
 */
public abstract class Join {

    /** Join type has not been specified, an internal error */
    public final static int UNSPECIFIED = -1;
    /** reserved for Mappings */
    public final static int MAPPING     = 0;
    /** Join is a SubMapping */
    public final static int SUBMAPPING  = 1;
    /** Join is a Collector */
    public final static int COLLECTOR   = 2;
    /** Join is a AttrBinding */
    public final static int ATTRBINDING = 3;
    /** Join is a EmptyElBinding  */
    public final static int EMPTYEL     = 4;
    /** Join is a SubElBinding */
    public final static int SUBEL       = 5;
    /** Join is a TextBinding  */
    public final static int TEXT        = 6;
    /** Join is an Interface */
    public final static int INTERFACE   = 7;

    /** Join subtypes, plus Mapping, for debug and error messages. */
    public final static String[] JOIN_TYPES = {
        "mapping", "submapping", "collector", "attrbinding",
        "emptyel", "subel",      "text",      "Interface" };

    /** element or attribute name in the element being joined */
    protected final String name;
    /**
     * Java class whose field XML is being bound to.
     */
    protected Class clazz;
    /** minimum number of occurrences of this Join in Ordering */
    protected int minOccur = 1;     // by default, is not optional
    /** maximum number of occurrences of this Join in Ordering */
    protected int maxOccur = 1;     // by default, only one permitted

    /** whether the binding has been established */
    protected boolean joined = false;

    /** getter method used to fetch field value from object */
    protected Method getter;
    protected String getterName;
    /** setter method used to set value of field in the bound object */
    protected Method setter;
    protected String setterName;
    /** used to get count of value instances in the bound object.  */
    protected Method sizer;
    protected String sizerName;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Bind an element or attribute value to a field in a Class.
     *
     * @param name attribute name or element tag
     */
    public Join (String name) {
        if (name == null)
            throw new NullPointerException("Join name cannot be null");
        // TextBindings are nameless :-(
//      if (name.equals(""))
//          throw new IllegalArgumentException("name must be specified");
        this.name = name;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /**
     * @return the Class being bound to
     */
    protected Class getClazz() {
        return clazz;
    }
    /**
     * @return the name of the element or attribute joined
     */
    protected String getName() {
        return name;
    }
    protected boolean tagMatch (String tag) {
        return tag == name;
    }
    /** @return the maximum number of occurrences permitted */
    protected int getMaxOccur () {
        return maxOccur;
    }
    /** @return the minimum number of occurrences that must be present */
    protected int getMinOccur () {
        return minOccur;
    }
    /**
     * Set the maximum number of occurrences of the attribute
     * or element in the XML.
     *
     * @return reference to this, for chaining
     */
    protected Join _setMaxOccur(int max) {
        if (joined)
            throw new IllegalStateException(
                    "can't modify maxOccur after join() call");
        if (joinType() == ATTRBINDING) {
            if (max != 1)
                throw new IllegalArgumentException(
                    "cannot set AttrBinding maxOccur to " + max);
        } else {
            if (max <= 0)
                throw new IllegalArgumentException (
                    "maxOccur must be positive: " + max);
            if (max < minOccur)
                throw new IllegalArgumentException (
                    "maxOccur (" + max
                    + ") cannot be less than minOccur (" + minOccur + ")");
        }
        maxOccur = max;
        return this;
    }
    /**
     * Set the minimum number of occurrences of the attribute
     * or element in the XML.
     *
     * @return reference to this, for chaining
     */
    protected Join _setMinOccur(int min) {
        if (joined)
            throw new IllegalStateException(
                    "can't modify minOccur after join() call");
        if (min < 0)
            throw new IllegalArgumentException(
                    "minOccur cannot be negative: " + min);
        if (min > maxOccur)
            throw new IllegalArgumentException(
                    "minOccur (" + min
                    + ")  cannot exceed maxOccur (" + maxOccur + ")");
        minOccur = min;
        return this;
    }
    // GETTER, SETTER, AND SIZER //////////////////////////
    /** @return the unqualified name of the getter method */
    protected String _getGetterName() {
        return getterName;
    }
    /**
     * Assign a name to the getter method, overriding any default.
     */
    protected void _setGetter(String s) {
        checkNameAndState (s, "getter");
        getterName = s;
        /* return this? */
    }
    /** @return the unqualified name of the setter method */
    protected String _getSetterName() {
        return setterName;
    }
    /**
     * Assign a name to the setter method, overriding any default.
     */
    protected void _setSetter(String s) {
        checkNameAndState (s, "setter");
        setterName = s;
        /* return this? */
    }
    /** @return the unqualified name of the sizer method */
    protected String _getSizerName() {
        return sizerName;
    }
    /**
     * Assign a name to the sizer method, overriding any default.
     */
    protected void _setSizer(String s) {
        checkNameAndState (s, "sizer");
        sizerName = s;
    }
    /**
     * Return a count of the number of instances in the parent
     * object that correspond to this Join.
     *
     * XXX MAY NOT RETURN A CORRECT VALUE IF minOccur == 0 and
     * XXX maxOccur == 1
     *
     * @param o object in which instances are to be found
     * @return the number of instances
     */
    protected int size(Object o)            throws CoreXmlException {
        if (!joined)
            throw new IllegalStateException (
                    "cannot get Join size until joined");
        if ( maxOccur == 1) {
            // XXX TWO CASES: constituent class (return 0 if null,
            // 1 otherwise) or primitive (return 1);
            return 1;
        } else {
            Integer result = null;
            try {
                result = (Integer) sizer.invoke(o, (Object[])null);
            } catch (IllegalAccessException iae) {
                throw new CoreXmlException(
                        "invoking sizer: " + iae);
            } catch (IllegalArgumentException iae) {
                throw new CoreXmlException(
                        "invoking sizer: " + iae);
            } catch (InvocationTargetException ie) {
                throw new CoreXmlException(
                        "invoking sizer: " + ie);
            }
            return result.intValue();
        }
    }
    /**
     * Find the setter, getter, and sizer methods in the class.
     * This is called recursively by the <code>Mapping.join()</code>
     * after Mapping definition is complete.  If this method is
     * called more than once, the results are unpredictable.
     */
    protected void findMethods(Class clazz) throws CoreXmlException {
        Method [] methods = clazz.getMethods();
        boolean getterFound = false;
        boolean setterFound = false;
        boolean sizerFound  = (maxOccur == 1);
        for (int i = 0; i < methods.length &&
                (!getterFound || !setterFound || !sizerFound)
                                                                    ; i++) {
            String name = methods[i].getName();
            if (!getterFound && name.equals(getterName)) {
                getterFound = true;
                getter = methods[i];
                // RESULT TYPE SHOULD BE SAME AS SETTER ARG TYPE
            }
            if (!setterFound && name.equals(setterName)) {
                setterFound = true;
                setter = methods[i];
                Class[] argTypes = methods[i].getParameterTypes();
                if (argTypes.length != 1)
                    throw new IllegalStateException(
                            "setter should take 1 parameter, actually takes "
                            + argTypes.length);
                // ONLY FOR BINDINGS:
//              if (this instanceof Binding)
//                  ((Binding)this).argTypeIndex = getTypeIndex(argTypes[0]);
            }
            if (!sizerFound && name.equals(sizerName)) {
                sizerFound = true;
                sizer = methods[i];
            }
        }
        if (!getterFound || !setterFound || !sizerFound) {
            StringBuffer sb = new StringBuffer("class: ")
                .append(clazz.getName()).append(": ")
                .append("\n    no match on setter, getter, or sizer name(s):");
            if (!getterFound)
                sb.append (" ").append(getterName);
            if (!setterFound)
                sb.append (" ").append(setterName);
            if (!sizerFound)
                sb.append (" ").append(sizerName);
            throw new CoreXmlException(sb.toString());
        }
    } // GEEP
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Called by subclasses after integrity checks, finding getter/
     * setter/sizer methods, and anything else related to a join()
     * call.  The join() call is made as the last step in setting
     * up a Mapping.  There must be only one join() call and so only
     * one Join.setClazz() call.
     *
     * @param clazz the class involved in the binding
     */
    protected void setClazz (Class clazz) {
        if (clazz == null)
            throw new NullPointerException("null class reference");
        if (joined)
            throw new IllegalStateException("second call to join");
        // INTEGRITY CHECKS ON minOccur AND maxOccur
        if (minOccur < 0)
            throw new IllegalStateException (
                    "minOccur is " + minOccur
                    + " but must be non-negative");
        if (minOccur > maxOccur)
            throw new IllegalStateException (
                    "minOccur " + minOccur
                    + " exceeds maxOccur " + maxOccur);
        if (maxOccur == 0)
            throw new IllegalStateException (
                    "maxOccur is " + maxOccur + " but must be positive");

        this.clazz = clazz;
        joined = true;
    }
    /**
     * Whether the recursive <code>Mapping.join()</code> call has
     * been made.  Once this is set, no further changes to the
     * Mapping definition may be made.
     *
     * @return whether XML/object binding has been set up
     */
    protected boolean joined () {
        return joined;
    }

    /**
     * Check whether the Join has been set up and if so whether the
     * object is an instance of the bound class.
     *
     * XXX If Join is an Interface, object may belong to any of
     * several classes.  So we need a boolean matchClass(Object o)
     *
     * @param o  object whose class is inspected
     */
    protected void checkJoinObject(Object o) {
        if (!joined())
            throw new IllegalStateException(
                    "cannot apply binding, has not yet been joined");
        if (o.getClass() != clazz )
            throw new IllegalArgumentException(
                    "binding for class " + clazz.getName()
                    + " being applied to instance of "
                    + o.getClass().getName());
    }
    /**
     * Check whether the target Node is an Element after checking
     * whether the Join has been set up and the object is an instance
     * of the appropriate class.
     */
    protected Element checkElmJoin (Node node, Object o) {
        checkJoinObject(o);
        if(! (node instanceof Element) )
            throw new IllegalArgumentException(
                "argument should be " + name + " Element but is " + node);
        return (Element) node;
    }
    /**
     * Confirm that the name being assigned is neither null nor
     * empty and that there has been no <code>join()</code> call.
     */
    protected void checkNameAndState (String name, String what) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException (what
                                    + " cannot be null or empty");
        if (joined)
            throw new IllegalStateException ("cannot change "
                    + what + " after join() call");
    }
    // ABSTRACT METHODS /////////////////////////////////////////////
    /**
     * Set the clazz for this Join and any descendents.  Subclasses
     * should call setClazz() to set the clazz here, in the parent.
     *
     * @param clazz Class involved in the Join
     */
    protected abstract void join (Class clazz)  throws CoreXmlException;

    /** @return the type index for a Join */
    protected abstract int joinType();

    /**
     * Apply a Join, binding an XML value to a field in the object.
     * @param node an XML element or attribute
     * @param o    an instance of the class being bound to
     */
    protected abstract void apply (Node node, Object o)
                                                throws CoreXmlException;

    /**
     * Apply a Join in the opposite direction, generating an
     * XML element or attribute whose value depends upon the value
     * of the bound field in the object.
     *
     * @param parent Element to which an attribute/value pair or
     *                 or subelement is being added
     * @param o      the object whose field is used to determine the
     *                 value set
     */
    protected abstract void generate (Node parent, Object o)
                                                throws CoreXmlException;
}
