/* CharTypes.java */
package org.xlattice.corexml.expr;

/**
 * Except where otherwise noted, cited rules are in XPath 1.0 standard.
 * Rule cites look like "[NN]", where NN represents a number.  Quotation
 * marks around characters are dropped when quoting rules.  
 * <p/>
 * This implementation is not fully consistent with XPath 1.0, which
 * appears not to make sense in certain respects.
 *
 * @author Jim Dixon
 */
public class CharTypes {

    private CharTypes () { }    // do not instantiate me, please
    
    /**
     * Rules in XML standard:
     * [3]  S ::= (#x20 | #x9 | #xD | #xA)+
     * [4]  NCName ::= (Letter | _) (NCNameChar)*
     * [5]  NCNameChar ::= Letter | Digit | . | - | _ | CombiningChar | Extender
     */
   
    /** @return true if the character is whitespace */
    public static boolean isSpace(char c) {
        return (c == ' ') || (c == '\n') || (c == '\t') || (c == '\r');
    }

    // Character.isDigit() is built into Java
   
    /**
     * XXX Not consistent with the XML spec, but OK for now.
     */
    public static boolean isLetter (char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }
    /**
     * Recognizer for characters that may be part of names.  XXX The
     * current implementation is NOT consistent with the XML Names spec,
     * which permits '.' and many other characters (letters, CombiningChar,
     * and Extender).
     */
    public static boolean isNameChar (char c) {
        return isLetter(c) || Character.isDigit(c) || c == '-' || c == '_';
    }
    /**
     * Recognizer for characters that may start names. 
     */
    public static boolean startsName (char c) {
        return isLetter(c) || c == '_';
    }
}
