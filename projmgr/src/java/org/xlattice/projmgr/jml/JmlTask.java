/* JmlTask.java */
package org.xlattice.projmgr.jml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Jim Dixon
 */

import antlr.TokenStreamException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;

import org.xlattice.corexml.om.Document;

/**
 * Simple Ant task for Jml.
 *
 * XXX Only supports xdoc output.
 * XXX 'Mockery' option doesn't make much sense as-is; it's just
 *     a verbose switch
 * XXX NEED TO COMPARE TIMESTAMPS ON SOURCE AND TARGET, to prevent
 *     pointless regeneration of files
 */
public class JmlTask extends MatchingTask {

    private File srcDir;
    private File destDir;
    private boolean mockery;

    public JmlTask() {
    }

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the base directory that we write to */
    public File getDestDir() {
        return destDir;
    }
    public void setDestDir(File dir) {
        destDir = dir;
    }
    /** @return whether we print out test messages */
    public boolean getMockery() {
        return mockery;
    }
    public void setMockery (boolean b) {
        mockery = b;
    }
    /** @return where we look for JML files */
    public File getSrcDir() {
        return srcDir;
    }
    public void setSrcDir(File dir) {
        srcDir = dir;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /**
     * Task initialization, will be called once.
     */
    public void init()                  throws BuildException {
        // nothing to do - so far
    }
   
    /**
     * Do whatever is to be done; may be called more than once.
     */
    public void execute()               throws BuildException {
        // getBaseDir() returns projmgr :-(
        if (srcDir == null) 
            srcDir = project.resolveFile("jml");
        if (destDir == null)
            destDir = project.resolveFile("xdocs");
        if (!destDir.exists())
            if (!destDir.mkdirs())
                throw new BuildException ("can't create destdir: "
                        + destDir);
        if (mockery) {
            StringBuffer sb = new StringBuffer();
            sb.append("srcDir:  ").append(srcDir).append("\n")
              .append("destDir: ").append(destDir).append("\n")
              .append("fileset: ").append(fileset.toString())
            ;
            System.out.println(sb.toString());
        }
        DirectoryScanner ds = getDirectoryScanner(srcDir);
        String [] srcFiles = ds.getIncludedFiles();
        String srcDirName = srcDir.getName();
        String destDirName = destDir.getName();
        for (int i = 0; i < srcFiles.length; i++) {
            String s = srcFiles[i];
            if (s.endsWith(".jml")) {
                int len = s.length();
                StringBuffer sbSrc = new StringBuffer(srcDirName)
                    .append(File.separator).append(s);
                StringBuffer sbDest = new StringBuffer(destDirName)
                    .append(File.separator)
                    .append(s.substring(0, len - 3)).append("xml");
                generate (sbSrc.toString(), sbDest.toString());
            }
        }
    }
    /**
     * Generate a single XDOCS file from a JML file.
     * @param src  path to input file
     * @param dest path to target
     */
    private void generate (String src, String dest) throws BuildException {
        File destDir = new File (new File(dest).getParent());
        if (!destDir.exists()) 
            if (!destDir.mkdirs())
                throw new BuildException("can't create directory: "
                        + destDir);
        Document doc = new Document();
        boolean successful = false;
        try {
            new JmlLexer (new JmlContext(), new FileReader(src))
                             .interpret(new XdocTarget(doc));
            successful = true;
        } catch (FileNotFoundException ioe) {
            System.out.println("can't open " + src);
        } catch (IOException ioe) {
            System.out.println("error reading " + src);
        } catch (TokenStreamException tse) {
            System.out.println("error parsing " + src);
        }
        if (successful) {
            try {
                BufferedWriter out 
                    = new BufferedWriter(new FileWriter(dest));
                out.write(doc.toXml());
                out.flush();
            } catch (IOException ioe) {
                System.out.println("error writing to " + dest);
            }
        } 


    }
}
