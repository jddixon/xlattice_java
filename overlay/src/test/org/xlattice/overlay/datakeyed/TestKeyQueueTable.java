/* TestKeyQueueTable.java */
package org.xlattice.overlay.datakeyed;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import junit.framework.*;
import org.xlattice.NodeID;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.GetBack;

/**
 * @author Jim Dixon
 **/

public class TestKeyQueueTable extends TestCase {

    static final int ID_LEN = NodeID.LENGTH;

    private Random rng = new Random();
    private KeyQueueTable table;
    private IMemCache memCache;
    
    // CONSTRUCTOR //////////////////////////////////////////////////
    public TestKeyQueueTable (String name) {
        super(name);
    }

    public void setUp() {
        table = null;
        memCache = MemCache.getInstance("");
    }

    private NodeID makeID () {
        byte[] key = new byte[ID_LEN];
        rng.nextBytes(key);
        return new NodeID (key);
    }
    public void testConstructor ()              throws Exception {
        table = new KeyQueueTable(memCache);
        assertEquals (0, table.size());
        GetBack cb = new GetBack();
        assertEquals (-1, cb.status);
        NodeID id = makeID();
        assertNotNull(table.enqueue (id, cb)); // creates new queue
        assertEquals (1, table.size());
    }
    public void testWithManyMembers()           throws Exception {
        table = new KeyQueueTable(memCache);
        // from 16 to 64 key queues
        final int TABLE_SIZE = 16 + rng.nextInt(49);
        int[] queueLen = new int[TABLE_SIZE];
        NodeID[] id   = new NodeID[TABLE_SIZE];
        byte[][] data = new byte[TABLE_SIZE][];
        int entryCount = 0;
        for (int i = 0; i < TABLE_SIZE; i++) {
            // from 8 to 32 callbacks in the queue
            queueLen[i] = 8 + rng.nextInt(25);
            entryCount += queueLen[i];
            id[i]   = makeID();
            // from 16 to 512 bytes in the byte array
            data[i] = new byte[ 16 + rng.nextInt(487) ];
            rng.nextBytes(data[i]);
        }
        final int ENTRY_COUNT = entryCount;
        GetBack[] cb = new GetBack[ENTRY_COUNT];
        int k = 0;
        for (int i = 0; i < TABLE_SIZE; i++) {
            int qLen = queueLen[i];
            for (int j = 0; j < qLen; j++, k++) {
                cb[k] = new GetBack();
                if (j == 0)
                    assertNotNull ( table.enqueue(id[i], cb[k]) );
                else
                    assertNull ( table.enqueue(id[i], cb[k]) );
            }
        }
        assertEquals(ENTRY_COUNT, k);
        assertEquals(TABLE_SIZE, table.size());
        KeyQueue[] keyQs = new KeyQueue[TABLE_SIZE];
        k = 0;
        for (int i = 0; i < TABLE_SIZE; i++) {
            keyQs[i] = table.remove(id[i]);
            assertEquals(queueLen[i], keyQs[i].size());
            for (int j = 0; j < queueLen[i]; j++, k++)
                assertEquals(-1, cb[k].status);
            k -= queueLen[i];
            keyQs[i].finishedGet (CallBack.OK, data[i]);
            for (int j = 0; j < queueLen[i]; j++, k++) {
                assertEquals(CallBack.OK, cb[k].status);
                byte[] dataBack = cb[k].data;
                assertEquals( data[i].length, dataBack.length );
            }
            // XXX could check every byte ...
        }
    }
}
