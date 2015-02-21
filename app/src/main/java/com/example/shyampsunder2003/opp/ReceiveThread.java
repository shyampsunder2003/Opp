package com.example.shyampsunder2003.opp;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;

/**
 * Created by shyampsunder2003 on 19-02-2015.
 */
public class ReceiveThread extends Thread {
    DatagramSocket receiveSocket;
    PeerListener receiveListener;
    LinkedList list=new LinkedList();
    ReceiveThread(DatagramSocket socket, PeerListener listener)
    {
        receiveSocket=socket;
        receiveListener=listener;
    }
    public void run()
    {
        byte[] buf = new byte[1024];
        DatagramPacket response = new DatagramPacket(buf, buf.length);
        while (true) {
            try {
                receiveSocket.receive(response);
                Log.d("Discovery", "Packet Received");
                byte[] data = response.getData();
                String message = new String(response.getData(), response.getOffset(), response.getLength(), "UTF-8");
                if (message.compareTo("I am here!") == 0) {
                    Log.d("Ack", response.getAddress().getHostAddress());
                    if (!list.contains(response.getAddress().getHostAddress())) {
                        list.addFirst(response.getAddress().getHostAddress());
                        receiveListener.peerFound(response.getAddress().getHostAddress());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    }

