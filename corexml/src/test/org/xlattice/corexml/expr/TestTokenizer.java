/* TestTokenizer.java */
package org.xlattice.corexml.expr;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

public class TestTokenizer extends TestCase {

    public Tokenizer lexer;
    public Tokenizer lexer1;
    public Tokenizer lexer2;
    public Tokenizer lexer3;
    public Token     token;
    
    public TestTokenizer (String name) {
        super(name);
    }

    public void setUp () {
    }

    public void tearDown() {
        lexer  = null;
        lexer1 = null;
        lexer2 = null;
        lexer3 = null;
    }
 
    public void testEmpty() throws Exception {
        lexer = new Tokenizer("");
        assertNotNull(lexer);
        token = lexer.getToken();
        assertNull(token);
        assertEquals(0, lexer.getLength());
        assertEquals(0, lexer.getOffset());
    }

    public void testLiterals() throws Exception {
        lexer = new Tokenizer("''");                // EMPTY STRING
        token = lexer.getToken();
        assertNotNull(token);
        assertTrue (token instanceof Literal);
        assertEquals("", ((Literal)token).getValue());

        lexer = new Tokenizer("'abc'");             // no delimiters
        token = lexer.getToken();
        assertNotNull(token);
        assertTrue (token instanceof Literal);
        assertEquals("abc", ((Literal)token).getValue());

        lexer = new Tokenizer("   'qrt'\t\t\n");    // delimiters
        token = lexer.getToken();
        assertNotNull(token);
        assertTrue (token instanceof Literal);
        assertEquals("qrt", ((Literal)token).getValue());

        lexer = new Tokenizer("   'qrt'\t\t\n'fghij'  ");   // two literals
        token = lexer.getToken();
        assertNotNull(token);
        assertTrue (token instanceof Literal);
        assertEquals("qrt", ((Literal)token).getValue());
        token = lexer.getToken();
        assertNotNull(token);
        assertTrue (token instanceof Literal);
        assertEquals("fghij", ((Literal)token).getValue());
        token = lexer.getToken();
        assertNull(token);                          // no more tokens

    }
    public void testNumerics() throws Exception {
        lexer = new Tokenizer("123");
        token = lexer.getToken();
        assertNotNull(token);
        assertTrue (token instanceof Numeric);
        Numeric n = (Numeric)token;
        assertEquals(123.0, n.getValue(), 0.0001);

        lexer = new Tokenizer("   123");
        token = lexer.getToken();
        assertNotNull(token);
        assertEquals(123.0, ((Numeric)token).getValue(), 0.0001);
        token = lexer.getToken();
        assertNull(token);

        lexer = new Tokenizer("   123   \n");
        token = lexer.getToken();
        assertNotNull(token);
        assertEquals(123.0, ((Numeric)token).getValue(), 0.0001);
        token = lexer.getToken();
        assertNull(token);

        lexer = new Tokenizer("789.123");
        token = lexer.getToken();
        assertNotNull(token);
        assertTrue (token instanceof Numeric);
        assertEquals(789.123, ((Numeric)token).getValue(), 0.00001);

        lexer = new Tokenizer ("  123  456  798");
        token = lexer.getToken();
        assertNotNull(token);
        assertEquals(123, ((Numeric)token).getValue(), 0.00001);
        token = lexer.getToken();
        assertNotNull(token);
        assertEquals(456, ((Numeric)token).getValue(), 0.00001);
        token = lexer.getToken();
        assertNotNull(token);
        assertEquals(798, ((Numeric)token).getValue(), 0.00001);
       
        lexer = new Tokenizer("-456");
        token = lexer.getToken();
        assertTrue (token instanceof Operator);
        assertEquals (Operator.MINUS, ((Operator)token).getIndex());
        token = lexer.getToken();
        assertEquals(456, ((Numeric)token).getValue(), 0.000001);

        // XXX '+' IS NOT A UNARY OPERATOR IN THE SPEC    
        lexer = new Tokenizer("+ 452");
        token = lexer.getToken();
        assertTrue (token instanceof Operator);
        assertEquals (Operator.PLUS, ((Operator)token).getIndex());
        token = lexer.getToken();
        assertEquals(452, ((Numeric)token).getValue(), 0.000001);
    }

