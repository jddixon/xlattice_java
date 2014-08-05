/* TestChrFunc.java */
package org.xlattice.projmgr.jml;

import java.io.StringReader;
import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

public class TestChrFunc extends CoreXmlTestCase {

    private JmlLexer lexer;
    private JmlContext ctx;
    private Document   doc;
    
    public TestChrFunc (String name) {
        super(name);
    }
    public void setUp() {
        lexer = null;
        ctx   = new JmlContext();
        doc   = new Document();
    }
    public void tearDown() {
    }
    private void checkOneChr(String in, String out) throws Exception { 
        // ERRORS IF NOT A new Document() HERE -- suggests something
        // wrong with the API
        doc = new Document();
        new JmlLexer (ctx, new StringReader(
                    "mock title\n%c(" + in + ")"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document><properties><title>mock title</title>"
              + "  </properties><body>"
              + "<p>" + out + "</p>"
              + "</body></document>"
                , doc.toXml());
    }
    public void testSeveralChr()                    throws Exception {
        checkOneChr("union",        "&#8746;");
        checkOneChr("implies",      "&#8658;");
        checkOneChr("infinity",     "&#8734;");
    }
}
