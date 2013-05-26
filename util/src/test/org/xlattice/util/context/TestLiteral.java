/* TestLiteral.java */
package org.xlattice.util.context;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

import org.xlattice.Context;

public class TestLiteral extends TestCase {

    Term term, term2;
    Expr expr;
    
    public TestLiteral (String name) {
        super(name);
    }

    private static final String THIS_IS       = "this is ";
    private static final String A_LITTLE_TEST = "a little test";
    public void setUp () {
        term  = new Literal(THIS_IS, 0, THIS_IS.length());
        term2 = new Literal(A_LITTLE_TEST, 0, A_LITTLE_TEST.length());
        expr  = null;
    }

    public void tearDown() {
    }
 
    public void testLiteral() {
        assertEquals (THIS_IS,       term.resolve(new Context()));
        assertEquals (A_LITTLE_TEST, term2.resolve(new Context()));
    }
}
