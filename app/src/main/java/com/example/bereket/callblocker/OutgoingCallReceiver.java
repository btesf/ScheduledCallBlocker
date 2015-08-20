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

/**
 * Created by bereket on 7/18/15.
 */
public class OutgoingCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //Determine the country code from current network (instead of system setting)
        //TODO better to use system wide configuration setting to get country code if the value is empty from telephone manager
        TelephonyManager tm = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
        String countryCodeValue = tm.getNetworkCountryIso();

        //if the phone has no network, (where we cannot determine countryCodeValue - will be null), we don't need to process any interception
        if(countryCodeValue != null){

            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            phoneNumber = ContactManager.standardizePhoneNumber(phoneNumber, countryCodeValue);

            ContactManager contactManager = ContactManager.getInstance(context);
            Contact blockedContact = contactManager.getContactByPhoneNumber(phoneNumber);

            if(blockedContact == null){

                return;
            }

            if(phoneNumber.equals(blockedContact.getPhoneNumber())){
                //if outgoing call is blocked proceed with blocking
                if(blockedContact.isIsOutGoingBlocked()){

                    setResultData(null);
                    //not recommended to abort broadcast
                    //abortBroadcast();
                    Intent i  = new Intent(context, CallBlockerActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
                    Notification notification = new NotificationCompat.Builder(context)
                            .setTicker("New outgoing call")
                            .setSmallIcon(android.R.drawable.ic_menu_report_image)
                            .setContentTitle("New outgoing call intercepted")
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
        //else { /*  if country code value is null, it means there is no network (no sim is inserted/phone is in airplane mode)*/}
    }
}
