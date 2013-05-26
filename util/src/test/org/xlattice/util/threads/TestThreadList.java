/* TestThreadList.java */
package org.xlattice.util.threads;

import java.util.EmptyStackException;
import junit.framework.*;

/**
 * Tests on ThreadList objects.  
 *
 * @author Jim Dixon
 */
public class TestThreadList extends TestCase {

    private ThreadList tl;
    private int zombieID;
    
    public TestThreadList (String name) {
        super(name);
    }

    /** A collection of killables. */
    public class Zombie implements Killable {
        final int id;
        boolean dying;
        public Zombie () {
            id = zombieID++;
            dying = false;
        }
        public void die() {
            dying = true;
        }
    }
    public void setUp () {
        zombieID = 0;
    }
    /** 
     * A mindless set of little tests that also sets up a fixture,
     * a list of so-many killables, and marks them all busy.  
     *
     * @param tList    the list to be populated
     * @param capacity the number of zombies to put in the list
     */
    public void fillerUp( ThreadList tList, int capacity) {
        assertEquals (0, tList.size());
        assertTrue ("idle threads in an empty list!", tList.allIdle());
        assertEquals (capacity, tList.capacity());
        for (int i = 0; i < capacity; i++) {
            Zombie z = new Zombie();
            tList.add(z);
            assertTrue("new zombie " + i + " is dying!", !z.dying);
        }
        // set all busy 
        assertEquals (tList.capacity(), tList.size());
        for (int i = 0; i < capacity; i++) {
            tList.startJob();
        }
        assertTrue ("aren't all busy", tList.allBusy());
        // all threads are busy
        try {
            tList.startJob();
            fail ("expected too-many-busy exception");
        } catch (IllegalStateException iob) {
            // ignore it
        }
    }
    /**
     * Empty the list, which is expected to be full.  Expect all the
     * busy flags to be set; set them all to idle.  XXX A shortcoming:
     * the busy count can be greater than the list size (but not the
     * list capacity).
     */
    public void carryOutTheDead (ThreadList tList) {
        int capacity = tList.capacity();
        assertEquals (tList.size(), capacity);
        tList.die();
        for (int i = capacity; i > 0; i --) {
            tList.endJob();
            assertFalse(
                    "expected some idle zombies (capacity is " 
                        + capacity + " and " + (capacity - i + 1)
                        + " has/have been killed)", 
                    tList.allBusy());
            Zombie z = (Zombie)tList.pop();
            assertTrue("new zombie " + i + " isn't dying!", z.dying);
        }
        assertEquals(0, tList.size());
        // no threads should be busy
        try {
            tl.endJob();
            fail ("expected none-busy exception");
        } catch (IllegalStateException iob) {
            // ignore it
        }
    }
    // ACTUAL TESTS /////////////////////////////////////////////////
    public void testShortList() {
        tl  = new ThreadList (ThreadList.MIN_CAPACITY - 2);
        assertEquals( ThreadList.MIN_CAPACITY, tl.capacity() );
        fillerUp(tl, ThreadList.MIN_CAPACITY);
        carryOutTheDead(tl);
    } 
    public void testDefaultSizeList() {
        tl  = new ThreadList (ThreadList.MIN_CAPACITY);
        assertEquals( ThreadList.MIN_CAPACITY, tl.capacity() );
        fillerUp(tl, ThreadList.MIN_CAPACITY);
        carryOutTheDead(tl);
    } 
    public void testListOverMin() {
        tl  = new ThreadList (ThreadList.MIN_CAPACITY + 2);
        assertEquals( ThreadList.MIN_CAPACITY + 2, tl.capacity() );
        fillerUp(tl, ThreadList.MIN_CAPACITY + 2);
        carryOutTheDead(tl);
    } 
}
