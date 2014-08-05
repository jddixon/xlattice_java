/* TestXdocTarget.java */
package org.xlattice.projmgr.jml;

import java.io.StringReader;
import junit.framework.*;

/**
 * @author Jim Dixon
 */

import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

/**
 * @todo add tests of %c, %bind, variables, and other functions
 */
public class TestXdocTarget extends CoreXmlTestCase {

    private JmlLexer lexer;
    private JmlContext ctx;
    private Document   doc;
    
    public TestXdocTarget (String name) {
        super(name);
    }
    public void setUp() {
        lexer = null;
        ctx   = new JmlContext();
        doc   = new Document();
    }
    public void tearDown() {
    }
    public void testEmptyInput()                throws Exception{ 
        new JmlLexer (ctx, new StringReader(""))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document><properties/><body/></document>"
                , doc.toXml());
    }
    // TITLE ////////////////////////////////////////////////////////
    public void testTitle()                     throws Exception {
        new JmlLexer (ctx, new StringReader("hi there\n"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>hi there</title></properties>"
              // OPTIMIZE LATER?
              + "<body><p></p></body></document>"
                , doc.toXml());
    } 
    // FONT MODIFIERS ///////////////////////////////////////////////
    // no newline, so not seen as title line
    public void testBold()                      throws Exception {
        new JmlLexer (ctx, new StringReader("*hi there*"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document><properties/>"
              + "<body><p><b>hi there</b></p></body></document>"
                , doc.toXml());
    } 
    public void testItalic()                      throws Exception {
        new JmlLexer (ctx, new StringReader("_hi there_"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document><properties/>"
              + "<body><p><i>hi there</i></p></body></document>"
                , doc.toXml());
    } 
    public void testTT()                      throws Exception {
        new JmlLexer (ctx, new StringReader("`hi there`"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document><properties/>"
              + "<body><p><tt>hi there</tt></p></body></document>"
                , doc.toXml());
    } 
    public void testBlockquote()                throws Exception {
        // flagged by SPACE-DQUOTE at line start
        new JmlLexer (ctx, new StringReader("abc\n  \"hi there\""))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>abc</title></properties>"
              + "<body><p></p>"
              + "<blockquote>hi there</blockquote></body></document>"
                , doc.toXml());
    }  
    public void testCode()                      throws Exception {
        // flagged by LT-LT at line start
        new JmlLexer (ctx, new StringReader("abc\n{{hi there}}"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>abc</title></properties>"
              + "<body><p></p>"
              + "<source><![CDATA[\nhi there]]></source></body>"
              + "</document>"
                , doc.toXml());
    }  
  
    // LINKS ////////////////////////////////////////////////////////
    // XXX A BUG? link is not recognized on first (title) line
    public void testLinkFails()                 throws Exception {
        new JmlLexer (ctx, new StringReader("@(hi there)"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties/>"
              + "<body><p>@(hi there)</p></body>"
              + "</document>"
                , doc.toXml());
    }  
    public void testLink()                      throws Exception {
        new JmlLexer (ctx, new StringReader("\n@(hi there)"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties/>"
              + "<body><p><a href=\"hi there\">hi there</a></p></body>"
              + "</document>"
                , doc.toXml());
    }  
    public void testLinkWithTag()               throws Exception {
        new JmlLexer (ctx, new StringReader("\n@(hi there, tag)"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties/>"
              + "<body><p><a href=\"hi there\">tag</a></p></body>"
              + "</document>"
                , doc.toXml());
    } 
    // LINE BREAKS, PARAGRAPHS //////////////////////////////////////
    public void testLineBreak()                 throws Exception {
        new JmlLexer (ctx, new StringReader("abc\nline1\\\nline2"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>abc</title></properties><body>"
              + "<p>line1<br/>line2</p>"
              + "</body></document>"
                , doc.toXml());
        
    }
    public void testPara()                      throws Exception {
        new JmlLexer (ctx, new StringReader("abc\nline1\n\nline2"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>abc</title></properties><body>"
              + "<p>line1</p><p>line2</p>"
              + "</body></document>"
                , doc.toXml());
        
    }
    // CURRENTLY INTERPRETED AS PARAGRAPH
    public void testBlankLine()                 throws Exception {
        new JmlLexer (ctx, new StringReader("abc\nline1\n\\\nline2"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>abc</title></properties><body>"
              + "<p>line1</p><p>line2</p>"
              + "</body></document>"
                , doc.toXml());
        
    }
    // COMBINATIONS /////////////////////////////////////////////////
    public void testSimpleInput()               throws Exception {
        new JmlLexer (ctx, new StringReader(
                "abc\n"                 // title
                + "_def_\\\n"           // italic + line break
                + "*oh hello*"))        // bold
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>abc</title></properties><body>"
              + "<p><i>def</i><br/><b>oh hello</b></p>"
              + "</body></document>"
                , doc.toXml());
    }
    // TABLES ///////////////////////////////////////////////////////
    public void testTables()                   throws Exception {
        new JmlLexer (ctx, new StringReader(
                      "xxx\n[\n"
                    + "col 11 | col 12 | col 13 ||"
                    + "col 21 | col 22 | col 23 ||"
                    + "col 31 | col 32 | col 33 \n"
                    + "]\n"
                    + "line after table"
                ))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>xxx</title></properties><body>"
              + "<p></p>"
              + "<table>"
              + "  <tr><td>col 11</td><td>col 12</td><td>col 13</td></tr>"
              + "  <tr><td>col 21</td><td>col 22</td><td>col 23</td></tr>"
              + "  <tr><td>col 31</td><td>col 32</td><td>col 33</td></tr>"
              + "</table>"
              + "<p>line after table</p>"
              + "</body></document>"
                , doc.toXml());
        
    }

    // SECTIONS /////////////////////////////////////////////////////
    public void testSectionsOneDeep()           throws Exception {
        new JmlLexer (ctx, new StringReader(
                "abc\n"                 // title
                + "# section A\n"       // italic + line break
                + "oh hello"))
            .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>abc</title></properties><body>"
              + "<p></p>"
              + "<section name=\"section A\"><p>oh hello</p></section>"
              + "</body></document>"
                , doc.toXml());
    }
    public void testMultipleSections()          throws Exception {
        new JmlLexer (ctx, new StringReader(
                "abc\n"                 // title
                + "# section A\n"       // italic + line break
                + "oh hello\n"
                + "## section A.1\n"
                + "tick tock\n"
                + "# section B\n"
                + "the mouse ...\n"
                + "## section B.1\n"
                + "ran up ...\n"
                // different from Mock test
                + "## section B.2\n"
                + "the clock"))
            .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>abc</title></properties><body>"
              + "<p></p>"
              + "<section name=\"section A\"><p>oh hello</p>"
              + "  <subsection name=\"section A.1\">"
              + "    <p>tick tock</p></subsection>"
              + "</section>"
              + "<section name=\"section B\"><p>the mouse ...</p>"
              + "  <subsection name=\"section B.1\">"
              + "    <p>ran up ...</p></subsection>"
              + "  <subsection name=\"section B.2\">"
              + "    <p>the clock</p></subsection>"
              + "</section>"
              + "</body></document>"
              
                , doc.toXml());
    }
    // LISTS ////////////////////////////////////////////////////////
  
    public void testUnnumberedLists()           throws Exception {
        new JmlLexer (ctx, new StringReader(
                      "xxx\n"
                    + "random text\n"
                    + "o first item\n"
                    + "o second item\n"
                    // XXX CHECK EFFECT OF TRAILING SLASH
                    + "o third item\n\n"
                    + "line after list"
                ))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>xxx</title></properties><body>"
              + "<p>random text</p>"
              + "<ul><li>first item</li><li>second item</li>"
              + "  <li>third item</li></ul>"
              + "<p>line after list</p>"
              + "</body></document>"
                , doc.toXml());
    }

    public void testNumberedLists()           throws Exception {
        new JmlLexer (ctx, new StringReader(
                      "xxx\n"
                    + "random text\n"
                    + "1 first item\n"
                    + "1 second item\n"
                    // XXX CHECK EFFECT OF TRAILING SLASH
                    + "1 third item\n\n"
                    + "line after list"
                ))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>xxx</title></properties><body>"
              + "<p>random text</p>"
              + "<ol><li>first item</li><li>second item</li>"
              + "  <li>third item</li></ol>"
              + "<p>line after list</p>"
              + "</body></document>"
                , doc.toXml());
    } 
    // **************************************************
    // XXX NEED CHECKS ON NESTED LISTS OF DIFFERENT TYPES
    // **************************************************


    // MESSY COMBINATION INVOLVING SECTIONS ****
    public void testMoreSimple()                throws Exception {
        new JmlLexer (ctx, new StringReader(
                "TITLE\n"
              + "the link is @(http://www.xlattice.org) \n"
              + "# section 1\n"
              + "`a = b + c`\n" 
              + "## section 1.1\n"
              + "random text \n"
              // section numbering changed from Mock test
              + "## section 1.2\n"
              + "more _random_ text \n"
              + "## section 1.3\n"
              + "more *truly* random text but "
              + "the next character is escaped: \\# \n"))
                .interpret(new XdocTarget(doc));
        assertSameSerialization(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<document>"
              + "<properties><title>TITLE</title></properties><body>"
              + "<p>the link is <a href=\"http://www.xlattice.org\">"
              + "  http://www.xlattice.org</a></p>"
              + "<section name=\"section 1\"><p><tt>a = b + c</tt></p>"
              + "  <subsection name=\"section 1.1\"><p>random text</p>"
              + "    </subsection>"
              + "  <subsection name=\"section 1.2\"><p>more <i>random</i>"
              + "    text</p></subsection>"
              + "  <subsection name=\"section 1.3\"><p>more <b>truly</b>"
              + "    random text but the next character is escaped: #</p>"
              + "    </subsection>"
              + "</section>"
              + "</body></document>"
              , doc.toXml());
    }
}
