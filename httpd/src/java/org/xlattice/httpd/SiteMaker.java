/* SiteMaker.java */
package org.xlattice.httpd;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.xlattice.CryptoException;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.bind.Mapping;
import org.xlattice.crypto.builds.BuildList;
import org.xlattice.crypto.builds.BuildMaker;
import org.xlattice.corexml.om.Document;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.httpd.sitemaker.*;
import org.xlattice.node.Configurer;
import org.xlattice.node.NodeConfig;
import org.xlattice.node.NodeInfoMgr;
import org.xlattice.util.FileLib;
import org.xlattice.util.cmdline.Bindery;
import org.xlattice.util.cmdline.BooleanOpt;
import org.xlattice.util.cmdline.CmdLineOpt;

/**
 * @author Jim Dixon
 */

public class SiteMaker {

    // VARIABLES SET FROM THE COMMAND LINE //////////////////////////
    private boolean verbose;
    
    /** 
     * Command line specifications, description of the command line
     * arguments.  This is used to convert command line arguments into
     * options by reflection.  
     */
    static CmdLineOpt[] options = {
                       // option  field
                       //  name    name     min      max
            new BooleanOpt ("v", "verbose"                )
    };
    // CONSTRUCTOR //////////////////////////////////////////////////
    protected SiteMaker () {}

