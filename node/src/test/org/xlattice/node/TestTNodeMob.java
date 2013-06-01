/* TestTNodeMob.java */
package org.xlattice.node;

import java.net.Inet4Address;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.*;

import org.xlattice.*;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAKeyGen;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.protocol.stun.Client;
import org.xlattice.protocol.stun.NattedAddress;
import org.xlattice.protocol.stun.StunConst;
import org.xlattice.protocol.xl.XL;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.StringLib;

/**
 * Test a small mob of TNodes operating in concert.
 * 
 * @author Jim Dixon
 */
public class TestTNodeMob extends TestCase {

    protected final Inet4Address stunServer;
    protected final Inet4Address localHost;
    protected final XL           xl;
    
    private Random rng = new Random ( new Date().getTime() );
    private RSAKeyGen keyGen;

    protected Thread          myThread;
    protected Timer           timer;
    protected TNode[]         tnodes;
    protected Port0Handler[]  p0Handlers;
    protected RSAKey[]        keys;
    protected NodeID[]        nodeIDs;
    protected RSAPublicKey[]  pubkeys;
    protected NattedAddress[] addr;
    protected NattedAddress   myAddr;
    protected int             myPort;
    
    // DEBUG LOGGING //////////////////////////////////////
    protected final NonBlockingLog log  
                        = NonBlockingLog.getInstance("TestTNodeMob.log");
    protected String PROMPT = "";
    protected void setUpLogging(String s) {
        PROMPT = s + ": ";
    }
    protected void LOG_MSG (String s) {
        log.message(PROMPT + s);
    }
    // END DEBUG
    public TestTNodeMob (String name)           throws Exception {
        super(name);
        keyGen= new RSAKeyGen();
        stunServer = (Inet4Address)Inet4Address
                                    .getByName("stun.xlattice.org");
        localHost = (Inet4Address)Inet4Address.getLocalHost();
        xl = new XL();
        
        myThread = Thread.currentThread();
    }

