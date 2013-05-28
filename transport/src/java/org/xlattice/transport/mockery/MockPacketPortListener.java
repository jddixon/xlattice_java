/* MockPacketPortListener.java */
package org.xlattice.transport.mockery;

import java.nio.ByteBuffer;

import org.xlattice.Address;
import org.xlattice.transport.PacketPortListener;
import org.xlattice.transport.SchPacketPort;

/**
 * @author Jim Dixon
 */

public class MockPacketPortListener implements PacketPortListener {

    public void setPacketPort (SchPacketPort ppt, ByteBuffer buffer){
    }
    public Address dataSentTo (){
        return null;
    }    
    public void dataSent (){
    }    
    public Address dataReceivedFrom (){
        return null;
    }    
    public void dataReceived (){
    }
    public void reportException (Exception exc){
    }
}
