package com.example.bereket.callblocker;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;


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
        mDataHelper = new DataBaseHelper(context);
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

        if(countryCodeValue != null){ // if network is available (in service area)

            isNumberStandardized = true;
        }
        else{
            //set system preference and that will fire a standardizing service to run when any telephone event is triggered
            setNonStandardizedPreference(true);
        }

        mDataHelper.insertContact(contactId, phoneNumber, displayNumber, contactName, isNumberStandardized);
    }

    public boolean deleteContact(Contact contact){

        return mDataHelper.deleteContact(contact);
    }

    public Contact getContactByPhoneNumber(String phoneNumber){

        return mDataHelper.getContactByPhoneNumber(phoneNumber);
    }


    public void standardizeNonStandardContactPhones(String countryCode){

        DataBaseHelper.ContactCursor contactCursor = mDataHelper.getNonStandardizedPhoneContacts();

        if(contactCursor != null){

            while (contactCursor.moveToNext()){

                Contact nonStandardContact = contactCursor.getContact();
                nonStandardContact.setPhoneNumber(standardizePhoneNumber(nonStandardContact.getPhoneNumber(), countryCode));
                nonStandardContact.setIsNumberStandardized(true);
            }

            contactCursor.close();
            setNonStandardizedPreference(false);
        }
    }

    public static String standardizePhoneNumber(String nonStandardPhone, String countryCode){

        String formattedPhoneNumber =  PhoneNumberUtil.normalizeDigitsOnly(nonStandardPhone);

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        //if country code is not null, standardize the number into E164 format
        if(countryCode != null){

            try {
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(formattedPhoneNumber, countryCode);
                //Since you know the country you can format it as follows:
                formattedPhoneNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164 );
            } catch (NumberParseException e) {
                System.err.println("NumberParseException was thrown: " + e.toString());
            }
        }

        return formattedPhoneNumber;
    }

    public boolean nonStandardizedPreferenceEnabled(){

        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(NON_STANDARDIZED_NUMBER_EXIST, false);
    }

    public void setNonStandardizedPreference(boolean exists){

        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                .putBoolean(NON_STANDARDIZED_NUMBER_EXIST, exists);
    }
}
