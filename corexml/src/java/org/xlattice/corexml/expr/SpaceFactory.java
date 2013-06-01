/* SpaceFactory.java */
package org.xlattice.corexml.expr;

import java.util.HashMap;

/**
 * Makes a String of spaces of the requested length.  Maintains
 * a cache, and returns a String from the cache if possible.
 *
 * @author Jim Dixon
 */
public class SpaceFactory {

    private static final HashMap store = new HashMap();    

    private static SpaceFactory factory = new SpaceFactory();

    private SpaceFactory () {}

    public static SpaceFactory getInstance() {
        return factory;
    }
    public static String makeSpaces(int indent) {
        if (indent <= 0)
            return "";

        Integer i = new Integer (indent);
        String candidate = (String) store.get(i);
        if (candidate != null) {
            return candidate;
        }
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < indent; j++)
                sb.append(' ');
        candidate = sb.toString();
        store.put(i, candidate);
        return candidate;
    }
}
