/* JmlScannerTokenTypes.java */

/**
 * @author Jim Dixon
 */

// $ANTLR 2.7.4: "jml.g" -> "JmlScanner.java"$

    package org.xlattice.projmgr.jml;
    import java.util.*;
    import java.io.*;

public interface JmlScannerTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int BOLD = 4;
	int ITALIC = 5;
	int TT = 6;
	int LINK = 7;
	int TITLE_LINE = 8;
	int TITLE_CHAR = 9;
	int LINEBREAK = 10;
	int BLANKLINE = 11;
	int FUNC_OR_VAR = 12;
	int CS_PARAM = 13;
	int PARAM = 14;
	int DQUOTED_PARAM = 15;
	int PARAM_TEXT = 16;
	int ID = 17;
	int ESC = 18;
	int ESC2 = 19;
	int LINE_START_THEN_MARKUP = 20;
	int BLANK_LINE_THEN_MARKUP = 21;
	int LEFT_EDGE_MARKUP = 22;
	int BLOCKQUOTE = 23;
	int BLOCK = 24;
	int SECTION = 25;
	int TITLE = 26;
	int NL = 27;
	int BEGIN_TABLE = 28;
	int END_TABLE = 29;
	int COL = 30;
	int ROW = 31;
	int WS = 32;
	int WS2 = 33;
	int OTHERWISE = 34;
}
