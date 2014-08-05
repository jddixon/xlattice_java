/* JmlLexer.java */
package org.xlattice.projmgr.jml;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * @author Jim Dixon
 */

import antlr.Token;
import antlr.TokenStreamException;

public class JmlLexer extends JmlScanner {
  
    private int   sectionLevel      = 0;
    // private Stack sectionTypeStack  = new Stack();
    //private int   listLevel         = 0;
    private Stack listTypeStack     = new Stack();
   
    protected static final Hashtable chrTable = new Hashtable();

    // see http://www.w3.org/TR/REC-html40/sgml/entities.html 
    static {
        // arrows
        chrTable.put("larrow",  "&larr;");
        chrTable.put("rarrow",  "&rarr;");
        chrTable.put("implies", "&#8658;"); // right double arrow, rArr

        // mathematics
        chrTable.put("infinity",        "&#8734;");
        chrTable.put("integral",        "&#8747;");
        
        // sets
        chrTable.put("element",         "&#8712;"); // isin
        chrTable.put("subset",          "&#8834;");
        chrTable.put("superset",        "&#8835;");
        chrTable.put("intersection",    "&#8745;");  // cap
        chrTable.put("union",           "&#8746;");  // cup
        
        // all Greek letters are actually available as character entities
        chrTable.put("lambda",          "&lambda;");

        // currencies, all as you might expect
        chrTable.put("euro",            "&euro;");
        chrTable.put("pound",           "&pound;");
        chrTable.put("yen",             "&yen;");

        // editor symbols
        chrTable.put("paragraph",       "&para;");
        chrTable.put("section",         "&sect;");
    }
    public JmlLexer (JmlContext ctx, Reader reader) {
        super(reader, ctx);
    }
    public JmlLexer (Reader reader) {
        super(reader);
    }
    public void interpret(JmlInterpreter interp)
                                throws IOException, TokenStreamException {
        interpreter = interp;
        interpreter.begin();        // if !nested
        Token t;
        for (t = nextToken(); 
                (t != null) && (t.getType() != Token.EOF_TYPE); 
                                                      t = nextToken()) {
            /* nothing to do */
        }
        outputAnyText(); 
        closeLists();
        closeSectionsDownTo(0);     // DEBUG
        interpreter.end();          // unless nested
    }
    
    // PROPERTIES ///////////////////////////////////////////////////
    public int getSectionLevel() {
        return sectionLevel;
    }
    // DOCUMENT /////////////////////////////////////////////////////
    protected void author (String name, String email) {
        interpreter.author(name, email);
    }
    protected void title(String title) {
        interpreter.title(title);
    }
    // LISTS ////////////////////////////////////////////////////////
    private void listAction (String name, int n) {
        int listLevel = listTypeStack.size();
        if (listLevel < n) { 
            listTypeStack.push (name);
            if(name.equals("UL"))
                interpreter.beginUL(n);
            else if (name.equals("OL"))
                interpreter.beginOL(n);
            else 
                throw new IllegalStateException(
                    "invalid list type: " + name);
        } else if (listLevel > n) {
            closeLists(n);
            interpreter.endListItem(n);
        } else {
            interpreter.endListItem(n);
        }
        interpreter.beginListItem(n);
    }
    protected void ul(int n) {
        listAction("UL", n);
    }
    protected void ol(int n) {
        listAction("OL", n);
    }
    /**
     * BRAIN-DEAD VERSION - no checks at all.
     */
    protected void closeList() {
        int level   = listTypeStack.size();
        String type = (String) listTypeStack.pop();
        if (type.equals("UL"))
            interpreter.endUL(level);
        else if (type.equals("OL"))
            interpreter.endOL(level);
    }
    protected void closeLists(int n) {
        for (int level = listTypeStack.size(); level > n; level--)
            closeList();
    }
    protected void closeLists() {
        closeLists(0);
    }
    // SECTIONS /////////////////////////////////////////////////////
    /** 
     * Called by the scanner when it sees the start of a new section.
     * XXX There are currently no checks on whether level transitions 
     * make sense.
     * 
     * @param text  section title
     * @param level single digit section level
     */
    protected void startSection(String title, int n) {
        if (n < 1 || n > 4) 
            throw new IllegalArgumentException(
                    "section number out of range: " + n );
        
        // any open ul/ol lists necessarily end 
        closeLists();
        if (n > sectionLevel) {
            interpreter.beginSectionList(n);
        } else if (n < sectionLevel) {
            closeSectionsDownTo (n);
            interpreter.endSection(n);
        } else {
            interpreter.endSection(n);
        }
        sectionLevel = n;
        interpreter.beginSection(title, n);
    }
    /**
     * Signal the end of each section higher than the new level.
     */
    protected void closeSectionsDownTo (int n) {
        for (int i = sectionLevel; i > n; i--) {
            interpreter.endSection(i);
            interpreter.endSectionList(i);
        }
    }
    // VARIABLES ////////////////////////////////////////////////////
    protected void variable(String name) {
        sb.append(context.lookup(name).toString());
    }
    // WIRED-IN FUNCTIONS ///////////////////////////////////////////
    /**
     * Largely a shorthand for character entities, especially
     * the more badly named.
     */
    public String c(String code) {
        if (code == null || code.length() == 0)
            return "";
        String val = (String) chrTable.get(code);
        if (val == null)
            return "";
        else 
            return val;
    }
    protected void bind(String name, String value) {
        context.bind(name, value);
    }
    protected void eval (String text) {
        /* STUB */
    }
    // OTHER FUNCTIONS //////////////////////////////////////////////
    protected void libcall(String id, Vector args) {
        // DEBUG
        StringBuffer sb = new StringBuffer("libcall: id = ")
            .append(id).append("\n");
        for (int i = 0; i < args.size(); i++) 
            sb.append(i).append("  ").append((String)args.get(i))
              .append("\n");
        System.out.println(sb.toString());
        // END
    }

}
