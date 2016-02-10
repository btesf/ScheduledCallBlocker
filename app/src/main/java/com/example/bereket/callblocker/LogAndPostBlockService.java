package com.example.bereket.callblocker;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Vibrator;

public class LogAndPostBlockService extends IntentService {

    private ContactManager mContactManager;
    private LogManager mLogManager;
    private Context mContext;

    private static final String ACTION_LOG_CALL_BY_CONTACT = "com.example.bereket.callblocker.action.log.call.by.contact";
    private static final String ACTION_LOG_CALL_BY_PHONE_NUMBER = "com.example.bereket.callblocker.action.log.call.by.phone.number";

    private static final String BlOCKED_CONTACT = "com.example.bereket.callblocker.extra.blocked.contact";
    public static final String BLOCKED_TYPE = "com.example.bereket.callblocker.extra.block.type";
    public static final String PHONE_NUMBER = "com.example.bereket.callblocker.extra.phone.number";
    private static final String COUNTRY_CODE = "om.example.bereket.callblocker.extra.country.code";

    private static final String PRIVATE_PERMISSION_KEY = "com.example.bereket.callblocker.PRIVATE";
    private static final String SEND_NOTIFICATION_ACTION = "com.example.bereket.callblocker.SEND_NOTIFICATION";

    public static void startActionLoggerAndNotification(Context context, Contact blockedContact, Integer blockTye) {

        Intent intent = new Intent(context, LogAndPostBlockService.class);
        intent.setAction(ACTION_LOG_CALL_BY_CONTACT);
        intent.putExtra(BlOCKED_CONTACT, blockedContact);
        intent.putExtra(BLOCKED_TYPE, blockTye);

        context.startService(intent);
    }

    public static void startActionLoggerAndNotification(Context context, String phoneNumber, Integer blockTye, String countryCode) {

        Intent intent = new Intent(context, LogAndPostBlockService.class);
        intent.setAction(ACTION_LOG_CALL_BY_PHONE_NUMBER);
        intent.putExtra(PHONE_NUMBER, phoneNumber);
        intent.putExtra(BLOCKED_TYPE, blockTye);
        intent.putExtra(COUNTRY_CODE, countryCode);

        context.startService(intent);
    }

    public LogAndPostBlockService() {
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

        sendNotification(blockedContact.getDisplayNumber(), blockType);

        if(blockType == BlockType.INCOMING){

            vibrate();
        }

        mLogManager.log(blockedContact, blockType);
    }

    private void handleActionLogByPhoneNumber(String blockedPhoneNumber, int blockType, String countryCodeValue) {

        sendNotification(blockedPhoneNumber, blockType);

        if(blockType == BlockType.INCOMING){

            vibrate();
        }

        mLogManager.log(blockedPhoneNumber, blockType, countryCodeValue);
    }

    private void sendNotification(String phoneNumber, int blockType){

        Intent intent = new Intent(SEND_NOTIFICATION_ACTION);

        intent.putExtra(PHONE_NUMBER, phoneNumber);
        intent.putExtra(BLOCKED_TYPE, blockType);

        sendOrderedBroadcast(intent, PRIVATE_PERMISSION_KEY);
    }

    private void vibrate(){

        if(mContactManager.enableIncomingBlockVibrationPreferenceEnabled()){

            long pattern[] = { 0, 200, 200, 200};
            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(pattern, -1);
        }
    }
}
