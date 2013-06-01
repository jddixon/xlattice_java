/* Step.java */
package org.xlattice.corexml.expr;

import java.util.ArrayList;

/**
 * A step, part of a relative or absolute XPath Location Path.
 * 
 * @author Jim Dixon
 */
public class Step extends Expr {

    /** axis along which the step is taken */
    public  final int axis;
    /** the node test */
    public  final NodeTest nodeTest;
    /** possibly empty set of predicates to be applied */
    private final ArrayList predicates;
  
    /**
     * Create a location path Step.
     * 
     * @param along  axis along which the step is to be taken
     * @param nt     node test to be applied
     * @param single XXX NOT CURRENTLY USED
     */
    public Step (int along, NodeTest nt, boolean single) {
        super("step");
        if (!AxisName.indexInRange(along))
            throw new IllegalStateException("axis name index out of range");
        axis = along;
        if (nt == null)
            throw new NullPointerException("null NodeTest");
        nodeTest = nt;
        predicates = new ArrayList();
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public int getAxisNameIndex () {
        return axis;
    }
    public NodeTest getNodeTest() {
        return nodeTest;
    }
    public Expr getPredicate(int n) {
        return (Expr)predicates.get(n);
    }
    /**
     * Add a predicate. This will be evaluated as a boolean, using
     * XPath's type casting rules.  
     */
    public void addPredicate(Expr e) {
        if (e == null)
            throw new NullPointerException ("adding null predicate");
        predicates.add(e);
    }
    public int predicateCount() {
        return predicates.size();
    }
    // SERIALIZATION ////////////////////////////////////////////////
    public String xmlBody(int indent) {
        StringBuffer sb = new StringBuffer();
        String spaces = SpaceFactory.getInstance().makeSpaces(indent);

        sb.append(spaces).append("<axis>").append(AxisName.NAMES[axis])
                         .append("</axis>\n")
          .append(spaces).append(nodeTest.startElement())
          .append(nodeTest.xmlBody(indent + 2))
          .append(spaces).append(nodeTest.endElement());
    
        for (int i = 0; i < predicates.size(); i++) {
            Expr e = (Expr) predicates.get(i);
            sb.append(spaces).append(e.startElement())
              .append(e.xmlBody(indent + 2))
              .append(spaces).append(e.endElement());
        }
        return sb.toString();
    }
}
