/* JmlContext.java */
package org.xlattice.projmgr.jml;

import org.xlattice.Context;

/**
 * @author Jim Dixon
 */

public class JmlContext extends Context {

    private final StringBuffer sb = new StringBuffer();
    
    public static final int LOWEST_LEVEL = 1;   // compatibility
    
    // CONSTRUCTORS /////////////////////////////////////////////////

    // XXX superclass has a Context(parent) constructor  
    public JmlContext () {
    }
        
    public boolean isNestedInterpreter() {
        return false;   // STUB !!!
    }

    // TEXT BEING COLLECTED /////////////////////////////////////////
//  public void appendChar (char c) {
//      sb.append(c);
//  }
//  public void appendText (String s) {
//      sb.append(s);
//  }
//  /** on probation */
//  protected void clearText() {
//      sb.delete(0, sb.length());
//  }
//  public String getText() {
//      String text = sb.toString();
//      clearText();
//      if (text == null)
//          return "";
//      else
//          return text;
//  }
//  /** debug */
//  public String peekText() {
//      return sb.toString();
//  }
}
