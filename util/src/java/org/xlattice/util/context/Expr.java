/* Expr.java */
package org.xlattice.util.context;

import java.text.ParseException;
import java.util.ArrayList;
import org.xlattice.Context;

/**
 * This class converts a substring into a sequence of Terms. 
 *
 * @author Jim Dixon
 */
public class Expr {

    private ArrayList terms;
  
    private final String text;
    private final int len;          // of text
    private final int startsAt;
    private int endsAt;
    private final boolean inSymbol;
 
    /**
     * Create an expression, a series of Terms, from a substring.
     * 
     * @param s string being resolved
     * @param curPos offset of first character
     * @param inSym  true if this expression is contained within a symbol
     */
    public Expr(String s, int curPos, boolean inSym) 
                                            throws ParseException {
        if (s == null) 
            throw new NullPointerException();
        text       = s;
        len        = s.length();
        startsAt   = curPos;
        endsAt     = -1;    // IF YOU SEE THIS, IT MARKS AN ERROR
        inSymbol   = inSym;
        
        terms = new ArrayList(); 
      
        TermParser p = new TermParser(s, curPos,  inSymbol);
        for (Term t = p.next(); t != null; t = p.next()) {
            terms.add(t);
            endsAt = t.to();
        }
    }
    /**
     * Converts an entire string into an expression.
     */
    public Expr(String s)                   throws ParseException {
        this (s, 0, false);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /**
     * Add a term to the expression.
     * 
     * @param t the term to be added
     * @return  a reference to this Expr; supports chaining.
     */
    public Expr add(Term t) {
        if (t == null)
            throw new NullPointerException();
        terms.add(t);
        return this;
    }
    /** @return the Nth term in the expression */
    public Term get(int n) {
        if (n < 0 || n >= terms.size())
            throw new IllegalStateException("index range error");
        return (Term) terms.get(n);
    }
    /** @return the number of terms in the expression */
    public int size() {
        return terms.size();
    }
    /** @return offset of the first character in the expression */
    public int from() {
        return startsAt;
    }
    /** @return offset of the character just beyond the end */
    public int to() {
        return endsAt;
    }
    // IMPLEMENTATION ///////////////////////////////////////////////
    /** 
     * Given a context, reduce this expression to a string by 
     * concatenating its Terms, first replacing any Symbol with
     * the value it binds to in the context.
     */
    public String resolve (Context ctx) {
        if (ctx == null) 
            throw new NullPointerException("null context");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < terms.size(); i++) {
            sb.append(((Term)terms.get(i)).resolve(ctx));
        }
        return sb.toString();
    }
    /** @return the expression in a form suitable for debugging */
    public String toString() {
        StringBuffer sb = new StringBuffer()
            .append("Expr[").append(startsAt).append(":")
            .append(endsAt).append("] \"").append(text).append("\"");
        return sb.toString();
    }
}
