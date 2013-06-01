/* Symbol.java */
package org.xlattice.corexml.expr;

/**
 * These are a subset of what rule [28] calls ExprToken but with the
 * addition of left and right curly brackets (braces), the colon, the
 * dollar sign, and possibly other symbols.
 * 
 * @author Jim Dixon
 */

public class Symbol implements Token {

    /** index of this Symbol in the table below */    
    private int index;
    
    public final static int LPAREN    =  0;
    public final static int RPAREN    =  1;
    public final static int LBRACKET  =  2;
    public final static int RBRACKET  =  3;
    public final static int LBRACE    =  4;
    public final static int RBRACE    =  5;
    public final static int AT        =  6;
    public final static int COMMA     =  7;
    public final static int DOLLAR    =  8;
    public final static int STAR      =  9;
    public final static int DOT       = 10;
    public final static int COLON     = 11;
    public final static int DDOT      = 12;
    public final static int DCOLON    = 13;

    /** list of the symbols in string form in the same order */
    public final static String [] SYMBOLS = {
        "(",  ")", "[", "]", "{", "}", 
        "@",  ",",  "$", "*", ".",
        ":",  "..", "::" };

    /** those symbols which only appear in one form */
    public final static String SINGLE_CHAR_SYMBOLS  = "()[]{}@,$*";
    /** all 1-char symbols, including those which start other symbols */
    public final static String ALL_ONE_CHAR_SYMBOLS = "()[]{}@,$*.:";

    /** 
     * Create a Symbol from its one-character form.
     * 
     * @throws IllegalStateException if it isn't a valid symbol.
     */
    public Symbol (char c) {
        index = mapChar(c);
    }
    /**
     * Create a Symbol given its String form.
     */
    public Symbol (String sym) {
        if (sym == null) 
            throw new NullPointerException("null symbol");
        int len = sym.length();
        if (len == 0)
            throw new IllegalStateException("empty symbol string");
        if (len == 1) {
            index = mapChar (sym.charAt(0));
        } else if (len == 2) {
            if (sym.equals(".."))
                index = 12;
            else if (sym.equals("::"))
                index = 13;
            else 
                unknownSymbol(sym);
        } else
            unknownSymbol(sym);
    }

    // STATIC METHODS ///////////////////////////////////////////////
    /** 
     * @return true if the character is a symbol and does not start
     *              a multi-character symbol 
     */
    public static boolean isSingleCharSymbol (char c) {
        return SINGLE_CHAR_SYMBOLS.indexOf(c) != -1;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the index of this symbol */
    public int getIndex() {
        return index;
    }
    // IMPLEMENTATION ///////////////////////////////////////////////    
    /**
     * Given a single character, return its index as a symbol.
     * 
     * @throws IllegalStateException if the character is not a valid symbol
     */
    private static int mapChar(char c) {
        int type = ALL_ONE_CHAR_SYMBOLS.indexOf(c);
        if (type < 0) 
            unknownSymbol(Character.toString(c));
        return type;
    } 
    private static void unknownSymbol(String sym) {
        throw new IllegalStateException("unknown symbol " + sym);
    }
    // EQUALS, HASHCODE /////////////////////////////////////////////
    public boolean equals(Object o) {
        if (o == null ||!(o instanceof Symbol) )
            return false;
        return (((Symbol)o).getIndex() == index);
    }
    public int hashCode() {
        return index;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    public String toString () {
        return new StringBuffer("[symbol:'").append(SYMBOLS[index])
            .append("']").toString();
    }
}
