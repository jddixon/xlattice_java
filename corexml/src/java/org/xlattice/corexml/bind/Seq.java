/* Seq.java */
package org.xlattice.corexml.bind;

import java.util.ArrayList;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Element;
import org.xlattice.corexml.om.Node;
import org.xlattice.corexml.om.NodeList;
import org.xlattice.corexml.om.Text;

/**
 * Represents a set of bindings (Joins) to be applied in serial order 
 * (in sequence).  Each Join is between 
 * <ul>
 *   <li> an XML attribute or element</li>
 *   <li> and a field in an object</li>
 * </ul>
 * 
 * The translation can be run in either direction.  An <code>apply</code>
 * call will run down the list of Joins, using each in turn to extract
 * values from XML and then either assign values to object fields or 
 * create instances of Java objects.  This is done recursively, 
 * potentially creating a tree of such objects.  A <code>generate</code>
 * call has the opposite effect: it applies the Joins in sequence to 
 * a Java object and any children, extracting values which are assigned
 * to XML nodes or used to create such nodes.
 * 
 * @author Jim Dixon
 */
public class Seq extends Ordering {
    /** the sequence of Joins */
    private final ArrayList joins;

    // CONSTRUCTOR //////////////////////////////////////////////////
    /** 
     * No-arg constructor. 
     * */
    public Seq () {
        joins = new ArrayList();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /** 
     * Return the Nth join in the sequence of bindings.
     * 
     * @param n index of the Join requested
     * @return  the Nth Join in the sequence 
     * @throws  IndexOutOfBoundsException
     */
    protected Join get (int n) {
        return (Join) joins.get(n);
    }
    // INTERFACE ORDERING ///////////////////////////////////////////
    /**
     * Add a join to this sequence.  Joins are applied in exactly the
     * order in which they are added to the Seq.
     *
     * @param j Join being added to the sequence
     */
    protected void add (Join j)                throws CoreXmlException {
        joins.add(j);
    }
    /**
     * Set the parent Class for this and immediate descendents.
     * This call causes join(clazz) calls in all except leaf 
     * descendents, although the clazz being assigned will change
     * as the join() cascades down.
     *
     * @param clazz parent Class being assigned
     */
    protected void join (Class clazz)          throws CoreXmlException {
        setClazz(clazz);
        for (int i = 0; i < joins.size(); i++)
            ((Join)joins.get(i)).join(clazz);
    }
    /** @return the number of Joins in the sequence */
    protected int size() {
        return joins.size();
    }
    /**
     * Skip any text nodes with no content after trimming.
     *
     * XXX BELONGS IN om/NodeList
     *
     * @param nodes list of XML nodes
     * @param k     current index into that list 
     * @return      index of first node not skipped
     */
    public static int skipWhitespaceText(NodeList nodes, int k) {
        Node node_ = nodes.get(k);
        while ( node_ instanceof Text ) {
            String s = ((Text)node_).getText().trim();
            if (s.length() > 0)         // Kth node has real text
                break;
            if (++k >= nodes.size())
                break;
            node_ = nodes.get(k);
        }
        return k;
    }
    /** 
     * Use the ordering to generate a set of values from the
     * children of an Element, recursing if possible.  The
     * values obtained will be used to set fields in the object, 
     * and/or instantiate child objects.
     *
     * @param elm Element whose children values come from
     * @param obj object in which values are assigned
     */
    protected void apply (Element elm, Object obj) throws CoreXmlException {
        Join join;
        NodeList nodes = elm.getNodeList();

        for (int j = 0; j < joins.size(); j++) {
            join = (Join) joins.get(j);
            // XXX WON'T WORK FOR INTERFACES XXX
            String curTag = join.getName();
            int matchCount = 0;         // matches found so far

            for (int k = 0; k < nodes.size(); /* nada */) {
    
                k = skipWhitespaceText(nodes, k);
                if ( k >= nodes.size() ) {
                    if (matchCount < join.getMinOccur())
                        throw new IllegalStateException(
                            "have binding but no more nodes to apply to");
                    else 
                        break;
                }
                Node node = nodes.get(k);
                if (node instanceof Text) {
                    while (!curTag.equals("")) {
                        // we have a Text node, but it doesn't match
                        // the current Join
                        if (matchCount < join.getMinOccur()) {
                            throw new IllegalStateException (
                                "min Join limit " + join.getMinOccur()
                                + " on tag " + curTag
                                + " not met, count is " + matchCount);
                        }
                        if (++j >= joins.size())
                            throw new IllegalStateException (
                                "more nodes to process, but no more Joins");
                        join = (Join) joins.get(j);
                        curTag = join.getName();
                        matchCount = 0;
                    } 
                    join.apply (node, obj);
                    k++;    // used up a node
                    if (++j >= joins.size())
                        break;
                    join = (Join) joins.get(j);
                    curTag = join.getName();
                    matchCount = 0;
                } else {
                    Element subElm = (Element) node;
                    // we have an Element node; its tag has to match the
                    // tag on the Join
                    // while (! curTag.equals(subElm.getName())) {
                    while (! join.tagMatch(subElm.getName()) ) {
                        // we have an Element node, but it doesn't match
                        // the current Join
                        if (matchCount < join.getMinOccur()) {
                            throw new IllegalStateException (
                                join.getName() +
                                ": min Join limit " + join.getMinOccur()
                                + " not met, count is " + matchCount);
                        }
                        if (++j >= joins.size())
                            throw new IllegalStateException (
                                "more nodes to process, but no more Joins");
                        join = (Join) joins.get(j);
                        curTag = join.getName();
                        matchCount = 0;
                    } 
                    //  vv
                    if (++matchCount > join.getMaxOccur())
                        throw new IllegalStateException (
                            "max Join limit " + join.getMaxOccur()
                            + " exceeded, count is " + matchCount);
                    join.apply(subElm, obj);
                    k++;    // used this node
                }
            } 
        }
    }
    /** 
     * Use the ordering to create XML nodes.  The ordering is used
     * to draw values from the object and, if they are present, from
     * children of the object.  These values are used to set attributes
     * in the Element, subelement values, and so on recursively.
     *
     * @param elm the Element the generated XML gets hung from
     * @param o   the object values are drawn from
     */
    protected void generate (Element elm, Object o)  throws CoreXmlException {
        int j;
        Join join;
        for (j = 0; j < joins.size(); j++) 
            ((Join)joins.get(j)).generate (elm, o);
    }
}
    
