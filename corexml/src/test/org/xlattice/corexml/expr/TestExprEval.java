/* TestExprEval.java */
package org.xlattice.corexml.expr;

import java.io.StringReader;

import junit.framework.*;

import org.xlattice.Context;
import org.xlattice.corexml.*;
import org.xlattice.corexml.om.*;

/**
 * @author Jim Dixon
 */

public class TestExprEval extends org.xlattice.corexml.CoreXmlTestCase {

    private XmlParser  xp;    
    private StringReader reader;
    private Document   doc;
    private Context    ctx;
    private ExprParser parser;
    private Expr       result;

    public TestExprEval (String name) {
        super(name);
    }
    public void setUp () { 
        ctx     = new Context();
        doc     = null;
        parser  = null;
        result  = null;   
    }
    public void tearDown() { }
 
    public void testSimpleNumbers() throws Exception{
        parser = new ExprParser(doc, ctx);
        assertNotNull(parser);
        result  = parser.eval("859");
        assertNotNull(result);
        assertTrue (result instanceof Numeric);
        assertEquals(859.0, ((Numeric)result).getValue(), 0.000001);
    }
    
    public void testBinaryExpr() throws Exception{
        parser = new ExprParser(doc, ctx);
        assertNotNull(parser);
        result  = parser.eval("859 + 1");
        assertNotNull(result);
        assertTrue (result instanceof Numeric);
        assertEquals(860.0, ((Numeric)result).getValue(), 0.000001);
        
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("21 * 5");
        assertEquals(105, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("21 - 5");
        assertEquals(16, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("21 div 5");
        assertEquals(4.2, ((Numeric)result).getValue(), 0.000001);
    }
    public void testThreeSomes() throws Exception{
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("2 + 3 * 5");
        assertEquals(17, ((Numeric)result).getValue(), 0.000001);
        
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("2 * 3 + 5");
        assertEquals(11, ((Numeric)result).getValue(), 0.000001);
       
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("2 - 3 + 5");
        assertEquals(4, ((Numeric)result).getValue(), 0.000001);
        
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("2 * 3 mod 5");
        assertEquals(1, ((Numeric)result).getValue(), 0.000001);
    }
    public void testNegated() throws Exception{
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("-1");
        assertEquals(-1, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("--13");
        assertEquals(13, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("-1 + 2");
        assertEquals(1, ((Numeric)result).getValue(), 0.000001);
    } 
    public void testParenthesized() throws Exception {
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("(1)");
        assertEquals(1, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("(-1)");
        assertEquals(-1, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("-(5)");
        assertEquals(-5, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("-(-7)");
        assertEquals(7, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("4 + - 7");
        assertEquals(-3, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("4 + ( 7)");
        assertEquals(11, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("4 + (-7)");
        assertEquals(-3, ((Numeric)result).getValue(), 0.000001);

        try {
            parser = new ExprParser(doc, ctx);
            result  = parser.eval("4  (-7)");
            fail("missing operator not caught");
        } catch (CoreXmlException cxe) { /* success */ }

    }
    public void testDivideByZero() throws Exception {
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("1 + 0");
        assertEquals(1, ((Numeric)result).getValue(), 0.000001);
   
        // EXPLORATORY
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("1 div 0");
        assertEquals(Double.POSITIVE_INFINITY, 
                        ((Numeric)result).getValue(), 0.000001);
    
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("1 mod 0");
        assertTrue(Double.isNaN(((Numeric)result).getValue()));
    
    }    
}
