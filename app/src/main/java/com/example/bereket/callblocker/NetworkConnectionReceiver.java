package com.example.bereket.callblocker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by bereket on 8/23/15.
 */
public class NetworkConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        TelephonyManager tm = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);


        switch(tm.getSimState()){

            case TelephonyManager.SIM_STATE_READY:
                    Log.d("bere.bere.bere", "SIM CARD IS READY..........");
                break;
            default:
                Log.d("bere.bere.bere", "Default........unknown state");
        }

        ComponentName receiver = new ComponentName(context, "com.example.bereket.callblocker.NetworkConnectionReceiver.class");

        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}
