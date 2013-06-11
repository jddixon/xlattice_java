/* Server.java */
package org.xlattice.httpd.cryptoserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;

import org.xlattice.Address;
import org.xlattice.CryptoException;
import org.xlattice.EndPoint;
import org.xlattice.NodeID;
import org.xlattice.Transport;
import org.xlattice.corexml.CoreXmlException;
import org.xlattice.corexml.bind.Mapping;
import org.xlattice.corexml.om.Document;
import org.xlattice.corexml.om.XmlParser;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.overlay.datakeyed.DiskCache;
import org.xlattice.httpd.HttpSListener;
import org.xlattice.httpd.HttpSListenerFactory;
import org.xlattice.httpd.Name2Hash;
import org.xlattice.httpd.SiteList;
import org.xlattice.node.Configurer;
import org.xlattice.node.NodeConfig;
import org.xlattice.overlay.datakeyed.MemCache;
import org.xlattice.transport.ClientServer;
import org.xlattice.transport.IOScheduler;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.SchedulableAcceptor;
import org.xlattice.transport.tcp.Tcp;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.Timestamp;

/**
 * First cut at an actual CryptoServer.
 *
 * @author Jim Dixon
 */

public class Server implements Runnable {

    // COMMAND LINE CONFIGURATION ///////////////////////////////////
    private static String serverVersion;
    
    // PRIVATE MEMBERS //////////////////////////////////////////////
    protected final NonBlockingLog debugLog;

    private RSAPublicKey masterPubKey;
    
    public final static String BASE_DIR_NAME = "./";
    protected SchedulableAcceptor   acc;

    protected IOScheduler scheduler;

    private   Name2Hash name2Hash = Name2Hash.getInstance();
    private   MemCache  memCache  = MemCache.getInstance();
    private   String    nodeDirName;
    private   String    sitesDirName;

    public Server () {
        debugLog = NonBlockingLog.getInstance("debug.log");
    }
    // LOGGING //////////////////////////////////////////////////////
    protected void DEBUG_MSG(String msg) {
        debugLog.message("Server" + msg);
    }
    // INTERFACE Runnable ///////////////////////////////////////////
    /**
     * Start the server running.  Exceptions encountered are all
     * turned into IllegalStateExceptions.
     */
    public void run () {
        HttpSListener.setServerName(serverVersion);
        
        // DIRECTORY STRUCTURE ////////////////////////////
        String nodeDirName    = "xlattice" + File.separator;
        String overlayDirName = nodeDirName + "overlays" + File.separator;
        String httpdDirName   = overlayDirName + "httpd" + File.separator;
        String buildDirName   = httpdDirName + "builds" + File.separator;
        String logDirName     = httpdDirName + "log" + File.separator;
        checkDir(logDirName);
       
        // NODE ///////////////////////////////////////////
        File nodeCfg        = checkFile(nodeDirName + "xlattice.xml");
        FileReader ncReader = null;
        NodeConfig nc       = null;
        try {
            ncReader = new FileReader(nodeCfg);
            Mapping ncMap = Configurer.buildMapping();
            Document doc  =  new XmlParser (ncReader).read();
            nc = (NodeConfig)ncMap.apply(doc);
        } catch (FileNotFoundException fnfe) {
            throw new IllegalStateException ("cannot find xlattice.xml");
        } catch (IOException ioe) {
            throw new IllegalStateException (
                    "problem reading xlattice.xml: " + ioe);
        } catch (CoreXmlException cxe ) {
            throw new IllegalStateException (
                    "problem parsing xlattice.xml: " + cxe);
        }
        NodeID nodeID = nc.getNodeID();

        // HTTPD.CFG //////////////////////////////////////
        // STUB: skipping for now

        // DISK CACHE INITIALIZATION //////////////////////
        DiskCache diskCache = DiskCache.getInstance();
        diskCache.init();
        
        // LOAD Name2Hash, GET SITE LIST //////////////////
        SiteList siteList = name2Hash.loadFromNodeDir(nodeDirName);
        if (siteList == null)
            throw new IllegalStateException("null site list");
        masterPubKey = siteList.getPublicKey();     // XXX KLUDGE
        int siteCount = siteList.size();

        // BANNER /////////////////////////////////////////
        StringBuffer sb = new StringBuffer("\n")
                    .append(serverVersion)
                    .append(" running ");
        for (int i = 0; i < siteCount; i++) {
            if (i > 0) 
                sb.append(", ");
            sb.append(siteList.toString(i));
        }
        sb.append(" at ").append(new Timestamp())
          .append("\n    items in MemCache:      ")
          .append(memCache.itemCount())
          .append("\n    bytes:                  ")
          .append(memCache.byteCount())
          .append("\n    items in Name2Hash map: ")
          .append(name2Hash.size());
        String banner = sb.toString();        
        System.out.println(banner); 
        DEBUG_MSG(banner);

        // START SCHEDULER ////////////////////////////////
        DEBUG_MSG(" starting scheduler");
        try {
            scheduler = new IOScheduler();
        } catch (IOException ioe) {
            throw new IllegalStateException(
                                "can't create IOScheduler: " + ioe);
        }
        boolean schedulerRunning = false;
        while (!schedulerRunning) {
            try {
                Thread.currentThread().sleep(10);   // ms
            } catch (InterruptedException ie) { /* ignore */ }
            schedulerRunning = scheduler.isRunning();
        }

        // START SERVERS //////////////////////////////////
        Class       protocol;
        int         serverPort = 80;                // default
        // protocol   = Tcp.class; 2011-08-23 fix
        Transport transport = new Tcp();
        for (int i = 0; i < siteCount; i++) {
            String domainName = siteList.toString(i);// XXX CHANGING INTERFACE
            InetAddress hostIP;
            try {
                hostIP = InetAddress.getByName(domainName);
            } catch (UnknownHostException uhe) {
                throw new IllegalStateException(domainName 
                        + " is unknown host: " + uhe);
            }
            Address  serverAddr = new IPAddress (hostIP, serverPort);
            // another 2011-08-23 fix
            // EndPoint serverEnd  = new EndPoint (Tcp.class, serverAddr);
            EndPoint serverEnd  = new EndPoint (transport, serverAddr);
            
            DEBUG_MSG(" setting up acceptor for " + domainName);
            try {
                acc = (SchedulableAcceptor)
                        // ((Transport)protocol.newInstance())
                        ((ClientServer)transport)        // 2011-08-23
                            .getAcceptor(serverAddr, false);
            /*
            } catch (InstantiationException ie) {
                throw new IllegalStateException(
                        "can't instantiate transport protocol");
            } catch (IllegalAccessException ie) {
                throw new IllegalStateException(
                        "can't access transport protocol instance");
            */
            } catch (IOException ioe) {
                throw new IllegalStateException("can't get acceptor");
            }
            // XXX 2011-08-23 NEEDS TO BE A FACTORY XXX
            acc.setCnxListenerFactory(new HttpSListenerFactory());
            acc.setReceiver(scheduler);
            scheduler.add(acc);
    
            ServerSocketChannel srvChan = (ServerSocketChannel)acc.getChannel();
            while (!srvChan.isRegistered()) {
                try {
                    Thread.currentThread().sleep(2);
                } catch (InterruptedException ie) { /* ignore ;-) */ }
            }
        } 
    }

