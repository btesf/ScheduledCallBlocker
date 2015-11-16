package com.example.bereket.callblocker;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bereket on 11/15/15.
 */
public class ContactsProvider {

    private ContentResolver contentResolver;
    private Context mContext;
    private static ContactsProvider mContactsProvider = null;
    private ContactManager mContactManager;

    private ContactsProvider(Context context){

        mContext = context;
        contentResolver = mContext.getContentResolver();
        mContactManager = ContactManager.getInstance(context);
    }

    public static ContactsProvider getInstatnce(Context context){

        if(mContactsProvider == null){

            mContactsProvider = new ContactsProvider(context);
        }

        return mContactsProvider;
    }

    public List<Contact> getAllContactsFromPhone(){

        List<Contact> contactList = new ArrayList<>();
        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY/*DISPLAY_NAME*/, ContactsContract.Contacts.HAS_PHONE_NUMBER};
        String selection = null;
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, selection, null, null);

        try{

            while(cursor.moveToNext()){

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                    String phoneNumber = null;

                    Cursor pCur = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{String.valueOf(contactId)}, null);

                    pCur.moveToFirst();

                    phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    //check if phone number is not empty, create new contact and put it in list
                    if(phoneNumber != null){

                        Contact contact = mContactManager.getEmptyContact();

                        contact.setId(contactId);
                        contact.setDisplayNumber(phoneNumber);
                        contact.setPhoneNumber(ContactManager.standardizePhoneNumber(phoneNumber, mContactManager.getCountryCodeFromNetwork()));
                        contact.setContactName(contactName);

                        contactList.add(contact);
                    }

                    pCur.close();
                }
            }

        } catch(Exception e){

            cursor.close();
        }

        return contactList;
    }
}
