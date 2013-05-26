/* TermParser.java */
package org.xlattice.util.context;

import java.text.ParseException;
import org.xlattice.Context;

/**
 * Parse a string or substring, returning a sequence of Terms,
 * where a Term is either a Literal (which cannot be parsed 
 * further) or a Symbol, something enclosed in braces (which
 * can be parsed further).
 *
 * @author Jim Dixon
 */
public class TermParser {

    private final String text;
    private final int len;
    private int startsAt;
    private int curPos;
    private int endsAt;
    private final boolean inSymbol;

    /**
     * Creates the base parser for a string.  The parse begins at the
     * first character of the string.
     * 
     * throws NullPointerException if argument is null
     */
    public TermParser (String s) {
        this (s, 0, false);
    }
    /**
     * Creates a possibly nested parser for a string.  The parser
     * is 'nested' if we are parsing a substring within a symbol.
     * 
     * @param s     the string being parsed
     * @param from  offset of the first character being parsed
     * @param inSym whether this is a substring within a symbol
     */
    public TermParser (String s, int from, boolean inSym) {
        text     = s;
        len      = s.length();
        startsAt = from;
        curPos   = startsAt;
        inSymbol = inSym;

        endsAt   = len;
    }
    /**
     * @return the next Term parsed out of the string.
     */
    public Term next()                      throws ParseException{
        
        // BEYOND END OF TXT? /////////////////////////////
        if (curPos >= endsAt) { 
            return null;
        }
        
        int symStart = text.indexOf("${", curPos);
        int symEnd   = text.indexOf("}",  curPos);
       
        if (curPos == symEnd && inSymbol) {
            endsAt = symEnd + 1;
            return null;
        }

        if (symStart < 0) {
            // NO SYMBOL STARTER FOUND ////////////////////////
            int litStart = curPos;

            if (inSymbol) {
                // IN SYMBOL //////////////////////////////
                if (symEnd < 0) {
                    throw new ParseException("missing closing brace", curPos);
                }
                curPos = symEnd;    // points to the }
                if (litStart == symEnd) {
                    return null;
                } else {       
                    Literal lit = new Literal(text, litStart, symEnd);
                    return lit;
                } 
            } else {
                // NO SYMBOL STARTER, NOT IN SYMBOL
                curPos = endsAt;
                Literal lit = new Literal (text, litStart, curPos);
                return lit;
            }
        }
        else {
            // SYMBOL STARTER WAS FOUND ///////////////////
            if (symEnd < symStart) {
                // but symbol ends before that
                int from = curPos;
                curPos = symEnd;
                Literal lit = new Literal(text, from, symEnd);
                return lit;
            } else { 
                if (curPos < symStart) {
                    // TEXT BEFORE ${ /////////////////////////
                    int from = curPos;
                    curPos   = symStart;
                    return new Literal (text, from, symStart);
                } else {
                    // NO TEXT BEFORE ${ //////////////////////
            
                    Term t = new Symbol(text, curPos, true);
                    curPos = t.to();    // is the RBRACE handled??
                    return t;
                }
            }
        }
    }
    public String toString() {
        StringBuffer sb = new StringBuffer("TermParser[")
            .append(startsAt).append(":").append(endsAt)
            .append("] \"") .append(text).append("\"")
            // FORMATTED FOR DEBUGGING:
            .append("\n\t\t\t")
            // END DEBUG FORMATTER
            .append("curPos = ").append(curPos)
            .append(" inSymbol = ").append(inSymbol);
        return sb.toString();
    }
}
