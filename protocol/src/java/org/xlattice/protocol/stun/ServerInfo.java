/* ServerInfo.java */
package org.xlattice.protocol.stun;

/**
 * Information on STUN server in raw form.
 *
 * @param name  STUN server name (dotted quad or fully qualified domain name)
 * @param port  primary STUN port
 * @param isUDP if true, udp server, otherwise tls/tcp server
 */
public class ServerInfo {
    public final String  name;
    public final int     port;
    public final boolean isUDP;
    public ServerInfo (String n, int p, boolean udp) {
        name = n;
        port = p;
        isUDP = udp;
    }
    public String toString() {
        return new StringBuffer(name)
            .append(':')
            .append(port)
            .append(' ')
            .append( (isUDP ? "udp" : "tls/tcp") )
            .toString();
    }  
}
        
    
