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
    ReceiveThread(DatagramSocket socket, PeerListener listener)
    {
        receiveSocket=socket;
        receiveListener=listener;
        mContext= (Context) listener;
        wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public void run()
    {
        byte[] buf = new byte[1024];
        DatagramPacket response = new DatagramPacket(buf, buf.length);
        while (true) {
            try {
                receiveSocket.receive(response);
                byte[] data = response.getData();
                String messageString = new String(response.getData(), response.getOffset(), response.getLength(), "UTF-8");
                Log.d("Discovery", "Packet Received from "+response.getAddress().getHostAddress()+" "+messageString);
                message=messageString.substring(0,messageString.indexOf("*"));
                if (message.compareTo("I am here!") == 0) {
                    Log.d("Ack", response.getAddress().getHostAddress());
                    if (!list.contains(response.getAddress().getHostAddress())) {
                        list.addFirst(response.getAddress().getHostAddress());
                        String mac=messageString.substring(messageString.indexOf("*")+1,messageString.length());
                        receiveListener.peerFound(response.getAddress().getHostAddress(),mac);
                    }
                }
                else if(message.compareTo("Broadcast") == 0)
                {
                    WifiInfo wInfo = wm.getConnectionInfo();
                    String macAddress = wInfo.getMacAddress();
                    message=messageString.substring(messageString.indexOf("*")+1,messageString.length());
                    if( message.compareTo(macAddress)!=0) {
                        String responseString = "I am here!"+"*"+macAddress;
                        data = responseString.getBytes();
                        DatagramPacket packet=new DatagramPacket(data,data.length,response.getAddress(),10000);
                        receiveSocket.send(packet);
                        Log.d("Receive Thread","Packet Sent to "+response.getAddress().getHostAddress()+" "+macAddress+" "+message);
                    }

                }
                else if(message.compareTo("getMessageListHash")==0)
                {

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                receiveSocket.close();
            }

        }
    }
    }

