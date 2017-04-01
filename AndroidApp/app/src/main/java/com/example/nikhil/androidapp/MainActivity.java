package com.example.nikhil.androidapp;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import java.net.*;
import java.util.*;
import com.illposed.osc.*;

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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
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



    public class MainActivity extends Activity {
        private final static String TAG = MainActivity.class.getSimpleName();

        private SensorManager sensorManager;
        private Sensor sensor,sensor_gyro;
        boolean acc_disp = false;
        TextView text;
        EditText port;
        TextView AX, AY,AZ, GX,GY,GZ;

        private float x, y, z;
        TextView infoIp, infoPort;
        TextView textViewState, textViewPrompt;

        static final int UdpServerPORT = 8080;
        UdpServerThread udpServerThread;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            infoIp = (TextView) findViewById(R.id.infoip);
            infoPort = (TextView) findViewById(R.id.infoport);
            textViewState = (TextView) findViewById(R.id.state);
            textViewPrompt = (TextView) findViewById(R.id.prompt);

            port = (EditText) findViewById(R.id._port);
            AX = (TextView) findViewById(R.id._ax);
            AY = (TextView) findViewById(R.id._ay);
            AZ = (TextView) findViewById(R.id._az);
            GX = (TextView) findViewById(R.id._gx);
            GY = (TextView) findViewById(R.id._gy);
            GZ = (TextView) findViewById(R.id._gz);

            infoIp.setText(getIpAddress());
            infoPort.setText(String.valueOf(UdpServerPORT));

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            sensor_gyro = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE).get(0);
            acc_disp = false;
        }

        @Override
        protected void onStart() {
            udpServerThread = new UdpServerThread(UdpServerPORT);
            udpServerThread.start();
            super.onStart();
        }


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
            DatagramSocket socket;

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
                    updateState("Starting UDP Server");
                    socket = new DatagramSocket(serverPort);

                    updateState("UDP Server is running");
                    Log.e(TAG, "UDP Server is running");

                    while (running) {
                        byte[] buf = new byte[256];

                        // receive request
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);     //this code block the program flow

                        // send the response to the client at "address" and "port"
                        InetAddress address = packet.getAddress();
                        int port = packet.getPort();

                        updatePrompt("Request from: " + address + ":" + port + "\n");

                        String dString = new Date().toString() + "\n"
                                + "Your address " + address.toString() + ":" + String.valueOf(port);
                        buf = dString.getBytes();
                        packet = new DatagramPacket(buf, buf.length, address, port);
                        socket.send(packet);

                    }

                    Log.e(TAG, "UDP Server ended");

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (socket != null) {
                        socket.close();
                        Log.e(TAG, "socket.close()");
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

        // ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ // accelerometer classes // ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ //
        private void init_perif() {
            // smthing
        }

        @Override
        protected void onResume() {
            super.onResume();
            sensorManager.registerListener(accelerationListener, sensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(gyroListener, sensor_gyro,
                    SensorManager.SENSOR_DELAY_FASTEST);
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
                refreshDisplay();
                AX.setText( "a_x:"+Float.toString(x));
                AY.setText( "a_y:"+Float.toString(y));
                AZ.setText( "a_z:"+Float.toString(z));
                /*GX.setText( "x:"+Float.toString(x));
                GY.setText( "x:"+Float.toString(x));
                GZ.setText( "x:"+Float.toString(x));*/

            }
        };

        private SensorEventListener gyroListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor_gyro, int acc) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {


                GX.setText( "g_x:"+Float.toString(event.values[0]));
                GY.setText( "g_y:"+Float.toString(event.values[1]));
                GZ.setText( "g_z:"+Float.toString(event.values[2]));
                refreshDisplay();

            }
        };

        private void refreshDisplay() {
            if (acc_disp == true) {
                String output = String.format("X:%3.2f m/s^2  |  Y:%3.2f m/s^2  |   Z:%3.2f m/s^2", x, y, z);
                text.setText(output);
            }

        }
    }