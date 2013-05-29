/* Client.java */
package org.xlattice.protocol.stun;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;        // XXX 
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.xlattice.Acceptor;
import org.xlattice.Connection;
import org.xlattice.Connector;
import org.xlattice.CryptoException;
import org.xlattice.EndPoint;
import org.xlattice.Protocol;
import org.xlattice.Transport;
import org.xlattice.protocol.Version;
import static org.xlattice.crypto.tls.TlsConst.*;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.tls.Tls;
import org.xlattice.transport.tls.TlsAddress;

import org.xlattice.util.StringLib;
import org.xlattice.util.NonBlockingLog;

/**
 * An RFC 3489 STUN client with a command-line interface.  References 
 * in this file to 'the RFC' mean IETF RFC 3489.
 *
 * The RFC states that clients should use server discovery,
 * learning what STUN servers are available for a domain from
 * SRV records in the DNS.  This is not yet implemented.
 *
 * Finally, the RFC requires that communications with the secret
 * server use a TLS connection.  In this interim release, they use bare
 * TCP/IP and are unencrypted.
 *
 * This implementation uses an uncomfortable mixture of XLattice
 * abstractions and plain old sockets.  This will be sorted out,
 * but in this class
 * <ul>
 *   <li><b>Inet4Address</b>es have <b>Host</b> in their names
 *   <li><b>ports</b> are called that, as for example <b>localPort</b></li>
 *   <li>an XLattice <b>IPAddress</b> has <b>Addr</b> in its name
 * </ul>
 *
 * An IPAddress in this terminology consists of an InetAddress and
 * a port number.
 *
 * @author Jim Dixon
 */
public class Client                         implements StunConst {

    // this range is wired into NattedAddress; take care if changing
    /** RFC 3489 test results */
    public final static int NOT_NATTED      = -3;
    public final static int NATTED          = -2;
    public final static int INCONCLUSIVE    = -1;
    public final static int NO_UDP          = 1;
    public final static int OPEN_INTERNET   = 2;
    public final static int SYM_FIREWALL    = 3;
    public final static int FULL_CONE       = 4;
    public final static int RESTRICTED      = 5;
    public final static int PORT_RESTRICTED = 6;
    public final static int SYMMETRIC_NAT   = 7;

    public       static Inet4Address LOOPBACK_ADDR;

    static { try {
        LOOPBACK_ADDR = getIPAddr("127.0.0.1");
    } catch (Exception e) { /* can't happen, we hope ;-) */ } }

    /** NAT descriptions indexed by type */
    public final static String[] NAT_TYPES = {
        "UNRECOGNIZED NAT TYPE 0", 
        "no UDP available",     "no NAT found, open Internet", 
        "symmetric firewall",   "full cone NAT", 
        "restricted NAT",       "port-restricted NAT",
        "symmetric NAT"};
    
    // INSTANCE VARIABLES ///////////////////////////////////////////
    private Inet4Address localHost;
    private int          localPort;
    private IPAddress    myAddr;        // XLattice abstraction
    private String       logDir;

    private final Tls        myTls;
    private final int        authLevel;
    private final TlsAddress myTlsAddr;
    private EndPoint         myTlsEnd;         // includes protocol
    private final TlsAddress serverTlsAddr;
    private       Connector  tlsCtr;

    private final boolean verbose;
    /** whether the server has a TLS/TCP interface */
    private       boolean authenticating;
    /** whether the client has been closed; of what value? */
    private       boolean closed;

    public final static String version = new Version().getVersion();

    /** server's primary address and port */
    private final Inet4Address priHost;
    private final int priPort;

    // FROM BINDING SERVERS ///////////////////////////////
    /** last test result; reset by getNatDescription() */
    private       int natType = -1;
    private       NattedAddress nattedAddr;

    /** index of BindingServer in use */
    private       int serverIndex;

    /** MAPPED-ADDRESS */
    private       Inet4Address mappedHost;
    private       int mappedPort;

    /** REFLECTED-FROM */
    private       Inet4Address reflectedHost;
    private       int reflectedFromPort;

    /** RESPONSE-ADDRESS */
    private       Inet4Address responseHost;
    private       int responsePort;

    /** CHANGE-REQUEST */
    private       int changeFlags;

    /** SOURCE-ADDRESS */
    private       Inet4Address sourceHost;
    private       int sourcePort;

    /** from CHANGED-ADDRESS: secondary address and port */
    private       Inet4Address secHost;
    private       int secPort;

    // FROM SHARED SECRET SERVER  /////////////////////////
    private byte[] userName;
    private byte[] password;
    /* calculated from the password */
    private SecretKey clientKey;

