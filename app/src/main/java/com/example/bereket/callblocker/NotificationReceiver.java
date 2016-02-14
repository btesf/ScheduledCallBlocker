package com.example.bereket.callblocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by bereket on 2/10/16.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private Context mContext;
    private ContactManager mContactManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        mContactManager = ContactManager.getInstance(context);

        String phoneNumber = intent.getStringExtra(LogAndPostBlockService.PHONE_NUMBER);
        Integer blockType = intent.getIntExtra(LogAndPostBlockService.BLOCKED_TYPE, -1);

        if(phoneNumber != null && blockType != -1){

            sendNotification(phoneNumber, blockType);
        }
    }

    private void sendNotification(String phoneNumber, int blockType){

        boolean disabledState = (blockType == BlockType.INCOMING) ? mContactManager.disableIncomingBlockNotificationPreferenceEnabled() : mContactManager.disableOutgoingBlockNotificationPreferenceEnabled();

        if(!disabledState){

            BlockedCallCounter blockedCallCounter = new BlockedCallCounter(mContext);
            int blockCount = blockedCallCounter.incrementAndGetBlockCount(blockType);
            //TODO put string values in xml file
            Intent i  = new Intent(mContext, LogActivity.class);
            PendingIntent pi = PendingIntent.getActivity(mContext, 0, i, 0);
            Notification notification = new NotificationCompat.Builder(mContext)
                    .setTicker("Call blocker")
                    .setSmallIcon(android.R.drawable.ic_menu_report_image) //TODO: change the drawable here
                    .setContentTitle( (blockType == BlockType.INCOMING ? "New incoming call blocked" : "New outgoing call blocked"))
                    .setContentText(blockCount +
                            (blockType == BlockType.INCOMING ? " incoming " : " outgoing ") + (blockCount == 1 ? "call is " : "calls are ")
                            + "blocked since you last checked")
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager)
                    mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(BlockType.INCOMING, notification);
        }
    }
}
