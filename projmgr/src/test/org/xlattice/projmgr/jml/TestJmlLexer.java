/* TestJmlLexer.java */
package org.xlattice.projmgr.jml;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author Jim Dixon
 */

import junit.framework.*;

import org.xlattice.corexml.CoreXmlTestCase;
import org.xlattice.corexml.om.*;

/**
 * Tests of JmlLexer functionality using the MockInterpreter.
 * 
 * TESTED ELSEWHERE:
 * <ul>
 *   <li>%c function in TestChr</li>
 * </ul>
 * 
 * NOT TESTED:
 * <ul>
 *   <li>nested interpreters </li>
 *   <li>%bind</li>
 *   <li>%raw</li>
 *   <li>other functions</li>
 *   <li>variables</li>
 * </ul>
 *
 * INADEQUATELY TESTED:
 *  <ul>
 *    <li>nested and mixed lists</li>
 *  </ul>
 */
public class TestJmlLexer extends CoreXmlTestCase {

    private JmlLexer lexer;
    private JmlContext ctx;
    private StringReader in;
    private StringWriter out;
    
    public TestJmlLexer (String name) {
        super(name);
    }
    public void setUp() {
        lexer = null;
        ctx   = new JmlContext();
        in    = null;
        out   = new StringWriter();
    }
    public void tearDown() {
    }
    public void testEmptyInput()                throws Exception{ 
        new JmlLexer (ctx, new StringReader(""))
                .interpret(new MockInterpreter(out));
        assertEquals("[begin][end]", out.toString());
    }
    // TITLE ////////////////////////////////////////////////////////
    public void testTitle()                     throws Exception {
        new JmlLexer (ctx, new StringReader("hi there\n"))
                .interpret(new MockInterpreter(out));
        assertEquals("[begin][title:'hi there'][text:'\n'][end]", 
                out.toString());
    }
    public void testAuthor()                    throws Exception {
        new JmlLexer (ctx, new StringReader(
                  "abc\n"
                + "%author(fred, fred@freddy.com)\n"
                + "%author(Jim Dixon, jdd@dixons.org)\n"  
                ))
                .interpret(new MockInterpreter(out));
        assertEquals(
                "[begin][title:'abc']"
              + "[author:'fred':'fred@freddy.com']"
              + "[author:'Jim Dixon':'jdd@dixons.org']"
              + "[text:'\n\n\n']"
              + "[end]", 
                out.toString());
    }
    // FONT MODIFIERS ///////////////////////////////////////////////
    public void testBold()                      throws Exception {
        new JmlLexer (ctx, new StringReader("*hi there*"))
                .interpret(new MockInterpreter(out));
        assertEquals("[begin][bold:'hi there'][end]", out.toString());
    }        
    public void testItalic()                    throws Exception {
        new JmlLexer (ctx, new StringReader("_hi there_"))
                .interpret(new MockInterpreter(out));
        assertEquals("[begin][italic:'hi there'][end]", out.toString());
    }      
    // 
    public void testTT()                        throws Exception {
        new JmlLexer (ctx, new StringReader("`hi there`"))
                .interpret(new MockInterpreter(out));
        assertEquals("[begin][tt:'hi there'][end]", out.toString());
    }       
    // TEXT BLOCKS //////////////////////////////////////////////////
    // blockquote flagged by SPACE-DQUOTE at start of line
    public void testBlockquote()                throws Exception {
        new JmlLexer (ctx, new StringReader("abc\n  \"hi there\""))
                .interpret(new MockInterpreter(out));
        assertEquals("[begin][title:'abc'][text:'\n']"+
                "[blockquote:'hi there'][end]", 
                out.toString());
    }        
    public void testCode()                throws Exception {
        new JmlLexer (ctx, new StringReader("abc\n{{hi there}}"))
                .interpret(new MockInterpreter(out));
        assertEquals("[begin][title:'abc'][text:'\n']"+
                "[code:'hi there'][end]", 
                out.toString());
    }        
    
