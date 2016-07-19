package com.example.bereket.callblocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
            Intent i  = new Intent(mContext, MainAppActivity.class);

            i.putExtra(Constants.FRAGMENT_ID, Constants.LOG_LIST_FRAGMENT);

            PendingIntent pi = PendingIntent.getActivity(mContext, 0, i, 0);
            Notification notification = new NotificationCompat.Builder(mContext)
                    .setTicker(mContext.getResources().getString(R.string.scheduled_call_blocker_notification_ticker))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image) //TODO: change the drawable here
                    .setContentTitle((blockType == BlockType.INCOMING ? mContext.getString(R.string.new_incoming_call_blocked) : mContext.getString(R.string.new_outgoing_call_blocked)))
                    .setContentText(blockCount +
                            (blockType == BlockType.INCOMING ? mContext.getString(R.string.notification_segment_incoming) : mContext.getString(R.string.notification_segment_outgoing)) + (blockCount == 1 ? mContext.getString(R.string.notification_call_is) : mContext.getString(R.string.notification_calls_are))
                            + mContext.getString(R.string.blocked_since_you_last_checked))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                            .build();

            NotificationManager notificationManager = (NotificationManager)
                    mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(BlockType.INCOMING, notification);
        }
    }
}
