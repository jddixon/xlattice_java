/* Literal.java */
package org.xlattice.util.context;

import org.xlattice.Context;

/**
 * A literal is simple text containing no Symbols.
 * 
 *
 *
 * @author Jim Dixon
 */
public class Literal implements Term {
    private final String text;
    private final int startsAt;
    private final int endsAt;

    /**
     * @param s      string the Literal occurs in
     * @param from   offset of first character
     * @param to     offset of character beyond end of literal
     */
    public Literal (String s, int from, int to) {
        text     = s;
        startsAt = from;
        endsAt   = to;
    }

    /**
     * Resolve the Literal in the context.
     * @return the Literal
     */
    public String resolve (Context ctx) {
        return text.substring(startsAt, endsAt);
    }

    /** @return offset of first character in Literal */
    public int from () {
        return startsAt;
    }
    /** @return offset of first character beyond the Literal */
    public int to() {
        return endsAt;
    }
    /** @return the Literal in a form suitable for debugging */
    public String toString() {
        StringBuffer sb = new StringBuffer("Literal[")
                            .append(startsAt).append(":").append(endsAt)
                            .append("] \"").append(text).append("\"");
        return sb.toString();
    }
}
