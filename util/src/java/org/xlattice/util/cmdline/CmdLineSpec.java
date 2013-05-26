/* CmdLineSpec.java */
package org.xlattice.util.cmdline;

/**
 * Specification for the binding between command line options and
 * a set of fields in an object.  The CmdLineSpec consists of a 
 * list of bindings between the various command line options and 
 * specific fields in the object.  The options may appear in any
 * order on the command line.  Bindings are effected using 
 * reflection.
 *
 * @author Jim Dixon
 */
public class CmdLineSpec {

    /** options passed to the Java runtime */
    private final String javaArgs;
    /** command line options passed to Java class */
    private final CmdLineOpt [] descriptors;
    /** description of other command line arguments */
    private final String otherArgs;

    /** 
     * Create a command line specification.  This consists of a
     * set of arguments passed to the Java interpreter, a list
     * of command line option descriptors, and a description of
     * any other values appearing on the command line after the
     * options.  Options are preceded by a minus sign ('-') and 
     * followed by a value.  
     * 
     * @param javaOpt arguments passed to java
     * @param opts    array of command line option descriptors
     * @param others  description of any other arguments
     */
    public CmdLineSpec (String javaOpt, CmdLineOpt [] opts, String others) {
        if (javaOpt == null) {
            javaArgs = "";
        } else {
            javaArgs = javaOpt;      // may be empty
        }
        if (opts == null) {
            descriptors = new CmdLineOpt[0];
        } else {
            descriptors = opts;
        }
        if (others == null) {
            otherArgs = "";
        } else { 
            otherArgs = others.trim();
        }
    }
    /** 
     * Default constructor.
     * @param opts array of command line option descriptors
     */
    public CmdLineSpec (CmdLineOpt[] opts) {
        this (null, opts, null);
    }
    // ACCESS METHODS ///////////////////////////////////////////////
    public CmdLineOpt findOption(String oName) {
        if (oName == null || oName.equals("")) {
            throw new IllegalArgumentException("search on null or empty name");
        }
        for (int i = 0; i < descriptors.length; i++) {
            if (oName.equals(descriptors[i].getOptionName())) {
                return descriptors[i];
            }
        }
        throw new CmdLineException("no such option");
    }
    public String getJavaArgs() {
        return javaArgs;
    }
    public CmdLineOpt[] getOptionDescriptors() {
        return descriptors;
    }
    public String getOtherArgDesc() {
        return otherArgs;
    }
}
