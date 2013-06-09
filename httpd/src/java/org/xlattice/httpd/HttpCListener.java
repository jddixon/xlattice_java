/* HttpCListener.java */
package org.xlattice.httpd;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.xlattice.transport.ConnectionListener;
import org.xlattice.transport.SchedulableConnection;
import org.xlattice.util.NonBlockingLog;

/**
 * @author Jim Dixon
 */

import org.xlattice.CryptoException;        // DEBUG
import org.xlattice.crypto.SHA1Digest;      // DEBUG
import org.xlattice.util.StringLib;         // DEBUG

/**
 * Constructs an HTTP client connection listener.  This will 
 * normally be created by a SchedulableConnector.  
 *
 * XXX THERE IS A KNOWN PROBLEM WITH MESSAGES OVER 
 * XXX    SchedulableTcpConnection.CNX_BUFSIZE, currently 64KB
 *
 * @author <A HREF="mailto:jddixon@users.sourceforge.net">Jim Dixon</A>
 */
public class HttpCListener 
                    extends HttpParser implements ConnectionListener {

    // PRIVATE MEMBERS //////////////////////////////////////////////
    private int    method;
    private String uri;           
//  private boolean inHeaderSection = true;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public HttpCListener (HttpRequest request) 
                                    throws MessageFormatException {
        this(request, HttpParser.HTTP_BUFSIZE, -1);
    }
    public HttpCListener (HttpRequest request, int maxBytes, 
                                               int debugIndex ) { // XXX
        super("error.log", "debug.log");
        index = debugIndex;              // instance index; was counter++
        if (request == null)
            throw new IllegalArgumentException("mull HttpRequest");
        this.request = request;
        uri     = request.getURI();
        version = request.getHttpVersion();
        if (maxBytes <= 0)
            throw new IllegalArgumentException("negative maxBytes");

        // XXX FUDGE FACTOR - this crudely allows for a header
        dataIn = ByteBuffer.allocate(maxBytes + 512);
        dataIn.clear();
       
        parserState = START_HEAD;
        
        DEBUG_MSG(" constructor: maxBytes = " + maxBytes 
                + ", request:\n" + request.toString());
    }
    // LOGGING //////////////////////////////////////////////////////
    protected void DEBUG_MSG(String msg) {
        if (debugLog != null)
            debugLog.message("HttpCListener[" + index + "]" + msg);
    }
    protected void ERROR_MSG(String msg) {
        if (errorLog != null)
            errorLog.message("HttpCListener[" + index + "]" + msg);
    }
    // INTERFACE ConnectionListener /////////////////////////////////
    public void setConnection (SchedulableConnection cnx, ByteBuffer buffer) {
        if (cnx == null || buffer == null)
            throw new IllegalArgumentException ("null connection or buffer");
        this.cnx = cnx;
        cnxInBuf = buffer;
        dataOut = ByteBuffer.wrap(request.getByteArray());
        cnx.sendData(dataOut);
        DEBUG_MSG(".setConnection: data sent to server");
    }
    public void dataSent () {
        cnx.initiateReading();
    }
   
    /**
     * If version 0.9, the entire response is the entityBody.
     * 
     * Otherwise, the first part of the message must be parsed,
     * then everything after the blank line is the entity body.
     *
     * XXX For the moment assume that if version 1.x, then everything
     * XXX up to the blank line must be in the first packet.
     */
    public void dataReceived () {
        cnx.getKey().interestOps(0);    // quiet, please
        cnxInBuf.flip();
        dataIn.clear();                 // WRONG, but we're desperate
        DEBUG_MSG(".dataReceived, version " + VERSIONS[version]
                + "\n    cnxInBuf.position = " + cnxInBuf.position() 
                + "\n    cnxInBuf.limit    = " + cnxInBuf.limit() 
                + "\n    starts with         " + firstTen(cnxInBuf.array())
                + "\n    dataIn.position   = " + dataIn.position()
                + "\n    dataIn.limit      = " + dataIn.limit()
                + "\n    dataIn.remaining  = " + dataIn.remaining()
                + "\n    dataIn.capacity   = " + dataIn.capacity()
                );
        int spaceAvail = dataIn.capacity() - dataIn.position();
        if (cnxInBuf.limit() <= spaceAvail) {
            DEBUG_MSG(".dataReceived (a), put " 
                                + cnxInBuf.limit() + " bytes");
            // hangs at the next statement unless we do a dataIn.clear()
            dataIn.put(cnxInBuf); 
            DEBUG_MSG(".dataReceived (a), about to clear cnxInBuf");
            cnxInBuf.clear();
            DEBUG_MSG(".dataReceived (a), cnxInBuf cleared");
        } else {
            DEBUG_MSG(".dataReceived (b), put " 
                                + dataIn.remaining() + " bytes");
            dataIn.put(cnxInBuf.array(), 0, dataIn.remaining());
            cnxInBuf.position(dataIn.remaining());
            cnxInBuf.compact();
            DEBUG_MSG(".dataReceived (b), after compacting"
                + "\n    cnxInBuf.position  = " + cnxInBuf.position()
                + "\n    cnxInBuf.limit     = " + cnxInBuf.limit()
                + "\n    cnxInBuf.remaining = " + cnxInBuf.remaining()
                + "\n    cnxInBuf.capacity  = " + cnxInBuf.capacity()
                + "\n    starts with        " + firstTen(cnxInBuf.array())
            );
        }
        DEBUG_MSG(".dataReceived, after copying "
                + "\n    dataIn.position  = " + dataIn.position()
                + "\n    dataIn.limit     = " + dataIn.limit()
                + "\n    dataIn.remaining = " + dataIn.remaining()
                + "\n    dataIn.capacity  = " + dataIn.capacity()
                + "\n    starts with        " + firstTen(dataIn.array())
        );
       
        dataIn.flip();

        DEBUG_MSG(".dataReceived, after flipping "
                + "\n    dataIn.position  = " + dataIn.position()
                + "\n    dataIn.limit     = " + dataIn.limit()
                + "\n    dataIn.remaining = " + dataIn.remaining()
                + "\n    dataIn.capacity  = " + dataIn.capacity()
                + "\n    starts with        " + firstTen(dataIn.array())
        );
        DEBUG_MSG(".dataReceived, top of switch, state " 
                + STATES[parserState]);
        switch (parserState) {
            case START_HEAD:
                DEBUG_MSG(".dataReceived: START_HEAD");

            case IN_HEAD:
                parseHeaderSection();
                DEBUG_MSG(" after parseHeadSection, state is "
                        + STATES[parserState]);
                if (parserState == IN_HEAD) {
                    cnx.initiateReading();
                    return;
                }
            case START_ENTITY:
                DEBUG_MSG(".dataReceived: START_ENTITY");
                if (contentLength > dataIn.capacity()) {
                    ERROR_MSG(
                        "INTERNAL ERROR: contentLength = " + contentLength
                      + " but buffer capacity is only " + dataIn.capacity() );
                    parserState = ABORT_PARSE;
                    _close();
                    return;
                }
            case IN_ENTITY:
                DEBUG_MSG(".dataReceived: IN_ENTITY"
                    + "\n    contentLength = " + contentLength
                    + "\n    dataIn.limit  = " + dataIn.limit()
                );
                if (version != V0_9 
                        && dataIn.limit() < contentLength) {
                    DEBUG_MSG(".dataReceived, IN_ENTITY, need more data");
                    parserState = IN_ENTITY;
                    cnx.initiateReading();
                    return;
                }
                DEBUG_MSG(".dataReceived, collecting entity");
                try {
                    // collect the entity
                    collectEntity(response, dataIn);    // does the flip()
                    DEBUG_MSG(
                        ".dataReceived; entity collected\n    starts with "
                            + firstTen(response.getEntity()));
                } catch (MessageFormatException mfe) {
                    ERROR_MSG(".dataReceived, from collectEntity: "
                            + mfe);
                    parserState = ABORT_PARSE;
                    _close();
                    return;
                }
                
            case END_ENTITY:
                DEBUG_MSG(".dataReceived: END_ENTITY");
                dataIn.clear();
                _close();
                return;

            case ABORT_PARSE:
                ERROR_MSG(".dataReceived, unexpected ABORT_PARSE");
                _close();
                return;

            default:
                ERROR_MSG(" INTERNAL ERROR: illegal parser state "
                        + parserState);
                _close();
                return;
                
        }
//      if (version == V0_9) {
//          // HTTP/0.9 ///////////////////////////////////
//          contentLength = -1;             // yes, I'm neurotic

//          if (dataIn.position() >= dataIn.capacity()) {
//              DEBUG_MSG(".dataReceived: dataIn filled");
//              try {
//                  response = new HttpResponse();
//                  response.setEntity(dataIn.array());
//              } catch (MessageFormatException mfe) {
//                  /* assumed to be impossible */
//                  ERROR_MSG(" internal error? " + mfe);
//              }
//              _close();
//          }
//      } else {
//          // HTTP/1.x ///////////////////////////////////
//          if (inHeaderSection) {
//              try {
//                  DEBUG_MSG(".dataReceived, parsing status line");
//                  response =  parseStatusLine(dataIn); 
//                  DEBUG_MSG(".dataReceived, status line is:\n    " 
//                          + response.getStatusLine());
//                  // collect headers
//                  collectHeaders(response, dataIn);
//                  DEBUG_MSG(".dataReceived, content length = " 
//                          + contentLength);
//                  skipEOL(dataIn);        // adjusts position
//                  dataIn.compact();
//                  DEBUG_MSG(".dataReceived, after compacting"
//                          + "\n    begins with " + firstTen(dataIn.array())
//                          + "\n    position is " + dataIn.position()
//                  );
//                  inHeaderSection = false;
//                  
//                  DEBUG_MSG(".dataReceived, have the entity");
//              } catch (MessageFormatException mfe) {
//                  ERROR_MSG(": " + mfe);
//                  _close();
//                  return; // GEEP
//              }
//          } else {
//              DEBUG_MSG(".dataReceived, collecting entity");
//              try {
//                  // collect the entity
//                  collectEntity(response, dataIn);    // does the flip()
//                  DEBUG_MSG(
//                      ".dataReceived; entity collected\n    starts with "
//                          + firstTen(response.getEntity()));
//              } catch (MessageFormatException mfe) {
//                  ERROR_MSG(".dataReceived, from collectEntity: "
//                          + mfe);
//              }
//              DEBUG_MSG(".dataReceived, calling _close()");
//              _close();
//          }
//      } // GEEP
    }
    /** 
     * This may alter parserState.
     */
    public void parseHeaderSection() {
        if (version == V0_9) {
            contentLength = -1;
            parserState   = START_ENTITY;
            try {
                response      = new HttpResponse();
            } catch (MessageFormatException mfe) {
                ERROR_MSG("impossible exception: " + mfe);
            }
        } else { 
            // HTTP/1.x ///////////////////////////////////
            // a better version would handle an incomplete
            // header section properly, setting IN_HEAD
            try {
                response =  parseStatusLine(dataIn); 
                DEBUG_MSG(".parseHeaderSection, status line is:\n    " 
                        + response.getStatusLine());
                // collect headers
                collectHeaders(response, dataIn);
                DEBUG_MSG(".parseHeaderSection, content length = " 
                        + contentLength);
                skipEOL(dataIn);        // adjusts position
                DEBUG_MSG(".parseHeaderSection, before compacting"
                        + "\n    position     = " + dataIn.position()
                        + "\n    limit        = " + dataIn.limit()
                );
                dataIn.compact();       // sets limit to capacity
                dataIn.flip();
                DEBUG_MSG(".parseHeaderSection, after compacting"
                        + "\n    position      = " + dataIn.position()
                        + "\n    limit         = " + dataIn.limit()
                        + "\n    remaining     = " + dataIn.remaining()
                        + "\n    contentLength = " + contentLength
                );
                parserState = START_ENTITY;
            } catch (MessageFormatException mfe) {
                ERROR_MSG(": " + mfe);
                parserState = ABORT_PARSE;
                _close();
                return;
            }
        }
    }
    public void reportDisconnect () {
        DEBUG_MSG(": unexpected disconnection");
        _close();
    }
    public void reportException (Exception exc) {
        DEBUG_MSG(": unexpected exception: " + exc);
        _close();
    }
    // PARSER METHODS ///////////////////////////////////////////////
    /**
     * Parse the first part of an HTTP response, creating the data
     * structure.  Any of CR, LF, or CRLF is a line ending. 
     * 
     * @param inBuf reference to dataIn, a convenience for testing
     * @return reference to the HttpResponse created
     */
    public HttpResponse parseStatusLine(ByteBuffer inBuf) 
                                            throws MessageFormatException {
        if (inBuf == null)
            throw new IllegalArgumentException ("null in buffer");
        int     curByte  = 0;
        byte[]  b        = inBuf.array();
        int     replyVersion;

        // status line: ///////////////////////////////////
        //   HTML/1.x NNN response-phrase EOL
        // white space should be interpreted liberally
        curByte = expect("HTTP/1.", b, curByte);
        char c = castByte(b[curByte++]);
        if (c == '1') 
            replyVersion = V1_1;
        else if (c == '0')
            replyVersion = V1_0;
        else
            throw new MessageFormatException("unsupported HTTP version 1."
                    + c);
        curByte  = skipW (b, curByte);
        int code = expect3Digits(b, curByte);
        curByte += 3;
        curByte  = skipW (b, curByte);      // expect and skip white space
        int start = curByte;
        curByte   = skipToEOL (b, start);
        String s  = new String (b, start, curByte - start);
        
        curByte   = skipEOL (b, curByte);   // expect and skip
        inBuf.position(curByte);
        return new HttpResponse(replyVersion, code, s);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    SchedulableConnection getConnection() {
        return cnx;
    }
    /**
     * Only useful for test purposes.
     */
    byte[] getDataIn() {
        if (dataIn == null)
            return null;
        else {
//          // DEBUG
//          SHA1Digest sha1 = null;
//          try {sha1 = new SHA1Digest(); } catch (CryptoException ce){};
//          sha1.update(dataIn.array());
//          byte [] hash = sha1.digest();
//          DEBUG_MSG(": " + uri + 
//                  " DataIn hash = " + StringLib.byteArrayToHex(hash));
//          // END
            return dataIn.array();
        }
    }
    /**
     * Only useful for test purposes.
     */
    byte[] getDataOut() {
        if (dataOut == null)
            return null;
        else
            return dataOut.array();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    private void _close() {
        try {
            cnx.getChannel().close();   // cancels the key
        } catch (IOException e) { /* ignore */ }
    }
}