    // LOGGING ////////////////////////////////////////////
    protected final NonBlockingLog clientLog;
    protected void LOG_MSG(String s) {
        clientLog.message(s);
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * This constructor is suitable only for local testing; it wires 
     * in a 127.0.0.1 local host address.
     */
    public Client (Inet4Address pA, int pP) {
        this (pA, pP, LOOPBACK_ADDR, 0, null, false, false);
    }
    /**
     * Client instance logging to default directory and using 
     * a default local address and ephemeral port number.
     *
     * @param pA           server IPv4 address
     * @param pP           primary port
     * @param authenticate whether to
     * @param verbose      whether the client should be talkative
     */
    public Client (Inet4Address pA, int pP,
                            boolean authenticate, boolean verbose)  {
        this (pA, pP, null, 0, null, authenticate, verbose);
    }

    // TAKE CARE: InetAddress vs Inet4Addresss
    public static Inet4Address getInterfaceAddress( NetworkInterface iface) {
        Inet4Address myAddr = null;
        String name = iface.getDisplayName();
        for (Enumeration<InetAddress>addrs = iface.getInetAddresses();
                addrs.hasMoreElements();
                ) {
            InetAddress addr = addrs.nextElement();
            if (addr instanceof Inet4Address) {
                //System.out.printf("DEBUG %s has ipv4 addr %s\n", name, addr);
                myAddr = (Inet4Address) addr;
            }
        }
        // System.out.printf("DEBUG selected %s as address on %s\n", myAddr, name);
        return myAddr;
    }
    /** 
     * Constructor using a named local interface; it selects an IPv4 
     * address on that interface.
     */
    public Client (Inet4Address pA, int pP, NetworkInterface iface) {
        this(pA, pP, iface, null, false, false);
    }
    public Client (Inet4Address pA, int pP, NetworkInterface iface, 
            String logDir, boolean authenticate, boolean verbose) {

        this(pA, pP, 
                getInterfaceAddress(iface), 0, 
                logDir, authenticate, verbose);
    }
    /**
     * Client instance.
     *
     * @param pA           server IPv4 address
     * @param pP           primary port
     * @param myA          null or preferred local IPv4 address
     * @param myP          preferred local port; if 0, ephemeral
     * @param logDir       directory to write log in (default = ./)
     * @param authenticate whether to
     * @param verbose      whether the client should be talkative
     */
    public Client (Inet4Address pA, int pP, Inet4Address myA, int myP,
                    String logDir, boolean authenticate, boolean verbose)  {
        if (pA == null)
            throw new IllegalArgumentException("null IP address");
        priHost   = pA;
        if (!validPort(pP))
            throw new IllegalArgumentException("port out of range: " + pP);
        priPort    = pP;
   
        if (myA == null)
            throw new IllegalArgumentException("local address is null");
        localHost = myA;

//      if (myA == null) {
//          // CHANGE TO THROW AN EXCEPTION IF myA NOT SPECIFIED XXX
//          try {
//              // this doesn't work - need to first enumerate over 
//              // interfaces and then over InetAddresses.  This code
//              // found 127.0.0.1, 127.0.1.1, and 0:0:0:0:0:0:0:1
//              localHost = (Inet4Address)InetAddress.getLocalHost();
//              // for remote communications we want to select a real 
//              // IP address
//              InetAddress[] ipAddrs = InetAddress.getAllByName(
//                      localHost.getCanonicalHostName());
//              if (ipAddrs != null && ipAddrs.length > 1) {
//                  for (InetAddress addr : ipAddrs) {
//                      System.out.printf ("local IP address: %s\n", addr);
//                  }
//              }
//          } catch (java.net.UnknownHostException uhe) {
//              throw new IllegalStateException(
//                      "should be impossible: " + uhe);
//          }
//      } else {
//          localHost = myA;
//      } // GEEP

        if (!validPort(myP))
            throw new IllegalArgumentException(
                                    "port out of range: " + myP);
        localPort = myP;
       
        myAddr   = new IPAddress(localHost, localPort);
        
        if (logDir == null) {
            logDir = ".";
        } else if ( logDir.indexOf("..") != -1) {
            throw new IllegalArgumentException(
                    "log directory names may not contain ..: " + logDir);
        }
        StringBuffer sb = new StringBuffer( logDir );
        if (!logDir.endsWith(File.separator))
            sb.append(File.separator);
        sb.append("stun.client.log");
        clientLog = NonBlockingLog.getInstance(sb.toString());
        
        authenticating = authenticate;
        // DEBUG
        if (authenticating)
            System.out.println("AUTHENTICATING");
        // END
        this.verbose   = verbose;
        if (verbose) {
            LOG_MSG (
                    "primary IP address:   " + priHost
                + "\nport:                 " + priPort
                + "\nauthenticating:       " + authenticating
            );
        }


        authLevel = ANONYMOUS_TLS;      // XXX only level supported
        if (authenticating) {
            myTls         = new Tls();
            myTlsAddr     = new TlsAddress(myAddr,
                                    authLevel, null, null, true);
            myTlsEnd      = new EndPoint(myTls, myTlsAddr);
            IPAddress priAddr
                          = new IPAddress(priHost, priPort);
            serverTlsAddr = new TlsAddress(priAddr, authLevel,
                                           null, null, false);
            try {
                tlsCtr        = myTls.getConnector(serverTlsAddr, true);
            } catch (IOException ioe) {
                LOG_MSG("can't get connector, disabling authentication - "
                        + ioe.toString());
                authenticating = false;
                tlsCtr         = null;
            }
            if (authenticating)
                fetchUserNameAndPassword();

        } else {
            myTls         = null;
            myTlsAddr     = null;
            myTlsEnd      = null;
            serverTlsAddr = null;
            tlsCtr        = null;
        }
    }
    // ACCESS METHODS ///////////////////////////////////////////////
    public int getChangeFlags() {
        return changeFlags;
    }
    public void setChangeFlags(int val) {
        changeFlags = 6 & val;
    }

    /** @deprecated; use getNattedAddress() instead */
    public IPAddress getClientAddr() {
        return myAddr;
    }

    /** @deprecated; use getNattedAddress() instead */
    public IPAddress getMappedAddr() {
        if (mappedHost == null)
            return null;
        else
            return new IPAddress(mappedHost, mappedPort);
    }

    /**
     * @return the REFLECTED-FROM value or null if none
     */
    public IPAddress getReflectedFromAddr() {
        if (reflectedHost == null)
            return null;
        else
            return new IPAddress(reflectedHost, reflectedFromPort);
    }
    /**
     * @return the RESPONSE-ADDRESS value or null if none
     */
    public IPAddress getResponseAddr() {
        if (responseHost == null)
            return null;
        else
            return new IPAddress(responseHost, responsePort);
    }
    /**
     * Causes a RESPONSE-ADDRSS attribute to be added to
     * BindingRequests.  If <b>respAddr</b> is null, this
     * behaviour ceases.
     */
    public void setResponseAddr(IPAddress respAddr) {
        if (respAddr == null) {
            responseHost = null;
            responsePort = 0;
        } else {
            responseHost = (Inet4Address) respAddr.getHost();
            responsePort = respAddr.getPort();
        }
    }
    public IPAddress getSecondaryAddr() {
        if (secHost == null)
            return null;
        else
            return new IPAddress(secHost, secPort);
    }
    public IPAddress getSecretServerAddr() {
        return serverTlsAddr;
    }
    public IPAddress getSourceAddr() {
        if (sourceHost == null)
            return null;
        else
            return new IPAddress(sourceHost, sourcePort);
    }
    /** @return index of UDP server last used; defaults to zero */
    public int getServerIndex() {
        return serverIndex;
    }

    /** 
     * @return local port number, valid after bind() 
     * @deprecated and redundant; use getNattedAddress() instead 
     */
    public int    getPort()     { return localPort;   }

    public byte[] getUserName() { return userName; }
    public byte[] getPassword() { return password; }

    // IMPLEMENTATION METHODS ///////////////////////////////////////
    protected void logAndThrow(String msg)      throws StunException {
        LOG_MSG(msg);
        throw new StunException(msg);
    }
    /**
     * @param outPkt  packet to be sent out, the BindingRequest
     * @param socket  UDP socket used 
     * @param timeout in ms
     * @param msgID   STUN message ID
     * @param buffer  used for receiving message
     */
    protected BindingResponse doSendRecv (DatagramPacket outPkt,
            DatagramSocket socket, int timeout, 
            byte[] msgID, byte[] buffer) throws IOException, StunException {
        socket.setSoTimeout(timeout);
        BindingResponse resp;
        try {
            // ERROR XXX 11111111111111111111111111111111111111111111
            // This gets a java.io.Exception: Invalid argument
            socket.send(outPkt);
    
            // receive BindingResponse and validate ///////////
            DatagramPacket inPkt = new DatagramPacket(buffer, 256);
            socket.receive(inPkt);
    
            // XXX MUST ALSO DEAL WITH PARSE FAILURES XXX
            StunMsg parsed = StunMsg.decode(buffer);
            if (parsed.type == BINDING_ERROR_RESPONSE) {
                // XXX incorrect formats are also possible
                logAndThrow( ((ErrorCode)parsed.get(0)).toString() );
            } else if (parsed.type != BINDING_RESPONSE) {
                logAndThrow( "unexpected message type " + parsed.type );
            }
            resp = (BindingResponse) parsed;
            byte[] respID = resp.getMsgID();
            boolean idOK = true;
            int i;
            for (i = 0; i < msgID.length; i++)
                if (msgID[i] != respID[i]) {
                    idOK = false;
                    break;
                 }
            if (!idOK)
                logAndThrow ("message ID differs at " + i);
        } catch (SocketTimeoutException se) { 
            resp = null;
        }
        return resp;
    }
    /**
     * Get a binding from one of the four UDP binding servers.  If the
     * index is -1, this signifies that (a) server zero is to be used
     * and (b) the preferred local port is to be used rather than an
     * ephemeral port.
     * 
     * @param n  which binding server to use
     */
    public void bind(int n) throws CryptoException, IOException,
                                    SocketTimeoutException, StunException {
        String errMsg;
        boolean sameLocalPort = false;
        if (n < -1 || 3 < n)
            logAndThrow(
                    "INTERNAL ERROR: BindingServer index out of range: " + n);
        if (n == -1) {
            n = 0;
            sameLocalPort = true;
        }
        if (closed)
            throw new StunException("Client has been closed");
        serverIndex = n;

        Inet4Address bindingServer = null;
        int bindingPort = 0;

        switch (serverIndex) {
            case 0:
                bindingServer = priHost;
                bindingPort   = priPort;
                break;
            case 1:
                bindingServer = priHost;
                bindingPort   = secPort;
                break;
            case 2:
                bindingServer = secHost;
                bindingPort   = priPort;
                break;
            case 3:
                bindingServer = secHost;
                bindingPort   = secPort;
                break;
            default:
                logAndThrow(
            "INTERNAL ERROR: BindingServer index out of range in switch: "
                        + serverIndex);
        }
        
        DatagramSocket socket;
        if (localPort != 0) {
            socket = new DatagramSocket(localPort, localHost);
        } else {
            socket = new DatagramSocket(0, localHost);
            localPort = socket.getLocalPort();
        }
        
        socket.setReuseAddress(true);
        socket.connect(bindingServer, bindingPort);

        StunMsg msg = new BindingRequest();
        if (authenticating) {
            msg.add (new UserNameAttr(userName));
            msg.add(new MessageIntegrity());
        }

        byte[] msgID = msg.getMsgID();
        byte[] serialized = new byte[msg.wireLength()];
        msg.encode(serialized);
        if (authenticating) {
            MessageIntegrity.setHMAC(serialized, clientKey);
        }
        DatagramPacket outPkt = new DatagramPacket(serialized,
                        serialized.length, bindingServer, bindingPort);

        // RFC 3489 section 9.3 requires that messages be sent 
        // repeatedly, with timeouts set to 100ms, then 200, then 400, 
        // then 800, then 1600, then four times more with 1600 ms timeouts, 
        // until a response is received.  Responses received with the wrong 
        // MsgID should be ignored in these calculations.
 
        byte[] buffer = new byte[256];
        // XXX ERROR - Invalid argument XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        BindingResponse resp = doSendRecv(outPkt, socket, 100, msgID, buffer);
        if (resp == null)
            resp = doSendRecv(outPkt, socket, 200, msgID, buffer);
        if (resp == null)
            resp = doSendRecv(outPkt, socket, 400, msgID, buffer);
        if (resp == null)
            resp = doSendRecv(outPkt, socket, 800, msgID, buffer);
        if (resp == null)
            resp = doSendRecv(outPkt, socket, 1600, msgID, buffer);
        if (resp == null)
            resp = doSendRecv(outPkt, socket, 1600, msgID, buffer);
        if (resp == null)
            resp = doSendRecv(outPkt, socket, 1600, msgID, buffer);
        if (resp == null)
            resp = doSendRecv(outPkt, socket, 1600, msgID, buffer);
        if (resp == null)
            resp = doSendRecv(outPkt, socket, 1600, msgID, buffer);
        if (resp == null)
            throw new SocketTimeoutException("STUN 9500ms timeout exceeded");
        if (authenticating) {
            // verify message using clientKey
            int msgLen  = HEADER_LENGTH + resp.length();
            int miIndex = resp.size() - 1;
            MessageIntegrity mi =
                (MessageIntegrity)resp.get(miIndex);
            boolean ok;
            try {
                ok = mi.verify(buffer, msgLen, clientKey);
            } catch (CryptoException ce) {
                ok = false;
            }
            if (!ok)
                logAndThrow( "HMAC verification failed" );
        }
        mappedHost    = null;
        reflectedHost = null;
        sourceHost    = null;
        //secHost       = null;

        for (int k = 0; k < resp.size(); k++) {
            StunAttr attr = resp.get(k);
            switch (attr.type) {
                case MAPPED_ADDRESS:
                    mappedHost = ((AddrAttr)attr).getAddress();
                    mappedPort = ((AddrAttr)attr).getPort();
                    break;

                case REFLECTED_FROM:
                    reflectedHost = ((AddrAttr)attr).getAddress();
                    reflectedFromPort = ((AddrAttr)attr).getPort();
                    break;

                case SOURCE_ADDRESS:
                    sourceHost = ((AddrAttr)attr).getAddress();
                    sourcePort = ((AddrAttr)attr).getPort();
//                  // DEBUG
//                  System.out.printf("setting source address to %s:%d\n",
//                          sourceHost, sourcePort);
//                  // END
                    break;

                case CHANGED_ADDRESS:
                    secHost = ((AddrAttr)attr).getAddress();
                    secPort = ((AddrAttr)attr).getPort();
//                  // DEBUG
//                  System.out.printf("setting changed/secondary address to %s:%d\n",
//                          secHost, secPort);
//                  // END
                    break;

                case MESSAGE_INTEGRITY:
                    // already dealt with
                    break;

                /* IGNORE EXTENSIONS */
                case XOR_ONLY:
                case XOR_MAPPED_ADDRESS:
                case SERVER_NAME:
                    break;
                case SECONDARY_ADDRESS:
                    secHost = ((AddrAttr)attr).getAddress();
                    secPort = ((AddrAttr)attr).getPort();
                    // DEBUG
                    System.out.printf("setting secondary address to %s:%d\n",
                            secHost, secPort);
                    // END
                    break;

                default:
                    logAndThrow(
                            "unexpected StunAttr type " + attr.type);
            }
        }
        changeFlags   = 0;
        socket.close();
    }
    public void close() {
        closed = true;

    }
    /**
     * XXX NEEDS TO THROW AN EXCEPTION if not authenticating.
     * Of course it will - an NPE, because tlsCtr will be null.
     */
    public void fetchUserNameAndPassword() {
        Connection knx = null;
        try {
            knx = tlsCtr.connect(myTlsEnd, true);
            OutputStream outs = knx.getOutputStream();
            InputStream  ins  = knx.getInputStream();

            SharedSecretRequest req = new SharedSecretRequest();
            int reqLen = req.wireLength();
            byte[] outBuffer = new byte[reqLen];
            req.encode(outBuffer);
            outs.write(outBuffer, 0, reqLen);

            byte[] inBuf = new byte[256];
            ins.read(inBuf);
            StunMsg resp = StunMsg.decode(inBuf);
            userName  = resp.get(0).value;
            password  = resp.get(1).value;
            clientKey = new SecretKeySpec(password, "HmacSHA1");
        } catch (IOException ioe) {
            LOG_MSG (
                "not authenticating; can't create username and password - "
                    + ioe.toString() );
            authenticating = false;
        } finally {
            if(knx != null)
                try { knx.close(); } catch (Throwable t) {}
        }
    }
    // RFC 2782 says that clients MUST prefer servers with
    // numerically lower priorities and SHOULD prefer 
    // servers with non-zero weights according to a rather
    // complicated randomized choice algorithm.
    protected static class FoursomeComp implements Comparator {
        public int compare (Object o1, Object o2) {
            if (! (o1 instanceof Foursome) )
                throw new IllegalArgumentException("not Foursome");
            if (! (o2 instanceof Foursome) )
                throw new IllegalArgumentException("not Foursome");
            Foursome obj1 = (Foursome) o1;
            Foursome obj2 = (Foursome) o2;
            if (obj1.priority < obj2.priority)
                return -1;
            if (obj1.priority > obj2.priority)
                return 1;
            if (obj1.weight > obj2.weight)
                return -1;
            else if (obj1.weight < obj2.weight)
                return 1;
            else
                return 0;
        } 
    }
    protected final static class Foursome {
        public final int priority;
        public final int weight;
        public final String server;
        public final int port;
        public Foursome(int p, int w, String s, int n) {
            priority = p;
            weight   = w;
            server   = s;
            port     = n;
        }
    }
    /**
     * Discover the STUN server for a given domain, using a particular
     * recursive name server for the search.  
     *
     * XXX This implementation is a bit of a hack: it returns the first
     * XXX match on the query string.
     *
     * @param nameServer name server to be used in the search
     * @param domain     fully qualified domain name
     * @param findUDP    if true, find a UDP server, otherwise, TCP
     */
    public static ServerInfo[] discoverServers (
            String nameServer, String domain, boolean findUDP) 
                                            throws NamingException {
        if (domain == null || domain.equals(""))
            throw new IllegalArgumentException("null or empty domain");
        StringBuffer sb = new StringBuffer("dns://");
        if (nameServer != null)
            sb.append(nameServer);
        sb.append("/_stun._");
        if (findUDP)
            sb.append("udp.");
        else
            sb.append("tcp.");
        sb.append(domain);
        String search = sb.toString();
//      // DEBUG
//      System.out.println("name server: " + nameServer
//              + "; domain: " + domain + ", looking for "
//              + (findUDP? "udp" : "tcp")
//              + "\n  search string is " + search);
//      // END
        DirContext dctx = new InitialDirContext();
        // the list is EMPTY if there is no match 
        Attributes attrs = dctx.getAttributes(
                    search,
                    new String[] {"SRV"});
//      // DEBUG
//      System.out.println("javax.naming returns " +
//              attrs.size() + " attribute(s)");
//      // END
        if (attrs.size() == 0)
            return null;

        FoursomeComp comp = new FoursomeComp();
        TreeSet sortedServers = new TreeSet (comp);
        NamingEnumeration idEnum = attrs.getIDs();
        while (idEnum.hasMore()) {
            String id = (String)idEnum.next();
            BasicAttribute attr = (BasicAttribute)attrs.get(id);
//          // DEBUG
//          System.out.println("attr = " + attr.toString());
//          // END
            // Looks like
            //   SRV: 10 0 3478 stun.xlattice.org.
            // Note the colon and trailing dot.
            // The two numeric fields are priority and weight.
            String[] s = attr.toString().trim().split(" ");
            if (s != null && s.length == 5)
                if (s[0] != null && s[0].equals("SRV:")) {
                    int priority  = -1;
                    int weight    = -1;
                    String server = null;
                    int port      = -1;
                    try {
                        priority = Integer.parseInt(s[1]);
                        weight   = Integer.parseInt(s[2]);
                        port     = Integer.parseInt(s[3]);
                        server   = s[4];

                    } catch (NumberFormatException nfe) {
                        priority = -1;
                    } 
                    if (priority != -1 && !server.equals(".")) {
                        sortedServers.add( new Foursome(
                                    priority, weight, server, port));
                    }
                }
        }
        ServerInfo ret[] = new ServerInfo [sortedServers.size()];
        Iterator it  = sortedServers.iterator();
        for (int i = 0; i < sortedServers.size(); i++) {
            Foursome f = (Foursome)it.next();
            ret[i] = new ServerInfo (f.server, f.port, findUDP);
        }
        return ret;
    }
    public static ServerInfo[] discoverServers (
                                String domain, boolean findUDP) 
                                                throws NamingException {
        return discoverServers (null, domain, findUDP);
    }
    // RFC 3489 TESTS ///////////////////////////////////////////////

    /**
     * Send a bare BindingRequest.  If it times out, return NO_UDP;
     * we can't go further.  If it doesn't time out, we have some 
     * sort of NAT between us and the server, so return NATTED.
     *
     * This test initializes the NattedAddress.  The NAT type must
     * be set after the last test, when the type is known.
     */
    protected int test1a()
                throws CryptoException, IOException, StunException {
        int type = -1;
        try {
            bind(-1);       // use index 0 and localPort if non-zero
        } catch (SocketTimeoutException ste) {
            type = NO_UDP;
        }
        if (mappedHost.equals(localHost)) {
            type = NOT_NATTED;
            nattedAddr = new NattedAddress(localHost, localPort);
        } else {
            type = NATTED;
            nattedAddr = new NattedAddress(localHost, localPort, 
                                           mappedHost, mappedPort);
        }
        return type;
    }
    /**
     */
    protected int test2(boolean natted)
                throws CryptoException, IOException, StunException {
        changeFlags = CHANGE_IP | StunAttr.CHANGE_PORT;
        try {
            bind(0);
        } catch (SocketTimeoutException ste) {
            return natted? INCONCLUSIVE : SYM_FIREWALL;
        }
        return natted? FULL_CONE : OPEN_INTERNET;
    }

    /**
     * Send out a bare BindingRequest to the server's alternative
     * address and port.  If it succeeds, we have a full cone NAT.
     * If it times out, we need to run test 3.
     *
     * @return FULL_CONE or INCONCLUSIVE
     */
    protected int test1b()
                throws CryptoException, IOException, StunException {
        Inet4Address mappedHostWas = mappedHost;
        int          mappedPortWas = mappedPort;
        try {
            bind(2);
        } catch (SocketTimeoutException ste) {
            // the RFC doesn't actually allow for this condition
            return INCONCLUSIVE;
        }
        if (mappedHostWas.equals(mappedHost)
                && (mappedPortWas == mappedPort))
            return INCONCLUSIVE;
        else
            return SYMMETRIC_NAT;
    }
    /**
     * Send a BindingRequest with a change port request to the
     * server.  If there is a response, we have a restricted NAT.
     * If it times out, we have a port restricted NAT.
     *
     * @return RESTRICTED or PORT_RESTRICTED
     */
    protected int test3()
                throws CryptoException, IOException, StunException {
        changeFlags = CHANGE_PORT;
        try {
            bind (0);
        } catch (SocketTimeoutException ste) {
            return PORT_RESTRICTED;
        }
        return RESTRICTED;
    }
    /**
     * Spend a lot of time guesstimating how long a port binding
     * lasts.  RFC 3489 section 10.1 suggests that (a) the client
     * obtain a binding using port X, obtaining a MappedAddress 
     * of pA, pP, and then (b) send BindingRequests from port Y != X 
     * with a ResponseAddress set to the pA, pP after time T.   If
     * the BindingRequest succeeds, T is too small.  If it fails, T
     * is too large.  A binary search determines the maximum lifetime.
     *
     * This implementation will return a value approximating the
     * nearest second.  No attempt is made to ensure that the value
     * returned is stable, so if the value returned is erratic, this
     * method may never return a value.
     *
     * @return the estimate in seconds
     */
    public int bindingLifetime() {
        Thread myThread = Thread.currentThread();
        Inet4Address lastHost;
        int lastPort;
        int lifetime   = -1;
        long guess     = 600000;    // guess at lifetime, ms (10 minutes)
        long lastGuess =     0;
        try {
            bind(0);
            lastHost = mappedHost;
            lastPort = mappedPort;
            while (true) {
                myThread.sleep(guess);
                bind(0);
                long newGuess;
                // XXX ignore mappedHost for now
                if (mappedPort != lastPort) {
                    // lifetime is too small
                    if (lastGuess < guess)
                        newGuess = guess * 2;
                    else 
                        newGuess = (guess + lastGuess)/2;
                } else {
                    // same port, so lifetime is too large; we are searching down
                    if (lastGuess < guess) 
                        newGuess = (guess + lastGuess)/2;
                    else
                        newGuess = guess / 2;
                }
                // DEBUG
                LOG_MSG ("lastGuess = " + lastGuess 
                        + "; guess = " + guess + "; newGuess = " + newGuess);
                // END
                lastGuess = guess;
                guess = newGuess;
                long delta = guess > lastGuess ? (guess - lastGuess)
                                               : (lastGuess - guess);
                if (delta < 1000L) {
                    // round to nearest second
                    lifetime = (int) ((guess + 500L)/1000L);
                    break;
                }
            }  
        } catch (CryptoException ce) {
            /* STUB */
        } catch (SocketTimeoutException ste) {
            // this is of course covered by the next exception
            /* STUB */
        } catch (IOException ioe) {
            /* STUB */
        } catch (StunException se) {
            /* STUB */
        } catch (InterruptedException inte) {
            /* STUB */
        }
        return lifetime;
    }
    // COMMAND LINE INTERFACE METHODS ///////////////////////////////
    /**
     * Needs to be prettier.
     */
    public static void usage () {
        System.out.println(
            "usage: ./stun.client.sh [options] server[:port]\n"
          + "where the options are\n"
          + "  -h       show this useful message and exit\n"
          + "  -d name  do server discover for domain name\n"
          + "  -l dir   write log to this directory (default = .)\n"
          + "  -p N     use local port N\n"
          + "  -s       just get the username and secret\n"
          + "  -t       use authentication (requires TCP secret server)\n"
          + "  -v       be talkative\n"
          + "Either the server name or its address may be specified.\n"
          + "The port defaults to 3478.\n"
        );
        System.exit(0);
    }
    public static void usage (String msg) {
        System.out.println(msg);
        usage();
    }
    public static Inet4Address getIPAddr(String s) {
        Inet4Address addr = null;
        try {
            addr = (Inet4Address) InetAddress.getByName(s);

        } catch (ClassCastException cce) {
            System.out.println(
                    "not a valid domain name or IPv4 address: " + s);
            usage();
        } catch (UnknownHostException uhe) {
            System.out.println("unknown host: " + s);
            System.exit(0);
        }
        return addr;
    }
    public static boolean validPort(int n) {
        return ( 0 <= n && n < 65536);
    }
    public static int getPortArg (String s) {
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
    /**
     * Determines the NAT type and returns it in a NattedAddress,
     * which contains the local address (host and port number) and the
     * mapped address if different.
     */
    public NattedAddress getNattedAddress() 
                throws CryptoException, IOException, StunException {

        // this call actually initializes a new NattedAddress
        natType = test1a();
        if (natType < 0)
            natType = test2 ( natType == NATTED );
        if (natType < 0)
            natType = test1b();
        if (natType < 0)
            natType = test3();
        nattedAddr.setNatType(natType);
        return nattedAddr;
    }
    /** 
     * Returns a String description of the NAT type, possibly with
     * embedded newlines, and newline-terminated.
     */
    public String getNatDescription() 
                throws CryptoException, IOException, StunException {
        
        getNattedAddress();  
        
        StringBuffer sb = new StringBuffer();

        if (natType < NO_UDP || natType > SYMMETRIC_NAT) 
            sb.append (
                "*INTERNAL ERROR: UNKNOWN NAT TYPE " + natType + "*");
        else 
            sb.append(NAT_TYPES[natType]);

        sb.append("\nlocal host:  ") .append(nattedAddr.getLocalHost())
          .append("\nlocal port:  ") .append(nattedAddr.getLocalPort());
        if (natType != NO_UDP) {
            sb.append("\nmapped host: ")
              .append(nattedAddr.getMappedHost())
              .append("\nmapped port: ")
              .append(nattedAddr.getMappedPort());
        }
        return sb.append('\n').toString();
    }
    /**
     * Command line interface to the STUN client.  Does the requested
     * test and exits.
     *
     */
    public static void main (String [] args) throws Exception {
        System.out.println("STUN client version " + version);

        // set up defaults
        // THE NAME SHOULD BE SET FROM THE COMMAND LINE:
        String ifaceName = "eth0";
        NetworkInterface iface = null;
        try {
            iface = NetworkInterface.getByName(ifaceName);
        } catch (SocketException se) {
            System.out.printf("can't find network interface %s - %s\n",
                    ifaceName, se);
            System.exit(-1);
        }
        Inet4Address pA  = null;                // server IP address
        int pP           = STUN_SERVER_PORT;    // server port
        Inet4Address myA = null;
        int myP          = 0;

        String  logDir          = ".";
        boolean authenticate    = false;
        boolean justGetSecret   = false;
        boolean serverDiscovery = false;
        String  domainName      = null;
        boolean verbose         = false;

        // now see what the user has to say
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals ("-h"))
                usage();

            if (args[i].equals("-d")) {
                serverDiscovery = true;
                if (++i >= args.length) 
                    usage("you must provide a domain name");
                domainName = args[i];
            } if (args[i].equals("-l")) {
                if (++i >= args.length) 
                    usage("you must name a log directory");
                logDir = args[i];
            } if (args[i].equals("-p")) {
                if (++i >= args.length) 
                    usage("you must supply a port number");
                myP = getPortArg( args[i] );
            } else if (args[i].equals("-s")) {
                authenticate = true;
                justGetSecret = true;
            } else if (args[i].equals("-t")) {
                authenticate = true;
            } else if (args[i].equals ("-v")) {
                verbose = true;

            } else if (!args[i].startsWith("-")) {
                if (i + 1 != args.length)
                    usage("server name must be last argument");
                String[] s = args[i].split(":");
                if (s.length == 1) {
                    pA = getIPAddr(args[i]);
                } else if (s.length == 2) {
                    pA = getIPAddr(s[0]);
                    pP = getPortArg(s[1]);
                } else {
                    usage ("can't understand " + args[i]);
                }
            } else {
                usage();
            }
        }
        if (pA == null)
            usage("no server address specified");
        Client client = new Client(pA, pP, iface,
                            logDir, authenticate, verbose);

        if (serverDiscovery) {
            // XXX simplified: don't allow name server, only udp
            ServerInfo[] servers = null;
            try {
                servers = client.discoverServers(domainName, !authenticate);
            } catch (NamingException ne) {
                System.out.println( ne.toString() );
                servers = null;
            }
            if (servers == null || servers.length == 0)
                System.out.println("no servers found for " + domainName);
            else 
                for (int i = 0 ; i < servers.length; i++)
                    System.out.println( servers[i].toString() );
        }
        else if (justGetSecret) {
            // must support this in combination with server discovery
            client.fetchUserNameAndPassword();
            byte[] userName = client.getUserName();
            byte[] password = client.getPassword();
            if (userName != null && password != null) {
                System.out.println(
                    "username is " + StringLib.byteArrayToHex(userName)
                + "\npassword is " + StringLib.byteArrayToHex(password));
                if (verbose) {
                    /* STUB - print out stuff about the TLS connection*/
                }
            } else {
                System.out.println("failed to get username and password");
            }
        } else {
            try {
                System.out.print( client.getNatDescription() );
            } catch (CryptoException ce) {
                throw new IllegalStateException(
                        "authentication failure during NAT type detection: "
                            + ce);
            } catch (IOException ioe) {
                throw new IllegalStateException (
                        "unexpected fault during NAT type detection: " + ioe);
            } catch (StunException se) {
                System.out.println(se.toString());
            } 
        } 
        client.close();
        System.exit(0);         // XXX hangs if not present
    }
}
