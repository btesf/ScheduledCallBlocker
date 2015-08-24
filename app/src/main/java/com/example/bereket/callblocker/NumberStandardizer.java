package com.example.bereket.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by bereket on 8/20/15.
 */
public class NumberStandardizer extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        PhoneServiceStateListener customPhoneListener = new PhoneServiceStateListener(context);
        telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_SERVICE_STATE);
    }

    private class PhoneServiceStateListener extends PhoneStateListener{

        private Context context;
        public PhoneServiceStateListener(Context context){
            this.context = context;
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            switch(serviceState.getState()){
                case ServiceState.STATE_IN_SERVICE:
                    //if(previou)
                    Log.d("bere.bere.bere", ".....................IN SERVICE..........");
                    break;
                case ServiceState.STATE_OUT_OF_SERVICE:
                    Log.d("bere.bere.bere", "......... OUT OF SERVICE ................");
                    break;
                case ServiceState.STATE_EMERGENCY_ONLY:
                    Log.d("bere.bere.bere", "......... EMERGENCY CALL ONLY ..............");
                    break;
                //case ServiceState.
                default:
                    break;
            }
        }

    }
}