    // LINKS ////////////////////////////////////////////////////////
    // link is not recognized on first (title) line
    public void testLinkFails()                 throws Exception {
        new JmlLexer (ctx, new StringReader("@(hi there)"))
                .interpret(new MockInterpreter(out));
        assertEquals(
                "[begin][text:'@(hi there)'][end]", 
                out.toString());
    }       
    public void testLink()                      throws Exception {
        new JmlLexer (ctx, new StringReader("\n@(hi there)"))
                .interpret(new MockInterpreter(out));
        assertEquals(
                "[begin][text:'\n'][link:'hi there'][end]", 
                out.toString());
    }       
    public void testLinkWithTag()               throws Exception {
        new JmlLexer (ctx, new StringReader("\n@(hi there, tag)"))
                .interpret(new MockInterpreter(out));
        assertEquals(
                "[begin][text:'\n'][link:'hi there:tag'][end]", 
                out.toString());
    }       
    // LINE BREAKS, PARAGRAPHS //////////////////////////////////////
    public void testLineBreaks()                throws Exception {
        new JmlLexer (ctx, new StringReader("abc\nline1\\\nline2"))
                .interpret(new MockInterpreter(out));
        assertEquals(
                "[begin][title:'abc'][text:'\nline1'][linebreak]"
                + "[text:'line2'][end]", 
                out.toString());
        
    }
    // ********************************
    // BLANK LINE, PARAGRAPH NOT TESTED
    // ********************************


    // COMBINATIONS /////////////////////////////////////////////////
    public void testSimpleInput()               throws Exception {
        in = new StringReader(
                "abc\n"                 // title
                + "_def_\\\n"           // italic + line break
                + "*oh hello*");        // bold
        lexer = new JmlLexer (new JmlContext(), in);
        lexer.interpret(new MockInterpreter(out));
        assertEquals(
                  "[begin]"
                + "[title:'abc'][text:'\n']"
                + "[italic:'def']"
                + "[linebreak]"
                + "[bold:'oh hello']"
                + "[end]", out.toString());
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
                .interpret(new MockInterpreter(out));
        assertEquals(
                  "[begin][title:'xxx'][text:'\n']"
                + "[beginTable]"
                + "[text:'col 11 '][col][text:' col 12 ']"
                +    "[col][text:' col 13 '][row]"
                + "[text:'col 21 '][col][text:' col 22 ']"
                +    "[col][text:' col 23 '][row]"
                + "[text:'col 31 '][col][text:' col 32 ']"
                +    "[col][text:' col 33 \n']"
                + "[endTable]"
                + "[text:'line after table'][end]", 
                out.toString());
        
    } 
    public void testSmallColumns()              throws Exception {
        new JmlLexer (ctx, new StringReader(
                      "xxx\n[\n"
                    + "a | b | c ||"
                    + "d | e | f ||"
                    + "g | h | i \n"
                    + "]\n"
                ))
                .interpret(new MockInterpreter(out));
        assertEquals(
                  "[begin][title:'xxx'][text:'\n']"
                + "[beginTable]"
                + "[text:'a '][col][text:' b '][col][text:' c '][row]"
                + "[text:'d '][col][text:' e '][col][text:' f '][row]"
                + "[text:'g '][col][text:' h '][col][text:' i \n']"
                + "[endTable][end]",
                out.toString());
        
    } // GEEP

