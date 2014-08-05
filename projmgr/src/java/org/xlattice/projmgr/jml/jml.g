/* jml.g */

header {
    package org.xlattice.projmgr.jml;
    import java.util.*;
    import java.io.*;
}

// CLASS PREAMBLE ///////////////////////////////////////////////////

class JmlScanner extends Lexer;

// CLASS OPTIONS ////////////////////////////////////////////////////
options {
    classHeaderPrefix = "public abstract";
    charVocabulary='\003'..'\377';      // ISO-Latin-1
    k=4;                // FIDDLING WITH THIS
    filter=OTHERWISE;   // handles anything not otherwise handled
}

// the notice below goes in the generated file, JmlScanner.java /////
{
    /////////////////////////////////////////////////////////////////
    // THIS CLASS IS AUTOMATICALLY GENERATED FROM jml.g.  DO NOT   //
    // EDIT THIS FILE.  EDIT THE GRAMMAR INSTEAD.                  //
    /////////////////////////////////////////////////////////////////
  
    protected boolean isNestedInterpreter = false;

    protected JmlContext context = new JmlContext();

    protected boolean inTable=false;

    protected JmlInterpreter interpreter;

    // XXX move this to Context, add StringBuffer interface 
    protected StringBuffer sb = new StringBuffer();

    // CONSTRUCTORS /////////////////////////////////////////////////
    public JmlScanner(Reader reader, JmlContext ctx) {
        this(reader);
        context     = ctx;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    protected JmlContext getContext() {
        return context;
    }
    protected void setContext (JmlContext ctx) {
        if (ctx == null)
            throw new NullPointerException("attempt to set null context");
        context = ctx;
    }
    protected JmlInterpreter getInterpreter() {
        return interpreter;
    }
    public boolean inTable() {
        return inTable;
    }
    // OUTPUT TEXT WITHOUT MARKUP ///////////////////////////////////
    protected void outputAnyText() {
        if (sb.length() > 0) {
            interpreter.text(sb.toString());
            sb.delete(0, sb.length());
        } 
    }
    // ABSTRACT METHODS /////////////////////////////////////////////
    public abstract void interpret(JmlInterpreter interpreter)
                                throws IOException, TokenStreamException;

    // DOCUMENT ///////////////////////////////////////////
    protected abstract void author(String name, String email);
    protected abstract void title(String title);
    // LISTS //////////////////////////////////////////////
    protected abstract void ul(int level);
    protected abstract void ol(int level);
    protected abstract void closeLists();
    // SECTIONS ///////////////////////////////////////////
    protected abstract void startSection(String text, int level);
    // VARIABLES //////////////////////////////////////////
    protected abstract void bind(String id, String value);
    protected abstract void variable(String id);
    // FUNCTIONS //////////////////////////////////////////
    protected abstract String c(String xx);
    protected abstract void eval(String text);
    protected abstract void libcall(String id, Vector args);
}

/////////////////////////////////////////////////////////////////////
// RULES ////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////

BOLD
    :   {outputAnyText();} '*'! (~'*')+ '*'! {interpreter.bold($getText);}
    ;

ITALIC
    :   {outputAnyText();} '_'! (~'_')+ '_'! {interpreter.italic($getText);}
    ;

TT  :   {outputAnyText();} '`'! (~'`')+ '`'! {interpreter.tt($getText);}
    ;

// LINKS ////////////////////////////////////////////////////////////

LINK
{
    String url=null, title=null;
}
    :   {getLine()!=1}? // distinguish from title
        {outputAnyText();}
        (    "@("!
            (~(')'|','))+    {url=$getText; $setText("");}
              ( ','! (~(')'))+ {title=$getText;} )?
            ')'! {interpreter.link(url,title);}
        |    "http://" (~(' '|'\n'))+
            {interpreter.link($getText,null);}
        )
    ;

// First line of non-nested input assumed to be title

TITLE_LINE
    :   {getLine()==1&&getColumn()==1&&!isNestedInterpreter}?
        (options{greedy=true;}:' '!|'\t'!)* (TITLE_CHAR)+
        {title($getText);}
    ;

protected
TITLE_CHAR
    :    ~('{' | '}' | '`' | '@' | '*' | '_' | '#' | '\n' | '-' )
    ;

LINEBREAK  
    :   '\\'! '\n'!   
        {newline(); outputAnyText(); interpreter.linebreak();}
    ;

BLANKLINE
    :   '\n'! '\\'! '\n'!   
        {newline(); outputAnyText(); interpreter.blankline();}
    ;

// FUNCTIONS AND VARIABLES //////////////////////////////////////////

FUNC_OR_VAR:
        { Vector args=null; }
        (
            options {
                // the grammar is ambiguous; all names match libcall
                generateAmbigWarnings=false;
            }
        // BUILT-IN FUNCTIONS ///////////////////////////////////////
        :
            "%author"! '(' aName:PARAM   ',' WS email:PARAM ')'
            {
                author(aName.getText(),email.getText());
            }
        // possibly should be recognized only in line 1 or when recursing
        |   "%bind"!   '(' id_:ID      ',' WS a_:PARAM ')'
            {bind(id_.getText(),a_.getText());}
        |   "%c"! '(' xx:ID ')'
                {outputAnyText(); interpreter.text(c(xx.getText()));}
        // EVAL GOES HERE
    //  |   "%eval"! WS! text_:BLOCK
    //      {eval(text_.getText());}
        |   "%raw"! WS! out:BLOCK
            {outputAnyText(); interpreter.rawOutput(out.getText());}
        // VARIABLES AND LIBRARY CALLS
        |   '%' id2:ID
            (   '(' ')'              {libcall(id2.getText(), null);}
            |   '(' args=CS_PARAM ')' {libcall(id2.getText(), args);}
            |    WS! arg:BLOCK         {Vector v=new Vector();
                                      v.addElement(arg.getText());
                                      libcall(id2.getText(), v);
                                     }
            |   {variable(id2.getText());}
            ) 
        )
    ;

// accept one or more comma-separated args
protected
CS_PARAM returns [Vector args=new Vector()]
    :   arg1:PARAM {args.addElement(arg1.getText());}
        ( ',' WS arg2:PARAM {args.addElement(arg2.getText());} )*
    ;

// an argument might be either a DQUOTED string, general text, or 
// a reference to a variable
protected
PARAM :   
        DQUOTED_PARAM
    |   PARAM_TEXT
    |   '%' id:ID
        {String value = (context.lookup(id.getText())).toString();
         $setText(value);
        }
    ;

protected
DQUOTED_PARAM
    :   '"'! (~'"')* '"'!
    ;

protected
PARAM_TEXT
    :   ( ~('"'|','|')'|'%') )+
    ;

// ID CHARACTERS ////////////////////////////////////////////////////
// an id is an alpha (including underscore) plus zero or more alphanumeric
protected
ID  :   ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
    ;

// ESCAPING /////////////////////////////////////////////////////////
ESC :   '\\'! c:~'\n' {sb.append(c);}
    ;

protected
ESC2:   '\\'! c:~'\n'
    ;

/////////////////////////////////////////////////////////////////////
// LEFT EDGE MARKUP /////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////

LINE_START_THEN_MARKUP
    :   {getColumn()==1}? LEFT_EDGE_MARKUP
    ;

BLANK_LINE_THEN_MARKUP
    :   "\n\n"! {newline(); newline(); sb.append('\n');} LEFT_EDGE_MARKUP
    ;

protected
LEFT_EDGE_MARKUP
{
    int level = JmlContext.LOWEST_LEVEL;
}
    :
        (
            {!inTable}?
            ("  " {level++;} )*
            (   "o "!       {outputAnyText(); ul(level);}
            |   "1 "!       {outputAnyText(); ol(level);}
            )
        |   BLOCKQUOTE  {outputAnyText(); closeLists();
                         interpreter.blockquote($getText);}
        |   BLOCK        {outputAnyText(); interpreter.code($getText);}
        |   SECTION
        |   BEGIN_TABLE       {inTable=true;}
        |   END_TABLE   {inTable=false;}
        )
    ;

// flagged by SPACE-DQUOTE at start of line
protected
BLOCKQUOTE
    :   "  \""! ('\n'{newline();}|~('\n'|'"'))+ '"'!
    ;

// flagged by LT-LT at start of line
protected
BLOCK:   "{{"! (options {greedy=false;}:'\n'{newline();}|ESC2|.)* "}}"!
    ;

// SECTIONS /////////////////////////////////////////////////////////

// section titles flagged by SHARP+ at start of line
protected
SECTION
    { outputAnyText(); }
    :   "# "!     secTitle1:TITLE NL! 
                  {startSection(secTitle1.getText(), 1);}
    |   "## "!    secTitle2:TITLE NL! 
                  {startSection(secTitle2.getText(), 2);}
    |   "### "!   secTitle3:TITLE NL! 
                  {startSection(secTitle3.getText(), 3);}
    |   "#### "!  secTitle4:TITLE NL! 
                  {startSection(secTitle4.getText(), 4);}
    ;

protected
TITLE 
    :   (~('#'|'\n'))+
    ;
protected
NL
    :    ('\n' {newline();} ('\n' {newline();})? )!
    ;
    
// TABLES ///////////////////////////////////////////////////////////

// LBRACKET at start of line
protected
BEGIN_TABLE
    :   '['! NL! {inTable=true; outputAnyText(); interpreter.beginTable();}
    ;

// LBRACKET at start of line
protected
END_TABLE
    :   ']'! NL! {inTable=false; outputAnyText(); interpreter.endTable();}
    ;

// BAR anywhere on line 
COL
    :   {inTable}? '|'!
        {outputAnyText(); interpreter.col();}
    ;

// DBAR anywhere on line - consume any newline found
ROW
    :   {inTable}? "||"! (NL)?! 
        {outputAnyText(); interpreter.row();}
    ;

// WHITESPACE ///////////////////////////////////////////////////////
protected
WS! :   (options {greedy=true;}:' '|'\t'|'\n'{newline();})*
    ;

protected
WS2!:   (options {greedy=true;}:' '|'\t'|'\n'{newline();})+
    ;

/////////////////////////////////////////////////////////////////////
protected
OTHERWISE:   
        "\n\n"  {
                    newline(); 
                    newline();
                    outputAnyText();
                    closeLists();
                    interpreter.paragraph();
                }
    |   '\n'    {newline(); sb.append('\n');}
    |   c:~'\n' {sb.append(c);}
    ;