    public void close()                     throws IOException {
        acc.close();
        try { scheduler.close(); } catch (Exception e) { /* ignore */ }
        Thread schedThread = scheduler.getThread();
        if (schedThread.isAlive()) {
            try {
                schedThread.join();
            } catch (InterruptedException ie) { 
                /* ignore for now */ 
            }
        }
    }
    /**
     * Verify that a directory exists and is in fact a directory.
     *
     * @param name path to the directory from the current directory
     * @return a reference to the File
     */
    private static File checkDir(String name) {
        File dir = new File(name);
        if (!dir.exists())
            throw new IllegalStateException("directory does not exist: "
                    + name);
        if (!dir.isDirectory())
            throw new IllegalStateException("is not a directory: "
                    + name);
        return dir;
    }
    /**
     * Verify that a (configuration) file exists and is not a directory.
     *
     * @param name path to the file from the current directory
     * @return a reference to the File
     */
    private static File checkFile(String name) {
        File file = new File(name);
        if (!file.exists())
            throw new IllegalStateException("file does not exist: "
                    + file);
        if (file.isDirectory())
            throw new IllegalStateException("file is a directory: "
                    + name);
        return file;
    }

    /**
     */
    public static void main (String[] args) {
        // 2011-08-23 fix
        Version version = new Version();
        
        StringBuffer sb = new StringBuffer("CryptoServer/")
                            .append(version.getMajor()).append(".")
                            .append(version.getMinor());
        int decimal = version.getDecimal();
        int build   = version.getBuild();
        if (decimal > 0)
            sb.append(".").append(decimal);
        if (build > 0)
            sb.append(" build ").append(build);
        serverVersion = sb.toString();

        // OLD STUFF ///////////////////////////
//      
//      for (int i = 0; i < args.length; i++) {
//          if (args[i].equals("-n")) {
//              try {
//                  InetAddress newHostIP = InetAddress.getByName(args[++i]);
//                  hostIP     = newHostIP;
//                  domainName = hostIP.getHostAddress();
//              } catch (UnknownHostException uhe) {
//                  /* we haven't assigned the name */
//              }
//          } else if (args[i].equals("-p")) {
//              try {
//                  int newPort = Integer.parseInt(args[++i]);
//                  port        = newPort;
//              } catch (NumberFormatException nfe) {
//                  /* we haven't assigned the port number */
//              }
//          }    
//      }

        Server instance = new Server();
        Thread server = new Thread (instance);
        server.start();
    }
}