    public void setUp () {
    }
    public void tearDown() {
        if (timer != null)
            timer.cancel();
        
        if (tnodes != null)
            for (int i = 0; i < tnodes.length; i++)
                if (tnodes[i] != null) {
                    if (tnodes[i].isRunning()) {
                        tnodes[i].close();  // blocks
                    }
                    tnodes[i] = null;
                }
        // XXX may need some mechanism for synchronizing here
        tnodes      = null;
        p0Handlers  = null;
        keys        = null;
        nodeIDs     = null;
        pubkeys     = null;
        addr        = null;
        myAddr      = null;
    }
    protected class OneShot                     extends TimerTask {
        protected final TNode tnode;
        public OneShot (TNode t) {
            tnode = t;
        }
        public void run () {
            new Thread(tnode).start();
            int n;
            final int COUNT  = 256;
            final int PERIOD =   8;
            for( n = 0; !tnode.isRunning() && n < COUNT; n++) {
                try {
                    Thread.currentThread().sleep(PERIOD);
                } catch (InterruptedException ie) {}
            }
            // DEBUG - seen occasionally if COUNT*PERIOD <= 32 for small
            // node counts, frequently for N around 100
            if (n >= COUNT) 
                LOG_MSG("OneShot couldn't start TNode in " 
                        + (n * PERIOD) + " ms");
            // END
        }
    }
    /**
     * Create a small mobility of idle nodes, start them running,
     * test a few things, and stop the mob.
     * 
     * @param N     node count 
     * @param M     peer offset
     * @param K     peer count
     * @param D     interval between starting nodes
     * @param J     period between pings, J * D ms
     * @param Z     maximum number of pings, le 0 means infinity
     */
    public void doTestMob (final int N, final int M, final int K, 
                           final int D, final int J, final int Z)
                                                throws Exception { 
        timer   = new Timer();
        tnodes  = new TNode[N];
        keys    = new RSAKey[N];
        nodeIDs = new NodeID[N];
        pubkeys = new RSAPublicKey[N];
        addr    = new NattedAddress[N];

        // lower D should give higher timeout
        // higher N and K should give higher timeout
        int myTimeout = 100 + N * K * 10;
        final int THRESHOLD = 80;
        if (D < THRESHOLD) 
            myTimeout *= THRESHOLD / D;
        // DEBUG
        System.out.println("timeout set to " + myTimeout + " ms");
        // END
       
        // create N TNodes ////////////////////////////////
        for (int i = 0; i < N; i++) {
            keys[i]    = (RSAKey) keyGen.generate();
            pubkeys[i] = (RSAPublicKey)keys[i].getPublicKey();
            byte[] id  = new byte[NodeID.LENGTH];
            rng.nextBytes(id);
            nodeIDs[i] = new NodeID(id);
            tnodes[i] = new TNode(keys[i], nodeIDs[i], 
                    null,               // lfs
                    null,               // overlays
                    null);              // peers
            
            tnodes[i].setD(D);          // ms, fundamental time period
            tnodes[i].setJ(J);          // interval between pings, J*D
            tnodes[i].setZ(Z);          // max number of pings

            tnodes[i].setTimeout(myTimeout);  // ms

            assertEquals(D, tnodes[i].getD());
            assertEquals(J, tnodes[i].getJ());
            assertEquals(Z, tnodes[i].getZ());
            assertEquals(myTimeout, tnodes[i].getTimeout());
        }
        
        // give each a NattedAddress //////////////////////
        // The current implementation of the STUN client 
        // returns the same myAddr in successive runs, so 
        // that the test nodes occupy the same band of ports.
        // This is dodgey: there is no guarantee that all of 
        // these ports are unoccupied.
        Client stunClient 
            = new Client(stunServer, StunConst.STUN_SERVER_PORT,
                            localHost, 0, null, false, false);
        NattedAddress myAddr = stunClient.getNattedAddress();
        stunClient.close(); 
       

        myPort = myAddr.getLocalPort(); 
        int nextPort = myPort + 1;      // XXX no guarantee ports are free
        for (int i = 0; i < N; i++) {
            stunClient = new Client(stunServer, StunConst.STUN_SERVER_PORT, 
                            localHost, nextPort++, null, false, false);
            addr[i] = stunClient.getNattedAddress();
            // DEBUG
//          LOG_MSG("node " + i + ": " 
//                  + nodeIDs[i].toString().substring(0, 8) 
//                  + "...  " + addr[i]);
            // END
            tnodes[i].addOverlay( new NattedOverlay(xl, addr[i]) );
            stunClient.close();
        }
        // add K peers to each ////////////////////////////
        for (int i = 0; i < N; i++) {
            int peerStart = i + M;
            for (int j = 0; j < K; j++) {
                int peerIndex = (peerStart + j) % N;
                tnodes[i].addPeer( new RSAPeer(
                        nodeIDs[peerIndex], 
                        pubkeys[peerIndex],
                        new NattedOverlay[] {
                            new NattedOverlay(xl, addr[peerIndex])},
                        null /* no Connectors */ )
                );
            }
        }
        // start them running /////////////////////////////
        Runtime rt = Runtime.getRuntime();
        final int MAX_DRAG = 64;
        for (int i = 0; i < N; i++) {
            long freemem;
            int j;
            for (j = 0; j < MAX_DRAG 
                    && (freemem = rt.freeMemory()) < 600000L; j++)
                try{myThread.sleep(D);} catch(InterruptedException ie){}
            if (j > 0)
                LOG_MSG("delayed OneShot[" + i + "] " + j * D + " ms");
            timer.schedule( new OneShot(tnodes[i]), 
                    (long)(i * D) );
        }
        
        // check that they are running ////////////////////
//      final int START_DELAY = 2 * N * D;
//      try {
//          // Time required for the entire run should be about
//          //   D * (N + K * (Z - 1))
//          // In practice the scheduler seems to hang for long 
//          // periods.
//          Thread.currentThread().sleep (START_DELAY);
//      } catch (InterruptedException ie) { /* ignore */ }

        p0Handlers = new Port0Handler[N];
        int ticks = 0;    
        for (int i = 0; i < N; i++) {
            while (!tnodes[i].isRunning()) {
                ticks++;
                try {myThread.sleep(D);} catch(InterruptedException ie){}
            }
            // DEBUG
            long freeMem = Runtime.getRuntime().freeMemory();
            LOG_MSG("free memory before getting p0Handler[" + i + "]: " 
                    + freeMem);
            if (freeMem < 400000L) {
                System.gc();
                freeMem = Runtime.getRuntime().freeMemory();
                LOG_MSG("free memory after gc() call: " + freeMem);
            }    
            // END
            p0Handlers[i] = tnodes[i].p0Handler;
        }
        // DEBUG
        LOG_MSG("all TNodes running after " + (ticks * D) + " ms");
        // END
        //
        ticks = 0;
        // Each node will send K * Z pings and get that 
        // many pongs back, and in turn be pinged by K neighbors Z
        // times and send that many pongs back, so totals are 
        // 2*K*Z in each direction.
        final long EXPECTED_OUT = 2 * K * Z - K; // XXX fudge factor
        final long EXPECTED_IN  = 2 * K * Z - K; // XXX fudge factor
        for (int i = 0; i < N; i++) {
            LOG_MSG ("waiting for p0Handler " + i + " to finish");
            Port0Handler p = p0Handlers[i];
            while ((p.getPktsIn() < EXPECTED_IN ) 
                                || (p.getPktsOut() < EXPECTED_OUT)) {
                LOG_MSG("  p0Handler " + i + ": " 
                        + p.getPktsIn()  + " packets in, "
                        + p.getPktsOut() + " packets out");
                if (p.stopped()) {
                    System.out.println("p0Handler " + i 
                                            + ": all pingers stopped");
                    break;
                }
                ticks++;
                try {myThread.sleep(J*D);} catch(InterruptedException ie){}
            }
        }
        // DEBUG
        LOG_MSG(
//              "paused for " + PAUSE + " ms; then " +
                "pinging complete after " 
                            + (/*PAUSE +*/ ticks * J * D) + " ms");
        // END
        
        // simple properties checks ///////////////////////
        if (K > 3)
            for (int i = 0; i < N; i++) {
                TNode t = tnodes[i];
                assertEquals( K, t.sizePeers() );
                // verify that what this node thinks is peer 3's address
                // is what that node has as an address
                RSAPeer peer3 = (RSAPeer) t.getPeer(3);
                NattedAddress peer3Addr 
                        = (NattedAddress)peer3.getOverlay(0).address();
                int peer3Index  = (i + M + 3) % N;
                TNode peer3Node = tnodes[peer3Index];
                NattedAddress peer3NodeAddr 
                    = (NattedAddress)peer3Node.getOverlay(0).address();
                assertTrue( peer3Addr.equals(peer3NodeAddr) );
            }
        // stop them //////////////////////////////////////
        LOG_MSG("stopping all TNodes");
        for (int i = 0; i < N; i++) 
            tnodes[i].close();
        LOG_MSG("all TNodes have stopped");

        // verify that they are stopped ///////////////////
        for (int i = 0; i < N; i++)
            assertFalse( "node " + i + " is still running",
                                    tnodes[i].isRunning());
        
        String header1 = "packet counts";
        String header2 = "node --in-- --out--";
        System.out.println(header1 + "\n" + header2);
        LOG_MSG(header1);
        LOG_MSG(header2);
        for (int i = 0; i < N; i++) {
            Port0Handler p = p0Handlers[i];
            StringBuffer sb = new StringBuffer();
            sb.append("  ")
              .append( i  )
              .append("     ")
              .append(p.getPktsIn())
              .append("     ")
              .append(p.getPktsOut());
            String line = sb.toString();
            LOG_MSG(line);
            System.out.println(line);
        }
    }

