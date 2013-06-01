/* NodeSet.java */
package org.xlattice.corexml.expr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.xlattice.corexml.om.Node;

/**
 * A container for unique Nodes.  No ordering is guaranteed on the
 * set.
 * <p/>
 * NodeSets are one of the fundamental XPath data types.  Typically
 * they represent the set of XML nodes which match a query.
 * <p/>
 * This implementation is not synchronized.
 *
 * @author Jim Dixon
 */
public class NodeSet extends Expr {

    public final static NodeSet EMPTY = new NodeSet(0);
    
    public final static int DEFAULT_NODESET_SIZE = 8;
    
    /** the set of nodes */
    private HashSet nodes;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /** 
     * Create a node set with a default size.
     */
    public NodeSet () {
        this(DEFAULT_NODESET_SIZE);
    }
    /** Create a node set, recommending an initial set size. */
    public NodeSet (int sizeHint) {
        super("nodeset");
        nodes = new HashSet (sizeHint);
    }
    /** 
     * Create a node set with an initial set size of one and a 
     * single member.
     * @param node the initial member 
     */
    public NodeSet (Node node) {
        this(1);
        nodes.add(node);
    }
    /**
     * Create a node set, setting its initial size to match that
     * of another set and copying that node set's members.
     */
    public NodeSet (NodeSet other) {
        this(other.size());
        Iterator it = other.iterator();
        while (it.hasNext())
            nodes.add((Node)it.next());
    }
    // METHODS //////////////////////////////////////////////////////
    /**
     * @param node the node to be added to the set
     * @return a reference to this list, to allow chaining
     * @throws NullPointerException if the Node argument is null
     */
    public NodeSet add (Node node) {
        if (node == null)
            throw new NullPointerException("null node");
        nodes.add(node);
        return this;
    } 
    /** 
     * Add any Nodes in the argument NodeSet to this NodeSet if 
     * not already present. 
     *
     * @param n   NodeSet whose nodes are to be added
     * @return    a reference to this NodeSet
     * @throws    NullPointerException if the parameter is null
     */
    public NodeSet add (NodeSet n) {
        Iterator it = n.iterator();
        while ( it.hasNext() ) 
            nodes.add( it.next() );
        return this;
    }
    /**
     * Remove any nodes in this set.
     */
    public void clear() {
        nodes.clear();
    }
    /** @return whether this set contains a specific node */
    public boolean contains(Node node) {
        if (node == null)
            return false;
        else 
            return nodes.contains(node);
    }
    /** @return an iterator over the set */
    public Iterator iterator() {
        return nodes.iterator();
    }
    /** @return whether the set is empty */
    public boolean isEmpty() {
        return nodes.isEmpty();
    } 
    /**
     * Copy the nodes from another NodeSet into this one, then
     * delete them from the source, to ease GC.
     *
     * @throws NullPointerException if otherSet is null
     */
    public NodeSet moveFrom (NodeSet otherSet) {
        if (otherSet == null)
            throw new NullPointerException("null copy set");
        if (!otherSet.isEmpty()) {
            Iterator it = otherSet.iterator();
            while (it.hasNext())
                nodes.add((Node)it.next());
            otherSet.clear();
        }
        return this;   
    }
    /**
     * Remove a node from this NodeSet.
     * @param node the node to be removed
     * @return     true if the node was present (and so has been removed)
     */
    public boolean remove (Node node) {
        if (node == null)
            return false;
        else
            return nodes.remove(node);
    }
    /**
     * @return number of nodes in the list
     */
    public int size () {
        return nodes.size();
    }
    /** 
     * Remove any Nodes in the argument NodeSet from this NodeSet if 
     * they are present.  The argument NodeSet is unaffected by this
     * operation.
     *
     * @param n   NodeSet whose nodes are to be added
     * @return    a reference to this NodeSet
     * @throws    NullPointerException if the parameter is null
     */
    public NodeSet subtract (NodeSet n) {
        Iterator it = n.iterator();
        while ( it.hasNext() ) 
            nodes.remove( it.next() );
        return this;
    }
    // EQUALS, HASHCODE /////////////////////////////////////////////
    /**
     * A strong equality check.  Returns true iff the other object
     * is the same object or a NodeSet with identical elements.
     *
     * @return whether the object is identical to this NodeSet 
     */
    public boolean equals (Object o) {
        if (o == null)
            return false;
        if (!(o instanceof NodeSet))
            return false;
        NodeSet other = (NodeSet) o;
        if (other == this)
            return true;
        if (other.size() != nodes.size())
            return false;
        Iterator it = other.iterator();
        // XXX THIS IS TOO STRONG - it requires that member nodes be
        // identical, not just equal to one another.  Need a 
        // Node.equals() method.
        while (it.hasNext()) 
            if (!nodes.contains((Node)it.next()))
                return false;
        return true;
    }
    // XXX NEED hashCode() //////////////////////////////////////////

    // SERIALIZATION ////////////////////////////////////////////////
    /**
     * Return the NodeSet in a form suitable for inclusion in XML 
     * dumps.  This is recursive, with two additional spaces of 
     * indentation at each level.
     *
     * @param indent the number of spaces indented so far
     */
    public String xmlBody(int indent) {
        StringBuffer sb = new StringBuffer();
        String spaces = SpaceFactory.getInstance().makeSpaces(indent);

        Iterator it = nodes.iterator();
        while (it.hasNext()){
            Node node = (Node) it.next();
            // XXX NEED TO HAVE NODE FOLLOW expr INTERFACE
//          sb.append(spaces).append(node.startElement())
//            .append(node.xmlBody(indent + 2))
//            .append(spaces).append(node.endElement());
            sb.append(node.toXml());
        }
        return sb.toString();
    }
}
