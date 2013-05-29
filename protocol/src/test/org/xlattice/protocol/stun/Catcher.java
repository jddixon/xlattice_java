/* Catcher.java */
package org.xlattice.protocol.stun;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.xlattice.util.StringLib;

/**
 * Collects a specified number of UDP messages (StunMsgs) sent
 * to an address.
 */
public class Catcher                        extends Thread {
    public int waitFor;
    public int received;
    public InetAddress myAddr;
    public int myPort;
    public volatile boolean running;
    public Hashtable messages; 
    public Thread catcherThread;

    public Catcher(InetAddress addr, int port, int count) 
                                            throws Exception {
        myAddr  = addr;
        myPort  = port;
        waitFor = count;
        messages = new Hashtable(waitFor);

        start();
    }
    public void run() {
        catcherThread = Thread.currentThread();
        DatagramSocket socket = null;
        try { 
            socket = new DatagramSocket(myPort, myAddr);
            socket.setReuseAddress(true);
            byte[] inBuf = new byte[256];
            DatagramPacket inPkt = new DatagramPacket (inBuf, 256);
            running = true;
            for (int i = 0; i < waitFor; i++) {
                socket.receive(inPkt);
                StunMsg msg = StunMsg.decode(inBuf);
                byte[] msgID = msg.getMsgID();
                put (msgID, msg);
            }
        } catch (Throwable t) { /* ignore */ }
        running = false;
    }
    public StunMsg get(byte[] msgID) {
        synchronized (messages) {
            return (StunMsg)messages.get(new MessageID(msgID));
        }
    }
    public Thread getThread() {
        return catcherThread;
    }
    public void put (byte[] id, StunMsg msg) {
        synchronized (messages) {
            messages.put( new MessageID(id),   msg);
            // WITH OR WITHOUT THE SYNC, THIS LOCKS EVERYTHING UP
            // assertNotNull( messages.get( new MessageID(msgID) ) );
        }
    }
    public void showKeys() {
        System.out.println("message IDs found:");
        synchronized (messages) {
            Set keys = messages.keySet();
            Iterator it = keys.iterator();
            while (it.hasNext()) {
                byte[] msgID = ((MessageID)it.next()).id;

                System.out.println("    " 
                + StringLib.byteArrayToHex(msgID, 0, 
                    StunConst.MSG_ID_LENGTH));
            }
        }
    }
    public int size() {
        return messages.size();
    } 
}
