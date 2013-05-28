/* Udp.java */
package org.xlattice.transport.udp;

import org.xlattice.Transport;

/**
 * XXX How does this relate to PacketPort?
 */
public class Udp                                implements Transport {


    public Udp() {
    }


    public String name() { 
        return "udp";
    }
}
