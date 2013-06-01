/* Holder.java */
package org.xlattice.corexml.om;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import org.xlattice.corexml.CoreXmlException;

/**
 * A Holder is something which can have children and namespaces, so a 
 * Document or an Element.
 *
 * @author Jim Dixon
 */
public abstract class Holder extends Node {

    /** every Holder has a list of child Nodes */
    protected NodeList nodes; 
    protected ArrayList nsUris;
    /** maps namespaces into prefixes */
    protected HashMap ns2pf;
    /** reverse mapping, prefixes into namespaces */
    protected HashMap pf2ns;
    /**
     * The Holder-child dependency forms a directed graph.  This
     * looks for cycles in that graph. 
     */
    public final CycleChecker cycleChecker;
    /**
      * Runs down the subtrees below the Holder, setting each 
      * Node's Document to match the Holder's.
      */
    public final DocSetter    docSetter;
    /**
     * Create a Holder, associated data structures, and a pair of
     * Visitors.
     */
    public Holder () {
        super ();
        nodes  = new NodeList();   // might want to be lazy 
        nodes.setHolder(this);
        
        // SILLY OVERKILL //
        ns2pf  = new HashMap();     // namespace --> prefix map
        pf2ns  = new HashMap();     // prefix --> namespace map
        nsUris = new ArrayList();
        cycleChecker = new CycleChecker();
        docSetter    = new DocSetter();
    }

    /**
     * Add a prefix-namespace pair, updating the maps.
     * 
     * XXX SHOULD BE MOVED TO Element XXX
     * @param prefix    the prefix, a NCNAME, may not be null
     * @param namespace XML-compatible namespace
     */
    public void addNamespace (String prefix, String namespace) {
        // XXX NEED MORE REASONABLE CHECKS 
        if (namespace == null)
            throw new NullPointerException("null namespace");
        ns2pf.put (namespace, prefix);
        if (prefix != null)
            pf2ns.put (prefix, namespace);
        nsUris.add(namespace);      // SILLY LEVEL OF OVERKILL
    } 
//  public void addNamespace (String namespace) {
//      this.addNamespace(null, namespace);
//  }


