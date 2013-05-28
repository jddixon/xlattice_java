/* IOScheduler.java */
package org.xlattice.transport;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.Iterator;

import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

import org.xlattice.*;
import org.xlattice.util.NonBlockingLog; 
import org.xlattice.util.Queue; 
import org.xlattice.util.Stack; 

/**
 *
 * @author Jim Dixon
 */
public class IOScheduler implements Runnable {
    // DEBUG
    protected final NonBlockingLog log 
                        = NonBlockingLog.getInstance("scheduler.log");
    protected void LOG_MSG(String s) {
        log.message(s);
    }
    // END
 
    private final Selector       selector;
    private final AttachmentPool aPool;    
    private final Thread         myThread;
    
    /** queue of pending Runnables */
    private Queue pending = new Queue();
    
    private volatile boolean haltNotRequested;
    private volatile boolean running;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    public IOScheduler ()                   throws IOException {
        selector = Selector.open();
        aPool    = new AttachmentPool();    // default max size
        myThread = new Thread(this);
        myThread.start();
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** exposed for testing, drop ASAP */ 
    public AttachmentPool getPool() {
        return aPool;
    }
    /** 
     * XXX May not want to expose this.
     */
    public Selector getSelector() {
        return selector;
    }
    /** For testing. */
    public Thread getThread() {
        return myThread;
    }
    /**
     * Returns the value of haltNotRequested; if it is false, a stop has been
     * requested.  This does not mean that the thread has actually
     * stopped haltNotRequested.
     */
    public synchronized boolean haltNotRequested() {
        return haltNotRequested;
    }
    public synchronized boolean isRunning() {
        return running;
    }
    // INTERFACE Runnable /////////////////////////////////////////// 
    public void run() {
        // LOG_MSG(": run()");
        running = true;
        haltNotRequested = true;
        while (haltNotRequested) {
            // run any pending jobs ///////////////////////
            synchronized(pending) {
                int count = pending.size();
                for (int i = 0; i < count; i++) {
                    LOG_MSG(".run(), running job " + i);
                    ((Runnable)pending.dequeue()).run();
                }
            }
            if (!haltNotRequested)  // may have been cleared by pending job
                break;
            // block waiting for I/O //////////////////////
            int keyCount = 0;
            try {
                keyCount = selector.select();
            } catch (IOException ioe) {
                /* XXX OTHERWISE IGNORE FOR NOW */
                continue;
            }
            StringBuffer sb = new StringBuffer()
                .append(" after select, keyCount is ")
                .append(keyCount)
                .append(" out of ")
                .append(selector.keys().size())
                .append(", ");
            if (haltNotRequested)
                sb.append("active");
            else
                sb.append("dying");
            LOG_MSG(sb.toString());

            if (keyCount == 0)
                continue;
            // handle the I/O events //////////////////////
            Iterator it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey sk = (SelectionKey)it.next();
                it.remove();
                if (!sk.isValid()) {
                    continue;
                }
                Attachment attachment = (Attachment)sk.attachment();
                LOG_MSG("attachment " + attachment.index);
                final int aType = attachment.type;
                if (attachment == null) {
                    LOG_MSG("** NULL ATTACHMENT **");
                    continue;
                }
                if (aType == Attachment.NIX_A) {
                    LOG_MSG("** ATTACHMENT HAS NIX_A TYPE, cancelling key **");
                    sk.cancel();
                    continue;
                }

                ///////////////////////////////////////////////////// 
                // TYPE == ACC_A ////////////////////////////////////
                ///////////////////////////////////////////////////// 
                if (sk.isAcceptable()) {
                    if (aType != Attachment.ACC_A) {
                        // DEBUG
                        // END
                        continue;
                    }
                    ServerSocketChannel srvChan 
                            = (ServerSocketChannel)sk.channel();
                    if (srvChan == null) {
                        continue;
                    }
                    Acceptor acceptor = (Acceptor)attachment.obj;
                    if (acceptor == null) {
                        // XXX CONSIDER sk.cancel();
                        continue;
                    }
                    try {
                        acceptor.accept();      // this does the adding too
                    } catch (IOException ioe) {
                        /* ignore, at least for now */
                    }
                    continue;
                }
                ///////////////////////////////////////////////////// 
                // OP_CONNECT SET ///////////////////////////////////
                ///////////////////////////////////////////////////// 
                // whether sk is ready to complete a connection or has
                // a (related?) error pending
                if (sk.isConnectable()) {
                    SocketChannel sChan = (SocketChannel)sk.channel();
                    if (sChan == null) {
                        // LOG_MSG(": connectable key is null");
                        continue;
                    }
                    if (sChan.isBlocking()) {
                        try {
                            sChan.configureBlocking(false);
                        } catch (IOException ioe) {
                            /* ignore */
                        }
                    } 

                    Object obj = attachment.obj;
                    if (obj == null) {
                        continue;
                    } 
                    // I: TYPE == CTR /////////////////////
                    if (aType == Attachment.CTR_A) {
                    //if (obj instanceof SchedulableConnector) {
                        // LOG_MSG("  attachment is SchedulableConnector");
                        SchedulableConnector ctr 
                                            = (SchedulableConnector) obj;
                        SchedulableConnection cnx;
                        try { 
                            cnx = ctr.connection();
                        } catch (IOException ioe) {
                            System.err.println(
                                    "error creating connection: " + ioe);
                            continue;
                        }
                        // reuse the attachment XXX
                        attachment.type = Attachment.CNX_A;
                        attachment.obj  = cnx;
                        sk.attach(attachment);      // pending connection
                        if (sChan.isConnected()) {
                            cnx.setKey(sk);         // starts I/O
                        }
                        continue;
                    // II: TYPE == CNX /////////////////////
                    } else {
                        if ( aType != Attachment.CNX_A)
                            throw new IllegalStateException(
                                "unexpected attachment type " + aType);
//                      if (!(obj instanceof SchedulableConnection)) {
//                          // XXX Not very graceful
//                          throw new IllegalStateException (
//                              "unexpected attachment type: " + obj);
//                      }
                        // SchedulableConnection
                        // LOG_MSG("  attachment is SchedulableConnection");
                        SchedulableConnection cnx 
                                        = (SchedulableConnection) obj;
                        if (sChan.isConnected()) {
                            // LOG_MSG("  connected: calling cnx.setKey");
                            cnx.setKey(sk);         // start I/O
                            continue;
                        }
                        else if (sChan.isConnectionPending()) {
                            // LOG_MSG("  connection pending");
                            try {
                                if (!sChan.finishConnect()) {
                                    // LOG_MSG("    finishConnect failed");
                                    continue;
                                }
                            } catch (IOException ioe) {
                                // quiet despair
                                continue;
                            }
                            cnx.setKey(sk);
                            continue;
                        } else {
                            throw new IllegalStateException (
            "Connector yielded unconnected Connection, no Connection pending");
                        }           
                    }
                    // III: TYPE == PKT ///////////////////

                    /* STUB -- allow for connected PacketPorts */
                    
                ///////////////////////////////////////////////////// 
                // isReadable OR isWritable /////////////////////////
                ///////////////////////////////////////////////////// 
                } else {
                    // OP_READ or OP_WRITE set //////////////////////
                    if (aType == Attachment.CNX_A) {
                        // XXX sChan is not used
                        SocketChannel sChan = (SocketChannel)sk.channel();
                        if (sChan == null) {
                            continue;
                        }
                        // END XXX
                        SchedulableConnection cnx 
                                = (SchedulableConnection)attachment.obj;
                        if (cnx == null) {
                            continue;
                        }
                        if (sk.isReadable()) {
                            cnx.readyToRead(/*sk*/);
                            continue;   // HANGS if not present
                        }
                        if (sk.isWritable()) {
                            cnx.readyToWrite(/*sk*/);
                        }
                        // OTHERWISE ?
                        
                    } else if (aType == Attachment.PKT_A) {
                        // XXX as above, not used
                        DatagramChannel dChan = (DatagramChannel)sk.channel();
                        if (dChan == null)
                            continue;
                        SchPacketPort spp 
                                = (SchPacketPort)attachment.obj;
                        if (spp == null) {
                            continue;
                        }
                        if (sk.isReadable()) {
                            spp.readyToRead();
                            continue;
                        }
                        if (sk.isWritable()) {
                            spp.readyToWrite();
                            continue;
                        }
                        // OTHERWISE ?
                    }
                }
            }
        }
        // LOG_MSG(": closing selector");
        try {
            selector.close();
        } catch (IOException ioe) { /* ignore */ }
        // LOG_MSG(": setting running to false");
        running = false;
    } 
    // PENDING JOBS /////////////////////////////////////////////////
    public void addRunnable (Runnable nonBlocking) {
        synchronized (pending) {
            pending.enqueue(nonBlocking);
        }
        // LOG_MSG(".addRunnable(): wakeup selector");
        selector.wakeup();
    }
    /**
     * Stop the scheduler; this call should block.
     * 
     * XXX This implementation might fail to execute pending jobs before
     * XXX terminating.
     */
    public void close () {
        if (Thread.currentThread() == myThread) {
            // LOG_MSG(".close(), clearing haltNotRequested directly");
            haltNotRequested = false;
            selector.wakeup();
        } else {
            // LOG_MSG(".close(), running haltNotRequested job");
            addRunnable (
                new Runnable() { 
                    public void run() { 
                        haltNotRequested = false; 
                    }
                });
            if (myThread != null) 
                try {
                    myThread.join();
                } catch (InterruptedException ie) { /* ignore */ }
        }
        while (selector != null && selector.isOpen()) 
            try {
                selector.close();
            } catch (IOException ioe){ /* ignore */ }
        running = false;
    }
    // ADDING CHANNELS //////////////////////////////////////////////

