/* Killable.java */
package org.xlattice.util.threads;

/** 
 * Interface implemented by threads handling queued jobs.
 *
 * @author Jim Dixon
 */
interface Killable {

    /** 
     * Terminate this thread in an orderly manner.  
     */
    public void die();

}
