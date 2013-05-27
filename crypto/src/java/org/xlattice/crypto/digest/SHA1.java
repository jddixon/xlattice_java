/* SHA1.java */
package org.xlattice.crypto.digest;

import org.xlattice.CryptoException;
import org.xlattice.crypto.Digest;
import org.xlattice.util.StringLib;

/**
 * Secure Hash Algorithm, as modified in FIPS 180-1, 17 April 1995,
 * to become SHA-1.
 * 
 * SHA1 is one of many similar algorithms.  Padding at least should
 * be split out, as it is much the same with many of them.
 *
 * XXX Need to review when to throw CryptoException.
 *
 * @author Jim Dixon
 */
public class SHA1 implements Digest {

    // STATIC ///////////////////////////////////////////////////////
    /** h0, the initialization vector, so to speak */
    public final static int A0 = 0x67452301;
    public final static int B0 = 0xEFCDAB89;
    public final static int C0 = 0x98BADCFE;
    public final static int D0 = 0x10325476;
    public final static int E0 = 0xC3D2E1F0;
   
    /** square roots as signed INT32s */
    public final static int K0 = 0x5A827999;    // sqrt(2)/4
    public final static int K1 = 0x6ED9EBA1;    // sqrt(3)/4
    public final static int K2 = 0x8F1BBCDC;    // sqrt(5)/4
    public final static int K3 = 0xCA62C1D6;    // sqrt(10)/4

