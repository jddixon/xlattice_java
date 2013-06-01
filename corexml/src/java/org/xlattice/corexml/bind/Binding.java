/* Binding.java */
package org.xlattice.corexml.bind;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xlattice.corexml.CoreXml;
import org.xlattice.corexml.CoreXmlException;
import static org.xlattice.corexml.bind.NameGenerator.*;

/**
 * Superclass for Joins that bind XML element or attribute values
 * to fields in Java objects.  The fields are either Java primitives
 * or Strings.
 * 
 * @author Jim Dixon
 */
public abstract class Binding extends Join {

    /////////////////////////////////////////////////////////////////
    // ARGUMENT/RESULT TYPES 
    // //////////////////////////////////////////////////////////////
    /** boolean field */
    public static final int BOOLEAN = 0;
    public static final int CHAR    = 1;
    public static final int FLOAT   = 2;
    public static final int DOUBLE  = 3;
    public static final int BYTE    = 4;
    public static final int SHORT   = 5;
    public static final int INT     = 6;
    public static final int LONG    = 7;
    /** String field */
    public static final int STRING  = 8;

    /** names of field types, for diagnostic messages */
    public static final String[] TYPES = {
        "boolean",  "char",     "float",    "double",
        "byte",     "short",    "int",      "long",     "String"};

    /////////////////////////////////////////////////////////////////
    /** name of the object field being bound to */
    protected final String fieldName;
    
    /** argument type as index into the list of types above */
    protected int    argTypeIndex;