    // SECTIONS /////////////////////////////////////////////////////
    // XXX MISSING SECTION LINE MEANS NO HTML OUTPUT FROM XDOCS
    // XXX Need to detect text before section heading and warn -
    // XXX or generate appropriate formatting 
    public void testSectionsOneDeep()           throws Exception {
        in = new StringReader(
                "abc\n"                     // title
                + "# section A\n\n\n"       // one extra NL should be eaten
                + "oh hello");
        lexer = new JmlLexer (new JmlContext(), in);
        lexer.interpret(new MockInterpreter(out));
        assertEquals(
                  "[begin]"
                + "[title:'abc'][text:'\n']"
                + "[beginSectionList:1]"
                + "[beginSection:'section A':1]"
                + "[text:'\noh hello']"
                + "[endSection:1]"
                + "[endSectionList:1]"
                + "[end]"
                , out.toString());
    }
    public void testMultipleSections()          throws Exception {
        in = new StringReader(
                "abc\n"                 // title
                + "# section A\n"       // italic + line break
                + "oh hello\n"
                + "## section A.1\n"
                + "tick tock\n"
                + "# section B\n"
                + "the mouse ...\n"
                + "## section B.1\n"
                + "ran up ...\n"
                + "### section B.1.a\n"
                + "the clock"
        );
        lexer = new JmlLexer (new JmlContext(), in);
        lexer.interpret(new MockInterpreter(out));
        assertEquals(
                  "[begin]"
                + "[title:'abc'][text:'\n']"
                + "[beginSectionList:1]"
                + "[beginSection:'section A':1]"
                + "[text:'oh hello\n']"
                + "[beginSectionList:2]"
                + "[beginSection:'section A.1':2]"
                + "[text:'tick tock\n']"
                + "[endSection:2]"
                + "[endSectionList:2]" 
                + "[endSection:1]"
                // NOTE BEHAVIOUR:
         //     + "[endSectionList:1]" 
         //     + "[beginSectionList:1]"
                + "[beginSection:'section B':1]"
                + "[text:'the mouse ...\n']"
                + "[beginSectionList:2]"
                + "[beginSection:'section B.1':2]"
                + "[text:'ran up ...\n']"
                + "[beginSectionList:3]"
                + "[beginSection:'section B.1.a':3]"
                + "[text:'the clock']"
                + "[endSection:3]"
                + "[endSectionList:3]" 
                + "[endSection:2]"
                + "[endSectionList:2]" 
                + "[endSection:1]"
                + "[endSectionList:1]" 
                + "[end]"
                , out.toString());
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
                .interpret(new MockInterpreter(out));
        assertEquals(
                  "[begin][title:'xxx'][text:'\nrandom text\n']"
                + "[beginUL:1]"
                // first character after 'o' is eaten
                + "[beginListItem:1][text:'first item\n'][endListItem:1]"
                + "[beginListItem:1][text:'second item\n'][endListItem:1]"
                + "[beginListItem:1][text:'third item\n']"
                + "[endUL:1]"
                + "[para][text:'line after list'][end]", 
                out.toString());
        
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
                .interpret(new MockInterpreter(out));
        assertEquals(
                  "[begin][title:'xxx'][text:'\nrandom text\n']"
                + "[beginOL:1]"
                // first character after 'o' is eaten
                + "[beginListItem:1][text:'first item\n'][endListItem:1]"
                + "[beginListItem:1][text:'second item\n'][endListItem:1]"
                + "[beginListItem:1][text:'third item\n']"
                + "[endOL:1]"
                + "[para][text:'line after list'][end]", 
                out.toString());
        
    } 
    // **************************************************
    // XXX NEED CHECKS ON NESTED LISTS OF DIFFERENT TYPES
    // **************************************************
    



    // MESSY COMBINATION INVOLVING SECTIONS ****
    public void testMoreSimple()                throws Exception {
        in = new StringReader(
                "TITLE\n"
              + "the link is @(http://www.xlattice.org) \n"
              + "# section 1\n"
              + "`a = b + c`\n" 
              + "## section 1.1\n"
              + "random text \n"
              + "### section 1.1.1\n"
              + "more _random_ text \n"
              + "#### section 1.1.1.1\n"
              + "more *truly* random text but "
              + "the next character is escaped: \\# \n")
            ;
        lexer = new JmlLexer (new JmlContext(), in);
        lexer.interpret(new MockInterpreter(out));
        assertEquals(
               //....x....1....x....2....x....3....x....4....x....5
                "[begin][title:'TITLE']"
              + "[text:'\nthe link is ']"
              + "[link:'http://www.xlattice.org']"
              + "[text:' \n']"
              + "[beginSectionList:1][beginSection:'section 1':1]"
              + "[tt:'a = b + c'][text:'\n']"
              + "[beginSectionList:2][beginSection:'section 1.1':2]"
              + "[text:'random text \n']"
              + "[beginSectionList:3][beginSection:'section 1.1.1':3]"
              + "[text:'more '][italic:'random'][text:' text \n']"
              + "[beginSectionList:4][beginSection:'section 1.1.1.1':4]"
              + "[text:'more '][bold:'truly'][text:' random text but "
              + "the next character is escaped: # \n']"
              + "[endSection:4][endSectionList:4]"
              + "[endSection:3][endSectionList:3]"
              + "[endSection:2][endSectionList:2]"
              + "[endSection:1][endSectionList:1][end]"
              , out.toString());
    }
}
