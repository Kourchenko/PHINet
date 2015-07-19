package com.ndnhealthnet.androidudpclient.Comm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;

/**
 * Handles Bluetooth Sensor Communication.
 *
 * Acts as a server (in that it can both listen and query). It has a single
 * thread that contacts each connected sensor at the user-defined interval.
 */
public class BTSensorComm extends Thread {

    private Context context;
    private BluetoothServerSocket mmServerSocket;
    private BluetoothAdapter mBluetoothAdapter;

    // TODO - loop over sensors and poll for data at user-defined intervals

    /**
     * TODO - doc
     *
     * @param context
     */
    public BTSensorComm(Context context) {
        this.context = context;

        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            // tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (Exception e) { }
        mmServerSocket = tmp;
    }

    @Override
    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                //        manageConnectedSocket(socket);
                //      mmServerSocket.close();
                break;
            }
        }
    }

    /**
     * Will cancel the listening socket and cause the thread to finish
     */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}
