package com.example.bereket.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by bereket on 8/17/15.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        PhoneCallStateListener customPhoneListener = new PhoneCallStateListener(context);
        telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    private class PhoneCallStateListener extends PhoneStateListener {

        private Context context;
        public PhoneCallStateListener(Context context){
            this.context = context;
        }


        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {

                case TelephonyManager.CALL_STATE_RINGING:

                    ContactManager contactManager = ContactManager.getInstance(context);

                    Contact blockedContact = contactManager.getContactByPhoneNumber(incomingNumber);

                    if(blockedContact == null){

                        break;
                    }

                    String block_number = blockedContact.getPhoneNumber();
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    //Turn ON the mute
                    audioManager.setStreamMute(AudioManager.STREAM_RING, true);
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    try {
                        Class clazz = Class.forName(telephonyManager.getClass().getName());
                        Method method = clazz.getDeclaredMethod("getITelephony");
                        method.setAccessible(true);
                        ITelephony telephonyService = (ITelephony) method.invoke(telephonyManager);

                        if(incomingNumber.equals(blockedContact.getPhoneNumber())) {
                            //if outgoing call is blocked proceed with blocking
                            if (blockedContact.isIsIncomingBlocked()) {
                                //telephonyService.silenceRinger();//Security exception problem
                                telephonyService = (ITelephony) method.invoke(telephonyManager);
                                telephonyService.silenceRinger();
                                telephonyService.endCall();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                    }
                    //Turn OFF the mute
                    audioManager.setStreamMute(AudioManager.STREAM_RING, false);
                    break;
                case PhoneStateListener.LISTEN_CALL_STATE:

            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    private interface ITelephony {

        boolean endCall();

        void answerRingingCall();

        void silenceRinger();
    }
}
