/* TestExprParser.java */
package org.xlattice.corexml.expr;

import java.io.StringReader;
import junit.framework.*;

import org.xlattice.Context;
import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

/**
 * @author Jim Dixon
 */

public class TestExprParser extends CoreXmlTestCase {

    private XmlParser  xp;    
    private StringReader reader;
    private Document   doc;
    private Context    ctx;
    private ExprParser parser;
    private Expr       result;

    static final String x = 
        "<project>" 
        +   "<id>corexml</id>"
        +   "<name>CoreXml</name>"
        +   "<version>0.1a1</version>"
        +   "<orgName>The Xlattice Project</orgName>"
        +   "<startYear>2004</startYear>"
        +   "<pkg>org.xlattice.corexml</pkg>"
        +   "<desc>An implementation of various XML facilities.</desc>"
        +   "<url>http://xlattice.sourceforge.net/CoreXml</url>"
        +   "<dependencies>"
        +   "   <dependency>"
        +   "       <groupId>ant</groupId>"
        +   "       <id>ant</id>"
        +   "       <version>1.5.4</version>"
        +   "       <artifact>jar</artifact>"
        +   "       <url>http://jakarta.apache.org/ant/</url>"
        +   "   </dependency>" 
        +   "   <dependency>"
        +   "       <groupId>ant</groupId>"
        +   "       <id>optional</id>"
        +   "       <version>1.5.4</version>"
        +   "       <artifact>jar</artifact>"
        +   "       <url>http://jakarta.apache.org/ant/</url>"
        +   "   </dependency>" 
        +   "   <dependency>"
        +   "       <groupId>junit</groupId>"
        +   "       <id>junit</id>"
        +   "       <version>3.8.1</version>"
        +   "       <artifact>jar</artifact>"
        +   "       <url>http://www.junit.org/</url>"
        +   "   </dependency>" 
        +   "</dependencies>"
        +"</project>";


    public TestExprParser (String name) {
        super(name);
    }

    public void setUp () throws Exception {
        doc = new XmlParser(new StringReader(x)).read();
        ctx = new Context();
        parser = null;
        result  = null;   
    }

    public void tearDown() {
    }
 
    public void testEmptyExpr () throws Exception {
        parser = new ExprParser(null, null);
        assertNotNull(parser);
        assertNull(parser.parse((String)null));
        assertNull(parser.parse(""));
        assertNull(parser.eval ((String)null));
        assertNull(parser.eval (""));
    }
    public void testEmptyExpr2 () throws Exception {
        parser = new ExprParser(doc, ctx);
        assertNotNull(parser);
        assertNull(parser.parse((String)null));
        assertNull(parser.parse(""));
        assertNull(parser.eval ((String)null));
        assertNull(parser.eval (""));
    }
    public void testLiteral () throws Exception {
        parser = new ExprParser(doc, ctx);
        assertNotNull(parser);
        result  = parser.eval("'id'");
        assertNotNull(result);
        assertEquals("id", ((Literal)result).getValue());
    }
}
