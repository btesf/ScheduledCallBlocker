package com.example.bereket.callblocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by bereket on 7/18/15.
 */
public class OutgoingCallReceiver extends BroadcastReceiver {

    private Context mContext;
    private ScheduleManager mScheduleManager;
    private LogManager mLogManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        mScheduleManager = ScheduleManager.getInstance(mContext);
        mLogManager = LogManager.getInstance(mContext);
        //Determine the country code from current network (instead of system setting)
        //TODO better to use system wide configuration setting to get country code if the value is empty from telephone manager
        TelephonyManager tm = (TelephonyManager)context.getSystemService(mContext.TELEPHONY_SERVICE);
        String countryCodeValue = tm.getNetworkCountryIso();

        //if the phone has no network, (where we cannot determine countryCodeValue - will be null), we don't need to process any interception
        if(countryCodeValue != null && !countryCodeValue.isEmpty()){

            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            phoneNumber = ContactManager.standardizePhoneNumber(phoneNumber, countryCodeValue);

            ContactManager contactManager = ContactManager.getInstance(mContext);
            //standardize any phoneNumbers with non-standard phone number (while the phone was out of service)
            if(contactManager.nonStandardizedPreferenceEnabled()){
Log.d("bere.bere.bere", "Preference is enabled");
                contactManager.standardizeNonStandardContactPhones(countryCodeValue);
            }

            Contact blockedContact = contactManager.getContactByPhoneNumber(phoneNumber);

            if(blockedContact == null){

                return;
            }

            if(phoneNumber.equals(blockedContact.getPhoneNumber())){

                boolean callBlocked = false;
                //if outgoing call is blocked proceed with blocking
                if(blockedContact.getOutGoingBlockedState() == BlockState.ALWAYS_BLOCK){
                    setResultData(null);
                    //not recommended to abort broadcast
                    //abortBroadcast();
                    sendNotification(blockedContact.getDisplayNumber());
                    callBlocked = true;
                }
                else if(blockedContact.getOutGoingBlockedState() == BlockState.SCHEDULED_BLOCK){
                    //get all the scheduled calls for this number
                    Calendar cal = Calendar.getInstance();
                    //get weekDay of today
                    int weekDay = TimeHelper.convertJavaDayOfWeekWithCallBlockerType(cal.get(Calendar.DAY_OF_WEEK));
                    //get a benchmarkCalendar that sets the month and year part to a standard/benchmark date so that only time search can happen
                    cal = TimeHelper.setCalendarToBenchmarkTime(cal);

                    if(mScheduleManager.timeExistsInSchedule(blockedContact.getId(),BlockType.OUTGOING, weekDay, cal.getTime())){
                        setResultData(null);
                        //not recommended to abort broadcast
                        //abortBroadcast();
                        sendNotification(blockedContact.getDisplayNumber());
                        callBlocked = true;
                    }
                }

                if(callBlocked){
                    //TODO: remove db call from here and run it under different service
                    mLogManager.log(blockedContact, BlockType.INCOMING);
                }
            }
        }
        //else { /*  if country code value is null, it means there is no network (no sim is inserted/phone is in airplane mode)*/}
    }

    private void sendNotification(String phoneNumber){
        Intent i  = new Intent(mContext, CallBlockerActivity.class);
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, i, 0);
        Notification notification = new NotificationCompat.Builder(mContext)
                .setTicker("New outgoing call")
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle("New outgoing call intercepted")
                .setContentText(phoneNumber + " is intercepted")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
