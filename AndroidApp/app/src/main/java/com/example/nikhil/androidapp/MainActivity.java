package com.example.nikhil.androidapp;

import java.net.InetAddress;

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


    public class MainActivity extends Activity {
        private final static String TAG = MainActivity.class.getSimpleName();

        private SensorManager sensorManager;
        private Sensor sensor,sensor_gyro;
        boolean acc_disp = false;
        EditText port;
        TextView AX, AY,AZ, GX,GY,GZ;
        Button button;
        private boolean connected = false;


        private float x, y, z;
        TextView infoIp, infoPort;
        TextView textViewState, textViewPrompt;

        static final int UdpServerPORT = 5000;
        UdpServerThread udpServerThread;
        DatagramSocket socket;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            infoIp = (TextView) findViewById(R.id.infoip);
            infoPort = (TextView) findViewById(R.id.infoport);
            textViewState = (TextView) findViewById(R.id.state);
            textViewPrompt = (TextView) findViewById(R.id.prompt);

            port = (EditText) findViewById(R.id._port);
            //final String Port = port.getText().toString().trim();

            AX = (TextView) findViewById(R.id._ax);
            AY = (TextView) findViewById(R.id._ay);
            AZ = (TextView) findViewById(R.id._az);
            GX = (TextView) findViewById(R.id._gx);
            GY = (TextView) findViewById(R.id._gy);
            GZ = (TextView) findViewById(R.id._gz);
            button = (Button) findViewById(R.id._start_server);
            button.setOnClickListener(connectListener);

            infoIp.setText(getIpAddress());
            infoPort.setText(String.valueOf(UdpServerPORT));

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            sensor_gyro = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR).get(0);
            acc_disp = false;
            updateState("UDP Server is not running");
        }

        private Button.OnClickListener connectListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!connected) {

                    button.setText("Stop");
                    connected = true;
                    udpServerThread = new UdpServerThread(UdpServerPORT);
                    udpServerThread.start();
                }
                else{
                    button.setText("Start Server");
                    udpServerThread.setRunning(false);
                    updateState("UDP Server is not running");
                    connected=false;
                    acc_disp=false;
                    //socket.close();
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
                        Log.d(TAG, "waiting for connection");
                        socket.receive(packet);     //this code block the program flow
                        Log.d(TAG, "Got connection");
                        // send the response to the client at "address" and "port"
                        InetAddress address = packet.getAddress();
                        int port = packet.getPort();

                        updateState("Request from: " + address + ":" + port + "\n");

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


                GX.setText( "g_x:"+Float.toString(event.values[0]));
                GY.setText( "g_y:"+Float.toString(event.values[1]));
                GZ.setText( "g_z:"+Float.toString(event.values[2]));

            }
        };

    }