    // OPERATORS ////////////////////////////////////////////////////
    void expectedOp (char c) throws Exception {
        // XXX DEPENDS UPON IMPLEMENTATION DETAILS XXX
        int expected = Operator.ALL_ONE_CHAR_OPERATORS.indexOf(c) + 4;
        Operator o = (Operator) lexer.getToken();
        int actual = o.getIndex();
        assertEquals ("wrong operator index for " + c, expected , actual);
    }
      
    // Can't handle * in this test, because * after an operator is a Symbol
    /** XXX CANNOT YET HANDLE CHARACTERS ALSO BEGINNING MULTI-CHAR OPS */
    public void testSingleCharOps()                 throws Exception {
        //String str = "|+=-*/<>";
        String str = "|+=-";
        lexer = new Tokenizer(str);
        for (int i = 0; i < str.length(); i++)
            expectedOp (str.charAt(i));
    }
    public void testSlashes()                       throws Exception {
        String str = "///abc/def/";
        lexer = new Tokenizer(str);
        token = lexer.getToken();
        assertTrue(token instanceof Operator);
        token = lexer.getToken();
        assertTrue(token instanceof Operator);
        token = lexer.getToken();
        assertTrue(token instanceof QName);
        assertEquals("abc", ((QName)token).getName());
        token = lexer.getToken();
        assertTrue(token instanceof Operator);
        token = lexer.getToken();
        assertTrue(token instanceof QName);
        assertEquals("def", ((QName)token).getName());
        token = lexer.getToken();
        assertTrue(token instanceof Operator);
    } 
    // SYMBOLS //////////////////////////////////////////////////////
    void expectedSym (char c)                       throws Exception {
        int expected = Symbol.ALL_ONE_CHAR_SYMBOLS.indexOf(c);
        Symbol o = (Symbol) lexer.getToken();
        int actual = o.getIndex();
        assertEquals ("wrong symbol index for " + c, expected , actual);
    }
    void testSingleCharSymbolString(String s)       throws Exception {
        lexer = new Tokenizer(s);
        for (int i = 0; i < s.length(); i++)
            expectedSym (s.charAt(i));
    }
    public void testOneCharSymbols()                throws Exception {
        testSingleCharSymbolString("()[]{}@,$.:");  // colon at end
        testSingleCharSymbolString(":@,.");         // dot at end
    }
    public void testMultiCharSymbols()              throws Exception {
        Symbol dot    = new Symbol(".");
        Symbol ddot   = new Symbol("..");
        assertEquals ( "..", Symbol.SYMBOLS[ddot.getIndex()]);
        Symbol colon  = new Symbol(":");
        Symbol dcolon = new Symbol("::");
        assertEquals ( "::", Symbol.SYMBOLS[dcolon.getIndex()]);
        lexer = new Tokenizer ("abc:::");
        assertTrue(lexer.getToken() instanceof QName);
        assertTrue(dcolon.equals((Symbol)lexer.getToken()));
        assertTrue(colon .equals((Symbol)lexer.getToken()));
        lexer = new Tokenizer ("..abc.:..");
        assertTrue(ddot.equals((Symbol)lexer.getToken()));
        assertTrue(lexer.getToken() instanceof QName );
        assertTrue(dot  .equals((Symbol)lexer.getToken()));
        assertTrue(colon.equals((Symbol)lexer.getToken()));
        assertTrue(ddot .equals((Symbol)lexer.getToken()));
    }
    // //////////////////////////////////////////////////////////////
    // XXX MISTAKES IN TESTING:
    // * an NCName which has no preceding token must become a QName
    // * ditto for an NCName preceded by @ :: ( [ or an Operator
    // * otherwise an NCName must be an OperatorName 
    // * except that if the next token is LPAREN it must be a 
    //     FunctionName or a NodeType
    // * or if the next token is DCOLON (::) it must be an AxisName
    // * or it's an error
    // In other words, we seem never to return an NCName
    // //////////////////////////////////////////////////////////////
    public void testQNames()                   throws Exception {
        lexer = new Tokenizer(" abc def ");
        token = lexer.getToken();
        assertNotNull(token);
        assertTrue (token instanceof QName);
        QName name = (QName) token;
        assertEquals("abc", name.getLocalPart());  
        name = (QName)lexer.getToken();
        assertEquals("def", name.getLocalPart());
        
        lexer = new Tokenizer(" _abc d-ef -12 ");
        name = (QName)lexer.getToken();
        assertEquals("_abc", name.getLocalPart());
        name = (QName)lexer.getToken();
        assertEquals("d-ef", name.getLocalPart());
        Operator op = (Operator)lexer.getToken();
        assertEquals(Operator.MINUS, op.getIndex());
        Numeric n = (Numeric)lexer.getToken();
        assertEquals (12.0, n.getValue(), 0.0001);

        lexer = new Tokenizer(" abc: ");
        name = (QName)lexer.getToken();
        assertEquals("abc", name.getLocalPart());
        token = lexer.getToken();
        assertNotNull(token);
        Symbol sym = (Symbol)token;
        assertEquals(Symbol.COLON, sym.getIndex());
       
        // XXX MY UNDERSTANDING OF THE RULES
        lexer = new Tokenizer(" abc : def ");
        name = (QName)lexer.getToken();
        assertEquals("abc", name.getLocalPart());
        sym = (Symbol)lexer.getToken();
        assertEquals(Symbol.COLON, sym.getIndex());
        name = (QName)lexer.getToken();
        assertEquals("def", name.getLocalPart());
    }
    public void testExprTokenDisambiguation()   throws Exception {
        lexer = new Tokenizer("*");
        assertNull(lexer.getLastToken());
        
        token = lexer.getToken();
        assertNotNull(token);
        assertTrue (token instanceof Symbol);
        assertEquals(Symbol.STAR, ((Symbol)token).getIndex());
        assertTrue ( token == lexer.getLastToken());
    
        // in each case the terminal * should be seen as a Symbol
        lexer = new Tokenizer("@*");
        token = lexer.getToken();
        assertTrue(token instanceof Symbol);
        assertEquals(Symbol.AT, ((Symbol)token).getIndex());
        token = lexer.getToken();
        assertTrue(token instanceof Symbol);
        assertEquals(Symbol.STAR, ((Symbol)token).getIndex());

        lexer = new Tokenizer("(*");
        token = lexer.getToken();
        assertTrue(token instanceof Symbol);
        assertEquals(Symbol.LPAREN, ((Symbol)token).getIndex());
        token = lexer.getToken();
        assertTrue(token instanceof Symbol);
        assertEquals(Symbol.STAR, ((Symbol)token).getIndex());
        
        lexer = new Tokenizer(")*");
        token = lexer.getToken();
        assertTrue(token instanceof Symbol);
        assertEquals(Symbol.RPAREN, ((Symbol)token).getIndex());
        token = lexer.getToken();
        assertTrue(token instanceof Operator);
        assertEquals(Operator.MUL, ((Operator)token).getIndex());
        
        lexer = new Tokenizer("[*");
        token = lexer.getToken();
        assertTrue(token instanceof Symbol);
        assertEquals(Symbol.LBRACKET, ((Symbol)token).getIndex());
        token = lexer.getToken();
        assertTrue(token instanceof Symbol);
        assertEquals(Symbol.STAR, ((Symbol)token).getIndex());
        
        lexer = new Tokenizer("**");    // 

        lexer = new Tokenizer("::*");   // two-character symbol
    } 

