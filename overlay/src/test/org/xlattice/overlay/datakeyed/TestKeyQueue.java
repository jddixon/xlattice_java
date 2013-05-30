/* TestKeyQueue.java */
package org.xlattice.overlay.datakeyed;

import java.util.Random;
import junit.framework.*;
import org.xlattice.NodeID;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.GetBack;

/**
 * @author Jim Dixon
 **/

public class TestKeyQueue extends TestCase {

    static final int ID_LEN = NodeID.LENGTH;

    private Random rng = new Random();
    private IMemCache memCache;
    
    private byte[] key;
    private NodeID id;
    private KeyQueue keyQ;
    
    // CONSTRUCTOR //////////////////////////////////////////////////
    public TestKeyQueue (String name) {
        super(name);
    }

    public void setUp() {
        key      = new byte[ID_LEN];
        rng.nextBytes(key);
        id       = new NodeID(key);
        keyQ     = null;
        memCache = MemCache.getInstance("");
    }

    public void testConstructor ()              throws Exception {
        final int LEN = 13;
        GetBack cb = new GetBack();
        assertEquals(-1, cb.status);
        keyQ = new KeyQueue (memCache, id, cb);
        assertEquals (1, keyQ.size());
        assertTrue   (id.equals(keyQ.getNodeID()));

        byte[] data = new byte[LEN];
        rng.nextBytes(data);
        keyQ.finishedGet(CallBack.OK, data);
        assertEquals (0, keyQ.size());
        assertEquals(CallBack.OK, cb.status);
        byte[] cbData = cb.data;
        assertEquals(LEN, cbData.length);
        for (int i = 0; i < LEN; i++) 
            assertEquals(data[i], cbData[i]);
    }
    public void testWithManyMembers()           throws Exception {
        final int N   = 10 + rng.nextInt(64);
        final int LEN = 32;
        GetBack[] cb = new GetBack[N];
        for (int i = 0; i < N; i++)
            cb[i] = new GetBack();
        keyQ = new KeyQueue (memCache, id, cb[0]);

        byte[]data = new byte[LEN + rng.nextInt(128)];
        rng.nextBytes(data);
        
        // we have already added cb[0]
        for (int i = 1; i < N; i++) {
            keyQ.add (cb[i]);
        }
        assertEquals(N, keyQ.size());
        keyQ.finishedGet(CallBack.OK, data);
        assertEquals(0, keyQ.size());
        for (int i = 0; i < N; i++) {
            assertEquals (CallBack.OK, cb[i].status);
            assertEquals (data.length, cb[i].data.length);
            for (int j = 0; j < data.length; j++)
                assertEquals(
                        data[j], cb[i].data[j]);
        }
    }
}
