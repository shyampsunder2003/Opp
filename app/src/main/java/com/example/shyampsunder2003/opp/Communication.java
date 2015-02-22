package com.example.shyampsunder2003.opp;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;


public class Communication extends ActionBarActivity implements PeerListener{
    LinkedList nodeList,macList,messageList;
    DatabaseHelp databaseHelp;
    TextView textScreen;
    EditText editText;
    WifiManager wm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        editText=(EditText) findViewById(R.id.editText3);
        databaseHelp=new DatabaseHelp(this);
        nodeList=new LinkedList();
        macList=new LinkedList();
        final long timeForDeviceRefresh=600000;
        textScreen=(TextView) findViewById(R.id.textScreen);

        final Discovery discovery=new Discovery(this);
        new Thread(new Runnable() {
            public String MD5(String md5) {
                try {
                    java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                    byte[] array = md.digest(md5.getBytes());
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < array.length; ++i) {
                        sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
                    }
                    return sb.toString();
                } catch (java.security.NoSuchAlgorithmException e) {
                }
                return null;
            }
            @Override
            public void run() {
                try {
                    discovery.startReceive();
                    Thread.sleep(5000);
                    discovery.startBroadcast();
                    Thread.sleep(5000);
                    discovery.broadcastStop();
                    int i;
                    long devicetime=0;
                    for(i=0;i<nodeList.size();++i)
                    {
                        devicetime=databaseHelp.getDeviceTimestamp((String) macList.get(i));
                        if(System.currentTimeMillis()-devicetime>timeForDeviceRefresh)
                        {
//                            databaseHelp.updateDeviceTime((String) macList.get(i));
//                            messageList=databaseHelp.getMessages();
//                            if(messageList.size()!=0)
//                            {
//                                String temp="";
//                                for(int j=0;j<messageList.size();++j)
//                                {
//                                    temp+=messageList.get(j);
//                                }
//                                String hashString=MD5(temp);
//                                DatagramSocket socket = new DatagramSocket(11000);
//                                DatagramPacket packet = new DatagramPacket(hashString.getBytes(), hashString.length(),
//                                        InetAddress.getByName((String) nodeList.get(i)), 10000);
//                                socket.send(packet);
//                                byte[] buf = new byte[1024];
//                                DatagramPacket response = new DatagramPacket(buf, buf.length);
//                                socket.receive(response);
//                            }
                            final int finalI = i;
                            final long finalDevicetime = devicetime;
                            runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      String update=String.valueOf(macList.get(finalI))+": "+String.valueOf(System.currentTimeMillis()- finalDevicetime >timeForDeviceRefresh);
                                      updateScreen(update);
                                      databaseHelp.updateDeviceTime((String) macList.get(finalI));
                                  }
                              });
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    public void add(View view)
    {
        WifiInfo wInfo = wm.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
        String message=editText.getText().toString();
        databaseHelp.createMessageEntry(message,macAddress);
        editText.setText("");
    }
    @Override
    public void peerFound(final String ip, final String mac) {
        if(!nodeList.contains(ip)) {
            nodeList.addFirst(ip);
            macList.addFirst(mac);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateScreen("New node found: " + ip + " " + mac);
                }
            });
            if(!databaseHelp.containsdevice(mac))
            {
                databaseHelp.createDeviceEntry(mac);
            }
        }

    }
    public void updateScreen(String str)
    {
        String temp=textScreen.getText().toString();
        temp+='\n'+str;
        textScreen.setText(temp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_communication, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}