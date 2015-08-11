package com.example.bereket.callblocker;

import android.content.Context;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

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
        String phoneNumber = standardizePhoneNumber(displayNumber);

        mDataHelper.insertContact(contactId, phoneNumber, displayNumber, contactName);
    }

    public Contact getContactByPhoneNumber(String phoneNumber){

        return mDataHelper.getContactByPhoneNumber(phoneNumber);
    }

    private String standardizePhoneNumber(String nonStandardPhone){

        return PhoneNumberUtil.normalizeDigitsOnly(nonStandardPhone);
    }
}
