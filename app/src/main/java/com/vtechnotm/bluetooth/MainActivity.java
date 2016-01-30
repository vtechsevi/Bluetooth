package com.vtechnotm.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "00:15:FF:F2:19:5F";


    AppCompatButton on, off;
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        on = (AppCompatButton) findViewById(R.id.on);
        off = (AppCompatButton) findViewById(R.id.off);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        chekBTState();


    }

    private void chekBTState() {
        if (bluetoothAdapter == null) {
            Snackbar.make(findViewById(R.id.on), "ERROR", Snackbar.LENGTH_SHORT).show();
            return;
        } else {

            if (bluetoothAdapter.isEnabled()) {
                Snackbar.make(findViewById(R.id.on), "TOURN ON..", Snackbar.LENGTH_SHORT).show();
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Snackbar.make(findViewById(R.id.on), "Could not create Insecure RFComm Connection", Snackbar.LENGTH_SHORT).show();

            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            bluetoothSocket = createBluetoothSocket(device);
        } catch (IOException e1) {
            Snackbar.make(findViewById(R.id.on), "ERROR IN CREATING SOCKET", Snackbar.LENGTH_SHORT).show();
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        bluetoothAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.

        try {
            bluetoothSocket.connect();
            Snackbar.make(findViewById(R.id.on), "CONNECT OK", Snackbar.LENGTH_SHORT).show();
        } catch (IOException e) {
            try {
                bluetoothSocket.close();
                Snackbar.make(findViewById(R.id.on), "ERROR IN CONNECTING TO SOCKET", Snackbar.LENGTH_SHORT).show();
            } catch (IOException e2) {
                Snackbar.make(findViewById(R.id.on), "ERROR IN CLOSING SOCKET", Snackbar.LENGTH_SHORT).show();
            }
        }

        // Create a data stream so we can talk to server.


        try {
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Snackbar.make(findViewById(R.id.on), "ERROR IN GET OUTPUTSTREAM", Snackbar.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onPause() {
        super.onPause();


        if (outputStream != null) {
            try {
                outputStream.flush();
            } catch (IOException e) {

            }
        }

        try {
            bluetoothSocket.close();
        } catch (IOException e2) {

        }
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();


        try {
            outputStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";


        }
    }
}
