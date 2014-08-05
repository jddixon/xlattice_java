/* JmlInterpreter.java */
package org.xlattice.projmgr.jml;

import java.io.IOException;

/**
 * @author Jim Dixon
 */

public interface JmlInterpreter {

    // DOCUMENT /////////////////////////////////////////////////////
    public void author(String name, String email);
    public void begin();
    public void end();
    public void title       (String s);
    
    // LINE BREAKS //////////////////////////////////////////////////
    public void blankline();
    public void linebreak();
    public void paragraph();

    // SIMPLE TEXT OUTPUT ///////////////////////////////////////////
    /** Escape where necessary. */
    public void text        (String s);
    /** Output without any change at all. */
    public void rawOutput   (String s);
   
    // TEXT REFORMATTING ////////////////////////////////////////////
    public void blockquote  (String s);
    public void bold        (String s);
    public void code        (String s);
    public void italic      (String s);
    public void link        (String url);
    public void link        (String url, String title);
    public void tt          (String s);

    // LISTS ////////////////////////////////////////////////////////
    public void beginOL (int n);
    public void endOL (int n);
    public void beginUL (int n);
    public void endUL (int n);
    public void beginListItem(int n);
    public void endListItem(int n);
    
    // SECTIONS /////////////////////////////////////////////////////
    public void beginSection (String title, int n);
    public void endSection (int n);
    public void beginSectionList (int n);
    public void endSectionList (int n);

    // TABLES ///////////////////////////////////////////////////////
    public void beginTable();
    public void endTable();
    public void col();
    public void row();
}
