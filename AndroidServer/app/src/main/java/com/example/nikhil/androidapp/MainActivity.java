package com.example.nikhil.androidapp;

import java.net.InetAddress;


import java.lang.Object;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import android.view.KeyEvent;

import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends Activity {
        private final static String TAG = MainActivity.class.getSimpleName();

        private SensorManager sensorManager;
        private Sensor sensor;
        private float x, y, z, w;
        TextView infoIp, infoPort;
        TextView textViewState, textViewPrompt;
        EditText port;
        TextView X, Y, Z, W, volumeUpView, volumeDownView;
        Button button;
        static int udpReceiverPort = 5000;
        static int udpSenderPort = 9999;
        static int clientReceiverPort = 8888;
        static int volumeUp = 0, volumeDown = 0;
        udpReceiverThread udpReceiveThread;
        udpSenderThread udpSendThread;
        DatagramSocket receiveSocket;
        DatagramSocket sendSocket;
        boolean running = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            this.setTitle("CAD RENDERING CONTROLLER");

            infoIp = (TextView) findViewById(R.id.infoip);
            infoPort = (TextView) findViewById(R.id.infoport);
            textViewState = (TextView) findViewById(R.id.state);
            textViewPrompt = (TextView) findViewById(R.id.prompt);

            port = (EditText) findViewById(R.id._port);
            port.setText(String.valueOf(udpReceiverPort));

            X = (TextView) findViewById(R.id.x);
            Y = (TextView) findViewById(R.id.y);
            Z = (TextView) findViewById(R.id.z);
            W = (TextView) findViewById(R.id.w);
            volumeUpView = (TextView) findViewById(R.id.volumeUp);
            volumeDownView = (TextView) findViewById(R.id.volumeDown);
            button = (Button) findViewById(R.id._start_server);
            button.setOnClickListener(connectListener);
            infoIp.setText(getIpAddress());
            infoPort.setText(String.valueOf(udpReceiverPort));

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            updateState("UDP Server is not running");
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



        private Button.OnClickListener connectListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    udpReceiverPort = Integer.parseInt(port.getText().toString());
                }
                catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }
                if (!running) {
                    infoPort.setText(String.valueOf(udpReceiverPort));
                    running = true;
                    udpReceiveThread = new udpReceiverThread(udpReceiverPort);
                    udpReceiveThread.start();
                }
                else{
                    running = false;
                    updateState("UDP Server is not running");

                    if(!receiveSocket.isClosed()) {
                           receiveSocket.close();
                           Log.d(TAG, "receiveSocket is closed\n");
                    }
                    if(!sendSocket.isClosed()) {
                        sendSocket.close();
                        Log.d(TAG, "sendSocket is closed\n");
                    }
                }
            }
        };

        private void updateState(final String state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewState.setText(state);
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
                sendSocket = new DatagramSocket(udpSenderPort);
                updateState("UDP Server is running");
                Log.d(TAG, "UDP Server is running\n");
                String recvString;

                while (running) {
                    byte[] buf = new byte[256];

                    // receive request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    Log.d(TAG, "waiting for connection at" + port+"\n");
                    receiveSocket.receive(packet);     // this code blocks the program flow
                    Log.d(TAG, "Got connection\n");
                    recvString = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    updateState(recvString);
                    Log.d(TAG, recvString +"\n");
                    if(recvString.equals("TYPE_ACCELEROMETER")) {
                        if(sensorManager.getSensorList(sensor.TYPE_ACCELEROMETER).size() == 0){
                            updateState("Sensor not present");
                            continue;
                        }
                        sensor = sensorManager.getSensorList(sensor.TYPE_ACCELEROMETER).get(0);
                    } else if(recvString.equals("TYPE_ROTATION_VECTOR")){
                        if(sensorManager.getSensorList(sensor.TYPE_ROTATION_VECTOR).size() == 0){
                            updateState("Sensor not present");
                            continue;
                        }
                        sensor = sensorManager.getSensorList(sensor.TYPE_ROTATION_VECTOR).get(0);
                    } else if(recvString.equals("TYPE_GRAVITY")){
                        if(sensorManager.getSensorList(sensor.TYPE_GRAVITY).size() == 0){
                            updateState("Sensor not present");
                            continue;
                        }
                        sensor = sensorManager.getSensorList(sensor.TYPE_GRAVITY).get(0);
                    } else if (recvString.equals("TYPE_LINEAR_ACCELERATION")){
                        if(sensorManager.getSensorList(sensor.TYPE_LINEAR_ACCELERATION).size() == 0){
                            updateState("Sensor not present");
                            continue;
                        }
                        sensor = sensorManager.getSensorList(sensor.TYPE_LINEAR_ACCELERATION).get(0);
                    } else if(recvString.equals("TYPE_GYROSCOPE")) {
                        if(sensorManager.getSensorList(sensor.TYPE_GYROSCOPE).size() == 0){
                            updateState("Sensor not present");
                            continue;
                        }
                        sensor = sensorManager.getSensorList(sensor.TYPE_GYROSCOPE).get(0);
                    } else {
                        updateState("Invalid sensor");
                        continue;
                    }

                    sensorManager.registerListener(sensorListener, sensor, 50000);
                    // send the response to the client at "address" and "port"
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();

                    //updateState("Request from: " + address + ":" + port + "\n");
                    udpSendThread = new udpSenderThread(address, clientReceiverPort);
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

        @Override
        public void run(){
            try {
                Log.d(TAG, "Started sending packets to ip"+address+":"+port+" using port "+udpSenderPort+"\n");
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
                    Log.d(TAG, jsonObject.toString() + '\n');
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
            X.setText( "x:"+String.valueOf(x));
            Y.setText( "y:"+String.valueOf(y));
            Z.setText( "z:"+String.valueOf(z));
            W.setText( "w:"+String.valueOf(w));
            volumeUpView.setText("volumeUp:"+Integer.toString(volumeUp));
            volumeDownView.setText("volumeDown:"+Integer.toString(volumeDown));

        }
    };



    //////////////////// Function to get current Server IP /////////////////////////////
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
                        ip += "LocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

}