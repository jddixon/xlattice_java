/* NodeList.java */
package org.xlattice.corexml.om;

import java.util.ArrayList;

/**
 * A container for Nodes.  Each Holder (Document or Element) has a 
 * NodeList, but the reverse is not necessarily true.
 * <p/>
 * XXX NEED TO SIMPLIFY THE HANDLING OF THE ELEMENT NODE IN A 
 * XXX DOCUMENT
 *
 * @author Jim Dixon
 */
public class NodeList {

    public final static int DEFAULT_NODELIST_SIZE = 8;
   
    /** list of child nodes */
    private ArrayList nodes;
    /** immediate parent, might be null */
    private Holder    holder;
    /** ultimate parent, might be null*/
    private Document  doc;

    /** 
     * Create a node list with a default size.
     */
    public NodeList () {
        this(DEFAULT_NODELIST_SIZE);
    }
    /**
     * Create a node list suggesting an initial size. 
     */
    public NodeList (int sizeHint) {
        nodes = new ArrayList (sizeHint);
    }
    /**
     * Create a list of nodes, initially with only one member.
     *
     * @param node the first element to be added to the list
     */
    public NodeList (Node node) {
        this(1);
        nodes.add(node);
    }
    /**
     * Add a Node to the NodeList.
     * 
     * XXX Should check for cycles; if the Holder is a document,
     * XXX there may be only one Element node.
     *
     * @param node the node to be appended
     * @return a reference to this list, to allow chaining
     * @throws NullPointerException if the Node argument is null
     */
    public NodeList append (Node node) {
        node.setHolder(holder);
        nodes.add(node);
        return this;
    }
    /**
     * Copy the nodes from another NodeList into this one, then
     * delete them from the source, to ease GC.
     *
     * @throws NullPointerException if otherList is null
     */
    public NodeList moveFrom (NodeList otherList) {
        for (int i = 0; i < otherList.size(); i++) {
            Node n = otherList.get(i);
            n.setHolder(holder);
            nodes.add(n);
        }
        otherList.clear();
        return this;   
    }
    public void clear() {
        nodes.clear();
    }
    /**
     * @param n    zero-based index at which the Node is to be inserted
     * @param node the node to be inserted
     * @return a reference to this list, to allow chaining
     * @throws IndexOutOfBoundsException if n is negative or out of range
     * @throws NullPointerException if the Node argument is null
     */
    public NodeList insert (int n, Node node) {
        node.setHolder(holder);
        nodes.add(n, node);
        return this;
    }
    /* @return whether there are no nodes in the list */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }
    /**
     * Return the Nth node in the list.
     * 
     * @param n index of the Node to be returned
     * @return the Nth node in the list 
     * @throws IndexOutOfBoundsException 
     */
    public Node get(int n) {
        return (Node) nodes.get(n);
    }

    /**
     * @return number of nodes in the list
     */
    public int size () {
        return nodes.size();
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the immediate parent of this list */
    public Holder getHolder() {
        return holder;
    }
    /** 
     * Change the immediate parent of this list, here and in 
     * descendent nodes.
     *
     * XXX SHOULD CHECK FOR GRAPH CYCLES
     *
     * @param h the new parent; may be null
     */
    public void setHolder(Holder h) {
        holder = h;                     // null is acceptable
        if (holder == null)
            doc = null;
        else 
            doc = holder.getDocument();
        for (int i = 0; i < nodes.size(); i++) {
            ((Node)nodes.get(i)).setHolder(h);
        }
    }
    // VISITOR-RELATED///////////////////////////////////////////////
    /** Take the visitor through every node in the list, recursing. */
    public void walkAll (Visitor v) {
        for (int i = 0; i < nodes.size(); i++) {
            ((Node)nodes.get(i)).walkAll(v);
        }
    }
    /** 
     * Take the Visitor through the list, visiting any node which is
     * a Holder, recursively.  Used when you don't want to visit, for
     * example, attributes.
     */
    public void walkHolders (Visitor v) {
        for (int i = 0; i < nodes.size(); i++) {
            Node n = (Node) nodes.get(i);
            if (n instanceof Holder)
                ((Holder)n).walkHolders(v);
        }
    }
    // SERIALIZATION METHODS ////////////////////////////////////////
    /** 
     * A String containing each of the Nodes in XML form, recursively,
     * without indenting.
     */
    public String toXml() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nodes.size(); i ++)
            sb.append ( ((Node)nodes.get(i)).toXml() );
        return sb.toString();
    }
}
