/* BindingServer.java */
package org.xlattice.protocol.stun;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.Vector;                // laziness

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.xlattice.CryptoException;
import org.xlattice.util.NonBlockingLog;

/**
 * A simple stateless single-threaded server that receives STUN
 * messages on a UDP port and relays replies through an array of
 * BindingSenders.
 * 
 * We need four of these in the STUN server, one for each of the four 
 * combinations of primary/secondary address and primary/secondary
 * port.  
 * 
 * XXX MUST CHECK FOR STALE SECRETS (grep on EXTRACT TIME)
 *
 * @author Jim Dixon
 */
public class BindingServer          extends Thread implements StunConst {
    public static final int INBUF_SIZE  = 512;
        
    private final DatagramSocket socket;
    private final Inet4Address myAddr;
    private final int          myPort;
    private final Inet4Address priAddr;
    private final int          priPort;
    private final Inet4Address secAddr;
    private final int          secPort;
    private final SecretKey    usernameSecret;
    private final SecretKey    passwordSecret;
    private final boolean      authenticating;
    private final boolean      authOptional;
    private final BindingSender[] senders;
    private final boolean      verbose;

    private volatile boolean   running;
    private Object lock = new Object();
    private Thread myThread;

    // these used to be private to run()
    private Inet4Address clientAddr;
    private int          clientPort = 0;
    private Inet4Address sourceAddr;
    private int          sourcePort;
    private Inet4Address targetAddr;
    private int          targetPort;
        
