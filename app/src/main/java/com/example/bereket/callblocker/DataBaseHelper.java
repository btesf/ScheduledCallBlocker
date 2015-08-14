package com.example.bereket.callblocker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by bereket on 7/21/15.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "callblocker.sqlite";
    private static int VERSION = 1;

    //blocked list table and columns
    private static String BLOCKED_LIST_TABLE = "BlockedList";
    public static String ID = "_id";
    public static String NAME = "name";
    public static String PHONE_NUMBER = "PhoneNumber";
    public static String DISPLAY_NUMBER = "DisplayNumber";
    public static String OUTGOING_CALL_BLOCKED = "OutgoingCall";
    public static String INCOMING_CALL_BLOCKED = "IncomingCall";
    public static String NO_OF_TIMES_OUTGOING_BLOCKED = "BlockedOutgoingCalls";
    public static String NO_OF_TIMES_INCOMING_BLOCKED = "BlockedIncomingCalls";

    private static String NO_NAME_CONTACT = "No name";


    public DataBaseHelper(Context context){
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String createBlockedListTable = " CREATE TABLE " +  BLOCKED_LIST_TABLE + " (" +
                ID + " CHAR(10) PRIMARY KEY, " +
                NAME + " varchar(100), " +
                PHONE_NUMBER + " char(20), " +
                DISPLAY_NUMBER + " char(20), " +
                OUTGOING_CALL_BLOCKED + " boolean default 0, " +
                INCOMING_CALL_BLOCKED + " boolean default 0, " +
                NO_OF_TIMES_OUTGOING_BLOCKED + " int default 0, " +
                NO_OF_TIMES_INCOMING_BLOCKED + " int default 0)";

        sqLiteDatabase.execSQL(createBlockedListTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //upgrade database goes here
    }

    /*
     * <writable_database>.insert(..., <content_value>) is not used in favor of INSERT OR REPLACE statement
     * to avoid multiple queries to verify uniqueness of contact ID but updating some columns
     */
    public void insertContact(String contactId, String phoneNumber,  String displayNumber, String contactName){

        if(phoneNumber == null || phoneNumber.isEmpty()) return;

        String query = "INSERT OR REPLACE INTO " + BLOCKED_LIST_TABLE  + " (" +
                        ID + ", " + PHONE_NUMBER + ", " + DISPLAY_NUMBER + ", " + NAME + ", " +
                        OUTGOING_CALL_BLOCKED + ", " + INCOMING_CALL_BLOCKED + ", " +
                        NO_OF_TIMES_OUTGOING_BLOCKED + ", " + NO_OF_TIMES_INCOMING_BLOCKED + ") " +
                        "VALUES ('" + contactId + "', '" + phoneNumber + "', '" + displayNumber + "', '" +
                        ((contactName == null || contactName.isEmpty()) ? NO_NAME_CONTACT : contactName) + "', " +
                        "COALESCE((SELECT " + OUTGOING_CALL_BLOCKED + " FROM " + BLOCKED_LIST_TABLE + " WHERE " + ID + " = " + contactId + "), 0), " + //default not blocked
                        "COALESCE((SELECT " + INCOMING_CALL_BLOCKED + " FROM " + BLOCKED_LIST_TABLE + " WHERE " + ID + " = " + contactId + "), 0), " + //default not blocked
                        "COALESCE((SELECT " + NO_OF_TIMES_OUTGOING_BLOCKED + " FROM " + BLOCKED_LIST_TABLE + " WHERE " + ID + " = " + contactId + "), 0), " + //default to zero
                        "COALESCE((SELECT " + NO_OF_TIMES_INCOMING_BLOCKED + " FROM " + BLOCKED_LIST_TABLE + " WHERE " + ID + " = " + contactId + "), 0))"; //default to zero

        getWritableDatabase().execSQL(query);
    }

    public Contact getContactByPhoneNumber(String phoneNumber){

        Cursor cursor = getWritableDatabase().query(BLOCKED_LIST_TABLE, null, PHONE_NUMBER + " = ? ", new String[]{phoneNumber}, null, null, null);

        if(cursor != null && cursor.getCount() > 0) {


            ContactCursor contactCursor = new ContactCursor(cursor);
            contactCursor.moveToFirst();
            Contact contact = contactCursor.getContact();
            contactCursor.close();

            return contact;


//            cursor.moveToFirst();
//            Contact contact = new Contact();
//
//            String contactId = cursor.getString(cursor.getColumnIndex(ID));
//            String displayNumber = cursor.getString(cursor.getColumnIndex(DISPLAY_NUMBER));
//            String contactName = cursor.getString(cursor.getColumnIndex(NAME));
//            boolean isOutgoingBlocked = cursor.getInt(cursor.getColumnIndex(OUTGOING_CALL_BLOCKED)) == 1 ? true : false;
//            boolean isIncomingBlocked = cursor.getInt(cursor.getColumnIndex(INCOMING_CALL_BLOCKED)) == 1 ? true : false;
//            int incomingBlockedCount = cursor.getInt(cursor.getColumnIndex(NO_OF_TIMES_INCOMING_BLOCKED));
//            int outgoingBlockedCount = cursor.getInt(cursor.getColumnIndex(NO_OF_TIMES_OUTGOING_BLOCKED));
//
//            contact.setId(contactId);
//            contact.setPhoneNumber(phoneNumber);
//            contact.setDisplayNumber(displayNumber);
//            contact.setContactName(contactName);
//            contact.setIsIncomingBlocked(isIncomingBlocked);
//            contact.setIsOutGoingBlocked(isOutgoingBlocked);
//            contact.setIncomingBlockedCount(incomingBlockedCount);
//            contact.setOutgoingBlockedCount(outgoingBlockedCount);
//            //close cursor
//            cursor.close();
//            return contact;

        }
        else {
            return null;
        }
        //return null;
    }

    public void updateContact(Contact contact){

        ContentValues cv = new ContentValues();
        cv.put(NAME, contact.getContactName());
        cv.put(PHONE_NUMBER, contact.getPhoneNumber());
        cv.put(DISPLAY_NUMBER, contact.getDisplayNumber());
        cv.put(OUTGOING_CALL_BLOCKED, contact.isIsOutGoingBlocked());
        cv.put(INCOMING_CALL_BLOCKED, contact.isIsIncomingBlocked());
        cv.put(NO_OF_TIMES_INCOMING_BLOCKED, contact.getIncomingBlockedCount());
        cv.put(NO_OF_TIMES_OUTGOING_BLOCKED, contact.getOutgoingBlockedCount());

        int rows = getWritableDatabase().update(BLOCKED_LIST_TABLE, cv, ID + " = ? ", new String[]{contact.getId()});
    }

    public ContactCursor queryContacts(){
        //Equivalent to "select * from run order by start_date asc"
        Cursor wrapped = getReadableDatabase().query(BLOCKED_LIST_TABLE, null, null, null, null, null, NAME  + " asc");
        return new ContactCursor(wrapped);
    }

    public static class ContactCursor extends CursorWrapper{

        public ContactCursor(Cursor cursor) {
            super(cursor);
        }

        public Contact getContact(){
            if(isBeforeFirst() || isAfterLast()) return null;

            Contact contact = new Contact();

            String contactId = getString(getColumnIndex(ID));
            String phoneNumber = getString(getColumnIndex(PHONE_NUMBER));
            String displayNumber = getString(getColumnIndex(DISPLAY_NUMBER));
            String contactName = getString(getColumnIndex(NAME));
            boolean isOutgoingBlocked = getInt(getColumnIndex(OUTGOING_CALL_BLOCKED)) == 1 ? true : false;
            boolean isIncomingBlocked = getInt(getColumnIndex(INCOMING_CALL_BLOCKED)) == 1 ? true : false;
            int incomingBlockedCount = getInt(getColumnIndex(NO_OF_TIMES_INCOMING_BLOCKED));
            int outgoingBlockedCount = getInt(getColumnIndex(NO_OF_TIMES_OUTGOING_BLOCKED));

            contact.setId(contactId);
            contact.setPhoneNumber(phoneNumber);
            contact.setDisplayNumber(displayNumber);
            contact.setContactName(contactName);
            contact.setIsIncomingBlocked(isIncomingBlocked);
            contact.setIsOutGoingBlocked(isOutgoingBlocked);
            contact.setIncomingBlockedCount(incomingBlockedCount);
            contact.setOutgoingBlockedCount(outgoingBlockedCount);

            return contact;
        }
    }
}
