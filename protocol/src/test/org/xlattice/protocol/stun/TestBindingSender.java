/* TestBindingSender.java */
package org.xlattice.protocol.stun;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import junit.framework.*;

import org.xlattice.Acceptor;
import org.xlattice.Connection;
import org.xlattice.EndPoint;
import org.xlattice.Protocol;
import org.xlattice.Transport;
import org.xlattice.transport.IPAddress;
import org.xlattice.transport.tcp.Tcp;

import org.xlattice.util.StringLib;

/**
 * @author Jim Dixon
 */

public class TestBindingSender extends TestCase implements StunConst {

    private Random rng = new Random();

    private final Tcp myTcp = new Tcp();
    ;
    private InetAddress thisHost;
    private Inet4Address clientAddr;
    private Inet4Address senderAddr;
    private int clientPort;
    private int senderPort;
    private EndPoint clientEnd;
    private EndPoint senderEnd;

    private Inet4Address catcherAddr;
    private int          catcherPort;

    private BindingSender sender;

    public TestBindingSender (String name)       throws Exception {
        super(name);
        try {
            thisHost = InetAddress.getLocalHost();
        } catch (java.net.UnknownHostException uhe) {
            System.err.println("can't get local host's name!");
        }
        senderAddr = (Inet4Address) thisHost;
        senderPort = 7456;
        // senderEnd  = new EndPoint (myTcp, senderAddr);
    }

    public void setUp ()                        throws Exception {
    }
    public void tearDown()                      throws Exception {
        if (sender != null && sender.isRunning()) {
            try {
                sender.close();         // does a join
            } catch (Throwable t) { /* ignore */ }
        }
        sender = null;
    }
    public void testMessageID() {
        int COUNT = 16;
        Hashtable messages = new Hashtable(COUNT);
        for (int i = 0; i < COUNT; i++) {
            StunMsg msg = new BindingRequest();
            byte[]  msgID = msg.getMsgID();
            MessageID key1 = new MessageID(msgID);
            MessageID key2 = new MessageID(msgID);
            messages.put ( key1, msg);
            assertTrue( key1.equals(key2) );
        }
        Set set = messages.keySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            MessageID key = (MessageID)it.next();
            byte[] id = key.id;
            MessageID keyCopy = new MessageID(id);
            assertTrue (key.equals(keyCopy));
            
            assertEquals(MSG_ID_LENGTH, id.length);
            assertNotNull( messages.get(key) );
            assertNotNull( messages.get(keyCopy ) );
        }
    }
    /////////////////////////////////////////////////////////////////
    public void testAFew()                      throws Exception {
        int count = 3 + rng.nextInt(6);     // so up to 8
        Outgoing[] outs = new Outgoing[count];
        catcherAddr = (Inet4Address) thisHost;
        catcherPort = 9999;
        for (int i = 0; i < count; i++) {
            StunMsg msg  = new BindingRequest();
            byte[] out = new byte[ msg.wireLength() ];
            msg.encode(out);
            outs[i] = new Outgoing(catcherAddr, catcherPort, out);
        }
        // CATCHER ////////////////////////////////////////
        Catcher catcher = new Catcher(catcherAddr, catcherPort, count);
        while (!catcher.running) 
            Thread.sleep(1);
        assertTrue(catcher.running);

        // SENDER /////////////////////////////////////////
        sender = new BindingSender(senderAddr, senderPort);
        while (!sender.isRunning())
            Thread.sleep(1);
        assertTrue(sender.isRunning());

        for (int i = 0; i < count; i++) 
            sender.schedule(outs[i]);

        for (int i = 0; i < 20; i++) 
            if (catcher.size() < count) {
                Thread.sleep(1);
            }
       
        for (int i = 0; i < count; i++) {
            StunMsg decoded = StunMsg.decode(outs[i].msg);
            byte[] msgID = decoded.getMsgID();
            assertEquals(MSG_ID_LENGTH, msgID.length);
            BindingRequest myMsg = (BindingRequest)catcher.get(msgID);
            assertNotNull(myMsg);
            for (int j = 0; j < 16; j++) {
                assertEquals(msgID[j], myMsg.getMsgID()[j]);
            }
        }
    }
}
