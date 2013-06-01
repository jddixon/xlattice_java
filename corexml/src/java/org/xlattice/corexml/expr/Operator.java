/* Operator.java */
package org.xlattice.corexml.expr;

/**
 * Token representing an XPath operator.  See the table below for
 * a list of such operators.  XPath distinguishes between operators
 * and symbols and star (*), for example, is one or the other depending
 * upon its immediate context.
 * <p/>
 * XXX SHOULD BE REWORKED SO THAT OPERATORS ARE ORDERED BY 
 * PRECEDENCE.  Probably ;-)
 *
 * @author Jim Dixon
 */
public class Operator implements FixedNameToken {

    /** index of this token in the table below */
    private int index;
    
    /** list of 1-char operators which only have one form */
    public static final String SINGLE_CHAR_OPERATORS  = "|+=-*";
    /** list of 1-char operators including those also part of multi-char ops */
    public static final String ALL_ONE_CHAR_OPERATORS = "|+=-*/<>";

    // INDEX VALUES IN TABLE BELOW MUST BE SAME AS IN THIS ARRAY 
    /** list of named operators */
    public static final String[] NAMES = {"or", "and", "mod", "div"}; 
   
    // these need not be in order; contain both forms of operators
    // including < and >
    /** complete table of operators, some in two forms */
    public static final String[] OPERATORS = 
            {"or", "and", "mod", "div", 
         "|",      "+",   "=",   "-",     "*",    "/",     "//",
         "!=",     ">",   "<",   ">=",    "<=",
                  "&gt;", "&lt;","&gt;=", "&lt;=" }; 

    /** tags used in representing the operators in XML debug output */
    public static final String[] TAGS  = {"or",  "and",   "mod",  "div",
        "union", "plus", "eq", "minus",   "mul", "slash", "dslash", 
        "ne",    "gt",   "lt", "ge",      "le" };
    
    /** precedence of each operator, in the same order */
    public static final int[] PRECEDENCE = {0,      1,      5,     5,
          7,       4,      2,     4,        5,      0,      0,
          2,       3,      3,     3,        3  };

    // NAMED OPERATORS //////////////////////////////////////////////
    /** 'or' */
    public static final int OR      =  0;
    /** 'and', all tokens being case-sensitive */
    public static final int AND     =  1;
    /** 'mod' */
    public static final int MOD     =  2;
    /** 'div' */
    public static final int DIV     =  3;

    // SINGLE CHARACTER OPERATORS /////////////////////////
    // >>> TAKE CARE IN MOVING THESE; expected to start at 4
    /** '|/  */
    public static final int BAR     =  4;
    /** '+'  */
    public static final int PLUS    =  5;
    /** '='  */
    public static final int EQ      =  6;
   
    /** '-'  BUT CAN BE PART OF A NAME */
    public static final int MINUS   =  7;
    /** '*'  BUT MIGHT INSTEAD BE A SYMBOL */
    public static final int MUL     =  8;
    
    // ALSO START LONGER SEQUENCES //////////////
    /** '/'  */
    public static final int SLASH   =  9;
  
    // HAVE ALTERNATIVE REPRESENTATIONS /////////
    /* '<' */
    /* '>' */

    // TWO CHARACTER STRINGS //////////////////////////////
    // also 'or', above 

    /** '//' */
    public static final int DSLASH  = 10;
   
    /** '!=' */
    public static final int NE      = 11;

    
    // FOUR CHARACTER STRINGS /////////////////////////////
    /** '&gt;' */
    public static final int GT      = 12;
    /** '&lt;' or non-XML equivalent */
    public static final int LT      = 13;

    // FIVE CHARACTER STRINGS /////////////////////////////
    /** '&gt;=' (or >=) */
    public static final int GE      = 14;
    /** '&lt;=  (or <=) */
    public static final int LE      = 15;
 
