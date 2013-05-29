/* TestKBucket.java */
package org.xlattice.protocol.xlkad;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

public class TestKBucket extends TestCase {

    private KBucket bucket;
    
    public TestKBucket (String name) {
        super(name);
    }

    public void setUp () {
        bucket = null;
    }

    public void testConstructors()              throws Exception {
    }

    // test the static function that tells you what bucket a node
    //   belongs in

    // verify that if you add M buckets they are there
    
    // verify that if the bucket is not empty and you 'touch' a 
    //   new Contact it is added at the end of the list

    // verify that if the bucket is full and you touch a new Contact
    //   it is NOT added
    
    // verify that if you touch an existing contact it is moved to 
    //   to the end of the list
}