    // CONSTRUCTORS /////////////////////////////////////////////////
    /** 
     * Create a binding between an XML name (tag or attribute name)
     * and a field in an associated class.  By default the setter
     * method name is 'set' concatenated with the field name with first
     * letter capitalized.  The getter name is constructed in the 
     * same way.  These default names may be overridden.
     *
     * @param name of an element name (tag) or attribute name
     * @param fieldName base name used in field setter/getter methods
     */
    public Binding (String name, String fieldName) {
        super (name);
        if (fieldName == null)
            throw new NullPointerException("null field name");
        if (fieldName.equals(""))
            throw new IllegalArgumentException("missing field name");
        this.fieldName = fieldName;
    }
    public Binding (String name) {
        this(name, name);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /**
     * Set the maximum number of times a bound value may occur.
     * @param n maximum number of occurrences
     * @return reference to this, for chaining
     */
    public Binding setMaxOccur (int n) {
        _setMaxOccur(n);
        return this;
    }
    /**
     * Set the minimum number of times a bound value must occur.
     * @param n minimum number of occurrences
     * @return reference to this, for chaining
     */
    public Binding setMinOccur (int n) {
        _setMinOccur(n);
        return this;
    }
    /**
     * The joined element or attribute need not appear in the XML
     * and should not be output if the field has its default value.
     * That is, this sets minOccur to zero.
     *
     * XXX DEFAULT VALUE CHECKS ARE NOT CURRENTLY IMPLEMENTED
     *
     * @return reference to this, for chaining
     */
    public Binding optional() {
        return setMinOccur(0);
    }
    /**
     * The joined element may be repeated any number of times.
     * That is, this sets maxOccur to Integer.MAX_VALUE.
     *
     * @return reference to this, for chaining
     */
    public Binding repeats() {
        return setMaxOccur(Integer.MAX_VALUE);
    }

    // GETTER, SETTER, AND SIZER //////////////////////////
    /** 
     * Set the unqualified name of the field getter method.  This 
     * overrides the default getter name.
     *
     * @param g new name of the getter
     * @return  a reference to this Binding, a convenience in chaining
     */
    public Binding setGetter (String g) {
        _setGetter(g);
        return this;
    }
    /** 
     * Set the unqualified name of the field setter method.  This 
     * overrides the default setter name.
     *
     * @param s new name of the setter
     * @return  a reference to this Binding, a convenience in chaining
     */
    public Binding setSetter (String s) {
        _setSetter(s);
        return this;
    }
    /** 
     * Set the unqualified name of the sizer method.  This 
     * overrides the default setter name.
     *
     * The sizer method is used to determine the number of values
     * present in a bound Java object.
     *
     * @param s new name of the setter
     * @return  a reference to this Binding, a convenience in chaining
     */
    public Binding setSizer (String s) {
        _setSizer(s);
        return this;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Method called when the definition of the Mapping is complete.
     * This assigns the parent class, finds getter/setter/sizer
     * methods, and determines the type of the field in the bound
     * object.
     */
    protected void join (Class clazz) throws CoreXmlException {
        setClazz (clazz);
        findMethods(clazz);
        // CAN BE DONE IN findMethods()
        Class[] argTypes = setter.getParameterTypes();
        argTypeIndex = getTypeIndex(argTypes[0]);    
    }
    /** 
     * Overrides the method in the superclass, Join.  Done this way
     * because we need to know the setter type before assigning the
     * default getter type.
     */
    protected void findMethods(Class clazz) throws CoreXmlException {
        // set names if not already set
        if (setterName == null) 
            setterName    = setterName(fieldName);
        if (sizerName == null) {
            // IS THIS RIGHT??
            sizerName     = sizerName(fieldName);
        } 
        Method [] methods = clazz.getMethods();
        boolean getterFound = false;
        boolean setterFound = false;
        boolean sizerFound  = (maxOccur == 1);
        for (int i = 0; 
                i < methods.length && (!setterFound || !sizerFound)
                                                            ; i++) {
            String name = methods[i].getName();
            if (!setterFound && name.equals(setterName)) {
                setterFound = true;
                setter = methods[i];
                Class[] argTypes = methods[i].getParameterTypes();
                if (argTypes.length != 1)
                    throw new IllegalStateException(
                            "setter should take 1 parameter, actually takes "
                            + argTypes.length);
                argTypeIndex = getTypeIndex(argTypes[0]);
                if (getterName == null) {
                    if (argTypeIndex == BOOLEAN)
                        getterName = isName(fieldName);
                    else
                        getterName = getterName(fieldName);
                }
            }
            if (!sizerFound && name.equals(sizerName)) {
                sizerFound = true;
                sizer = methods[i];
            }
        } 
        // do second check for getterName, which may have just 
        // been set
        for (int i = 0; i < methods.length && !getterFound; i++) {
            String name = methods[i].getName();
            if (!getterFound && name.equals(getterName)) {
                getterFound = true;
                getter = methods[i];
                // RESULT TYPE SHOULD BE SAME AS SETTER ARG TYPE
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
    } 
    /**
     * Get the value of the associated field in a bound object
     * as a String.
     * @param o object involved in this Binding
     */
    protected String getField (Object o) throws CoreXmlException {
        Object result;
        try {
            // XXX Adding an (Object cast) eliminates a warning but
            // results in three errors (wrong number of arguments)
            // in testDefaultBindings.  Casting to Object[] fixes.
            // Message from JRE advises the first cast, is wrong.
            result = getter.invoke(o, (Object[]) null); 
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
        String value;
        if (argTypeIndex == STRING)
            value = (String)result; // can be null, so next gives NPE
        else 
            value = result.toString();
        return value;
    }
    /**
     * Set a field in the bound object to the value passed.
     *
     * The calling method must have confirmed that the object is
     * an instance of the appropriate class and that the binding
     * has been joined.
     * 
     * @param o     object that the binding is to be applied to
     * @param value String value to be set; must be converted
     */
    protected void setField (Object o, String value) 
                                            throws CoreXmlException {
        if (value == null)
            throw new IllegalArgumentException (
                                    "setField called with null value");
        Object myArg = null;
        switch (argTypeIndex) {
            case BOOLEAN:
                // XXX doesn't follow XPath approach
                // XXX also not consistent with documentation elsewhere!
                myArg = Boolean.valueOf(value);
                break;
            case CHAR:
                if (value.length() != 1)
                    throw new IllegalArgumentException(
                            "char field but XML value of length " 
                            + value.length());
                myArg = new Character (value.charAt(0));
                break;
            case FLOAT:
                myArg = Float.valueOf(value);
                break;
            case DOUBLE:
                myArg = Double.valueOf(value);
                break;
            case BYTE:
                if (value.length() > 2 && 
                        (value.startsWith("0x") || value.startsWith("0X")))
                    myArg = Byte.valueOf(value.substring(2, 16));
                else
                    myArg = Byte.valueOf(value);
                break;
            case SHORT:
                if (value.length() > 2 && 
                        (value.startsWith("0x") || value.startsWith("0X")))
                    myArg = Short.valueOf(value.substring(2, 16));
                else
                    myArg = Short.valueOf(value);
                break;
            case INT:
                if (value.length() > 2 && 
                        (value.startsWith("0x") || value.startsWith("0X")))
                    myArg = Integer.valueOf(value.substring(2, 16));
                else
                    myArg = Integer.valueOf(value);
                break;
            case LONG:
                if (value.length() > 2 && 
                        (value.startsWith("0x") || value.startsWith("0X")))
                    myArg = Long.valueOf(value.substring(2, 16));
                else
                    myArg = Long.valueOf(value);
                break;
            case STRING:
                myArg = value;
                break;
            default:
                throw new CoreXmlException(
                        "parameter type out of range: " + argTypeIndex);
        }
        try {
            setter.invoke(o, new Object [] { myArg } ); 
        } catch (IllegalAccessException iae) {
            throw new CoreXmlException(
                    "invoking setter: " + iae);
        } catch (IllegalArgumentException iae) {
            throw new CoreXmlException(
                    "invoking setter: " + iae);
        } catch (InvocationTargetException ie) {
            throw new CoreXmlException(
                    "invoking setter with value ["
                    + value + "]: " + ie);
        } 
    }
    /** @return the string corresponding to a type index */
    protected static final String typeToString (int i) {
        return TYPES[i];
    }
    
    /**
     * Given a method argument type learned by reflection, 
     * return the index of that type.
     *
     * @param argType learned from reflection
     * @return        type index
     */
    protected static int getTypeIndex(Class argType) {
        if (argType == java.lang.Boolean.TYPE) 
            return BOOLEAN;
        else if (argType == java.lang.Character.TYPE) 
            return CHAR;
        else if (argType == java.lang.Float.TYPE) 
            return FLOAT;
        else if (argType == java.lang.Double.TYPE) 
            return DOUBLE;
        else if (argType == java.lang.Byte.TYPE) 
            return BYTE;
        else if (argType == java.lang.Short.TYPE) 
            return SHORT;
        else if (argType == java.lang.Integer.TYPE) 
            return INT;
        else if (argType == java.lang.Long.TYPE) 
            return LONG;
        else if (argType == java.lang.String.class) 
            return STRING;
        else 
            throw new IllegalArgumentException (
                    "unrecognized argument or return type " + argType);
    }
}
