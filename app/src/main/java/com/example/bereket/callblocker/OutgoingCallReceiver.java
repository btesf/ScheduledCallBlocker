package com.example.bereket.callblocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

/**
 * Created by bereket on 7/18/15.
 */
public class OutgoingCallReceiver extends BroadcastReceiver {

    private Context mContext;
    private ScheduleManager mScheduleManager;
    private LogManager mLogManager;
    private ContactManager mContactManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        mScheduleManager = ScheduleManager.getInstance(mContext);
        mLogManager = LogManager.getInstance(mContext);
        mContactManager = ContactManager.getInstance(mContext);

        //Determine the country code from current network (instead of system setting)
        String countryCodeValue = mContactManager.getCountryCodeFromNetwork();
        //if the phone has no network, (where we cannot determine countryCodeValue - will be null), we don't need to process any interception
        if(countryCodeValue != null && !countryCodeValue.isEmpty()){

            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            phoneNumber = ContactManager.standardizePhoneNumber(phoneNumber, countryCodeValue);

            if(mContactManager.globalBlockOutgoingBlockPreferenceEnabled()){

                setResultData(null);
                LoggerAndNotificationService.startActionLoggerAndNotification(context, phoneNumber, BlockType.OUTGOING, countryCodeValue);
            }
            else{

                //standardize any phoneNumbers with non-standard phone number (while the phone was out of service)
                if(mContactManager.nonStandardizedPreferenceEnabled()){

                    mContactManager.standardizeNonStandardContactPhones(countryCodeValue);
                }

                Contact blockedContact = mContactManager.getContactByStandardizedPhoneNumber(phoneNumber);

                if(blockedContact == null){

                    return;
                }

                boolean callBlocked = false;
                //if outgoing call is blocked proceed with blocking
                if(blockedContact.getOutGoingBlockedState() == BlockState.ALWAYS_BLOCK){
                    setResultData(null);
                    //not recommended to abort broadcast
                    //abortBroadcast();

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
                        callBlocked = true;
                    }
                }

                if(callBlocked){

                    LoggerAndNotificationService.startActionLoggerAndNotification(context, blockedContact, BlockType.OUTGOING);
                }

            }
        }
        //else { /*  if country code value is null, it means there is no network (no sim is inserted/phone is in airplane mode)*/}
    }
}
