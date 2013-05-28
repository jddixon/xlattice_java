/* TestTimeServer.java */
package org.xlattice.transport.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import junit.framework.*;

import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.IOScheduler;
import org.xlattice.transport.SchedulableConnection;
import org.xlattice.transport.TimeSListener;
import org.xlattice.transport.mockery.MockSchConnection;

/**
 * @author Jim Dixon
 */

public class TestTimeServer extends TestCase {

    public static final String TIME_LOG = "junk.time.log";
    public static final int DEFAULT_SERVER_PORT = 60001;
    
    IOScheduler scheduler;
        
    static InetAddress host;
    static {
        try {
            host = InetAddress.getLocalHost();
        } catch (java.net.UnknownHostException uhe) {
            System.out.println("you have a problem: " + uhe);
        }
    }
    
    SocketChannel sChan;
    
    private InetSocketAddress nearEnd;
    private int nearPort;
    private Socket nearSock;
    
    private InetSocketAddress farEnd;
    private int farPort;
    
    private InetSocketAddress server;
    private int serverPort;
    private ServerSocket serverSock;
    
    ConnectionListener listener;
    SchedulableConnection cnx;

    public TestTimeServer (String name) {
        super(name);
    }

    private void makeServerSocket (int port)       throws Exception {
        serverSock = new ServerSocket();
        try {
            serverSock.setReuseAddress(true);
        } catch (java.net.SocketException se) {
            System.out.println("error setting SO_REUSEADDR on");
        }
        serverPort = DEFAULT_SERVER_PORT;
        boolean gotOne = false;
        while (!gotOne && serverPort < DEFAULT_SERVER_PORT + 16) {
            try {
                server = new InetSocketAddress(host, serverPort);
                serverSock.bind(server);
                gotOne = true;
            } catch (IOException ioe) {
                System.out.println("problem binding server socket to port " 
                        + (serverPort++));
            }
        }
    }
    private void makeSocketChannel()            throws Exception {
        
        sChan = SocketChannel.open();
        nearSock = sChan.socket();
        try {
            nearSock.setReuseAddress(true);
        } catch (java.net.SocketException se) {
            System.out.println("error setting SO_REUSEADDR on");
        }
        nearEnd = new InetSocketAddress(host, DEFAULT_SERVER_PORT - 1);
        try {
            nearSock.bind (nearEnd);
        } catch (IOException ioe) {
            System.out.println("couldn't bind local end");
        }
        if (!nearSock.isBound())
            System.out.println("* near end still not bound* ");
        nearPort = nearSock.getLocalPort();
        try {
            sChan.connect(server);
        } catch (java.net.ConnectException ce) {
            System.out.println("Connection failed: " + ce);
        }

        if (sChan.isOpen()) {
            farPort = nearSock.getPort();
        }
    }
    public void setUp () {
        sChan     = null;
        scheduler = null;
        listener  = null;
        cnx       = null;
    }
    public void tearDown () {
        try {
            nearSock.close();
            scheduler.close();
            Thread.currentThread().sleep(2);    // let the scheduler die
            serverSock.close();
        } catch (Exception e) { /* ignored */ }
    }

    public void testConstructors()              throws Exception {
        makeServerSocket(DEFAULT_SERVER_PORT);
        makeSocketChannel();
        assertNotNull(sChan);
        assertNotNull(sChan.socket());

        scheduler = new IOScheduler();
        listener  = new TimeSListener(TIME_LOG);
        cnx       = new MockSchConnection (sChan, scheduler, listener);

        ByteBuffer buffer = ByteBuffer.allocate(512);
        String msg = "abcdef";
        buffer.put(msg.getBytes());
        buffer.flip();

        ((MockSchConnection)cnx).fakeDataIn(buffer);
        cnx.readyToRead();
        cnx.readyToWrite();

        ByteBuffer results = ((MockSchConnection)cnx).getResults();
        assertNotNull(results);
        byte[] contents = results.array();
        String data = new String(contents, 0, results.limit());
        assertTrue (data.endsWith("abcdef\r\n"));
    }
}
