/* SecretServer.java */
package org.xlattice.protocol.stun;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.crypto.SecretKey;

import org.xlattice.Connection;
import static org.xlattice.crypto.tls.TlsConst.*;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.tls.Tls;
import org.xlattice.transport.tls.TlsAcceptor;
import org.xlattice.transport.tls.TlsAddress;
import org.xlattice.util.NonBlockingLog;

/**
 *
 * @author Jim Dixon
 */
public class SecretServer       extends Thread implements StunConst {
   
    public static final int BUFSIZE = 256;
    
    // INSTANCE VARIABLES ///////////////////////////////////////////
    private final Tls myTls;
    private final TlsAcceptor acceptor;
    private final SecretKey usernameSecret;
    private final SecretKey passwordSecret;
    private final KeyStore keyStore;
    private final char[] passphrase;
    private final boolean verbose;
    
    private SecureRandom rng = null;
    private volatile boolean running;
    private Object lock = new Object();
    private Thread myThread;

    // LOGGING //////////////////////////////////////////////////////
    protected final NonBlockingLog serverLog;
    protected void LOG_MSG(String s) {
        serverLog.message(
                new StringBuffer("SecretServer: ").append(s).toString());
    }
    protected final void badArg(String msg)
                                        throws IllegalArgumentException {
        LOG_MSG(msg);
        throw new IllegalArgumentException(msg);
    }
        
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * Creates a server which provides username/password pairs over
     * a secure TLS connection.  The caller must provide two crypto-grade 
     * secret keys for generating HMACs.
     *
     * @param host         server IPv4 address
     * @param port         IP port number
     * @param ksName       key store file name
     * @param passwd       key store passphrase
     * @param secret1      used for HMACs on usernames
     * @param secret2      used for creating passwords
     * @param logDir       where we write our logs
     * @param verbose      whether we are verbose when we log
     */
    public SecretServer (Inet4Address host, int port, 
                         String ksName, String passwd,
                         SecretKey secret1, SecretKey secret2,
                         String logDir, boolean verbose) 
                            throws GeneralSecurityException, IOException {

        // we trust that the Server has checked the logDir variable
        serverLog = NonBlockingLog.getInstance(logDir + "stun.server.log");
        
        if (host == null) 
            badArg("null primary address");
        if (port < 0 || 65535 < port)
            badArg("invalid port number " + port);
        if (ksName == null) 
            badArg("null key store name");
        if (passwd == null || passwd.length() == 0)
            badArg("null or empty passphrase");
        passphrase = passwd.toCharArray();
        keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(ksName), passphrase);
        
        myTls = new Tls();
        IPAddress  myIPAddr = new IPAddress(host, port);
        TlsAddress myAddr   = new TlsAddress( myIPAddr, ANONYMOUS_TLS,
                                keyStore, passphrase, true);
        try {
            acceptor = (TlsAcceptor) myTls.getAcceptor(myAddr, true);
        } catch (IOException ioe) {
            String msg = new StringBuffer("can't start secret server - ")
                .append(ioe.toString())
                .toString();
            LOG_MSG(msg);
            throw new IllegalStateException (msg);
        } 
            
        if (secret1 == null || secret2 == null)
            throw new IllegalArgumentException("null secret");
        usernameSecret = secret1;
        passwordSecret = secret2;
        this.verbose = verbose;

        rng = new SecureRandom();
        start();
    }
    public SecretServer (Inet4Address host, int port, 
                         String ksName, String passwd,
                         SecretKey secret1, SecretKey secret2,
                         boolean verbose) 
                            throws GeneralSecurityException, IOException {
        this (host, port, ksName, passwd, secret1, secret2, "./", verbose);
    }

    protected final SharedSecretErrorResponse malformed(StunMsg msg) {
        SharedSecretErrorResponse resp 
                    = new SharedSecretErrorResponse(msg.getMsgID());
        resp.add(new ErrorCode(400));
        return resp;
    }
    public void run() {
        myThread = Thread.currentThread();
        Connection conn = null;
        running = true;
        while (true) {
            synchronized (lock) {
                if (!running)
                    break;
            }
            try {
                conn = acceptor.accept();
                IPAddress clientIP 
                            = (IPAddress)conn.getFarEnd().getAddress();
                // XXX should be outside loop
                byte[] buffer = new byte[BUFSIZE];
                InputStream ins = conn.getInputStream();
                int bytesIn = ins.read(buffer, 0, BUFSIZE);

                StunMsg msg = null;
                StunMsg resp = null;
                try {
                    msg = StunMsg.decode(buffer);
                } catch (IllegalArgumentException iae) {
                    continue;       // can't decode message, so drop it    
                } catch (IllegalStateException ise) {
                    continue;       // can't decode message, so drop it    
                }
                if (resp == null && msg.type != SHARED_SECRET_REQUEST) {
                    resp = malformed(msg);
                } 
                if (resp == null && msg.size() != 0) {
                    // Per RFC, ignore 0x8000 and higher, 
                    // complain about 0x7fff and lower
                    for (int i = 0; i < msg.size(); i++) {
                        if (msg.get(i).type < 0x8000) {
                            resp = malformed(msg);
                            break;
                        }
                    }
                }
                if (resp == null /* no error so far */ ) {
                    resp = new SharedSecretResponse(msg.getMsgID());
                    byte[] userName = UserName.generate(
                            (Inet4Address)clientIP.getHost(), 
                            clientIP.getPort(), rng, usernameSecret);
                    resp.add(new UserNameAttr(userName));
                    byte[] password = Password
                                .generate(passwordSecret, userName);
                    resp.add(new PasswordAttr(password));
                }
                int outLen = resp.wireLength();
                byte[] outBuf = new byte[outLen];
                resp.encode(outBuf);
                OutputStream outs = conn.getOutputStream();
                outs.write(outBuf, 0, outLen);
                if (verbose) {
                    StringBuffer sb = new StringBuffer("client ")
                                        .append(clientIP);
                    if (resp.type == SHARED_SECRET_ERROR_RESPONSE)
                        sb.append(" - FAILED");
                    LOG_MSG(sb.toString());
                }
                // if there has been no problem, let the client
                // close the connection
            } catch ( Exception e ) {
                if (conn != null && !conn.isClosed())
                    try {
                        conn.close();
                    } catch ( Exception e2) { /* ignore it */ }
            }
        }
    }
    /**
     * Closes the Acceptor and then blocks until this thread stops
     * running.
     */
    public void close()                         throws Exception {
        synchronized (lock) {
            running = false;
            if (acceptor != null && !acceptor.isClosed())
                acceptor.close();
        }
        if ( Thread.currentThread() != myThread && myThread != null ) 
            myThread.join();
    }
    public synchronized boolean isRunning() {
        return running;
    }
}
