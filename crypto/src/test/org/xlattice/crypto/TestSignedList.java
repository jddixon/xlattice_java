/* TestSignedList.java */
package org.xlattice.crypto;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.xlattice.CryptoException;
import org.xlattice.DigSigner;
import org.xlattice.SigVerifier;
// moved to xlattice.overlay, don't want it in this earlier module
//import org.xlattice.overlay.builds.BuildList;
import org.xlattice.util.Base64Coder;

public class TestSignedList extends TestCase {

    private SignedList myList;
    private RSAKeyGen keyGen;
    private RSAKey key;
    private RSAPublicKey pubKey;

    private class MySignedList extends SignedList {

        MySignedList (RSAPublicKey publicKey, String title)
                                            throws CryptoException {
            super(publicKey, title);
        }
        MySignedList (Reader reader)
                                throws CryptoException, IOException {
            super(reader);
        }
        public void readContents (BufferedReader in)
                                throws CryptoException, IOException {
            String line = in.readLine();
            if (!line.equals("# END CONTENT #"))
                throw new CryptoException ("missing END CONTENT line");
        }

        public int size () {
            return 0;
        }
        /** Should never be called. */
        public String toString(int n) {
            return "";
        }
    }
    public TestSignedList (String name)     throws CryptoException {
        super(name);
        keyGen= new RSAKeyGen();

    }

    public void setUp () {
        myList = null;
    }

    /**
     * Generate a few random RSA keys, create MyLists, test.
     */
    public void testGeneratedSignedList()       throws Exception {
        for (int i = 0; i < 8; i++) {
            // create keys
            key = (RSAKey) keyGen.generate();
            assertNotNull(key);
            pubKey = (RSAPublicKey) key.getPublicKey();
            assertNotNull(pubKey);
            // create and test signed list
            myList = new MySignedList (pubKey, "document 1");
            assertNotNull(myList);
            myList.sign(key);
            assertTrue(myList.isSigned());
            assertTrue (myList.verify());

            // Generate a new SignedList from the serialization of the
            // current one, use it to test Reader constructor.
            String myDoc = myList.toString();
            MySignedList myList2 = new MySignedList(new StringReader(myDoc));
            assertTrue(myList2.isSigned());
            assertEquals (myDoc, myList2.toString());
            assertTrue(myList2.verify());
        }
    }
    private void checkSameHash(byte[] h1, byte[] h2) {
        assertEquals(20, h1.length);
        assertEquals(h1.length, h2.length);
        for (int i = 0; i < 20; i++)
            assertEquals(h1[i], h2[i]);
    }
    private boolean notSameHash(byte[] h1, byte[] h2) {
        assertEquals(20, h1.length);
        assertEquals(h1.length, h2.length);
        for (int i = 0; i < 20; i++)
            if (h1[i] != h2[i])
                return true;
        return false;
    }
    public void testListHash()                  throws Exception {
        RSAKey       key2;
        RSAPublicKey pubKey2;
        SignedList   list2;
        byte[] myHash;
        byte[] hash2;
        byte[] bHash;

        for (int i = 0; i < 8; i++) {
            key = (RSAKey) keyGen.generate();
            pubKey = (RSAPublicKey) key.getPublicKey();
            myList = new MySignedList (pubKey, "document 1");
            myHash = myList.getHash();
            list2  = new MySignedList (pubKey, "document 1");
            hash2  = list2.getHash();
            checkSameHash (myHash, hash2);
            list2  = new MySignedList (pubKey, "document 2");
            hash2  = list2.getHash();
            assertTrue (notSameHash(myHash, hash2));

//          // a build list with the same key and title has same hash
//          BuildList buildList = new BuildList(pubKey, "document 1");
//          bHash = buildList.getHash();
//          assertEquals (20, bHash.length);
//          checkSameHash (bHash, myHash);
        }
    }
}
