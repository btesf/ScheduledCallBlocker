package com.example.bereket.callblocker;

import android.content.Context;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;


/**
 * Created by bereket on 8/11/15.
 */
public class ContactManager {

    private DataBaseHelper mDataHelper;
    private Context mContext;

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

        //standardize phone number
        String phoneNumber = standardizePhoneNumber(displayNumber, "ET");

        mDataHelper.insertContact(contactId, phoneNumber, displayNumber, contactName);
    }

    public boolean deleteContact(Contact contact){

        return mDataHelper.deleteContact(contact);
    }

    public Contact getContactByPhoneNumber(String phoneNumber){

        return mDataHelper.getContactByPhoneNumber(phoneNumber);
    }

    public static String standardizePhoneNumber(String nonStandardPhone, String countryCode){


        String formattedPhoneNumber =  PhoneNumberUtil.normalizeDigitsOnly(nonStandardPhone);

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(formattedPhoneNumber, "ET");
            //Since you know the country you can format it as follows:
            formattedPhoneNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164 );
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        return formattedPhoneNumber;
    }
}
