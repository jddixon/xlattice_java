/* TestSchUdpPort.java */
package org.xlattice.transport.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.*;

import org.xlattice.*;
import org.xlattice.transport.*;
import org.xlattice.transport.mockery.MockPacketPortListener;
import org.xlattice.util.NonBlockingLog;

/**
 * XXX ASAP rip an AbstractTestPackerPort out of this.
 * 
 * @author Jim Dixon
 */
public class TestSchUdpPort extends TestCase {

    public final static byte PING = PingPongPkt.PING;
    public final static byte PONG = PingPongPkt.PONG;

    protected     Random      rng;
    
    private       IOScheduler scheduler;
    private       IPAddress   localAddr;
    private final InetAddress localHost;
    
    private       SchUdpPort  udpPort1;
    private       int         port1;
    private       IPAddress   addr1;
    
    private       SchUdpPort  udpPort2;
    private       int         port2;
    private       IPAddress   addr2;

    private       SchUdpPort[] udpPorts;
    private       IPAddress[]  addrs;
    private       PingPonger[] pingPongers;
    private       Thread[]     ppThreads;


    // DEBUG LOGGING //////////////////////////////////////
    protected final NonBlockingLog log  
                        = NonBlockingLog.getInstance("TestSchUdpPort.log");
    protected String PROMPT = "";
    protected void setUpLogging(String s) {
        PROMPT = s + ": ";
    }
    protected void LOG_MSG (String s) {
        log.message(PROMPT + s);
    }
    // END DEBUG
    public TestSchUdpPort (String name)            throws Exception {
        super(name);
        localHost = InetAddress.getLocalHost(); 
        rng       = new Random();
    }

