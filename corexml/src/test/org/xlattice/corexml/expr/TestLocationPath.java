/* TestLocationPath.java */
package org.xlattice.corexml.expr;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import junit.framework.*;

import org.xlattice.Context;
import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

/**
 * @author Jim Dixon
 */

public class TestLocationPath extends CoreXmlTestCase {

    private XmlParser    xp;    
    private StringReader reader;
    private Document     doc;
    private Context      ctx;
    private ExprParser   parser;
    private Expr         result;
    private LocationPath lp;
    private NodeSet      nodes;

    static final String x = 
        "<project>" 
        // KLUDGE TO DEAL WITH id BEING A RESERVED NAME
        +   "<id>corexml</id>"
        +   "<name>CoreXml</name>"
        +   "<version>0.1a1</version>"
        +   "<orgName>The XLattice Project</orgName>"
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


    public TestLocationPath (String name) {
        super(name);
    }

    public void setUp ()                        throws Exception {
        doc    = new XmlParser(new StringReader(x)).read();
        ctx    = new Context();
        parser = new ExprParser(doc, ctx);
        result = null;
        lp     = null;
        nodes  = null;
    }

    public void tearDown() {
    }

    // PARSER TESTS /////////////////////////////////////////////////
    public void testSingleNodeTestParse ()      throws Exception {
        assertNotNull(parser);
        result  = parser.parse("/");
        assertNotNull(result);
        assertTrue (result instanceof LocationPath);
        assertSameSerialization(
                "<locpath><absolute>true</absolute></locpath>", 
                result.toXml());

        result  = parser.parse("/child::project");
        assertNotNull(result);
        assertTrue (result instanceof LocationPath);
        assertSameSerialization(
                "<locpath><absolute>true</absolute>"
              + "  <step><axis>child</axis>"
              + "    <nodetest><qname>project</qname></nodetest>"
              + "    </step>"
              + "</locpath>", 
                result.toXml());

        Expr result2 = parser.parse("/project");
        assertSameSerialization(result.toXml(), result2.toXml());
    }  
    public void testTwoStepNodeTestParse ()     throws Exception {
        result  = parser.parse("/child::project/child::id");
        Expr result2 = parser.parse("/project/id");
        assertSameSerialization(result.toXml(), result2.toXml());
        assertSameSerialization(
                "<locpath><absolute>true</absolute>"
              + "  <step><axis>child</axis>"
              + "    <nodetest><qname>project</qname></nodetest>"
              + "  </step>"
              + "  <step><axis>child</axis>"
              + "    <nodetest><qname>id</qname></nodetest>"
              + "  </step>"
              + "</locpath>", 
                result.toXml());
    }  
    public void testThreeStepNodeTestParse ()   throws Exception {
        Expr result = parser.parse("/project/dependencies/dependency");
        assertSameSerialization(
                "<locpath><absolute>true</absolute>"
              + "  <step><axis>child</axis>"
              + "    <nodetest><qname>project</qname></nodetest>"
              + "  </step>"
              + "  <step><axis>child</axis>"
              + "    <nodetest><qname>dependencies</qname></nodetest>"
              + "  </step>"
              + "  <step><axis>child</axis>"
              + "    <nodetest><qname>dependency</qname></nodetest>"
              + "  </step>"
              + "</locpath>", 
                result.toXml());
    } 

    /////////////////////////////////////////////////////////////////
    // EVALUATION TESTS /////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////
    