    // PROPERTIES ///////////////////////////////////////////////////
    /** @return a reference to the list of children of this Holder */
    public NodeList getNodeList() {
        return nodes;
    }
    /** 
     * Set this Holder's ultimate parent, the Document it belongs
     * to.
     */
    public void setDocument (Document newDoc) {
        if (this instanceof Document)
            throw new IllegalStateException(
                    "attempt to set a Document's document");
        doc = newDoc;                   // may be null
        if (this instanceof Element)
            ((Element)this).getAttrList().walkAll(docSetter);
        nodes.walkAll (docSetter);      // set in subtree
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /** 
     * Add a child Node to the Holder.
     *
     * @param elm  child Node to be added
     * @return     reference to this Holder, for convenience in chaining
     * @throws     NullPointerException if the child is null
     */
    public Holder addChild (Node elm) {
        nodes.append(elm);
        return this;
    }
    // VISITOR-RELATED///////////////////////////////////////////////
    /**
     * Walk down the subtrees, child nodes and their descendents,
     * looking for this node; if found, there is a cycle in the graph.
     */
    public class CycleChecker implements Visitor {
        public CycleChecker() {}
        /** On arriving at the node, do the identity check. */
        public void onEntry(Node node)      throws GraphCycleException {
            if (node == Holder.this)
                throw new GraphCycleException();
        }
        /** On leaving, do nothing */
        public void onExit(Node node) { }
    }
    /** 
     * Walk down the subtrees, child nodes and their descendents,
     * setting each node's Document to match this Holder's 
     * Document.
     */
    public class DocSetter implements Visitor {
        public DocSetter () {}
        /** On arriving at the node, set its Document. */
        public void onEntry (Node n) {
            n.setDocument(Holder.this.getDocument());
        }
        /** On leaving, do nothing. */
        public void onExit (Node n) { }
    }
    /**
     * Take a Visitor on that walk down the subtrees, visiting
     * every Node.
     */
    public void walkAll (Visitor v) {
        // DEBUG
//      System.out.println("Holder.walkall()");
        // END
        v.onEntry(this);
        if (this instanceof Element)
            ((Element)this).walkAttrs(v);
        nodes.walkAll(v);
        v.onExit(this);
    }
    /**
     * Take a Visitor on that walk down the subtrees, visiting
     * only subnodes which are themselves Holders.
     */
    public void walkHolders (Visitor v) {
        v.onEntry(this);
        nodes.walkHolders(v);
        v.onExit(this);
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Arrive here having seen either START_DOCUMENT or START_TAG and
     * having created a Node of the appropriate type.
     */
    void populator (XmlPullParser xpp, final int depth, final int endEvent) 
                throws CoreXmlException, IOException, XmlPullParserException {
        if (!nodes.isEmpty()) 
            throw new IllegalStateException("NodeList is not empty");
        int elementCount = 0;
        int event;
       
        // COLLECT ANY NAME SPACES ////////////////////////
        int myDepth = xpp.getDepth();
        int nsPrev  = myDepth <= 0 ? 0 : xpp.getNamespaceCount(myDepth - 1);
        int nsNow   = xpp.getNamespaceCount(myDepth);
        nsUris = new ArrayList (nsNow - nsPrev);  // XXX CHECK ME
        
        for (int i = nsPrev; i < nsNow; i++) {
            String prefix = xpp.getNamespacePrefix(i);
            String uri    = xpp.getNamespaceUri(i);
            addNamespace (prefix, uri);
            // DEBUG
            //System.out.println("namespace " + i + ", " + prefix + ":" + uri); 
            // END
        }
        // COLLECT ATTRIBUTES /////////////////////////////
        if (isElement()) {
            int count = xpp.getAttributeCount();
            Element me = (Element)this;
            for (int i = 0; i < count; i++) {
                // IGNORE TYPE FOR NOW
                // IGNORE ATTR NAMESPACE
                me.addAttr(xpp.getAttributePrefix(i),
                            xpp.getAttributeName(i), xpp.getAttributeValue(i));
            }
        }
        // COLLECT CHILDREN ///////////////////////////////
        // detect empty document
        try {
            event = xpp.nextToken();
        } catch (IOException ioe) {
            return;
        }
        for ( /* empty document detection did nextToken() */; 
                event != XmlPullParser.END_DOCUMENT && event != endEvent;
                        event = xpp.nextToken()) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if (isDocument() && elementCount > 0)
                        throw new CoreXmlException(
                                "more than one root element found");
                    elementCount++;
                    Element elm = new Element(xpp.getName());
                    elm.populator(xpp, depth + 1, XmlPullParser.END_TAG);
                    nodes.append(elm);
                    if (isDocument()) {
                        Document me = (Document) this;
                        if (me.getElementNode() == null)
                            me.setElementNode(elm);
                        else 
                            throw new IllegalStateException (
                                "second element at root level in document");
                    }
                    break;
                case XmlPullParser.IGNORABLE_WHITESPACE:
                case XmlPullParser.TEXT:
                    nodes.append( new Text(xpp.getText()));
                    break;
                case XmlPullParser.COMMENT:
                    nodes.append( new Comment(xpp.getText()));
                    break;
                case XmlPullParser.CDSECT:
                    nodes.append( new Cdata(xpp.getText()));
                    break;
                case XmlPullParser.PROCESSING_INSTRUCTION:
                    nodes.append( new ProcessingInstruction (xpp.getText() ));
                    break;              
                
                // //////////////////////////////////////////////////
                // THESE ARE NOT YET HANDLED ////////////////////////
                // //////////////////////////////////////////////////
                case XmlPullParser.DOCDECL:
                    // DEBUG
                    System.out.println("    *** IGNORING DOCDECL TOKEN ***");
                    // END
                    break;
                case XmlPullParser.ENTITY_REF:
                    // DEBUG
                    System.out.println("    *** IGNORING ENTITY_REF TOKEN ***");
                    // END
                    break;
                default:
                    throw new CoreXmlException(
                        "unknown event type " + event);
            } 
        }
    }
}

