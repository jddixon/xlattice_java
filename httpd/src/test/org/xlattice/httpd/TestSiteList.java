/* TestSiteList.java */
package org.xlattice.httpd;

import junit.framework.*;

/**
 * @author Jim Dixon
 */

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger; 
import java.util.Date;
import java.util.Random;

import org.xlattice.CryptoException;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.RSAKeyGen;

public class TestSiteList extends TestCase {

    private SiteList myList;

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
    
    final static private BigInteger p = new BigInteger(
"11585915895977729349927141485551842863761068448179004047266108348797920568589963318011635656475777096786995238780470018198645622126817935053764429544450131");
    final static private BigInteger q = new BigInteger(
"11467414266740093735891803632246889441466384740340435567479479847133626695022437546886477932950292503316460953732154702533130908630285843790724777725677413");
    final static private BigInteger e = new BigInteger("65537");
    final static private BigInteger d = new BigInteger(
"73092836534239037461702886727379083749515267581013163660069811635506446917188175395140681814113812996430983110942879749268447276410295820186709052981693411210191550395573747993456516408384793841993811607501646162554848604488002220656846265130166815413401292017141086533220649184728677162184903459954078066873");
                                             
    public TestSiteList (String name)     throws CryptoException {
        super(name);
        keyGen= new RSAKeyGen();
    }

    public void setUp () {
        myList = null;
    }

    public void testEmptySiteList()            throws Exception {
        key = (RSAKey) keyGen.generate();
        pubKey = (RSAPublicKey) key.getPublicKey();
        myList = new SiteList (pubKey, "document 1");
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

    public void testGeneratedSiteList()        throws Exception {
        String [] siteNames = {
            "www.siteA.com", 
            "www.siteB.com", 
            "www.siteC.com", 
            "www.siteD.com", 
            "www.siteE.com"
        };
        key    = (RSAKey) keyGen.generate();
        pubKey = (RSAPublicKey) key.getPublicKey();
        myList = new SiteList (pubKey, "document 1");

        for (int i = 0; i < siteNames.length; i++) {
            myList.add (siteNames[i]);
            assertEquals(i + 1, myList.size());
        }
        myList.sign(key);
        assertTrue(myList.isSigned());
        assertTrue (myList.verify());
        for (int i = 0; i < siteNames.length; i++)
            assertEquals (siteNames[i], myList.toString(i) );

        String textForm = myList.toString();
      
        // Convert the SiteList to string form, then read it, 
        // creating a new SiteList.  Verify the digital signature
        // on the new list.
        SiteList list2 = new SiteList (new StringReader(textForm));
        assertNotNull(list2);
        assertEquals(siteNames.length, list2.size());
        assertTrue (list2.isSigned());
        assertEquals(textForm, list2.toString());
        assertTrue (list2.verify());
    } 
}
