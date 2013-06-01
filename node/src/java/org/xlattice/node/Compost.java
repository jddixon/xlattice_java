/* Compost.java */
package org.xlattice.node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jim Dixon
 */

import org.xlattice.CryptoException;
import org.xlattice.NodeID;
import org.xlattice.crypto.SHA1Digest;

/**
 * XXX NEED TO REVIEW THIS FOR MULTI-THREADING XXX
 */
public class Compost {

    public  static final int LENGTH = 1024;
    private static final Map heaps = new HashMap();
    
    private final SecureRandom rng;
    private final byte[] data;

    private final String myFileName;
    
    // CONSTRUCTORS AND SUCH ////////////////////////////////////////
    private Compost (String path)   throws CryptoException, IOException {
        SecureRandom _rng = null;
        try {
            _rng = SecureRandom.getInstance("SHA1PRNG");
        } catch (java.security.NoSuchAlgorithmException nsae) {
            throw new CryptoException("missing crypto library? - " 
                    + nsae);
        }
        rng = _rng;
        myFileName = path;
        File file  = new File(myFileName);
        data       = new byte[LENGTH];
        
        boolean initializing = false;
        if (file.exists()) {
            if (file.isDirectory())
                throw new IllegalArgumentException("file is a directory");
            if (file.length() == LENGTH) {
                FileInputStream fins = null;
                try {
                    fins = new FileInputStream(file);
                    int count = fins.read(data);
                    if (count != LENGTH)
                        initializing = true;
                } catch (IOException ioe) {
                    initializing = true;
                } finally {
                    if (fins != null)
                        try {
                            fins.close();
                        } catch (IOException ioe) { /* ignored */ }
                }
            } else {
                // length wrong; reinitialize
                initializing = true;
            }
        } else {
            // no such file
            initializing = true;  
        }
        if (initializing) {
            // may be done by implementation, as in java.util.Random
            rng.setSeed(System.currentTimeMillis());
            rng.nextBytes(data);
            store();
        }
    }
    public static Compost getInstance(String name) 
                                throws CryptoException, IOException {
        if (name == null || name.equals(""))
            throw new IllegalArgumentException("null or empty file name");
        Compost heap = (Compost)heaps.get(name);
        if (heap == null) 
            heap = new Compost(name);
        return heap;
    }
    public static Compost getInstance() 
                                throws CryptoException, IOException {
        return getInstance("compost.heap");
    }
    // IMPLEMENTATION ///////////////////////////////////////////////
    /**
     * Get a quasi-random offset into the data array seen as int[].
     *
     * Adding 128 converts a signed byte into a non-negative value
     * in the range 0..255.  
     */
    private final int getOffset () {
        return 128 + data[ 128 + data[128 + data[0]]];
    }
    private final void store ()                 throws IOException {
        if (data == null || data.length != LENGTH)
            throw new IllegalStateException(
                    "data array null or of the wrong length");
        File file = new File(myFileName);
        FileOutputStream fouts = new FileOutputStream (file);
        fouts.write(data);
        fouts.flush();
        fouts.close();
    }
    /**
     * Add some quasi-random data to the compost heap.  
     * 
     * @param payment array of byte arrays
     * @throws CryptoException if the library is not installed
     * @throws IllegalArgumentException if no payment is provided
     * @throws IOException if the compost heap cannot be written to disk
     */
    private final void turnItOver(byte[][] payment)
                                throws CryptoException, IOException {
        if (payment == null || payment.length == 0)
            throw new IllegalArgumentException (
                    "no data sources specified");
        
        SHA1Digest sha1 = new SHA1Digest();
        // offset into the data array on an int span
        int offset = getOffset() * 4;
        sha1.update(data, offset, LENGTH - offset);
        for (int i = 0; i < payment.length; i++) {
            if (payment[i] == null || payment[i].length == 0)
                throw new IllegalArgumentException ("data source "
                        + i + " is null or zero-length");
            sha1.update(payment[i]);
        }
        byte[] hash = sha1.digest();
        // circular rotation of the data array by 7 bytes, working
        // in the hash
        for (int i = 0; i < LENGTH; i++)
            data[i] = (byte)(hash[i % 20] ^ data[(i + 7) % LENGTH]);
        store();                    
    }
    // ACCESS METHODS ///////////////////////////////////////////////
    /**
     * Calculates a random 160-bit value by XORing data from the
     * compost heap with output from Java's crypto-quality random
     * number generator.  The 'payment' supplied should be a number
     * of sources of more or less random data such as the time of 
     * day in ms, an IP address, and so forth.
     *
     * @param payment a vector of byte arrays 
     * @return a 160-bit value wrapped as a NodeID
     *
     */
    public NodeID getNodeID (byte[][] payment)  
                                throws CryptoException, IOException {
        turnItOver(payment);            // causes a store()
        int offset = 4 * getOffset();
        return _getNodeID(offset);
    }
    private NodeID _getNodeID(int offset) {
        byte[] value = new byte[NodeID.LENGTH];
        rng.nextBytes(value);
        for (int i = 0; i < value.length; i++)
            value[i] ^= data[(i + offset) % LENGTH];
        return new NodeID(value);
    }
        
    public NodeID[] getNodeID (byte[][] payment, int n)
                                throws CryptoException, IOException {
        if (n < 1 || n > payment.length)
            throw new IllegalArgumentException(
                "non-positive or greater than number of data sources: "
                    + n);
        turnItOver(payment);
        NodeID[] ids = new NodeID[n];
        int offset = 4 * getOffset();
        for (int i = 0; i < n; i++) {
            ids[i] = _getNodeID(offset);
            offset = (offset) + 4 % LENGTH;
        }
        return ids;
    }
}
