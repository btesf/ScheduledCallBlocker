package com.example.bereket.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Vibrator;
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

    //static listener to avoid setting multiple instance of PhoneCallStateListeners multiple times
    private static PhoneCallStateListener customPhoneListener = null;
    @Override
    public void onReceive(Context context, Intent intent) {

        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if(customPhoneListener == null){

            customPhoneListener = new PhoneCallStateListener(context);
            telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
}

class PhoneCallStateListener extends PhoneStateListener {

    private Context context;
    private ScheduleManager mScheduleManager;
    private ContactManager mContactManager;
    /*
    The static variable below is used to avoid Lolipop's issue of sending four phone states  for a single call as follows:
        RINGING
        RINGING
        IDLE
        IDLE

        For each call, as a result, two ringing events are detected and two logs would appear. To avoid that, the static variable will hold
        the previous state and once similar state is encountered the second time, it simply ignores and continue
     */
    private static int phoneState = TelephonyManager.CALL_STATE_IDLE; //random number for first initiation

    public PhoneCallStateListener(Context context){
        this.context = context;
        mScheduleManager = ScheduleManager.getInstance(context);
        mContactManager = ContactManager.getInstance(context);
    }


    @Override
    public void onCallStateChanged(int state, String incomingNumber) {

        if(state != phoneState){

            phoneState = state;

            switch (state) {

                case TelephonyManager.CALL_STATE_RINGING:

                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                    try{//this try-finally state is set here to mute audio whenever a call is made - before it is analysed.
                        //sometimes processing takes some time that audio starts to play before a call is blocked.
                        //here no matter what the audio will be muted and in the finally block, it is un-muted again
                        audioManager.setStreamMute(AudioManager.STREAM_RING, true);

                        //Determine the country code from current network (instead of system setting)
                        String countryCodeValue = mContactManager.getCountryCodeFromNetwork();
                        incomingNumber = ContactManager.standardizePhoneNumber(incomingNumber, countryCodeValue);

                        //check if a global block setting is set - if so block instantly
                        if(mContactManager.globalBlockIncomingBlockPreferenceEnabled()){

                            blockCall();
                            LogAndPostBlockService.startActionLoggerAndNotification(context, incomingNumber, BlockType.INCOMING, countryCodeValue);
                        }
                        else if(mContactManager.globalBlockNonWhitelistIncomingBlockPreferenceEnabled()){

                            //standardize any phoneNumbers with non-standard phone number (registered while the phone was out of service)
                            if(mContactManager.nonStandardizedPreferenceEnabled()){

                                mContactManager.standardizeNonStandardContactPhones(countryCodeValue);
                            }

                            Contact whiteListContact = mContactManager.getContactByStandardizedPhoneNumber(incomingNumber);

                            if(whiteListContact == null || whiteListContact.getIncomingBlockedState() != BlockState.WHITE_LIST){

                                blockCall();
                                LogAndPostBlockService.startActionLoggerAndNotification(context, incomingNumber, BlockType.INCOMING, countryCodeValue);
                            }
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

                                    LogAndPostBlockService.startActionLoggerAndNotification(context, blockedContact, BlockType.INCOMING);
                                }
                            }
                        }
                    }
                    finally{
                        //Turn OFF the mute
                        audioManager.setStreamMute(AudioManager.STREAM_RING, false);
                    }

                    break;

                case PhoneStateListener.LISTEN_CALL_STATE:
                    break;

                default:

                    audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    //Turn OFF the mute
                    audioManager.setStreamMute(AudioManager.STREAM_RING, false);

                    break;
            }
        }
        //check if there are any non-standardized numbers and run standardizing service
        super.onCallStateChanged(state, incomingNumber);
    }


    public boolean blockCall(){

        boolean callBlocked = false;

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

        return callBlocked;
    }
}