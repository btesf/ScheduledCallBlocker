package com.example.bereket.callblocker;

import android.content.Context;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created by bereket on 8/11/15.
 */
public class ContactManager {

    private DataBaseHelper mDataHelper;
    private Context mContext;

    public static String NON_STANDARDIZED_NUMBER_EXIST = "non.standard.number.exists";
    public static String COUNTRY_CODE_PREFERENCE = "country.code.preference.value";
    private static ContactManager mContactManager = null;
    private static String NO_NAME_CONTACT = "No name";
    private static String TEMPORARY_COUNTRY_CODE_VALUE = "US";

    private ContactManager(Context context){

        mContext = context;
        mDataHelper = DataBaseHelper.getInstance(context);
    }

    public static ContactManager getInstance(Context context){

        if(mContactManager == null){

            mContactManager = new ContactManager(context);
        }

        return mContactManager;
    }

    public DataBaseHelper.ContactCursor queryContacts(){

        return mDataHelper.queryContacts();
    }

    public long insertContact(Contact contact){

       return mDataHelper.insertContact(contact);
    }

    public Contact getEmptyContact(){

        return mDataHelper.getEmptyContact();
    }

    public void copyContactDetails(Contact oldContact, Contact newContact){

        newContact.setIncomingBlockedCount(oldContact.getIncomingBlockedCount());
        newContact.setIncomingBlockedState(oldContact.getIncomingBlockedState());
        newContact.setOutgoingBlockedCount(oldContact.getOutgoingBlockedCount());
        newContact.setOutGoingBlockedState(oldContact.getOutGoingBlockedState());
        //don't copy visibility state (in case of hidden contacts saved for log purpose) - because copying precedes update. We want the contact to be visible after a user added it explicitly
    }

