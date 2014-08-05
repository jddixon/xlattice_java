/* JmlScanner.java */

/**
 * @author Jim Dixon
 */

// $ANTLR 2.7.4: "jml.g" -> "JmlScanner.java"$

    package org.xlattice.projmgr.jml;
    import java.util.*;
    import java.io.*;

import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

public abstract class JmlScanner extends antlr.CharScanner implements JmlScannerTokenTypes, TokenStream
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
public JmlScanner(InputStream in) {
	this(new ByteBuffer(in));
}
public JmlScanner(Reader in) {
	this(new CharBuffer(in));
}
public JmlScanner(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public JmlScanner(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = true;
	setCaseSensitive(true);
	literals = new Hashtable();
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		setCommitToPath(false);
		int _m;
		_m = mark();
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '*':
				{
					mBOLD(true);
					theRetToken=_returnToken;
					break;
				}
				case '_':
				{
					mITALIC(true);
					theRetToken=_returnToken;
					break;
				}
				case '`':
				{
					mTT(true);
					theRetToken=_returnToken;
					break;
				}
				default:
					if (((LA(1)=='@'||LA(1)=='h') && (LA(2)=='('||LA(2)=='t') && (_tokenSet_0.member(LA(3))) && ((LA(4) >= '\u0003' && LA(4) <= '\u00ff')))&&(getLine()!=1)) {
						mLINK(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='\\') && (LA(2)=='\n')) {
						mLINEBREAK(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='\n') && (LA(2)=='\\')) {
						mBLANKLINE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='%') && (_tokenSet_1.member(LA(2))) && (true) && (true)) {
						mFUNC_OR_VAR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='\\') && (_tokenSet_2.member(LA(2))) && (true) && (true)) {
						mESC(true);
						theRetToken=_returnToken;
					}
					else if (((_tokenSet_3.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (true) && (true))&&(getColumn()==1)) {
						mLINE_START_THEN_MARKUP(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='\n') && (LA(2)=='\n')) {
						mBLANK_LINE_THEN_MARKUP(true);
						theRetToken=_returnToken;
					}
					else if (((LA(1)=='|') && (LA(2)=='|') && (true) && (true))&&(inTable)) {
						mROW(true);
						theRetToken=_returnToken;
					}
					else if (((_tokenSet_5.member(LA(1))) && (true) && (true) && (true))&&(getLine()==1&&getColumn()==1&&!isNestedInterpreter)) {
						mTITLE_LINE(true);
						theRetToken=_returnToken;
					}
					else if (((LA(1)=='|') && (true) && (true) && (true))&&(inTable)) {
						mCOL(true);
						theRetToken=_returnToken;
					}
				else {
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {
					commit();
					try {mOTHERWISE(false);}
					catch(RecognitionException e) {
						// catastrophic failure
						reportError(e);
						consume();
					}
					continue tryAgain;
				}
				}
				}
				commit();
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_ttype = testLiteralsTable(_ttype);
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				if ( !getCommitToPath() ) {
					rewind(_m);
					resetText();
					try {mOTHERWISE(false);}
					catch(RecognitionException ee) {
						// horrendous failure: error in filter rule
						reportError(ee);
						consume();
					}
					continue tryAgain;
				}
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mBOLD(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BOLD;
		int _saveIndex;
		
		outputAnyText();
		_saveIndex=text.length();
		match('*');
		text.setLength(_saveIndex);
		{
		int _cnt3=0;
		_loop3:
		do {
			if ((_tokenSet_6.member(LA(1)))) {
				matchNot('*');
			}
			else {
				if ( _cnt3>=1 ) { break _loop3; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt3++;
		} while (true);
		}
		_saveIndex=text.length();
		match('*');
		text.setLength(_saveIndex);
		interpreter.bold(new String(text.getBuffer(),_begin,text.length()-_begin));
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mITALIC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ITALIC;
		int _saveIndex;
		
		outputAnyText();
		_saveIndex=text.length();
		match('_');
		text.setLength(_saveIndex);
		{
		int _cnt6=0;
		_loop6:
		do {
			if ((_tokenSet_7.member(LA(1)))) {
				matchNot('_');
			}
			else {
				if ( _cnt6>=1 ) { break _loop6; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt6++;
		} while (true);
		}
		_saveIndex=text.length();
		match('_');
		text.setLength(_saveIndex);
		interpreter.italic(new String(text.getBuffer(),_begin,text.length()-_begin));
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mTT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = TT;
		int _saveIndex;
		
		outputAnyText();
		_saveIndex=text.length();
		match('`');
		text.setLength(_saveIndex);
		{
		int _cnt9=0;
		_loop9:
		do {
			if ((_tokenSet_8.member(LA(1)))) {
				matchNot('`');
			}
			else {
				if ( _cnt9>=1 ) { break _loop9; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt9++;
		} while (true);
		}
		_saveIndex=text.length();
		match('`');
		text.setLength(_saveIndex);
		interpreter.tt(new String(text.getBuffer(),_begin,text.length()-_begin));
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLINK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LINK;
		int _saveIndex;
		
		String url=null, title=null;
		
		
		if (!(getLine()!=1))
		  throw new SemanticException("getLine()!=1");
		outputAnyText();
		{
		switch ( LA(1)) {
		case '@':
		{
			_saveIndex=text.length();
			match("@(");
			text.setLength(_saveIndex);
			{
			int _cnt14=0;
			_loop14:
			do {
				if ((_tokenSet_0.member(LA(1)))) {
					{
					match(_tokenSet_0);
					}
				}
				else {
					if ( _cnt14>=1 ) { break _loop14; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt14++;
			} while (true);
			}
			url=new String(text.getBuffer(),_begin,text.length()-_begin); text.setLength(_begin); text.append("");
			{
			switch ( LA(1)) {
			case ',':
			{
				_saveIndex=text.length();
				match(',');
				text.setLength(_saveIndex);
				{
				int _cnt18=0;
				_loop18:
				do {
					if ((_tokenSet_9.member(LA(1)))) {
						{
						match(_tokenSet_9);
						}
					}
					else {
						if ( _cnt18>=1 ) { break _loop18; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt18++;
				} while (true);
				}
				title=new String(text.getBuffer(),_begin,text.length()-_begin);
				break;
			}
			case ')':
			{
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			_saveIndex=text.length();
			match(')');
			text.setLength(_saveIndex);
			interpreter.link(url,title);
			break;
		}
		case 'h':
		{
			match("http://");
			{
			int _cnt21=0;
			_loop21:
			do {
				if ((_tokenSet_10.member(LA(1)))) {
					{
					match(_tokenSet_10);
					}
				}
				else {
					if ( _cnt21>=1 ) { break _loop21; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt21++;
			} while (true);
			}
			interpreter.link(new String(text.getBuffer(),_begin,text.length()-_begin),null);
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mTITLE_LINE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = TITLE_LINE;
		int _saveIndex;
		
		if (!(getLine()==1&&getColumn()==1&&!isNestedInterpreter))
		  throw new SemanticException("getLine()==1&&getColumn()==1&&!isNestedInterpreter");
		{
		_loop24:
		do {
			if ((LA(1)==' ') && (_tokenSet_5.member(LA(2))) && (true) && (true)) {
				_saveIndex=text.length();
				match(' ');
				text.setLength(_saveIndex);
			}
			else if ((LA(1)=='\t') && (_tokenSet_5.member(LA(2))) && (true) && (true)) {
				_saveIndex=text.length();
				match('\t');
				text.setLength(_saveIndex);
			}
			else {
				break _loop24;
			}
			
		} while (true);
		}
		{
		int _cnt26=0;
		_loop26:
		do {
			if ((_tokenSet_5.member(LA(1)))) {
				mTITLE_CHAR(false);
			}
			else {
				if ( _cnt26>=1 ) { break _loop26; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt26++;
		} while (true);
		}
		title(new String(text.getBuffer(),_begin,text.length()-_begin));
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mTITLE_CHAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = TITLE_CHAR;
		int _saveIndex;
		
		{
		match(_tokenSet_5);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLINEBREAK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LINEBREAK;
		int _saveIndex;
		
		_saveIndex=text.length();
		match('\\');
		text.setLength(_saveIndex);
		_saveIndex=text.length();
		match('\n');
		text.setLength(_saveIndex);
		newline(); outputAnyText(); interpreter.linebreak();
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBLANKLINE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BLANKLINE;
		int _saveIndex;
		
		_saveIndex=text.length();
		match('\n');
		text.setLength(_saveIndex);
		_saveIndex=text.length();
		match('\\');
		text.setLength(_saveIndex);
		_saveIndex=text.length();
		match('\n');
		text.setLength(_saveIndex);
		newline(); outputAnyText(); interpreter.blankline();
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mFUNC_OR_VAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = FUNC_OR_VAR;
		int _saveIndex;
		Token aName=null;
		Token email=null;
		Token id_=null;
		Token a_=null;
		Token xx=null;
		Token out=null;
		Token id2=null;
		Token arg=null;
		
		Vector args=null;
		{
		if ((LA(1)=='%') && (LA(2)=='a') && (LA(3)=='u') && (LA(4)=='t')) {
			_saveIndex=text.length();
			match("%author");
			text.setLength(_saveIndex);
			match('(');
			mPARAM(true);
			aName=_returnToken;
			match(',');
			mWS(false);
			mPARAM(true);
			email=_returnToken;
			match(')');
			
			author(aName.getText(),email.getText());
			
		}
		else if ((LA(1)=='%') && (LA(2)=='b') && (LA(3)=='i') && (LA(4)=='n')) {
			_saveIndex=text.length();
			match("%bind");
			text.setLength(_saveIndex);
			match('(');
			mID(true);
			id_=_returnToken;
			match(',');
			mWS(false);
			mPARAM(true);
			a_=_returnToken;
			match(')');
			bind(id_.getText(),a_.getText());
		}
		else if ((LA(1)=='%') && (LA(2)=='c') && (LA(3)=='(') && (_tokenSet_1.member(LA(4)))) {
			_saveIndex=text.length();
			match("%c");
			text.setLength(_saveIndex);
			match('(');
			mID(true);
			xx=_returnToken;
			match(')');
			outputAnyText(); interpreter.text(c(xx.getText()));
		}
		else if ((LA(1)=='%') && (LA(2)=='r') && (LA(3)=='a') && (LA(4)=='w')) {
			_saveIndex=text.length();
			match("%raw");
			text.setLength(_saveIndex);
			_saveIndex=text.length();
			mWS(false);
			text.setLength(_saveIndex);
			mBLOCK(true);
			out=_returnToken;
			outputAnyText(); interpreter.rawOutput(out.getText());
		}
		else if ((LA(1)=='%') && (_tokenSet_1.member(LA(2))) && (true) && (true)) {
			match('%');
			mID(true);
			id2=_returnToken;
			{
			if ((LA(1)=='(') && (LA(2)==')')) {
				match('(');
				match(')');
				libcall(id2.getText(), null);
			}
			else if ((LA(1)=='(') && (_tokenSet_0.member(LA(2)))) {
				match('(');
				args=mCS_PARAM(false);
				match(')');
				libcall(id2.getText(), args);
			}
			else if ((_tokenSet_11.member(LA(1)))) {
				_saveIndex=text.length();
				mWS(false);
				text.setLength(_saveIndex);
				mBLOCK(true);
				arg=_returnToken;
				Vector v=new Vector();
				v.addElement(arg.getText());
				libcall(id2.getText(), v);
				
			}
			else {
				variable(id2.getText());
			}
			
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mPARAM(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PARAM;
		int _saveIndex;
		Token id=null;
		
		switch ( LA(1)) {
		case '"':
		{
			mDQUOTED_PARAM(false);
			break;
		}
		case '%':
		{
			match('%');
			mID(true);
			id=_returnToken;
			String value = (context.lookup(id.getText())).toString();
			text.setLength(_begin); text.append(value);
			
			break;
		}
		default:
			if ((_tokenSet_12.member(LA(1)))) {
				mPARAM_TEXT(false);
			}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS;
		int _saveIndex;
		
		{
		_loop80:
		do {
			if ((LA(1)==' ') && (_tokenSet_0.member(LA(2))) && ((LA(3) >= '\u0003' && LA(3) <= '\u00ff')) && (true)) {
				_saveIndex=text.length();
				match(' ');
				text.setLength(_saveIndex);
			}
			else if ((LA(1)=='\t') && (_tokenSet_0.member(LA(2))) && ((LA(3) >= '\u0003' && LA(3) <= '\u00ff')) && (true)) {
				_saveIndex=text.length();
				match('\t');
				text.setLength(_saveIndex);
			}
			else if ((LA(1)=='\n') && (_tokenSet_0.member(LA(2))) && ((LA(3) >= '\u0003' && LA(3) <= '\u00ff')) && (true)) {
				_saveIndex=text.length();
				match('\n');
				text.setLength(_saveIndex);
				newline();
			}
			else {
				break _loop80;
			}
			
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mID(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ID;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			matchRange('a','z');
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':
		{
			matchRange('A','Z');
			break;
		}
		case '_':
		{
			match('_');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		_loop48:
		do {
			switch ( LA(1)) {
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':  case 'g':  case 'h':
			case 'i':  case 'j':  case 'k':  case 'l':
			case 'm':  case 'n':  case 'o':  case 'p':
			case 'q':  case 'r':  case 's':  case 't':
			case 'u':  case 'v':  case 'w':  case 'x':
			case 'y':  case 'z':
			{
				matchRange('a','z');
				break;
			}
			case 'A':  case 'B':  case 'C':  case 'D':
			case 'E':  case 'F':  case 'G':  case 'H':
			case 'I':  case 'J':  case 'K':  case 'L':
			case 'M':  case 'N':  case 'O':  case 'P':
			case 'Q':  case 'R':  case 'S':  case 'T':
			case 'U':  case 'V':  case 'W':  case 'X':
			case 'Y':  case 'Z':
			{
				matchRange('A','Z');
				break;
			}
			case '_':
			{
				match('_');
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				matchRange('0','9');
				break;
			}
			default:
			{
				break _loop48;
			}
			}
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mBLOCK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BLOCK;
		int _saveIndex;
		
		_saveIndex=text.length();
		match("{{");
		text.setLength(_saveIndex);
		{
		_loop64:
		do {
			// nongreedy exit test
			if ((LA(1)=='}') && (LA(2)=='}') && (true)) break _loop64;
			if ((LA(1)=='\\') && (_tokenSet_2.member(LA(2))) && ((LA(3) >= '\u0003' && LA(3) <= '\u00ff')) && ((LA(4) >= '\u0003' && LA(4) <= '\u00ff'))) {
				mESC2(false);
			}
			else if ((LA(1)=='\n') && ((LA(2) >= '\u0003' && LA(2) <= '\u00ff')) && ((LA(3) >= '\u0003' && LA(3) <= '\u00ff')) && (true)) {
				match('\n');
				newline();
			}
			else if (((LA(1) >= '\u0003' && LA(1) <= '\u00ff')) && ((LA(2) >= '\u0003' && LA(2) <= '\u00ff')) && ((LA(3) >= '\u0003' && LA(3) <= '\u00ff')) && (true)) {
				matchNot(EOF_CHAR);
			}
			else {
				break _loop64;
			}
			
		} while (true);
		}
		_saveIndex=text.length();
		match("}}");
		text.setLength(_saveIndex);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final Vector  mCS_PARAM(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		Vector args=new Vector();
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CS_PARAM;
		int _saveIndex;
		Token arg1=null;
		Token arg2=null;
		
		mPARAM(true);
		arg1=_returnToken;
		args.addElement(arg1.getText());
		{
		_loop36:
		do {
			if ((LA(1)==',')) {
				match(',');
				mWS(false);
				mPARAM(true);
				arg2=_returnToken;
				args.addElement(arg2.getText());
			}
			else {
				break _loop36;
			}
			
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
		return args;
	}
	
	protected final void mDQUOTED_PARAM(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DQUOTED_PARAM;
		int _saveIndex;
		
		_saveIndex=text.length();
		match('"');
		text.setLength(_saveIndex);
		{
		_loop40:
		do {
			if ((_tokenSet_13.member(LA(1)))) {
				matchNot('"');
			}
			else {
				break _loop40;
			}
			
		} while (true);
		}
		_saveIndex=text.length();
		match('"');
		text.setLength(_saveIndex);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mPARAM_TEXT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PARAM_TEXT;
		int _saveIndex;
		
		{
		int _cnt44=0;
		_loop44:
		do {
			if ((_tokenSet_12.member(LA(1)))) {
				{
				match(_tokenSet_12);
				}
			}
			else {
				if ( _cnt44>=1 ) { break _loop44; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt44++;
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mESC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESC;
		int _saveIndex;
		char  c = '\0';
		
		_saveIndex=text.length();
		match('\\');
		text.setLength(_saveIndex);
		c = LA(1);
		matchNot('\n');
		sb.append(c);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mESC2(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESC2;
		int _saveIndex;
		char  c = '\0';
		
		_saveIndex=text.length();
		match('\\');
		text.setLength(_saveIndex);
		c = LA(1);
		matchNot('\n');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLINE_START_THEN_MARKUP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LINE_START_THEN_MARKUP;
		int _saveIndex;
		
		if (!(getColumn()==1))
		  throw new SemanticException("getColumn()==1");
		mLEFT_EDGE_MARKUP(false);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mLEFT_EDGE_MARKUP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LEFT_EDGE_MARKUP;
		int _saveIndex;
		
		int level = JmlContext.LOWEST_LEVEL;
		
		
		{
		switch ( LA(1)) {
		case '{':
		{
			mBLOCK(false);
			outputAnyText(); interpreter.code(new String(text.getBuffer(),_begin,text.length()-_begin));
			break;
		}
		case '#':
		{
			mSECTION(false);
			break;
		}
		case '[':
		{
			mBEGIN_TABLE(false);
			inTable=true;
			break;
		}
		case ']':
		{
			mEND_TABLE(false);
			inTable=false;
			break;
		}
		default:
			if ((LA(1)==' ') && (LA(2)==' ') && (LA(3)=='"')) {
				mBLOCKQUOTE(false);
				outputAnyText(); closeLists();
				interpreter.blockquote(new String(text.getBuffer(),_begin,text.length()-_begin));
			}
			else if (((LA(1)==' '||LA(1)=='1'||LA(1)=='o') && (LA(2)==' ') && (true))&&(!inTable)) {
				{
				_loop56:
				do {
					if ((LA(1)==' ')) {
						match("  ");
						level++;
					}
					else {
						break _loop56;
					}
					
				} while (true);
				}
				{
				switch ( LA(1)) {
				case 'o':
				{
					_saveIndex=text.length();
					match("o ");
					text.setLength(_saveIndex);
					outputAnyText(); ul(level);
					break;
				}
				case '1':
				{
					_saveIndex=text.length();
					match("1 ");
					text.setLength(_saveIndex);
					outputAnyText(); ol(level);
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
			}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBLANK_LINE_THEN_MARKUP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BLANK_LINE_THEN_MARKUP;
		int _saveIndex;
		
		_saveIndex=text.length();
		match("\n\n");
		text.setLength(_saveIndex);
		newline(); newline(); sb.append('\n');
		mLEFT_EDGE_MARKUP(false);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mBLOCKQUOTE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BLOCKQUOTE;
		int _saveIndex;
		
		_saveIndex=text.length();
		match("  \"");
		text.setLength(_saveIndex);
		{
		int _cnt61=0;
		_loop61:
		do {
			if ((LA(1)=='\n')) {
				match('\n');
				newline();
			}
			else if ((_tokenSet_14.member(LA(1)))) {
				{
				match(_tokenSet_14);
				}
			}
			else {
				if ( _cnt61>=1 ) { break _loop61; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt61++;
		} while (true);
		}
		_saveIndex=text.length();
		match('"');
		text.setLength(_saveIndex);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mSECTION(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SECTION;
		int _saveIndex;
		Token secTitle1=null;
		Token secTitle2=null;
		Token secTitle3=null;
		Token secTitle4=null;
		outputAnyText();
		
		if ((LA(1)=='#') && (LA(2)=='#') && (LA(3)=='#') && (LA(4)==' ')) {
			_saveIndex=text.length();
			match("### ");
			text.setLength(_saveIndex);
			mTITLE(true);
			secTitle3=_returnToken;
			_saveIndex=text.length();
			mNL(false);
			text.setLength(_saveIndex);
			startSection(secTitle3.getText(), 3);
		}
		else if ((LA(1)=='#') && (LA(2)=='#') && (LA(3)=='#') && (LA(4)=='#')) {
			_saveIndex=text.length();
			match("#### ");
			text.setLength(_saveIndex);
			mTITLE(true);
			secTitle4=_returnToken;
			_saveIndex=text.length();
			mNL(false);
			text.setLength(_saveIndex);
			startSection(secTitle4.getText(), 4);
		}
		else if ((LA(1)=='#') && (LA(2)=='#') && (LA(3)==' ')) {
			_saveIndex=text.length();
			match("## ");
			text.setLength(_saveIndex);
			mTITLE(true);
			secTitle2=_returnToken;
			_saveIndex=text.length();
			mNL(false);
			text.setLength(_saveIndex);
			startSection(secTitle2.getText(), 2);
		}
		else if ((LA(1)=='#') && (LA(2)==' ')) {
			_saveIndex=text.length();
			match("# ");
			text.setLength(_saveIndex);
			mTITLE(true);
			secTitle1=_returnToken;
			_saveIndex=text.length();
			mNL(false);
			text.setLength(_saveIndex);
			startSection(secTitle1.getText(), 1);
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mBEGIN_TABLE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BEGIN_TABLE;
		int _saveIndex;
		
		_saveIndex=text.length();
		match('[');
		text.setLength(_saveIndex);
		_saveIndex=text.length();
		mNL(false);
		text.setLength(_saveIndex);
		inTable=true; outputAnyText(); interpreter.beginTable();
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mEND_TABLE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = END_TABLE;
		int _saveIndex;
		
		_saveIndex=text.length();
		match(']');
		text.setLength(_saveIndex);
		_saveIndex=text.length();
		mNL(false);
		text.setLength(_saveIndex);
		inTable=false; outputAnyText(); interpreter.endTable();
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mTITLE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = TITLE;
		int _saveIndex;
		
		{
		int _cnt69=0;
		_loop69:
		do {
			if ((_tokenSet_15.member(LA(1)))) {
				{
				match(_tokenSet_15);
				}
			}
			else {
				if ( _cnt69>=1 ) { break _loop69; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt69++;
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mNL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NL;
		int _saveIndex;
		
		{
		match('\n');
		newline();
		{
		if ((LA(1)=='\n')) {
			match('\n');
			newline();
		}
		else {
		}
		
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COL;
		int _saveIndex;
		
		if (!(inTable))
		  throw new SemanticException("inTable");
		_saveIndex=text.length();
		match('|');
		text.setLength(_saveIndex);
		outputAnyText(); interpreter.col();
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mROW(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ROW;
		int _saveIndex;
		
		if (!(inTable))
		  throw new SemanticException("inTable");
		_saveIndex=text.length();
		match("||");
		text.setLength(_saveIndex);
		{
		if ((LA(1)=='\n')) {
			mNL(false);
		}
		else {
		}
		
		}
		outputAnyText(); interpreter.row();
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mWS2(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS2;
		int _saveIndex;
		
		{
		int _cnt83=0;
		_loop83:
		do {
			switch ( LA(1)) {
			case ' ':
			{
				_saveIndex=text.length();
				match(' ');
				text.setLength(_saveIndex);
				break;
			}
			case '\t':
			{
				_saveIndex=text.length();
				match('\t');
				text.setLength(_saveIndex);
				break;
			}
			case '\n':
			{
				_saveIndex=text.length();
				match('\n');
				text.setLength(_saveIndex);
				newline();
				break;
			}
			default:
			{
				if ( _cnt83>=1 ) { break _loop83; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			}
			_cnt83++;
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mOTHERWISE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = OTHERWISE;
		int _saveIndex;
		char  c = '\0';
		
		if ((LA(1)=='\n') && (LA(2)=='\n')) {
			match("\n\n");
			
			newline(); 
			newline();
			outputAnyText();
			closeLists();
			interpreter.paragraph();
			
		}
		else if ((LA(1)=='\n') && (true)) {
			match('\n');
			newline(); sb.append('\n');
		}
		else if ((_tokenSet_2.member(LA(1)))) {
			c = LA(1);
			matchNot('\n');
			sb.append(c);
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[8];
		data[0]=-19791209299976L;
		for (int i = 1; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 0L, 576460745995190270L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[8];
		data[0]=-1032L;
		for (int i = 1; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 562988608126976L, 576601490462867456L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 38654706688L, 576460752303423488L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = new long[8];
		data[0]=-39616778339336L;
		data[1]=-2882303767959568386L;
		for (int i = 2; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = new long[8];
		data[0]=-4398046511112L;
		for (int i = 1; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = new long[8];
		data[0]=-8L;
		data[1]=-2147483649L;
		for (int i = 2; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = new long[8];
		data[0]=-8L;
		data[1]=-4294967297L;
		for (int i = 2; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = new long[8];
		data[0]=-2199023255560L;
		for (int i = 1; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = new long[8];
		data[0]=-4294968328L;
		for (int i = 1; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 4294968832L, 576460752303423488L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = new long[8];
		data[0]=-19945828122632L;
		for (int i = 1; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = new long[8];
		data[0]=-17179869192L;
		for (int i = 1; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = new long[8];
		data[0]=-17179870216L;
		for (int i = 1; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = new long[8];
		data[0]=-34359739400L;
		for (int i = 1; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	
	}
