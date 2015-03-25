package com.example.shyampsunder2003.opp;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
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
        editText = (EditText) findViewById(R.id.editText3);
        if (databaseHelp == null)
            databaseHelp = new DatabaseHelp(this);
//        //databaseHelp.open();
//        databaseHelp.delete();
//        //databaseHelp.open();

            nodeList = new LinkedList();
            macList = new LinkedList();
            final long timeForDeviceRefresh = 60;
            textScreen = (TextView) findViewById(R.id.textScreen);
            final Discovery discovery = new Discovery(this);
            new Thread(new Runnable() {
                public String MD5(String md5) {
                    try {
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        byte[] array = md.digest(md5.getBytes());
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < array.length; ++i) {
                            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
                        }
                        return sb.toString();
                    } catch (NoSuchAlgorithmException e) {
                    }
                    return null;
                }

                @Override
                public void run() {
                    try {
                        discovery.startReceive();
                        Thread.sleep(5000);
                        discovery.startBroadcast();
                        Thread.sleep(10000);
                        while (nodeList.size() == 0) ;
                        discovery.broadcastStop();
                        int i;
                        long devicetime = 0;
                        for (i = 0; i < nodeList.size(); ++i) {
                            Log.d("Communication", "nodeList");
                            //databaseHelp.open();
                            devicetime = databaseHelp.getDeviceTimestamp((String) macList.get(i));
                            //databaseHelp.close();
                            if (System.currentTimeMillis() - devicetime > timeForDeviceRefresh) {
                            //databaseHelp.open();
                            databaseHelp.updateDeviceTime((String) macList.get(i));
                            messageList=databaseHelp.getMessages();
                            //databaseHelp.close();
                            if(messageList.size()!=0)
                            {
                                String temp="";
                                for(int j=0;j<messageList.size();++j)
                                {
                                    temp+="/"+messageList.get(j);
                                }
                                String hashString=MD5(temp);
                                String request="getMessageListHash*";
                                DatagramSocket socket = new DatagramSocket(11000);
                                DatagramPacket packet = new DatagramPacket(request.getBytes(), request.length(),
                                        InetAddress.getByName((String) nodeList.get(i)), 10000);
                                socket.send(packet);
                                byte[] buf = new byte[1024];
                                DatagramPacket response = new DatagramPacket(buf, buf.length);
                                socket.receive(response);
                                String messageString = new String(response.getData(), response.getOffset(), response.getLength(), "UTF-8");
                                final String finalMessageString = messageString;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateScreen(finalMessageString);
                                    }
                                });
                                //databaseHelp.open();
                                int compareResult=databaseHelp.getMessageListHash().compareTo(messageString);
                                //databaseHelp.close();
                                if(compareResult==0)
                                {
                                    final DatagramPacket finalResponse = response;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateScreen("No messages to transfer to "+ finalResponse.getAddress());
                                        }
                                    });
                                    socket.close();
                                }
                                else
                                {
                                    request="getMessageHashSize*";
                                    packet = new DatagramPacket(request.getBytes(), request.length(),
                                            InetAddress.getByName((String) nodeList.get(i)), 10000);
                                    socket.send(packet);
                                    response=new DatagramPacket(buf,buf.length);
                                    socket.receive(response);
                                    messageString=  new String(response.getData(), response.getOffset(), response.getLength(), "UTF-8");
                                    int messageListSize=Integer.parseInt(messageString);
                                    for(int j=0;j<messageListSize;++j)
                                    {
                                        request="getMessageHash"+j+"*";
                                        packet = new DatagramPacket(request.getBytes(), request.length(),
                                                InetAddress.getByName((String) nodeList.get(i)), 10000);
                                        socket.send(packet);
                                        response=new DatagramPacket(buf,buf.length);
                                        socket.receive(response);
                                        messageString=  new String(response.getData(), response.getOffset(), response.getLength(), "UTF-8");
                                        //databaseHelp.open();
                                        boolean containsMessage=databaseHelp.containsMessage(messageString);
                                        //databaseHelp.close();
                                        if(!containsMessage)
                                        {
                                            request="getMessage"+j+"*";
                                            packet = new DatagramPacket(request.getBytes(), request.length(),
                                                    InetAddress.getByName((String) nodeList.get(i)), 10000);
                                            socket.send(packet);
                                            response=new DatagramPacket(buf,buf.length);
                                            socket.receive(response);
                                            messageString=  new String(response.getData(), response.getOffset(), response.getLength(), "UTF-8");
                                            final String message=messageString.substring(0,messageString.indexOf("*"));
                                            String mac=messageString.substring(messageString.indexOf("*") + 1, messageString.length());
                                            //databaseHelp.open();
                                            databaseHelp.createMessageEntry(message,mac);
                                            //databaseHelp.close();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    updateScreen("Message: "+ message+" added to database");
                                                }
                                            });
                                        }
                                    }
                                }

                            }
                            } else {
                                final long finalDevicetime1 = devicetime;
                                final int finalI1 = i;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateScreen("Not new Device");
                                    }
                                });

                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
    }
    public String MD5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }
    public void add(View view)
    {
        WifiInfo wInfo = wm.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
        String message=editText.getText().toString();
        //databaseHelp.open();
        databaseHelp.createMessageEntry(message, macAddress);
        editText.setText("");
        //databaseHelp.close();
    }
    public void done(View view)
    {
        //databaseHelp.open();
        LinkedList messages=databaseHelp.getMessages();
        updateScreen("Message List");
        for(int i=0;i<messages.size();++i)
        {
            updateScreen(String.valueOf(i)+":"+messages.get(i));
        }
        updateScreen("Message List Hash String");
        updateScreen(databaseHelp.getMessageListHash());
        //databaseHelp.close();
    }
    @Override
    public void peerFound(final String ip, final String mac) {
        if(!nodeList.contains(ip)) {
            nodeList.addFirst(ip);
            macList.addFirst(mac);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateScreen("New node found: " + mac);
                }
            });
            Log.d("Peer Found",ip+" "+mac+String.valueOf(databaseHelp==null));
            if (databaseHelp==null)
                databaseHelp=new DatabaseHelp(this);
            //databaseHelp.open();
            if(!databaseHelp.containsdevice(mac))
            {
                databaseHelp.createDeviceEntry(mac);
                Log.d("Peer Found",ip+" "+mac+String.valueOf(databaseHelp==null));
            }
            //databaseHelp.close();


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
