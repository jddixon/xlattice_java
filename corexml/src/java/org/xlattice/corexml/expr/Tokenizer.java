/* Tokenizer.java */
package org.xlattice.corexml.expr;

import java.util.Stack;
import org.xlattice.corexml.CoreXmlException;

/**
 * Given a string, produce a sequence of XPath tokens.
 * 
 * @author Jim Dixon
 */
public class Tokenizer {

    /** the text being parsed */
    private final String str;
    /** length of the text */
    private final int len;
    /** position of next char we will look at */
    private int offset;

    public static final String MSG_EXPECTED_AXIS = "expected Axis name";
    public static final String MSG_EXPECTED_NODE_TYPE_OR_FUNC 
        = "expected node type or function name";
    public static final String MSG_EXPECTED_OP_NAME 
        = "expected operator name";

    // CONSTRUCTORS /////////////////////////////////////////////////
    /** 
     * Create the tokenizer.
     *
     * @param s the string we are dividing into tokens
     */
    public Tokenizer (String s) {
        str = s;
        len = s.length();
    }

    // PUBLIC INTERFACE /////////////////////////////////////////////
    /** whether a token has been pushed back */
    private boolean pushedBack = false;     // XXX
    /** if a token has been pushed back, previous value of lastToken */
    // XXX WON'T WORK RIGHT WITH STACK
    private Token prevLastToken;            //
    /** the last token returned, if any */
    private Token lastToken;
    
    private Stack stack = new Stack();
    /**
     * Push a token back on the input.  Allows a degree of lookahead.
     */
    public void pushback (Token t) {
        if(stack.isEmpty())
            prevLastToken = lastToken;
        lastToken = t;
        // DEBUG
        if (t == null)
            System.out.println("Tokenizer.pushback: receiving " + t);
        // END
        stack.push(t);
    }
    /**
     * Get the next token.  If anything is in the stack (in other 
     * words, if tokens have been pushed back), it is used first.
     */
    public Token getToken () throws CoreXmlException {
        prevLastToken = lastToken;
        if (!stack.isEmpty()) {
            Token ret  = (Token)stack.pop();
            lastToken  = ret;
            // DEBUG
            if (ret == null)
                System.out.println("lexer returning previously pushed-back "
                    + ret);
            // END
            return ret;
        }
        Token t = getToken_();
        // HANDLE QNames INCORRECTLY SEEN AS FUNCTION NAMES
        if (t instanceof FunctionName) {
            Token peek = getToken_();
            if (pushedBack)
                throw new CoreXmlException("pushback stack full!");
            if (!ExprParser.SYM_RPAREN.equals(peek)) {
                String name = ((FunctionName)t).getName();
                t = new QName(name);
            }
        }
        lastToken = t;
        return t;
    }
   
