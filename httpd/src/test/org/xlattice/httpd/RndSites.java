/* RndSites.java */
package org.xlattice.httpd;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import junit.framework.*;

import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAKeyGen;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.node.NodeConfig;
import org.xlattice.node.NodeInfoMgr;
import org.xlattice.overlay.namekeyed.DiskByName;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.SchedulableConnection;
import org.xlattice.util.ArrayStack;
import org.xlattice.util.FileLib;
import org.xlattice.util.NonBlockingLog;
/**
 * 
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public abstract class RndSites extends TestCase {

    public final static int MAX_SITE_COUNT = 26;

    public final static String INFO_DIR_NAME = "test.node.info";
    private NodeInfoMgr nodeInfoMgr = new NodeInfoMgr (INFO_DIR_NAME);

    public final static String MASTER = "master.xlattice.org";
    /** for the RSA key that signs BuildLists etc */
    private NodeConfig masterConfig;
    
    public final static String DUMMY_NODE = "test.xlattice.org";
    /** for the dummy node that is being generated */
    protected NodeConfig dummyNodeConfig;
    
    /** length of "sites/" */
    public final static int SITES_LEN      = 6;
    /** key is path to file, value is SiteFile */
    protected Map pathMap;
    
    protected NonBlockingLog debugLog;    // DEBUG

    // XXX KNOWN PROBLEM IF THIS EXCEEDS CNX_BUFSIZE ////////////////
    // XXX   2005-01-30 20:30 also if it equals - sigh - XXX ////////
    protected static final int BUFSIZE = HttpParser.HTTP_BUFSIZE/2;

    // COMMON TOOLS /////////////////////////////////////////////////
    protected final Random rng = new Random ();
    protected final RSAKeyGen    keyGen;
    protected final SHA1Digest sha1;
    
    // MANUAL CONFIGURATION /////////////////////////////////////////
    // these defaults can conveniently be reset in a subclass's _setUp()

    protected String BASE_DIR_NAME = "junk.testSubClassNameHere/";
    protected int SITE_COUNT =  3;

    // have used m=84 and d=4, which also give k=4
    /** subdirectory count */
    protected int M          = 20; 
    /** max subdirectory depth */
    protected int D          =  3;
    /** file count */
    protected int N          = 64;
   
    // PROPERTIES ///////////////////////////////////////////////////
    protected RSAKey       masterKey;
    protected RSAPublicKey pubKey;
    protected File         baseDir;
    protected String       baseDirName;
    protected DiskByName   tmpDisk;

    protected ArrayStack   sites;
    protected SiteList     siteList;
  
    // CONSTRUCTORS /////////////////////////////////////////////////
    public RndSites (String name)               throws Exception {
        super(name);
        keyGen    = new RSAKeyGen();
        sha1      = new SHA1Digest();
        setDebugLog("debug.log");   // DEBUG
        DEBUG_MSG(" constructor");
    }
    // LOGGING //////////////////////////////////////////////////////
    private void checkLogName(String name) {
        if (name == null || name.equals(""))
            throw new IllegalArgumentException("null or empty log name");
    }    
    public void setDebugLog (String name) {
        checkLogName(name);
        debugLog   = NonBlockingLog.getInstance(name);
    }
    protected void DEBUG_MSG(String msg) {
        // debugLog.message("RndSites:" + msg);
        System.out.printf("RndSites DEBUG: %s\n", msg);     // DEBUG
    }
    // SETUP, TEARDOWN //////////////////////////////////////////////
    public void setUp ()                        throws Exception {
        _setUp();
        DEBUG_MSG(".setUp, after _setUp:\n    BASE_DIR_NAME = " 
                + BASE_DIR_NAME);
       
        pathMap      = new HashMap();

        masterConfig = nodeInfoMgr.get(MASTER);
        masterKey    = masterConfig.getRSAKey();
        pubKey       = (RSAPublicKey)masterKey.getPublicKey();
      
        dummyNodeConfig = nodeInfoMgr.get(DUMMY_NODE);
        
        if (BASE_DIR_NAME.endsWith(File.separator))
            baseDirName = BASE_DIR_NAME;
        else
            baseDirName = new StringBuffer(BASE_DIR_NAME)
                            .append(File.separator)
                            .toString();
        baseDir     = new File(baseDirName);
        // DEBUG
        System.out.printf("RndSites.setup: baseDir is %s\n", baseDir);
        // END
        if (baseDir.exists()) {
            FileLib.recursingDelete(baseDirName);
        }
        if (!baseDir.mkdir())
            fail("couldn't create " + baseDirName);
        tmpDisk = DiskByName.getInstance(baseDirName);  // used by RndSite

        sites = new ArrayStack(SITE_COUNT);
        siteList = new SiteList (pubKey, baseDirName);
        for (int i = 0; i < SITE_COUNT; i++) {
            String siteName = new StringBuffer ("www.site")
                                .append((char)('A' + i))
                                .append(".com")
                                .toString();
            sites.push (new RndSite (this, siteName, M, D, N));
            siteList.add(siteName);
        }
        siteList.sign(masterKey);
        assertTrue (siteList.verify());
        // DEBUG
        //System.out.println("SiteList:\n" + siteList.toString());
        // END

    }
    public void tearDown()                      throws Exception { 
        _tearDown();
        // recursingDelete(baseDirName);
    }
    public abstract void _setUp()               throws Exception;
    public abstract void _tearDown()            throws Exception;

}
