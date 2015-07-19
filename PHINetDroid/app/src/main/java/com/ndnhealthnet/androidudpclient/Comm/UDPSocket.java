package com.ndnhealthnet.androidudpclient.Comm;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class handles outbound UDP packets and listens when reply expected.
 */
public class UDPSocket extends AsyncTask<byte[], Void, Void> {

    String destAddr;
    int destPort;

    public UDPSocket(int port, String addr){
        destPort = port;
        destAddr = addr;
    }

    @Override
    protected Void doInBackground(byte[]... message) {

        try {
            final DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(destAddr);
            byte[] packetContent = message[0];

            DatagramPacket sendPacket = new DatagramPacket(packetContent, packetContent.length, IPAddress, destPort);
            clientSocket.send(sendPacket);

            /**
             * if we've just sent a packet, we must listen for incoming packet; otherwise
             * we may not be able to detect incoming, requested packets from the server
             *
             * listen in new thread for 2 seconds (arbitrarily chosen)
             */
            Timer t = new Timer();
            t.schedule(new TimerTask() {

                @Override
                public void run() {

                    byte[] receiveData = new byte[1024];


                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    try {
                        clientSocket.setSoTimeout(2000); // only listen for 2 seconds
                        clientSocket.receive(receivePacket);
                        String packetSourceIP = receivePacket.getAddress().getHostAddress();
                        int packetPort = receivePacket.getPort();

                        UDPListener.handleNDNPacket(receiveData, packetSourceIP, packetPort);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    clientSocket.close();
                    this.cancel();

                }
            }, 0); // start listening now (i.e., wait 0 seconds)

        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return null;
    }
}