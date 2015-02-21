package com.example.shyampsunder2003.opp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import static android.widget.Toast.LENGTH_SHORT;


public class MainActivity extends ActionBarActivity {

    TextView text;
    EditText e1,e2;
    public static volatile boolean flag=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text=(TextView) findViewById(R.id.textView);
        e1=(EditText) findViewById(R.id.editText);
        e2=(EditText) findViewById(R.id.editText2);
    }
    public void begin(View view)
    {
        WifiManager mainWifiObj;
        flag=false;
        Random r=new Random();
        int start=Integer.parseInt(e1.getText().toString());
        int end=Integer.parseInt(e2.getText().toString());
        final int randomval=r.nextInt(end-start)+start;
        text.setText(String.valueOf(randomval));
        flag=true;
        new Thread(new Runnable() {
            public void run() {

                try {
                    int i;
                    for(i = 0; i < randomval && flag; i++) {
                        Thread.sleep(1000);
                        Log.d("Sleep",String.valueOf(i));
                    }
                    if(flag)
                    {
                        Log.d("Hotspot","Turned on");
                        createWifiAccessPoint();
                    }
                } catch (InterruptedException e) {

                }

            }
        }).start();
        mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiScanReceiver wifiReciever = new WifiScanReceiver(mainWifiObj,flag);
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifiObj.disconnect();
        mainWifiObj.startScan();

    }
    private void createWifiAccessPoint() {
        WifiManager wifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(false);
        }
        Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();   //Get all declared methods in WifiManager class
        boolean methodFound=false;
        for(Method method: wmMethods){
            if(method.getName().equals("setWifiApEnabled")){
                methodFound=true;
                WifiConfiguration netConfig = new WifiConfiguration();
                netConfig.allowedAuthAlgorithms.clear();
                netConfig.allowedGroupCiphers.clear();
                netConfig.allowedPairwiseCiphers.clear();
                netConfig.allowedProtocols.clear();
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                netConfig.SSID="Opp";
                try {
                    boolean apstatus=(Boolean) method.invoke(wifiManager, netConfig,true);
                    //statusView.setText("Creating a Wi-Fi Network \""+netConfig.SSID+"\"");
                    for (Method isWifiApEnabledmethod: wmMethods)
                    {
                        if(isWifiApEnabledmethod.getName().equals("isWifiApEnabled")){
                            while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){
                            };
                            for(Method method1: wmMethods){
                                if(method1.getName().equals("getWifiApState")){
                                    int apstate;
                                    apstate=(Integer)method1.invoke(wifiManager);
                                    //                    netConfig=(WifiConfiguration)method1.invoke(wifi);
                                    //statusView.append("\nSSID:"+netConfig.SSID+"\nPassword:"+netConfig.preSharedKey+"\n");
                                }
                            }
                        }
                    }
                    if(apstatus)
                    {
                        System.out.println("SUCCESSdddd");
                        //statusView.append("\nAccess Point Created!");
                        //finish();
                        //Intent searchSensorsIntent = new Intent(this,SearchSensors.class);
                        //startActivity(searchSensorsIntent);
                    }else
                    {
                        System.out.println("FAILED");
                        //statusView.append("\nAccess Point Creation failed!");
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!methodFound){
            //statusView.setText("Your phone's API does not contain setWifiApEnabled method to configure an access point");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