    public void testTinyMob ()                  throws Exception {
        setUpLogging("tiny");
        LOG_MSG("entering");
        //          N  M  K   D  J  Z
        doTestMob ( 2, 1, 1, 10, 5, 3);
    } 
    public void testSmallMob ()                 throws Exception {
        setUpLogging("small");
        LOG_MSG("entering");
        //          N  M  K   D  J  Z
        doTestMob ( 3, 1, 2, 10, 5, 3);
    }
    // On the development machine, this is sensitive to both N and 
    // K.  It succeeds up to N=6, K=4.  Above that it generally hangs,
    // apparently due to lack of memory.
    // 
    //  6 * 4 * 20 = 480 pings, 960 messages in all.
    //  Ping interval is D*J = 100 ms, Z pings should take 2s.
    //  Runtime reported by JUnit, which includes setup, etc, is about
    //  5s.  Doubling D increased JUnit reported time to 6.9s for
    //  Z = 20.  For Z = 200, JUnit-reported time is 9.5s; however,
    //  highest reported ping count is 25, suggesting that the nodes
    //  are being closed prematurely.
    public void testMiddlingMob ()              throws Exception {
        setUpLogging("middling");
        LOG_MSG("entering");
        //          N  M  K   D  J  Z
        doTestMob ( 6, 1, 4, 40, 5, 20);
    } 

//  // 99 * 17 * 13 pings, same number of pongs, 43758 messages
//  // time required approximately 3520 + 760 = 4280ms, so over 
//  // 10,000 packets per second
//  // 
//  // After much fiddling with the code, now gets a JVM out of memory
//  // error.
//  public void testLargerMob ()                throws Exception {
//      setUpLogging("larger");
//      LOG_MSG("entering");
//      //           N  M   K   D   J   Z
//      doTestMob ( 99, 7, 17, 40, 19, 13);
//  } 
}
