package com.example.bereket.callblocker;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class SaveFromPhoneBookService extends IntentService {

    private Context mContext;
    private ContactManager mContactManager;

    private static final String SEARCH_AND_SAVE_FROM_PHONEBOOK = "com.example.bereket.callblocker.action.search.and.save.from.phonebook";
    private static final String CONTACT = "com.example.bereket.callblocker.contact";
    private static final String COUNTRY_CODE_VALUE = "com.example.bereket.callblocker.country.code.value";
    private static final String DISPLAY_NUMBER = "com.example.bereket.callblocker.display.number";
    private static final String IS_NUMBER_STANDARDIZED = "com.example.bereket.callblocker.number.standardized";
    private static final String CONTACT_TYPE = "com.example.bereket.callblocker.contact.type";


    public static void startActionSaveFromPhoneBook(Context context, Contact param1, String countryCodeValue, String displayNumber, boolean isNumberStandardized, int contactType) {
        Intent intent = new Intent(context, SaveFromPhoneBookService.class);
        intent.setAction(SEARCH_AND_SAVE_FROM_PHONEBOOK);
        intent.putExtra(CONTACT, param1);
        intent.putExtra(COUNTRY_CODE_VALUE, countryCodeValue);
        intent.putExtra(DISPLAY_NUMBER, displayNumber);
        intent.putExtra(IS_NUMBER_STANDARDIZED, isNumberStandardized); //when copying contact details, isNumberStandardized is not copied. So we need it here
        intent.putExtra(CONTACT_TYPE, contactType);

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

                final Contact oldContact = (Contact)intent.getSerializableExtra(CONTACT);
                final String countryCode = intent.getStringExtra(COUNTRY_CODE_VALUE);
                final String displayNumber = intent.getStringExtra(DISPLAY_NUMBER);
                final boolean isNumberStandardized = intent.getBooleanExtra(IS_NUMBER_STANDARDIZED, false);
                final int contactType = intent.getIntExtra(CONTACT_TYPE, ContactType.EMPTY_CONTACT);
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
                newContact.setContactType(contactType);

                mContactManager.copyContactDetails(oldContact, newContact);

                mContactManager.updateOldContactWithNewContactWithId(oldContact, newContact);
                //send a broadcast so that the blocked list UI can refresh it's list once the old contact is replaced with the contact from phone book
                sendBroadcast(new Intent(Constants.ACTION_REFRESH_BLOCKED_LIST_UI), Constants.PRIVATE_PERMISSION); //here a private permission is added so that the broadcast can be interoggated if it holds the right permission when sent
                //send another delayed broadcast to give time for SingleContactFragment to register its receiver. Sometimes,the broadcast is sent before the
                //broadcast receiver is registered in the fragment
                sendDelayedBroadcast();
            }
        }
    }

    private void sendDelayedBroadcast(){

        new SendDelayedBroadcast().execute();
    }
    //this AsyncTask sends a broadcast half a second later
    private class SendDelayedBroadcast extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //do nothing - not critical
            }

            sendBroadcast(new Intent(Constants.ACTION_REFRESH_SINGLE_CONTACT_FRAGMENT_UI), Constants.PRIVATE_PERMISSION); //here a private permission is added so that the broadcast can be interoggated if it holds the right permission when sent
            return null;
        }
    }
}
