/* TestTokenFactory.java */
package org.xlattice.corexml.expr;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestTokenFactory extends TestCase {

    private TokenFactory factory = TokenFactory.getInstance();

    public TestTokenFactory (String name) {
        super(name);
    }

    public void setUp () { }

    public void tearDown() { }
 
    public void testTheFactory() {
        AxisName     aName;
        FunctionName fName;
        NodeType     nName;
        Operator     oName;
        for (int i = 0; i < AxisName.NAMES.length; i++) {
            Token t = factory.tokenFor(AxisName.NAMES[i]);
            assertNotNull ("null token returned for AxisName for " 
                    + AxisName.NAMES[i], t);
            aName = (AxisName) t;
            assertEquals(AxisName.NAMES[i], aName.getName());
        }
        for (int i = 0; i < FunctionName.NAMES.length; i++) {
            fName = (FunctionName) factory.tokenFor(FunctionName.NAMES[i]);
            assertEquals(FunctionName.NAMES[i], fName.getName());
        }
        for (int i = 0; i < NodeType.NAMES.length; i++) {
            nName = (NodeType) factory.tokenFor(NodeType.NAMES[i]);
            assertEquals(NodeType.NAMES[i], nName.getName());
        }
        // named operators
        for (int i = 0; i < Operator.NAMES.length; i++) {
            oName = (Operator) factory.tokenFor(Operator.NAMES[i]);
            assertEquals(Operator.NAMES[i], oName.getName());
        }
        // all operators
        for (int i = 0; i < Operator.OPERATORS.length; i++) {
            if (!Operator.OPERATORS[i].equals("*")) {
                Token t = factory.tokenFor(Operator.OPERATORS[i]);
                assertNotNull ("null token returned for Operator for " 
                        + Operator.OPERATORS[i], t);
                assertTrue (
                        "operator " + Operator.OPERATORS[i] + " returns " + t,
                        t instanceof Operator);
                assertEquals (
                        new Operator(Operator.OPERATORS[i]), t);
            } 
        }
        
        // symbols
        for (int i = 0; i < Symbol.SYMBOLS.length; i++) {
            if (!Symbol.SYMBOLS[i].equals("*")) {
                Symbol sym = (Symbol) factory.tokenFor(Symbol.SYMBOLS[i]);
                assertEquals (new Symbol(Symbol.SYMBOLS[i]), sym);
            }
        }
    }
}