    // INSTANCE VARIABLES ///////////////////////////////////////////
    /** capitalized as in the standard, violating Java norms */
    private int A, B, C, D, E;      // hash from the last round
    /** input that has not yet been processed */
    private byte[] buffer;          // 64-byte (512 bit) 
    /** number of meaningful bytes in the buffer */
    private int    bufBytes;
    /** blocks processed so far */
    private long   blocksProcessed;
    /** int[16] input block expanded */
    private int[] w = new int [80];
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    /**
     * No-arg constructor.
     */
    public SHA1 () {
        buffer = new byte[64];
        _reset();
    }
    /**
     * Cloning constructor.  Duplicate the state of the other SHA1
     * digest.
     */
    public SHA1 (SHA1 other) {
        A = other.A;
        B = other.B;
        C = other.C;
        D = other.D;
        E = other.E;
        bufBytes = other.bufBytes;
        for (int i = 0; i < bufBytes; i++)
            buffer[i] = other.buffer[i];
        blocksProcessed = other.blocksProcessed;
    }
    // IMPLEMENTATION ///////////////////////////////////////////////
    private final void _reset() {
        A = A0;
        B = B0;
        C = C0;
        D = D0;
        E = E0;
        bufBytes        = 0;    // buffer is effectively empty
        blocksProcessed = 0L;   // zero blocks processed
    }
    /**
     * Expand byte[64]/int32[16]/512 bit block to int32[80].  No check is 
     * made on the size of the block, but it must be 64 bytes long, and
     * the usual padding (1, zeroes, then int64 length bringing it to a
     * 512-bit boundary) must have been added, unless of course you are
     * writing a paper for a conference.
     *
     * The final rotation by one bit is what distinguishes SHA1 from
     * SHA0.
     *
     * @param msg    message being hashed
     * @param offset beginning of the block in the message byte array
     */
    protected final void _expand(final byte[] msg, int offset) {
        int i;
        for (i =  0; i < 16; i++)
            w[i] = (msg[offset++]       ) << 24 |
                   (msg[offset++] & 0xff) << 16 |
                   (msg[offset++] & 0xff) <<  8 |
                   (msg[offset++] & 0xff)       ;
        for (i = 16; i < 80; i++) {
            int val = w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16];
            w[i] = val << 1 | val >>> 31;   // idiom for val <<< 1
        }
    }
    /** unoptimized */
    protected final int _f0(int x, int y, int z) {
        return (x & y) | ((~x) & z);
    }
    /** unoptimized */
    protected final int _f1(int x, int y, int z) {
        return x ^ y ^ z;
    }
    /** unoptimized */
    protected final int _f2(int x, int y, int z) {
        return (x & y) | (x & z) | (y & z);
    }
    /* f3 is the same as f1 */
  
    /**
     * SHA's 80-step cycle, unoptimized.  Useful for exercises
     * like the Shandong tests, where results for so many 
     * subrounds are of interest.  Shandong paper has a collision at
     * subround 57 (58, one-based).
     * 
     * No checks on input are performed.
     * 
     * @param msg    buffer holding data to be compressed
     * @param offset of first byte to be processed
     * @param stopAt last SHA subround
     * @param pretty whether to pretty-print results
     */
    protected final void _cycle(final byte[] msg, int offset, 
                                      int stopAt, boolean pretty) {
        if (stopAt <= 0 | stopAt > 80)
            stopAt = 80;
        int roundCount = 0;
        _expand(msg, offset);       // builds int[80]w 
        if (pretty) 
            prettyPrintMsg(); 
        int a = A;
        int b = B;
        int c = C;
        int d = D;
        int e = E;
        int x;

        for (int i = 0; i < 20 && roundCount < stopAt; i++, roundCount++) {
            x = ((a <<  5) | (a >>> 27)) + _f0(b, c, d) + e + w[i] + K0;
            e = d;
            d = c;
            c = (b << 30) | (b >>>  2);
            b = a;
            a = x;
            if (pretty)
                prettyPrint(i, a, b, c, d, e);
        } 
        for (int i = 20; i < 40 && roundCount < stopAt; i++, roundCount++) {
            x = ((a <<  5) | (a >>> 27)) + _f1(b, c, d) + e + w[i] + K1;
            e = d;
            d = c;
            c = (b << 30) | (b >>>  2);
            b = a;
            a = x;
            if (pretty)
                prettyPrint(i, a, b, c, d, e);
        }
        for (int i = 40; i < 60 && roundCount < stopAt; i++, roundCount++) {
            x = ((a <<  5) | (a >>> 27)) + _f2(b, c, d) + e + w[i] + K2;
            e = d;
            d = c;
            c = (b << 30) | (b >>>  2);
            b = a;
            a = x;
            if (pretty)
                prettyPrint(i, a, b, c, d, e);
        } 
        for (int i = 60; i < 80 && roundCount < stopAt; i++, roundCount++) {
            x = ((a <<  5) | (a >>> 27)) + _f1(b, c, d) + e + w[i] + K3;
            e = d;
            d = c;
            c = (b << 30) | (b >>>  2);
            b = a;
            a = x;
            if (pretty)
                prettyPrint(i, a, b, c, d, e);
        }
        A += a;
        B += b;
        C += c;
        D += d;
        E += e;
        blocksProcessed++;
    }
    /**
     * SHA's 80-step cycle unrolled.  There are no checks on input.
     * 
     * @param msg    buffer holding data to be compressed
     * @param offset of first byte to be processed
     */
    protected final void _fastCycle(final byte[] msg, int offset) {
        _expand(msg, offset);       // builds int[80]w 
        
        int a = A;
        int b = B;
        int c = C;
        int d = D;
        int e = E;
        int x;

        e += ((a<<5)|(a>>>27)) + ((b&(c^d))^d) + K0 + w[ 0]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + ((a&(b^c))^c) + K0 + w[ 1]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + ((e&(a^b))^b) + K0 + w[ 2]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + ((d&(e^a))^a) + K0 + w[ 3]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + ((c&(d^e))^e) + K0 + w[ 4]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + ((b&(c^d))^d) + K0 + w[ 5]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + ((a&(b^c))^c) + K0 + w[ 6]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + ((e&(a^b))^b) + K0 + w[ 7]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + ((d&(e^a))^a) + K0 + w[ 8]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + ((c&(d^e))^e) + K0 + w[ 9]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + ((b&(c^d))^d) + K0 + w[10]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + ((a&(b^c))^c) + K0 + w[11]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + ((e&(a^b))^b) + K0 + w[12]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + ((d&(e^a))^a) + K0 + w[13]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + ((c&(d^e))^e) + K0 + w[14]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + ((b&(c^d))^d) + K0 + w[15]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + ((a&(b^c))^c) + K0 + w[16]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + ((e&(a^b))^b) + K0 + w[17]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + ((d&(e^a))^a) + K0 + w[18]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + ((c&(d^e))^e) + K0 + w[19]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + (b^c^d) + K1 + w[20]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + (a^b^c) + K1 + w[21]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + (e^a^b) + K1 + w[22]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + (d^e^a) + K1 + w[23]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + (c^d^e) + K1 + w[24]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + (b^c^d) + K1 + w[25]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + (a^b^c) + K1 + w[26]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + (e^a^b) + K1 + w[27]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + (d^e^a) + K1 + w[28]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + (c^d^e) + K1 + w[29]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + (b^c^d) + K1 + w[30]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + (a^b^c) + K1 + w[31]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + (e^a^b) + K1 + w[32]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + (d^e^a) + K1 + w[33]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + (c^d^e) + K1 + w[34]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + (b^c^d) + K1 + w[35]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + (a^b^c) + K1 + w[36]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + (e^a^b) + K1 + w[37]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + (d^e^a) + K1 + w[38]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + (c^d^e) + K1 + w[39]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + ((b&c)|((b|c)&d)) + K2 + w[40]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + ((a&b)|((a|b)&c)) + K2 + w[41]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + ((e&a)|((e|a)&b)) + K2 + w[42]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + ((d&e)|((d|e)&a)) + K2 + w[43]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + ((c&d)|((c|d)&e)) + K2 + w[44]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + ((b&c)|((b|c)&d)) + K2 + w[45]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + ((a&b)|((a|b)&c)) + K2 + w[46]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + ((e&a)|((e|a)&b)) + K2 + w[47]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + ((d&e)|((d|e)&a)) + K2 + w[48]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + ((c&d)|((c|d)&e)) + K2 + w[49]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + ((b&c)|((b|c)&d)) + K2 + w[50]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + ((a&b)|((a|b)&c)) + K2 + w[51]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + ((e&a)|((e|a)&b)) + K2 + w[52]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + ((d&e)|((d|e)&a)) + K2 + w[53]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + ((c&d)|((c|d)&e)) + K2 + w[54]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + ((b&c)|((b|c)&d)) + K2 + w[55]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + ((a&b)|((a|b)&c)) + K2 + w[56]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + ((e&a)|((e|a)&b)) + K2 + w[57]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + ((d&e)|((d|e)&a)) + K2 + w[58]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + ((c&d)|((c|d)&e)) + K2 + w[59]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + (b^c^d) + K3 + w[60]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + (a^b^c) + K3 + w[61]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + (e^a^b) + K3 + w[62]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + (d^e^a) + K3 + w[63]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + (c^d^e) + K3 + w[64]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + (b^c^d) + K3 + w[65]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + (a^b^c) + K3 + w[66]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + (e^a^b) + K3 + w[67]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + (d^e^a) + K3 + w[68]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + (c^d^e) + K3 + w[69]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + (b^c^d) + K3 + w[70]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + (a^b^c) + K3 + w[71]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + (e^a^b) + K3 + w[72]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + (d^e^a) + K3 + w[73]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + (c^d^e) + K3 + w[74]; c=(c<<30)|(c>>>2);
        
        e += ((a<<5)|(a>>>27)) + (b^c^d) + K3 + w[75]; b=(b<<30)|(b>>>2);
        d += ((e<<5)|(e>>>27)) + (a^b^c) + K3 + w[76]; a=(a<<30)|(a>>>2);
        c += ((d<<5)|(d>>>27)) + (e^a^b) + K3 + w[77]; e=(e<<30)|(e>>>2);
        b += ((c<<5)|(c>>>27)) + (d^e^a) + K3 + w[78]; d=(d<<30)|(d>>>2);
        a += ((b<<5)|(b>>>27)) + (c^d^e) + K3 + w[79]; c=(c<<30)|(c>>>2);

        A += a;
        B += b;
        C += c;
        D += d;
        E += e;
        blocksProcessed++;
    }

    /**
     * Zero fill the buffer, put in the length, and run it through 
     * the mill.  The 0x80 byte must have already been added.
     *
     * There is no check on the parameter.
     *
     * @param bytesLastBlock bytes in the last block processed
     */
    protected final void padAndAddLength (int bytesLastBlock) {
        if (bytesLastBlock >= 56)
            blocksProcessed--;
        long bitLen = blocksProcessed * 512 + bytesLastBlock * 8;

        for (int i = bufBytes; i < 56; i++)
            buffer[i] = (byte)0;
        buffer[63] = (byte) bitLen ;    bitLen >>= 8;
        buffer[62] = (byte) bitLen ;    bitLen >>= 8;
        buffer[61] = (byte) bitLen ;    bitLen >>= 8;
        buffer[60] = (byte) bitLen ;    bitLen >>= 8;
        buffer[59] = (byte) bitLen ;    bitLen >>= 8;
        buffer[58] = (byte) bitLen ;    bitLen >>= 8;
        buffer[57] = (byte) bitLen ;    bitLen >>= 8;
        buffer[56] = (byte) bitLen ;
        _fastCycle(buffer, 0);
    } 
    /** 
     * There is not enough room for the length left in the block
     * (it's over 56 bytes long), so just zero fill and run it
     * through.  We will put the length in a final block..
     */
    protected final void padAndUpdate () {
        for (int i = bufBytes; i < 64; i++)
            buffer[i] = (byte)0;
        _fastCycle(buffer, 0);
    }
    // PROPERTIES (intermediate results) ////////////////////////////
    /** 
     * Returns the result of an 80-step round without adding the
     * initial hash, h0; in the form of an int array.
     * 
     * @return the digest as an int array 
     */
    public final int[] intResult() {
        return new int [] {A, B, C, D, E};
    }
    /** 
     * Returns the result of an 80-step round without adding the
     * initial hash, h0; in the form of a byte array.
     * 
     * @return the digest as an int array 
     */
    public final byte[] byteResult() {
        int[]  val  = intResult();
        byte[] hash = new byte[20];
        int j = 0;
        for (int i = 0; i < val.length; i++) {
            hash[j++] = (byte)(val[i] >> 24); 
            hash[j++] = (byte)(val[i] >> 16);
            hash[j++] = (byte)(val[i] >>  8);
            hash[j++] = (byte)(val[i]      );
        }
        return hash;
    }
    // INTERFACE Digest /////////////////////////////////////////////
    public byte[] digest () {
        // We can be confident that bufBytes is non-negative and less
        // than 64, the buffer's capacity.
        buffer[bufBytes++] = (byte)0x80;    // the 1 bit
        if (bufBytes == 64) {
            // we just took the last free byte
            _fastCycle(buffer, 0);
            bufBytes = 0;
            padAndAddLength(63);
        } else if (bufBytes > 56) {
            int lastBufBytes = bufBytes - 1;
            padAndUpdate();                 // zero-fills, no length
            bufBytes = 0;
            padAndAddLength(lastBufBytes);  // zero-fills, adds length
        } else {
            padAndAddLength(bufBytes - 1);  // may zero-fill, adds length
        }
        byte[] hash = byteResult();
        _reset();
        return hash;
    }
    public byte[] digest (byte[] data) {
        update(data); 
        return digest();
    }
    public final int length () {
        return 20;
    }
    public final void reset () {
        _reset();
    }
    /**
     * @param data   buffer holding data being added
     * @param offset where we start 
     * @param len    length in bytes of data being added
     */
    public void update (byte[] data, final int offset, final int len) {
        if (data == null)
            throw new IllegalArgumentException("null data");
        if (offset >= data.length || len <= 0 || offset + len > data.length)
            throw new IllegalArgumentException(
                    "data length=" + data.length 
                    + ", offset=" + offset + ", len=" + len);
        
        int from     = offset;
        final int to = offset + len;
        if (bufBytes > 0) {
            // This "improvement" slows things down by about 5%:
 //         if (len >= 64 - bufBytes) {
 //             System.arraycopy(data, from, buffer, bufBytes, 64 - bufBytes);
 //             _fastCycle(buffer, 0);
 //             bufBytes = 0;
 //         } else {
 //             System.arraycopy(data, from, buffer, bufBytes, len);
 //             bufBytes += len;
 //         }
            // try to fill the buffer
            while (bufBytes < 64 && from < to)
                buffer[bufBytes++] = data[from++];
            if (bufBytes == 64) {
                _fastCycle(buffer, 0);
                bufBytes = 0;
            }
        }
        if (bufBytes == 0) {
            for (  ; from + 64 <= to; from += 64)
                _fastCycle(data, from);
            if (from != from + len)
                for (int i = from; i < to; i++)
                    buffer[bufBytes++] = data[i];
        }
    }
    public void update (byte[] data) {
        update(data, 0, data.length);
    }
    // SERIALIZATION ////////////////////////////////////////////////
    /**
     * @param x an arbitrary integer value
     * @return the value of the argument as a unsigned hex
     */
    protected static String intToHex (int x) {
        byte [] b = new byte[4];
        b[3] = (byte) x;    x >>= 8;
        b[2] = (byte) x;    x >>= 8;
        b[1] = (byte) x;    x >>= 8;
        b[0] = (byte) x;
        return StringLib.byteArrayToHex(b);
    }
    /**
     * Display a (subround) number followed by a hex representation of 
     * five integer values.  Useful for pretty-printing A, B, C, D, E
     * at the end of a subround.
     */
    protected static void prettyPrint (
                            int i, int v, int w, int x, int y, int z) {
        StringBuffer sb = new StringBuffer();
        if (i < 10)
            sb.append(" ");
        sb.append(i)
          .append("  ")
          .append(intToHex(v))
          .append(" ")
          .append(intToHex(w))
          .append(" ")
          .append(intToHex(x))
          .append(" ")
          .append(intToHex(y))
          .append(" ")
          .append(intToHex(z));
        System.out.println(sb.toString());
    }
    /**
     * Display the value of a 512-bit message block after expansion
     * into the first 16 values of the int[] w workspace.
     */
    protected void prettyPrintMsg() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 16;   ) {
            sb.append(intToHex(w[i++]))
              .append(" ")
              .append(intToHex(w[i++]))
              .append(" ")
              .append(intToHex(w[i++]))
              .append(" ")
              .append(intToHex(w[i++]));
            if (i < 16) 
                sb.append("\n");
        }
        System.out.println(sb.toString());
    }         
}
