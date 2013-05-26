/* TestExpr.java */
package org.xlattice.util.context;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

import org.xlattice.Context;

public class TestExpr extends TestCase {

    Expr expr;
    Context emptyContext, capContext, xyzContext;
    
    public TestExpr (String name) {
        super(name);
    }

    private static final String THIS_IS       = "this is ";
    private static final String A_LITTLE_TEST = "a little test";
    private static final String COMBO = THIS_IS + A_LITTLE_TEST;
    
    public void setUp () {
        expr  = null;
        emptyContext = new Context();
        capContext   = new Context()
                        .bind("abc", "ABC")
                        .bind("def", "DEF")
                        .bind("ghi", "GHI")
                        .bind("mno", "MNO")
                        .bind("pqr", "PQR");
        xyzContext   = new Context()
                        .bind("def", "XYZ");
    }

    public void tearDown() {
    }

    public void testSingleLiteral() throws Exception {
        //               0123
        expr = new Expr("abc");
        assertEquals(1, expr.size());
        Literal lit0_3 = (Literal) expr.get(0);
        assertEquals(0, lit0_3.from());
        assertEquals(3, lit0_3.to());
        assertEquals("abc", lit0_3.resolve(emptyContext));
        assertEquals("abc", expr.resolve(emptyContext));
    } 

    public void testSingleSymbol() throws Exception {
        //               0123456
        expr = new Expr("${def}");
        assertEquals(1, expr.size());
        Symbol  sym0_6  = (Symbol) expr.get(0);
        Expr    expr2_5 = sym0_6.get();
        assertEquals(1, expr2_5.size());
        Literal lit2_5  = (Literal)expr2_5.get(0);

        assertEquals ( 0, expr.from());
        assertEquals ( 6, expr.to());
        assertEquals ( 0, sym0_6.from());
        assertEquals ( 6, sym0_6.to());
        assertEquals ( 2, expr2_5.from());
        assertEquals ( 5, expr2_5.to());
        assertEquals ( 2, lit2_5.from());
        assertEquals ( 5, lit2_5.to());

        assertEquals ("def", lit2_5 .resolve(emptyContext));
        // inner expression consists only of a literal
        assertEquals ("def", expr2_5 .resolve(capContext));
        assertEquals ("def", expr2_5 .resolve(xyzContext));
        
        assertEquals ("DEF", sym0_6 .resolve(capContext));
        assertEquals ("XYZ", sym0_6 .resolve(xyzContext));
        // unbound symbols resolve to empty String
        assertEquals ("",    sym0_6 .resolve(emptyContext));

        assertEquals ("DEF", expr.resolve(capContext));
        assertEquals ("XYZ", expr.resolve(xyzContext));
        assertEquals ("",    expr.resolve(emptyContext));
        
    } 

    public void testSymbolBracketedByLiterals() throws Exception {
        //               012345678901
        expr = new Expr("abc${def}ghi");
        assertEquals(3, expr.size());
        Literal lit0_3  = (Literal)expr.get(0);
        Symbol  sym3_9  = (Symbol) expr.get(1);
        Literal lit9_12 = (Literal)expr.get(2);
        Expr    expr5_8 = sym3_9.get();
        
        assertEquals(1, expr5_8.size());
        Literal lit5_8  = (Literal)expr5_8.get(0);

        assertEquals ( 0, expr.from());
        assertEquals (12, expr.to());
        assertEquals ( 0, lit0_3.from());
        assertEquals ( 3, lit0_3.to());
        assertEquals ( 3, sym3_9.from());
        assertEquals ( 9, sym3_9.to());
        assertEquals ( 5, expr5_8.from());
        assertEquals ( 8, expr5_8.to());
        assertEquals ( 5, lit5_8.from());
        assertEquals ( 8, lit5_8.to());
        assertEquals ( 9, lit9_12.from());
        assertEquals (12, lit9_12.to());

        assertEquals ("abc", lit0_3 .resolve(emptyContext));
        assertEquals ("def", lit5_8 .resolve(emptyContext));
        assertEquals ("ghi", lit9_12.resolve(emptyContext));

        assertEquals ("def", expr5_8 .resolve(capContext));
        assertEquals ("def", expr5_8 .resolve(xyzContext));
        
        assertEquals ("DEF", sym3_9 .resolve(capContext));
        assertEquals ("XYZ", sym3_9 .resolve(xyzContext));
        // unbound symbols resolve to empty String
        assertEquals ("",    sym3_9 .resolve(emptyContext));

        assertEquals ("abcDEFghi", expr.resolve(capContext));
        assertEquals ("abcXYZghi", expr.resolve(xyzContext));
        assertEquals ("abcghi",    expr.resolve(emptyContext));
        
    } 

    public void testNestedSymbols() throws Exception {
        //               0123456789012
        expr = new Expr("${${${def}}}");
        Context ctx = new Context().bind("def", "wombat")
                                   .bind("wombat", "squirrel")
                                   .bind("squirrel", "humungous porcupine");
        assertEquals(1, expr.size());
        Symbol  sym0_12  = (Symbol) expr.get(0);
        Expr expr2_11 = sym0_12.get();
        assertEquals("squirrel", expr2_11.resolve(ctx));
        assertEquals("humungous porcupine", expr.resolve(ctx));
        assertEquals("", expr.resolve(emptyContext));
    } 

    public void testSymbolString() throws Exception {
        //               0123456789012
        expr = new Expr("abc${def}${ghi}jkl${mno}pqr");
        assertEquals(6, expr.size());
        assertEquals("abcDEFGHIjklMNOpqr", expr.resolve(capContext));
        assertEquals("abcXYZjklpqr", expr.resolve(xyzContext));
    } // GEEP

    public void testMix() throws Exception {
        //               0123456789012
        expr = new Expr("abc${def${ghi}jkl}mno${pqr}");
        assertEquals(4, expr.size());
        assertEquals("abcmnoPQR", expr.resolve(capContext));
        assertEquals("abcmno", expr.resolve(xyzContext));
    } 
}
