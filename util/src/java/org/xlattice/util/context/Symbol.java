/* Symbol.java */
package org.xlattice.util.context;

import java.text.ParseException;
import org.xlattice.Context;

/** 
 * This represents what is inside the delimiting ${ and } and may 
 * be any Expr, any sequence of literals and symbols.
 * 
 * @author Jim Dixon
 */
public class Symbol implements Term {

    private Expr expr;
    private final int startsAt;
    private int endsAt;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Create a Symbol, meaning a substring bracketed by ${ and }
     *
     * @param s        the string the symbol appears in
     * @param curPos   current position in the string
     * @param inSymbol whether this symbold is contained within another
     */
    public Symbol (String s, int curPos, boolean inSymbol) 
                                            throws ParseException {
        int len = s.length();
        startsAt = curPos;
        expr = new Expr(s,  
                curPos + 2,     // skip the leading ${
                inSymbol);
        endsAt = expr.to() + 1;

    }

    // PROPERTIES ///////////////////////////////////////////////////
    public int from() {
        return startsAt;
    }
    public int to() {
        return endsAt;
    }
    public Expr get() {
        return expr;
    }
    // IMPLEMENTATION ///////////////////////////////////////////////
    public String toString() {
        StringBuffer sb = new StringBuffer()
            .append("Symbol: ").append(expr.toString());
        return sb.toString();
    }
    public String resolve (Context ctx) {
        String result = (String) ctx.lookup((String)(expr.resolve(ctx)));
        if (result == null) 
            return "";
        else 
            return result;
    }
}
