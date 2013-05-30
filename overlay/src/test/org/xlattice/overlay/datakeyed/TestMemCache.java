/* TestMemCache.java */
package org.xlattice.overlay.datakeyed;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.NodeID;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.GetBack;        /* test class */
import org.xlattice.overlay.PutBack;        /* test class */
import org.xlattice.transport.SchedulableConnection;
import org.xlattice.transport.mockery.*;
import org.xlattice.util.NonBlockingLog;

public class TestMemCache extends TestCase {

    private Random rng = new Random ();
    private SHA1Digest sha1;
    private MemCache memCache = MemCache.getInstance();

    protected final static NonBlockingLog debugLog
                = NonBlockingLog.getInstance("debug.log");
    
    // TEST DATA ////////////////////////////////////////////////////
    private static final int FAKE_ARRAY_SIZE = 17;
    byte[][] keyValue;
    NodeID[] id  ;
    byte[][] data;
    long     bytesWritten;

    // CONSTRUCTOR //////////////////////////////////////////////////
    public TestMemCache (String name) {
        super(name);
    }

    public void setUp ()                        throws Exception {
        sha1 = new SHA1Digest();
    }

    public void testEmptyCache()                throws Exception {
        assertNotNull(memCache);
        assertEquals(0,  memCache.itemCount());
        assertEquals(0L, memCache.byteCount());
    }
    // LOGGING //////////////////////////////////////////////////////
    protected void DEBUG_MSG(String msg) {
        if (debugLog != null)
            debugLog.message("TestMemCache" + msg);
    }
    // TEST SETUP /////////////////////////////////////////
    private void makeFakes()                    throws Exception {
        keyValue = new byte[FAKE_ARRAY_SIZE][];
        id       = new NodeID [FAKE_ARRAY_SIZE];
        data     = new byte[FAKE_ARRAY_SIZE][];
        bytesWritten = 0L;

        for (int i = 0; i < FAKE_ARRAY_SIZE; i++) {
            // values are random-length byte arrays
            data[i]     = new byte[64 + rng.nextInt(128)];
            rng.nextBytes(data[i]);             // more random bits
            
            // keys are NodeIDs
            keyValue[i] = sha1.digest(data[i]);;
            id[i] = new NodeID(keyValue[i]);
        }
    }
    // END TEST SETUP /////////////////////////////////////

    private void waitUntilDone (CallBack[] cb) {
        DEBUG_MSG(".waitUntilDone");
        boolean done = false;
        while (!done) {
            try {
                Thread.currentThread().sleep(50);
            } catch (InterruptedException ie) { /* ignored */ }
            boolean oneIsntDone = false;
            for (int i = 0; i < cb.length; i++) {
                if (cb[i].getStatus() == -1) {
                    oneIsntDone = true;
                    break;
                }
            }
            if (!oneIsntDone)
                done = true;
        }
    }
    public void testDirectOps()                 throws Exception {
        DEBUG_MSG(".testDirectOps");
        assertNotNull (memCache);
        makeFakes();
        PutBack[] putBacks = new PutBack[FAKE_ARRAY_SIZE];
        for (int i = 0; i < FAKE_ARRAY_SIZE; i++) {
            DEBUG_MSG(".testDirectOps, doing put " + i);
            putBacks[i] = new PutBack();
            memCache.put (id[i], data[i], putBacks[i]);
            bytesWritten += data[i].length;
            assertEquals (i + 1,        memCache.itemCount());
            assertEquals (bytesWritten, memCache.byteCount());
        }
        waitUntilDone(putBacks);
        for (int i = 0; i < FAKE_ARRAY_SIZE; i++) {
            assertEquals(CallBack.OK, putBacks[i].status);
        }
        // read back and compare
        GetBack[] getBacks = new GetBack[FAKE_ARRAY_SIZE];
        for (int i = 0; i < FAKE_ARRAY_SIZE; i++) {
            DEBUG_MSG(".testDirectOps, doing get " + i);
            getBacks[i] = new GetBack();
            memCache.get(id[i], getBacks[i]);
        }
        DEBUG_MSG(" calling waitUntilDone on getBacks array");
        waitUntilDone(getBacks);
        for (int i = 0; i < FAKE_ARRAY_SIZE; i++) {
            assertEquals(CallBack.OK, getBacks[i].status);
            byte[] b   = getBacks[i].data;
            assertNotNull(b);
            assertEquals(data[i].length, b.length);
            for (int j = 0; j < data[i].length; j++)
                assertEquals (data[i][j], b[j]);
        }
        memCache.clear();
        assertEquals (0,  memCache.itemCount());
        assertEquals (0L, memCache.byteCount());

//      // this gets an OK because although the cache has been
//      // cleared the data is still available on disk
//      GetBack callBack = new GetBack();
//      memCache.get(id[0], callBack);
//      waitUntilDone( new GetBack[]{callBack} );
//      assertEquals(CallBack.OK, callBack.status);
//          
//      byte[] b = callBack.data;
//      assertNotNull(b);
//      assertEquals(data[0].length, b.length);
//      for (int j = 0; j < data[0].length; j++)
//          assertEquals (data[0][j], b[j]);
    }
}
