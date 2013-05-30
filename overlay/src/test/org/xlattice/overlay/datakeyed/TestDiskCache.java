/* TestDiskCache.java */
package org.xlattice.overlay.datakeyed;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.NodeID;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.GetBack;    // mock
import org.xlattice.overlay.PutBack;    // mock
import org.xlattice.util.StringLib;

public class TestDiskCache extends TestCase {

    private DiskCache cache = DiskCache.getInstance();
    private Random rng      = new Random();
    private SHA1Digest sha1;

    // CONSTRUCTOR //////////////////////////////////////////////////
    public TestDiskCache (String name) {
        super(name);
    }

    public void setUp()                         throws Exception {
        sha1 = new SHA1Digest();
    }

    public void testFilter()                    throws Exception {
        NumberFormat countFmt   = new DecimalFormat("000000");
        NumberFormat posRateFmt = new DecimalFormat("0.00000000");
       
        assertNotNull(cache);
        cache.init();
        assertEquals (0, cache.size());
        // DEBUG
        for (int i = 0; i < 500000; i += 10000)
            System.out.println(countFmt.format(i) + " " 
                             + posRateFmt.format(cache.falsePositives(i)));
        // END
        
        // for a m = 20, k = 6 filter the predicted false positive 
        // rate with n = 100,000 keys in the filter is 0.00684297
        byte[] key = new byte[20];
        for (int i = 0; i < 100000; i++) {
            rng.nextBytes(key);
            cache.insert(new NodeID(key));
        }
        int positives = 0;
        for (int i = 0; i < 100000; i++) {
            rng.nextBytes(key);
            if (cache.member(new NodeID(key)))
                positives++;
        }
        // the actual rate seen in successive runs was
        //   676, 694, 712, 672, 655, 707, 648, ...
        // so a good match to the predicted rate
        
        // DEBUG
        System.out.println(
            "100K hits against a filter with 100K members delivered "
            + positives + " false positives");
        // END
    }

    public void testThreadPool() {
        DiskIOThreadPool threadPool = cache.getThreadPool();
        assertNotNull(threadPool);
        // XXX NO LONGER PART OF INTERFACE
//      assertEquals(0,             threadPool.getThreadCount());
        assertEquals("DiskCache",   threadPool.getName());
        assertEquals(-1,            threadPool.getMaxLenJobQueue());
        assertEquals(32,            threadPool.getMaxThreads());
    }
    public void waitForCallBacks(CallBack[] cb) {
        boolean finished = false;
        while (!finished) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException ie) { /* ignore it */ }
            boolean oneStillWaiting = false;
            for (int i = 0; i < cb.length; i++) {
                if (cb[i].getStatus() == -1) {
                    oneStillWaiting = true;
                    break;
                }
            }
            if (!oneStillWaiting)
                finished = true;
        } 
    }
    /**
     * Create a random number of random-length files with random 
     * content, write them to disk, read them back, and verify 
     * that the data written and the data read match.
     */
    // XXX should rm xlattice/overlays/store/* before running this XXX
    public void testDiskIO() {
        DiskIOThreadPool threadPool = cache.getThreadPool();
        final int N   = 103 + rng.nextInt(256);
        byte[][] data = new byte[N][];
        byte[][] key  = new byte[N][];
        NodeID[] id   = new NodeID[N];

        PutBack[] putBack = new PutBack[N];
        for (int i = 0; i < N; i++) {
            int len    = 64 + rng.nextInt(128);
            data[i]    = new byte[len];
            rng.nextBytes(data[i]);
            key[i]     = sha1.digest(data[i]);
            id[i]      = new NodeID(key[i]);
            putBack[i] = new PutBack();
            cache.acceptWriteJob(id[i], data[i], putBack[i]);
        }
        waitForCallBacks(putBack);
        for (int i = 0; i < N ; i++) {
            assertEquals("callback " + i + " not OK: ",
                    CallBack.OK, putBack[i].status);
        }
        GetBack[] getBack = new GetBack[N];
        for (int i = 0; i < N; i++) {
            getBack[i] = new GetBack();
            cache.acceptReadJob(id[i], getBack[i]);
        }
        waitForCallBacks(getBack);
        for (int i = 0; i < N ; i++ ) {
            assertEquals(CallBack.OK, getBack[i].status);
            byte[] diskData = getBack[i].data;
            assertNotNull(diskData);
            assertEquals(data[i].length, diskData.length);
            for (int j = 0; j < data[i].length; j++)
                assertEquals ("data[" + i + "][" + j + "] discrepency: ",
                        data[i][j], diskData[j]);
        }
    }
}
