package com.amr_system.nikhil.androidapp;

import java.net.InetAddress;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.view.KeyEvent;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
        private final static String TAG = MainActivity.class.getSimpleName();

        private SensorManager sensorManager;
        private Sensor sensor;
        private float x, y, z, w;
        TextView UserMsg, ServerStatus;
        TextView GithubLink;
        Button StartServerButton, DebugButton;
        // TextView textViewPrompt;
        // TextView X, Y, Z, W, volumeUpView, volumeDownView;
        Button button;
        static int udpReceiverPort = 1111;
        static int ClientReceiverPort = 2222;
        static int volumeUp = 0, volumeDown = 0;
        udpReceiverThread udpReceiveThread;
        udpSenderThread udpSendThread;
        DatagramSocket receiveSocket;
        DatagramSocket sendSocket;
        boolean running = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.dark_red));
                // window.setNavigationBarColor(ContextCompat.getColor(MainActivity.this, R.color.dark_red)); // // TODO: 23/4/17 Check with om can something good be designed around it
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.light_red)));
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            }
            setContentView(R.layout.activity_main);
            this.setTitle(getString(R.string.app_name));

            UserMsg = (TextView) findViewById(R.id.user_msg);
            ServerStatus = (TextView) findViewById(R.id.server_status);

            StartServerButton = (Button) findViewById(R.id.start_server);
            StartServerButton.setOnClickListener(MainClickListener);
            DebugButton = (Button) findViewById(R.id.debug);
            DebugButton.setOnClickListener(MainClickListener);
            GithubLink = (TextView) findViewById(R.id.github_link);
            GithubLink.setOnClickListener(MainClickListener);

            UserMsg.setText(getString(R.string.user_msg)+getIpAddress()+":"+String.valueOf(udpReceiverPort));
            // UserMsg.setText(getString(R.string.user_msg_off));
            updateServerState(getString(R.string.server_status_off));


            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Log.d(TAG, "TAG string is " + TAG + "\n");
        }

        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                volumeUp = (volumeUp+1)%2;
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                volumeDown = (volumeDown+1)%2;
                return true;
            }

            else {
                volumeUp = 0;
                volumeDown = 0;
                return super.onKeyDown(keyCode, event);
            }
        }



        private Button.OnClickListener MainClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.start_server) {
                    if (!running) {
                        // infoPort.setText(String.valueOf(udpReceiverPort));
                        // todo: Set a server running animantion change colours of title bar here
                        // Layout Status changes
                        // UserMsg.setText(getString(R.string.user_msg_on));
                        StartServerButton.setText(getString(R.string.stop_server));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Window window = getWindow();
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.dark_green));
                            // window.setNavigationBarColor(ContextCompat.getColor(MainActivity.this, R.color.dark_green));
                            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.light_green)));
                            getSupportActionBar().setDisplayShowTitleEnabled(false);
                            getSupportActionBar().setDisplayShowTitleEnabled(true);
                        }
                        updateServerState(getString(R.string.server_status_on));

                        running = true;
                        udpReceiveThread = new udpReceiverThread(udpReceiverPort);
                        udpReceiveThread.start();
                    } else {
                        running = false;
                        // updateServerState("UDP Server is not running");
                        // todo: Set all status of not running server here
                        // UserMsg.setText(getString(R.string.user_msg_off));
                        StartServerButton.setText(getString(R.string.start_server));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Window window = getWindow();
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.dark_red));
                            // window.setNavigationBarColor(ContextCompat.getColor(MainActivity.this, R.color.dark_red));
                            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.light_red)));
                            getSupportActionBar().setDisplayShowTitleEnabled(false);
                            getSupportActionBar().setDisplayShowTitleEnabled(true);
                        }
                        updateServerState(getString(R.string.server_status_off));

                        if (!receiveSocket.isClosed()) {
                            receiveSocket.close();
                            Log.d(TAG, "receiveSocket is closed\n");
                        }
                        if (!sendSocket.isClosed()) {
                            sendSocket.close();
                            Log.d(TAG, "sendSocket is closed\n");
                        }
                    }
                }
                else if(v.getId()==R.id.github_link) {
                    Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.github_link_address)));
                    startActivity(viewIntent);
                }
                else if(v.getId()==R.id.debug) {
                    DebugButton.setText(getString(R.string.layout_debug_msg));
                }
            }
        };

        // todo: see if required
        private void updateServerState(final String state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //textViewState.setText(state);
                    ServerStatus.setText(state);
                }
            });
        }

    private class udpReceiverThread extends Thread {
        int serverPort;


        private udpReceiverThread(int serverPort) {
            super();
            this.serverPort = serverPort;
        }

        @Override
        public void run() {

            try {
                receiveSocket = new DatagramSocket(serverPort);
                sendSocket = new DatagramSocket();
                updateServerState(getString(R.string.server_status_on));
                Log.d(TAG, "UDP Server is running\n");
                String recvString;

                while (running) {
                    byte[] buf = new byte[256];

                    // receive request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    Log.d(TAG, "waiting for connection at" + udpReceiverPort+"\n");
                    receiveSocket.receive(packet);     // this code blocks the program flow
                    Log.d(TAG, "Got connection\n");
                    recvString = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    updateServerState(recvString);
                    Log.d(TAG, recvString);
                    Log.d(TAG, "TYPE_ACCELEROMETER");
                    if(recvString.equals("TYPE_ACCELEROMETER")) {
                        if(sensorManager.getSensorList(sensor.TYPE_ACCELEROMETER).size() == 0){
                            updateServerState(getString(R.string.sensor_not_present));
                            continue;
                        }
                        sensor = sensorManager.getSensorList(sensor.TYPE_ACCELEROMETER).get(0);
                    } else if(recvString.equals("TYPE_ROTATION_VECTOR")){
                        if(sensorManager.getSensorList(sensor.TYPE_ROTATION_VECTOR).size() == 0){
                            updateServerState(getString(R.string.sensor_not_present));
                            continue;


                       }
                        sensor = sensorManager.getSensorList(sensor.TYPE_ROTATION_VECTOR).get(0);
                    } else if(recvString.equals("TYPE_GRAVITY")){
                        if(sensorManager.getSensorList(sensor.TYPE_GRAVITY).size() == 0){
                            updateServerState(getString(R.string.sensor_not_present));
                            continue;
                        }
                        sensor = sensorManager.getSensorList(sensor.TYPE_GRAVITY).get(0);
                    } else if (recvString.equals("TYPE_LINEAR_ACCELERATION")){
                        if(sensorManager.getSensorList(sensor.TYPE_LINEAR_ACCELERATION).size() == 0){
                            updateServerState(getString(R.string.sensor_not_present));
                            continue;
                        }
                        sensor = sensorManager.getSensorList(sensor.TYPE_LINEAR_ACCELERATION).get(0);
                    } else if(recvString.equals("TYPE_GYROSCOPE")) {
                        if(sensorManager.getSensorList(sensor.TYPE_GYROSCOPE).size() == 0){
                            updateServerState(getString(R.string.sensor_not_present));
                            continue;
                        }
                        sensor = sensorManager.getSensorList(sensor.TYPE_GYROSCOPE).get(0);
                    } else {
                        updateServerState("Invalid sensor");
                        continue;
                    }

                    sensorManager.registerListener(sensorListener, sensor, 50000);
                    // send the response to the client at "address" and "port"
                    InetAddress ClientAddress = packet.getAddress();
                    int ClientPort = packet.getPort();

                    updateServerState(getString(R.string.server_status_connected)+ClientAddress.toString().substring(1)+":"+ClientPort);
                    udpSendThread = new udpSenderThread(ClientAddress, ClientReceiverPort);
                    udpSendThread.start();
                    Log.d(TAG, "UDP Send Thread started\n");
                }

                Log.d(TAG, "UDP Server ended\n");

            } catch (Exception e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            } finally {
                if (receiveSocket != null) {
                    receiveSocket.close();
                    Log.d(TAG, "receiveSocket is closed\n");
                }
            }
        }
    }


    ///////////////////////////////   Sending Thread   ////////////////////////////////
    private class udpSenderThread extends Thread{
        private InetAddress address;
        private int port;

        private udpSenderThread(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        // TODO: 22/4/17 Put a simple click to accept authentication procedure
        // TODO: 22/4/17 close thread when client closes currently a client can start as many threads as he wants
        @Override
        public void run(){
            try {
                Log.d(TAG, "Started sending packets to ip"+address+":"+port+"\n");
                JSONObject jsonObject = new JSONObject();
                JSONObject rotationJsonObj = new JSONObject();
                JSONObject volumeJsonObj = new JSONObject();
                byte[] buf;
                DatagramPacket packet;
                while(running){
                    rotationJsonObj.put("x", x);
                    rotationJsonObj.put("y", y);
                    rotationJsonObj.put("z", z);
                    rotationJsonObj.put("w", w);
                    jsonObject.put("sensor", rotationJsonObj);
                    volumeJsonObj.put("volumeUp", volumeUp);
                    volumeJsonObj.put("volumeDown", volumeDown);
                    jsonObject.put("volume_keys", volumeJsonObj);
                    //Log.d(TAG, jsonObject.toString() + '\n');
                    buf = jsonObject.toString().getBytes();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    sendSocket.send(packet);
                    Thread.sleep(25);
                }
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if (sendSocket != null) {
                    sendSocket.close();
                    Log.d(TAG, "sendSocket is closed\n");
                }
            }
        }

    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(sensorListener);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (!receiveSocket.isClosed()) {
            receiveSocket.close();
            Log.d(TAG, "receiveSocket is closed\n");
        }
        if (!sendSocket.isClosed()) {
            sendSocket.close();
            Log.d(TAG, "sendSocket is closed\n");
        }
        super.onDestroy();
    }


    ////////////////////////////////////////// Sensors Classes ////////////////////////////////
    private SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            if(event.values.length > 3) {
                w = event.values[3];
            }
            // TODO: 22/4/17 Close sensor feed when client exits  
            // X.setText( "x:"+String.valueOf(x));
            // Y.setText( "y:"+String.valueOf(y));
            // Z.setText( "z:"+String.valueOf(z));
            // W.setText( "w:"+String.valueOf(w));
            // volumeUpView.setText("volumeUp:"+Integer.toString(volumeUp));
            // volumeDownView.setText("volumeDown:"+Integer.toString(volumeDown));

        }
    };



    //////////////////// Function to get current Server IP /////////////////////////////
    // TODO: 22/4/17 it is possible to have more than one ip address format accordingly
    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip = "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

}