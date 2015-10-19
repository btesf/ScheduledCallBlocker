package com.example.bereket.callblocker;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.File;
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

    public void insertContact(String contactId, String displayNumber, String contactName){

        String countryCodeValue = null;
        //to make sure if a number is standardized (into E-164 format) before it is stored. Otherwise a system preference will be set so that a standardizing service would run
        boolean isNumberStandardized = false;

        TelephonyManager tm = (TelephonyManager)mContext.getSystemService(mContext.TELEPHONY_SERVICE);
        countryCodeValue = tm.getNetworkCountryIso();
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

    public boolean updateContact(Contact contact){

        return mDataHelper.updateContact(contact);
    }

    public boolean deleteContact(Contact contact){

        return mDataHelper.deleteContact(contact);
    }

    public Contact getContactByPhoneNumber(String phoneNumber){

        return mDataHelper.getContactByPhoneNumber(phoneNumber);
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
}
