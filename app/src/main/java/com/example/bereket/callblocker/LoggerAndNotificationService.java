package com.example.bereket.callblocker;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

public class LoggerAndNotificationService extends IntentService {

    private ContactManager mContactManager;
    private LogManager mLogManager;
    private Context mContext;

    private static final String ACTION_LOG_CALL_BY_CONTACT = "com.example.bereket.callblocker.action.log.call.by.contact";
    private static final String ACTION_LOG_CALL_BY_PHONE_NUMBER = "com.example.bereket.callblocker.action.log.call.by.phone.number";

    private static final String BlOCKED_CONTACT = "com.example.bereket.callblocker.extra.blocked.contact";
    private static final String BLOCKED_TYPE = "com.example.bereket.callblocker.extra.block.type";
    private static final String PHONE_NUMBER = "com.example.bereket.callblocker.extra.phone.number";
    private static final String COUNTRY_CODE = "om.example.bereket.callblocker.extra.country.code";

    public static void startActionLoggerAndNotification(Context context, Contact blockedContact, Integer blockTye) {

        Intent intent = new Intent(context, LoggerAndNotificationService.class);
        intent.setAction(ACTION_LOG_CALL_BY_CONTACT);
        intent.putExtra(BlOCKED_CONTACT, blockedContact);
        intent.putExtra(BLOCKED_TYPE, blockTye);

        context.startService(intent);
    }

    public static void startActionLoggerAndNotification(Context context, String phoneNumber, Integer blockTye, String countryCode) {

        Intent intent = new Intent(context, LoggerAndNotificationService.class);
        intent.setAction(ACTION_LOG_CALL_BY_PHONE_NUMBER);
        intent.putExtra(PHONE_NUMBER, phoneNumber);
        intent.putExtra(BLOCKED_TYPE, blockTye);
        intent.putExtra(COUNTRY_CODE, countryCode);

        context.startService(intent);
    }

    public LoggerAndNotificationService() {
        super("LoggerAndNotificationService");

        mContext = getApplication();
        mContactManager = ContactManager.getInstance(mContext);
        mLogManager = LogManager.getInstance(mContext);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {

            final String action = intent.getAction();

            if (ACTION_LOG_CALL_BY_CONTACT.equals(action)) {

                final Contact blockedContact = (Contact)intent.getSerializableExtra(BlOCKED_CONTACT);
                final int blockType = intent.getIntExtra(BLOCKED_TYPE, -1);

                handleActionLogByContact(blockedContact, blockType);
            }
            else if(ACTION_LOG_CALL_BY_PHONE_NUMBER.equals(action)){

                final String blockedPhoneNumber = intent.getStringExtra(PHONE_NUMBER);
                final int blockType = intent.getIntExtra(BLOCKED_TYPE, -1);
                final String countryCodeValue = intent.getStringExtra(COUNTRY_CODE);

                handleActionLogByPhoneNumber(blockedPhoneNumber, blockType, countryCodeValue);
            }
        }
    }

    private void handleActionLogByContact(Contact blockedContact, int blockType) {

        if(blockType == BlockType.INCOMING){

            vibrate();
        }

        sendNotification(blockedContact.getDisplayNumber(), blockType);
        mLogManager.log(blockedContact, blockType);
    }

    private void handleActionLogByPhoneNumber(String blockedPhoneNumber, int blockType, String countryCodeValue) {

        if(blockType == BlockType.INCOMING){

            vibrate();
        }

        sendNotification(blockedPhoneNumber, blockType);
        mLogManager.log(blockedPhoneNumber, blockType, countryCodeValue);
    }

    private void sendNotification(String phoneNumber, int blockType){

        if(!mContactManager.disableIncomingBlockNotificationPreferenceEnabled()){

            BlockedCallCounter blockedCallCounter = new BlockedCallCounter(getApplicationContext());
            int blockCount = blockedCallCounter.incrementAndGetBlockCount(blockType);
//TODO put string values in xml file
            Intent i  = new Intent(getApplicationContext(), LogActivity.class);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
            Notification notification = new NotificationCompat.Builder(getApplicationContext())
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
                    getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(BlockType.INCOMING, notification);
        }
    }

    private void vibrate(){

        if(mContactManager.enableIncomingBlockVibrationPreferenceEnabled()){

            long pattern[] = { 0, 200, 200, 200};
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(pattern, -1);
        }
    }
}