    // STATIC METHODS ///////////////////////////////////////////////
    /**
     * Constucts a standard HTTPd node directory.  The standard name
     * for the directory is "xlattice/".  Contents are
     *   xlattice.xml        -- the node configuration file
     *   overlays/           -- overlay directory
     *     httpd/            -- server configuration information and logs
     *       sites.cfg       -- a signed list of Web sites served
     *       builds/         -- directory holding BuildLists
     *         SITE_NAME.cfg --   BuildList, one for each site
     *       log/            -- log files listed below and possibly others
     *         access.log
     *         error.log
     *     memcache/         -- configuration data for MemCache
     *     name2hash/        -- configuration data for Name2Hash
     *     store/            -- contains files named by content or title key
     *
     * XXX 05-02-17: NEED TO CHECK THAT THIS CONFORMS TO REALITY XXX
     *
     * Given a populated sites directory (conventional name "sites/"),
     * generate a node directory (conventionally "xlattice/") suitable
     * suitable for use with other classes in this package.  The name of
     * each subdirectory of sitesDir is a fully qualified domain name.
     * 
     *   XXX We do not currently check this.
     *   
     * This set of names is used to create a SiteList.  The files 
     * and subdirectories contained in the site directories are used
     * to create BuildLists for each site.  The SiteList and BuildList
     * are signed using the masterKey.
     * 
     * Directory names are paths from the default directory.
     *
     * The site is generated from a set of site configuration objects.
     * These are normally created by SiteConfigurer from an XML file,
     * sitemaker.xml, which describes the site.
     *
     * @param sc SiteConfig object 
     * @throws IllegalArgumentException if params fail checks
     */
    public static void generate (SiteConfig sc) 
                throws CoreXmlException, CryptoException, IOException {

        // SiteConfig VALIDATION ////////////////////////////////////
        checkNotNull((Object)sc,  "SiteConfig");
        
        // NODE INFORMATION ///////////////////////////////
        NodeInfo ni = sc.getNodeInfo();
        checkNotNull((Object)ni,    "NodeInfo");
        String nodeInfoDirName   = ni.getDir();
        checkNotNull(nodeInfoDirName,"nodeInfoDirName");
        StringBuffer sb = new StringBuffer(nodeInfoDirName);
        if (!nodeInfoDirName.endsWith(File.separator))
            sb.append(File.separator);
        sb.append("node.info");
        NodeInfoMgr infoMgr = new NodeInfoMgr(sb.toString());
        
        String masterKeyName = ni.getMaster();
        checkNotNull(masterKeyName, "masterKeyName");
        RSAKey masterKey     = infoMgr.get(masterKeyName).getRSAKey();
        RSAPublicKey pubKey;        // master public key
        pubKey = (RSAPublicKey)masterKey.getPublicKey();
        
        String nodeKeyName   = ni.getNodeKey();
        checkNotNull(nodeKeyName,   "nodeKeyName");
        NodeConfig nc        = infoMgr.get(nodeKeyName);

        // SITES //////////////////////////////////////////
        int siteCount = sc.sizeSite();
        if (siteCount <= 0)
            throw new IllegalArgumentException ("zero sites");
        SiteInfo[] sites = new SiteInfo[siteCount];
        File[] siteDirs  = new File[siteCount];
        for (int i = 0; i < siteCount; i++) {
            sites[i] = sc.getSite(i);
            checkNotNull(sites[i].getName(), 
                            "name for site " + i);
            
            checkNotNull(sites[i].getDir(),  
                            "source directory for site " + i);
            String srcDirName = sites[i].getDir();
            if (!srcDirName.endsWith(File.separator)) {
                srcDirName = new StringBuffer(srcDirName)
                                .append(File.separator).toString();
                sites[i].setDir(srcDirName);
            }
            siteDirs[i] = new File(srcDirName);
            if (!siteDirs[i].exists())
                throw new IllegalArgumentException(
                        "site source directory does not exist: " 
                        + srcDirName);
            if (!siteDirs[i].isDirectory())
                throw new IllegalArgumentException(
                        "site source 'directory' is not a directory: "
                        + srcDirName);
            if (siteDirs[i].length() == 0)
                throw new IllegalArgumentException(
                        "site source directory is empty: " + srcDirName);
            int port = sites[i].getPort();
            if (port <= 0 || port > 65535)
                throw new IllegalArgumentException(
                        "port number out of range: " + port);
        }
        // OUTPUT DIRECTORY ///////////////////////////////
        OutputInfo oi = sc.getOutput();
        checkNotNull((Object) oi,   "OutputInfo");
        String outputDirName = oi.getDir();
        checkNotNull(outputDirName, "output directory name");

        // GENERATION ///////////////////////////////////////////////
        File outputDir = new File(outputDirName);
        if (outputDir.exists())
           FileLib.recursingDelete (outputDirName);
        if (!outputDir.mkdirs())
            throw new IllegalStateException("problem creating " 
                    + outputDirName);
        String nodeDirName  = FileLib.mkSubDir (outputDirName, "xlattice");
       
        // NODE CONFIGURATION FILE, xlattice.xml //////////
        Mapping ncMap = Configurer.getMap();            
        Document doc = ncMap.generate(nc);
        File xmlConfigFile = new File ( new StringBuffer(nodeDirName)
                                    .append("xlattice.xml").toString() );
        FileWriter xmlWriter = new FileWriter (xmlConfigFile);
        xmlWriter.write (doc.toXml());
        xmlWriter.flush();
        xmlWriter.close();
       
        // CREATE OVERLAY DIRECTORY ///////////////////////
        String overlayDirName  = FileLib.mkSubDir (nodeDirName, "overlays");
        String httpdDirName    = FileLib.mkSubDir (overlayDirName, "httpd");
        String storeDirName    = FileLib.mkSubDir (overlayDirName, "store");

        // CREATE AND WRITE httpd.cfg /////////////////////

        // STUB    
        
        // MAKE SIGNED LIST OF SITES //////////////////////
        // nodeKeyName is conventionally the fully qualified domain
        // name of the server itself; this may also be a/the site name
        SiteList siteList = new SiteList (pubKey, nodeKeyName); 
        for (int i = 0; i < siteCount; i++) {
            // XXX MISSING PORT NUMBER         <--------------------------
            siteList.add(sites[i].getName());
        }
        siteList.sign(masterKey);
        if(!siteList.verify())
            throw new IllegalStateException("site list does not verify");
        
        // MAKE BUILD LISTS ///////////////////////////////
        // and copy source files into store ///////////////
        BuildList[] buildLists = new BuildList[siteCount];
        for (int i = 0; i < siteCount; i++) {
            // DEBUG
            System.out.printf("SITE_MAKER: directory is %s\n" +
                              "            site is      %s\n", 
                                sites[i].getDir(), sites[i].getName());
            // END
            BuildMaker maker = new BuildMaker(masterKey, pubKey,
                                sites[i].getName(),     // title
                                sites[i].getDir(),      // source
                                BuildMaker.FLAT,        // strategy
                                storeDirName);          // target
            buildLists[i] = maker.makeBuildList();
            if(!buildLists[i].verify())
                throw new IllegalStateException(
                                    "build list does not verify");
        }
        // WRITE SITE LIST ////////////////////////////////
        File sListFile = new File ( new StringBuffer(httpdDirName)
                                    .append("sites.cfg").toString() );
        FileWriter slWriter = new FileWriter (sListFile);
        slWriter.write (siteList.toString());
        slWriter.flush();
        slWriter.close();

        // WRITE BUILD LISTS //////////////////////////////
        String buildDir = FileLib.mkSubDir (httpdDirName, "builds");
        for (int i = 0; i < buildLists.length; i++) {
            BuildList list = buildLists[i];
            File listFile = new File ( new StringBuffer(buildDir)
                                        .append(sites[i].getName())
                                        .append(".cfg")
                                        .toString() );
            FileWriter writer = new FileWriter(listFile);
            writer.write(list.toString());
            writer.flush();
            writer.close();
        } 
       
    }
    /**
     * XXX THIS SHOULD BE httpDirOK or similar and should call 
     * XXX org.xlattice.node.X.nodeDirOK.  Generally, knowledge of
     * how xlattice/ is structured should be in org.xlattice.node,
     * and knowledge of how overlays/httpd/ is structured should be
     * here.
     */
    public boolean nodeDirOK (String nodeDirName)
                                throws CryptoException, IOException {

        // STUB
        return false;
    }
    // UTILITY METHODS //////////////////////////////////////////////
    protected static void checkNotNull(Object o, String name) {
        if (o == null) 
            throw new IllegalArgumentException("null " + name);
    }
    protected static void checkNotNull(String s, String name) {
        if (s == null || s.equals(""))
            throw new IllegalArgumentException("null or empty " + name);
    }
    // MAIN /////////////////////////////////////////////////////////
    /**
     * Main.  The only command line argument supported at this 
     * point is -v, setting the verbose option.
     * 
     * Expects to find "sitemaker.xml" in the current directory.
     * Uses this to arrange things appropriately and then invoke 
     * SiteMaker.generate().
     *
     */
    public static void main (String [] args)    throws Exception {
        SiteMaker instance;
        instance = new SiteMaker();
        Mapping map = SiteConfigurer.getMap();

        // read command line, set options
        int next = Bindery.bind (args, options, instance);
       
        // read the configuration file
        boolean configOK = true;
        File cfgFile = new File("sitemaker.xml");
        if (!cfgFile.exists())
            throw new IllegalStateException("missing configuration file");
        
        SiteConfig sc = null;
        try {
            sc = SiteConfigurer.configureSite (new FileReader(cfgFile));
        } catch (Exception ex) {
            throw new Exception("problem reading configuration file: " 
                    + ex);
        } 
        generate(sc);
    } 
    
}
