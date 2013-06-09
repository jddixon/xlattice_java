/* Server.java */
package org.xlattice.httpd;

import java.net.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Rudimentary HTTP daemon.  Runs a different thread for each 
 * incoming connection.
 * 
 * @author Jim Dixon
 */
public class Server {

    private Server() {}

    public void run (int port) throws IOException {
        System.out.println("run(): creating server socket");        // DEBUG
        // POSSIBLY NEED TO SET SO_REUSEADDR BEFORE BINDING, 
        //  see setReuseAddress()
        ServerSocket acceptor = new ServerSocket( port );
        System.out.println(                                         // DEBUG
                "    socket created, listening to port: " + port);  // DEBUG
        
        while ( true ) {
            // we accept and reply to one msg, then close the connection
            // create a Connection thread for each accepted connection
            // DEBUG
            System.out.println("Server creating a new Connection");
            System.out.flush();
            // END
            Socket sock = acceptor.accept();
            System.out.println("Connection.run()"); // DEBUG
            try {
                OutputStream out = sock.getOutputStream(); // for writing
                String msg = new BufferedReader(
                                new InputStreamReader(sock.getInputStream()))
                                    .readLine();
                System.out.println( "request was: " + msg );
       
                // handle "GET /foo"
                StringTokenizer st = new StringTokenizer( msg );
        
                if ( (st.countTokens() <= 2) && st.nextToken().equals("GET") ) {
                    if ( (msg = st.nextToken()).startsWith("/") ) {
                        msg = msg.substring( 1 );
                    }
                    // If the last char is a slash then return the default
                    // "index.html" as with standard web servers.
                    if ( msg.endsWith("/") || msg.equals("") ) {
                        msg = msg + "index.html";
                    }
                    // Use the file name to read in the file from disk
                    // into a byte array. Then write it to the OutputStream
                    // we got from the socket.
                    try {
                        System.out.println("File= " + msg);
                        FileInputStream fis = new FileInputStream ( msg );
                        byte [] data = new byte [ fis.available() ];
                        fis.read( data );
                        out.write( data );
                    } catch ( FileNotFoundException e ) {
                        new PrintStream( out )
                            .println( "404 Object Not Found" );
                    } catch ( SecurityException e ) {
                        new PrintStream( out )
                            .println( "403 Forbidden" );
                    } 
                } else {
                    new PrintStream( out )
                        .println( "400 Bad Request" );
                }
            } catch ( IOException e ) {
                 System.out.println( "I/O error " + e );
            }

            // allow time for Windoze machines to get page
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) { /* ignore */ } 
            // CLOSE THE SOCKET
            try {
                sock.close();
            } catch ( IOException e ) {
                 System.out.println( "error closing socket: " + e );
            }
        }
        // unreachable
        // acceptor.close();
    }
    /**
     *
     * Defaults to running on port 80.  On UNIX/Linux systems will 
     * throw exception if run on port below 1024 by user without
     * root privileges.
     */
    public static void main( String argv[] ) throws IOException {
        // System.setSecurityManager( new SecMgr() );

        int port = 80;
        if (argv.length > 0) {
            port = Integer.parseInt (argv[0]);
            if( port <= 1023 || port > 65535 ) {
                System.out.println(
                    "port must be between 1023 and 65536, exclusive");
                System.exit(0);
            }
        }
        System.out.println("Server.main(), calling constructor");
        Server s = new Server();
        // DEBUG
        System.out.println("invoking Server.run(" + port + ")");
        // END
        s.run(port);
    } 
}
