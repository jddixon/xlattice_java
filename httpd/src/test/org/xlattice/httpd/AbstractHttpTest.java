/* AbstractHttpTest.java */
package org.xlattice.httpd;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.Date;

//import java.util.HashMap;
//import java.util.Map;
import java.util.Random;

import junit.framework.*;

import org.xlattice.Address;
import org.xlattice.EndPoint;
import org.xlattice.Transport;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.crypto.builds.BuildList;
import org.xlattice.httpd.headers.*;
import org.xlattice.httpd.sitemaker.*;
import org.xlattice.overlay.datakeyed.DiskCache;
import org.xlattice.overlay.datakeyed.MemCache;
import org.xlattice.transport.ClientServer;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.IOScheduler;
import org.xlattice.transport.SchedulableAcceptor;
import org.xlattice.transport.SchedulableConnector;
import org.xlattice.transport.SchedulableConnection;
import org.xlattice.util.StringLib;

/**
 * @author Jim Dixon
 */

public abstract class AbstractHttpTest extends RndSites {

    protected Class       protocol;
    
    protected SchedulableConnector [][] ctr;
    protected SchedulableConnection[][] cnx;
   
    /** one per site */
    protected SchedulableAcceptor[]     acc;
    protected EndPoint[]                serverEnd;
    protected Address[]                 serverAddr;

    protected IOScheduler clientScheduler;
    protected IOScheduler serverScheduler;

    private String  nodeDirName;
    private String  sitesDirName;

    private final String siteConfigHeader =
          "<siteconfig>\n"
        + "  <nodeinfo dir=\"" 
                    + System.getProperty("user.home") + File.separator 
        +                    "\" master=\"master.xlattice.org\" \n"
        + "            nodekey=\"ns1.xlattice.org\"/>\n"
        + "  <sites>\n";
   
    private final String path2NodeDir = "generated" + File.separator;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public AbstractHttpTest (String name)        throws Exception{
        super(name);
    }

    protected void DEBUG_MSG(String msg) {
        // 2013-07-14
        // debugLog.message("AbstractHttpTest" + msg);
        System.out.println("AbstractHttpTest" + msg);
    }
    // SETUP, TEARDOWN //////////////////////////////////////////////
    /**
     * Create an XML site configuration for the test run.
     *
     * This must be called after _setUp(); otherwise BASE_DIR_NAME
     * will not have been defined.
     */
    private String makeSiteConfig (int siteCount) {
        System.out.printf("makeSiteCount(%d)\n", siteCount);    // DEBUG
        if (siteCount < 1 || siteCount > MAX_SITE_COUNT) 
            throw new IllegalArgumentException("impossible site count: " 
                    + siteCount);
        StringBuffer sb = new StringBuffer (siteConfigHeader);
        for (int i = 0; i < siteCount; i++) {
            char letter = (char)('A' + i);
            sb.append("    <site name=\"www.site") 
              .append(letter)
              .append(".com\" port=\"80\"\n          dir=\"")
              .append(BASE_DIR_NAME)
              .append("sites/www.site")
              .append(letter)
              .append(".com/\"/>\n");
        }
        sb.append( "  </sites>\n  <output dir=\"")
          .append(path2NodeDir)
          .append("\"/>\n</siteconfig>\n");
        return sb.toString();
    } 
    public abstract void implSetUp()            throws Exception;
    public void _setUp ()                       throws Exception {

        // configuration for RndSites /////////////////////
        BASE_DIR_NAME = "junk.testHttpOverTcp/";
        SITE_COUNT    =  2;     // v0.9 HttpCListener not tested XXX
        
        // THE FOLLOWING ARE FOR EACH SITE, so for example if
        // SITE_COUNT is 3, N = 64 gives you 192 files.
        // Have also used m=84 and d=4, which also give k=4
        M          = 20;        // subdirectory count
        D          =  3;        // max subdirectory depth
        N          =  8;        // file count
        
//      M          =  2;        // subdirectory count
//      D          =  2;        // max subdirectory depth
//      N          =  3;        // file count
        // end RndSites config ////////////////////////////

        nodeDirName   = new StringBuffer (path2NodeDir)
                                .append("xlattice").append(File.separator)
                                .toString();
        sitesDirName  = new StringBuffer (BASE_DIR_NAME)
                                .append("sites")
                                .append(File.separator)
                                .toString();

        // these should be assigned in implSetUp()
        acc         = null;
        serverEnd   = null;
        serverAddr  = null;

        try {
            clientScheduler = new IOScheduler();
            serverScheduler = new IOScheduler();
        } catch (IOException ioe) {
            System.err.println("can't create IOScheduler: " + ioe);
        }
        try {
            do { 
                // allow a bit of time for things to start running
                Thread.currentThread().sleep(2);           // ms
            } while (  !clientScheduler.isRunning() 
                    || !serverScheduler.isRunning());
        } catch (InterruptedException ie) { /* ignore */ }
        implSetUp();
    }
    public abstract void implTearDown()         throws Exception;
    public void _tearDown ()                    throws Exception {

    }

