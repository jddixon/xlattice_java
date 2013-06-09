/* HttpSListener.java */
package org.xlattice.httpd;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.StringTokenizer;

/**
 * @author Jim Dixon
 */

import org.xlattice.httpd.headers.*;
import org.xlattice.overlay.CallBack;
import org.xlattice.overlay.GetCallBack;
import org.xlattice.overlay.PutCallBack;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.SchedulableConnection;
import org.xlattice.util.NonBlockingLog;
import org.xlattice.util.Timestamp;

/**
 * Constructs an HTTP server connection listener.  Each
 * SchedulableConnection created by an HTTP SchedulableAcceptor will
 * have one of these attached to it.
 *
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class HttpSListener extends HttpParser
                implements ConnectionListener, HttpSCallBacks  {

    private static String serverName = "server name has not been set";

    // PRIVATE MEMBERS //////////////////////////////////////////////
    private Name2Hash name2Hash  = Name2Hash.getInstance();
    private SiteList  siteList   = name2Hash.getSiteList();

    private NonBlockingLog accessLog;

    private ByteBuffer dataIn = ByteBuffer.allocate(HTTP_BUFSIZE);
    
    private        String callerIP;
    private        int    callerPort;
    /** preceding in String form */
    private        String clientID;

    private        boolean dataPending;

    private        int     method;
    private        String  absPath;
    private        int     status;

    // XXX THIS IS A HACK XXX Belongs in HttpParser
    private        String  contentType = "text/plain"; // asc txt text diff pot

    // CONSTRUCTORS /////////////////////////////////////////////////
    public HttpSListener() {
        super("error.log", "debug.log");
        index = counter++;          // instance index
        setAccessLog("access.log");
    }
    // LOGGING //////////////////////////////////////////////////////
    public void setAccessLog (String name) {
        if (accessLog != null)
            throw new IllegalStateException(
                    "can't change access log name");
        if (name != null)
            accessLog   = NonBlockingLog.getInstance(name);
    }
    protected void accLog(String msg) {
        if (accessLog != null)
            accessLog.message(msg);
    }
    protected void DEBUG_MSG(String msg) {
        if (debugLog != null)
            debugLog.message("HttpSListener[" + index + "]" + msg);
    }
    protected void ERROR_MSG(String msg) {
        if (errorLog != null)
            errorLog.message("HttpSListener[" + index + "]" + msg);
    }
    // INTERFACE ConnectionListener /////////////////////////////////
    public void setConnection (SchedulableConnection cnx, ByteBuffer buffer) {
        if (cnx == null || buffer == null)
            throw new IllegalArgumentException ("null connection or buffer");
        this.cnx = cnx;
        cnxInBuf   = buffer;

        // get information about the client
        sChan       = (SocketChannel)cnx.getChannel();
        Socket sock = sChan.socket();
        // XXX we need the numeric IP address for filtering XXX
        callerIP    = sock.getInetAddress().toString();
        int slashAt = callerIP.indexOf('/');
        if (slashAt >= 0)   // XXX weird possibilities ignored
            callerIP = callerIP.substring(slashAt + 1);
        callerPort  = sock.getPort();
        clientID    = new StringBuffer(callerIP)
                        .append(":").append(callerPort).toString();
        cnx.initiateReading();
    }

    /* private */ int curByte;        // 0
    /* private */ boolean haveCR;     // false
    /* private */ boolean haveCRLF;   // false

    /**
     * IOScheduler signals that we have data from the client.  
     *
     * XXX This implementation cannot cope with multiple packets
     * XXX received on the same connection.
     */
    public void dataReceived () {
        cnxInBuf.flip();
        dataIn.put(cnxInBuf);
        cnxInBuf.clear();
        
        // DEBUG
        DEBUG_MSG(".dataReceived():\n" +
                new String(dataIn.array(), 0, dataIn.limit())
                + "\n");
        // END

        // parse request line /////////////////////////////
        status  = 200;
        try {
            request = parseRequestLine(dataIn);
        } catch (NotImplementedException nfe) {
            ERROR_MSG(clientID + ": " + nfe);
            status = 501;
        } catch (MessageFormatException mfe) {
            ERROR_MSG (clientID + ": " + mfe);
            status = 400;
        }
        if (request == null) {
            _close();
            return;
        }
        method  = request.getMethod();
        absPath = request.getURI();
        // XXX THIS SHOULD BE CONFIGURABLE XXX
        if (absPath.equals("/"))
            absPath = "/index.html";
        version = request.getHttpVersion();
        if (version == V0_9 && status != 200) {
            _close();
            return;
        }
        // parse headers //////////////////////////////////
        // DEBUG_MSG(" about to parse headers, status = " + status);
        if (version > V0_9) {
            try {
                collectHeaders (request, dataIn);
//          } catch (NotImplementedException nfe) {
//              ERROR_MSG(clientID + ": " + nfe);
//              status = 501;
            } catch (MessageFormatException mfe) {
                ERROR_MSG (clientID + ": " + mfe);
                status = 400;
            }
        }
        // get any entity /////////////////////////////////
        // DEBUG_MSG(" about to look for entity, status = " + status);
        if (version > V0_9 && status == 200) {
            try {
                skipEOL(dataIn);
                collectEntity (request, dataIn);
                dataIn.clear();
            } catch (MessageFormatException mfe) {
                ERROR_MSG (clientID + " can't collect entity: " + mfe);
                status = 400;
            }
        }
        // locate file ////////////////////////////////////
        //DEBUG_MSG(" about to look for file, status = " + status);
        String mySite = null;
        if (version == V0_9) {
            if (siteList == null) {
                mySite = "www.siteA.com";       // GROAN XXX A HACK
            } else {
                mySite = siteList.toString(0);
                DEBUG_MSG(" using " + mySite + " from site list");

            }
        } else {
            if (host == null) {
                ERROR_MSG(clientID + " 1.x but no Host header");
                status = 400;       // no host specified
            } else {
                mySite = host;
                DEBUG_MSG(" using " + mySite + " from Host header");
            }
        }

        // log access /////////////////////////////////////
        accLog(new StringBuffer(clientID)
                            .append(" ")
                            .append(HttpParser.METHODS[method])
                            .append(" ")
                            .append(absPath)
                            .toString());
        // check for availability /////////////////////////
        // DEBUG_MSG(" ready for availability check; status = " + status);
        byte[] data = null;
        if (status == 200 && (method == HTTP_GET || method == HTTP_HEAD
                           /* method == HTTP_POST */ )) {
            DEBUG_MSG(" calling name2Hash.get on:\n    " + mySite + absPath);
            name2Hash.get(mySite +          // XXX HACK ;-)
                          absPath,   this); // this is callback
        } else 
            buildResponse(null);
    }
    /**
     * Maps callback status into appropriate HTTP status code, 
     * sending byte array forward if getStatus is OK.
     * 
     * @param getStatus callback status code
     * @param data      byte array returned by get
     */
    public void finishedGet (int getStatus, byte[] data) {
        DEBUG_MSG(".finishedGet: status = " + getStatus 
                + (data == null ? ", null data" : ", have data"));

        if (getStatus == CallBack.OK) {
            if (data == null) {
                wrappedEntity = null;
                ERROR_MSG(" INTERNAL ERROR: callback OK but null data"); 
                status = 404;
            } else {
                wrappedEntity = ByteBuffer.wrap(data);
                dataPending = true;     // we have an entity to send
                DEBUG_MSG(".finishedGet: dataPending := true");
            }
            buildResponse (data);
        } else {
            switch (getStatus) {
                case CallBack.NOT_FOUND: 
                    status = 404; 
                    break;
                case CallBack.NOT_IMPLEMENTED:
                    status = 501;
                    break;
                default: 
                    ERROR_MSG("finishedGet: UNEXPECTED call back status " 
                            + getStatus);
                    status = 500;  // server error
            }
            buildResponse (null);
        }
    }
    // BUILD RESPONSE ///////////////////////////////////////
    
    protected void buildResponse (byte[] data) {
        DEBUG_MSG(".buildResponse");
        if (version == V0_9 && data == null) {
            _close();
            return;
        }
        // construct response /////////////////////////////
        try {
            if (version == V0_9)
                response = new HttpResponse();
            else
                response = new HttpResponse (statusLine(version, status));
            response.setEntity(data);    // may be null
            if (data != null && data.length > 0)
                DEBUG_MSG(" added entity to response, bytes; "
                                                + data.length);
            else
                DEBUG_MSG(" set response entity to null");
        } catch (MessageFormatException mfe) {
            /* assumed impossible */
            ERROR_MSG( " internal error? " + mfe);
            status = 500;
        } catch (IllegalParserStateException ipse) {
            ERROR_MSG( " INTERNAL ERROR: " + ipse);
            status = 500;
        }
        // send headers and entity ////////////////////////
        if (version == V0_9) {
            if (status == 200) {
                DEBUG_MSG(" sending 0.9 data");
                dataPending = false;
                cnx.sendData( wrappedEntity );
            } else {
                _close();
            }
            return;
        } else /* HTTP/1.x */ {
            if (response == null) {
                try {
                    response = new HttpResponse (
                            statusLine(version, status));
                } catch (MessageFormatException mfe) {
                    ERROR_MSG("INTERNAL ERROR: " + mfe);
                } catch (IllegalParserStateException ipse) {
                    ERROR_MSG("INTERNAL ERROR: " + ipse);
                }
            }
            // ADD HEADERS //////////////////////
            if (response == null) {
                _close();
                return;
            }
            // guess at content type
            if ( absPath != null) {
                // XXX must not be last character
                int extStarts = absPath.lastIndexOf('.');
                if (extStarts != -1 
                        && (extStarts + 1 < absPath.length())) {
                    String ext = absPath.substring(extStarts + 1);
                    if (ext.equals("html"))
                        contentType = "text/html; charset=ISO-8859-1";
                    else if (ext.equals("css"))
                        contentType = "text/css";
                    else if (ext.equals("gif"))
                        contentType = "image/gif";
                    else if (ext.equals("jpg"))
                        contentType = "image/jpeg";
                    else if (ext.equals("png"))
                        contentType = "image/png";
                }
            }
            try {
                response.addHeader((Header)new ConnectionHeader("close"));
                if (dataPending && data != null)
                    response.addHeader((Header)
                            new ContentLengthHeader(data.length));
                response.addHeader((Header)new ContentTypeHeader(contentType));
                response.addHeader((Header)new DateHeader(now));
                response.addHeader((Header)
                            new ServerHeader(serverName));
            } catch (MalformedHeaderException mhe) {
                ERROR_MSG(" INTERNAL ERROR: " + mhe);
            } catch (MessageFormatException mfe) {
                ERROR_MSG(" INTERNAL ERROR: " + mfe);
            }
            // END ADD HEADERS //////////////////
            DEBUG_MSG(" sending 1.x status + headers:\n" 
                                                + response.toString());
            try {
                cnx.sendData(
                        ByteBuffer.wrap(response.toString().getBytes()));
            } catch (java.nio.channels.CancelledKeyException cke) {
                ERROR_MSG(".buildResponse, dropping it: " + cke);
            }
        }
    }
    /**
     * IOSchedule signalling back that message to client has been
     * sent.
     *
     * In actual use, we get one dataSent() call for the header block,
     * but SIX TO SEVEN dataSent() invocations for the entity.
     */
    public void dataSent () {
        DEBUG_MSG(".dataSent(): dataPending = " + dataPending);
        byte[] data = response.getEntity();
        // DEBUG
        if (data == null)
            DEBUG_MSG(".dataSent(): null entity");
        // END
        // KLUDGE
        if (wrappedEntity == null || !wrappedEntity.hasRemaining())
            data = null;
        // END
        if (version != V0_9 && dataPending && data != null) {
            DEBUG_MSG("sending 1.x data");
            dataPending = false;
            cnx.sendData(wrappedEntity);
        } else {
            // XXX HACK! 
            try {
                Thread.currentThread().sleep(1);
            } catch (InterruptedException ie) { }
            // END HACK
            DEBUG_MSG("message sent, closing");
            _close();
        }
    }

    public void reportDisconnect () {
        ERROR_MSG(new StringBuffer(" unexpected disconnection by client ")
                            .append(callerIP)
                            .append(":")
                            .append(callerPort)
                            .toString());
        _close();
    }
    public void reportException (Exception exc) {
        ERROR_MSG(new StringBuffer(callerIP)
                            .append(":")
                            .append(callerPort)
                            .append(" unexpected exception: ")
                            .append(exc.toString())
                            .toString());
        _close();
    }
    // INTERFACE HttpSCallBacks /////////////////////////////////////
    // PROPERTIES ///////////////////////////////////////////////////
    public static void setServerName(String s) {
        if (s == null || s.equals(""))
            throw new IllegalArgumentException (
                    "null or empty server name");
        serverName = s;
    }
    public String getPath () {
        return absPath;
    }
    public String getMethod () {
        return HttpParser.METHODS[method];
    }
    public int getStatus () {
        return status;
    }
    // PARSER METHODS ///////////////////////////////////////////////
    /**
     * Given a ByteBuffer holding a client request, constructs a
     * data structure populated with the results of a parse.
     *
     * The ByteBuffer should have been flipped.
     *
     * XXX The URI may have been %XX encoded (RFC 2396), needs to be
     * XXX decoded here.
     */
    protected HttpRequest parseRequestLine(ByteBuffer buf)
            throws MessageFormatException, NotImplementedException {
        if (buf == null)
            throw new IllegalArgumentException ("null in buffer");
        int     curByte  = 0;
        byte[]  b        = buf.array();
        int     requestVersion;
        HttpRequest request;

        // request line:
        //   METHOD ws URI [ws HTTP/1.x] EOL
        int start  = curByte;
        curByte    = skipToW(b, curByte);
        String reqMethod = new String(b, start, curByte - start);
        curByte    = skipW(b, curByte);
        start      = curByte;
        curByte    = skipToWorEOL (b, curByte);
        String uri = new String(b, start, curByte - start);

        if (isEOL(b[curByte])) {
            requestVersion = V0_9;
            if (!reqMethod.equals("GET")) {
                throw new NotImplementedException(
                        "HTTP/0.9 but method is " + reqMethod);
            }
            request = new HttpRequest(HTTP_GET, uri, requestVersion );
        } else {
            curByte = skipW(b, curByte);
            curByte = expect ("HTTP/1.", b, curByte);
            char c  = castByte(b[curByte++]);
            if (c == '1')
                requestVersion = V1_1;
            else if (c == '0')
                requestVersion = V1_0;
            else
                throw new MessageFormatException( "invalid version 1."
                        + c);
            curByte = skipEOL(b, curByte);
            Integer whichMethod = (Integer)methodMap.get(reqMethod);
            if (whichMethod == null)
                throw new NotImplementedException ("invalid method "
                        + reqMethod);
            request = new HttpRequest(
                            whichMethod.intValue(), uri, requestVersion);
            buf.position(curByte);
        }
        return request;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    private void _close() {
        /** Close the socket and cancel the selection key. */
        cnx.getKey().cancel();
        try {
            sChan.close();
        } catch (IOException ioe) { /* just ignore it */ }
    }
}
