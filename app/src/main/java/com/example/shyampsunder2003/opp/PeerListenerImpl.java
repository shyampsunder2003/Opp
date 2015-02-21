package com.example.shyampsunder2003.opp;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by shyampsunder2003 on 22-02-2015.
 */
public class PeerListenerImpl extends Activity implements PeerListener {
    boolean apNode;
    Discovery discovery;
    PeerListenerImpl(boolean ap)
    {
        apNode=ap;
        discovery=new Discovery(this);
        if(apNode)
        {
            discovery.startBroadcast();
        }
        else
        {
            discovery.startReceive();
        }
    }


    @Override
    public void peerFound(String ip) {
        Toast.makeText(this,"Node found "+ip,Toast.LENGTH_LONG).show();
    }
}
