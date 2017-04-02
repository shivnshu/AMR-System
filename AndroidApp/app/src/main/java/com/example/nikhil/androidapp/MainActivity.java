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


public class MainActivity extends Activity {
        private final static String TAG = MainActivity.class.getSimpleName();

        private SensorManager sensorManager;
        private Sensor sensor,sensor_gyro;
        boolean acc_disp = false;
        private boolean connected = false;
        private float x, y, z;
        private float x_g, y_g, z_g,w_g;
        TextView infoIp, infoPort;
        TextView textViewState, textViewPrompt;
        EditText port;
        TextView AX, AY,AZ, GX,GY,GZ,GW;
        Button button;
        //static final int UdpServerPORT = 5000;
        static int UdpServerPORT = 5000;
        UdpServerThread udpServerThread;
        DatagramSocket socket;



        // ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ // main // ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ //
        @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            infoIp = (TextView) findViewById(R.id.infoip);
            infoPort = (TextView) findViewById(R.id.infoport);
            textViewState = (TextView) findViewById(R.id.state);
            textViewPrompt = (TextView) findViewById(R.id.prompt);

            port = (EditText) findViewById(R.id._port);
            port.setText(String.valueOf(UdpServerPORT));
            //final String Port = port.getText().toString().trim();
            //UdpServerPORT = Integer.parseInt(port.getText().toString().trim());

            AX = (TextView) findViewById(R.id._ax);
            AY = (TextView) findViewById(R.id._ay);
            AZ = (TextView) findViewById(R.id._az);
            GX = (TextView) findViewById(R.id._gx);
            GY = (TextView) findViewById(R.id._gy);
            GZ = (TextView) findViewById(R.id._gz);
            GW = (TextView) findViewById(R.id._gw);
            button = (Button) findViewById(R.id._start_server);
            button.setOnClickListener(connectListener);
            button.setText("Start/Stop Server");

            infoIp.setText(getIpAddress());
            infoPort.setText(String.valueOf(UdpServerPORT));

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            sensor_gyro = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR).get(0);
            acc_disp = false;
            updateState("UDP Server is not running");
        }
        // ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ //

        // ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ // UDP Server // ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ //
        private Button.OnClickListener connectListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    UdpServerPORT = Integer.parseInt(port.getText().toString());
                }
                catch (NumberFormatException e)
                {
                    // handle the exception
                }
                if (!connected) {

                    //button.setText("Stop");
                    infoPort.setText(String.valueOf(UdpServerPORT));
                    connected = true;
                    udpServerThread = new UdpServerThread(UdpServerPORT);
                    udpServerThread.start();
                }
                else{
                    //button.setText("Start Server");
                    udpServerThread.setRunning(false);
                    updateState("UDP Server is not running");
                    connected=false;
                    acc_disp=false;

                    if(!socket.isClosed()) {
                           socket.close();
                           Log.d(TAG, "socket.close()");
                    }
                    //socke10t.close();
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

        private void updatePrompt(final String prompt) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewPrompt.append(prompt);
                }
            });
        }

        private class UdpServerThread extends Thread {

            int serverPort;
            boolean running;

            public UdpServerThread(int serverPort) {
                super();
                this.serverPort = serverPort;
            }

            public void setRunning(boolean running) {
                this.running = running;
            }

            @Override
            public void run() {

                running = true;

                try {
                    socket = new DatagramSocket(serverPort);

                    updateState("UDP Server is running");
                    Log.d(TAG, "UDP Server is running");

                    while (running) {
                        byte[] buf = new byte[256];

                        // receive request
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        Log.d(TAG, "waiting for connection at" + port);
                        socket.receive(packet);     //this code block the program flow
                        Log.d(TAG, "Got connection");
                        // send the response to the client at "address" and "port"
                        InetAddress address = packet.getAddress();
                        int port = packet.getPort();

                        updateState("Request from: " + address + ":" + port + "\n");
                        String s;
                        while(true && running){
                            s = Float.toString(x_g) + " " + Float.toString(y_g) + " " + Float.toString(z_g) + " " + Float.toString(w_g) + "\n";
                            buf = s.getBytes();
                            packet = new DatagramPacket(buf, buf.length, address, port);
                            socket.send(packet);
                            //Thread.sleep(8);
                        }

                    }

                    Log.e(TAG, "UDP Server ended");

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (socket != null) {

                        socket.close();
                        Log.d(TAG, "socket.close()");

                    }
                }
            }
        }

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
                            ip += "SiteLocalAddress: "
                                    + inetAddress.getHostAddress() + "\n";
                        }

                    }

                }

            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                ip += "Something Wrong! " + e.toString() + "\n";
            }

            return ip;
        }
        // ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ //

        // ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ // accelerometer classes // ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ //
        private void init_perif() {
            // smthing
        }

        @Override
        protected void onResume() {
            super.onResume();
            sensorManager.registerListener(accelerationListener, sensor,
                    sensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(gyroListener, sensor_gyro,
                    sensorManager.SENSOR_DELAY_FASTEST);
        }

        @Override
        protected void onStop() {
            sensorManager.unregisterListener(accelerationListener);
            super.onStop();
        }

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
                w_g = event.values[3];
                GX.setText( "g_x:"+Float.toString(x_g));
                GY.setText( "g_y:"+Float.toString(y_g));
                GZ.setText( "g_z:"+Float.toString(z_g));
                GW.setText( "g_w:"+Float.toString(w_g));

            }
        };
        // ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ //
    }