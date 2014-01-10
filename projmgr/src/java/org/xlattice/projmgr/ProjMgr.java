/* ProjMgr.java */
package org.xlattice.projmgr;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.FileWriter;

import antlr.TokenStreamException;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.om.Document;
import org.xlattice.util.cmdline.*;
import org.xlattice.util.JnlpVersion;

/**
 * @author Jim Dixon
 */

public class ProjMgr {

    public ProjMgr() { }

    // COMMAND LINE INTERFACE AND main() ////////////////////////////
    static CmdLineOpt[] options = {
        new BooleanOpt ("a", "writeAntBuild",
                                            "write build.xml"),
        new BooleanOpt ("c", "writeCmdFiles",   
                                            "make command files"),
        new BooleanOpt ("h", "showHelp",    "show help msg"),
        new BooleanOpt ("v", "verbose",     "show version number and halt"),
    };
    
    // set defaults 
    boolean writeAntBuild;  // false
    boolean writeCmdFiles;  // false
    boolean showHelp;       // false
    boolean verbose;        // false

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
        // don't want the "org.xlattice." bit
        String pgmVersion = new Version().getVersion().substring(13);
        if (verbose) {
            StringBuffer sb = new StringBuffer()
                // XXX BUG IN VERSION
                .append(pgmVersion)
                .append("\nhelp requested?        ").append(showHelp)
                .append("\nverbose?               ").append(verbose)
                .append("\nwriting build.xml?     ").append(writeAntBuild)
                .append("\nwriting command files? ").append(writeCmdFiles);
            System.out.println(sb);
            System.out.flush();
            System.exit(0);
        }
        if (showHelp) {
            System.out.println("sorry, no help is available");
            System.out.flush();
            System.exit(0);
        }
        Project project = null;
        try {
            project = new ProjectReader(
                            new FileReader("project.xml")).read();
        } catch (FileNotFoundException e) {
            // XXX GRACELESS IF PARENT PROJECT.XML MISSING
            System.out.println("can't open project.xml: " + e);
            return;
        } catch (IOException e) {
            // XXX .. OR OTHER ERROR IN HANDLING PARENT
            System.out.println("fatal I/O error: " + e);
            return;
        } catch (CoreXmlException e) {
            System.out.println(e);
            return;
        }
        if (writeAntBuild) {
            try {
                AntBuild build =  new StandardAntBuild(project);
                FileWriter buildWriter = new FileWriter("build.xml");
                buildWriter.write(build.getDoc().toXml());
                buildWriter.flush();
                buildWriter.close();
            } catch (IOException e) {
                System.out.println("fatal I/O error");
            } 
        }
        if (writeCmdFiles) {
            try {
                CmdFiles cmdGen = new CmdFiles(project);
                cmdGen.writeClasspathCmd(
                        new FileWriter("classpath.sh"), false);
                cmdGen.writeBuildCmd(
                        new FileWriter("build.sh"), false);
            } catch (IOException e) {
                System.out.println("fatal I/O error");
            } 
        } 
    }
    public static void main (String [] args) {
        ProjMgr pm = new ProjMgr();
        int next = Bindery.bind (args, options, pm);
        pm.handleCmdLine(args, next);
    } 
    
    /////////////////////////////////////////////////////////////////
    // UTILITY METHODS //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    private static final boolean isLetter (char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }
    /**
     * IDs, groupIDs, and artifact names must begin with a letter or
     * underscore, be at least one character long, and otherwise 
     * consist of letters, digits, and underscores.
     */
    public static final boolean isWellFormedId (String s) {
        if (s == null || s.equals(""))
            return false;
        char c = s.charAt(0);
        if ( c != '_' && ! isLetter(c))
            return false;
        for ( int i = 1; i < s.length(); i++) { 
            c = s.charAt(i);
            if ( c != '_' && !isLetter(c) && !Character.isDigit(c))
                return false;
        }
        return true;
    }
    /** 
     * @return whether the string represents a valid JNLP version number
     */
    public static final boolean isWellFormedVersion (String v) {
        try { 
            JnlpVersion jv = new JnlpVersion(v);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
