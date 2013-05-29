/* Server.java */
package org.xlattice.protocol.stun;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.xlattice.Transport;
import org.xlattice.protocol.Version;
import org.xlattice.util.NonBlockingLog;

/**
 * A STUN server which listens for UDP BindingRequests on four combinations
 * of primary/secondary IP address and primary/secondary port and
 * optionally listens for SharedSecretRequests on TLS (TCP) connections
 * to the primary IP address and port.
 *
 * By default the primary port is 3478.
 *
 * XXX NEED TO CLEAN UP CODE RELATING TO THIS CLASS EXTENDING Thread
 *
 * @author Jim Dixon
 */
public class Server     /* extends Thread */ {

    public final String SERVER_PROPERTIES_FILE = "stun.server.properties";

    // INSTANCE VARIABLES ///////////////////////////////////////////
    private final Properties props;

    private final Inet4Address primaryHost;
    private final Inet4Address secondaryHost;
    private final int primaryPort;
    private final int secondaryPort;
    
    private       boolean authenticating;
    private final boolean verbose;

    // only used if authenticating
    private final String  keyStoreName;
    private final String  passwd;
    private SecretServer secretServer;
    private KeyGenerator keyGen;
    private SecretKey secret1   = null;
    private SecretKey secret2   = null;

    private BindingSender[] senders;
    private BindingServer[] bindingServers;

    private volatile boolean running;
    private Object lock = new Object();

