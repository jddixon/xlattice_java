/* JmlWriter.java */
package org.xlattice.projmgr.jml;

import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author Jim Dixon
 */
public abstract class JmlWriter implements JmlInterpreter {

    protected final Writer writer;

    // CONSTRUCTOR //////////////////////////////////////////////////
    /** 
     * @param w typically a StringWriter
     */
    public JmlWriter(Writer w) {
        writer = w;
    }
    // GENERATE OUTPUT //////////////////////////////////////////////
   
    // LOW LEVEL WRITES ///////////////////////////////////
    protected final void print (String text) {
        if (text == null)
            text = "";
        try {
            writer.write(text);
        } catch (IOException ioe) {
            System.err.println("error writing interpreter output: ");
            ioe.printStackTrace(System.err);
        }
    }
    protected final void println (String text) {
        print(text);
        print("\n");
    }
}
