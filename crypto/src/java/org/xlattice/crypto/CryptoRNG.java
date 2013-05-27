/* CryptoRNG.java */
package org.xlattice.crypto;

import java.security.SecureRandom; 

/**
 * Random number generator with crypto-level security.
 * 
 * Wherever a value is described as "random" in the comments on
 * methods, it should be understood that the value is a pseudo-
 * random value and that the set of such values is very nearly
 * uniformly distributed over the interval in question, except
 * that Gaussian values have a distribution which is very nearly
 * Gaussian.
 * 
 * This should be one of two classes implementing an RNG interface,
 * with the other class offering a lower-quality (and therefore 
 * cheaper) pseudo-random number sequence.
 *
 * XXX This implementation is the thinnest of wrappers around the 
 * XXX JCA SecureRandom class. 
 *
 * @author Jim Dixon
 */
public class CryptoRNG {

    private final SecureRandom rng;
    
    public CryptoRNG() {
        rng = new SecureRandom();
    }
    public CryptoRNG(byte[] seed) {
        rng = new SecureRandom(seed);
    }
    public boolean nextBoolean() {
        return rng.nextBoolean();
    }
    /**
     * Fills the byte array with random bytes.
     */
    public void nextBytes (byte[] b) {
        if (b != null)
            rng.nextBytes(b);
    }
    /** @return a random value between 0.0 inclusive and 1.0 exclusive */
    public double nextDouble() {
        return rng.nextDouble();
    }
    /** @return a random value between 0.0 inclusive and 1.0 exclusive */
    public float nextFloat() {
        return rng.nextFloat();
    }
    /**
     * Returns a random value from a Gaussian sequence with mean 0.0 
     * and standard deviation 1.0.
     */
    public double nextGaussian() {
        return rng.nextGaussian();
    }
    public int nextInt() {
        return rng.nextInt();
    }
    /**
     * Returns a pseudo-random value between 0 (inclusive) and 
     * n (exclusive).
     */
    public int nextInt(int n) {
        return rng.nextInt(n);
    }
    public long nextLong() {
        return rng.nextLong();
    }
    /**
     * Reseed the random number generator using the binary value passed.
     * This is guaranteed not to reduce the randomness of the generator.
     * The new seed value affects but does not reset it.
     */
    public void reseed (byte[] seed) {
        rng.setSeed(seed);
    }
}
