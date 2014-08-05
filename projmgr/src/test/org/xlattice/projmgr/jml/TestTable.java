/* TestTable.java */
package org.xlattice.projmgr.jml;

import java.io.StringReader;
import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

public class TestTable extends CoreXmlTestCase {

    private JmlLexer lexer;
    private JmlContext ctx;
    private Document   doc;
    
    public TestTable (String name) {
        super(name);
    }
    public void setUp() {
        lexer = null;
        ctx   = new JmlContext();
        doc   = new Document();
    }
    public void tearDown() {
    }
    public void testTodoList()                  throws Exception { 
        doc = new Document();
        String todo = new StringBuffer("To Do List\n\n")
            // XXX add author
            .append("[\n*what*  | *urgency* | *details* ||\n\n\n")
            .append("review testing |  high  |") 
            .append("JXCL and UCovered testing needs a thorough review. \n")
            .append("This should begin in JXCL and then progress \n")
            .append("to UCovered after JXCL is known to be bug-free.\n") 
            .append("]\n\n\n").toString();  // should eat superfluous NL
        
        new JmlLexer (ctx, new StringReader(todo))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document><properties><title>To Do List</title>"
              + "  </properties><body>"
              + "  <p></p><table>"
              + "    <tr><td><b>what</b></td><td><b>urgency</b></td>"
              + "      <td><b>details</b></td></tr>"
              + "    <tr><td>review testing</td><td>high</td>"
              + "      <td>JXCL and UCovered testing needs a thorough review."
              + "        This should begin in JXCL and then progress "
              + "        to UCovered after JXCL is known to be bug-free."
              + "      </td></tr></table>"
              + "<p></p></body></document>"
                , doc.toXml());
    } 

    // TEST IMMEDIATELY ADJACENT TABLES
    public void testAdjacentTables()                throws Exception { 
        doc = new Document();
        String todo = new StringBuffer("Adjacent Tables\n\n")
            .append("[\na\n]\n[\nb\n]\n").toString();
        
        new JmlLexer (ctx, new StringReader(todo))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document><properties><title>Adjacent Tables</title>"
              + "  </properties><body>"
              + "  <p></p><table><tr><td>a</td></tr></table>"
              + "  <table><tr><td>b</td></tr></table>"
              + "</body></document>"
                , doc.toXml());
    } // GEEP
    // TEST TABLE STARTING IMMEDIATELY AFTER SECTION HEADING
    //
}