    // LOGGING //////////////////////////////////////////////////////
    protected final NonBlockingLog serverLog;
    protected void LOG_MSG(String s) {
        serverLog.message(
                new StringBuffer("Server: ").append(s).toString());
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Creates a server instance, setting parameters first from the 
     * values passed, then if there are no such values from system 
     * properties, then if there are no such system properties from
     * the SERVER_PROPERTIES_FILE file, and lastly by default.
     * 
     * If for some reason the server is not able to provide authentication,
     * it proceeds without it.
     *
     * XXX There is no way to make the authentication optional.
     *
     * @param primary        primary IPv4 address; may be null
     * @param secondary      secondary IPv4 address; may be null
     * @param p              primary port; -1 if not specified
     * @param q              secondary port; -1 if not specifieid
     * @param keyStoreName   key store file name; or null
     * @param passwd         passphrase in String format; or null
     * @param logDir         name of directory to write log to
     * @param authenticating whether the server supports shared secrets
     * @param verbose        whether to talk a lot
     *
     * @throws NumberFormatException if a port isn't numeric
     * @throws SocketException if BindingSender can't open socket
     * @throws StunException if problem resolving host name
     */
    public Server (String primary, String secondary,
                   int p, int q,
                   String keyStoreName, String passwd, String logDir,
                   boolean authenticating, boolean verbose) 
            throws NumberFormatException, SocketException, StunException {

        this.verbose        = verbose;

        if (logDir == null) {
            logDir = ".";
        } else if ( logDir.indexOf("..") != -1) {
            throw new IllegalArgumentException(
                    "log directory names may not contain ..: " + logDir);
        }
        StringBuffer sb = new StringBuffer( logDir );
        if (!logDir.endsWith(File.separator))
            sb.append(File.separator);
        sb.append("stun.server.log");
        serverLog = NonBlockingLog.getInstance(sb.toString());
        
        LOG_MSG("version " + new Version().getVersion());
        props = new Properties();
        
        // set defaults /////////////////////////////////////////////
        props.setProperty( "stun.primary.host",     "72.44.80.208");
        props.setProperty( "stun.secondary.host",   "72.44.80.209");
        props.setProperty( "stun.primary.port",     "3478");
        props.setProperty( "stun.secondary.port",   "3479");
        props.setProperty( "stun.server.keystore",  "stun.server.private");
        props.setProperty( "stun.server.password",  "87654321");
    
        
        // load SERVER_PROPERTIES_FILE (possibly overriding defaults)
        try {
            props.load(new FileInputStream(SERVER_PROPERTIES_FILE));
        } catch (IOException ioe) { 
            LOG_MSG(SERVER_PROPERTIES_FILE + " not found");
        } catch (SecurityException se) {
            LOG_MSG("can't read " + SERVER_PROPERTIES_FILE + " - " 
                    + se.toString());
        }
        
        // any environmental variables override /////////////////////
        setFromEnv ( props, "STUN_PRIMARY_HOST",    "stun.primary.host" ); 
        setFromEnv ( props, "STUN_SECONDARY_HOST",  "stun.secondary.host" ); 
        setFromEnv ( props, "STUN_PRIMARY_PORT",    "stun.primary.port" ); 
        setFromEnv ( props, "STUN_SECONDARY_PORT",  "stun.secondary.port" ); 
        setFromEnv ( props, "STUN_SERVER_KEYSTORE", "stun.server.keystore" );
        setFromEnv ( props, "STUN_SERVER_PASSWORD", "stun.server.password" );
        
        
        // any constructor arguments override ///////////////////////
        if (primary != null && primary.length() != 0)
            props.setProperty("stun.primary.host",  primary);
        if (secondary != null && secondary.length() != 0)
            props.setProperty("stun.secondary.host",  secondary);
        if (p != -1)
            props.setProperty("stun.primary.port",  Integer.toString(p));
        if (q != -1)
            props.setProperty("stun.secondary.port",  Integer.toString(q));
        if (keyStoreName != null && keyStoreName.length() != 0)
            props.setProperty("stun.server.keystore", keyStoreName);
        if (passwd != null)
            props.setProperty("stun.server.password", passwd);

        // set parameters from final Properties object //////////////
        String hostName;
        hostName    = props.getProperty("stun.primary.host");
        try { 
            primaryHost = (Inet4Address) InetAddress.getByName(hostName);
        } catch (ClassCastException cce) {
            throw new StunException(
                "host name does not resolve to an IPv4 address: " + hostName);
        } catch (UnknownHostException uhe) {
            throw new StunException("unknown host: " + hostName);
        }
        hostName    = props.getProperty("stun.secondary.host");
        try { 
            secondaryHost = (Inet4Address) InetAddress.getByName(hostName);
        } catch (ClassCastException cce) {
            throw new StunException(
                "host name does not resolve to an IPv4 address: " + hostName);
        } catch (UnknownHostException uhe) {
            throw new StunException("unknown host: " + hostName);
        }
        primaryPort   = Integer.parseInt(
                                props.getProperty("stun.primary.port"));
        secondaryPort = Integer.parseInt(
                                props.getProperty("stun.secondary.port"));
        
        this.keyStoreName   = props.getProperty("stun.server.keystore");
        this.passwd         = props.getProperty("stun.server.password");
        this.authenticating = authenticating;
        
        if (verbose) {
            LOG_MSG("init():"
                + "\n  primary IP address:   " + primaryHost
                + "\n  port:                 " + primaryPort
                + "\n  secondary IP address: " + secondaryHost
                + "\n  port:                 " + secondaryPort
                + "\n  key store name:       " + this.keyStoreName
                + "\n  passphrase:           " + this.passwd
                + "\n  authenticating:       " + authenticating
                );
        }
        if (authenticating) {
            try {
                keyGen = KeyGenerator.getInstance("HmacSHA1");
                secret1 = keyGen.generateKey();
                secret2 = keyGen.generateKey();
            } catch (GeneralSecurityException gse) {
                cantAuth(gse.toString());
            }
        }
        // start the SharedSecretServer
        if (authenticating) {
            boolean loaded = false;
            try {
                secretServer = new SecretServer (primaryHost, primaryPort, 
                    this.keyStoreName, this.passwd, secret1, secret2, 
                    logDir, verbose);
                loaded = true;
            } catch (GeneralSecurityException gse) {
                cantAuth(gse.toString());
            } catch (IOException ioe) {
                cantAuth(ioe.toString());
            }
            if (loaded) {
                boolean secretServerRunning = false;
                for (int n = 0; n < 32; n++) {
                    if (secretServer.isRunning()) {
                        secretServerRunning = true;
                    } else {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ie) { }
                    }
                }
                if (!secretServerRunning) 
                    cantAuth("secret server initialization timed out");
            }
        }
        // start the four BindingSenders
        senders = new BindingSender[4];
        senders[0] = new BindingSender (primaryHost, primaryPort, logDir);
        senders[1] = new BindingSender (primaryHost, secondaryPort, logDir);
        senders[2] = new BindingSender (secondaryHost, primaryPort, logDir);
        senders[3] = new BindingSender (secondaryHost, secondaryPort, logDir);

        for (int i = 0; i < 4; i++) {
            boolean senderRunning = false;
            for (int j = 0; j < 128; j++) {
                if (senders[i].isRunning()) {
                    senderRunning = true;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ie) { }
                }
            }
            if (!senderRunning) {
                String msg = "BindingSender " + i + " didn't start";
                if (verbose) 
                    msg += "\n  priHost " + primaryHost
                        +  "\n  secHost " + secondaryHost
                        +  "\n  priPort " + primaryPort
                        +  "\n  secPort " + secondaryPort;
                LOG_MSG(msg);
                throw new IllegalStateException(msg);
            }
        }

