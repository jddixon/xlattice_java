/* BuildElementFactory.java */
package org.xlattice.projmgr;

import org.xlattice.corexml.om.*;

/**
 * Methods which construct parts of the AntBuild, the object which
 * becomes an Ant build.xml file.
 *
 * @author Jim Dixon
 */
public class BuildElementFactory {

    public static final int MIN_WIDTH =  32;    // completely arbitrary
    public static final int MAX_WIDTH = 120;

    // UTILITIES ////////////////////////////////////////////////////
    public static final Element makeDeleteDir(String name) {
        return new Element("delete").addAttr("dir", name);
    }
    public static final Element makeMkdir(String dir) {
        if (dir == null || dir.equals(""))
            throw new IllegalArgumentException("null or empty dir");
        return new Element("mkdir").addAttr("dir", dir);
    }
    public static final Element makePathElement(String type, String el) {
        return new Element("pathelement").addAttr(type, el);
    }
    public static final Element makePathElementLoc(String el) {
        return makePathElement ("location", el);
    }
    public static final Element makePathElementPath (String el ) {
        return makePathElement ("path", el);
    }
    public static final Element makeProperty (String name, String value) {
        if (name == null || value == null)
            throw new NullPointerException("null name or value for property");
        return new Element ("property")
                    .addAttr("name",  name)
                    .addAttr("value", value);
    }
    public static final NodeList makeSectionHeader (String title, int width) {
        if (width < MIN_WIDTH)
            width = MIN_WIDTH;
        if (width > MAX_WIDTH)
            width = MAX_WIDTH;
        // allow for this many characters around the comment
        width -= 10;
        StringBuffer sb = new StringBuffer (width);
        for (int i = 0; i < width; i++)
            sb.append('=');
        Comment bar = new Comment(sb.toString());
        sb.delete(2, width);
        for (int i = 0; i < title.length(); i++)
            sb.append(" ").append(Character.toUpperCase(title.charAt(i)));
        sb.append (" ");
        for (int i = sb.length(); i < width; i++)
            sb.append("=");
        return new NodeList(3).append(bar)
                        .append(new Comment(sb.toString())).append(bar);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public static final NodeList makeSourceProperties (int width) {
        NodeList nodes = new NodeList();
        nodes.moveFrom(makeSectionHeader("source properties", width))
            .append(makeProperty("src_dir",        "src/java"))
            .append(makeProperty("test_src_dir",   "src/test"))
            // sample_src_dir omitted
            ;
        return nodes;
    }
    public static final NodeList makeDocProperties (int width) {
        NodeList nodes = new NodeList();
        nodes.moveFrom(makeSectionHeader("doc properties", width))
            .append(makeProperty("xdoc_dir",       "xdocs"))
            .append(makeProperty("jml_dir",        "jml"))
            // where the Javadocs get copied to
            .append(makeProperty("root_api_dir",
                                        "../xdocs/components/${name}/api"))
            // where 
            .append(makeProperty("root_jml_dir",
                                        "../jml/components/${name}"));
        return nodes;
    }
    public static final NodeList makeBuildProperties (int width) {
        NodeList nodes = new NodeList();
        nodes.moveFrom(makeSectionHeader("build properties", width))
            .append(makeProperty("lib_dir",        "../lib"))
            .append(makeProperty("build_dir",      "target"))
            .append(makeProperty("classes_dir",    "${build_dir}/classes"))
            .append(makeProperty("test_classes_dir",
                                            "${build_dir}/test_classes"))
            // skipping sample_classes_dir
            // skipping sample_jar_name
            .append(makeProperty("test_report_dir",
                                            "${build_dir}/test-reports"))
            .append(makeProperty("javadoc_dir",     "${build_dir}/docs/api"));
        return nodes;
    }
    public static final NodeList makeDistProperties (int width) {
        NodeList nodes = new NodeList();
        nodes.moveFrom(makeSectionHeader("distribution properties", width))
            .append(makeProperty("zip_dir",        "distribution"))
            
            .append(makeProperty("dist_base",      "dist-files`"))
            .append(makeProperty("dist_dir",       
                                            "${dist_base}/${projectName}"))
            .append(makeProperty("dist_lib",       "${dist_dir}/lib"))
            .append(makeProperty("dist_name",      "${name}-${version}"));
        return nodes;
    }

    // INIT SECTION /////////////////////////////////////////////////
    public static final NodeList makeInitSection (int width) {
        NodeList nodes = new NodeList()
                        .moveFrom (makeSectionHeader("init", width));
        Element targ = new Element("target")
                        .addAttr("name",        "init")
                        .addAttr("description", 
                                    "o create necessary directories");
        
        NodeList subels = targ.getNodeList();
        subels.append(makeMkdir("${lib_dir}"))
              .append(makeMkdir("${src_dir}"))
              .append(makeMkdir("${test_src_dir}"))
              .append(makeMkdir("${jml_dir}"))
              .append(makeMkdir("${xdoc_dir}"));

        return nodes.append(targ);
    }
    // DEPENDENCIES SECTION /////////////////////////////////////////
    /**
     * Build an Ant <code>available</code> element for a dependency.
     * The names passed are assumed to be well-formed.
     *
     * @param libdir The path in the file system, typically ${lib_dir}/id
     * @param id     Name of the file of interest, without any extension
     * @param artifact The file extension, typically <code>jar</code>
     * @return The Ant element.
     */
    public static final Element makeAvailableForDep (String libdir,
                                String groupId,
                                String artifactId,
                                String version,
                                String type) {
        if (libdir == null || groupId    == null
                           || artifactId == null || type == null)
            throw new NullPointerException (
                    "null library path, id, or extension");
        StringBuffer fName = new StringBuffer(libdir).append("/")
                            .append(groupId).append("/").append(artifactId);
        if (version != null)
            fName.append("-").append(version);
        fName.append(".").append(type);
        StringBuffer prop = new StringBuffer(groupId).append("-")
                                        .append(artifactId).append("-present");
        return new Element("available")
                    .addAttr("file",     fName.toString())
                    .addAttr("property", prop.toString());
    }
    /**
     * Build an Ant <code>get</code> target for any missing dependencies.
     * The names passed are assumed to be well-formed.
     */
    public static final Element makeDepGetterTarget (String libdir,
                  String groupId, String artifactId, String version,
                  String type, String url) {
        if (libdir == null || groupId == null || artifactId == null
                              || type == null || url == null)
            throw new NullPointerException ("null path, id, or extension");
        String trimmedUrl = url.trim();
        StringBuffer fName = new StringBuffer(libdir).append("/")
            .append(groupId).append("/").append(artifactId);
        if (version != null)
            fName.append("-").append(version);
        fName.append(".").append(type);
        StringBuffer targName = new StringBuffer("get-")
                        .append(groupId).append("-").append(artifactId);
        StringBuffer prop = new StringBuffer(groupId).append("-")
                                .append(artifactId).append("-present");
        // XXX NOTICE ASSUMPTION THAT groupId DOES NOT APPEAR IN 
        // XXX   GET PATH *AND* THAT IF URL IS NOT TERMINATED WITH A SLASH
        // XXX   IT SHOULD BE FOLLOWED BY A SPACE
        StringBuffer fullURL = new StringBuffer(trimmedUrl);
        if (!trimmedUrl.endsWith("/"))
            fullURL.append(" ");
        fullURL.append(artifactId);
        if (version != null)
            fullURL.append("-").append(version);
        fullURL.append(".").append(type);
        Element targ = new Element ("target")
                    .addAttr("name",    targName.toString())
                    .addAttr("unless",  prop.toString())
                    .addAttr("depends", "init");
        Element action = new Element ("get")
                    .addAttr("dest",    fName.toString())
                    .addAttr("usetimestamp", "true")        // harmless
                    .addAttr("ignoreerrors", "true")
                    .addAttr("src",     fullURL.toString());
        targ.getNodeList().append(action);
        return targ;
    }
    public static final Element makeGetDepsTarget(Dependency[] deps) {
        if (deps.length == 0)
            throw new IllegalArgumentException("no dependencies in list");
        StringBuffer sb = new StringBuffer();
        sb.append("get-").append(deps[0].getGroupId())
          .append("-").append(deps[0].getArtifactId());
        for (int i = 1; i < deps.length; i++)
            sb.append(",get-").append(deps[i].getGroupId())
              .append("-").append(deps[i].getArtifactId());
        return new Element ("target")
                    .addAttr("name",    "get-deps")
                    .addAttr("depends", sb.toString());
    }
    public static final NodeList makeGetDepsSection (
                            NodeList initTargetNodes,
                            Dependency[] deps, String libdir, int width) {
        if (deps.length == 0)
            throw new IllegalArgumentException("no dependencies in list");
        NodeList nodes = new NodeList();
        nodes.moveFrom (makeSectionHeader("dependencies", width));
        for (int i = 0; i < deps.length; i++) {
            Dependency dep  = deps[i];
            String groupId  = dep.getGroupId();
            String artifact = dep.getArtifactId();
            String version  = dep.getVersion();
            String type     = dep.getType();
            String url      = dep.getUrl();
            initTargetNodes.append (
                    makeAvailableForDep (libdir, groupId, artifact,
                                                            version, type));
            nodes.append (makeDepGetterTarget (libdir, groupId, artifact,
                                                    version, type, url));
        }
        nodes.append (makeGetDepsTarget(deps));
        return nodes;
    }
    private static final Element makeLibInclusions (Dependency[] deps) {
        Element fileset   = new Element("fileset")
                        .addAttr("dir",         "${lib_dir}");
        for (int i = 0; i < deps.length; i++) {
            String groupId = deps[i].getGroupId();
            if ( groupId.equals("ant") )
                continue;
            StringBuffer sb = new StringBuffer(groupId).append("/")
                .append(deps[i].getArtifactId());
            String version = deps[i].getVersion();
            if (version != null)
                sb.append("-").append(version);
            sb.append(".").append(deps[i].getType());
            fileset.getNodeList()
                .append(new Element("include").addAttr("name", sb.toString()));
        } 
        return fileset;
    }
    public static final NodeList makeCompileTarget (
                            Dependency[] deps, String libdir, int width) {

        NodeList nodes = new NodeList()
            .moveFrom (makeSectionHeader("compile source files", width));
        Element targ = new Element("target")
                        .addAttr("name",        "compile")
                        .addAttr("description", "o Compile the code")
                        .addAttr("depends",     "get-deps");
        NodeList targNodes = targ.getNodeList()
            .append(makeMkdir("${classes_dir}"));
        Element javac = new Element("javac")
                        .addAttr("destdir",     "${classes_dir}")
                        .addAttr("deprecation", "true")
                        .addAttr("debug",       "true")
                        .addAttr("optimize",    "false")
                        .addAttr("excludes",    "**/package.html");
        Element src = new Element("src");
        src.getNodeList().append(makePathElementLoc("${src_dir}"));

        Element classpath = new Element("classpath");
        
//      Element fileset   = new Element("fileset")
//                      .addAttr("dir",         "${lib_dir}");
//      for (int i = 0; i < deps.length; i++) {
//          String groupId = deps[i].getGroupId();
//          if ( groupId.equals("ant") )
//              continue;
//          StringBuffer sb = new StringBuffer(groupId).append("/")
//              .append(deps[i].getArtifactId());
//          String version = deps[i].getVersion();
//          if (version != null)
//              sb.append("-").append(version);
//          sb.append(".").append(deps[i].getType());
//          fileset.getNodeList()
//              .append(new Element("include").addAttr("name", sb.toString()));
//      } // GEEP
        classpath.getNodeList().append(makeLibInclusions(deps));
        javac.getNodeList().append(src).append(classpath);
        targNodes.append(javac);
        return nodes.append(targ);
    }
    public static final NodeList makeCompileTestsTarget (
                            Dependency[] deps, String libdir, int width) {

        NodeList nodes = new NodeList()
            .moveFrom (makeSectionHeader("compile tests", width));
        Element targ = new Element("target")
                        .addAttr("name",        "compile-tests")
                        .addAttr("description", "o Compile unit tests")
                        .addAttr("depends",     "compile");
        NodeList targNodes = targ.getNodeList()
            .append(makeMkdir("${test_classes_dir}"));
        Element javac = new Element("javac")
                        .addAttr("destdir",     "${test_classes_dir}")
                        .addAttr("deprecation", "true")
                        .addAttr("debug",       "true")
                        .addAttr("optimize",    "false")
                        .addAttr("excludes",    "**/package.html");
        Element src = new Element("src");
        src.getNodeList().append(makePathElementLoc("${test_src_dir}"));

        Element classpath = new Element("classpath");
//      Element fileset   = new Element("fileset")
//                      .addAttr("dir",         "${lib_dir}");
//      fileset.getNodeList().append(new Element("include")
//                      .addAttr("name",        "**/.jar"));
        classpath.getNodeList()
            .append(makeLibInclusions(deps))
            .append(makePathElementPath("${classes_dir}"));
        javac.getNodeList().append(src).append(classpath);
        targNodes.append(javac);
        return nodes.append(targ);
    }
    public static final NodeList makeCompileSamplesTarget (
                            Dependency[] deps, String libdir, int width) {
        NodeList nodes = new NodeList()
            .moveFrom (makeSectionHeader("compile sample classes", width));
        Element targ = new Element("target")
                        .addAttr("name",        "compile-samples")
                        .addAttr("description",
                            "o Compile sample classes, build samples jar")
                        .addAttr("depends",     "compile-tests");
        NodeList targNodes = targ.getNodeList()
            .append(makeMkdir("${sample_classes_dir}"));

        Element javac = new Element("javac")
                        .addAttr("destdir",     "${sample_classes_dir}")
                        .addAttr("deprecation", "true")
                        .addAttr("debug",       "true")
                        .addAttr("optimize",    "false")
                        .addAttr("excludes",    "**/package.html");

        Element src = new Element("src");
        src.getNodeList().append(makePathElementLoc("${sample_src_dir}"));

        Element classpath = new Element("classpath");
//      Element fileset   = new Element("fileset")
//                      .addAttr("dir",         "${lib_dir}");
//      fileset.getNodeList().append(new Element("include")
//                      .addAttr("name",        "**/.jar"));
        classpath.getNodeList()
            .append(makeLibInclusions(deps))
            .append(makePathElementPath("${clases.dir}"));
        javac.getNodeList().append(src).append(classpath);
        targNodes.append(javac)
            .append(new Element("jar")
                        .addAttr("jarfile",
                                "${build_dir}/${sample_jar_name}.jar")
                        .addAttr("excludes",    "**/package.html")
                        .addAttr("basedir",     "${sample_classes_dir}"));
        return nodes.append(targ);

    }
    public static final NodeList makeTestsTarget ( 
            int width, Dependency[] deps, boolean haveSampleData ) { 
        NodeList nodes = new NodeList() 
                    .moveFrom (makeSectionHeader("unit tests", width));
        Element targ = new Element("target")
                        .addAttr("name",        "test")
                        .addAttr("description", "o Run the unit tests");
        if (haveSampleData)
                    targ.addAttr("depends",     "compile-samples");
        else
                    targ.addAttr("depends",     "compile-tests");

        NodeList targNodes = targ.getNodeList()
            .append(makeMkdir("${test_report_dir}"));

        Element junit = new Element("junit")
                        .addAttr("dir",         "${basedir}")
                        .addAttr("failureproperty",
                                                "test.failure")
                        .addAttr("fork",        "true")
                        .addAttr("haltonerror", "false")
                        .addAttr("printSummary","yes");
       NodeList junitNodes = junit.getNodeList()
           .append(new Element("sysproperty")
                        .addAttr("key",         "basedir")
                        .addAttr("value",       "${basedir}"))
           .append(new Element("formatter")
                        .addAttr("type",        "xml"))
           .append(new Element("formatter")
                        .addAttr("usefile",     "false")
                        .addAttr("type",        "plain"));
        Element classpath = new Element("classpath");
        
//      Element fileset   = new Element("fileset")
//                      .addAttr("dir",         "${lib_dir}");
//      fileset.getNodeList().append(new Element("include")
//                      .addAttr("name",        "**/*.jar"));
        
        classpath.getNodeList().append(makeLibInclusions(deps))
            .append(makePathElementPath("${test_classes_dir}"))
       //   .append(makePathElementPath("${sample_classes_dir}"))
            .append(makePathElementPath("${classes_dir}"));
        junitNodes.append(classpath);

        Element batchtest = new Element("batchtest")
                        .addAttr("todir",       "${test_report_dir}");
        Element fileset2 = new Element("fileset")
                        .addAttr("dir",         "${test_src_dir}");
        fileset2.getNodeList().append( new Element("include")
                        .addAttr("name",        "**/Test*.java"));
        batchtest.getNodeList().append(fileset2);
        junitNodes.append(batchtest);
        targNodes.append(junit);
        return nodes.append(targ);
    }
    public static final NodeList makeJarTarget (int width) {
        NodeList nodes = new NodeList()
            .moveFrom (makeSectionHeader("create jar", width));
        Element targ = new Element("target")
                        .addAttr("name",        "jar")
                        .addAttr("description", "o Create the jar")
                        .addAttr("depends",     "test");
        targ.getNodeList().append(new Element ("jar")
                        .addAttr("jarfile",
                                    "${build_dir}/${dist_name}.jar")
                        .addAttr("excludes",    "**/package.html")
                        .addAttr("basedir",     "${classes_dir}"));
        return nodes.append(targ);
    }
    public static final NodeList makeJavadocTarget (int width,
                                    String name, String orgName, String pkg) {
        NodeList nodes = new NodeList()
            .moveFrom (makeSectionHeader("generate javadoc", width));
        Element targ = new Element("target")
                        .addAttr("name",        "javadoc")
                        .addAttr("description", "o Generate javadoc")
                        .addAttr("depends",     "jar");

        NodeList targNodes = targ.getNodeList()
            .append(makeMkdir("${javadoc_dir}"));
        Element timestamp = new Element("tstamp");
        timestamp.getNodeList()
            .append(new Element("format")
                        .addAttr("pattern",     "2003-yyyy")
                        .addAttr("property",    "year"));
        targNodes.append(timestamp)
            .append(makeProperty("copyright",
                "Copyright &amp;copy; " + orgName + ". All Rights Reserved."))
            .append(makeProperty("title",       name + " lastest API"));
        Element javadoc = new Element("javadoc")
                        .addAttr("use",         "true")
                        .addAttr("private",     "false")
                        .addAttr("destdir",     "${javadoc_dir}")
                        .addAttr("author",      "true")
                        .addAttr("version",     "true")
                        .addAttr("sourcepath",  "${src_dir}")
                        .addAttr("packagenames", pkg + ".*");
        Element classpath = new Element("classpath");
        Element fileset   = new Element("fileset")
                        .addAttr("dir",         "${lib_dir}");
        fileset.getNodeList().append(new Element("include")
                        .addAttr("name",        "**/*.jar"));
        classpath.getNodeList().append(fileset)
            .append(makePathElementLoc("${build_dir}/${dist_name}.jar"));
        javadoc.getNodeList().append(classpath);
        targNodes.append(javadoc);
        return nodes.append(targ);
    }
    public static final NodeList makeClean (int width) {
        NodeList nodes = new NodeList()
            .moveFrom (makeSectionHeader("clean", width));
        Element targ = new Element("target")
                        .addAttr("name",        "clean")
                        .addAttr("description",
                                "o Clean up the generated directories");

        NodeList targNodes = targ.getNodeList()
            .append(makeDeleteDir("${build_dir}"))
            .append(makeDeleteDir("${dist_dir}"))
            .append(makeDeleteDir("${src_dist_dir}"))
            .append(new Comment("note in original:"))
            .append(new Comment("does NOT remove distribution directory"));
        return nodes.append(targ);
    }
}
