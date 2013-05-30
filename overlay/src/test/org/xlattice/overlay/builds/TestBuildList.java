/* TestBuildList.java */
package org.xlattice.overlay.builds;

import junit.framework.*;

/**
 * @author Jim Dixon
 **/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger; 
import java.util.Date;
import java.util.Random;

// FIXME
import org.xlattice.crypto.builds.BuildList;
import org.xlattice.crypto.builds.BuildMaker;
// END

import org.xlattice.CryptoException;
import org.xlattice.DigSigner;
import org.xlattice.SigVerifier;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.RSAKeyGen;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.util.Base64Coder;

public class TestBuildList extends TestCase {

    private BuildList myList;

    private RSAKeyGen keyGen;
    private RSAKey key;
    private RSAPublicKey pubKey;

    private Random rng = new Random( new Date().getTime() );

    final static String CRLF = "\r\n";
    final static String FOLD = "\r\n ";

    // a folded RSA public key
    final static String docPK1 =
"rsa AL0zGtdGkuJdH1vd4TaUMmRvdEBepnGfAbvZXPkdsVq367VUevbfzNL4W6u+Ks8+BksZzZPc";
    final static String docPK2 =
"yLJsnDZr7mE/rHSwQ7la1HlSWwNDlhQtCnKTlSoqffVhofhtak/SqBOJVLkWrouaK60uCiZV0Hw";
    final static String docPK3 =
"YTM6Pqo8sqYinA3W8mvK2tsW/ 65537";
    final static String docPubKey = docPK1 + FOLD + docPK2 + FOLD +  docPK3;
    
    final static String docTitle = "document 1";
    final static String docTime  = "2004-11-18 20:03:34";
    final static String docEncodedSig   =
"tIQJ+7Y27eIyQCb3esTgU/AdBfPDAGEOhU/KShAo5N5dfxtjkH04N5IwvyftEJd5jM0kHB1LD1TtavoxZ0gx4eADizHcDjEpZOiO+wUHIcbGsuvLUvZvBttPPBRuRfZgZXkvvSMBX0KIwRVgFqwaRB5gzQyD2skcP2kGFBWrFdM=";
    final static String testDoc = docPubKey + CRLF + 
                     docTitle  + CRLF +
                     docTime   + CRLF +
                     docEncodedSig;
    
    final static private BigInteger p = new BigInteger(
"11585915895977729349927141485551842863761068448179004047266108348797920568589963318011635656475777096786995238780470018198645622126817935053764429544450131");
    final static private BigInteger q = new BigInteger(
"11467414266740093735891803632246889441466384740340435567479479847133626695022437546886477932950292503316460953732154702533130908630285843790724777725677413");
    final static private BigInteger e = new BigInteger("65537");
    final static private BigInteger d = new BigInteger(
"73092836534239037461702886727379083749515267581013163660069811635506446917188175395140681814113812996430983110942879749268447276410295820186709052981693411210191550395573747993456516408384793841993811607501646162554848604488002220656846265130166815413401292017141086533220649184728677162184903459954078066873");
                                             
    public TestBuildList (String name)     throws CryptoException {
        super(name);
        keyGen= new RSAKeyGen();
    }

    public void setUp () {
        myList = null;
    }

    public void testEmptyBuildList()            throws Exception {
        key = (RSAKey) keyGen.generate();
        pubKey = (RSAPublicKey) key.getPublicKey();
        myList = new BuildList (pubKey, "document 1");
        assertNotNull(myList);
        assertEquals(0, myList.size());
        assertFalse(myList.isSigned());

        myList.sign(key);
        assertTrue(myList.isSigned());
        assertTrue (myList.verify());

        try {
            myList.sign(key);
            fail("sign() succeeded although list already signed");
        } catch (CryptoException e) { /* success */ }
    } 

    public void testGeneratedBuildList()        throws Exception {
        key = (RSAKey) keyGen.generate();
        pubKey = (RSAPublicKey) key.getPublicKey();
        myList = new BuildList (pubKey, "document 1");

        byte[] hash1 = new byte[20];
        byte[] hash2 = new byte[20];
        byte[] hash3 = new byte[20];
        byte[] hash4 = new byte[20];
        rng.nextBytes(hash1);
        rng.nextBytes(hash2);
        rng.nextBytes(hash3);
        rng.nextBytes(hash4);
       
        // XXX NOTE WE CAN ADD DUPLICATE OR CONFLICTING ITEMS !! XXX
        myList.add (hash1, "fileForHash1");
        assertEquals(1, myList.size());
        myList.add (hash2, "fileForHash2");
        myList.add (hash3, "fileForHash3");
        myList.add (hash4, "fileForHash4");
        assertEquals(4, myList.size());
        // check (arbitrarily) second content line
        assertEquals ( Base64Coder.encode(hash2) + " fileForHash2",
                       myList.toString(1) );
        myList.sign(key);
        assertTrue(myList.isSigned());
        assertTrue (myList.verify());

        String myDoc = myList.toString();
       
        BuildList list2 = new BuildList (new StringReader(myDoc));
        assertNotNull(list2);
        assertEquals(4, list2.size());
        assertTrue (list2.isSigned());
        assertEquals(myDoc, list2.toString());
        assertTrue (list2.verify());

        // test item gets - sloppy naming, so can't loop :-(
        byte[] b = myList.getHash(0);
        assertEquals (hash1.length, b.length);
        for (int i = 0; i < hash1.length; i++)
            assertEquals (hash1[i], b[i]);
        assertEquals ("fileForHash1", myList.getPath(0));
        
        b = myList.getHash(1);
        assertEquals (hash2.length, b.length);
        for (int i = 0; i < hash2.length; i++)
            assertEquals (hash2[i], b[i]);
        assertEquals ("fileForHash2", myList.getPath(1));
    } 
}
