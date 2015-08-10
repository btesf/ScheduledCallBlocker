package com.example.bereket.callblocker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by bereket on 7/21/15.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "callblocker.sqlite";
    private static int VERSION = 1;

    //blocked list table and columns
    private static String BLOCKED_LIST_TABLE = "BlockedList";
    private static String ID = "_id";
    private static String NAME = "name";
    private static String PHONE_NUMBER = "PhoneNumber";
    private static String OUTGOING_CALL_BLOCKED = "OutgoingCall";
    private static String INCOMING_CALL_BLOCKED = "IncomingCall";
    private static String NO_OF_TIMES_OUTGOING_BLOCKED = "BlockedOutgoingCalls";
    private static String NO_OF_TIMES_INCOMING_BLOCKED = "BlockedIncomingCalls";

    private static String NO_NAME_CONTACT = "No name";


    public DataBaseHelper(Context context){
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String createBlockedListTable = " CREATE TABLE " +  BLOCKED_LIST_TABLE + " (" +
                ID + " CHAR(10) PRIMARY KEY, " +
                NAME + " varchar(100), " +
                PHONE_NUMBER + " char(15), " +
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
    public void insertContact(String contactId, String phoneNumber, String contactName){

        if(phoneNumber == null || phoneNumber.isEmpty()) return;

        String query = "INSERT OR REPLACE INTO " + BLOCKED_LIST_TABLE  + " (" +
                        ID + ", " + PHONE_NUMBER + ", " + NAME + ", " +
                        OUTGOING_CALL_BLOCKED + ", " + INCOMING_CALL_BLOCKED + ", " +
                        NO_OF_TIMES_OUTGOING_BLOCKED + ", " + NO_OF_TIMES_INCOMING_BLOCKED + ") " +
                        "VALUES ('" + contactId + "', '" + phoneNumber + "', '" +
                        ((contactName == null || contactName.isEmpty()) ? NO_NAME_CONTACT : contactName) + "', " +
                        "COALESCE((SELECT " + OUTGOING_CALL_BLOCKED + " FROM " + BLOCKED_LIST_TABLE + " WHERE " + ID + " = " + contactId + "), 0), " + //default not blocked
                        "COALESCE((SELECT " + INCOMING_CALL_BLOCKED + " FROM " + BLOCKED_LIST_TABLE + " WHERE " + ID + " = " + contactId + "), 0), " + //default not blocked
                        "COALESCE((SELECT " + NO_OF_TIMES_OUTGOING_BLOCKED + " FROM " + BLOCKED_LIST_TABLE + " WHERE " + ID + " = " + contactId + "), 0), " + //default to zero
                        "COALESCE((SELECT " + NO_OF_TIMES_INCOMING_BLOCKED + " FROM " + BLOCKED_LIST_TABLE + " WHERE " + ID + " = " + contactId + "), 0))"; //default to zero

        getWritableDatabase().execSQL(query);
    }

    public Contact getContactByPhoneNumber(String phoneNumber){

        Cursor cursor = getWritableDatabase().query(BLOCKED_LIST_TABLE, null, PHONE_NUMBER + " = ? ", new String[]{phoneNumber}, null, null, null);

        if((cursor != null) && (cursor.getCount() > 0)) return null;

        return new ContactCursor(cursor).getContact();
    }

    public void updateContact(Contact contact){

        ContentValues cv = new ContentValues();
        cv.put(NAME, contact.getContactName());
        cv.put(PHONE_NUMBER, contact.getPhoneNumber());
        cv.put(OUTGOING_CALL_BLOCKED, contact.getOutgoingBlockedCount());
        cv.put(INCOMING_CALL_BLOCKED, contact.getIncomingBlockedCount());
        cv.put(NO_OF_TIMES_INCOMING_BLOCKED, contact.getIncomingBlockedCount());
        cv.put(NO_OF_TIMES_OUTGOING_BLOCKED, contact.getOutgoingBlockedCount());

        getWritableDatabase().update(BLOCKED_LIST_TABLE, cv, ID + " = ? ", new String[]{contact.getId()});
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
            String contactName = getString(getColumnIndex(NAME));
            boolean isOutgoingBlocked = getInt(getColumnIndex(OUTGOING_CALL_BLOCKED)) == 1 ? true : false;
            boolean isIncomingBlocked = getInt(getColumnIndex(INCOMING_CALL_BLOCKED)) == 1 ? true : false;
            int incomingBlockedCount = getInt(getColumnIndex(NO_OF_TIMES_INCOMING_BLOCKED));
            int outgoingBlockedCount = getInt(getColumnIndex(NO_OF_TIMES_OUTGOING_BLOCKED));

            contact.setId(contactId);
            contact.setPhoneNumber(phoneNumber);
            contact.setContactName(contactName);
            contact.setIsIncomingBlocked(isIncomingBlocked);
            contact.setIsOutGoingBlocked(isOutgoingBlocked);
            contact.setIncomingBlockedCount(incomingBlockedCount);
            contact.setOutgoingBlockedCount(outgoingBlockedCount);

            return contact;
        }
    }
}
