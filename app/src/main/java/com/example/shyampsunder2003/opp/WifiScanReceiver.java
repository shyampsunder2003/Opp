package com.example.shyampsunder2003.opp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by shyampsunder2003 on 21-02-2015.
 */
class WifiScanReceiver extends BroadcastReceiver {
    WifiManager wifiManager;
    public static volatile boolean flag=true;
    String networkSSID = "Opp";
    WifiScanReceiver(WifiManager wifi, boolean status) {
        wifiManager = wifi;
        flag=status;
    }

    public void onReceive(Context c, Intent intent) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiManager.addNetwork(conf);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        Log.d("Wifi", "Reached the connection phase ");
        for (WifiConfiguration i : list) {
            if(wifiManager.getConnectionInfo().getSSID()==null) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    //wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    //wifiManager.reconnect();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("Wifi", "Reached the connection phase inside loop ");
                    break;
                }
            }
        }

    }
}