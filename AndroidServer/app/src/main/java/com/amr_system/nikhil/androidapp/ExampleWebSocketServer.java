package com.amr_system.nikhil.androidapp;


import android.util.Log;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


public class ExampleWebSocketServer extends WebSocketServer {

    private static String TAG = "SocketServer";


    public ExampleWebSocketServer( InetSocketAddress address ) {
        super( address );
    }

    public ExampleWebSocketServer( int port ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        this.sendToAll( "new connection: " + handshake.getResourceDescriptor() );
        Log.d(TAG, conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!\n");
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        this.sendToAll( conn + " has left the room!\n" );
        Log.d(TAG, conn + " has left the room!\n" );
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        this.sendToAll( message );
        Log.d(TAG, conn + ": " + message );
    }

    @Override
    public void onFragment( WebSocket conn, Framedata fragment ) {
        Log.d(TAG, "received fragment: " + fragment );
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "SocketServer started!\n");
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text
     *            The String to send across the network.
     * @throws InterruptedException
     *             When socket related I/O errors occur.
     */
    public void sendToAll( String text ) {
        Collection<WebSocket> con = connections();
        synchronized ( con ) {
            for( WebSocket c : con ) {
                c.send( text );
            }
        }
    }
}