    // INTERNAL METHODS /////////////////////////////////////////////
    /**
     * Get the next token from the input string, without considering
     * the pushback stack.
     */
    private Token getToken_ () throws CoreXmlException {
        if (offset >= len) 
            return null;
        char c = str.charAt(offset);
        // SKIP SPACES //////////////////////////////////////////////
        while ( CharTypes.isSpace(c) ) 
            if ( ++offset >= len )
                break;
            else
                c = str.charAt(offset);
        if (offset >= len)
            return null;
        int startToken = offset;    // it's not on a space

        // LITERALS /////////////////////////////////////////////////
        // XXX THIS WILL NOT HANDLE ESCAPED DELIMITERS 
        if (c == '\'' || c == '\"') {
            char delim = c;
            boolean foundSecondDelim = false;
            offset++;   // skip starting delimiter
            while (offset < len) {
                c = str.charAt(offset++);
                if (c == delim) {
                    foundSecondDelim = true;
                    break;
                }
            }
            if (!foundSecondDelim)
                throw new CoreXmlException("unterminated quotation");
            lastToken = new Literal (str.substring(startToken + 1, offset - 1));
            return lastToken;
        }
        // HANDLE NUMBERS ///////////////////////////////////////////
        else if (Character.isDigit(c)) {
            do {
                if ( ++offset < len )
                    c = str.charAt(offset);
                else
                    break;
            } while (Character.isDigit(c));
            // offset points to a non-digit
            if (c == '.') {
                offset ++;  // char after dot
                while ((offset < len) 
                        && Character.isDigit((c = str.charAt(offset++))))
                    ;
            }
            lastToken = new Numeric (str.substring(startToken, offset));
            return lastToken;
        }
        // HANDLE * /////////////////////////////////////////////////
        else if (c == '*') {
            offset++;
            // section 3.7 of spec, rule [28]
            boolean returnSymbol = false;
           
            if (lastToken == null)
                returnSymbol = true;
            else if (lastToken instanceof Symbol) {
                int type = ((Symbol)lastToken).getIndex();
                if ( type == Symbol.AT || type == Symbol.DCOLON
                  || type == Symbol.LPAREN || type == Symbol.LBRACKET )
                    returnSymbol = true;
            } else if (lastToken instanceof Operator) {
                returnSymbol = true;
            } 
            if (returnSymbol) {
                lastToken = new Symbol(c);
                return lastToken;
            } else {
                lastToken = new Operator(c);
                return lastToken;
            }
        }
        // NCNAMEs //////////////////////////////////////////////////
        // XXX WORKING HERE: if an NCNames is preceded by 
        //   @ :: ( [ or an Operator, it must be returned as a QName
        // If an NCName is followed by ::, it must be an AxisName
        // //////////////////////////////////////////////////////////
        else if (CharTypes.startsName(c)) {
            if (++offset == len) {
                lastToken = TokenFactory.
                                tokenFor(str.substring(startToken, offset));
                return lastToken;
            }
            for (c = str.charAt(offset); CharTypes.isNameChar(c); 
                                                c = str.charAt(offset)) 
                if (++offset >= len)
                    break;
            // we have a name
            if ( offset >= len || c != ':' || (c == ':' 
                        && ((offset + 1) == len) 
                           || !CharTypes.isNameChar(str.charAt(offset+1)))) {
                lastToken = TokenFactory
                            .tokenFor(str.substring(startToken, offset));
                return lastToken;
            }
            // POSSIBLE QCNAME XXX WORKING HERE
        }
        // QNAMES INCLUDING FUNCTIONS ///////////////////////////////

        // EXPR TOKENS (PUNCTUATION MARKS) //////////////////////////
        // Minus (-) handled here, so must FOLLOW names /////////////
        else if (Symbol.isSingleCharSymbol(c) ) {
            offset++;
            lastToken = new Symbol(c);
            return lastToken;
        }
        // MULTI-CHARACTER SYMBOLS ARE :: and .. 
        // XXX THIS CODE CAN BE SIMPLIFIED
        else if (c == ':') {
            if (++offset >= len) {
                lastToken = new Symbol(c);
                return lastToken;
            }
            c = str.charAt(offset);
            if (c == ':') {
                offset++;
                lastToken =  new Symbol("::");
                return lastToken;
            } else {
                lastToken = new Symbol(":");
                return lastToken;
            } 
        } // GEEP
        else if (c == '.') {
            if (++offset >= len) {
                lastToken = new Symbol(c);
                return lastToken;
            }
            c = str.charAt(offset);
            if (c == '.') {
                offset++;
                lastToken = new Symbol("..");
                return lastToken;
            } else {
                lastToken = new Symbol(".");
                return lastToken;
            }
        }
        // OPERATORS ////////////////////////////////////////////////
        else if (Operator.isSingleCharOperator(c)) {
            offset++;
            lastToken = new Operator(c);
            return lastToken;
        }
        // XXX MOST MULTI-CHAR OPS NOT HANDLED ****************
        else if (c == '/') {
            if (++offset >= len) {
                lastToken = new Operator(c);
                return lastToken;
            }
            c = str.charAt(offset);
            if (c == '/') {
                offset++;
                lastToken =  new Operator("//");
                return lastToken;
            } else {
                lastToken = new Operator("/");
                return lastToken;
            } 
        } // GEEP

        // AXIS NAMES ///////////////////////////////////////////////
        
        // NODE TYPES ///////////////////////////////////////////////
        // [35] node types are not variable names
        // [38] NodeType = comment | text | processing-instruction | node
        
        // VARIABLE REFERENCES //////////////////////////////////////
        
        // DOLLAR-BRACE CONSTRUCTS //////////////////////////////////
        
        // UNKNOWN TOKENS ///////////////////////////////////////////
        throw new CoreXmlException ("unrecognized token");
    }

    // PROPERTIES -- JUST FOR DEBUGGING /////////////////////////////
    /** @return the text being tokenized, for debugging */
    public String getText() {
        return str;
    }
    /** @return the length of text being tokenized, for debugging */
    public int getLength() {
        return len;
    }
    /** @return the current offset into the text, for debugging */
    public int getOffset() {
        return offset;
    }
    /** @return the last token returned by the lexer, for debugging */
    public Token getLastToken() {
        return lastToken;
    }
}
