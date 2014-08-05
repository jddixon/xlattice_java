/* Main.java */
package org.xlattice.projmgr.jml;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

/**
 * @author Jim Dixon
 */

import antlr.TokenStreamException;

import org.xlattice.corexml.om.Document;
import org.xlattice.util.cmdline.*;

/**
 * XXX Need to replace references to '/' with File.separator.
 */
public class Main {

    // COMMAND LINE INTERFACE AND main() ////////////////////////////
    static CmdLineOpt[] options = {
        new StringOpt  ("i", "inFile",  "input file"),
        new StringOpt  ("o", "outFile", "output file"),
        new StringOpt  ("l", "listFile","list of input files"),
        new StringOpt  ("t", "target",  "target (x[doc], h[tml], m[ock])"),
        new BooleanOpt ("h", "showHelp","show help msg"),
        new BooleanOpt ("v", "verbose", "show version number and halt"),
    };
    
    // set defaults 
    boolean showHelp;
    boolean verbose; 
    String  inFile;
    String  outFile;
    String  listFile;
    String  target = "x";

    // methods for manipulating command line options
    String addDefaultExtension(String fileName, String ext) {
        if (fileName.indexOf('.') < 0)
            return new StringBuffer(fileName).append('.').append(ext)
                .toString();
        else
            return fileName;
    }
    /**
     * Simplistic method for removing extensions: drops anything
     * following a dot ('.').
     */
    String stripExtension (String name) {
        int dotPos = name.indexOf('.');
        if (dotPos < 0)
            return name;
        else 
            return name.substring(0,dotPos);
    }
    /**
     * If an input file is specified with the -i option, ignore
     * any file name passed after the arguments.  If there is no
     * extension on the input file name, add a .jml.  If no output
     * file is specified, the output file is written to the ../xdocs
     * directory.  By default the output extension is .xml.
     * 
     * If both an input file name and a list file are specified, the
     * list file is ignored and the rules above are followed.  If 
     * no input file is specified, but a list file is present, base
     * names are taken from the file name list and used for both input
     * and output.  In this case any output specified using the -o
     * option is ignored.
     *
     * If there is neither a -i infile name or -l list file name,
     * but there is a name after the command line options, that 
     * name is used to make both input and output file names.
     */
    public void handleCmdLine (String [] args, int next) {
        String inExt  = ".jml";
        String outExt = ".xml";     // default
        String pathPrefix = "";
        
        if (target.equals("h"))
            outExt = ".html";
        else if (target.equals("m"))
            outExt = ".mock";

        if (inFile != null) {
            System.out.println("inFile specified");
            String baseName = inFile;
            if (baseName.length() > 4)
                baseName = stripExtension(baseName);
            // strip off any prefix
            int slashPos = baseName.indexOf("/");
            if (slashPos >= 0) {
                // include the separator
                pathPrefix = baseName.substring(0, slashPos + 1);
                if (slashPos >= baseName.length() - 1)
                    throw new IllegalStateException(
                            "invalid input file name: " + baseName);
                baseName = baseName.substring(slashPos + 1);
            } 
            inFile  = new StringBuffer(pathPrefix)
                            .append(baseName).append(inExt).toString();
            if (outFile == null) {
                outFile = new StringBuffer("../xdocs/").append(pathPrefix)
                            .append(baseName).append(outExt).toString();
            } else {
                outFile = stripExtension(outFile);
                int slashPos2 = outFile.indexOf("/");
                if (slashPos2 < 0)
                    outFile = new StringBuffer("../xdocs/").append(pathPrefix)
                            .append(outFile).append(outExt).toString();
                else
                    outFile += outExt;
            }
        } else if (listFile != null) {
            System.out.println("*** can't handle list files yet ***");
            verbose = true;         // ie, print args and exit
        } else if (next < args.length) {
            System.out.println("base name specified");
            String baseName = stripExtension(args[next]);
            int slashPos = baseName.indexOf("/");
            if (slashPos >= 0) {
                // include the separator
                pathPrefix = baseName.substring(0, slashPos + 1);
                if (slashPos >= baseName.length() - 1)
                    throw new IllegalStateException(
                            "invalid input file name: " + baseName);
                baseName = baseName.substring(slashPos + 1);
            }
            inFile  = new StringBuffer(pathPrefix)
                            .append(baseName).append(inExt).toString();
            outFile = new StringBuffer("../xdocs/").append(pathPrefix)
                            .append(baseName).append(outExt).toString();
        }
        if (verbose) {
            StringBuffer sb = new StringBuffer()
                .append("input file:       ").append(inFile)
                .append("\noutput file:    ").append(outFile)
                .append("\nfile list:      ").append(listFile)
                .append("\ntarget:         ").append(target)
                .append("\nhelp requested? ").append(showHelp);
            if (next > args.length - 1)
                sb.append("\nnext = ").append(next)
                  .append(" but arg count is ").append(args.length);
            else 
                sb.append("\nnext:             ").append(args[next]);
            System.out.println(sb.toString());
            System.exit(0);
        }
        if (showHelp) {
            System.out.println("sorry, no help is available");
            System.out.flush();
            System.exit(0);
        }
        if (inFile == null) {
            System.out.println("exiting: no input file specified");
            System.exit(1);
        }
        if (outFile == null) {
            System.out.println("INTERNAL ERROR: no output file specified");
            System.exit(1);
        }
        if (!target.equals("x")) {
            System.out.println(
                    "sorry, only output to xdocs currently supported");
            System.exit(1);
        }
        Document doc = new Document();
        boolean successful = false;
        try {
            new JmlLexer (new JmlContext(), new FileReader(inFile))
                             .interpret(new XdocTarget(doc));
            successful = true;
        } catch (FileNotFoundException ioe) {
            System.out.println("can't open " + inFile);
        } catch (IOException ioe) {
            System.out.println("error reading " + inFile);
        } catch (TokenStreamException tse) {
            System.out.println("error parsing " + inFile);
        }
        if (successful) {
            try {
                BufferedWriter out 
                    = new BufferedWriter(new FileWriter(outFile));
                out.write(doc.toXml());
                out.flush();
            } catch (IOException ioe) {
                System.out.println("error writing to " + outFile);
            }
        } 
    }
    public static void main (String [] args) {
        Main pm = new Main();
        int next = Bindery.bind (args, options, pm);
        pm.handleCmdLine(args, next);
    }
}
