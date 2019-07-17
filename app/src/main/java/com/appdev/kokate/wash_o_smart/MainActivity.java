package com.appdev.kokate.wash_o_smart;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private final String DEVICE_ADDRESS="20:13:10:15:33:66";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothAdapter myBluetooth = null;
    private BluetoothDevice device;
    private BluetoothSocket bluetoothSocket;
    boolean stopThread;
    private InputStream inputStream;
    private TextView textView;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



    }

    public boolean BTinit() {
        boolean found = false;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device does not support bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter,0);
        }
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();

        if (bluetoothDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(),"Please pair the device first!", Toast.LENGTH_SHORT).show();
        }
        else {
            for (BluetoothDevice iterator: bluetoothDevices ) {
                if (iterator.getAddress().equals(DEVICE_ADDRESS)) {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }

        return found;


    }

    public boolean BTconnect() {
        boolean connected = true;

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(PORT_UUID);
        } catch (IOException io) {
            io.printStackTrace();
            connected=false;
        }

        if (connected) {
            try {
                inputStream = bluetoothSocket.getInputStream();
            }catch (IOException e) {
                e.printStackTrace();
            }

        }

        return connected;
    }

    void beginListenForData() {
        final Handler handler = new Handler();

        stopThread = false;
        buffer = new byte[1024];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    try
                    {
                        int byteCount = inputStream.available();

                        if (byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes,"UTF-8");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textView.append(string);
                                }
                            });
                            
                        }
                    } catch (IOException e) {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();

    }
}

