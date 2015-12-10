package com.example.bereket.callblocker;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

public class SaveFromPhoneBookService extends IntentService {

    private Context mContext;
    private ContactManager mContactManager;

    private static final String SEARCH_AND_SAVE_FROM_PHONEBOOK = "com.example.bereket.callblocker.action.search.and.save.from.phonebook";
    private static final String NEW_CONTACT_ID = "com.example.bereket.callblocker.new.inserted.id";
    private static final String COUNTRY_CODE_VALUE = "com.example.bereket.callblocker.country.code.value";
    private static final String DISPLAY_NUMBER = "com.example.bereket.callblocker.display.number";
    private static final String IS_NUMBER_STANDARDIZED = "com.example.bereket.callblocker.number.standardized";

    public static final String ACTION_REFRESH_BLOCKED_LIST_UI = "com.example.bereket.callblocker.refresh.blocked.list.ui";
    public static final String PRIVATE_PERMISSION = "com.example.bereket.callblocker.PRIVATE";

    public static void startActionSaveFromPhoneBook(Context context, long param1, String countryCodeValue, String displayNumber, boolean isNumberStandardized) {
        Intent intent = new Intent(context, SaveFromPhoneBookService.class);
        intent.setAction(SEARCH_AND_SAVE_FROM_PHONEBOOK);
        intent.putExtra(NEW_CONTACT_ID, param1);
        intent.putExtra(COUNTRY_CODE_VALUE, countryCodeValue);
        intent.putExtra(DISPLAY_NUMBER, displayNumber);
        intent.putExtra(IS_NUMBER_STANDARDIZED, isNumberStandardized); //when copying contact details, isNumberStandardized is not copied. So we need it here

        context.startService(intent);
    }

    public SaveFromPhoneBookService() {
        super("SaveFromPhoneBookService");

        mContext = getApplication();
        mContactManager = ContactManager.getInstance(mContext);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {

            final String action = intent.getAction();

            if (SEARCH_AND_SAVE_FROM_PHONEBOOK.equals(action)) {

                final long oldContactId = intent.getLongExtra(NEW_CONTACT_ID, -1);
                final String countryCode = intent.getStringExtra(COUNTRY_CODE_VALUE);
                final String displayNumber = intent.getStringExtra(DISPLAY_NUMBER);
                final boolean isNumberStandardized = intent.getBooleanExtra(IS_NUMBER_STANDARDIZED, false);
                /** implement your definition here **/

                Contact newContact = mContactManager.getEmptyContact();

                Contact phoneBookContact = mContactManager.getContactFromPhoneBook(displayNumber, countryCode);

                if(phoneBookContact == null) return;

                newContact.setId(phoneBookContact.getId());
                newContact.setDisplayNumber(phoneBookContact.getDisplayNumber());
                newContact.setPhoneNumber(phoneBookContact.getPhoneNumber());
                newContact.setContactName(phoneBookContact.getContactName());
                //add isNumberStandardized flag
                newContact.setIsNumberStandardized(isNumberStandardized);

                Contact oldContact = new Contact();
                oldContact.setId(oldContactId);

                mContactManager.updateOldContactWithNewContactWithId(oldContact, newContact);
                //send a broadcast so that the blocked list UI can refresh it's list once the old contact is replaced with the contact from phone book
                sendBroadcast(new Intent(ACTION_REFRESH_BLOCKED_LIST_UI), PRIVATE_PERMISSION); //here a private permission is added so that the broadcast can be interoggated if it holds the right permission when sent
            }
        }
    }
}
