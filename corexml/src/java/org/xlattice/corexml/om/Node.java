/* Node.java */
package org.xlattice.corexml.om;

import org.xlattice.Context;
import org.xlattice.corexml.expr.Numeric;

/**
 * Superclass for all XML nodes in XLattice's object model.
 *
 * @author Jim Dixon
 */
public abstract class Node {

    /** this node's ultimate parent; may be null */
    protected Document doc;
    /** this node's immediate parent; may be null */
    private Holder   holder;

    Node() { }

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return this node's ultimate parent, the XML document */
    public Document getDocument() {
        return doc;
    }
    /**
     * Change this node's ultimate parent, its Document.
     *
     * This gets overridden in Holder and possibly elsewhere.
     *
     * XXX possibility of introducing cycles or inconsistencies
     *
     * @param newDoc new value assigned; may be null
     */
    public void setDocument(Document newDoc) {
        doc = newDoc;
    }
    /**
     * Get this node's parent.
     *
     * @return a reference to this Node's immediate parent
     */
    public Holder getHolder() {
        return holder;
    }
    /**
     * Set or change this node's immediate parent; also change
     * this node's ultimate parent if necessary.
     *
     * XXX There is no check for inconsistencies with the
     * parent's NodeList, nor against the introduction of cycles
     * into the node graph.
     *
     * @param h a reference to the new parent, may be null
     */
    public void setHolder(Holder h) {
        holder = h; 
        if (holder == null)
            doc = null;
        else if (holder.getDocument() != doc) 
            setDocument (holder.getDocument());
    }

    // VISITOR-RELATED///////////////////////////////////////////////
    /**
     * Runs down the graph counting nodes by type; another Visitor.
     */
    public class NodeCounter implements Visitor {
        private int attrCount;
        private int commentCount;
        private int docCount;
        private int docTypeCount;
        private int elementCount;
        private int piCount;
        private int textCount;
        
        public NodeCounter () { }

        public void onEntry (Node n) {
            if (n.isAttr())                     attrCount++;
            if (n.isComment())                  commentCount++;
            if (n.isDocument())                 docCount++;
            if (n.isDocType())                  docTypeCount++;
            if (n.isElement())                  elementCount++;
            if (n.isProcessingInstruction())    piCount++;
            if (n.isText())                     textCount++;
        }
        public void onExit (Node n) { }
        // PROPERTIES /////////////////////////////////////
        public int attrCount()      { return attrCount; }
        public int commentCount()   { return commentCount; }
        public int docCount()       { return docCount; }
        public int docTypeCount()   { return docTypeCount; }
        public int elementCount()   { return elementCount; }
        public int piCount()        { return piCount; }
        public int textCount()      { return textCount; }
    }

    /**
     * Walk a Visitor through a Node.  This is overridden when 
     * suitable by subclasses.
     */
    public void walkAll (Visitor v) {
        v.onEntry(this);
        // Holders also visit their NodeLists
        v.onExit(this);
    }
//  // EVAL /////////////////////////////////////////////////////////
//  public final Boolean evalAsBoolean(String s) { 
//      // STUB
//      return Boolean.FALSE;
//  }
//  /**
//   * XXX XPath refers to this as evalAsLocation?
//   */
//  public final NodeSet evalAsNodeSet(String s) { 
//      // STUB
//      return null;
//  }
//  public final Numeric evalAsNumeric(String s) { 
//      // STUB
//      return new Numeric(0.0);
//  }
//  public final String evalAsString(String s) { 
//      // STUB
//      return null;
//  }
//  // XXX SHOULD BE NodeSet? XXX
//  public final Boolean evalAsBoolean(Context ctx, String s) { 
//      // STUB
//      return Boolean.FALSE;
//  }
//  public final Node evalAsLocation(Context ctx, String s) { 
//      // STUB
//      return null;
//  }
//  public final Numeric evalAsNumeric(Context ctx, String s) { 
//      // STUB
//      return new Numeric(0.0);
//  }
//  public final String evalAsString(Context ctx, String s) { 
//      // STUB
//      return null;
//  } 
    // TYPE IDENTIFIERS /////////////////////////////////////////////
    /** one of these gets overridden in each subclass */
    public boolean isAttr()     { return false; }
    /** one of these gets overridden in each subclass */
    public boolean isComment()  { return false; }
    /** one of these gets overridden in each subclass */
    public boolean isDocument() { return false; }
    /** one of these gets overridden in each subclass */
    public boolean isDocType()  { return false; }
    /** one of these gets overridden in each subclass */
    public boolean isElement()  { return false; }

    // CDATA subclass of Text
    /** one of these gets overridden in each subclass */
    public boolean isText()     { return false; }
    /** one of these gets overridden in each subclass */
    public boolean isProcessingInstruction () { 
        return false; 
    }
    // SERIALIZATION ////////////////////////////////////////////////
    // this should be changed to follow the same pattern as expr,
    // supporting indenting
    public abstract String toXml();
}
