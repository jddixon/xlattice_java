/* TokenFactory.java */
package org.xlattice.corexml.expr;

import java.util.HashMap;

/**
 * Given a character string, constructs and returns a Token of the
 * appropriate type.
 * 
 * XXX AMBIGUITY: * may be either an Operator or a Symbol, so we
 *     do not allow it here
 * XXX PROBLEM: div must be a possible node name (as in HTML) 
 *
 * @author Jim Dixon
 */
public class TokenFactory {

    private static TokenFactory INSTANCE = new TokenFactory();
    private static HashMap names = new HashMap();
    static {
        // load the axis names
        for (int i = 0; i < AxisName.NAMES.length; i++)
            names.put(AxisName.NAMES[i], 
                            new AxisName(AxisName.NAMES[i]) );
        for (int i = 0; i < FunctionName.NAMES.length; i++)
            names.put(FunctionName.NAMES[i],   
                            new FunctionName(FunctionName.NAMES[i]));
        for (int i = 0; i < NodeType.NAMES.length; i++)
            names.put (NodeType.NAMES[i],
                            new NodeType(NodeType.NAMES[i]));
        // all of the Operators
        for (int i = 0; i < Operator.OPERATORS.length; i++) {
            if (!Operator.OPERATORS[i].equals("*"))
                names.put (Operator.OPERATORS[i],
                            new Operator(Operator.OPERATORS[i]));
        }
        
        // all symbols
        for (int i = 0; i < Symbol.SYMBOLS.length; i++) {
            if (!Symbol.SYMBOLS[i].equals("*"))
                names.put (Symbol.SYMBOLS[i], 
                            new Symbol(Symbol.SYMBOLS[i]));
        }
    }
    // CONSTRUCTOR //////////////////////////////////////////////////
    private TokenFactory() {}

    // STATIC METHODS ///////////////////////////////////////////////
    public static TokenFactory getInstance() {
        return INSTANCE;
    }
    /**
     * Look up a string and return the corresponding token.
     */
    public static Token tokenFor(String s) {
        Token t = (Token) names.get(s);
        if (t == null)
            t = new QName(s);
        return t;
    }
}
