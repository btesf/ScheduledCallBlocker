package com.example.bereket.callblocker;

import android.content.Context;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
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

    public void insertContact(Contact contact){

        mDataHelper.insertContact(contact);
    }

    public Contact getEmptyContact(){

        Contact contact = new Contact();

        contact.setOutgoingBlockedCount(0);
        contact.setOutGoingBlockedState(BlockState.DONT_BLOCK);
        contact.setIncomingBlockedCount(0);
        contact.setIncomingBlockedState(BlockState.DONT_BLOCK);
        contact.setIsNumberStandardized(false);

        return contact;
    }

    public void copyContactDetails(Contact oldContact, Contact newContact){

        newContact.setIncomingBlockedCount(oldContact.getIncomingBlockedCount());
        newContact.setIncomingBlockedState(oldContact.getIncomingBlockedState());
        newContact.setOutgoingBlockedCount(oldContact.getOutgoingBlockedCount());
        newContact.setOutGoingBlockedState(oldContact.getOutGoingBlockedState());
    }

    public void insertNewOrUpdateExistingContact(Long contactId, String displayNumber, String contactName){
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
        //standardize phone number
        String phoneNumber = standardizePhoneNumber(displayNumber, countryCodeValue);

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
        newContact.setPhoneNumber(phoneNumber);
        newContact.setContactName(contactName);
        newContact.setIsNumberStandardized(isNumberStandardized);

        //if country code cannot be decided just insert the number
        if(isNumberStandardized == false){
            // try to find out if there are any duplicate numbers - try to prevent from inserting any duplicate numbers
            //mDataHelper.insertContact(newContact);
            List<Contact> temporarilyStandardizedNumbers;
            //if country code preference is set try to standardize the number using it
            if(isCountryCodePreferenceSet()){

                String countryCodePreferenceValue = getCountryCodePreference();

                newContact.setPhoneNumber(standardizePhoneNumber(displayNumber, countryCodePreferenceValue));
                newContact.setIsNumberStandardized(true);
                //get a list of temporarily standardized number (check if there are any un-standardized number in the list and standardize them  before comparing the phoneNumber against the list)
                temporarilyStandardizedNumbers = getTemporarilyStandardizedContactPhones(countryCodePreferenceValue);

                Contact contact = numberExistsInTemporaryList(newContact.getPhoneNumber(), temporarilyStandardizedNumbers);

                if(contact != null){
                    //if the newContact Id is less than or equal to the existing one, this means the new contact comes from the phone contact
                    if(newContact.getId() <= contact.getId()){
                        copyContactDetails(contact, newContact);
                        //change the id of the old contact so that all referencing tables' ids could also be updated (schedule, log tables)
                        mDataHelper.updateContactId(contact.getId(), newContact.getId());
                        updateContact(newContact);
                        //TODO remove the line below - it is temporary
                        Toast.makeText(mContext, "New number is replaced by new one", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //TODO: is this a good idea to simply show a toast and stop or is it better to show a dialog to ignore/replace the new number
                        Toast.makeText(mContext, "Number already exist in list.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    //TODO: better to set number standardized preference to true and update existing contact (if current contact id is less than the existing
                    mDataHelper.insertContact(newContact);
                }
                //we used the preference value, thus no need to set global nonStandardPreference indicator setting/value to true
                setNonStandardizedPreference(false);
            }
            else{

                String tempCountryCodeValue = TEMPORARY_COUNTRY_CODE_VALUE;

                phoneNumber = standardizePhoneNumber(displayNumber, tempCountryCodeValue);
                //get a list of temporarily standardized numbers (if there will be any un-standardized numbers, standardize them using the temporary country code value first)
                temporarilyStandardizedNumbers = getTemporarilyStandardizedContactPhones(tempCountryCodeValue);

                Contact contact = numberExistsInTemporaryList(phoneNumber, temporarilyStandardizedNumbers);

                if(contact != null){
                    //TODO: is this a good idea to simply show a toast and stop or is it better to show a dialog to ignore/replace the new number
                    Toast.makeText(mContext, "Number already exist in list.", Toast.LENGTH_SHORT).show();
                }
                else{

                    mDataHelper.insertContact(newContact);
                }
            }
        }
        else{

            Contact oldContact = getContactByPhoneNumber(displayNumber);

            if(oldContact == null){// if number doesn't exist - or didn't match for the reason of one of the numbers is non-standardized

                mDataHelper.insertContact(newContact);
            }
            else{

                copyContactDetails(oldContact, newContact); //from old to new
                //if both ids are similar the new contact must have come from phone's contact. - otherwise current timestamp would be returned and the ids will differ
                if(oldContact.getId() == newContact.getId()){

                    updateContact(newContact);
                }
                else{
                    //if the latest id is timestamp, check if there is an older contact (by comparing ids) and update the contact if the older contact id is greater than the new one
                    //i.e - if the new contact comes from contact list, it will be less than the new one - we want to update this time.
                    if(oldContact.getId() > newContact.getId()){
                        //change the id of the old contact so that all referencing tables' ids could also be updated (schedule, log tables)
                        mDataHelper.updateContactId(oldContact.getId(), newContact.getId());
                        updateContact(newContact);
                    }
                }
            }
        }
    }


    public boolean updateContact(Contact contact){

        return mDataHelper.updateContact(contact);
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
        if(countryCode != null){

            try {
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(formattedPhoneNumber, countryCode.toUpperCase());
                //Since you know the country you can format it as follows:
                formattedPhoneNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164 );
            } catch (NumberParseException e) {
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

    public static boolean numberHasProperFormat(String number){

        final String numberFormatRegEx = "[+]?[0-9]{6,15}";

        return number.matches(numberFormatRegEx);
    }

    private void setCountryCodePreference(String countryCodePreference){

        PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).edit().putString(COUNTRY_CODE_PREFERENCE, countryCodePreference).commit();
    }

    private String getCountryCodePreference(){

        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getString(COUNTRY_CODE_PREFERENCE, null);
    }

    private boolean isCountryCodePreferenceSet(){

        String countryCodeValue = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getString(COUNTRY_CODE_PREFERENCE, null);

        return  (countryCodeValue == null || countryCodeValue.isEmpty()) ? true : false;
    }
}
