package com.example.shyampsunder2003.opp;

import android.content.Context;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shyampsunder2003 on 19-02-2015.
 */
public class Discovery {
    DatagramSocket socket = null;
    SendThread sendThread;
    ReceiveThread receiveThread;
    PeerListener mContext;
    TimerTask timerTask;
    Discovery(PeerListener c)
    {

        mContext=c;
        try {
            socket=new DatagramSocket(10000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    public void startBroadcast(){
        try {
            if(socket==null)
            socket = new DatagramSocket(10000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            sendThread=new SendThread((Context) mContext);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        receiveThread=new ReceiveThread(socket, mContext);
        timerTask=new DiscoveryBroadcastTimer(sendThread);
        Timer timer = new Timer();
        timer.schedule(timerTask, 0,5000);
        if(!receiveThread.isAlive())
            receiveThread.start();
    }
    public void startReceive()
    {
        receiveThread=new ReceiveThread(socket, (PeerListener) mContext);
        if(!receiveThread.isAlive())
            receiveThread.start();
    }
    public void broadcastStop()
    {
        timerTask.cancel();
    }
}
