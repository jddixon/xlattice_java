/* TestSecretServer.java */
package org.xlattice.protocol.stun;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.KeyStore;
import java.util.Random;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import junit.framework.*;

import org.xlattice.Acceptor;
import org.xlattice.Connection;
import org.xlattice.Connector;
import org.xlattice.EndPoint;
import org.xlattice.Protocol;
import org.xlattice.Transport;
import static org.xlattice.crypto.tls.TlsConst.*;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.tls.Tls;
import org.xlattice.transport.tls.TlsAddress;

import org.xlattice.util.StringLib;

/**
 * @author Jim Dixon
 */

public class TestSecretServer extends TestCase implements StunConst {

    private KeyGenerator keyGen;
    private Random rng = new Random();

    private SecretKey userNameKey;
    private SecretKey passwordKey;

    private final Tls myTls;
    private       int authLevel;
    private final String serverKeyStoreName;
    private final String serverPasswd;
    private final char[] passphrase;
    private final String clientKeyStoreName;
    private final String clientPasswd;

    private Inet4Address thisHost;
    private IPAddress clientAddr;
    private int clientPort;
    private EndPoint clientTlsEnd;
    private final IPAddress serverAddr;
    private final int serverPort;
    private final EndPoint serverEnd;
    private final TlsAddress serverTlsAddr;

    private Acceptor  acceptor;
    private Connector connector;
    private SecretServer server;
    
    public TestSecretServer (String name)       throws Exception {
        super(name);
        keyGen = KeyGenerator.getInstance("HmacSHA1");
        try {
            thisHost = (Inet4Address)Inet4Address.getLocalHost();
        } catch (java.net.UnknownHostException uhe) {
            System.err.println("can't get local host's name!");
        }

        // TEMPORARY HACK junk.tls.init
        myTls               = new Tls();
        authLevel           = ANONYMOUS_TLS;
        serverKeyStoreName  = "test.server.private";
        serverPasswd        = "87654321";
        passphrase          = serverPasswd.toCharArray();
        clientKeyStoreName  = null;
        clientPasswd        = null;
        // END HACK

        serverPort = 7456;
        serverAddr = new IPAddress (thisHost, serverPort);
        KeyStore serverKeys = KeyStore.getInstance("JKS");
        serverKeys.load( new FileInputStream(serverKeyStoreName),
                         passphrase );
        serverTlsAddr = new TlsAddress(serverAddr,
                authLevel, serverKeys, passphrase, true);
        serverEnd  = new EndPoint (myTls, serverTlsAddr);
    }

    public void setUp ()                        throws Exception {
        userNameKey = null;
        passwordKey = null;

        clientPort = 0;       // EPHEMERAL PORT 
        clientAddr = new IPAddress (thisHost, clientPort);
        TlsAddress clientTlsAddr = new TlsAddress(clientAddr,
                authLevel, null, null, true);
        clientTlsEnd  = new EndPoint (myTls, clientTlsAddr);
        connector  = myTls.getConnector(serverTlsAddr, true);
    }
    public void tearDown()                      throws Exception {
        if (server != null && server.isRunning()) 
            try { server.close(); } catch (Throwable t) {}  // blocks
        server = null;
    }

    public void startServer()                   throws Exception {
        userNameKey = keyGen.generateKey();
        passwordKey = keyGen.generateKey();
        server      = new SecretServer(thisHost, serverPort,
                        serverKeyStoreName, serverPasswd,
                        userNameKey, passwordKey, false);
        Thread.sleep(1);
        assertTrue (server.isRunning());
    }
    public void checkProtocol()                 throws Exception {
        Connection knx    = connector.connect(clientTlsEnd, true);
        OutputStream outs = knx.getOutputStream();
        InputStream  ins  = knx.getInputStream();
        byte[] trxID;
        SharedSecretRequest req = new SharedSecretRequest();
        int reqLen = req.wireLength();
        byte[] outBuffer = new byte[reqLen];
        req.encode(outBuffer);
        outs.write(outBuffer, 0, reqLen);
        byte[] inBuf = new byte[256];
        int count = ins.read(inBuf);
        StunMsg msg = StunMsg.decode(inBuf);
        assertEquals (2,        msg.size());
        
        StunAttr attr0 = msg.get(0);
        assertEquals(USERNAME,    attr0.type);
        byte[] userName = attr0.value;
        assertTrue( UserName.verify(userNameKey, userName, 0) );
        
        StunAttr attr1 = msg.get(1);
        assertEquals(PASSWORD,     attr1.type);
        byte[] password = attr1.value;
        // the password is HMAC-SHA1 of the username
        assertTrue( Password.verify(passwordKey, userName, password) );
        
    }
    public void testTlsServer()                 throws Exception {
        startServer();
        checkProtocol();
    }
}
