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

        public PhoneCallStateListener(Context context){
            this.context = context;
            mScheduleManager = ScheduleManager.getInstance(context);
        }


        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            //Determine the country code from current network (instead of system setting)
            //TODO better to use system wide configuration setting to get country code if the value is empty from telephone manager
            TelephonyManager tm = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
            String countryCodeValue = tm.getNetworkCountryIso();

            incomingNumber = ContactManager.standardizePhoneNumber(incomingNumber, countryCodeValue);
            ContactManager contactManager = ContactManager.getInstance(context);
            //standardize any phoneNumbers with non-standard phone number (while the phone was out of service)
            if(contactManager.nonStandardizedPreferenceEnabled()){

                contactManager.standardizeNonStandardContactPhones(countryCodeValue);
            }

            switch (state) {

                case TelephonyManager.CALL_STATE_RINGING:

                    Contact blockedContact = contactManager.getContactByPhoneNumber(incomingNumber);

                    if(blockedContact == null){

                        break;
                    }

                    //check if current phone number is standardized; if not standardize
                    String phoneNumber = blockedContact.getPhoneNumber();
                    if(!blockedContact.isIsNumberStandardized()){
                        phoneNumber = ContactManager.standardizePhoneNumber(phoneNumber, countryCodeValue);
                    }
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
                            if (blockedContact.getIncomingBlockedState() == BlockState.ALWAYS_BLOCK) {
                                //telephonyService.silenceRinger();//Security exception problem
                                telephonyService = (ITelephony) method.invoke(telephonyManager);
                                telephonyService.silenceRinger();
                                telephonyService.endCall();
                                sendNotification(blockedContact.getDisplayNumber());
                            }
                            else if(blockedContact.getOutGoingBlockedState() == BlockState.SCHEDULED_BLOCK){
                                //get all the scheduled calls for this number
                                Calendar cal = Calendar.getInstance();
                                //get weekDay of today
                                int weekDay = TimeHelper.convertJavaDayOfWeekWithCallBlockerType(cal.get(Calendar.DAY_OF_WEEK));
                                //get a benchmarkCalendar that sets the month and year part to a standard/benchmark date so that only time search can happen
                                cal = TimeHelper.getBenchmarkCalendar();
                                if(mScheduleManager.timeExistsInSchedule(blockedContact.getId(),BlockType.INCOMING, weekDay, cal.getTime())){
                                    setResultData(null);
                                    //not recommended to abort broadcast
                                    //abortBroadcast();
                                    sendNotification(blockedContact.getDisplayNumber());
                                }
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                    }
                    //Turn OFF the mute
                    audioManager.setStreamMute(AudioManager.STREAM_RING, false);
                    break;

                case PhoneStateListener.LISTEN_CALL_STATE:
                    break;

            }
            //check if there are any non-standardized numbers and run standardizing service
            super.onCallStateChanged(state, incomingNumber);
        }

        private void sendNotification(String phoneNumber){
            Intent i  = new Intent(context, CallBlockerActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
            Notification notification = new NotificationCompat.Builder(context)
                    .setTicker("Call blocker")
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle("New incoming call intercepted")
                    .setContentText(phoneNumber + " is intercepted")
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
        }
    }
}