        // start the four BindingServers
        bindingServers = new BindingServer[4];
        if (authenticating) {
            // authentication is made optional
            bindingServers[0] = new BindingServer(senders[0].getSocket(),
                    primaryHost, primaryPort, secondaryHost, secondaryPort,
                    secret1, secret2, true, senders, logDir, verbose);
            bindingServers[1] = new BindingServer(senders[1].getSocket(),
                    primaryHost, secondaryPort, secondaryHost, secondaryPort,
                    secret1, secret2, true, senders, logDir, verbose);
            bindingServers[2] = new BindingServer(senders[2].getSocket(),
                    secondaryHost, primaryPort, secondaryHost, secondaryPort,
                    secret1, secret2, true, senders, logDir, verbose);
            bindingServers[3] = new BindingServer(senders[3].getSocket(),
                    secondaryHost, secondaryPort, secondaryHost, secondaryPort,
                    secret1, secret2, true, senders, logDir, verbose);
        } else {
            bindingServers[0] = new BindingServer(senders[0].getSocket(),
                    primaryHost, primaryPort, secondaryHost, secondaryPort,
                    null, null, true, senders, logDir, verbose);
            bindingServers[1] = new BindingServer(senders[1].getSocket(),
                    primaryHost, secondaryPort, secondaryHost, secondaryPort,
                    null, null, true, senders, logDir, verbose);
            bindingServers[2] = new BindingServer(senders[2].getSocket(),
                    secondaryHost, primaryPort, secondaryHost, secondaryPort,
                    null, null, true, senders, logDir, verbose);
            bindingServers[3] = new BindingServer(senders[3].getSocket(),
                    secondaryHost, secondaryPort, secondaryHost, secondaryPort,
                    null, null, true, senders, logDir, verbose);
        }
        for (int i = 0; i < 4; i++) {
            boolean serverRunning = false;
            for (int j = 0; j < 32; j++) {
                if (bindingServers[i].isRunning()) {
                    serverRunning = true;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ie) { }
                }
            }
            if (!serverRunning) {
                String msg = "BindingServer " + i + " didn't start";
                LOG_MSG(msg);
                throw new IllegalStateException(msg);
            }
        }
        // after all children are running:
        running = true;
    }
    /**
     * Constructor with logDir defaulting.
     */
    public Server (String primary, String secondary,
                   int p, int q,
                   String keyStoreName, String passwd, 
                   boolean authenticating, boolean verbose) 
            throws NumberFormatException, SocketException, StunException {
        this (primary, secondary, p, q, keyStoreName, passwd, null,
                authenticating, verbose);
    }
    private final void logProps (String where) {
        String pHost  = props.getProperty("stun.primary.host");
        String sHost  = props.getProperty("stun.secondary.host");
        String pPort  = props.getProperty("stun.primary.port");
        String sPort  = props.getProperty("stun.secondary.port");
        String ksName = props.getProperty("stun.server.keystore");
        String passwd = props.getProperty("stun.server.password");
        
        StringBuffer sb = new StringBuffer(where)
            .append("\nstun.primary.host    = ")  .append(pHost)
            .append("\nstun.secondary.host  = ")  .append(sHost)
            .append("\nstun.primary.port    = ")  .append(pPort)
            .append("\nstun.secondary.port  = ")  .append(sPort)
            .append("\nstun.server.keystore = ")  .append(ksName)
            .append("\nstun.server.password = ")  .append(passwd);
        
        LOG_MSG(sb.toString());
    }
    private final void cantAuth(String msg) {
        LOG_MSG("disabling authentication - " + msg);
        authenticating = false;
    }
    /**
     * If a name is set in the environment, set it in the Properties
     * object under the local name passed.
     * @param props     local Properties object
     * @param envName   name in the environment
     * @param propName  corresponding local name
     */
    private final void setFromEnv(Properties props, 
                                    String envName, String propName) {
        String s = System.getProperty(envName);
        if (s != null)
            props.setProperty(propName, s);
    }
    /**
     */
    public void close()                         throws Exception {
        synchronized (lock) {
            running = false;
            // shut down TLS server
            if (secretServer != null) 
                secretServer.close();       // blocks
            // shut down BindingServers
            for (int i = 0; i < 4; i++)
                bindingServers[i].close();
            // shut down BindingSenders
            for (int i = 0; i < 4; i++)
                senders[i].close();
        }
    }
    public synchronized boolean isRunning() {
        return running;
    }
    // COMMAND LINE INTERFACE ///////////////////////////////////////
    /**
     * Needs to be prettier.
     */
    public static void usage () {
        System.out.println(
            "usage: ./stun.server.sh [options]\n"
          + "where the options are\n"
          + "  -h       show this useful message and exit\n"
          + "  -v       chatter each time you serve a client\n"
          + "  -a addr  primary IPv4 address\n"
          + "  -b addr  secondary IPv4 address\n"
          + "  -c port  primary port, defaults to 3478\n"
          + "  -d port  secondary port, defaults to 3479 or primary + 1\n"
          + "  -k name  key store file name\n"
          + "  -l dir   write the server log to directory named\n"
          + "  -p str   passphrase\n"
          + "  -t       authenticating TLS server"
        );
        System.exit(0);
    }
    public static void usage (String msg) {
        System.out.println(msg);
        usage();
    }
