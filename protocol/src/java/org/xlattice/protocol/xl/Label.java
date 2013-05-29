/* Label.java */
package org.xlattice.protocol.xl;

/**
 * Dunno how to use this yet.
 *
 * @author Jim Dixon
 */
public class Label {

    // CONSTANTS ////////////////////////////////////////////////////
    /* but the value length is 4 */
    protected static final int LABEL_LENGTH =  8;
    protected static final int VALUE_SHIFT  = 12;
    protected static final int UNSHIFTED_VALUE_MASK = 0xfffff000;
    protected static final int SHIFTED_VALUE_MASK   = 0x000fffff;

}