    // ACCEPTORS //////////////////////////////////////////
    /**
     * Try to add an Acceptor, closing it if there is any problem
     * during the add.
     */
    private void addAcceptor(SchedulableAcceptor acc) {
        try {
            acc.setKey(acc.getChannel().register(
                    selector, 
                    SelectionKey.OP_ACCEPT,
                    aPool.get(Attachment.ACC_A, acc)));
        } catch (Exception e) {
            System.err.println ("couldn't add Acceptor: " + e);
            e.printStackTrace();
            try {
                acc.close();
            } catch (Exception e2) { /* ignore */ }
        }
    }
    public void add (final SchedulableAcceptor acc) {
        if (Thread.currentThread() == myThread) 
            addAcceptor(acc);
        else 
            addRunnable ( new Runnable () {
                public void run () {
                    addAcceptor(acc);
                }
            });
    }
    // CONNECTIONS //////////////////////////////////////// 
    /**
     * Add the SchedulableConnection to the selector, getting
     * back a SelectionKey.  This is passed to the ConnectionListener,
     * which decides which operation(s) to enable (OP_READ or
     * OP_WRITE).
     */
    private void addConnection(SchedulableConnection cnx) {
        try {
            // no I/O operations of interest yet, so zero argument
            cnx.setKey(cnx.getChannel().register(
                    selector, 
                    0, 
                    aPool.get(Attachment.CNX_A, cnx)));
        } catch (ClosedChannelException cce) {
            // LOG_MSG(".addConnection(): ClosedChannelException");
            // XXX should log, close connection, cancel any selection key
        }
    }
    public void add (final SchedulableConnection cnx) {
        // LOG_MSG(".add(SchedulableConnection)");
        if (Thread.currentThread() == myThread) 
            addConnection(cnx);
        else 
            addRunnable ( new Runnable () {
                public void run () {
                    addConnection(cnx);
                }
            });
    } 
    // CONNECTORS /////////////////////////////////////////
    private void addConnector(SchedulableConnector conn) {
        try {
            conn.setKey(conn.getChannel().register(
                selector, 
                SelectionKey.OP_CONNECT,
                aPool.get( Attachment.CTR_A, conn)));
        } catch (ClosedChannelException cce) {
            /* ignore for now */
        }
    }
    public void add (final SchedulableConnector conn) {
        if (Thread.currentThread() == myThread) 
            addConnector(conn);
        else 
            addRunnable ( new Runnable () {
                public void run () {
                    addConnector(conn);
                }
            });
    } 
    // PACKET PORTS ///////////////////////////////////////
    /**
     * Add the SchPacketPort to the selector, getting
     * back a SelectionKey.  This is passed to the PacketPortListener,
     * which decides which operation(s) to enable (OP_READ or
     * OP_WRITE).
     */
    private void addPacketPort(SchPacketPort spp) {
        try {
            // no I/O operations of interest yet, so zero argument
            spp.setKey(spp.getChannel().register(
                    selector, 
                    0, 
                    aPool.get(Attachment.PKT_A, spp)));
        } catch (ClosedChannelException cce) {
            LOG_MSG(".addPacketPort(): ClosedChannelException");
            // XXX should log, close connection, cancel any selection key
        }
    }
    public void add (final SchPacketPort spp) {
        LOG_MSG("add(SchPacketPort)");
        if (Thread.currentThread() == myThread) 
            addPacketPort(spp);
        else 
            addRunnable ( new Runnable () {
                public void run () {
                    addPacketPort(spp);
                }
            });
    } 
}
