package com.example.shyampsunder2003.opp;

import java.util.TimerTask;

/**
 * Created by shyampsunder2003 on 19-02-2015.
 */
public class DiscoveryBroadcastTimer extends TimerTask {

    SendThread sendThread;
    DiscoveryBroadcastTimer(SendThread t)
    {
        sendThread=t;
    }
    @Override
    public void run() {
        sendThread.run();
    }
}
