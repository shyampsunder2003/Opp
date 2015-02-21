package com.example.shyampsunder2003.opp;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by shyampsunder2003 on 19-02-2015.
 */
public class SendThread extends Thread {
    DatagramSocket sendSocket;
    Context mContext;
    SendThread(Context c) throws SocketException {
        sendSocket=new DatagramSocket();
        mContext=c;
    }
    public void run()
    {
        String data = "Broadcast";
        try {
            if (sendSocket != null) {
                sendSocket.setBroadcast(true);
            } else {
                sendSocket = new DatagramSocket();
                sendSocket.setBroadcast(true);
                sendSocket.setSoTimeout(0);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket(data.getBytes(), data.length(),
                    getBroadcastAddress(), 10000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (sendSocket != null) {
                sendSocket.send(packet);
                Log.d("Discovery", "Broadcast done");
            } else {
                sendSocket = new DatagramSocket();
                sendSocket.send(packet);
                Log.d("Discovery", "Broadcast done");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

}

