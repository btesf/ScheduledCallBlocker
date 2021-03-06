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

    public DataBaseHelper.ContactCursor queryContacts(int contactType){

        return mDataHelper.queryContacts(contactType);
    }

    public DataBaseHelper.ContactCursor queryContacts(String queryString, int contactType){

        return mDataHelper.queryContacts(queryString, contactType);
    }

    public long insertContact(Contact contact){

       return mDataHelper.insertContact(contact);
    }

    public Contact getEmptyContact(){

        return mDataHelper.getEmptyContact();
    }

    //from old to new
    public void copyContactDetails(Contact oldContact, Contact newContact){

        newContact.setIncomingBlockedCount(oldContact.getIncomingBlockedCount());
        newContact.setOutgoingBlockedCount(oldContact.getOutgoingBlockedCount());
        //if contact type is different, then the blocked states should be the new one's default state
        newContact.setIncomingBlockedState((oldContact.getContactType() != newContact.getContactType()) ? newContact.getIncomingBlockedState() : oldContact.getIncomingBlockedState());
        newContact.setOutGoingBlockedState((oldContact.getContactType() != newContact.getContactType()) ? newContact.getOutGoingBlockedState() : oldContact.getOutGoingBlockedState());
        //don't copy visibility state (in case of hidden contacts saved for log purpose) - because copying precedes update. We want the contact to be visible after a user added it explicitly
    }

    public void setDefaultBlockStateByContactType(Contact contact, int contactType){
        /*
        WHITELIST -> set both incoming and outgoing call settings to whitelist(allow) by default
        BLOCKLIST -> set both incoming and outgoing call settings to block by default
         */
        switch(contactType){

            case ContactType.WHITE_LIST_CONTACT:

                contact.setIncomingBlockedState(BlockState.WHITE_LIST);
                contact.setOutGoingBlockedState(BlockState.WHITE_LIST);

                break;
            case ContactType.BLOCKED_CONTACT:

                contact.setIncomingBlockedState(BlockState.ALWAYS_BLOCK);
                contact.setOutGoingBlockedState(BlockState.DONT_BLOCK);

                break;
        }

    }

    public void insertNewOrUpdateExistingContact(Long contactId, String displayNumber, String contactName, boolean isManual, int contactType){
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
        newContact.setContactType(contactType);
        setDefaultBlockStateByContactType(newContact, contactType);

        //if country code cannot be decided just insert the number
        if(isNumberStandardized){
            //if there are un-standardized number saved before, standardize them now (network available) before checking for duplicates
            standardizeNonStandardContactPhones(countryCodeValue);
            newContact.setPhoneNumber(standardizePhoneNumber(displayNumber, countryCodeValue));

            Contact oldContact = getContactByPhoneNumber(displayNumber);

            if(oldContact == null){// if number doesn't exist - or didn't match for the reason of one of the numbers is non-standardized

                newContact.setId(mDataHelper.insertContact(newContact));

                Toast.makeText(mContext, mContext.getString(R.string.contact_added_successfully), Toast.LENGTH_SHORT).show();

                if(isManual) {
                    //call a background service to replace the number from phone book if exists
                    SaveFromPhoneBookService.startActionSaveFromPhoneBook(mContext, newContact, countryCodeValue, displayNumber, isNumberStandardized, contactType);
                }
            }
            else{

                copyContactDetails(oldContact, newContact); //from old to new
                //if both ids are similar the new contact must have come from phone's contact. - otherwise current timestamp would be returned and the ids will differ
                if((oldContact.getId() == newContact.getId())){

                   if(oldContact.getContactType() != ContactType.HIDDEN_CONTACT){

                       if(oldContact.getContactType() != contactType) {
                           //set default block setting for old user - we don't to lose other oldContact properties
                           setDefaultBlockStateByContactType(oldContact, contactType);
                           oldContact.setContactType(newContact.getContactType());
                           updateContact(oldContact);
                           Toast.makeText(mContext, mContext.getString(R.string.number_is_moved_to) + (contactType == ContactType.BLOCKED_CONTACT ? mContext.getString(R.string.blocked_list) : mContext.getString(R.string.white_list)), Toast.LENGTH_SHORT).show();
                       }
                       else{

                           Toast.makeText(mContext, mContext.getString(R.string.contact_is_updated), Toast.LENGTH_SHORT).show();
                           updateContact(newContact);
                       }
                   } else{

                       Toast.makeText(mContext, mContext.getString(R.string.contact_added_successfully), Toast.LENGTH_SHORT).show();
                       updateContact(newContact);
                    }
                }
                else if((oldContact.getId() < newContact.getId()) && (oldContact.getContactType() != newContact.getContactType())){

                    int oldContactType = oldContact.getContactType();
                    //during adding hidden contact or category change (e.g. blocklist -> whitelist or viceversa)
                    oldContact.setContactType(newContact.getContactType());
                    setDefaultBlockStateByContactType(oldContact, newContact.getContactType());//this method is called on the oldContact
                    updateContact(oldContact);

                    if(oldContactType == ContactType.HIDDEN_CONTACT){
                        Toast.makeText(mContext, mContext.getString(R.string.contact_added_successfully), Toast.LENGTH_SHORT).show();
                    } else{//category change
                        Toast.makeText(mContext, mContext.getString(R.string.number_is_moved_to) + (contactType == ContactType.BLOCKED_CONTACT ? mContext.getString(R.string.blocked_list) : mContext.getString(R.string.white_list)), Toast.LENGTH_SHORT).show();
                    }
                }
                else if(oldContact.getId() > newContact.getId()){
                //if the latest id is timestamp, check if there is an older contact (by comparing ids) and update the contact if the older contact id is greater than the new one
                //i.e - if the new contact comes from contact list, it will be less than the new one - we want to update this time.
                    int oldContactType = oldContact.getContactType();
                    //change the id of the old contact so that all referencing tables' ids could also be updated (schedule, log tables)
                    updateOldContactWithNewContactWithId(oldContact, newContact);
                    //if the contact is saved on log (while global block setting is set), we don't want to show an update toast  - because the user doesn't know the contact was already saved
                    if(oldContactType != ContactType.HIDDEN_CONTACT) {
                        Toast.makeText(mContext, mContext.getString(R.string.old_number_is_replaced_by_new_one), Toast.LENGTH_SHORT).show();
                    }

                } else { //newcontactId is greater than old contact id

                    Toast.makeText(mContext, mContext.getString(R.string.number_already_exists_in_list), Toast.LENGTH_SHORT).show();
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
                if((newContact.getId() <= contact.getId())){

                    copyContactDetails(contact, newContact);
                    //if the contact is saved on log (while global block setting is set), it is hidden contact, so we don't want to show an update toast - because the user doesn't know the contact was already saved
                    if(contact.getContactType() != ContactType.HIDDEN_CONTACT){

                        if(contact.getContactType() != contactType) {

                            updateOldContactWithNewContactWithId(contact, newContact);//no problem here, because newContact will have all the necessary details
                            Toast.makeText(mContext, mContext.getString(R.string.number_is_moved_to) + (contactType == ContactType.BLOCKED_CONTACT ? mContext.getString(R.string.blocked_list) : mContext.getString(R.string.white_list)), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            //change the id of the old contact so that all referencing tables' ids could also be updated (schedule, log tables)
                            updateOldContactWithNewContactWithId(contact, newContact);
                            Toast.makeText(mContext, mContext.getString(R.string.contact_is_updated), Toast.LENGTH_SHORT).show();
                        }
                    } else{
                        //change the id of the old contact so that all referencing tables' ids could also be updated (schedule, log tables)
                        updateOldContactWithNewContactWithId(contact, newContact);
                        Toast.makeText(mContext, mContext.getString(R.string.contact_added_successfully), Toast.LENGTH_SHORT).show();
                    }

                } else if((contact.getId() < newContact.getId()) && (contact.getContactType() != newContact.getContactType())){//&& contact.getContactType() == ContactType.HIDDEN_CONTACT){ //if contact was there but hidden, make it visible contact - this happens when a contact is added from Phonebook by a logger service

                    contact.setContactType(newContact.getContactType());
                    setDefaultBlockStateByContactType(contact, newContact.getContactType());
                    updateContact(contact);
                    Toast.makeText(mContext, mContext.getString(R.string.contact_added_successfully), Toast.LENGTH_SHORT).show();
                }
                else if((contact.getId() < newContact.getId()) && (contact.getContactType() != newContact.getContactType())){ //if manually add from different category is performed (where the contact may exist in another category), a category change must be done.
                    //set default block setting for old user - we don't to lose other oldContact properties
                    setDefaultBlockStateByContactType(contact, newContact.getContactType());

                    int oldContactType = contact.getContactType();

                    contact.setContactType(newContact.getContactType());
                    //sometimes, if the original contact itself was not standardized, and fetched from numberExistsInTemporaryList it will be standardized
                    //with temporary country code - as a result, a wrong number will be saved. To prevernt that check if oldContact is standardized and if not reset it with display number
                    if(!contact.isIsNumberStandardized()) contact.setPhoneNumber(displayNumber);

                    updateContact(contact);

                    if(oldContactType == ContactType.HIDDEN_CONTACT){

                        Toast.makeText(mContext, mContext.getString(R.string.contact_added_successfully), Toast.LENGTH_SHORT).show();
                    }
                    else{

                        Toast.makeText(mContext, mContext.getString(R.string.number_is_moved_to) + (contactType == ContactType.BLOCKED_CONTACT ? mContext.getString(R.string.blocked_list) : mContext.getString(R.string.white_list)), Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                   Toast.makeText(mContext, mContext.getString(R.string.number_already_exists_in_list), Toast.LENGTH_SHORT).show();
                }
            }
            else{

                newContact.setId(mDataHelper.insertContact(newContact));
                Toast.makeText(mContext, mContext.getString(R.string.contact_added_successfully), Toast.LENGTH_SHORT).show();

                if (isManual) {
                    //call a background service to replace the number from phone book if exists
                    SaveFromPhoneBookService.startActionSaveFromPhoneBook(mContext, newContact, countryCodeValue, displayNumber, isNumberStandardized, contactType);
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

        /*if a contact is deleted all related records in other tables will be deleted too
        but if a contact has a log, we want to keep the contact so that to preserve the logs
        in this case, deleting all the schedule entries (if any) is enough - only changing the contact
        type to HIDDEN
        */

        if(mDataHelper.contactHasLog(contact.getId())){

            mDataHelper.deleteAllSchedulesForContact(contact.getId());
            contact.setContactType(ContactType.HIDDEN_CONTACT);

            return updateContact(contact);
        }
        else {

            return mDataHelper.deleteContact(contact);
        }
    }

    public Contact getContactById(long id){

        return mDataHelper.getContactById(id);
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
            //do nothing for the time being. Null countryCodeValue is evaluated by the calling/client methods
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

                if(!contact.isIsNumberStandardized()) {

                    contact.setPhoneNumber(standardizePhoneNumber(contact.getPhoneNumber(), countryCode));
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

    public boolean whitelistIncomingAllowPreferenceEnabled(){

        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getBoolean(mContext.getResources().getString(R.string.incoming_whitelist_pref_key), false);
    }

    public boolean whitelistOutgoingAllowPreferenceEnabled(){

        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getBoolean(mContext.getResources().getString(R.string.outgoing_whitelist_pref_key), false);
    }

    public boolean disableIncomingBlockNotificationPreferenceEnabled(){

        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getBoolean(mContext.getResources().getString(R.string.disable_incoming_block_notification_pref_key), false);
    }

    public boolean disableOutgoingBlockNotificationPreferenceEnabled(){

        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getBoolean(mContext.getResources().getString(R.string.disable_outgoing_block_notification_pref_key), false);
    }

    public boolean enableIncomingBlockVibrationPreferenceEnabled(){

        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getBoolean(mContext.getResources().getString(R.string.enable_vibration_pref_key), false);
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
