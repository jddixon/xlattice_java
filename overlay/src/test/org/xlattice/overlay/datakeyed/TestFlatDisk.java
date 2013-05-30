/* TestFlatDisk.java */
package org.xlattice.overlay.datakeyed;

import java.util.Random;

/**
 * @author Jim Dixon
 **/

import junit.framework.*;

import org.xlattice.NodeID;
import org.xlattice.crypto.SHA1Digest;

public class TestFlatDisk extends AbstractDiskTest {

    // CONSTRUCTOR //////////////////////////////////////////////////
    public TestFlatDisk (String name) {
        super(name);
    }

    /**
     * Must set itemCount and create an AbstractDisk, assigning
     * it to disk.
     */
    public void _setUp() {
        itemCount = 517;            // number of files to create
        disk = new FlatDisk();
    }
}