    public void insertNewOrUpdateExistingContact(Long contactId, String displayNumber, String contactName, boolean isManual){
        /* check if number exists in contact. If exists then check if the two numbers are inserted differently (i.e one manually, the other from contact).
          That can be identified by comparing the two ids. Both ids will be the same if inserted from contact. If so just call the insertContact... method and
          it will update any newly modified contact details without changing contact id.

          On the other hand, if one is inserted manually, the old contact id will be replaced with the contact id and, then all the other contact details will
          be modified/updated
         */
        String countryCodeValue = null;
        //to make sure if a number is standardized (into E-164 format) before it is stored. Otherwise a system preference will be set so that a standardizing service would run
        boolean isNumberStandardized = false;

        countryCodeValue = getCountryCodeFromNetwork();

        if(countryCodeValue != null && !countryCodeValue.isEmpty()){ // if network is available (in service area)

            isNumberStandardized = true;
        }
        else{
            //set system preference and that will fire a standardizing service to run when any telephone event is triggered
            setNonStandardizedPreference(true);
        }

        //prepare a new contact
        Contact newContact = getEmptyContact();

        newContact.setId(contactId);
        newContact.setDisplayNumber(displayNumber);
        newContact.setContactName(contactName);
        newContact.setIsNumberStandardized(isNumberStandardized);
        newContact.setIsContactVisible(ContactVisibilityState.VISIBLE); //

        //if country code cannot be decided just insert the number
        if(isNumberStandardized){
            //if there are un-standardized number saved before, standardize them now (network available) before checking for duplicates
            standardizeNonStandardContactPhones(countryCodeValue);
            newContact.setPhoneNumber(standardizePhoneNumber(displayNumber, countryCodeValue));

            Contact oldContact = getContactByPhoneNumber(displayNumber);

            if(oldContact == null){// if number doesn't exist - or didn't match for the reason of one of the numbers is non-standardized

                long insertedContactId = mDataHelper.insertContact(newContact);

                //TODO: update this toast below
                Toast.makeText(mContext, "Contact added successfully", Toast.LENGTH_SHORT).show();

                if(isManual) {
                    //call a background service to replace the number from phone book if exists
                    SaveFromPhoneBookService.startActionSaveFromPhoneBook(mContext, insertedContactId, countryCodeValue, displayNumber, isNumberStandardized);
                }
            }
            else{

                copyContactDetails(oldContact, newContact); //from old to new
                //if both ids are similar the new contact must have come from phone's contact. - otherwise current timestamp would be returned and the ids will differ
                if(oldContact.getId() == newContact.getId()){

                    updateContact(newContact);
                    //TODO remove the line below - it is temporary
                    Toast.makeText(mContext, "Contact is updated", Toast.LENGTH_SHORT).show();
                }
                else{
                    //if the latest id is timestamp, check if there is an older contact (by comparing ids) and update the contact if the older contact id is greater than the new one
                    //i.e - if the new contact comes from contact list, it will be less than the new one - we want to update this time.
                    if(oldContact.getId() > newContact.getId()){
                        //change the id of the old contact so that all referencing tables' ids could also be updated (schedule, log tables)
                        updateOldContactWithNewContactWithId(oldContact, newContact);
                        //TODO remove the toast line and conditions below - it is temporary
                        //if the contact is saved on log (while global block setting is set), we don't want to show an update toast  - because the user doesn't know the contact was already saved
                        if(oldContact.isContactVisible()) {

                            Toast.makeText(mContext, "Old number is replaced by new one", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        //TODO: is this a good idea to simply show a toast and stop or is it better to show a dialog to ignore/replace the new number
                        Toast.makeText(mContext, "Number already exist in list.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        else{
            // try to find out if there are any duplicate numbers - try to prevent from inserting any duplicate numbers
            //mDataHelper.insertContact(newContact);
            List<Contact> temporarilyStandardizedNumbers;
            String countryCode;

            if(isCountryCodePreferenceSet()){

                countryCode = getCountryCodePreference();
                newContact.setIsNumberStandardized(true);
                //we used the preference value, thus no need to set global nonStandardPreference indicator setting/value to true
                setNonStandardizedPreference(false);
                newContact.setPhoneNumber(standardizePhoneNumber(displayNumber, countryCode));
            }
            else{

                countryCode = TEMPORARY_COUNTRY_CODE_VALUE;
                newContact.setPhoneNumber(displayNumber);
            }
            //get a list of temporarily standardized number (check if there are any un-standardized number in the list and standardize them  before comparing the phoneNumber against the list)
            temporarilyStandardizedNumbers = getTemporarilyStandardizedContactPhones(countryCode);
            String phoneNumber = standardizePhoneNumber(displayNumber, countryCode);
            Contact contact = numberExistsInTemporaryList(phoneNumber, temporarilyStandardizedNumbers);

            if(contact != null){
                //if the newContact Id is less than or equal to the existing one, this means the new contact comes from the phone contact
                if(newContact.getId() <= contact.getId()){
                    copyContactDetails(contact, newContact);
                    //change the id of the old contact so that all referencing tables' ids could also be updated (schedule, log tables)
                    updateOldContactWithNewContactWithId(contact, newContact);
                    //TODO remove the toast line and conditions below - it is temporary
                    //if the contact is saved on log (while global block setting is set), it is hidden contact, so we don't want to show an update toast - because the user doesn't know the contact was already saved
                    if(contact.isContactVisible()) {

                        Toast.makeText(mContext, "Old number is replaced by new one", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    //TODO: is this a good idea to simply show a toast and stop or is it better to show a dialog to ignore/replace the new number
                    Toast.makeText(mContext, "Number already exist in list.", Toast.LENGTH_SHORT).show();
                }
            }
            else{

                long insertedContactId = mDataHelper.insertContact(newContact);

                //TODO: update this toast below
                Toast.makeText(mContext, "Contact added successfully", Toast.LENGTH_SHORT).show(); //TODO is there a better message here?

                if(isManual) {
                    //call a background service to replace the number from phone book if exists
                    SaveFromPhoneBookService.startActionSaveFromPhoneBook(mContext, insertedContactId, countryCodeValue, displayNumber, isNumberStandardized);
                }
            }
        }
    }


    public Contact getContactFromPhoneBook(String phoneNumber, String countryCode){
        //if country code is not set (not determined from the network) try to get it from saved preference
        if(countryCode == null) {

            countryCode = isCountryCodePreferenceSet() ? getCountryCodePreference() : TEMPORARY_COUNTRY_CODE_VALUE;
        }

        String standardizedPhoneNumber = standardizePhoneNumber(phoneNumber, countryCode);
        ContactsProvider contactsProvider = ContactsProvider.getInstatnce(mContext);
        //the lines below are commented b/c they are replaced with newer more optimized method
        //Map<String, Contact> contactsFromPhoneBook = contactsProvider.getAllContactsFromPhone(countryCode);
        //return contactsFromPhoneBook.get(standardizedPhoneNumber);
        return contactsProvider.getContactFromPhoneBook(standardizedPhoneNumber, countryCode);
    }

    public boolean updateContact(Contact contact){

        return mDataHelper.updateContact(contact);
    }

    /**
     * change the id of the old contact so that all referencing tables' ids could also be updated (schedule, log tables)
     */
    public boolean updateOldContactWithNewContactWithId(Contact oldContact, Contact newContact) {

        mDataHelper.updateContactId(oldContact.getId(), newContact.getId());

        return updateContact(newContact);
    }

    public boolean deleteContact(Contact contact){

        return mDataHelper.deleteContact(contact);
    }

    public Contact getContactByStandardizedPhoneNumber(String phoneNumber){

        return mDataHelper.getContactByPhoneNumber(phoneNumber);
    }

    public Contact getContactByPhoneNumber(String phoneNumber){

        String countryCodeValue = getCountryCodeFromNetwork();
        phoneNumber = ContactManager.standardizePhoneNumber(phoneNumber, countryCodeValue);

        return getContactByStandardizedPhoneNumber(phoneNumber);
    }

    //will return null if country code is not deduced from network/ telephony manager. Try to get it
    // from preference setting if country code cannot be get from network
    public String getCountryCodeFromNetwork(){

        TelephonyManager tm = (TelephonyManager)mContext.getSystemService(mContext.TELEPHONY_SERVICE);
        String countryCodeValue = tm.getNetworkCountryIso();

        if(countryCodeValue == null || countryCodeValue.isEmpty()){
            //get it from preference
            //TODO get it from preference setting
        }
        else{
            //save country code value in preference for later use
            setCountryCodePreference(countryCodeValue);
        }

        return countryCodeValue;
    }

    public void unblockBlockedNumbersByType(Integer blockType){

            mDataHelper.unblockListByType(blockType);
    }

    public void unblockScheduledListByType(Integer blockType){

        mDataHelper.unblockScheduledListByType(blockType);
    }

    public Map<Integer,Schedule> queryContactSchedule(long contactId, int blockType){

        return mDataHelper.queryContactSchedule(contactId, blockType);
    }

    public void standardizeNonStandardContactPhones(String countryCode){

        DataBaseHelper.ContactCursor contactCursor = mDataHelper.getNonStandardizedPhoneContacts();

        if(contactCursor != null){
            while (contactCursor.moveToNext()){

                Contact nonStandardContact = contactCursor.getContact();
                nonStandardContact.setPhoneNumber(standardizePhoneNumber(nonStandardContact.getPhoneNumber(), countryCode));
                nonStandardContact.setIsNumberStandardized(true);
                //TODO how is updating unstandardized contact would work here - doesn't it time consuming?
                mDataHelper.updateContact(nonStandardContact);
            }

            contactCursor.close();
            setNonStandardizedPreference(false);
            //delete duplicate numbers
            mDataHelper.cleanUpDuplicateContacts();
        }
    }

    public static String standardizePhoneNumber(String nonStandardPhone, String countryCode){

        String formattedPhoneNumber =  PhoneNumberUtil.normalizeDigitsOnly(nonStandardPhone);

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        //if country code is not null, standardize the number into E164 format
        if(countryCode != null && !countryCode.isEmpty()){

            try {
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(formattedPhoneNumber, countryCode.toUpperCase());
                //Since you know the country you can format it as follows:
                formattedPhoneNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
            } catch (NumberParseException e) {
                //TODO: remove this exception message
                Log.d("bere.bere.bere", "Formatted phone number" + formattedPhoneNumber + ", nonStandard phone " + nonStandardPhone);
                System.err.println("NumberParseException was thrown: " + e.toString());
            }
        }

        return formattedPhoneNumber;
    }

    private List<Contact> getTemporarilyStandardizedContactPhones(String countryCode){

        List<Contact> contacts = new ArrayList<Contact>();

        DataBaseHelper.ContactCursor contactCursor = mDataHelper.queryContacts();

        if(contactCursor != null){

            while (contactCursor.moveToNext()){

                Contact contact = contactCursor.getContact();

                if(!contact.isIsNumberStandardized()){

                    contact.setPhoneNumber(standardizePhoneNumber(contact.getPhoneNumber(), countryCode));
                    contact.setIsNumberStandardized(true);
                }

                contacts.add(contact);
            }

            contactCursor.close();
        }

        return contacts;
    }

    private Contact numberExistsInTemporaryList(String phoneNumber, List<Contact> temporaryContactList){

        Contact contact = null;

        if(temporaryContactList.size() > 0){

           for(Contact c : temporaryContactList){

               if(c.getPhoneNumber().equals(phoneNumber)){

                   contact = c;
                   break;
               }
           }
        }

        return contact;
    }

    public boolean nonStandardizedPreferenceEnabled(){

        return  PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getBoolean(NON_STANDARDIZED_NUMBER_EXIST, false);
    }

    public void setNonStandardizedPreference(boolean exists){

        PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).edit().putBoolean(NON_STANDARDIZED_NUMBER_EXIST, exists).commit();
    }

    public boolean globalBlockIncomingBlockPreferenceEnabled(){

        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getBoolean(mContext.getResources().getString(R.string.block_all_incoming_numbers_pref_key), false);
    }

    public boolean globalBlockOutgoingBlockPreferenceEnabled(){

        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getBoolean(mContext.getResources().getString(R.string.block_all_outgoing_numbers_pref_key), false);
    }

    public boolean disableIncomingBlockNotificationPreferenceEnabled(){

        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getBoolean(mContext.getResources().getString(R.string.disable_incoming_block_notification_pref_key), false);
    }

    public boolean disableOutgoingBlockNotificationPreferenceEnabled(){

        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getBoolean(mContext.getResources().getString(R.string.disable_outgoing_block_notification_pref_key), false);
    }

    public static boolean numberHasProperFormat(String number){

        final String numberFormatRegEx = "[+]?[0-9]{6,15}";

        return number.matches(numberFormatRegEx);
    }

    public Long getArbitraryContactId(){

        return (new Date()).getTime();
    }

    private void setCountryCodePreference(String countryCodePreference){

        PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).edit().putString(COUNTRY_CODE_PREFERENCE, countryCodePreference).commit();
    }

    private String getCountryCodePreference(){

        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getString(COUNTRY_CODE_PREFERENCE, null);
    }

    private boolean isCountryCodePreferenceSet(){

        String countryCodeValue = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getString(COUNTRY_CODE_PREFERENCE, null);

        return  (countryCodeValue == null || countryCodeValue.isEmpty()) ? false : true;
    }
}
