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

        final Context mContext = context;
        final PendingResult result = goAsync();

        Thread thread = new Thread() {

            public void run() {

                TelephonyManager tm = (TelephonyManager)mContext.getSystemService(mContext.TELEPHONY_SERVICE);

                switch(tm.getSimState()){

                    case TelephonyManager.SIM_STATE_READY:

                        ContactManager contactManager = null;

                        try {
                            contactManager = ContactManager.getInstance(mContext);
                            String countryCodeValue = contactManager.getCountryCodeFromNetwork();
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

                result.finish();
            }
        };

        thread.start();
    }
}
