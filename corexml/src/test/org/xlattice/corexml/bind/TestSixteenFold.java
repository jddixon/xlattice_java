/* TestSixteenFold.java */
package org.xlattice.corexml.bind;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.Random;

import junit.framework.*;

import org.xlattice.corexml.*;
import org.xlattice.corexml.om.*;
import static org.xlattice.util.StringLib.*;

/**
 * Test 16 combinations of Interface usage: four ways of
 * connecting Interfaces and four types of quantifiers.  The
 * four types of connection are
 * <ul>
 *   <li>Mapping whose main class has an Interface for a member</li>
 *   <li>Mapping where the Interface instances are under a Collector</li>
 *   <li>SubMapping representing an Interface</li>
 *   <li>SubMapping where the interface instances are under a Collector</li>
 * </ul>
 * The four types of quantifier under test are
 * <ul>
 *   <li>?, optional(), where [min, max] is [0, 1]</li>
 *   <li>*, where [min, max] is [0, Integer.MAX_VALUE]</li>
 *   <li>+, where [min, max] is [1, Integer.MAX_VALUE]</li>
 *   <li>[N1, N2], where N1 is greater than 1 and N2 is greater than or equal to N1</li>
 * </ul>
 *
 * XXX THIS SHOULD BECOME TestTwentyFold, ADDING A FIFTH TYPE OF QUANTIFIER:
 * XXX NONE AT ALL.  Further desirable improvements:
 *   data[0] (?)     fails if 2 Markers
 *   data[2] (+)     fails if 0 Markers
 *   data[3] (n1,n2) fails if 0, n1-1, n2+1 markers
 *   reconfirm that the [n1,n2] range is inclusive at both ends
 * XXX THEN RENAME THIS TO TestInterface.
 * 
 * @author Jim Dixon
 */
public class TestSixteenFold extends CoreXmlTestCase {

    // CONSTANTS ////////////////////////////////////////////////////

    // CLASS NAMES ////////////////////////////////////////
    protected static final String C_V =
                            "org.xlattice.corexml.bind.V";
    // marker interface
    protected static final String I_M =
                            "org.xlattice.corexml.bind.Marker";
    // contains no or one instance of Marker
    protected static final String C_W1 =
                            "org.xlattice.corexml.bind.W1";
    // contains instances of Marker
    protected static final String C_W =
                            "org.xlattice.corexml.bind.W";

    // F, X, Y, Z implement Marker
    protected static final String C_F =
                            "org.xlattice.corexml.bind.F";
    protected static final String C_X =
                            "org.xlattice.corexml.bind.X";
    protected static final String C_Y =
                            "org.xlattice.corexml.bind.Y";
    protected static final String C_Z =
                            "org.xlattice.corexml.bind.Z";

    // TEST XML DATA //////////////////////////////////////
    String startXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    String startV   = "<v>\n";
    String startW   = "<w attr1=\"value1\" attr2=\"value2\">\n";
    String startCollector = "<fxyzs>\n";
    String endCollector   = "</fxyzs>\n";
    String endW = "</w>";
    String endV = "</v>";

    // TEST DATA STRUCTURES ///////////////////////////////
    protected String[]  data = new String [16];
    protected Mapping[] maps = new Mapping[16];

    private long seed  = new Date().getTime();
    private Random rng = new Random(seed);

    // TEST PARAMETERS ////////////////////////////////////
    private boolean hasOptional;
    int starCount;
    int plusCount; 
    int n1;
    int n2;
    
