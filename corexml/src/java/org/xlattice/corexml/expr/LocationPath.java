/* LocationPath.java */

/**
 * @author Jim Dixon
 **/

/* LocationPath */
package org.xlattice.corexml.expr;

import java.util.ArrayList;

/**
 * XPath 1.0 location path expression.  This may be absolute (relative
 * to the document root) or relative (relative to the context node).
 * It consists of a number, possibly zero, of steps, each of which
 * moves the cursor relative to its last position.  
 * <p/>
 * As an example, "../.." is a relative location path expression 
 * which moves the cursor up two steps.
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class LocationPath extends Expr {

    /** whether the path is absolute */
    public final boolean isAbsolute;

    /** a list of the steps involved in the path */
    private ArrayList steps;

    /**
     * Construct a location path.
     * @param b if true, the path is absolute 
     */
    public LocationPath (boolean b) {
        super("locpath");
        isAbsolute = b;
        steps = new ArrayList();
    }

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return whether the location path is absolute */
    public boolean isAbsolute() {
        return isAbsolute;
    }
    /**
     * Add a step to the location path.
     * 
     * @param step the Step to be added, must not be null
     */
    public void addStep (Step step) {
        if (step == null)
            throw new NullPointerException("adding null step");
        steps.add(step);
    }
    /** @return the Nth step in the location path */
    public Step getStep (int n) {
        return (Step) steps.get(n);
    }
    /** @return the number of steps in the location path */
    public int stepCount() {
        return steps.size();
    }
    // SERIALIZATION ////////////////////////////////////////////////
    /** 
     * Return a string suitable for representing the location path 
     * in XML output.  Steps are indented.
     *
     * @param indent the number of spaces indented so far
     */
    public String xmlBody(int indent) {
        StringBuffer sb = new StringBuffer();
        String spaces = SpaceFactory.getInstance().makeSpaces(indent);
        sb.append(spaces)
          .append("<absolute>");
        if (isAbsolute)
            sb.append("true");
        else 
            sb.append("false");
        sb.append("</absolute>\n");
        for (int i = 0; i < steps.size(); i++) {
            Step step = (Step) steps.get(i);
            sb.append(spaces).append(step.startElement())
              .append(step.xmlBody(indent + 2))
              .append(spaces).append(step.endElement());
        }
        return sb.toString();
    }
}
