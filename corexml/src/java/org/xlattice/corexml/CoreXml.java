/* CoreXml.java */
package org.xlattice.corexml;

import org.xmlpull.v1.XmlPullParser;

/**
 * Some static helper functions.
 *
 * @author Jim Dixon
 */
public class CoreXml {

    private CoreXml() {}

    /** 
     * Capitalize the first letter of a String.  If the String is
     * null, it is replaced by an empty String.
     *
     * @deprecated Use org.xlattice.util.StringLib.ucFirst() instead
     */
    public static final String capitalize (String s) {
        if (s == null || s.equals(""))
            return "";
        
        StringBuffer sb = new StringBuffer() 
            .append (Character.toUpperCase(s.charAt(0)));
        if (s.length() > 0)
            sb.append (s.substring(1));
        return sb.toString();
    }

    /**
     * Return the type of an XmlPullParser event.
     *
     * @param  t XmlPullParser event
     * @return   String name of the event
     */
    public final static String eventType(int t) {
        if (t < 0 || t > 10) 
            throw new IllegalArgumentException("no such event type");
        return XmlPullParser.TYPES[t];
    }
}
