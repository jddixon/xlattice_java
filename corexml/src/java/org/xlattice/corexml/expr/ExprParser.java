/* ExprParser.java */
package org.xlattice.corexml.expr;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Jim Dixon
 */

import org.xlattice.Context;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.*;   // FIX ME

/**
 * XPath 1.0 expression parser.  This class is best understood by 
 * reading it with the W3C XPath 1.0 specification in hand.
 * <p/>
 * The parse() method accepts an expression in String form, tokenizes it
 * using its lexer, and returns an unevaluated expression tree.
 * <p/>
 * The eval() method accepts an XPath expression tree and returns a
 * result.  The evaluation is done given the current context and 
 * context node.  XPath return types do not map particularly well into
 * Java types.
 * <p/>
 * This implementation is incomplete.  
 * <ul>
 * <li>all operators and symbols are recognized</li>
 * <li>primary expressions are parsed</li>
 * <li>most XPath library functions are <b>not</b> implemented</li>
 * <li>location paths are dealt with only along the child axis and 
 *      from the document root</li>
 * </ul>
 * 
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class ExprParser {

    /** the node expressions are evaluated in relation to */
    protected Node node;
    /** the context, hashed access to global variables */
    protected final Context ctx;

    /** the string being parsed */
    private String s;
    /** the tokenizer wich is doing the lexing */
    private Tokenizer lexer;
    /** the current token */
    private Token t;

    public static final Operator OP_SLASH  
                            = (Operator) TokenFactory.tokenFor("/");
    public static final Operator OP_DSLASH 
                            = (Operator) TokenFactory.tokenFor("//");
    public static final Operator OP_STAR
                            = new Operator("*");

    public static final Symbol   SYM_COLON
                            = (Symbol)   TokenFactory.tokenFor(":");
    public static final Symbol   SYM_DCOLON
                            = (Symbol)   TokenFactory.tokenFor("::");
    public static final Symbol   SYM_DOT
                            = (Symbol)   TokenFactory.tokenFor(".");
    public static final Symbol   SYM_DDOT
                            = (Symbol)   TokenFactory.tokenFor("..");
    public static final Symbol   SYM_LBRACKET
                            = (Symbol)   TokenFactory.tokenFor("[");
    public static final Symbol   SYM_LPAREN
                            = (Symbol)   TokenFactory.tokenFor("(");
    public static final Symbol   SYM_RBRACKET
                            = (Symbol)   TokenFactory.tokenFor("]");
    public static final Symbol   SYM_RPAREN
                            = (Symbol)   TokenFactory.tokenFor(")");
    public static final Symbol   SYM_STAR
                            = new Symbol("*");

    // NODETYPES AND NODE TESTS ///////////////////////////
    public static final NodeType NTYPE_COMMENT 
        = (NodeType) TokenFactory.tokenFor("comment"); 
    public static final NodeTest NTEST_COMMENT = new NodeTest (NTYPE_COMMENT);
    
    public static final NodeType NTYPE_ELEMENT 
        = (NodeType) TokenFactory.tokenFor("element"); 
    public static final NodeTest NTEST_ELEMENT = new NodeTest (NTYPE_ELEMENT);
    
    public static final NodeType NTYPE_NODE 
        = (NodeType) TokenFactory.tokenFor("node"); 
    public static final NodeTest NTEST_NODE = new NodeTest (NTYPE_NODE);
    
    public static final NodeType NTYPE_PI 
        = (NodeType) TokenFactory.tokenFor("processing-instruction"); 
    public static final NodeTest NTEST_PI = new NodeTest (NTYPE_PI);

    public static final NodeType NTYPE_TEXT 
        = (NodeType) TokenFactory.tokenFor("text"); 
    public static final NodeTest NTEST_TEXT = new NodeTest (NTYPE_TEXT);
   
    public static final NodeTest NTEST_STAR = new NodeTest (SYM_STAR);

    // CONSTRUCTOR //////////////////////////////////////////////////
    /**
     * Create an expression parser.
     * 
     * @param n current node 
     * @param c current context
     */
    public ExprParser (Node n, Context c) {
        node  = n;
        ctx   = c;
    }

    // PARSER STATE /////////////////////////////////////////////////
    /** the expression collected by the parser up to this point */
    private Expr soFar;

    /** @return the context node */
    public Node getNode() {
        return node;
    }
    /**
     * Change the context node, the node that expressions are evaluated
     * in relation to.
     */
    public void setNode(Node newContextNode) {
        if (node == null)
            throw new NullPointerException("attempt to set null context node");
        node = newContextNode;
    }
    // PARSER METHODS ///////////////////////////////////////////////
    /**
     * @throws CoreXmlException if t not next Token from lexer
     */
    void expect (Token t)                   throws CoreXmlException {
        Token actual = lexer.getToken();
        if (!t.equals( actual))
            throw new CoreXmlException("expected " + t
                    + "\n\tbut found " + actual);
    }
    /**
     * Parse a String holding an XPath expression.  If the String
     * is null or empty, return null.  Otherwise, return an
     * unevaluated expression tree.
     *
     * @return a parse tree
     */
    public Expr parse(String str) throws CoreXmlException {
        if (str == null || str.length() == 0)
            return null;
        s     = str;
        lexer = new Tokenizer (s);
        return parse_(0, null);
    }

    /**
     * Parse the rest of the expression.  If a left subexpression has
     * been seen, a reference to it is passed.  If an operator has been
     * seen, its precedence is passed.  If this method encounters an
     * operator of lower precedence, it will stop the parse of the
     * right subexpression, pushing the lower-precedence operator back
     * on the lexer.
     *
     * @param soFar      null or any left subexpression
     * @param precedence the precedence of the operator last seen or zero
     */
    private Expr parse_ (int precedence, Expr soFar)
                                            throws CoreXmlException {
        for (t = lexer.getToken(); t != null; t = lexer.getToken()) {
            /////////////////////////////////////////////////////////
            // PRIMARY EXPRESSIONS //////////////////////////////////
            /////////////////////////////////////////////////////////

            // COREEXPR HANDLES ///////////////////////////

            if (t instanceof Literal) {
                if (soFar == null) {
                    // soFar = new StringExpr(((Literal)t).getValue());
                    soFar = (Literal) t;
                } else {
                    throw new IllegalStateException("missing operator?");
                }
            } else if (t instanceof Numeric) {
                if (soFar == null) {
                    soFar = (Numeric) t;
                } else {
                    throw new IllegalStateException("missing operator?");
                }
            }
            // RELATIVE LOCATION PATH HANDLES /////////////
            else if ((t instanceof AxisName) || (t instanceof QName   )
                  || (t instanceof NCName  ) || (t instanceof NodeType)) {
                soFar = parseStep (t, soFar);
                break;
            }
            // SYMBOLS ////////////////////////////////////
            else if (t instanceof Symbol) {
                Symbol sym  = (Symbol) t;
                int symType = sym.getIndex();
                Expr e;
                switch (symType) {
                    // RELATIVE LOCATION PATH HANDLES /////
                    case Symbol.DOT:
                    case Symbol.DDOT:
                    case Symbol.AT:
                    case Symbol.STAR:
                        soFar = parseStep (t, soFar);
                        break;
                    // FUNCTION NAMES /////////////////////


                    // VARIABLES //////////////////////////
                    // Not subject to further tokenization.
                    // Values must be Expr (expressions).
                    case Symbol.DOLLAR: {
                        Token varName = lexer.getToken();
                        if (varName == null)
                            throw new CoreXmlException (
                                    "missing variable name after $");
                        if (!(varName instanceof QName))
                            throw new CoreXmlException (
                                    "$ followed by " + varName);
                        String name = ((QName)varName).getName();
                        Expr value = (Expr) ctx.lookup(name);
                        if (value == null)
                            throw new CoreXmlException("variable $"
                                    + name + " not defined in context");
                        // we have a value
                        if (soFar == null) {
                            soFar = value;
                            break;
                        } else
                            throw new CoreXmlException (
                                "missing operator before variable $" + name);
                    }

                    // PARENTHESIZED EXPRESSIONS //////////
                    // Given a left parenthesis, the parser
                    // accepts everything up to the matching
                    // right parenthesis as an expression.  The
                    // closing right parenthesis is pushed back
                    // to the lexer; this code confirms that
                    // the closing parenthesis is present and
                    // then discards it.
                    case Symbol.LPAREN:
                        if (soFar != null)
                            throw new CoreXmlException(
                                    "found LPAREN but have left operand");
                        e = parse_(0, null);
                        // THIS IS expect()
                        t = lexer.getToken();
                        if (! ((t instanceof Symbol)
                                && ((Symbol)t).getIndex() == Symbol.RPAREN)) {
                            throw new CoreXmlException("missing RPAREN");
                        }
                        return e;

                    // PARENTHESIS HANDLING, cont.  The right parenthesis
                    // should mark the end of an expression.  It is pushed
                    // back on input so that balancing can be checked.
                    case Symbol.RPAREN:
                        if (soFar == null)
                            throw new CoreXmlException(
                                    "found RPAREN but no subexpression");
                        lexer.pushback(sym);
                        return soFar;

                    /////////////////////////////////////////////////
                    // LOCATION PATH EXPRESSIONS ////////////////////
                    /////////////////////////////////////////////////

                    // PARTIAL HANDLING OF SQUARE BRACKETS

                    case Symbol.LBRACKET:
                        if (soFar != null)
                            throw new CoreXmlException(
                                    "found LBRACKET but have left operand");
                        e = parse_(0, null);
                        // THIS IS expect()
                        t = lexer.getToken();
                        if (! ((t instanceof Symbol)
                                && ((Symbol)t).getIndex() == Symbol.RBRACKET)) {
                            throw new CoreXmlException("missing RBRACKET");
                        }
                        return e;

                    case Symbol.RBRACKET:
                        if (soFar == null)
                            throw new CoreXmlException(
                                    "found RBRACKET but no subexpression");
                        lexer.pushback(sym);
                        return soFar;

                    // PARTIAL HANDLING OF BRACES ALONG THE SAME LINES

                    case Symbol.LBRACE:
                        if (soFar != null)
                            throw new CoreXmlException(
                                    "found LBRACE but have left operand");
                        e = parse_(0, null);
                        // THIS IS expect()
                        t = lexer.getToken();
                        if (! ((t instanceof Symbol)
                                && ((Symbol)t).getIndex() == Symbol.RBRACE)) {
                            throw new CoreXmlException("missing RBRACE");
                        }
                        return e;

                    case Symbol.RBRACE:
                        if (soFar == null)
                            throw new CoreXmlException(
                                    "found RBRACE but no subexpression");
                        lexer.pushback(sym);
                        return soFar;

                    default:
                        throw new IllegalStateException(
                            "unrecognized/unimplemented Symbol "
                            + ((Symbol)t).toString());
                }
            }
            // OPERATORS //////////////////////////////////
            else if (t instanceof Operator) {
                Operator op = (Operator) t;
                int opType = op.getIndex();
                int thisOpPrecedence = Operator.PRECEDENCE[opType];
                // XXX NEEDS SOME THOUGHT.  This says that we push
                // back lower precedence operators only if we have
                // collected an expression.
                if (soFar != null && thisOpPrecedence <= precedence) {
                    lexer.pushback(op);
                    return soFar;
                }
                if (opType == Operator.MINUS && soFar == null)
                    soFar = parseUnaryExpr (op);
                else if(opType == Operator.SLASH || opType == Operator.DSLASH){
                    if (soFar == null)
                        soFar = parseAbsLocationPath (op);
                    else {
                        soFar = parseStep(op, soFar);
                    }
                }
                else {
                    if (soFar == null)
                        throw new CoreXmlException (
                                "missing left operand to " + op.toString());
                    else switch (thisOpPrecedence) {
                        case 7:
                            soFar = parseUnionExpr(op, soFar);
                            break;
                        case 5:
                            soFar = parseMultiplicativeExpr(op, soFar);
                            break;
                        case 4:
                            soFar = parseAdditiveExpr(op, soFar);
                            break;
                        case 3:
                            soFar = parseRelationalExpr(op, soFar);
                            break;
                        case 2:
                            soFar = parseEqualityExpr(op, soFar);
                            break;
                        case 1:
                            soFar = parseAndExpr(op, soFar);
                            break;
                        case 0:
                            soFar = parseOrExpr(op, soFar);
                            break;
                        default:
                            throw new IllegalStateException(
                                    "unsupported operator precedence "
                                    + thisOpPrecedence);
                    }
                }
            } // end Operators
            // UNKNOWN TOKEN //////////////////////////////
            else throw new IllegalStateException(
                        "unrecognized token " + t.toString());

        }
        return soFar;

    }
    /////////////////////////////////////////////////////////////////
    // PARSE LOCATION PATH EXPRESSIONS //////////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Parse an absolute location path, having encountered an Operator
     * token.
     * @return a LocationPath subexpression tree
     */
    LocationPath parseAbsLocationPath (Operator op)
                                            throws CoreXmlException {
        return collectSteps ( new LocationPath (true),  // isAbsolute
                              op == OP_SLASH,           // single
                              op == OP_DSLASH);         // required
    }
    /**
     * Parse an absolute location path, having encountered the current
     * token.
     * @return a LocationPath subexpression tree
     */
    LocationPath parseRelLocationPath (Token t)
                                            throws CoreXmlException {
        lexer.pushback(t);
        return collectSteps ( new LocationPath (false), // isAbsolute
                              true,                     // single
                              true);                    // required
    }
    /**
     * Collect a series of steps and append them to the location
     * path, which is returned.  
     * @param lp       location path input
     * @param single   collect only one step
     * @param required whether a step MUST be found
     * @return  location path with steps appended
     */
    LocationPath collectSteps (LocationPath lp, boolean single, 
                                                boolean required) 
                                            throws CoreXmlException {
        for (Token t = lexer.getToken(); t != null; t = lexer.getToken()) {
            Step step = null;
            if (t instanceof AxisName)
                step = collectStep ((AxisName)t,   single);
            else if (t instanceof Symbol)
                step = collectStep ((Symbol)t,     single);
            else if (t instanceof NodeType)
                step = collectStep ((NodeType)t,   single);
            else if (t instanceof NCName)
                step = collectStep ((NCName)t,     single);
            else if (t instanceof QName)
                step = collectStep ((QName)t,      single);
            else 
                throw new CoreXmlException("unexpected token " + t);
            if (step == null) 
                break;
            lp.addStep(step);
            t = lexer.getToken();
            if (t != null) {
              boolean match = false;
              if ( t.equals(OP_SLASH)) {
                  match  = true;
                  single = true;
              } else if (t.equals(OP_DSLASH)) {
                  match  = true;
                  single = false;
              }
              if (!match) {
                  lexer.pushback(t);
                  break;
              }
            } 
        }
        if (required && lp.stepCount() <= 0)
            throw new CoreXmlException("location path has no steps");
        return lp;
    }
    /**
     * Having encountered an axis name, collect the step.
     */
    Step collectStep (AxisName aName, boolean single) 
                                            throws CoreXmlException {
        expect (SYM_DCOLON);
        return collectPredicates (new Step (aName.getIndex(), 
                    collectNodeTest(), single));
    }
    /**
     * Having encountered a symbol, collect the step.
     */
    Step collectStep (Symbol sym, boolean single) 
                                            throws CoreXmlException {
        int symType = sym.getIndex();
        switch (symType) {
            case Symbol.DOT:
                return new Step(AxisName.SELF, NTEST_NODE, single);
            case Symbol.DDOT:
                return new Step(AxisName.PARENT, NTEST_NODE, single);
            case Symbol.AT:
                return collectPredicates(
                        new Step (AxisName.ATTRIBUTE, NTEST_NODE, single));
            case Symbol.STAR:
                return collectPredicates(
                        new Step (AxisName.CHILD, NTEST_ELEMENT, single));
            default: 
                lexer.pushback(sym);
                return null;
        }
    }
    /** 
     * Having encountered a nodeType, collect the step, defaulting
     * it to one along the CHILD axis.
     */
    Step collectStep (NodeType nodeType,   boolean single)  
                                            throws CoreXmlException {
        lexer.pushback (nodeType);
        return collectPredicates(
                new Step ( AxisName.CHILD, collectNodeTest(), single));
    }
    /**
     * Verify NCName followed by :*, then collect the step, defaulting
     * it to one along the CHILD axis.
     */
    Step collectStep (NCName   ncName,     boolean single)  
                                            throws CoreXmlException {
        expect (SYM_COLON);
        expect (SYM_STAR);
        return (collectPredicates (
                    new Step (AxisName.CHILD, new NodeTest(ncName), single)));
    }
    /**
     * Having encountered a QName, collect the step, defaulting it to
     * one along the CHILD axis.
     */
    Step collectStep (QName    qName,      boolean single) 
                                            throws CoreXmlException {
        return (collectPredicates (
                    new Step (AxisName.CHILD, new NodeTest(qName), single)));
    }
    /** 
     * Expect the next tokens from the lexer to represent a NodeTest.
     * 
     * @return the NodeTest constructed from the stream of Tokens
     */
    NodeTest collectNodeTest()              throws CoreXmlException {
        Token t = lexer.getToken();
        NodeTest nt = new NodeTest(t);  // exception if not valid token
        if (nt.getType() instanceof NodeType) {
            expect(SYM_LPAREN);
            t = lexer.getToken();
            if (t instanceof Literal) {
                nt.setPIName((Literal)t);   // exception if not PI node test
                t = lexer.getToken();
            }
            lexer.pushback(t);              // LAZINESS
            expect(SYM_RPAREN);
        }
        return nt;
    }
    /**
     * Given a step, collect any predicates following it.
     */
    Step collectPredicates (Step step)      throws CoreXmlException {
        for (Token t = lexer.getToken(); t != null; t = lexer.getToken()) {
            if (SYM_LBRACKET.equals(t)) {
                Expr e = parse_ (0, null);
                if (e == null) 
                    throw new CoreXmlException("predicate has no expression");
                step.addPredicate(e);
                expect(SYM_RBRACKET);
            } else {
                lexer.pushback(t);
                break;
            }
        }
        return step;
    }
    /////////////////////////////////////////////////////////////////
    /** 
     * Parse a step beginning with Token, using the expression 
     * collected so far to build a location path.
     *
     * @param t     curent token
     * @param soFar expression seen so far, must be a LocationPath
     */
    LocationPath parseStep (Token t, Expr soFar)
                                            throws CoreXmlException {
        if (!(soFar instanceof LocationPath))
            throw new CoreXmlException ("operator " + t
                    + " not following LocationPath - expression is "
                    + soFar.toString());

        // a Step can only follow a location path
        LocationPath lhs = (LocationPath) soFar;

        // SYMBOLS 
        if (t instanceof Symbol) {
            throw new CoreXmlException("can't handle " + t + " yet");
        }
        // OPERATORS
        else if (t instanceof Operator) {
            int opType = ((Operator)t).getIndex();
            if (opType == Operator.SLASH || opType == Operator.DSLASH) {
                // XXX SIGNIFICANT IF DSLASH XXX
                t = lexer.getToken();
            } else 
                throw new CoreXmlException("INTERNAL ERROR: " + t +
                        "before Step");
        }
        // AXIS /////////////////////////////////////////////////////
        int axis = AxisName.CHILD;      // default
        boolean usedTheToken = false;
        if (t instanceof AxisName) {
            usedTheToken = true;
            axis = ((AxisName)t).getIndex();
            // expect() again
            t = lexer.getToken();
            if (! ((t instanceof Symbol)
                        && (((Symbol)t).getIndex() == Symbol.DCOLON))) {
                throw new CoreXmlException("missing :: after axis name");
            }
        } else if ( (t instanceof Symbol)
                && (((Symbol)t).getIndex() == Symbol.AT)){
            usedTheToken = true;
            axis = AxisName.ATTRIBUTE;      // @ is the abbreviation
        }
        // NODE TEST ////////////////////////////////////////////////
        if (usedTheToken)  {
            t = lexer.getToken();
            if (t == null)
                throw new CoreXmlException("missing node test after axis name");
        }

        // there are four types: NodeType, STAR, NCName, and QName
        NodeTest nt = new NodeTest(t);

        // DO CHECKS ...

        Step step = new Step(axis, nt, false);      // KLUDGE ******
        // PREDICATES ///////////////////////////////////////////////
        for (t = lexer.getToken();
                t != null && (t instanceof Symbol)
                && ((Symbol)t).getIndex() == Symbol.LBRACKET;
                t = lexer.getToken() ) {
            Expr e = parse_ (0, null);
            if (e == null)
                throw new CoreXmlException("null predicate");
            step.addPredicate(e);
            t = lexer.getToken();
            if (t == null || !(t instanceof Symbol)
                          || (((Symbol)t).getIndex() != Symbol.RBRACKET))
                throw new CoreXmlException("predicate missing right bracket");
        }
        if (t != null) {
            lexer.pushback(t);
        }
        lhs.addStep(step);
        return lhs;
    }
    /////////////////////////////////////////////////////////////////
    // PARSE EXPRESSIONS INVOLVING OPERATORS ////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Parse a unary expression with a leading minus.
     */
    UnaryExpr parseUnaryExpr (Operator op)  throws CoreXmlException {
        Expr subexpr = parse_(Operator.PRECEDENCE[op.getIndex()], null);

        return new UnaryExpr (op, subexpr);
    }
    /** Called after seeing a '|' (BAR) */
    BinaryExpr parseUnionExpr (Operator op, Expr left)
                                            throws CoreXmlException {
        Expr right = parse_(Operator.PRECEDENCE[op.getIndex()], null);

        return new BinaryExpr (op, left, right);
    }
    BinaryExpr parseMultiplicativeExpr (Operator op, Expr left)
                                            throws CoreXmlException {
        Expr right = parse_(Operator.PRECEDENCE[op.getIndex()], null);
        if (right == null)
            throw new CoreXmlException("missing right operand to "
                    + op.toString());
        return new BinaryExpr (op, left, right);
    }
    BinaryExpr parseAdditiveExpr (Operator op, Expr left)
                                            throws CoreXmlException {
        Expr right = parse_(Operator.PRECEDENCE[op.getIndex()], null);
        // possible reshuffling of the left operator
        return new BinaryExpr (op, left, right);
    }
    BinaryExpr parseRelationalExpr (Operator op, Expr left)
                                            throws CoreXmlException {
        Expr right = parse_(Operator.PRECEDENCE[op.getIndex()], null);
        // possible reshuffling of the left operator
        return new BinaryExpr (op, left, right);
    }
    BinaryExpr parseEqualityExpr (Operator op, Expr left)
                                            throws CoreXmlException {
        Expr right = parse_(Operator.PRECEDENCE[op.getIndex()], null);
        // possible reshuffling of the left operator
        return new BinaryExpr (op, left, right);
    }
    BinaryExpr parseAndExpr (Operator op, Expr left)
                                            throws CoreXmlException {
        Expr right = parse_(Operator.PRECEDENCE[op.getIndex()], null);
        // possible reshuffling of the left operator
        return new BinaryExpr (op, left, right);
    }
    BinaryExpr parseOrExpr (Operator op, Expr left)
                                            throws CoreXmlException {
        Expr right = parse_(Operator.PRECEDENCE[op.getIndex()], null);
        // possible reshuffling of the left operator
        return new BinaryExpr (op, left, right);
    }
    // //////////////////////////////////////////////////////////////
    // EXPRESSION EVALUATION ////////////////////////////////////////
    // //////////////////////////////////////////////////////////////

    // EXTERNAL INTERFACE /////////////////////////////////
    public Expr eval (String str)           throws CoreXmlException {
        Expr e = parse(str);
        if (e == null)
            return e;
        else
            return eval_ (e);
    }
    public Expr eval (Expr e)               throws CoreXmlException {
        if (e == null)
            return e;
        else
            return eval_ (e);
    }
    // INTERNAL ///////////////////////////////////////////
    private Expr eval_ (Expr e)             throws CoreXmlException {
        if (e.tag == "number" || e.tag == "string" || e.tag == "bool"
                                                   || e.tag == "nodeset" )
            return e;
        else if (e instanceof LocationPath) {
            NodeSet result = evalLocationPath((LocationPath)e);
            return result;
        }
        else if (e instanceof BinaryExpr)
            return evalBinary((BinaryExpr)e);
        else if (e instanceof UnaryExpr)
            return evalUnary((UnaryExpr)e);
        else
            throw new CoreXmlException ("unimplemented expression type "
                    + e.toString());
    }
    // LOCATION PATHS /////////////////////////////////////
    // XXX NOT PRIVATE, SO WE CAN TEST
    NodeSet evalLocationPath (LocationPath lp)
                                            throws CoreXmlException {
        NodeSet nodes = new NodeSet();
        Node    ctxNode;
        if (lp.isAbsolute()) {
            if (node instanceof Document)
                ctxNode = node;
            else 
                ctxNode = node.getDocument();
            int stepCount = lp.stepCount();
            ArrayList steps = new ArrayList(stepCount);
            for (int i = 0; i < lp.stepCount(); i++) {
                steps.add(lp.getStep(i));
            }
            if (steps.size() > 0) {
                takeStep (ctxNode, nodes, steps);
            }

        } else {
            throw new IllegalStateException("can't handle relative paths yet");
        }
        return nodes;
    }
    
    // XXX NOT PRIVATE, SO WE CAN TEST
    Node takeStep (Node ctxNode, NodeSet collected, ArrayList steps) 
                                                throws CoreXmlException {
        int ctxPosition = 0;
        ArrayList mySteps = (ArrayList) steps.clone();
        Step step = (Step) mySteps.remove(0);
        int axis = step.getAxisNameIndex();
        // AXIS ///////////////////////////////////////////
        if (axis != AxisName.CHILD)
            throw new IllegalStateException("can't handle "
                + AxisName.NAMES[axis]);
        
        NodeSet  matchNodes = new NodeSet();
        NodeList ctxNodes;
        if (ctxNode instanceof Holder)
            ctxNodes = ((Holder)ctxNode).getNodeList();
        else
            throw new IllegalStateException(
                    "context node not Holder, can't handle");
        
        // NODETEST ///////////////////////////////////////
        Token testType = step.getNodeTest().getType();
        if (!(testType instanceof QName))
            throw new IllegalStateException(
                    "can only handle QName nodetests");
        String elName = ((QName)testType).getName();
        for (int i = 0; i < ctxNodes.size(); i++) {
            Node node = ctxNodes.get(i);
            if ((node instanceof Element) 
                    && ((Element)node).getName().equals(elName)) {
                // NOTE DEFINITION OF POSITION
                ctxPosition++;
                
                int pCount = step.predicateCount();
                if (pCount == 0)  
                        matchNodes.add(node);
                else {
                    // APPLY PREDICATES BEFORE NODES COLLECTED 
                    // for the moment, only consider numeric equality to index
                    for (int j = 0; j < step.predicateCount(); j++) {
                        Expr e = (Expr)step.getPredicate(j);
                        Expr e1 = eval_(e);
                        if (!(e1 instanceof Numeric)) {
                            throw new IllegalStateException (
                                "can't handle non-numeric predicates yet :"
                                + e1);
                        }
                        int predicateValue = ((Numeric)e1).intValue();
                        if (predicateValue == ctxPosition) {
                            matchNodes.add(node);
                        }
                    } 
                }
            }
        }
        if (mySteps.size() > 0) {
            Iterator it = matchNodes.iterator();
            while (it.hasNext()) {
                Node thisNode = (Node) it.next();
                takeStep (thisNode, collected, mySteps);
            }
        } else {
            collected.add(matchNodes);
        }
        return ctxNode;
    }
    
    // UNARY EXPRESSIONS //////////////////////////////////
    private Expr evalUnary (UnaryExpr e)    throws CoreXmlException {
        if (e.getOperatorType() != Operator.MINUS)
            throw new CoreXmlException (
                    "INTERNAL ERROR: unary expression without minus");
        Expr sub = eval_(e.getSubexpr());
        if ( sub instanceof Numeric )
            return new Numeric ( -((Numeric)sub).getValue() );
        else
            throw new CoreXmlException (
                    "unary expression has non-numeric subexpression");
    }
    // BINARY EXPRESSIONS /////////////////////////////////
    private Expr evalBinary (BinaryExpr e)  throws CoreXmlException {
        int opType = e.getOperatorType();
        switch (opType) {
            case Operator.BAR:
                return evalNodeSetBinary (e);

            case Operator.MUL:
            case Operator.DIV:
            case Operator.MOD:

            case Operator.PLUS:
            case Operator.MINUS:
                return evalNumericBinary (e);

            case Operator.LT:
            case Operator.GT:
            case Operator.LE:
            case Operator.GE:
                // THIS IS WRONG - RESULT IS BOOL
                return evalNumericBinary (e);

            case Operator.EQ:
            case Operator.NE:
                throw new CoreXmlException("can't handle eq/ne yet");

            case Operator.AND:
            case Operator.OR:
                return evalBoolBinary (e);


            default:
                throw new CoreXmlException(
                        "unknown or unimplemented operator type " + opType);
        }
    }
    Bool evalBoolBinary( BinaryExpr e)  throws CoreXmlException {
        // long-winded to ease debugging
        int opType = e.getOperatorType();
        Expr left  = eval_(e.getLeftSubexpr());

        Expr right = eval_(e.getRightSubexpr());

        return Bool.FALSE;  // HACK to get this to compile ;-)
    }
    Literal evalLiteralBinary( BinaryExpr e)  throws CoreXmlException {
        int opType = e.getOperatorType();
        Expr left  = eval_(e.getLeftSubexpr());
        Expr right = eval_(e.getRightSubexpr());
        return null;        // HACK to get things to compile
    }
    NodeSet evalNodeSetBinary( BinaryExpr e)  throws CoreXmlException {
        int opType = e.getOperatorType();
        Expr left  = eval_(e.getLeftSubexpr());
        if (! (left instanceof NodeSet) )
            throw new CoreXmlException("left operand not node set");
        Expr right = eval_(e.getRightSubexpr());
        if (! (right instanceof NodeSet) )
            throw new CoreXmlException("right operand not node set");

        // COMBINE NODESETS AND RETURN
        return null;        // HACK
    }
    Numeric evalNumericBinary( BinaryExpr e)  throws CoreXmlException {
        int opType = e.getOperatorType();
        Expr left  = eval_(e.getLeftSubexpr());
        double leftVal = castToNumeric(left).getValue();
        Expr right = eval_(e.getRightSubexpr());
        double rightVal = castToNumeric(right).getValue();
        double result;
        switch(opType) {
            case Operator.MUL:
                result = leftVal * rightVal;
                break;
            case Operator.DIV:
                result = leftVal / rightVal;
                break;
            case Operator.MOD:
                result = leftVal % rightVal;
                break;
            case Operator.PLUS:
                result = leftVal + rightVal;
                break;
            case Operator.MINUS:
                result = leftVal - rightVal;
                break;
            default:
                throw new CoreXmlException (
                        "illegal numeric operator " + e.getOperator());
        }
        return new Numeric(result);
    }
    // TYPE CASTS ///////////////////////////////////////////////////

    public Bool castToBool (Expr e)  throws CoreXmlException {
        if (e instanceof Bool) {
            return (Bool) e;
        } else if (e instanceof Literal) {
            if ( ((Literal)e).getValue().length() > 0)
                return Bool.TRUE;
            else
                return Bool.FALSE;
        } else if (e instanceof NodeSet) {
            if ( ((NodeSet)e).size() > 0 )
                return Bool.TRUE;
            else
                return Bool.FALSE;
        } else if (e instanceof Numeric) {
            double d = ((Numeric)e).getValue();
            if (Double.isNaN(d) || d == 0.0)
                return Bool.FALSE;
            else
                return Bool.TRUE;
        } else {
            cantCast(e);
            return null;
        }
    }
    public Numeric castToNumeric(Expr e)  throws CoreXmlException {
        if (e instanceof Numeric) {
            return (Numeric) e;
        } else if (e instanceof Bool) {
            if (e == Bool.TRUE)
                return Numeric.ONE;
            else
                return Numeric.ZERO;
        } else if (e instanceof Literal) {
            return new Numeric (number( ((Literal)e).getValue() ));
        } else if (e instanceof NodeSet) {
            return new Numeric ( ((NodeSet)e).size() );
        } else {
            cantCast(e);
            return null;
        }
    }
    public Literal castToLiteral(Expr e)    throws CoreXmlException {
        if (e instanceof Literal) {
            return (Literal) e;
        } else if (e instanceof Bool) {
            if ((Bool)e == Bool.TRUE)
                return Literal.TRUE;
            else
                return Literal.FALSE;
        } else if (e instanceof Numeric) {
            Double d = new Double(((Numeric)e).getValue());
            return new Literal(string(d));      // XPath rules
        } else if (e instanceof NodeSet) {
            throw new CoreXmlException(
                    "cast of NodeSet to Literal not implemented");
        } else {
            cantCast(e);
            return null;
        }
    }
    public void cantCast (Expr e)  throws CoreXmlException {
        throw new CoreXmlException(
                "don't know how to cast expression: " + e);
    }
    // //////////////////////////////////////////////////////////////
    // XPATH CORE FUNCTIONS /////////////////////////////////////////
    // //////////////////////////////////////////////////////////////

    // NODESET FUNCTIONS ////////////////////////////////////////////
    // there are 7 such functions, some having two forms

    // 1. count ///////////////////////////////////////////
    /**
     * XPath NodeSet function 1, count.
     * 
     * @return the number of nodes in the NodeSet
     */
    public static Numeric count(NodeSet nodes) {
        if (nodes == null)
            return Numeric.ZERO;
        else
            return new Numeric(nodes.size());
    }
    // 2. id //////////////////////////////////////////////
    public Numeric id() {
        throw new IllegalStateException("not implemented");
    }
    // 3. last ////////////////////////////////////////////
    public Numeric last() {
        throw new IllegalStateException("not implemented");
    }
    // 4. name ////////////////////////////////////////////
    public String name() {
        throw new IllegalStateException("not implemented");
    }
    public static String name(NodeSet nodes) {
        throw new IllegalStateException("not implemented");
    }
    // 5. localName ////////////////////////////////////////
    public String localName() {
        throw new IllegalStateException("not implemented");
    }
    public static String localName(NodeSet nodes) {
        throw new IllegalStateException("not implemented");
    }
    // 6. namespaceUri /////////////////////////////////////
    public String namespaceUri() {
        throw new IllegalStateException("not implemented");
    }
    public static String namespaceUri(NodeSet nodes) {
        throw new IllegalStateException("not implemented");
    }
    // 7. position /////////////////////////////////////////
    public Numeric position() {
        throw new IllegalStateException("not implemented");
    }

    // STRING FUNCTIONS /////////////////////////////////////////////
    // There are 10 such functions, some with more than one form.

    // 1. string //////////////////////////////////////////
    // ****************************************************
    // XXX NEED TO REVIEW THIS AGAINST THE SPECS **********
    // ****************************************************
    /** 
     * XPath String function 1, string(node).
     */
    public static String string (Node node) {
        if (node instanceof Text) {
            return ((Text)node).getText();
        } else if (node instanceof Cdata) {
            return ((Cdata)node).getText();
        } else if (node instanceof Holder) {
            StringBuffer sb = new StringBuffer();
            NodeList itsNodes = ((Holder) node).getNodeList();
            for (int i = 0; i < itsNodes.size(); i++)
                sb.append ( string(itsNodes.get(i)) );
            return sb.toString();
        } else 
            return "";
    }
    /**
     * Convert the context node.  XXX Implement this by passing
     * a NodeSet containing only the context node to the next
     * function.
     *
     * XXX THE NODE BEING PASSED HERE IS NOT NECESSARILY THE
     *     CURRENT CONTEXT NODE
     */
    public String string() {
        return string (new NodeSet(node));
    }
    /**
     * Convert objects to Java Strings in accordance with the spec.
     * XXX This implementation is incomplete.
     */
    public static String string(Object e) {
        if (e instanceof NodeSet) {
            // XXX REVIEW IMPLEMENTATION AGAINST SPECS
            NodeSet nodes = (NodeSet)e;
            if (nodes.size() == 0) {
                return "";
            } else {
                StringBuffer sb = new StringBuffer();
                Iterator it = nodes.iterator();
                while (it.hasNext())
                    sb.append ( string ((Node)it.next()) );
                return sb.toString();
            }
        } else if (e instanceof Double) {
            double d = ((Double)e).doubleValue();
            // standard requires that integer values be represented as such
            long ld  = (long) d;
            if (d == ld)                // remarkably, this works ;-)
                return Long.toString(ld);
            else
                return Double.toString(d);
        } else if (e instanceof Boolean) {
            if (e == Boolean.TRUE)
                return "true";
            else
                return "false";
        } else if (e instanceof String) {
            return (String)e;
        } else  {
            // otherwise, just trust to Java
            return e.toString();
        }
    }
    // 2. concat //////////////////////////////////////////
    /**
     * This interface deviates from the specs, because
     * Java 1.4 doesn't support variable length argument lists.
     *
     * @return the concatenation of its arguments
     */
    public static String concat (String [] args) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args.length; i++)
            sb.append(args[i]);
        return sb.toString();
    }
    // BOOLEAN FUNCTIONS ////////////////////////////////////////////
    // There are 5 such functions.

    // 1. boolean /////////////////////////////////////////
    public static boolean booleanFunc(Object o) {
        if (o instanceof Boolean) {
            if (o == Boolean.TRUE)
                return true;
            else
                return false;
        } else if (o instanceof Double) {
            Double d = (Double)o;
            return (!d.isNaN() && d.doubleValue() != 0.0);
        } else if (o instanceof NodeSet) {
            return ! ((NodeSet)o).isEmpty();
        } else if (o instanceof String) {
            return ((String)o).length() > 0;
        } else
            throw new IllegalStateException("not implemented");
    }
    // NUMERIC FUNCTIONS ////////////////////////////////////////////
    // There are 5 such functions.

    // 1. number //////////////////////////////////////////
    /** 
     * XPath numeric function 1, number()
     */
    public static Double number (Object o) {
        if (o instanceof Double)
            return (Double) o;
        if (o instanceof String) {
            try {
                return new Double((String)o);
            } catch (NumberFormatException nfe) {
                return new Double(Double.NaN);
            }
        } else if (o instanceof Boolean) {
            if (o == Boolean.TRUE)
                return new Double (1);
            else
                return new Double (0);
        } else if (o instanceof NodeSet) {
            // XXX CONVERT USING string((NodeSet)o) THEN HANDLE AS STRING
            throw new IllegalStateException("not implemented");
        } else
            throw new IllegalStateException("not implemented");
    }
    public static Double number () {
        throw new IllegalStateException("not implemented");
    }

    // SERIALIZATION ////////////////////////////////////////////////
}
