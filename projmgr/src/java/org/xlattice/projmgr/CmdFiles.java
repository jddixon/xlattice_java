/* CmdFiles.java */
package org.xlattice.projmgr;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Generates classpath.{bat,sh} and build.{bat,sh} for project components.
 * The first sets up the Java classpath, the second runs Ant.
 *
 * XXX The Windows code has not been implemented.
 *
 * @author Jim Dixon
 */
public class CmdFiles {

    private Project project;
    private String  libDir;
    private Dependency antDep;
    private Dependency optionalDep;
    private Dependency junitDep;
    private ArrayList   otherDeps = new ArrayList();
   
    public CmdFiles (Project p) {
        if (p == null || p.equals(""))
            throw new IllegalArgumentException ("null or empty project");
        project = p;
        libDir  = project.getLibDir();
        if (libDir == null) 
            throw new NullPointerException("libDir not specified in Project");
        Dependency [] deps = p.getDependencies();
        for (int i = 0; i < deps.length; i++) {
            Dependency dep = deps[i];
            if (dep.getGroupId().equals("ant") 
                    && dep.getArtifactId().equals("ant")) {
                antDep = dep;
            } else if (dep.getGroupId().equals("ant") 
                    && dep.getArtifactId().equals("optional")) {
                optionalDep = dep;
            } else if (dep.getGroupId().equals("junit") 
                    && dep.getArtifactId().equals("junit")) {
                junitDep = dep;
            } 
            else
                otherDeps.add(dep);
        }
        if (antDep == null || optionalDep == null || junitDep == null)
            throw new IllegalStateException("essential dependency missing");
    }
    private void checkArgs(Writer writer, boolean forWindows) {
        if(writer == null)
            throw new IllegalArgumentException("writer is null");
        if(forWindows)
            throw new IllegalStateException("Windows not yet supported");
    }
    /**
     * XXX This will cause problems if jar is oddly named or lacks
     * version number.
     */
    private String makePath (Dependency dep) {
        StringBuffer sb = new StringBuffer(libDir)
            .append("/").append(dep.getGroupId())
            .append("/").append(dep.getArtifactId());
        String version = dep.getVersion();
        if (version != null)
            sb.append("-").append(version);
        sb.append(".").append(dep.getType());
        return sb.toString();
    }
    /**
     * Write the classpath.{bat,sh} command file which sets up the
     * classpath for Java.
     *
     * XXX The result will not generally be executable.  Needs a 
     *     chmod u+x or equivalent.
     */
    public void writeClasspathCmd (Writer writer, boolean forWindows) 
                                                    throws IOException {
        checkArgs(writer, forWindows);
        StringBuffer sb = new StringBuffer() 
            .append("#!/bin/sh\n\n")
            .append("# Usage:\n")
            .append("#    . classpath.sh [build|run] [set] [quiet]\n\n")
            .append("# This script sets CLASSPATH and optionally echoes\n")
            .append("# LCP (LOCALCLASSPATH) to the caller.\n")
            .append("#\n")
            .append("# The current directory and JAVA_HOME/lib/tools are")
                .append(" always on the local path.\n")
            .append("LCP=.:$JAVA_HOME/lib/tools.jar:$LCP\n")
            .append("# ... as is everything else but ant and JUnit\n");
        for (int i = 0; i < otherDeps.size(); i++)
            sb.append("LCP=")
                .append(makePath((Dependency)otherDeps.get(i)))
                .append(":$LCP\n");
        
        sb.append("# If the first argument is 'build', JUnit and Ant are added to the local\n")
            .append("# path, which will become CLASSPATH if the second argument is 'set'.\n")
            .append("# unless there is a 'quiet' argument, the local class path is echoed.\n")
            .append("if [ \"$1\" = \"build\" ] ; then \n")
            .append("    #LCP=../lib/junit/junit-3.8.1.jar:$LCP\n")
            .append("    LCP=").append(makePath(junitDep)).append(":$LCP\n")
            .append("    #LCP=`echo ../lib/ant/*.jar | tr ' ' ':'`:$LCP\n")
            .append("    #LCP=../lib/ant/ant.jar:../lib/ant/optional.jar:$LCP\n")
            .append("    LCP=").append(makePath(antDep)).append(":$LCP\n")
            .append("    LCP=").append(makePath(optionalDep)).append(":$LCP\n")
            .append("    if [ \"$2\" = \"set\" ] ; then\n")
            .append("        CLASSPATH=$LCP\n")
            .append("        if [ ! \"$3\" = \"quiet\" ] ; then\n")
            .append("            echo $LCP\n")
            .append("        fi\n")
            .append("    elif [ ! \"$2\" = \"quiet\" ] ; then\n")
            .append("        echo $LCP\n")
            .append("    fi\n")
            .append("else \n")
            .append("    LCP=target/classes:target/test-classes:$LCP\n")
            .append("    if [ \"$1\" = \"run\" ] ; then\n")
            .append("        if [ \"$2\" = \"set\" ] ; then\n")
            .append("            CLASSPATH=$LCP\n")
            .append("            if [ ! \"$3\" = \"quiet\" ] ; then\n")
            .append("                echo $LCP\n")
            .append("            fi\n")
            .append("        elif [ ! \"$2\" = \"quiet\" ] ; then\n")
            .append("            echo $LCP\n")
            .append("        fi\n")
            .append("    else \n")
            .append("        CLASSPATH=$LCP\n")
            .append("        if [ ! \"$1\" = \"quiet\" ] ; then\n")
            .append("            echo $CLASSPATH\n")
            .append("        fi\n")
            .append("    fi\n")
            .append("fi\n")
            .append("export CLASSPATH\n");
        writer.write(sb.toString());
        writer.flush();
        writer.close();
        // CHMOD u+x ///
    }
    /**
     * Write the build.{bat,sh} command file which runs Ant after
     * calling classpath.{bat,sh} to set up the classpath.
     *
     * XXX The result will not generally be executable.  Needs a 
     *     chmod u+x or equivalent.
     */
    public void writeBuildCmd(Writer writer, boolean forWindows) 
                                                    throws IOException {
        checkArgs(writer, forWindows);
        StringBuffer sb = new StringBuffer()    
            .append("#!/bin/sh\n\n")
            .append("if [ -z \"$JAVA_HOME\" ] ; then\n")
            .append("  JAVA=`/usr/bin/which java`\n")
            .append("  if [ -z \"$JAVA\" ] ; then\n")
            .append("    echo \"Cannot find JAVA. Please set your PATH.\"\n")
            .append("    exit 1\n")
            .append("  fi\n")
            .append("  JAVA_BIN=`dirname $JAVA`\n")
            .append("  JAVA_HOME=$JAVA_BIN/..\n")
            .append("else\n")
            .append("  JAVA=$JAVA_HOME/bin/java\n")
            .append("fi\n\n")
            .append("echo \"JAVA=$JAVA\"\n\n")
            .append("#echo setting LOCALCLASSPATH\n")
            .append("LOCALCLASSPATH=`/bin/sh $PWD/classpath.sh build`\n")
            .append("\n")
            .append("CMD=\"$JAVA $OPTS -classpath $LOCALCLASSPATH org.apache.tools.ant.Main $@ -buildfile build.xml\"\n")
            .append("echo $CMD\n")
            .append("$CMD\n");
        writer.write(sb.toString());
        writer.flush();
        writer.close();
        // CHMOD u+x ///
    } 
    //  for debugging 
    public Project getProject() { return project; }
    public Dependency getAntDep () { return antDep; }
    public Dependency getOptionalDep () { return optionalDep; }
    public Dependency getJunitDep () { return junitDep; }
    public ArrayList  getOtherDeps () { return otherDeps; }
}
