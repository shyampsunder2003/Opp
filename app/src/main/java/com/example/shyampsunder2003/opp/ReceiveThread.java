package com.example.shyampsunder2003.opp;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * Created by shyampsunder2003 on 19-02-2015.
 */
public class ReceiveThread extends Thread {
    DatagramSocket receiveSocket;
    PeerListener receiveListener;
    LinkedList list=new LinkedList();
    Context mContext;
    WifiManager wm;
    String message;
    DatabaseHelp databaseHelp;
    ReceiveThread(DatagramSocket socket, PeerListener listener)
    {
        receiveSocket=socket;
        receiveListener=listener;
        mContext= (Context) listener;
        wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        databaseHelp=new DatabaseHelp((Context) listener);
    }

    public void run()
    {
        byte[] buf = new byte[1024];
        DatagramPacket response = new DatagramPacket(buf, buf.length);
        try {
        while (true) {

            //databaseHelp.open();
            receiveSocket.receive(response);
            byte[] data = response.getData();
            String messageString = new String(response.getData(), response.getOffset(), response.getLength(), "UTF-8");
            Log.d("Discovery", "Packet Received from " + response.getAddress().getHostAddress() + " " + messageString);
            String mac = null;
            message = messageString.substring(0, messageString.indexOf("*"));
            if (message.compareTo("I am here!") == 0) {
                Log.d("Ack", response.getAddress().getHostAddress());
                //if (!list.contains(response.getAddress().getHostAddress())) {
                //    list.addFirst(response.getAddress().getHostAddress());
                mac = messageString.substring(messageString.indexOf("*") + 1, messageString.length());
                receiveListener.peerFound(response.getAddress().getHostAddress(), mac);
                //}
            } else if (message.compareTo("Broadcast") == 0) {
                WifiInfo wInfo = wm.getConnectionInfo();
                String macAddress = wInfo.getMacAddress();
                message = messageString.substring(messageString.indexOf("*") + 1, messageString.length());
                if (message.compareTo(macAddress) != 0) {
                    String responseString = "I am here!" + "*" + macAddress;
                    data = responseString.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, response.getAddress(), 10000);
                    receiveSocket.send(packet);
                    Log.d("Receive Thread", "Packet Sent to " + response.getAddress().getHostAddress() + " " + macAddress + " " + message);
                }

            } else if (message.compareTo("getMessageListHash") == 0) {
                    //databaseHelp.open();
                String hash = databaseHelp.getMessageListHash();
                data = hash.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, response.getAddress(), 11000);
                receiveSocket.send(packet);
                //databaseHelp.close();
            } else if (message.compareTo("getMessageHashSize") == 0) {
                    //databaseHelp.open();
                LinkedList l = databaseHelp.getMessages();
                int size = l.size();
                String messageSize = String.valueOf(size);
                data = messageSize.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, response.getAddress(), 11000);
                receiveSocket.send(packet);
                    //databaseHelp.close();
            } else if (message.substring(0, message.length() - 1).compareTo("getMessageHash") == 0) {
                int messageNumber = Integer.parseInt(message.substring(message.length() - 1, message.length()));
                    //databaseHelp.open();
                LinkedList l = databaseHelp.getMessagesHash();
                String messageHashResponse = (String) l.get(messageNumber);
                data = messageHashResponse.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, response.getAddress(), 11000);
                receiveSocket.send(packet);
                    //databaseHelp.close();
            } else if (message.substring(0, message.length() - 1).compareTo("getMessage") == 0) {
                int messageNumber = Integer.parseInt(message.substring(message.length() - 1, message.length()));
                    //databaseHelp.open();
                LinkedList l = databaseHelp.getMessages();
                String messageResponse = (String) l.get(messageNumber);
                messageResponse += "*" + mac;
                data = messageResponse.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, response.getAddress(), 11000);
                receiveSocket.send(packet);
                    //databaseHelp.close();
            }
        }
        }
        catch (IOException e) {
                e.printStackTrace();
        }




    }
    }