    // TEST CASE CONSTRUCTOR ////////////////////////////////////////
    public TestSixteenFold (String name) {
        super(name);
    }
    public Mapping makeW(int k)             throws CoreXmlException {
        Mapping map;
        if ((k % 4) == 0) {
            map = new Mapping("w", C_W1);
        } else {
            map = new Mapping("w", C_W);
        }
        map.add( new AttrBinding("attr1") );
        map.add( new AttrBinding("attr2") );
        return map;
    }
    public SubMapping makeSubW(int k)       throws CoreXmlException {
        SubMapping submap;
        if ((k % 4) == 0)
            submap = new SubMapping("w", C_W1);
        else
            submap = new SubMapping("w", C_W);
        submap.add( new AttrBinding("attr1") );
        submap.add( new AttrBinding("attr2") );
        return submap;
    }
    public void addImplementors(Interface iface)
                                            throws CoreXmlException {
        SubMapping subMapF = new SubMapping("f", C_F, "marker");
        subMapF.add( new TextBinding("fValue") );
        iface.add(subMapF);

        SubMapping subMapX = new SubMapping("x", C_X, "marker");
        subMapX.add( new AttrBinding("attr3") );
        subMapX.add( new TextBinding("valueX") );
        iface.add(subMapX);

        SubMapping subMapY = new SubMapping("y", C_Y, "marker");
        subMapY.add( new AttrBinding("attr4") );
        subMapY.add( new TextBinding("valueY") );
        iface.add(subMapY);

        SubMapping subMapZ = new SubMapping("z", C_Z, "marker");
        subMapZ.add( new AttrBinding("hyph-attr") );
        subMapZ.add( new TextBinding("valueZ") );
        iface.add(subMapZ);
    }
    public Mapping generateMapping(int k)   throws CoreXmlException {

        // 0-3:   Interface DIRECTLY BELOW MAPPING ////////
        // 4-7:   Interface BELOW COLLECTOR BELOW MAPPING
        // 8-11:  Interface DIRECTLY BELOW SUBMAPPING /////
        // 12-15: Interface BELOW COLLECTOR BELOW SUBMAPPING

        Mapping    map    = null;
        SubMapping submap = null;
        Collector  lector = null;
        Interface  iface;
        if (k < 8) {
            map = makeW(k);
        } else {
            map = new Mapping("v", C_V);
            submap = makeSubW(k);
        }
        if ((k % 8) >= 4)
            lector = new Collector("fxyzs");
        iface = new Interface("Marker", I_M, "marker");
        switch ( k % 4) {
            case 0: iface.optional();                      break;
            case 1: iface.optional().repeats();            break;
            case 2: iface.repeats();                       break;
            case 3: iface.setMaxOccur(n2).setMinOccur(n1); break;
        }
        addImplementors(iface);
        if ((k % 8) >= 4) {
            lector.add(iface);
            if (k < 8) {
                // 4, 5, 6, 7
                map.add(lector);
            } else {
                // 12, 13, 14, 15
                submap.add(lector);
                map.add(submap);
            }
        } else if (k < 8) {
            // 0, 1, 2, 3
            map.add(iface);
        } else {
            // 8, 9, 10, 11
            submap.add(iface);
            map.add(submap);
        }
        map.join();
        return map;
    }
    public void generateMappings()      throws CoreXmlException {
//      // DEBUG
//      System.out.printf(
//          "generateMappings:\n\thasOptional = %s, starCount = %d, plusCount = %d, n1 = %d, n2 = %d\n",
//          hasOptional, starCount, plusCount, n1, n2);
//      // END
        for (int k = 0; k < 16; k++)
            maps[k] = generateMapping(k);
    }
    /** string of hash digits */
    public String noise() {
        int n = 8 + rng.nextInt(16);
        byte[] data = new byte[n];
        rng.nextBytes(data);
        return byteArrayToHex(data);
    }
    public String randomF() {
        return new StringBuffer("<f>")
            .append(noise())
            .append("</f>\n").toString();
    }
    public String randomX() {
        return new StringBuffer("<x attr3=\"")
            .append(noise())
            .append("\">")
            .append(noise())
            .append("</x>\n").toString();
    }
    public String randomY() {
        return new StringBuffer("<y attr4=\"")
            .append(rng.nextInt(9999))
            .append("\">")
            .append(noise())
            .append("</y>\n").toString();
    }
    public String randomZ() {
        return new StringBuffer("<z hyph-attr=\"")
            .append(rng.nextBoolean())
            .append("\">")
            .append(noise())
            .append("</z>\n").toString();
    }
    /** @return one randomly chosen serialized marker */
    public String randomMarker() {
        int which = rng.nextInt(4);
        switch(which) {
            case 0:     return randomX();
            case 1:     return randomY();
            case 2:     return randomZ();
            default:    return randomF();
        }
    }
    /** @return n serialized random Markers */
    public String nMarker(int n) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++)
            sb.append(randomMarker());
        return sb.toString();
    }
    /**
     * Marker is implemented by F, X, Y, Z; return any one of them
     * as an XML fragment OR (50:50) an empty string.
     */
    public String optionalMarker() {
        if (!hasOptional)
            return "";
        return randomMarker();
    }
    /**
     * Populate the sixteen data Strings.  We use W1 instead of
     * W for the ? quantifier.
     */
    public void generateTestData() {

        // 0-3:   Interface DIRECTLY BELOW MAPPING ////////
        // 4-7:   Interface BELOW COLLECTOR BELOW MAPPING
        // 8-11:  Interface DIRECTLY BELOW SUBMAPPING /////
        // 12-15: Interface BELOW COLLECTOR BELOW SUBMAPPING
        for (int i = 0; i < 16; i++) {
            StringBuffer sb = new StringBuffer();
            sb.append(startXml);
            if ( i >= 8)
                sb.append(startV);
            sb.append(startW);
            if ( (i % 8) >= 4)
                sb.append(startCollector);
            switch ( i % 4 ) {
                case 0: sb.append(optionalMarker());    break;  // ?
                case 1: sb.append(nMarker(starCount));  break;  // *
                case 2: sb.append(nMarker(plusCount));  break;  // +
                case 3:
                    sb.append(nMarker(n1 + rng.nextInt (n2 - n1 + 1)));
                    break;                                      // n1..n2
            }
            if ( (i % 8) >= 4)
                sb.append(endCollector);
            sb.append(endW);
            if ( i >= 8)
                sb.append(endV);
            data[i] = sb.toString();
        }
    }
    /**
     * Generate a quasi-random combination of 16 sets of test data
     * and four basic mappings, each quantified four different
     * ways.
     */
    public void setUp()                         throws Exception {
        seed ^= rng.nextLong();
        rng.setSeed(seed);

        // values relating to the 4 different quantifiers
        hasOptional = rng.nextBoolean();
        starCount   = rng.nextInt(9);       // 0..8
        plusCount   = 1 + rng.nextInt(9);   // 1..8
        n1          = 2 + rng.nextInt(4);   // 2..5
        n2          = n1 + rng.nextInt(4);  // n1..(n1+3)

        generateTestData();
        generateMappings();

    }
    // XXX THIS TEST INCORRECTLY ASSUMES that the attributes will
    // be serialized in a particular order
    public void testFixture()                   throws Exception {
        for (int i = 0; i < 16; i++) {
            if (data[i] != null) {

//              // DEBUG
//              System.out.println("---- data[" + i + "] ------------\n"
//                      + data[i]);
//              // END


                Document doc = new XmlParser(new StringReader(data[i]))
                                .read();
                Object o = maps[i].apply(doc);
                assertNotNull(o);
                Document doc2;
                if (i < 8) {
                    assertTrue( o instanceof W );
                    W tree = (W) o;
                    doc2 = maps[i].generate(tree);

                } else {
                    assertTrue( o instanceof V );
                    V tree = (V) o;
                    doc2 = maps[i].generate(tree);
                }
                // Doesn't work if hasOptional == false
                // OR starCount happens to be zero
                if ( !( (i % 4) == 0 && !hasOptional ) 
                  && !( (i % 4) == 1 && starCount == 0) )
                    assertSameSerialization (data[i], doc2.toXml());
            }
        }
    }
}
