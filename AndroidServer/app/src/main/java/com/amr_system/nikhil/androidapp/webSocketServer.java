package com.amr_system.nikhil.androidapp;


import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


public class webSocketServer extends WebSocketServer {

    private static String TAG = "SocketServer";


    public webSocketServer(InetSocketAddress address ) {
        super( address );
    }

    public webSocketServer(int port ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        // this.sendToAll( "new connection: " + handshake.getResourceDescriptor() );
        Log.d(TAG, conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected!!\n");
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        // this.sendToAll( conn + " has left the room!\n" );
        Log.d(TAG, conn + " left!!\n" );
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        // this.sendToAll( message );
        Log.d(TAG, conn + ": " + message );
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer bb) {
        Log.d(TAG, "abcd:  "+bb.toString());
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AMR-System/models/" + "tmp.stl");
            FileChannel channel = new FileOutputStream(file, false).getChannel();
            channel.write(bb);
            channel.close();
        } catch (IOException e) {
            Log.e(TAG, "I/O Error: " + e.getMessage());
        }
    }

    Framedata f;
    @Override
    public void onFragment( WebSocket conn, Framedata fragment ) {
        Log.d(TAG, "received fragment: " + fragment );
        if(fragment.getOpcode().equals(Framedata.Opcode.BINARY)){
            f = fragment;
        } else {
            f.append(fragment);
        }
        if(fragment.isFin()) {
            try {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AMR-System/models/" + "tmp.stl");
                FileChannel channel = new FileOutputStream(file, false).getChannel();
                channel.write(f.getPayloadData());
                channel.close();
            } catch (IOException e) {
                Log.e(TAG, "I/O Error: " + e.getMessage());
            }
            // Log.d(TAG, "Full fragment: " + f.getPayloadData());
        }
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

    public void sendToAll( String text ) {
        Collection<WebSocket> con = connections();
        synchronized ( con ) {
            for( WebSocket c : con ) {
                c.send( text );
            }
        }
    }
}
