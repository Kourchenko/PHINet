package com.ndnhealthnet.androidudpclient.Comm;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.Activities.MainActivity;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

/**
 * Class handles incoming UDP packets.
 */
public class NetworkListener extends Thread {

    static  DatagramSocket clientSocket = null;
    static Context context;

    public NetworkListener(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        try {
            InetSocketAddress address = new InetSocketAddress(MainActivity.deviceIP, ConstVar.PHINET_PORT);

            clientSocket = new DatagramSocket(null);
            clientSocket.bind(address); // give receiver static address

            byte[] receiveData = new byte[1024];

            while (MainActivity.continueReceiverExecution) { // loop for packets while true

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                try {
                    clientSocket.receive(receivePacket);

                    String hostIP = receivePacket.getAddress().toString();  //.getAddress().getHostAddress();
                    int hostPort = receivePacket.getPort();

                    handlePacket(receiveData, hostIP, hostPort);

                } catch (SocketTimeoutException e) {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }

    /**
     * Helper method that handles all incoming packets;
     * may be invoked from elsewhere in code (namely, NetworkSocket)
     *
     * @param packet received
     * @param hostIP of sender
     * @param hostPort of sender
     */
    static void handlePacket(byte[] packet, String hostIP, int hostPort) {
      // TODO -
    }
}