    // UNIT TESTS ///////////////////////////////////////////////////
    // Any and all unit tests should be at this level.  Use
    // impl{SetUp,TearDown}() to do any protocol-specific stuff.
    public void testNBServer ()                 throws Exception {
        DEBUG_MSG(".testNBServer");
        Name2Hash name2Hash = Name2Hash.getInstance(path2NodeDir);
        assertNotNull(name2Hash);
        assertEquals(path2NodeDir, name2Hash.getPathToXLattice());
        MemCache  memCache  = MemCache.getInstance();
        assertEquals(path2NodeDir, memCache.getPathToXLattice());
        DiskCache diskCache = DiskCache.getInstance();
        assertEquals(path2NodeDir, diskCache.getPathToXLattice());
        diskCache.init();

        SHA1Digest sha1 = new SHA1Digest();
        HttpRequest[][]  requests  = new HttpRequest [SITE_COUNT][];
        // XXX NOT USED
        HttpResponse[][] responses = new HttpResponse[SITE_COUNT][];
        String siteDesc = makeSiteConfig(SITE_COUNT);

        // DEBUG * ERROR * directory name includes site name
        System.out.printf("siteDesc: '%s'\n", siteDesc);
        // END
        
        SiteConfig siteConfig = SiteConfigurer.configureSite(
                                    new StringReader(siteDesc));
        // create the node directory (xlattice/) //////////
        SiteMaker.generate (siteConfig);            // XXX FAILS
        name2Hash.loadFromNodeDir(nodeDirName);
      
        // XXX CHECK TO MAKE SURE SiteList and BuildList match
        SiteList nodeSiteList    = name2Hash.getSiteList();
        if (!nodeSiteList.verify())
            fail("site list does not verify");
        assertEquals (SITE_COUNT, nodeSiteList.size());
        BuildList[] buildLists = name2Hash.getBuildLists();
        assertEquals (SITE_COUNT, buildLists.length);
        for (int i = 0; i < SITE_COUNT; i++) {
            BuildList buildList = buildLists[i];
            if (!buildList.verify())
                fail("build list does not verify");
            assertEquals(nodeSiteList.toString(i), buildList.getTitle());
        } 

        // get a server running for each site
        acc = new SchedulableAcceptor[SITE_COUNT];
        for (int i = 0; i < SITE_COUNT; i++) {
            DEBUG_MSG("opening Acceptor for site " + i);

            // //////////////////////////////////////////////////////
            // XXX PROBLEM with the util-0.3.8 package, which drops
            // getAcceptor() from the Transport interface
            // Solved by replacing (Transport) with (ClientServer)
            // //////////////////////////////////////////////////////
            acc[i] = (SchedulableAcceptor) (
                       ((ClientServer)protocol.newInstance()) 
                            .getAcceptor(serverAddr[i], false) 
                     );
            HttpSListener.setServerName("HttpOverTcpServer");
           
            // XXX The connection listener is added by the acceptor
            // acc[i].setConnListener(new HttpSListener());       // XXX ???

            acc[i].setReceiver(serverScheduler);
            serverScheduler.add(acc[i]);
            ServerSocketChannel srvChan 
                        = (ServerSocketChannel)acc[i].getChannel();
            while (!srvChan.isRegistered()) 
                Thread.currentThread().sleep(2);
        }
        
        // SET UP A CONNECTOR FOR EACH PAGE ///////////////
        ctr = new SchedulableConnector[SITE_COUNT][N];
        HttpCListener[][] listener = new HttpCListener[SITE_COUNT][];
        Header dateHeader = new DateHeader();
        for (int i = 0; i < SITE_COUNT; i++) {
            RndSite site = (RndSite)sites.peek(i);
                Header hostHeader = new HostHeader(site.name);

                int httpVersion = HttpParser.V1_1;
                if (i == 1)
                    httpVersion = HttpParser.V1_0;
                else if (i == 2)
                    httpVersion = HttpParser.V0_9;
                BuildList buildList = buildLists[i];
                String title = buildList.getTitle();

                listener[i] = new HttpCListener[N];
                requests[i] = new HttpRequest[N];
                for (int j = 0; j < N; j++) {
                    // XXX SAME FIX: Transport -> ClientServer
                    ctr[i][j] = (SchedulableConnector)
                            ((ClientServer)protocol.newInstance())
                                .getConnector(serverAddr[i], false);
                    
                    String fileName = new StringBuffer(File.separator)
                                        .append(buildList.getPath(j))
                                        .toString();
                    String nameAsKey = new StringBuffer(title)
                                        .append(fileName)
                                        .toString();
                    // XXX MODIFY TO MAKE SAY 1/3 REQUESTS HTTP_HEAD XXX
                    HttpRequest request = 
                        requests[i][j]  = 
                            new HttpRequest(HttpParser.HTTP_GET, 
                                        fileName,
                                        httpVersion);   // CHANGE 02-26
                    if (httpVersion > HttpParser.V0_9) {
                        request.addHeader(dateHeader);
                        request.addHeader(hostHeader);
                    }
                    int fileLen = ((SiteFile)pathMap.get(nameAsKey))
                                                        .data.length;
                    listener [i][j] = new HttpCListener( 
                            requests[i][j],
                            fileLen,
                            i * N + j);             // instance index XXX
                assertNull   (listener[i][j].getConnection());

                // XXX The connection listener is added by the acceptor
                // ctr[i][j].setConnListener(listener[i][j]);

                ctr[i][j].setReceiver(clientScheduler);
                assertNull (ctr[i][j].getKey());
            }
        }
        // SCHEDULE ALL CONNECTORS ////////////////////////
        for (int i = 0; i < SITE_COUNT; i++) {
            BuildList buildList = buildLists[i];
            for (int j = 0; j < N; j++) {
                clientScheduler.add(ctr[i][j]); 
            }
        }
        // WAIT UNTIL ALL CONNNECTIONS ESTABLISHED ////////
        cnx = new SchedulableConnection[SITE_COUNT][];
        boolean waiting = true;
        while (waiting) {
            Thread.currentThread().sleep(100);
            boolean oneNotDone = false;;
            for (int i = 0; i < SITE_COUNT && !oneNotDone; i++) {
                cnx[i] = new SchedulableConnection[N];
                for (int j = 0; j < N && !oneNotDone; j++) 
                    if (cnx[i][j] == null) {
                        cnx[i][j] = listener[i][j].getConnection();
                        if (cnx[i][j] == null) {
                            DEBUG_MSG(".testNBServer: cnx[" + i + "]["
                                    + j + "] is still null");
                            oneNotDone = true;
                            break;
                        }
                    }
            }
            waiting = oneNotDone;
        }
        // WAIT UNTIL ALL RESULTS ARE IN //////////////////
        DEBUG_MSG(".testNBServer: waiting for results");
        waiting = true;
        while (waiting) {
            Thread.currentThread().sleep(500);      // XXX LONG DELAY
            boolean oneNotDone = false;;
            for (int i = 0; i < SITE_COUNT && !oneNotDone; i++) {
                BuildList buildList = buildLists[i];
                for (int j = 0; j < N && !oneNotDone; j++) {
                    if (!cnx[i][j].isClosed()) {
                        // DEBUG
                        DEBUG_MSG(".testNBServer: cnx[" + i + "]["
                            + j + "] still open; content length = "
                            + listener[i][j].getContentLength()
                            );
                        // END
                        oneNotDone = true;
                        break;
                    }
                }
            }
            waiting = oneNotDone;
        }
       
        // CHECK VERSION, STATUS CODE /////////////////////
        DEBUG_MSG(".testNBServer: check version, status code");
        for (int i = 0; i < SITE_COUNT; i++) {
            String expectedStatusLine = "HTTP/1.1 200 OK\n";
            if (i == 1)
                expectedStatusLine = "HTTP/1.0 200 OK\n";
            if (i != 2)
                for (int j = 0; j < N; j++) {
                    HttpResponse response = listener[i][j].getResponse();
                    String statusLine = response.getStatusLine();
                    assertEquals(expectedStatusLine, statusLine);
                }
        }
            
        // VERIFY RESULTS /////////////////////////////////
        DEBUG_MSG(".testNBServer: verifying data returned");
        for (int i = 0; i < SITE_COUNT; i++) {
            BuildList buildList = buildLists[i];
            for (int j = 0; j < N; j++) {
                HttpResponse response = listener[i][j].getResponse();
                byte[] results = response.getEntity();
                // XXX THIS IS WHAT HAPPENS AS OF 2005-09-20 
                //     -- well, sometimes :-)
                assertNotNull("response [" + i + "][" + j 
                        + "] has null entity",results);
                assertEquals(listener[i][j].getContentLength(), results.length);
                byte[] actualHash   = sha1.digest(results);
                byte[] expectedHash = buildList.getHash(j);
                assertEquals(expectedHash.length, actualHash.length);
                for (int k = 0; k < expectedHash.length; k++) {
                    assertEquals(expectedHash[k], actualHash[k]);
//                  // DEBUG
//                  if (expectedHash[k] != actualHash[k]) {
//                      System.out.println("  hashes don't match:"
//                          + "\n    actual data:   "
//                              + HttpParser.firstTen(results)
//                          + "\n    actual hash:   " 
//                              + HttpParser.firstTen(actualHash)
//                          + "\n    expected hash: "
//                              + HttpParser.firstTen(expectedHash) 
//                              
//                              );
//                      break;
//                  }
//                  // END
                } 
            }
        } 

        // shouldn't this be in _tearDown() ?
        for (int i = 0; i < SITE_COUNT; i++)
            acc[i].close();
        try { serverScheduler.close(); } catch (Exception e) { /* ignore */ }
        Thread schedThread = serverScheduler.getThread();
        if (schedThread.isAlive())
            schedThread.join();
        assertFalse(schedThread.isAlive());

        try { clientScheduler.close(); } catch (Exception e) { /* ignore */ }
        schedThread = clientScheduler.getThread();
        if (schedThread.isAlive())
            schedThread.join();
        assertFalse(schedThread.isAlive());
    }
}