    // STEP TESTS - we will need a LOT of these tests /////////////// 
    public void testChildStepFromRoot ()        throws Exception {
        // build LocationPath manually
        lp = new LocationPath (true);  // absolute
        assertNotNull(lp);
        assertEquals (0, lp.stepCount());
        NodeTest nodeTest = new NodeTest(new QName("project"));
        Step step = new Step (AxisName.CHILD, nodeTest, true);
        lp.addStep(step);
        assertEquals (1, lp.stepCount());
        assertSameSerialization(
                 "<locpath><absolute>true</absolute>" +
                 "  <step> <axis>child</axis>" +
                 "    <nodetest><qname>project</qname></nodetest>" +
                 "  </step>" +
                 "</locpath>",
                lp.toXml());
        
        // confirm that parser returns an equivalent location path
        Expr result2  = parser.parse("/project");
        assertTrue(result2 instanceof LocationPath);
        LocationPath lp2 = (LocationPath)result2;
        assertSameSerialization(lp.toXml(), lp2.toXml());
       
        // XXX UNSAFE - depends upon details of implementation
        NodeSet collected = new NodeSet();
        ArrayList steps = new ArrayList();
        steps.add(step);
        parser.takeStep (doc, collected, steps);
        assertEquals (1, collected.size()); 
        Iterator it = collected.iterator();
        while (it.hasNext()) {
            Node found = (Node)it.next();
            assertTrue(found instanceof Element);
            assertEquals("project", ((Element)found).getName());
        }
    } 
    public void testTwoChildStepFromRoot ()     throws Exception {
        lp = new LocationPath (true);  // absolute
        assertNotNull(lp);
        assertEquals (0, lp.stepCount());
        NodeTest nodeTest1 = new NodeTest(new QName("project"));
        Step step1 = new Step (AxisName.CHILD, nodeTest1, false);
        lp.addStep(step1);
        NodeTest nodeTest2 = new NodeTest(new QName("orgName"));
        Step step2 = new Step (AxisName.CHILD, nodeTest2, false);
        lp.addStep(step2);
        assertEquals (2, lp.stepCount());
       
        assertSameSerialization(
                 "<locpath><absolute>true</absolute>" +
                 "  <step> <axis>child</axis>" +
                 "    <nodetest><qname>project</qname></nodetest>" +
                 "  </step>" +
                 "  <step> <axis>child</axis>" +
                 "    <nodetest><qname>orgName</qname></nodetest>" +
                 "  </step>" +
                 "</locpath>",
                lp.toXml());
        
        NodeSet collected = new NodeSet();
        ArrayList steps = new ArrayList();
        steps.add(step1);
        parser.takeStep (doc, collected, steps);
        assertEquals (1, collected.size());
        Iterator it = collected.iterator();
        Node found = null;
        while (it.hasNext()) {
            found = (Node)it.next();
            assertTrue(found instanceof Element);
            assertEquals("project", ((Element)found).getName());
        }
        parser.setNode(found);
        assertEquals (found, parser.getNode());
        steps.clear();
        steps.add(step2);
        collected.clear();
        parser.takeStep (found, collected, steps);
        assertEquals (1, collected.size());
        Iterator it2 = collected.iterator();
        while (it2.hasNext()) {
            found = (Node)it2.next();
            assertTrue(found instanceof Element);
            String name = ((Element)found).getName();
            assertEquals ("orgName", name);
        }
        assertEquals ("The XLattice Project", parser.string(found)); 
        assertEquals ("The XLattice Project", parser.string(collected));
    }

    
    // EVAL TESTS ///////////////////////////////////////////////////
    public void testSingleNodeTestEval ()       throws Exception {
        result  = parser.parse("/project");
        assertTrue(result instanceof LocationPath);
        lp = (LocationPath)result;
        assertSameSerialization(
                 "<locpath><absolute>true</absolute>" +
                 "  <step> <axis>child</axis>" +
                 "    <nodetest><qname>project</qname></nodetest>" +
                 "  </step>" +
                 "</locpath>",
                lp.toXml());
        
        assertTrue(lp.isAbsolute());
        assertEquals(1, lp.stepCount());
        Step step = lp.getStep(0);
        
        result  = parser.eval (result);
        assertTrue(result instanceof NodeSet);
        nodes = (NodeSet)result;
        assertEquals (1, nodes.size());
        Iterator it = nodes.iterator();
        Node found = null;
        while (it.hasNext()) {
            found = (Node)it.next();
            assertTrue(found instanceof Element);
            String name = ((Element)found).getName();
            assertEquals ("project", name);
        }
    } 
    public void testNodeTestEval2 ()            throws Exception {
        result  = parser.parse("/project/orgName");
        assertTrue(result instanceof LocationPath);
        lp = (LocationPath)result;
        assertTrue(lp.isAbsolute());
        assertEquals(2, lp.stepCount());
        assertSameSerialization(
                 "<locpath><absolute>true</absolute>" +
                 "  <step> <axis>child</axis>" +
                 "    <nodetest><qname>project</qname></nodetest>" +
                 "  </step>" +
                 "  <step> <axis>child</axis>" +
                 "    <nodetest><qname>orgName</qname></nodetest>" +
                 "  </step>" +
                 "</locpath>",
                lp.toXml());
        
        result  = parser.eval (result);
        assertTrue(result instanceof NodeSet);
        nodes = (NodeSet)result;
        assertEquals (1, nodes.size()); 
        Iterator it = nodes.iterator();
        Node found = null;
        while (it.hasNext()) {
            found = (Node)it.next();
            assertTrue(found instanceof Element);
            String name = ((Element)found).getName();
            assertEquals ("orgName", name);
        }
        assertEquals("The XLattice Project", parser.string(nodes));
    } 
    public void testNodeTestEval3 ()            throws Exception {
        result  = parser.parse("/project/dependencies/dependency");
        lp      = (LocationPath)result;
        assertEquals(3, lp.stepCount());
        assertSameSerialization(
                 "<locpath><absolute>true</absolute>" +
                 "  <step> <axis>child</axis>" +
                 "    <nodetest><qname>project</qname></nodetest>" +
                 "  </step>" +
                 "  <step> <axis>child</axis>" +
                 "    <nodetest><qname>dependencies</qname></nodetest>" +
                 "  </step>" +
                 "  <step> <axis>child</axis>" +
                 "    <nodetest><qname>dependency</qname></nodetest>" +
                 "  </step>" +
                 "</locpath>",
                lp.toXml());
        
        result  = parser.eval (result);
        assertTrue(result instanceof NodeSet);
        nodes = (NodeSet)result;
        assertEquals (3, nodes.size()); 
        Iterator it = nodes.iterator();
        Node found = null;
        while (it.hasNext()) {
            found = (Node)it.next();
            assertTrue(found instanceof Element);
            String name = ((Element)found).getName();
            assertEquals ("dependency", name);
        }
        assertNotNull(found);
    }
    // 2004-07-20: capable of handling only this limited type of 
    // predicate (selection of element node by index on element nodes)
    public void testPosition()                  throws Exception {
        result  = parser.parse("/project/dependencies/dependency[1]/id");
        lp = (LocationPath)result;
        result  = parser.eval (result);
        assertTrue(result instanceof NodeSet);
        nodes = (NodeSet)result;
        assertEquals (1, nodes.size()); 
        assertEquals("ant", parser.string(nodes));

        assertEquals("optional", parser.string(parser.eval(
                            "/project/dependencies/dependency[2]/id")));
        assertEquals("junit", parser.string(parser.eval(
                            "/project/dependencies/dependency[3]/id")));
        assertEquals("3.8.1", parser.string( parser.eval(
                            "/project/dependencies/dependency[3]/version")));

    } 
}