//  public static Inet4Address getIPAddr(String s) {
//      Inet4Address addr = null;
//      try {
//          addr = (Inet4Address) InetAddress.getByName(s);

//      } catch (ClassCastException cce) {
//          System.out.println(
//                  "not a valid domain name or IPv4 address: " + s);
//          usage();
//      } catch (UnknownHostException uhe) {
//          System.out.println("unknown host: " + s);
//          System.exit(0);
//      }
//      return addr;
//  }
    public static boolean validPort(int n) {
        return ( 0 <= n && n < 65536);
    }
    public static int getPort (String s) {
        int port = -1;
        try {
            port = Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            System.out.println("not a valid port number: " + s);
            usage();
        }
        if (!validPort(port)) {
            System.out.println("port number is out of range: " + port);
            usage();
        }
        return port;
    }
    public static void main (String [] args) {
        // set up defaults
        String pA = null;       // primary IP address
        String sA = null;       // secondary
        int pP = -1;            // primary port
        int sP = -1;            // secondary
        String keyStoreName = null;
        String logDir       = ".";
        String passwd       = null;
        boolean authenticating = false;
        boolean verbose = false;
        // now see what the user has to say
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals ("-h"))
                usage();

            if (args[i].equals("-a")) {
                if (++i == args.length)
                    usage("missing primary host");
                else
                    pA = args[i];
            } else if (args[i].equals("-b")) {
                if (++i == args.length)
                    usage("missing secondary host");
                else
                    sA = args[i];
            } else if (args[i].equals("-c")) {
                if (++i == args.length) {
                    usage("missing primary port");
                } else {
                    pP = getPort(args[i]);
                    sP = pP + 1;            // default
                }
            } else if (args[i].equals("-d")) {
                if (++i == args.length)
                    usage("missing secondary port");
                else
                    sP = getPort(args[i]);
            } else if (args[i].equals("-k")) {
                if (++i == args.length)
                    usage("missing keystore name");
                else
                    keyStoreName = args[i];
            } else if (args[i].equals("-l")) {
                if (++i >= args.length) 
                    usage("you must name a log directory");
                logDir = args[i];
            } else if (args[i].equals("-p")) {
                if (++i == args.length)
                    usage("missing password");
                else
                    passwd = args[i];
            } else if (args[i].equals("-t")) {
                authenticating = true;
            } else if (args[i].equals ("-v")) {
                verbose = true;
            } else {
                usage("unrecognized option " + args[i]);
            }
        }
        try {
            Server server = new Server(pA, sA, pP, sP, 
                keyStoreName, passwd, logDir,
                authenticating, verbose);
        } catch (NumberFormatException nfe) {
            System.out.println("port number is invalid: " 
                    + nfe.toString());
        } catch (StunException se) {
            System.out.println("problem resolving host name: " 
                    + se.toString());
        } catch (SocketException soe) {
            System.out.println("can't open UDP socket" + soe);
        }
    }
}
