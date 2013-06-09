/* HostHeader.java */
package org.xlattice.httpd.headers;

import org.xlattice.httpd.Header;
import org.xlattice.httpd.RequestHeader;
import org.xlattice.httpd.MalformedHeaderException;

/**
 * XXX This isn't a AbstractText, because of the optional :portNumber.
 *
 * @author Jim Dixon
 */
public class HostHeader extends Header implements RequestHeader {
    private final String host;
    private final int port;

    public HostHeader (String hostName)     
                                    throws MalformedHeaderException {
        int colonAt = hostName.indexOf(":");
        if (colonAt == -1) {
            host = hostName;
            port = 80;
        } else if (colonAt > 1) {
            host = hostName.substring(0, colonAt);
            try {
                port = Integer.parseInt(hostName.substring(colonAt + 1));
            } catch (NumberFormatException nfe) {
                throw new MalformedHeaderException ("not a proper number: "
                        + nfe);
            } catch (IndexOutOfBoundsException ioobe) {
                throw new MalformedHeaderException("missing port number: "
                        + ioobe);
            }
        } else {
            throw new MalformedHeaderException("missing host name: "
                    + hostName);
        }
        checkPort(port);
    }
    public HostHeader (String hostName, int portNumber) 
                                    throws MalformedHeaderException {
        if (hostName == null || hostName.equals(""))
            throw new MalformedHeaderException (
                    "null or empty host name");
        host = hostName;
        checkPort(portNumber);
        port = portNumber;
    }
    // ARGUMENT CHECKERS ////////////////////////////////////////////
    private void checkPort (int port)   
                                    throws MalformedHeaderException {
        if (port < 1 | port > 65535)
            throw new MalformedHeaderException("invalid port number " 
                    + port);
    }
    // INTERFACE AbstractHeader /////////////////////////////////////
    public String getTag () {
        return "Host";
    }
    public String toString() {
        StringBuffer sb = new StringBuffer("Host: ").append(host);
        if (port != 80) 
            sb.append(":").append(port);
        return sb.append("\n").toString();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
}
