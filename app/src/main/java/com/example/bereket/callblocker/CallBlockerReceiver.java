package com.example.bereket.callblocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by bereket on 7/18/15.
 */
public class CallBlockerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String phoneNumber = getResultData();

        if(phoneNumber == null){
            phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        }

        DataBaseHelper helper = new DataBaseHelper(context);

        DataBaseHelper.ContactCursor contactCursor = helper.queryContacts();

        contactCursor.moveToFirst();

        do{
            String str = contactCursor.getString(contactCursor.getColumnIndex(DataBaseHelper.PHONE_NUMBER));

            Log.d("bere.bere.bere", "Called No: " + phoneNumber + ", DB number: " + str);
            //Determine the country code from current network (instead of system setting)
            //TODO better to use system wide configuration setting to get country code if the value is empty from telephone manager
            TelephonyManager tm = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);

            String countryCodeValue = tm.getNetworkCountryIso();

            if(countryCodeValue != null){

                Log.d("bere.bere.bere", countryCodeValue);

                if(phoneNumber.equals(str)){//"0911510873")){
                    setResultData(null);
                    abortBroadcast();
                    Intent i  = new Intent(context, CallBlockerActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
                    Notification notification = new NotificationCompat.Builder(context)
                            .setTicker("New outgoing call")
                            .setSmallIcon(android.R.drawable.ic_menu_report_image)
                            .setContentTitle("New call intercepted")
                            .setContentText(phoneNumber + " is intercepted")
                            .setContentIntent(pi)
                            .setAutoCancel(true)
                            .build();

                    NotificationManager notificationManager = (NotificationManager)
                            context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, notification);

                    break;
                }
            }

        }
        while(contactCursor.moveToNext());
    }
}
