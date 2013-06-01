/* TestExprToXml.java */
package org.xlattice.corexml.expr;

import java.io.StringReader;
import junit.framework.*;

import org.xlattice.Context;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

/**
 * @author Jim Dixon
 */

public class TestExprToXml extends CoreXmlTestCase {

    private XmlParser  xp;    
    private StringReader reader;
    private Document   doc;
    private Context    ctx;
    private ExprParser exp;
    private NodeSet    nodeset;
    private String     result;

    public TestExprToXml (String name) {
        super(name);
    }

    public void setUp () throws Exception {
        doc = null;
        ctx = new Context();
        exp = null;
        nodeset = null;     // result
        result  = null;     // result as String
    }

    public void tearDown() {
    }
 
    public void testSingleLiteral () throws Exception {
        exp = new ExprParser(doc, ctx);
        assertNotNull(exp);
        Expr etree = exp.parse("'id'");
        assertNotNull(etree);
        assertSameSerialization("<string>id</string>", etree.toXml());
    }
    public void testSingleNumber () throws Exception {
        exp = new ExprParser(doc, ctx);
        assertNotNull(exp);
        Expr etree = exp.parse("125");
        assertNotNull(etree);
        assertSameSerialization("<number>125</number>", etree.toXml());
    }
    // UNION EXPR ///////////////////////////////////////////////////
    public void testUnionExpr () throws Exception {
        // syntactically correct, semantic error
        exp = new ExprParser(doc, ctx);
        assertNotNull(exp);
        Expr etree = exp.parse(" 'abc' | 'def' ");
        assertNotNull(etree);
        assertSameSerialization("<union><left><string>abc</string></left>"
                + "<right><string>def</string></right></union>",
                etree.toXml());
       
        // once again ignoring semantics
        exp = new ExprParser(doc, ctx);
        etree = exp.parse(" 'abc' | 'def' | 'ghi'");
        assertSameSerialization(
                  "<union><left>"
                + "  <union><left><string>abc</string></left>"
                + "    <right><string>def</string></right></union>"
                + "  </left>"
                + "  <right><string>ghi</string></right></union>",
                etree.toXml());
    }
    // UNARY EXPR ///////////////////////////////////////////////////
    public void testUnaryExpr () throws Exception {
        exp = new ExprParser(doc, ctx);
        assertNotNull(exp);
        Expr etree = exp.parse("-125");
        assertNotNull(etree);
        assertSameSerialization(
                "<minus><number>125</number></minus>", 
                etree.toXml());

        // no folding yet, please
        exp = new ExprParser(doc, ctx);
        etree = exp.parse("- -125");
        assertNotNull(etree);
        assertSameSerialization(
                "<minus><minus><number>125</number></minus></minus>", 
                etree.toXml());

        etree =  new ExprParser(doc, ctx).parse("- - -125");
        assertSameSerialization(
        "<minus><minus><minus><number>125</number></minus></minus></minus>", 
                etree.toXml());
        
        // ignoring the semantics
        etree =  new ExprParser(doc, ctx).parse("- 125 | 3");
        assertSameSerialization(
        "<minus><union><left><number>125</number></left>"
        + "<right><number>3</number></right></union></minus>", 
                etree.toXml());
    } 
    // MULTIPLICATIVE EXPR //////////////////////////////////////////
    public void testMultiplicativeExpr () throws Exception {
        exp = new ExprParser(doc, ctx);
        assertNotNull(exp);
        Expr etree = exp.parse("125 + 3");
        assertNotNull(etree);
        assertSameSerialization(
                "<plus><left><number>125</number></left>"
                + "<right><number>3</number></right></plus>",
                etree.toXml());

        exp = new ExprParser(doc, ctx);
        assertNotNull(exp);
        etree = exp.parse("125 + - 3");
        assertNotNull(etree);
        assertSameSerialization(
                "<plus><left><number>125</number></left>"
                + "<right><minus><number>3</number></minus></right></plus>",
                etree.toXml());


    }
    // PRECEDENCE HANDLING //////////////////////////////////////////
    public void testComplexExpr () throws Exception {
        exp = new ExprParser(doc, ctx);
        assertNotNull(exp);
        Expr etree = exp.parse("119 + 3 | 7 * 13");
        assertNotNull(etree);
        assertSameSerialization(
                "<plus><left><number>119</number></left>"
                + "<right><mul><left><union>"
                +   "<left><number>3</number></left>"
                +   "<right><number>7</number></right></union></left>"
                +   "<right><number>13</number></right>"
                + "</mul></right></plus>" ,
                etree.toXml());
    }
    public void testComplexExpr2 () throws Exception {
        exp = new ExprParser(doc, ctx);
        assertNotNull(exp);
        Expr etree = exp.parse("119 * 3 | 7 + 13");
        assertNotNull(etree);
        assertSameSerialization(
                "<plus><left><mul><left><number>119</number></left>"
                + "<right><union>"
                +   "<left><number>3</number></left>"
                +   "<right><number>7</number></right></union></right>"
                +   "</mul></left>"
                +   "<right><number>13</number></right></plus>",
                etree.toXml());
    }
    public void testMissingLeftSubExpr() throws Exception {
        exp = new ExprParser(doc, ctx);
        assertNotNull(exp);
        try {
            Expr etree = exp.parse(" + 119 ");
            fail("didn't catch missing left subexpression");
        } catch ( CoreXmlException cxe ) { /* success */ }

        // NEED MORE EXTENSIVE TESTING 
    }
    public void testMissingRightSubExpr() throws Exception {
        exp = new ExprParser(doc, ctx);
        assertNotNull(exp);
        try {
            Expr etree = exp.parse("119 * ");
            fail("didn't catch missing right subexpression");
        } catch ( CoreXmlException cxe ) { /* success */ }

        // NEED MORE EXTENSIVE TESTING 
    }
}