    /**
     * Create an operator token, given its single-character representation.
     * 
     * @throws IllegalStateException if the character is not recognized
     */
    public Operator (char c) {
        index = mapChar(c);
    }
    /**
     * Create an operator token, given a String form.
     * <p/>
     * Assign the token value.  The actual parsing is done elsewhere,
     * so the String is an exact match, but the number of characters
     * differs from case to case.
     */
    public Operator (String s) {
        if (s == null)
            throw new NullPointerException("operator text missing");
        int len = s.length();
        if (len == 0)
            throw new IllegalStateException("empty operator string");
        if (len == 1) {
            index = mapChar (s.charAt(0));
        } else if (len == 2) {
                if (s.equals("or"))         { index = OR;     }
                else if (s.equals("//"))    { index = DSLASH; }
                else if (s.equals("!="))    { index = NE;     }
                else if (s.equals("<="))    { index = LE;     }
                else if (s.equals(">="))    { index = GE;     }
                else unknownOperator(s);
        } else if (len == 3) {
                if (s.equals("and"))        { index = AND;    }
                else if (s.equals("mod"))   { index = MOD;    }
                else if (s.equals("div"))   { index = DIV;    }
                else unknownOperator(s);
        } else if (len == 4) {
                if (s.equals("&lt;"))       { index = LT;     }
                else if (s.equals("&gt;"))  { index = GT;     }
                else unknownOperator(s);
        } else if (len == 5) {
                if (s.equals("&lt;="))      { index = LE;     }
                else if (s.equals("&gt;=")) { index = GE;     }
                else unknownOperator(s);
        } else
            unknownOperator(s);
    }
    // STATIC METHODS ///////////////////////////////////////////////
    /**
     * Whether a character by itself is an operator and is not the first 
     * character in another operator.
     * 
     * @return true if the character is a one-character operator 
     */
    public static boolean isSingleCharOperator (char c) {
        return SINGLE_CHAR_OPERATORS.indexOf(c) != -1;
    }
    public static int getOperatorIndex (String str) {
        int index = -1;
        
        // XXX WORKING HERE
    
        return index;
    }
    /** 
     * True if the single character is a valid operator.  Of course
     * the lt, gt symbols will be encoded with a leading ampersand.
     *
     * [32] Operator ::= OperatorName | MultiplyOperator 
     *                     | / | // | | | + | - | = | != | < | <= | > | >= 
     * [33] OperatorName ::= and | or | mod | div
     * [34] MultiplyOperator = *
     */
    public static boolean startsOperator(char c) {
        return 
            (c == '/') || (c == '*') || (c == '|') || (c == '+') 
         || (c == '-') || (c == '=') || (c == '!') || (c == '<') || (c == '>');
    }
    // FIXED NAME TOKEN INTERFACE ////////////////////////////////////
    /** @return a reference to the list of operators with English names */
    public static String [] getNames() {
        return NAMES;
    }
    public int getIndex() {
        return index;
    } 
    public String getName() {
        return NAMES[index];
    }
    // EQUALS, HASHCODE /////////////////////////////////////////////
    public boolean equals(Object o) {
        if (! (o instanceof Operator) )
            return false;
        return (((Operator)o).getIndex() == index);
    }
    public int hashCode() {
        return index;
    }
    // IMPLEMENTATION ///////////////////////////////////////////////
    /** 
     * Given a single character, return its operator index, if any.
     * @throws IllegalStateException if there is no match 
     */
    private static int mapChar(char c) {
        int index = ALL_ONE_CHAR_OPERATORS.indexOf(c);
        if (index < 0)
            unknownOperator(Character.toString(c));
        return index + 4;
    }
    private static void unknownOperator(String s) {
        throw new IllegalStateException("unknown operator: " + s);
    }
    // SERIALIZATION ////////////////////////////////////////////////
    /** @return a String representing the operator for debug messages */
    public String toString() {
        return new StringBuffer("[operator:")
            .append(TAGS[index]).append("]").toString();
    }
}
