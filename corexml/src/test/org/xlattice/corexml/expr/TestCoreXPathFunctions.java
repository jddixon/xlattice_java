/* TestCoreXPathFunctions.java */
package org.xlattice.corexml.expr;

import junit.framework.*;
import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

/**
 * Tests of the XPath Core Function Library, as specified in section
 * 4 of <a href="http://www.w3.org/TR/xpath">the XPath specs.</a>
 *
 * @author Jim Dixon
 */
public class TestCoreXPathFunctions extends CoreXmlTestCase {

    private NodeSet nodes;
    
    public TestCoreXPathFunctions (String name) {
        super(name);
    }
    public void setUp () { 
        nodes = null;
    }

    public void tearDown() { }

    /////////////////////////////////////////////////////////////////
    // NODESET FUNCTIONS: there are 7 ///////////////////////////////
    /////////////////////////////////////////////////////////////////
    public void testNodeSetFunc() {
        // 1. count //////////////////////////////////////////////////
        assertEquals(0, 
                ((Numeric)ExprParser.count(nodes)).getValue(), 0.0001);
        nodes = new NodeSet();
        assertEquals(0, 
                ((Numeric)ExprParser.count(nodes)).getValue(), 0.0001);
        nodes.add (new Comment("oh"))
             .add (new Comment("oh"))
             .add (new Comment("oh"));
        assertEquals(3, 
                ((Numeric)ExprParser.count(nodes)).getValue(), 0.0001);
    }
    /////////////////////////////////////////////////////////////////
    // STRING FUNCTIONS: there are 10 ///////////////////////////////
    /////////////////////////////////////////////////////////////////
    public void testStringFunc() {
       
        // 1. string /////////////////////////////////////////////////
        // section 4.2 of specs
        // conversion of Booleans /////////////////////////
        assertEquals("false", ExprParser.string(Boolean.FALSE));
        assertEquals("true",  ExprParser.string(Boolean.TRUE ));
        
        // conversion of NodeSets /////////////////////////
        // "return... the string-value of the node in the node-set that is
        // first in document order.  If the node set is empty, an empty
        // string is returned."
        assertEquals("", ExprParser.string(new NodeSet()));
       
        // XXX MISSING OTHER NODESET TESTS

        // conversion of Numerics /////////////////////////
        assertEquals("NaN", ExprParser.string(new Double(Double.NaN)));
        // dunno how to generate positive/negative zero XXX
        assertEquals("Infinity", 
                ExprParser.string(new Double(Double.POSITIVE_INFINITY)));
        assertEquals("-Infinity", 
                ExprParser.string(new Double(Double.NEGATIVE_INFINITY)));
        // the standard requires that integer values be represented as such
        assertEquals("252", 
                ExprParser.string(new Double(252.0)));
        assertEquals("-47", 
                ExprParser.string(new Double(-47.0)));
        // it also requires that excess decimal digits not be displayed
        assertEquals("252.17", 
                ExprParser.string(new Double(252.17)));
        // ... and that the leading zero be displayed
        assertEquals("0.25217", 
                ExprParser.string(new Double(0.25217)));
        
        
    }
    /////////////////////////////////////////////////////////////////
    // BOOLEAN FUNCTIONS: there are 5 ///////////////////////////////
    /////////////////////////////////////////////////////////////////
    public void testBooleanFunc() {
        // 1. boolean ///////////////////////////////////////////////
        // Boolean conversion 
        assertTrue ( ExprParser.booleanFunc(Boolean.TRUE) );
        assertFalse( ExprParser.booleanFunc(Boolean.FALSE));
        
        // NodeSet conversion 
        nodes = new NodeSet();
        assertFalse( ExprParser.booleanFunc(nodes) );
        nodes.add (new Comment("oh"))
             .add (new Comment("oh"))
             .add (new Comment("oh"));
        assertTrue( ExprParser.booleanFunc(nodes) );
                
        // Numeric conversion 
        assertFalse (ExprParser.booleanFunc (new Double(0.0)));
        assertFalse (ExprParser.booleanFunc (new Double(Double.NaN)));
        assertTrue  (ExprParser.booleanFunc (new Double(0.000000000001)));
        assertTrue  (ExprParser.booleanFunc (new Double(-475)));

        // String conversion 
        assertFalse (ExprParser.booleanFunc (""));
        assertTrue  (ExprParser.booleanFunc ("a"));
        assertTrue  (ExprParser.booleanFunc ("abcdef"));

        // other types XXX NO IMPLEMENTATION
    }
    /////////////////////////////////////////////////////////////////
    // NUMERIC FUNCTIONS: there are 5 ///////////////////////////////
    /////////////////////////////////////////////////////////////////
    public void testNumericFunc() {
        // 1. number ////////////////////////////////////////////////
        // String conversion 
        assertEquals (64.72, ExprParser.number ("  64.72 ").doubleValue(),
                                                        0.0000001);
        assertEquals (-4.72, ExprParser.number ("  -4.72 ").doubleValue(),
                                                        0.0000001);
        assertTrue   (ExprParser.number("64.72a").isNaN());

        // Boolean conversion
        assertEquals (1, ExprParser.number(Boolean.TRUE).doubleValue(),  
                                                        0.0000001);
        assertEquals (0, ExprParser.number(Boolean.FALSE).doubleValue(), 
                                                        0.0000001);

        // NodeSet conversion 
        // XXX MISSING 
    }
    
}
