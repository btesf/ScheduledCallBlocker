package com.example.bereket.callblocker;

import android.content.Context;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Map;


/**
 * Created by bereket on 8/11/15.
 */
public class ContactManager {

    private DataBaseHelper mDataHelper;
    private Context mContext;

    public static String NON_STANDARDIZED_NUMBER_EXIST = "non.standard.number.exists";
    private static ContactManager mContactManager = null;

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

    public void insertContact(String contactId, String displayNumber, String contactName){

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

        mDataHelper.insertContact(contactId, phoneNumber, displayNumber, contactName, isNumberStandardized);
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

    public void insertNewOrUpdateExistingContact(String contactId, String displayNumber, String contactName){
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
        newContact.setContactName(contactName);
        newContact.setIsNumberStandardized(isNumberStandardized);

        //if country code cannot be decided just insert the number
        if(isNumberStandardized == false){

            mDataHelper.insertContact(newContact);
        }
        else{

            Contact oldContact = getContactByPhoneNumber(displayNumber);

            if(oldContact == null){// if number doesn't exist - or didn't match for the reason of one of the numbers is non-standardized

                mDataHelper.insertContact(newContact);
            }
            else{

                copyContactDetails(oldContact, newContact); //from old to new
                //if old contact exists and id is the same, just update the contact details (in case some of them are changed later)
                if(!oldContact.getId().equals(newContact.getId())){
                    //change the id of the old contact so that all referencing tables' ids could also be updated (schedule, log tables)
                    mDataHelper.updateContactId(oldContact.getId(), newContact.getId());
                }

                updateContact(newContact);
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

        return countryCodeValue;
    }

    public Map<Integer,Schedule> queryContactSchedule(String contactId, int blockType){

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
}
