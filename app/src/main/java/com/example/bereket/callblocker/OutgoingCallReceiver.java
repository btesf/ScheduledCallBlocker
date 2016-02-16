package com.example.bereket.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
    public void onReceive(final Context context, final Intent intent) {

        mContext = context;
        mScheduleManager = ScheduleManager.getInstance(mContext);
        mLogManager = LogManager.getInstance(mContext);
        mContactManager = ContactManager.getInstance(mContext);

        final PendingResult result = goAsync();

        Thread thread = new Thread() {

            public void run() {
                //Determine the country code from current network (instead of system setting)
                String countryCodeValue = mContactManager.getCountryCodeFromNetwork();
                //if the phone has no network, (where we cannot determine countryCodeValue - will be null), we don't need to process any interception
                if(countryCodeValue != null && !countryCodeValue.isEmpty()){

                    String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    phoneNumber = ContactManager.standardizePhoneNumber(phoneNumber, countryCodeValue);

                    if(mContactManager.globalBlockOutgoingBlockPreferenceEnabled()){

                        if(mContactManager.whitelistOutgoingAllowPreferenceEnabled()){

                            //standardize any phoneNumbers with non-standard phone number (registered while the phone was out of service)
                            if(mContactManager.nonStandardizedPreferenceEnabled()){

                                mContactManager.standardizeNonStandardContactPhones(countryCodeValue);
                            }

                            Contact whiteListContact = mContactManager.getContactByStandardizedPhoneNumber(phoneNumber);

                            if(whiteListContact != null && whiteListContact.getIncomingBlockedState() == BlockState.WHITE_LIST){

                                result.finish();

                                return;
                            }
                            else{

                                result.setResultData(null);
                                LogAndPostBlockService.startActionLoggerAndNotification(context, phoneNumber, BlockType.OUTGOING, countryCodeValue);
                            }
                        }
                        else{

                            result.setResultData(null);
                            LogAndPostBlockService.startActionLoggerAndNotification(context, phoneNumber, BlockType.OUTGOING, countryCodeValue);
                        }
                    }
                    else if(mContactManager.whitelistOutgoingAllowPreferenceEnabled()){

                        //standardize any phoneNumbers with non-standard phone number (registered while the phone was out of service)
                        if(mContactManager.nonStandardizedPreferenceEnabled()){

                            mContactManager.standardizeNonStandardContactPhones(countryCodeValue);
                        }

                        Contact whiteListContact = mContactManager.getContactByStandardizedPhoneNumber(phoneNumber);

                        if(whiteListContact == null || whiteListContact.getOutGoingBlockedState() != BlockState.WHITE_LIST){

                            result.setResultData(null);
                            LogAndPostBlockService.startActionLoggerAndNotification(context, phoneNumber, BlockType.OUTGOING, countryCodeValue);
                        }
                    } else{
                        //standardize any phoneNumbers with non-standard phone number (while the phone was out of service)
                        if(mContactManager.nonStandardizedPreferenceEnabled()){

                            mContactManager.standardizeNonStandardContactPhones(countryCodeValue);
                        }

                        Contact blockedContact = mContactManager.getContactByStandardizedPhoneNumber(phoneNumber);

                        if(blockedContact == null){

                            result.finish();

                            return;
                        }

                        boolean callBlocked = false;
                        //if outgoing call is blocked proceed with blocking
                        if(blockedContact.getOutGoingBlockedState() == BlockState.ALWAYS_BLOCK){
                            result.setResultData(null);
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
                                result.setResultData(null);
                                //not recommended to abort broadcast
                                //abortBroadcast();
                                callBlocked = true;
                            }
                        }

                        if(callBlocked){

                            LogAndPostBlockService.startActionLoggerAndNotification(context, blockedContact, BlockType.OUTGOING);
                        }
                    }
                }
                //else { /*  if country code value is null, it means there is no network (no sim is inserted/phone is in airplane mode)*/}
                result.finish();
            }
        };

        thread.start();
    }
}
