/* AddrAttr.java */
package org.xlattice.protocol.stun;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.xlattice.protocol.TLV16;
import org.xlattice.util.UIntLib;

/**
 * Superclass for attributes carrying IPv4 address and ports.
 * Quantities in the value byte array are construed as big-endian
 * unsigned integers.  Bytewise:
 *
 *  +-----+-----+----+----+
 *  |  0  |  1  |   port  |
 *  +-----+-----+----+----+
 *  |     IPv4 address    |
 *  +-----+-----+----+----+
 *
 * @author Jim Dixon
 */
public abstract class AddrAttr extends StunAttr {

    // INSTANCE VARIABLES ///////////////////////////////////////////
    // XXX We are now storing the same data twice
    private final Inet4Address addr;
    private final int port;
   
    // CONSTRUCTORS /////////////////////////////////////////////////
    public AddrAttr( int type, Inet4Address ipv4Addr, int port ) {
        super(type, new byte[8]);

        if (ipv4Addr == null)
            throw new IllegalArgumentException("null address");
        if (port < 0 || 65535 < port)
            throw new IllegalArgumentException("port out of range");
        addr = ipv4Addr;
        this.port = port;
        
        // THIS IS NOW REDUNTANT; dunno if it should be retained
        value[1] = 1;           // family: IPv4
        value[2] = (byte)(0xff & (port >> 8));
        value[3] = (byte)(0xff & port);
        System.arraycopy( ipv4Addr.getAddress(), 0, value, 4, 4);
    }

    /**
     * Type and length (=8) have already been decoded.  The offset
     * is to the data structure depicted above.
     */
    protected static TLV16 decodeValue (int type, byte[] message, int offset) 
                                            throws UnknownHostException {
//      // DEBUG
//      System.out.println("AddrAttr.decodeValue: type = " + type 
//              + ", offset = " + offset + ", family = " 
//              + message[offset] + message[offset + 1]);
//      // END
        if (message[offset++] != 0 || message[offset++] != 1)
            throw new IllegalStateException(
                    "address attribute has wrong family");
        int _port  = UIntLib.decodeUInt16(message, offset);
        offset += 2;
        byte[] _addr = new byte[4];
        System.arraycopy (message, offset, _addr, 0, 4);
        Inet4Address v4Addr = 
                (Inet4Address) InetAddress.getByAddress(_addr);
        switch (type) {
            case MAPPED_ADDRESS: 
                return new MappedAddress(v4Addr, _port);
            case RESPONSE_ADDRESS:
                return new ResponseAddress(v4Addr, _port);
            case SOURCE_ADDRESS:
                return new SourceAddress(v4Addr, _port);
            case CHANGED_ADDRESS:
                return new ChangedAddress(v4Addr, _port);
            case REFLECTED_FROM:
                return new ReflectedFrom(v4Addr, _port);
                
            case XOR_MAPPED_ADDRESS:
                return new XorMappedAddress(v4Addr, _port);
            case SECONDARY_ADDRESS:
                return new SecondaryAddress(v4Addr, _port);
                
            default:
                throw new IllegalStateException(
                   "unknown address attribute " + type);
        }
    }
    // ACCESS METHODS ///////////////////////////////////////////////
    public Inet4Address getAddress() {
        return addr;
    }
    public int getPort() {
        return port;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        switch (type) {
            case MAPPED_ADDRESS: 
                sb.append("MappedAddress:    ");       break;
            case RESPONSE_ADDRESS:
                sb.append("ResponseAddress:  ");     break;
            case SOURCE_ADDRESS:
                sb.append("SourceAddress:    ");       break;
            case CHANGED_ADDRESS:
                sb.append("ChangedAddress:   ");      break;
            case REFLECTED_FROM:
                sb.append("ReflectedFrom:    ");       break;
                
            // XXX extensions
            case XOR_MAPPED_ADDRESS:
                sb.append("XorMappedAddress: ");    break;
            case SECONDARY_ADDRESS:
                sb.append("SecondaryAddress: ");    break;
                
            default:
                throw new IllegalStateException(
                   "unknown address attribute " + type);
        }
        sb.append (addr.toString())
          .append (':')
          .append (port);
        return sb.toString();
    }
}
