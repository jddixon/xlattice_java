/* AttrList.java */
package org.xlattice.corexml.om;

import java.util.ArrayList;

/**
 * A container for attributes.  The order of attributes in the
 * container is not significant and is not guaranteed to be 
 * repeatable.
 * 
 * @author Jim Dixon
 */
public class AttrList {

    private ArrayList attrs;
    private Element   holder;
   
    /**
     * Create the container, specifying a size.
     *
     * @param sizeHint preferred size
     */
    AttrList (int sizeHint) {
        attrs = new ArrayList (sizeHint);
    }
    /**
     * Create the container that will hold one attribute, initializing 
     * it to that size.
     *
     * @param attr the attribute held
     */
    AttrList (Attr attr) {
        this(1);
        attrs.add(attr);
    }
    /**
     * Create a container with a default size of 8.
     */
    AttrList() {
        this(8);
    }
    /**
     * Add an attribute to an existing container.
     * 
     * @param  attr the attribute to be inserted
     * @return a reference to this list, to allow chaining
     * @throws NullPointerException if the Attr argument is null
     */
    public AttrList add (Attr attr) {
        attr.setHolder(holder);
        attrs.add(attr);
        return this;
    }
    /**
     * Insert an attribute into an existing container in a particular
     * place, displacing any existing attributes if necessary.
     * 
     * @param n    zero-based index at which the Attr is to be inserted
     * @param attr the attribute to be inserted
     * @return a reference to this list, to allow chaining
     * @throws IndexOutOfBoundsException if n is negative or out of range
     * @throws NullPointerException if the Attr argument is null
     */
    public AttrList insert (int n, Attr attr) {
        attr.setHolder(holder);
        attrs.add(n, attr);
        return this;
    }
    /**
     * Get the Nth attribute.
     * 
     * @param n index of the Attr to be returned
     * @return the Nth attr in the list 
     * @throws IndexOutOfBoundsException 
     */
    public Attr get(int n) {
        return (Attr) attrs.get(n);
    }
	
    /**
     * @return number of attrs in the list
     */
    public int size () {
        return attrs.size();
    }

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the Element that the attribute belongs to */
    public Holder getHolder () {
        return holder;
    }
    /**
     * Set the Holder for this attribute.  By definition the Holder
     * must be an XML Element.
     * 
     * @param h the Holder being assigned
     */
    public void setHolder(Element h) {
        holder = h;
        for (int i = 0; i < attrs.size(); i++) 
            ((Attr)attrs.get(i)).setHolder(h);
    }
    // VISITOR-RELATED///////////////////////////////////////////////
    /**
     * Walk a Visitor through the list of attributes, visiting each
     * in turn.
     * @param v the visitor
     */
    public void walkAll (Visitor v) {
        for (int i = 0; i < attrs.size(); i++) {
            ((Attr)attrs.get(i)).walkAll(v);
        }
    } 
    // SERIALIZATION ////////////////////////////////////////////////
    /** @return the list in XML String form */
    public String toXml() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < attrs.size(); i++)
            sb.append(((Attr)attrs.get(i)).toXml());
        return sb.toString();
    }
}
