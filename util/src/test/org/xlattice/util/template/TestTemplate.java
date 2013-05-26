/* TestTemplate.java */
package org.xlattice.util.template;

import java.util.Random;
import junit.framework.*;

/**
 * @author Jim Dixon
 **/

import org.xlattice.Context;
import org.xlattice.Template;

public class TestTemplate extends TestCase {

    Random rng = new Random();
    Context nullCtx = new Context();
    
    public TestTemplate (String name) {
        super(name);
    }

    public void setUp () {
    }
    // BINARY TEMPLATES /////////////////////////////////////////////
    /** 
     * Dunno what this is good for yet.
     */
    public void testBinary()                    throws Exception {
        try {
            BinaryTemplate junk = new BinaryTemplate (null);
            fail("didn't catch null arg to constructor");
        } catch (IllegalArgumentException iae) { /* success */ }
        byte[] data1 = new byte[ 16 + rng.nextInt(16) ];
        byte[] data2 = new byte[ 16 + rng.nextInt(16) ];
        rng.nextBytes(data1);
        rng.nextBytes(data2);
        BinaryTemplate t1 = new BinaryTemplate (data1);
        assertNotNull(t1);
        assertEquals( data1.length, t1.getBytes(nullCtx).length );
        BinaryTemplate t2 = new BinaryTemplate (data2);
        assertEquals( data2.length, t2.getBytes(nullCtx).length );

        // SEQ TEMPLATE ///////////////////////////////////
        SeqTemplate seq = new SeqTemplate ( new Template[] {t1, t2} );
        assertEquals (2, seq.size());
        int len = data1.length + data2.length;
        byte[] data = new byte[len];
        for (int k = 0; k < data1.length; k++)
            data[k] = data1[k];
        for (int k = 0 ; k < data2.length; k++)
            data[data1.length + k] = data2[k];
        byte[]seqData = seq.getBytes(nullCtx);
        assertEquals(data.length, seqData.length);
        for (int i = 0; i < data.length; i++)
            assertEquals(data[i], seqData[i]);
    }
    // STRING TEMPLATES /////////////////////////////////////////////
    public String makeRandomString () {
        StringBuffer sb = new StringBuffer();
        int len = 16 + rng.nextInt(16);
        for (int i = 0; i < len; i++) 
            sb.append( (char)('A' + rng.nextInt(26)));
        return sb.toString();
    }
    public void testStrings()                   throws Exception {
        try {
            StringTemplate junk = new StringTemplate (null);
            fail("didn't catch null arg to constructor");
        } catch (IllegalArgumentException iae) { /* success */ }
        String s1 = makeRandomString();
        String s2 = makeRandomString();
        StringTemplate t1 = new StringTemplate(s1);
        StringTemplate t2 = new StringTemplate(s2);
        assertEquals (s1, t1.toString(nullCtx));
        assertEquals (s2, t2.toString(nullCtx));

        // SEQ TEMPLATE ///////////////////////////////////
        SeqTemplate seq = new SeqTemplate ( new Template[] {t1, t2} );
        assertEquals (2, seq.size());
        assertEquals ( s1 + s2, seq.toString(nullCtx) );
    }
    // VAR TEMPLATES ////////////////////////////////////////////////
    // XXX no tests with binary data
    public void testVar()                       throws Exception {
        VarTemplate t1 = new VarTemplate("var1");
        VarTemplate t2 = new VarTemplate("var2");
        
        Context ctx = new Context();
        String s1 = makeRandomString();
        String s2 = makeRandomString();
        ctx.bind("var1", s1);
        ctx.bind("var2", s2);
        assertEquals (s1, t1.toString(ctx));
        assertEquals (s2, t2.toString(ctx));

        // SEQ TEMPLATE ///////////////////////////////////
        SeqTemplate seq = new SeqTemplate ( new Template[] {t1, t2} );
        assertEquals (2, seq.size());
        assertEquals ( s1 + s2, seq.toString(ctx) );
        
        String s3 = makeRandomString();
        String s4 = makeRandomString();
        ctx.bind("var1", s3);
        ctx.bind("var2", s4);
        assertEquals ( s3 + s4, seq.toString(ctx) );
    }
}