    public void setUp ()                        throws Exception {
        scheduler  = null;
    }
    public void tearDown()                      throws Exception {
        if (udpPort1 != null && !udpPort1.isClosed())
            udpPort1.close();
        if (udpPort2 != null && !udpPort2.isClosed())
            udpPort2.close();

        if (scheduler != null && scheduler.isRunning()) {
            scheduler.close();
        }
        pingPongers = null;

        ppThreads = null;
    }
    public void testConstructor()               throws Exception {
        // Note that if localHost is null, it is construed as the 
        // wildcard address, 0.0.0.0/0
        try {
            udpPort1 = new SchUdpPort (localHost, -1);
            fail("UdpPort constructor accepted negative port number");
        } catch (IllegalArgumentException iae) { /* ok */ }
        
        udpPort1 = new SchUdpPort (localHost, 0);
        int portAssigned = udpPort1.getLocalPort();
        assertFalse ( portAssigned == 0 );
        udpPort1.close();
        assertTrue (udpPort1.isClosed());

        // XXX THIS TEST WILL OCCASIONALLY FAIL, because port 33333
        // will be in use.  MARK??
        udpPort1 = new SchUdpPort (localHost, 33333);
        addr1 = (IPAddress)udpPort1.getNearAddress();
        assertTrue( addr1.equals( new IPAddress (localHost, 33333) ) );

        // properties /////////////////////////////////////
        assertTrue (localHost.equals( udpPort1.getLocalHost()));
        assertEquals(33333,  udpPort1.getLocalPort());
        assertEquals (0, udpPort1.getTimeout());
        // XXX MORE PLEASE
        
        // scheduler-related //////////////////////////////
        assertNull(udpPort1.getKey());

        // look at underlying NIO channel /////////////////
        DatagramChannel dChan = (DatagramChannel)udpPort1.getChannel();
        assertNotNull(dChan);
        assertFalse(dChan.isBlocking());
        assertTrue(dChan.isOpen());



        
        // tidy up ////////////////////////////////////////
        udpPort1.close();
        assertFalse(dChan.isOpen());
        
    }
    public void testPingPongPkts()              throws Exception {
        final byte PING = (byte) PingPongPkt.PING;
        int count = 1 + rng.nextInt(3);
        for (int i = 0; i < count; i++) {
            int len = 16 + rng.nextInt(8);
            byte[] buff = new byte[len];
            rng.nextBytes(buff);
            buff[0] = PING;
            buff[1] = (byte)i;          // src
            buff[2] = (byte)(i + 1);    // dest
            buff[3] = (byte)(len - 8);  // payload length
            PingPongPkt p = new PingPongPkt(buff);
            assertEquals( PING,     p.getType() );
            assertEquals( i,        p.getSrc()  );
            assertEquals( i + 1,    p.getDest() );
            assertEquals( len - 8,  p.getLen()  );
            int msgID   = p.getMsgID();
            byte[] data = p.getData();
            PingPongPkt q = new PingPongPkt(
                        PING, (byte)i, (byte)(i + 1), msgID, data);
            assertEquals( PING,     q.getType() );
            assertEquals( i,        q.getSrc()  );
            assertEquals( i + 1,    q.getDest() );
            assertEquals( len - 8,  q.getLen()  );
            assertEquals( msgID,    q.getMsgID());
            byte[] qData = q.getData();
            for (int j = 0; j < len - 8; j++)
                assertEquals ( data[j], qData[j] );
        }
    }
    /**
     * @param N number of pingpongers
     * @param K number of neighbors to each
     * @param D delta-T in ms
     * @param J ping interval in delta-Ts
     * @param Z maximum number of pings from each pingponger
     */
    public IOScheduler setUpPingPongers(final int N, final int K, 
                            final int D, final int J, final int Z) 
                                                throws Exception {
        IOScheduler sch = new IOScheduler();
        udpPorts    = new SchUdpPort[N];
        addrs       = new IPAddress[N];
        pingPongers = new PingPonger[N];
        int msgID   = 0;
        
        for (int i = 0; i < N; i++) {
            DatagramChannel chan = DatagramChannel.open();
            DatagramSocket socket = chan.socket();
            socket.bind( new InetSocketAddress(localHost, 0) );

            udpPorts[i] = new SchUdpPort( chan );
            addrs[i]    = (IPAddress)udpPorts[i].getNearAddress();
        }
        for (int i = 0; i < N; i++) {
            IPAddress[] neighbors = new IPAddress[K];
            PingPongPkt[] pkts    = new PingPongPkt[K];
            for (int k = 0; k < K; k++) {
                int dest = (i + 1 + k) % N;
                neighbors[k] = addrs[ dest ];
                int len = 16 + rng.nextInt( 128 - 16 );
                byte[] data = new byte[len];
                rng.nextBytes(data);
                pkts[k] = new PingPongPkt( PING, (byte)i, (byte)dest, 
                                           msgID++, data);
            }
            pingPongers[i] = new PingPonger(i, N, neighbors, pkts,
                                           D, J, Z);
            udpPorts[i].setListener(pingPongers[i]);
            sch.add(udpPorts[i]);
        }
        ppThreads = new Thread[N];
        for (int i = 0; i < N; i++) {
            ppThreads[i] = new Thread( pingPongers[i] );
            ppThreads[i].start();
        }
        LOG_MSG("pingPongers started");
        
        for (int i = 0; i < N; i++) {
            if ( ! pingPongers[i].isRunning() || ! ppThreads[i].isAlive() ) {
                // failures if sleep(50)
                try {Thread.currentThread().sleep(200);}
                catch (InterruptedException ie) {}
                assertTrue ( "pingPonger[" + i + "] Thread isn't alive",
                    ppThreads[i].isAlive() );
                assertTrue ( "pingPonger[" + i + "] isn't running",
                    pingPongers[i].isRunning() );
            }
        }

        return sch;
    }
    public void doTestPingPongers(final int N, final int K, 
            final int D, final int J, final int Z)
                                                throws Exception {
        scheduler = setUpPingPongers (N, K, D, J, Z);
        
        // we need to close pingPonger only after all are done
        boolean stillRunning = true;
        final int PATIENCE = 1024;
        for (int n = 0 ; stillRunning && n < PATIENCE; n++) {
            try {Thread.currentThread().sleep( 100);}
            catch (InterruptedException ie) {}
            stillRunning = false;
            for (int i = 0; i < N; i++) {
                if(!pingPongers[i].pongsIn) {
                    stillRunning = true;
                    break;
                }
            }
            LOG_MSG(n + ": pingPongers still running");
        }
        for (int i = 0; i < N; i++) {
            pingPongers[i].close();
        }
        LOG_MSG("all pingPongers have been closed"); 
        scheduler.close();
    }
    // takes about 0.5s on dev machine; N*K*Z*2 = 20 packets
    public void testMininalPingPongers()        throws Exception {
        //                 N   K   D   J   Z
        doTestPingPongers( 2,  1, 10,  7,  5);
      //doTestPingPongers( 3,  2, 10,  3,  2);
    }
    // takes about 1.2s on development machine; 2200 packets, so
    // about 2180/0.7s = 3100 packets per second
    public void testMiddlingPingPongers()       throws Exception {
        //                 N   K   D   J   Z
        doTestPingPongers(20,  5, 10,  7, 11);
    }
    // This test takes a long time
//  // 99 * 25 * 11 * 2 = 54450 messages
//  public void testManyPingPongers()           throws Exception {
//      //                 N   K   D   J   Z
//      doTestPingPongers(99, 25, 20,  7, 11);
//  }
    
//  public void testWithBlockingPorts ()        throws Exception {
//      final int count = 3 + rng.nextInt(12);      // so 3 to 14
//      UdpPort[] blocking = new UdpPort[count];
//      IPAddress[]  bAddr  = new IPAddress[count];
//      ByteBuffer[] inBuf  = new ByteBuffer[count];
//      ByteBuffer[] outBuf = new ByteBuffer[count];
//      int[] len           = new int[count];
//      for (int i = 0; i < count; i++) {
//          len[i]   = 16 + rng.nextInt(129 - 16);  // so 16 to 128
//          byte[] b = new byte[len[i]];
//          rng.nextBytes(b);
//          outBuf[i] = ByteBuffer.wrap(b);
//          inBuf[i]  = ByteBuffer.allocate(128);
//      }
//      try {
//          udpPort1  = new SchUdpPort(localHost);
//         
//          for (int i = 0; i < count; i++) {
//              blocking[i] = new UdpPort(localHost);
//              bAddr[i]    = (IPAddress) blocking[i].getNearAddress();
//          }
//     


//      // tidy up ////////////////////////////////////////
//      } finally { 
//          for (int i = 0; i < count; i++) 
//              try { blocking[i].close(); } catch (IOException ioe) {}
//          if (udpPort1 != null)
//              try { udpPort1.close(); } catch (IOException ioe) {}
//          if (udpPort2 != null)
//              try { udpPort2.close(); } catch (IOException ioe) {}
//      }
//  } // GEEP
}
