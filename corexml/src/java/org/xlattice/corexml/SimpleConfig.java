/* SimpleConfig.java */
package org.xlattice.corexml;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Bind elements from a simple configuration file to fields in
 * the object.  The configuration file has an optional XML
 * declaration, which will be ignored, possibly some comments,
 * which will be ignored, one top-level element, and an arbitrary
 * number of subelements.  The name of the top-level element is 
 * ignored.  The subelements follow the pattern
 * <pre>
 *   &lt;tag&gt;value&lt;/tag&gt;
 * </pre>
 * This method looks for a <code>setTag(TYPE newValue)</code> method in the 
 * object.  If it finds them, it attempts to use the method 
 * to set the field corresponding to the element name (the tag)
 * to the element value.
 *
 * @author Jim Dixon
 */
public class SimpleConfig {
   
    private SimpleConfig () {}

    /** 
     * Binds elements in a simple XML configuration file to fields with
     * the same name in an object.
     * <p/>
     * Returns the number of fields bound, throws an exception if a
     * tag is found that cannot be bound to a field.
     * 
     * @param o      the object whose fields are being bound
     * @param reader should contain an XML configuration file
     * @return       number of tags found and bound
     * @throws IOException if the input ends prematurely
     * @throws CoreXmlException if there is any other problem reading the input
     * @throws NumberFormatException 
     */
    static public synchronized int bind ( Object o, Reader reader) 
                throws CoreXmlException, IOException, NumberFormatException {
        if (o == null || reader == null) {
            throw new NullPointerException();
        }
        Map map = new HashMap();
        try {
            XmlPullParser xpp = XmlPullParserFactory
                                        .newInstance().newPullParser();
            xpp.setInput(reader);
    
            // get past start of enclosing element
            int event = xpp.next();
            if (event != xpp.START_TAG) 
                throw new CoreXmlException("expected element");
            String tag = xpp.getName();
            
            // process subelements
            for ( event = xpp.next(); event != xpp.END_TAG; 
                                                        event = xpp.next()) {
                if (event == xpp.TEXT) 
                    continue;
    
                if (event != xpp.START_TAG) 
                    throw new CoreXmlException("expected subelement");
                
                String name = xpp.getName();
                
                event = xpp.next();
                if (event != xpp.TEXT) 
                    throw new CoreXmlException("missing subelement value");
                
                String value = xpp.getText();
                
                event = xpp.next();
                if (event != xpp.END_TAG) 
                    throw new CoreXmlException("subelement not ended");
                if (!name.equals(xpp.getName()))
                    throw new CoreXmlException("unmatched end tag");
                map.put(name, value);    
            }
            // we are on an end tag
            if (!tag.equals(xpp.getName()))
                throw new CoreXmlException("unmatched end tag " 
                        + xpp.getName());
    
            Method [] methods = o.getClass().getMethods();
            
            // bind tag values (all of which will be Strings) to fields
            Iterator it = map.keySet().iterator();
            while (it.hasNext()) {
                String name  = (String) it.next();
                String value = (String) map.get(name);
                StringBuffer setName = new StringBuffer("set")
                        .append(String.valueOf(
                                Character.toUpperCase(name.charAt(0))));
                if (name.length() > 1) 
                    setName.append(name.substring(1));
                String setter = setName.toString();
    
                int mIndex;
                for (mIndex = 0; mIndex < methods.length; mIndex++) 
                    if (setter.equals(methods[mIndex].getName()))
                        break;
                if (mIndex >= methods.length) 
                    throw new CoreXmlException("no field matches subelement "
                            + name);
    
                // we know the setter's index; now get its argument type
                Class [] argTypes = methods[mIndex].getParameterTypes();
                if (argTypes.length != 1) 
                    throw new CoreXmlException("setter " + setter 
                            + " has wrong number of parameters (" 
                            + argTypes.length + ")");
                Class argType = argTypes[0];
                Object myArg = null;
    
                if (argType == java.lang.Integer.TYPE) {
                    if ( value.length() > 2 && (
                            value.startsWith("0x") || value.startsWith("0X")))
                        myArg = Integer.valueOf(value.substring(2), 16);
                    else 
                        myArg = Integer.valueOf(value);
                } else if (argType == java.lang.Long.TYPE) {
                    if ( value.length() > 2 && (
                            value.startsWith("0x") || value.startsWith("0X"))) 
                        myArg = Long.valueOf(value.substring(2), 16);
                    else 
                        myArg = Long.valueOf(value);
                } else if (argType == java.lang.Character.TYPE) {
                    // XXX THROW EXCEPTION IF MORE THAN ONE CHAR
                    myArg = new Character(value.charAt(0));
                } else if (argType == java.lang.Float.TYPE) {
                    myArg = Float.valueOf(value);
                } else if (argType == java.lang.Double.TYPE) {
                    myArg = Double.valueOf(value);
                } else if (argType == java.lang.Boolean.TYPE) {
                    myArg = Boolean.valueOf(value);
                } else if (argType == String.class) {
                    myArg = value;
                }
    
                if (myArg == null) 
                    throw new CoreXmlException ("can't handle parameter for "
                            + setter + "; type is " + argType);
                try { 
                    methods[mIndex].invoke(o, new Object[] {myArg} );
                } catch (IllegalAccessException iae) { 
                    throw new CoreXmlException (iae.toString());
                } catch (IllegalArgumentException iae) {
                    throw new CoreXmlException (iae.toString());
                } catch (InvocationTargetException ite) {
                    throw new CoreXmlException (ite.toString());
                }
            } 
        } catch (XmlPullParserException e) {
            throw new CoreXmlException (e.toString());
        }
        return map.size();
    }
}
