package com.example.bereket.callblocker;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bereket on 11/15/15.
 */
public class ContactsProvider {

    private ContentResolver contentResolver;
    private Context mContext;
    private static ContactsProvider mContactsProvider = null;
    private DataBaseHelper mDatabaseHelper;

    private ContactsProvider(Context context){

        mContext = context;
        contentResolver = mContext.getContentResolver();
        mDatabaseHelper = DataBaseHelper.getInstance(context);
    }

    public static ContactsProvider getInstatnce(Context context){

        if(mContactsProvider == null){

            mContactsProvider = new ContactsProvider(context);
        }

        return mContactsProvider;
    }

    /**
     * if country code is provided, the numbers will be standardized with it before being returned, otherwise
     */
    public Map<String, Contact> getAllContactsFromPhone(String countryCode){

        Map<String, Contact> contactMap = new HashMap<>();
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

                        Contact contact = mDatabaseHelper.getEmptyContact();

                        contact.setId(contactId);
                        contact.setDisplayNumber(phoneNumber);
                        String standardizedPhoneNumber = ContactManager.standardizePhoneNumber(phoneNumber, countryCode);
                        contact.setPhoneNumber(standardizedPhoneNumber);
                        contact.setContactName(contactName);

                        contactMap.put(standardizedPhoneNumber, contact);
                    }

                    pCur.close();
                }
            }

        } catch(Exception e){

        }
        finally {
            cursor.close();
        }

        return contactMap;
    }

    public Contact getContactFromPhoneBook(String searchPhoneNumber, String countryCode){

        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY/*DISPLAY_NAME*/, ContactsContract.Contacts.HAS_PHONE_NUMBER};
        String selection = null;
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, selection, null, null);
        Contact contact = null;

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
                            new String[]{String.valueOf(contactId)}, null);;

                    try{

                        pCur.moveToFirst();

                        phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        //check if phone number is not empty, create new contact and put it in list
                        if(phoneNumber != null){

                            String standardizedPhoneNumber = ContactManager.standardizePhoneNumber(phoneNumber, countryCode);

                            if(searchPhoneNumber.equals(standardizedPhoneNumber)){

                                contact = mDatabaseHelper.getEmptyContact();

                                contact.setId(contactId);
                                contact.setDisplayNumber(phoneNumber);
                                contact.setPhoneNumber(standardizedPhoneNumber);
                                contact.setContactName(contactName);

                                break;
                            }
                        }
                    }
                    finally { //I enclose with try-finally block to close pCur cursor before breaking out of the loop

                        pCur.close();
                    }
                }
            }

        } catch(Exception e){

        }
        finally {
            cursor.close();
        }

        return contact;
    }
}