    // XXX PUSHBACK TEST -- NEED TO TEST DEPTH > 1 ******************
    public void testPushBack() throws Exception {
        lexer = new Tokenizer("1/2//3");
        token = lexer.getToken();       // number
        token = lexer.getToken();       // slash
        assertTrue (token instanceof Operator);
        assertEquals(Operator.SLASH, ((Operator)token).getIndex());
        lexer.pushback(token);
        token = lexer.getToken();
        assertTrue (token instanceof Operator);
        assertEquals(Operator.SLASH, ((Operator)token).getIndex());
        
        token = lexer.getToken();       // number
        token = lexer.getToken();       // dslash
        assertTrue (token instanceof Operator);
        assertEquals(Operator.DSLASH, ((Operator)token).getIndex());
        lexer.pushback(token);
        token = lexer.getToken();
        assertTrue (token instanceof Operator);
        assertEquals(Operator.DSLASH, ((Operator)token).getIndex());
    
        // push back a token not on the input
        lexer.pushback (new Literal("test"));
        token = lexer.getToken();  
        assertTrue (token instanceof Literal);
        assertEquals("test", ((Literal)token).getValue());
        
        token = lexer.getToken();
        assertTrue (token instanceof Numeric);
        assertEquals(3, ((Numeric)token).getValue(), 0.000001);
    }
    // **************************************************************
    // XXX DEVISE A TEST USING div AS AN ELEMENT NAME, AS IN HTML
    // **************************************************************
    //
    // XXX *** TOKENIZER IS INCOMPLETE AND NEEDS MUCH MORE TESTING ***
}