    // LOGGING //////////////////////////////////////////////////////
    protected final NonBlockingLog serverLog;
    protected void LOG_MSG(String s) {
        serverLog.message(
                new StringBuffer("BindingServer: ").append(s).toString());
    }
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Creates a binding server which listens on the socket passed
     * and optionally permits or enforces authentication.  For
     * authentication to be possible two SecretKeys must be supplied,
     * one for validating client usernames and the other for validating
     * client passwords.  Client may specify that BindingResponses are
     * to be sent from any of four address/port combinations, so
     * BindingResponse messages are relayed through an array of four
     * BindingSenders which run on separate threads.
     * 
     * @param socket  used for receiving messages
     * @param priAddr primary server address
     * @param priPort primary server port
     * @param secAddr secondary server address
     * @param secPort secondary server port
     * @param secret1 if present, used for creating username HMACs
     * @param secret2 used for creating passwords
     * @param authOptional whether clients may omit authentication
     * @param senders used for sending messages from different address/port
     */
    protected BindingServer (final DatagramSocket socket, 
                             Inet4Address priAddr, int priPort,
                             Inet4Address secAddr, int secPort,
                             SecretKey secret1, SecretKey secret2,
                             boolean authOptional, BindingSender[] senders,
                             String logDir, boolean verbose) {
        // Server must have checked logDir variable
        serverLog = NonBlockingLog.getInstance(logDir + "stun.server.log");
        if (socket == null)
            throw new IllegalArgumentException("null DatagramSocket");
        this.socket = socket; 
        myAddr = (Inet4Address)socket.getLocalAddress();
        myPort = socket.getLocalPort();
//      // DEBUG
//      System.out.println("BindingServer.init():"
//              + "\n    address = " + myAddr.toString()
//              + "\n    port    = " + myPort
//              + "\n    secAddr = " + secAddr.toString()
//              + "\n    secPort = " + secPort
//      );
//      // END

        if (priAddr == null)
            badArg("null server primary address");
        if (priPort <= 0 || priPort > 65535)
            badArg("primary port number out of range: " + priPort);
        this.priAddr = priAddr;
        this.priPort = priPort;
        
        if (secAddr == null)
            badArg("null server secondary address");
        if (secPort <= 0 || secPort > 65535)
            badArg("secondary port number out of range: " + secPort);
        this.secAddr = secAddr;
        this.secPort = secPort;

        // if authentication is to be performed, must have both
        // secrets
        if (secret1 == null || secret2 == null) {
            usernameSecret = null;
            passwordSecret = null;
        } else {
            usernameSecret = secret1;
            passwordSecret = secret2;
        }
        if (usernameSecret == null) {
            LOG_MSG("username secret null, disabling authentication");
            authenticating    = false;
        } else {
            authenticating    = true;
        }
        this.authOptional = authOptional;   // ignored if !authenticating

        if (senders == null || senders.length != 4)
            badArg("null BindingSender array or wrong number of them");
        this.senders = senders;
        this.verbose = verbose;
        start();
        
    }
    protected BindingServer (final DatagramSocket socket, 
                             Inet4Address priAddr, int priPort,
                             Inet4Address secAddr, int secPort,
                             SecretKey secret1, SecretKey secret2,
                             boolean authOptional, BindingSender[] senders,
                             boolean verbose) {
        this (socket, priAddr, priPort, secAddr, secPort, secret1, secret2,
                authOptional, senders, "." + File.separator, verbose);
    }
    private final void badArg(String msg)   
                                throws IllegalArgumentException {
        LOG_MSG(msg);
        throw new IllegalArgumentException (msg);
    }
    /** 
     * Make an error response message.  XXX This cannot carry any
     * attributes other than UnknownAttribute.
     */
    private BindingErrorResponse makeErrorResponse(
                                            StunMsg msg, int n) {
        BindingErrorResponse resp 
                        = new BindingErrorResponse(msg.getMsgID());
        ErrorCode errCode = new ErrorCode(n);
        resp.add(errCode);

        LOG_MSG( new StringBuffer()
          .append( clientAddr.toString() )
          .append( ':' )
          .append( clientPort )
          .append( ' ' )
          .append( errCode.toString() )
          .toString());
        
        return resp;
    }
    public void run() {
        byte[] inBuf = new byte[INBUF_SIZE];
        running = true;
        myThread = Thread.currentThread();

        while (running) {
            synchronized (lock) {
                if (!running)
                    break;
            }
            clientAddr = null;
            try {
                DatagramPacket inPkt = new DatagramPacket(inBuf, INBUF_SIZE);
                boolean doAuth = authenticating & !authOptional;
                SecretKey clientKey = null;
                socket.receive(inPkt);
                clientAddr = (Inet4Address)inPkt.getAddress();
                clientPort = inPkt.getPort();

                // XXX NEED TO VERIFY THIS ASSUMPTION AGAINST RFC
                sourceAddr = myAddr;            // default
                sourcePort = myPort;            // default
                // XXX END NEED
                targetAddr = clientAddr;        // default
                targetPort = clientPort;        // default

                StunMsg resp = null;        // if error, not null
                int senderIndex = 0;        // default = pA/pP
                Vector unknowns = null;
                StunMsg msg  = null;
                try {
                    msg = StunMsg.decode(inBuf);
                } catch (IllegalArgumentException iae) {
                    resp = makeErrorResponse (msg, 400);    // malformed 
                }
                if (resp == null && msg.type != BINDING_REQUEST) {
                    if (msg.type == SHARED_SECRET_REQUEST)
                        resp = makeErrorResponse (msg, 433);    // use TLS
                    else 
                        resp = makeErrorResponse (msg, 400);    // malformed
                }
                if (verbose) {
                    LOG_MSG ( new StringBuffer("binding request from ")
                        .append(clientAddr)
                        .append(':')
                        .append(clientPort)
                        .toString() );
                }    
                // CHECK ATTRIBUTES ///////////////////////
                if (resp == null) {
                    boolean hasUserName = false;
                    boolean hasMsgInteg = false;
                    for (int i = 0; i < msg.size(); i++) {
                        int type = msg.get(i).type;
                
                        if (type == CHANGE_REQUEST) {
                            int flags = msg.get(i).value[3];
                            switch (flags) {
                                case 0:     
                                    senderIndex = 0;    break;
                                case CHANGE_PORT:
                                    if (myPort == priPort)
                                        sourcePort = secPort;
                                    else 
                                        sourcePort = priPort;
                                    senderIndex = 1;    break;
                                case CHANGE_IP:
                                    if (myAddr.equals(priAddr))
                                        sourceAddr = secAddr;
                                    else 
                                        sourceAddr = priAddr;
                                    senderIndex = 2;    break;
                                case CHANGE_PORT + CHANGE_IP:
                                    if (myAddr.equals(priAddr))
                                        sourceAddr = secAddr;
                                    else 
                                        sourceAddr = priAddr;
                                    if (myPort == priPort)
                                        sourcePort = secPort;
                                    else 
                                        sourcePort = priPort;
                                    senderIndex = 3;    break;
                                default:
                                    LOG_MSG("unknown message type " + type
                                        + " from " + clientAddr 
                                        + ':' + clientPort);
                                    resp = makeErrorResponse(msg, 400);
                            }
                        } else if (type == RESPONSE_ADDRESS) {
                            AddrAttr attr = (AddrAttr)msg.get(i);
                            targetAddr = attr.getAddress();
                            targetPort = attr.getPort();
                        } else if (type == USERNAME) {
                            hasUserName = true;
                            byte[] userName = msg.get(i).value;

                            // extract age (rounded down to nearest 10 
                            // minutes), 430 if expired, where 'expired' 
                            // means more than 20 minutes old (so between
                            // 10 and 20 minutes old)
                            UserName decoded = null;
                            try {
                                decoded = UserName.decode (
                                    usernameSecret, userName, 0);
                            } catch (CryptoException ce) {
                                LOG_MSG("can't decode UserName - " 
                                        + ce);
                                resp = makeErrorResponse(msg, 500);
                                break;
                            }
                            long ageInMs = new Date().getTime() - decoded.ms;
                            if (ageInMs > 20 * 60 * 1000) {
                                resp = makeErrorResponse (msg, 430);
                                break;
                            }
                            // calculate the password from the password key
                            byte[] password = null;
                            try {
                                password = Password.generate (
                                            passwordSecret, userName);
                            } catch (CryptoException ce) {
                                // should never happen, would be an 
                                // internal error
                                resp = makeErrorResponse (msg, 500);
                                break;
                            }
                            // create key used to verify HMAC
                            clientKey = new SecretKeySpec(
                                                    password, "HmacSHA1");

                        } else if (type == MESSAGE_INTEGRITY) {
                            hasMsgInteg = true;
                            // this must be the last attribute
                            if (i != msg.size() - 1) {
                                resp = makeErrorResponse(msg, 400);// malformed
                            } else if (!hasUserName) {
                                resp = makeErrorResponse(msg, 432);// missing
                            }
                            if (!doAuth && authOptional)
                                doAuth = true;
                            if (!doAuth) {
                                // the RFC doesn't cover this possibility;
                                // we respond with a general failure
                                resp = makeErrorResponse(msg, 600);
                            }
                        } else if (
                                type == MAPPED_ADDRESS     ||
                                type == SOURCE_ADDRESS     ||
                                type == CHANGED_ADDRESS    ||
                                type == PASSWORD           ||
                                type == ERROR_CODE         ||
                                type == UNKNOWN_ATTRIBUTES ||
                                type == REFLECTED_FROM     ||
                                // Vovida extension; should never see
                                type == XOR_ONLY) {
                            resp = makeErrorResponse(msg, 400);
                        } else if (type >= 0x8000) {
                            // ignored
                            continue;
                        } else {
                            // less than 0x8000 
                            if (unknowns == null) 
                                unknowns = new Vector();
                            unknowns.add ( new Integer(type) );
                        }
                    } // end for

                } // end if (resp == null)
                
                if (unknowns != null) {
                    resp = makeErrorResponse(msg, 420);
                    int[] attrs = new int[ unknowns.size() ];
                    for (int j = 0; j < attrs.length; j++) 
                        attrs[j] = ((Integer)unknowns.get(j)).intValue();
                    resp.add( new UnknownAttributes(attrs) );
                }

                // VERIFY HMAC ////////////////////////////
                if (resp == null && doAuth) {
                    if (clientKey == null) {
                        resp = makeErrorResponse(msg, 432); // missing username
                        doAuth = false;         // we can't, no client key
                    } else {
                        // verify HMAC, 431 if it fails
                        int msgLen  = HEADER_LENGTH + msg.length();
                        int miIndex = msg.size() - 1;
                        MessageIntegrity mi = 
                            (MessageIntegrity)msg.get(miIndex);
                        boolean ok;
                        try {
                            ok = mi.verify(inBuf, msgLen, clientKey);
                        } catch (CryptoException ce) { 
                            ok = false;
                        }
                        if (!ok) {
                            LOG_MSG("HMAC verification failed "
                                    + clientAddr + ':' + clientPort);
                            resp = makeErrorResponse(msg, 431);        
                        }
                    }
                }
               
                // GENERATE STANDARD RESPONSE /////////////
                if (resp == null) {
                    resp = new BindingResponse(msg.getMsgID());
                    // MappedAddress, SourceAddress, ChangedAddress
                    resp.add (new MappedAddress  (clientAddr, clientPort));    
                    resp.add (new SourceAddress  (sourceAddr, sourcePort));
                    resp.add (new ChangedAddress (secAddr,    secPort));
                    if ( !targetAddr.equals(clientAddr) 
                                        || (targetPort != clientPort) )
                        resp.add (new ReflectedFrom(clientAddr, clientPort));
                    if (doAuth) 
                        resp.add (new MessageIntegrity());
                } else {
                    // any response is an error, so don't authenticate
                    doAuth = false;
                }
                
                // XXX it would be more efficient to use the same buffer
                // each time, but would need to change Outgoing constructor,
                // adding a length
                byte[] outBuf = new byte[ resp.wireLength() ];
                resp.encode(outBuf);
                if (doAuth && clientKey != null) {
                    try {
                        MessageIntegrity
                            .setHMAC(outBuf, outBuf.length, clientKey);
                    } catch (CryptoException ce) {
                        resp = makeErrorResponse(msg, 500);
                        outBuf = new byte[ resp.wireLength() ];
                        resp.encode(outBuf);
                    }
                }
                Outgoing out = new Outgoing (clientAddr, clientPort, outBuf);
                senders[senderIndex].schedule(out);
                if (verbose) {
                    StringBuffer sb = new StringBuffer("client ")
                                        .append(clientAddr.toString())
                                        .append(':')
                                        .append(clientPort);
                    if (resp.type == BINDING_ERROR_RESPONSE)
                        sb.append (" - FAILED");
                    LOG_MSG(sb.toString());
                }
                    
            } catch (IOException ioe) { 
                StringBuffer sb = new StringBuffer("dropping packet");
                if (clientAddr != null) {
                    sb.append( " from ")
                      .append( clientAddr.toString() )
                      .append( ':' )
                      .append( clientPort );
                }
                sb.append(" - ")
                  .append(ioe.toString());
                LOG_MSG(sb.toString());
                /* drop the packet */ 
            } catch (IllegalArgumentException iae) {
                LOG_MSG("dropping packet, unexpected exception - "
                        + iae);
            }
        } // end while 
    } // end run()
    /**
     * Closes the Socket and then blocks until this thread stops
     * running.
     */
    public void close()                     throws Exception {
        synchronized (lock) {
            running = false;
            socket.close();
        }
        if ( Thread.currentThread() != myThread && myThread != null ) 
            myThread.join();
    }
    public synchronized boolean isRunning() {
        return running;
    }
    
    
}
