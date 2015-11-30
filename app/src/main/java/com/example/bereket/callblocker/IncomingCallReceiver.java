package com.example.bereket.callblocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.util.Calendar;

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
        private ScheduleManager mScheduleManager;
        private LogManager mLogManager;
        private ContactManager mContactManager;

        public PhoneCallStateListener(Context context){
            this.context = context;
            mScheduleManager = ScheduleManager.getInstance(context);
            mLogManager = LogManager.getInstance(context);
            mContactManager = ContactManager.getInstance(context);
        }


        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {

                case TelephonyManager.CALL_STATE_RINGING:

                    //Determine the country code from current network (instead of system setting)
                    String countryCodeValue = mContactManager.getCountryCodeFromNetwork();
                    incomingNumber = ContactManager.standardizePhoneNumber(incomingNumber, countryCodeValue);

                    //check if a global block setting is set - if so block instantly
                    if(mContactManager.globalBlockIncomingBlockPreferenceEnabled()){

                        blockCall();
                        LoggerAndNotificationService.startActionLoggerAndNotification(context, incomingNumber, BlockType.INCOMING, countryCodeValue);
                    }
                    else{
                        //standardize any phoneNumbers with non-standard phone number (registered while the phone was out of service)
                        if(mContactManager.nonStandardizedPreferenceEnabled()){

                            mContactManager.standardizeNonStandardContactPhones(countryCodeValue);
                        }

                        Contact blockedContact = mContactManager.getContactByStandardizedPhoneNumber(incomingNumber);

                        if(blockedContact == null){

                            break;
                        }

                        if(incomingNumber.equals(blockedContact.getPhoneNumber())) {

                            boolean callBlocked = false;
                            //if outgoing call is blocked proceed with blocking
                            if (blockedContact.getIncomingBlockedState() == BlockState.ALWAYS_BLOCK) {

                                callBlocked = blockCall();
                            }
                            else if(blockedContact.getIncomingBlockedState() == BlockState.SCHEDULED_BLOCK){
                                //get all the scheduled calls for this number
                                Calendar cal = Calendar.getInstance();
                                //get weekDay of today
                                int weekDay = TimeHelper.convertJavaDayOfWeekWithCallBlockerType(cal.get(Calendar.DAY_OF_WEEK));
                                //get a benchmarkCalendar that sets the month and year part to a standard/benchmark date so that only time search can happen
                                cal = TimeHelper.setCalendarToBenchmarkTime(cal);
                                if(mScheduleManager.timeExistsInSchedule(blockedContact.getId(),BlockType.INCOMING, weekDay, cal.getTime())){

                                    callBlocked = blockCall();
                                }
                            }

                            if(callBlocked){

                                LoggerAndNotificationService.startActionLoggerAndNotification(context, blockedContact, BlockType.INCOMING);
                            }
                        }
                    }

                    break;

                case PhoneStateListener.LISTEN_CALL_STATE:
                    break;

            }
            //check if there are any non-standardized numbers and run standardizing service
            super.onCallStateChanged(state, incomingNumber);
        }


        public boolean blockCall(){

            boolean callBlocked = false;

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            //Turn ON the mute
            audioManager.setStreamMute(AudioManager.STREAM_RING, true);
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            try {

                Class clazz = Class.forName(telephonyManager.getClass().getName());
                Method method = clazz.getDeclaredMethod("getITelephony");
                method.setAccessible(true);
                ITelephony telephonyService = (ITelephony) method.invoke(telephonyManager);
                telephonyService.silenceRinger();
                telephonyService.endCall();
                callBlocked = true;

            } catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
            }
            //Turn OFF the mute
            audioManager.setStreamMute(AudioManager.STREAM_RING, false);

            return callBlocked;
        }
    }
}
