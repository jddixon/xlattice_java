/* TestVariables.java */
package org.xlattice.corexml.expr;

import java.io.StringReader;

import junit.framework.*;

import org.xlattice.Context;
import org.xlattice.corexml.*;
import org.xlattice.corexml.om.*;

/**
 * @author Jim Dixon
 */

public class TestVariables extends org.xlattice.corexml.CoreXmlTestCase {

    private XmlParser  xp;    
    private StringReader reader;
    private Document   doc;
    private Context    ctx;
    private ExprParser parser;
    private Expr       result;

    public TestVariables (String name) {
        super(name);
    }
    public void setUp () { 
        ctx     = new Context();
        doc     = null;
        parser  = null;
        result  = null;   
    }
    public void tearDown() { }
 
    public void testSimpleVariables() throws Exception{
        ctx.bind ( "abc", new Numeric(859) );
        parser = new ExprParser(doc, ctx);
        assertNotNull(parser);
        result  = parser.eval("$abc");
        assertNotNull(result);
        assertTrue (result instanceof Numeric);
        assertEquals(859.0, ((Numeric)result).getValue(), 0.000001);
    }
    
    public void testBinaryExpr() throws Exception{
        ctx.bind ( "one", new Numeric(1) );
        parser = new ExprParser(doc, ctx);
        assertNotNull(parser);
        result  = parser.eval("859 + $one");
        assertNotNull(result);
        assertTrue (result instanceof Numeric);
        assertEquals(860.0, ((Numeric)result).getValue(), 0.000001);
        
        ctx.bind ( "five", new Numeric(5) );
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("21 * $five");
        assertEquals(105, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("21 - $five");
        assertEquals(16, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("21 div $five");
        assertEquals(4.2, ((Numeric)result).getValue(), 0.000001);
    }
    public void testThreeSomes() throws Exception{
        ctx.bind ( "two",   new Numeric(2) )
           .bind ( "three", new Numeric(3) )
           .bind ( "five",  new Numeric(5) );
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("$two + $three * $five");
        assertEquals(17, ((Numeric)result).getValue(), 0.000001);
        
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("$two * $three + $five");
        assertEquals(11, ((Numeric)result).getValue(), 0.000001);
       
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("$two - $three + $five");
        assertEquals(4, ((Numeric)result).getValue(), 0.000001);
        
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("$two * $three mod $five");
        assertEquals(1, ((Numeric)result).getValue(), 0.000001);
    }
    public void testNegated() throws Exception{
        ctx.bind ( "negOne",  new Numeric(-1) );
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("$negOne");
        assertEquals(-1, ((Numeric)result).getValue(), 0.000001);
    } 
    public void testParenthesized() throws Exception {
        ctx.bind ( "one",       new Numeric(1)  )
           .bind ( "negOne",    new Numeric(-1) )
           .bind ( "five",      new Numeric(5)  )
           .bind ( "seven",     new Numeric(7)  );
        parser = new ExprParser(doc, ctx);
        result  = parser.eval("($one)");
        assertEquals(1, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("($negOne)");
        assertEquals(-1, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("-($five)");
        assertEquals(-5, ((Numeric)result).getValue(), 0.000001);

        parser = new ExprParser(doc, ctx);
        result  = parser.eval("-(-$seven)");
        assertEquals(7, ((Numeric)result).getValue(), 0.000001);
    }
    public void testMissingBits() throws Exception {
        parser = new ExprParser(doc, ctx);
        try {
            result  = parser.eval("$");
            fail("parser accepted bare $!");
        } catch (CoreXmlException cxe) { /* success */ }
        
        parser = new ExprParser(doc, ctx);
        try {
            result  = parser.eval("$joe");
            fail("parser accepted undefined variable");
        } catch (CoreXmlException cxe) { /* success */ }
    }
    public void testExpressionsStoredInContext() throws Exception {
        parser = new ExprParser(doc, ctx);
        Expr e = parser.parse("6 * 7 div 3");
        ctx.bind("abd", e);
        parser = new ExprParser(doc, ctx);
        result = parser.eval("$abd");
        assertTrue (result instanceof Numeric);
        assertEquals(14, ((Numeric)result).getValue(), 0.000001);
    }
}
