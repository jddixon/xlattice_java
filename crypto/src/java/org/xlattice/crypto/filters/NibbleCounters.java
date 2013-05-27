/* NibbleCounters.java */
package org.xlattice.crypto.filters;

/** 
 * As it stands, this class is not thread-safe.  Using classes are
 * expected to provide synchronization.
 *
 * @author Jim Dixon
 */
class NibbleCounters {

    private short [] counters;
    
    public NibbleCounters (int filterInts) {
        counters = new short[filterInts * 8];
    }

    public final void clear() {
        for (int i = 0; i < counters.length; i++) {
            counters[i] = (short) 0;
        }
    }
    /** 
     * Increment the nibble, ignoring any overflow
     * @param filterWord offset of 32-bit word 
     * @param filterBit  offset of bit in that word (so in range 0..31)
     * @return value of nibble after operation
     */
    public final int inc (int filterWord, int filterBit) {
        int counterShort = 8 * filterWord + filterBit / 4;
        int counterCell  = filterBit % 4;
        int shiftBy      = counterCell * 4;
        int cellValue    = 0xf & (counters[counterShort] >> shiftBy);
        if (cellValue < 15) {
            cellValue++;
        }
        // mask off the nibble and then OR new value in
        // TABLE LOOKUP MORE EFFICIENT
        counters[counterShort] &= ((~0xf) << shiftBy);
        counters[counterShort] |= (cellValue << shiftBy);
        return cellValue;
    }
    /** 
     * Decrement the nibble, ignoring any overflow
     * @param filterWord offset of 32-bit word 
     * @param filterBit  offset of bit in that word (so in range 0..31)
     * @return value of nibble after operation
     */
    public final int dec (int filterWord, int filterBit) {
        int counterShort = 8 * filterWord + filterBit / 4;
        int counterCell  = filterBit % 4;
        int shiftBy      = counterCell * 4;
        int cellValue    = 0xf & (counters[counterShort] >> shiftBy);
        if (cellValue > 0) {
            cellValue--;
        }
        // mask off the nibble and then OR new value in
        counters[counterShort] &= ((~0xf) << shiftBy);
        counters[counterShort] |= (cellValue << shiftBy);
        return cellValue;
    }
}
