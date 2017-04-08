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
import java.util.Date;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import android.view.KeyEvent;


public class MainActivity extends Activity {
        private final static String TAG = MainActivity.class.getSimpleName();

        private SensorManager sensorManager;
        private Sensor sensor,sensor_gyro;
        private float x, y, z;
        private float x_g, y_g, z_g,w_g;
        TextView infoIp, infoPort;
        TextView textViewState, textViewPrompt;
        EditText port;
        TextView AX, AY,AZ, GX,GY,GZ,GW,SCALEUP,SCALEDOWN;
        Button button;
        static int udpReceiverPort = 5000;
        static int udpSenderPort = 9999;
        static int clientReceiverPort = 8888;
        static int ScaleUp = 0, ScaleDown = 0;
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

            AX = (TextView) findViewById(R.id._ax);
            AY = (TextView) findViewById(R.id._ay);
            AZ = (TextView) findViewById(R.id._az);
            GX = (TextView) findViewById(R.id._gx);
            GY = (TextView) findViewById(R.id._gy);
            GZ = (TextView) findViewById(R.id._gz);
            GW = (TextView) findViewById(R.id._gw);
            SCALEUP = (TextView) findViewById(R.id._scaleup);
            SCALEDOWN = (TextView) findViewById(R.id._scaledown);
            button = (Button) findViewById(R.id._start_server);
            button.setOnClickListener(connectListener);
            infoIp.setText(getIpAddress());
            infoPort.setText(String.valueOf(udpReceiverPort));

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            sensor_gyro = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR).get(0);
            updateState("UDP Server is not running");
            Log.d(TAG, "TAG string is " + TAG + "\n");
        }


        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                ScaleUp = (ScaleUp+1)%2;
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                ScaleDown = (ScaleDown+1)%2;
                return true;
            }

            else {
                ScaleUp = 0;
                ScaleDown = 0;
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

                updateState("UDP Server is running");
                Log.d(TAG, "UDP Server is running\n");

                while (running) {
                    byte[] buf = new byte[256];

                    // receive request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    Log.d(TAG, "waiting for connection at" + port+"\n");
                    receiveSocket.receive(packet);     // this code blocks the program flow
                    Log.d(TAG, "Got connection\n");
                    // send the response to the client at "address" and "port"
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();

                    updateState("Request from: " + address + ":" + port + "\n");
                    udpSendThread = new udpSenderThread(address, clientReceiverPort);
                    udpSendThread.run();
                }

                Log.e(TAG, "UDP Server ended\n");

            } catch (Exception e) {
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
                sendSocket = new DatagramSocket(udpSenderPort);
                Log.d(TAG, "Started sending packets to ip"+address+":"+port+" using port "+udpSenderPort+"\n");
                String s;
                byte[] buf;
                DatagramPacket packet;
                while(running){
                    //Log.d(TAG, Integer.toString(ScaleUp)+Integer.toString(ScaleDown)+"\n");
                    s = Float.toString(x_g) + " " + Float.toString(y_g) + " " + Float.toString(z_g) + " " + Float.toString(w_g) + " " + Integer.toString(ScaleUp) + " " + Integer.toString(ScaleDown) + "\n";
                    buf = s.getBytes();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    //Log.d(TAG, "Sending "+s+"\n");
                    sendSocket.send(packet);
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


        @Override
        protected void onResume() {
            super.onResume();
            sensorManager.registerListener(accelerationListener, sensor,
                    50000);
            sensorManager.registerListener(gyroListener, sensor_gyro,
                    50000);
        }

        @Override
        protected void onStop() {
            sensorManager.unregisterListener(accelerationListener);
            super.onStop();
        }


        ////////////////////////////////////////// Sensors Classes ////////////////////////////////
        private SensorEventListener accelerationListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int acc) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                //refreshDisplay();
                AX.setText( "a_x:"+Float.toString(x));
                AY.setText( "a_y:"+Float.toString(y));
                AZ.setText( "a_z:"+Float.toString(z));
                SCALEUP.setText("Scale UP:"+Integer.toString(ScaleUp));
                SCALEDOWN.setText("Scale DOWN:"+Integer.toString(ScaleDown));


            }
        };

        private SensorEventListener gyroListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor_gyro, int acc) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {

                x_g = event.values[0];
                y_g = event.values[1];
                z_g = event.values[2];
                //w_g = event.values[3];
                w_g = 0;
                GX.setText( "g_x:"+Float.toString(x_g));
                GY.setText( "g_y:"+Float.toString(y_g));
                GZ.setText( "g_z:"+Float.toString(z_g));
                GW.setText( "g_w:"+Float.toString(w_g));

            }
        };
    }