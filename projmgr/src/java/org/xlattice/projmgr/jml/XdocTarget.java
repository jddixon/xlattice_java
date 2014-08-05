/* XdocTarget.java */
package org.xlattice.projmgr.jml;

import java.util.Stack;

/**
 * @author Jim Dixon
 */

// XXX NAIL THIS DOWN LATER
import org.xlattice.corexml.om.*;

/**
 */
public class XdocTarget extends OmMaker {

    protected final Element  docElm;
    
    protected final Element  propElm;       // below doc
    protected Element title;                // below properties
    protected Element author;               // below properties
    
    protected final Element  bodyElm;       // below doc
   
    // MOVE TO SUPERCLASS ??
    protected Element curElm;
    protected NodeList curNodes;
    protected Stack elmStack = new Stack();

    // CONSTRUCTOR //////////////////////////////////////////////////
    /** 
     * @param doc Document used to return results
     */
    public XdocTarget(Document doc) {
        super(doc);
        docElm     = new Element("document");
        doc.getNodeList().append(docElm);
        doc.setElementNode(docElm);         // XXX
      
        propElm    = new Element("properties");
        bodyElm    = new Element("body");
        curNodes   = docElm.getNodeList().append(propElm);
        pushCurrent (bodyElm);
    }
    // UTILITY METHODS ////////////////////////////////////

    // move to superclass?
    protected void pushCurrent (Element elm) {
        curNodes.append(elm);
        curElm = elm;
        elmStack.push(curElm);
        curNodes = curElm.getNodeList();
        // DEBUG
        //System.out.println("pushed " + curElm.getName());
        if (elm != (Element)elmStack.peek())
            throw new IllegalStateException("pushed " + elm.getName()
                    + " but " + ((Element)elmStack.peek()).getName() 
                    + " on top of stack");
        // END
    }
    protected void pushCurrent (String tag) {
        pushCurrent(new Element(tag));
    }
    protected void popCurrent() {
        // DEBUG
        //Element elm = (Element)
        // END
        elmStack.pop();
        // DEBUG
        //System.out.println("popped " + elm.getName());
        // END
        curElm   = (Element) elmStack.peek();
        curNodes = curElm.getNodeList();
    }
    protected void popCurrentExpecting (String tag) {
        Element elm = (Element) elmStack.pop();
        // DEBUG
        //System.out.println("popped " + elm.getName());
        // END
        if (curElm != elm) 
            throw new IllegalStateException("curElm is " + curElm.getName() 
                    + " but tag on stack top was " + elm.getName());
        if (!elm.getName().equals(tag)) 
            throw new IllegalStateException("tag on popped Element is "
                    + elm.getName() + " but should be " + tag);
        if (elmStack.size() > 0) {
            curElm = (Element) elmStack.peek();
            curNodes = curElm.getNodeList();
        } else {
            curElm = null;
            curNodes = null;
        }
    }
    protected void popIfP() {
        if (curElm.getName().equals("p")) 
            popCurrent();
    }
    protected void pushUnlessAlreadyP() {
        String name = curElm.getName();
        // XXX will eventually need to deal with th
        if (!(name.equals("p") || name.equals("li") 
                               || name.equals("td") ))
            pushCurrent("p");
    }
    public void wrap (String tag, String text) {
        Element elm = new Element(tag);
        elm.getNodeList().append(new Text(text));
        curNodes.append(elm);
    }
    // DOCUMENT ///////////////////////////////////////////
    public void author(String name, String email) {
        Element elm = new Element("author").addAttr("email", email);
        elm.getNodeList().append(new Text(name));
        propElm.getNodeList().append(elm);
    }
        
    public void begin() { /* ignore */ }
    public void end() { 
        popIfP();
        popCurrentExpecting("body");
    }
    
    public void title  (String s) {
        Element title = new Element("title");
        title.getNodeList().append(new Text(s));
        propElm.getNodeList().append(title);
    }
    
    // LINE BREAKS ////////////////////////////////////////
    public void blankline() {
        popIfP();
        pushCurrent("p");
    }
    public void linebreak() {
        curNodes.append(new Element("br"));
    }
    public void paragraph() {
        popIfP();
        pushCurrent("p");
    }
    // SIMPLE TEXT OUTPUT /////////////////////////////////
    public void text      (String s)         {
        pushUnlessAlreadyP();
        curNodes.append(new Text(s));
    }
    public void rawOutput  (String s)         {
        popIfP();
        text(s);
    }
    // TEXT REFORMATTING //////////////////////////////////
    public void blockquote (String s) {
        popIfP();
        wrap("blockquote", s);
    }
    public void bold   (String s) {
        pushUnlessAlreadyP();
        wrap("b", s);
    }
    /**
     * Xdoc 'source' wraps text with a &lt;pre&gt;; this 
     * implementation also makes it CDATA.  output appears in
     * a box.`
     *
     * XXX A blank line after the section heading injects 
     * XXX <p>\n</p> into the output.
     */
    public void code   (String s) {
        popIfP();
        wrap("source", 
            new StringBuffer("<![CDATA[").append(s.trim()).append("]]>")
                .toString());
    }
    public void italic (String s) {
        pushUnlessAlreadyP();
        wrap("i", s);
    }
    public void tt     (String s) {
        pushUnlessAlreadyP();
        wrap("tt", s);
    }
    // LINKS //////////////////////////
    public void link   (String url) {
        link (url, null);
    }
    public void link   (String url, String tag) {
        pushUnlessAlreadyP();
        if (tag == null) {
            tag = url;
        } 
        // scanner does not trim tag
        String tag_ = tag.trim();
        Element elm = new Element("a").addAttr("href", url);
        elm.getNodeList().append(new Text(tag_));
        curNodes.append(elm);
    }
    // LISTS ////////////////////////////////////////////////////////
    public void beginOL (int n) {
        popIfP();
        pushCurrent("ol");
    }
    public void endOL (int n) {
        popCurrentExpecting("li");
        popCurrentExpecting("ol");
    }
    public void beginUL (int n) {
        popIfP();
        pushCurrent("ul");
    }
    public void endUL (int n) { 
        popCurrentExpecting("li");
        popCurrentExpecting("ul");
    }
    public void beginListItem(int n) {
        pushCurrent("li");
    }
    public void endListItem(int n) {
        popCurrentExpecting("li");
    }
    // SECTIONS ///////////////////////////////////////////
    public void beginSection (String title, int n) {
        Element elm;
        popIfP();
        if (n == 1)
            elm = new Element("section");
        else if (n == 2)
            elm = new Element("subsection");
        else
            throw new IllegalStateException("section level "
                    + n + " out of range");
        elm.addAttr("name", title);
        pushCurrent(elm);
    }
    public void endSection (int n) {
        popIfP();
        if (n == 1)
            popCurrentExpecting ("section");
        else if (n == 2)
            popCurrentExpecting ("subsection");
        else
            throw new IllegalStateException("section level "
                    + n + " out of range");
    }
    public void beginSectionList (int n) { /* ignore */ }
    public void endSectionList (int n)   { /* ignore */ }

    // TABLES /////////////////////////////////////////////
    public void beginTable() {
        popIfP();
        pushCurrent("table");
        pushCurrent("tr");
        pushCurrent("td");
    }
    public void endTable() {
        popCurrentExpecting("td");
        popCurrentExpecting("tr");
        popCurrentExpecting("table");
    }
    public void  col() {
        popCurrentExpecting("td");
        pushCurrent("td");
    }
    public void  row() {
        popCurrentExpecting("td");
        popCurrentExpecting("tr");
        pushCurrent("tr");
        pushCurrent("td");
    }
}
