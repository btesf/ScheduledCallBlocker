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

                ContactManager contactManager = null;

                try {
                    contactManager = ContactManager.getInstance(context);
                    String countryCodeValue = contactManager.getCountryCodeFromNetwork();
//TODO: you should call a service from here - because on Receive won't run for more than few seconds - if not milliseconds. This is not an appropriate place to do such DB intensive work
                    contactManager.standardizeNonStandardContactPhones(countryCodeValue);
                }
                catch(Exception e){
                    Log.d("bere.bere.bere", e.getMessage());
                }

                if(contactManager != null){
                    //if all numbers are standardized, set the preference to false
                    contactManager.setNonStandardizedPreference(false);
                }

                break;
            default:
        }
    }
}
