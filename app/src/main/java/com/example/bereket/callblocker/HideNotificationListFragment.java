package com.example.bereket.callblocker;

import android.app.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ListFragment;

/**
 * Created by bereket on 2/10/16.
 *
 * This class is prepared to prevent notification from happening on callblock fragments.
 * When any of the fragments appeared, we don't want the notification to show up. This class
 * prevents that from happening by registering a broadcast receiver that reveives a SEND_NOTIFICATION_ACTION action type
 */
abstract public class HideNotificationListFragment extends ListFragment {

    private static final String SEND_NOTIFICATION_ACTION = "com.example.bereket.callblocker.SEND_NOTIFICATION";
    public static final String PRIVATE_PERMISSION = "com.example.bereket.callblocker.PRIVATE";

    private BroadcastReceiver mOnNotificationSentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            doOnBroadcastReceived();
            abortBroadcast();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        //unregister the broadcast listener  - we don't want a zombie listener!
        getActivity().unregisterReceiver(mOnNotificationSentReceiver);
    }

    @Override
    public void onResume(){
        super.onResume();

        //register a receiver that gets notified when block action happened and broadcast for notification is sent
        IntentFilter filter = new IntentFilter(SEND_NOTIFICATION_ACTION);
        //only receive broadcasts which are sent through the valid private permission - we don't want to receive a broadcast just matching an intent - we want the permission too
        getActivity().registerReceiver(mOnNotificationSentReceiver, filter, PRIVATE_PERMISSION, null);
    }

    abstract public void doOnBroadcastReceived();
}
