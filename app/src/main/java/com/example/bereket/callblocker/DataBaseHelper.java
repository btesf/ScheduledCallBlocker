package com.example.bereket.callblocker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static String IS_NUMBER_STANDARDIZED = "IsNumberStandardized";
    public static String IS_CONTACT_VISIBLE = "IsContactVisible";

    //block type table and columns constants
    private static String BLOCK_STATE_TABLE = "BlockState";
    private static String BLOCK_STATE_ID = "id";
    private static String BLOCK_STATE_NAME = "name";

    //block state table and column constants
    private static String BLOCK_TYPE_TABLE = "BlockType";
    private static String BLOCK_TYPE_NAME = "name";
    private static String BLOCK_TYPE_ID = "id";

    //scheduled block table
    private static String BLOCK_SCHEDULE_TABLE = "BlockSchedule";
    private static String BLOCK_SCHEDULE_ID = "_id";
    private static String BLOCK_SCHEDULE_CONTACT_ID = "contactId";
    private static String BLOCK_SCHEDULE_WEEK_DAY = "weekDay";
    private static String BLOCK_SCHEDULE_FROM = "startTime";
    private static String BLOCK_SCHEDULE_TO = "endTime";
    private static String BLOCK_SCHEDULE_BLOCK_TYPE = "blockType";

    //call log table
    private static String CALL_LOG_TABLE = "CallLog";
    private static String CALL_LOG_ID = "_id";
    private static String CALL_LOG_CONTACT_ID = "contactId";
    private static String CALL_LOG_BLOCK_TYPE = "blockType";
    private static String CALL_LOG_TIME = "time";

    //private static String NO_NAME_CONTACT = "No name";

    private static DataBaseHelper mDatabaseHelper = null;

    public static DataBaseHelper getInstance(Context context){

        if(mDatabaseHelper == null){

            mDatabaseHelper = new DataBaseHelper(context);
        }

        return mDatabaseHelper;
    }

    private DataBaseHelper(Context context){
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //holds blocked type reference table - 1-outgoing, 2-incoming
        String createBlockTypeTable = " CREATE TABLE " + BLOCK_TYPE_TABLE + " (" +
                BLOCK_TYPE_ID + " tinyint PRIMARY KEY, " +
                BLOCK_TYPE_NAME + " varchar(20) )";

        //holds blocked state reference table - 0-unblocked, 1-unscheduled, 2-scheduled
        String createBlockStateTable = " CREATE TABLE " + BLOCK_STATE_TABLE + " (" +
                BLOCK_STATE_ID + " tinyint PRIMARY KEY, " +
                BLOCK_STATE_NAME + " varchar(20) )";

        //holds blocked contacts records
        String createBlockedListTable = " CREATE TABLE " +  BLOCKED_LIST_TABLE + " (" +
                ID + " integer primary key, " +
                NAME + " varchar(100), " +
                PHONE_NUMBER + " char(20), " +
                DISPLAY_NUMBER + " char(20), " +
                OUTGOING_CALL_BLOCKED + " tinyint default 0, " +
                INCOMING_CALL_BLOCKED + " tinyint default 0, " +
                IS_NUMBER_STANDARDIZED + " boolean, " +
                NO_OF_TIMES_OUTGOING_BLOCKED + " int default 0, " +
                NO_OF_TIMES_INCOMING_BLOCKED + " int default 0," +
                IS_CONTACT_VISIBLE + " boolean default 1," +
                " FOREIGN KEY (" + OUTGOING_CALL_BLOCKED + ") REFERENCES " + BLOCK_STATE_TABLE + "(" + BLOCK_STATE_ID + "), "  +
                " FOREIGN KEY (" + INCOMING_CALL_BLOCKED + ") REFERENCES " + BLOCK_STATE_TABLE + "(" + BLOCK_STATE_ID + ")"  +
                ")";

        //scheduled block table
        String createBlockScheduleTable = " CREATE TABLE " + BLOCK_SCHEDULE_TABLE + " (" +
                BLOCK_SCHEDULE_ID + " integer primary key autoincrement, " +
                BLOCK_SCHEDULE_CONTACT_ID + " integer, " +
                BLOCK_SCHEDULE_WEEK_DAY + " tinyint, " +
                BLOCK_SCHEDULE_BLOCK_TYPE + " tinyint, " +
                BLOCK_SCHEDULE_FROM + " long, " +
                BLOCK_SCHEDULE_TO + " long, " +
                " FOREIGN KEY (" + BLOCK_SCHEDULE_CONTACT_ID + ") REFERENCES " + BLOCKED_LIST_TABLE + "(" + ID + ") ON DELETE CASCADE ON UPDATE CASCADE, "  +
                " FOREIGN KEY (" + BLOCK_SCHEDULE_BLOCK_TYPE + ") REFERENCES " + BLOCK_TYPE_TABLE + "(" + BLOCK_TYPE_ID + ") ON DELETE CASCADE ON UPDATE CASCADE"  +
                ")";

        //call log table
        String createCallLogTable = " CREATE TABLE " +  CALL_LOG_TABLE + " ( " +
                CALL_LOG_ID + " integer primary key autoincrement, " +
                CALL_LOG_CONTACT_ID + " integer, " +
                CALL_LOG_BLOCK_TYPE + " tinyint, " +
                CALL_LOG_TIME + " datetime," +
                " FOREIGN KEY (" + CALL_LOG_CONTACT_ID + ") REFERENCES " + BLOCKED_LIST_TABLE + "(" + ID + ") ON DELETE CASCADE ON UPDATE CASCADE, "  +
                " FOREIGN KEY (" + CALL_LOG_BLOCK_TYPE + ") REFERENCES " + BLOCK_TYPE_TABLE + "(" + BLOCK_TYPE_ID + ") ON DELETE CASCADE ON UPDATE CASCADE"  +
                " )";

        //block type reference insert string
        String insertBlockTypeReferences = " INSERT INTO " + BLOCK_TYPE_TABLE + " VALUES (1, 'outgoing'), (2, 'incoming') ";
        String insertBlockStateReferences = " INSERT INTO " + BLOCK_STATE_TABLE + " VALUES (0, 'unblocked'), (1, 'always blocked'), (2, 'scheduled block') ";

        //block state reference insert string
        sqLiteDatabase.execSQL(createBlockTypeTable);
        sqLiteDatabase.execSQL(createBlockStateTable);
        sqLiteDatabase.execSQL(createBlockedListTable);
        sqLiteDatabase.execSQL(createBlockScheduleTable);
        sqLiteDatabase.execSQL(createCallLogTable);
        //run insert statements
        sqLiteDatabase.execSQL(insertBlockTypeReferences);
        sqLiteDatabase.execSQL(insertBlockStateReferences);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //upgrade database goes here
    }

    /**
     * the method below is overrided for the sake of enforcing foreign key support in sqlite. By default foreign keys are set to off in sqlite
     * I want to enable foreign key support everytime a DB is opened
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    /*
     directly add a new number
     */

    public Contact getContactByPhoneNumber(String phoneNumber){

        //There are two kinds of numbers inserted into the database. 1. From contact, 2. directly (manuall)
        /*
        The problem is when a new number is added manually and the phone is out of network, the number won't be standardized.
        After standardization (always happening before number is searched in blocked table), there will be two similar numbers; one from contact, other manual
        but with similar phone number signature. The only way to determine which is which is to give an id format which can be easily be differentiated .
        - give very high value for non contact numbers and,
        - give very low value for contact numbers
        then, taking the top 1 will always return the least number
         */
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + BLOCKED_LIST_TABLE + " WHERE " + PHONE_NUMBER + " = ?  ORDER BY " + ID + " ASC LIMIT 1 ", new String[]{phoneNumber});

        if(cursor != null && cursor.getCount() > 0) {

            ContactCursor contactCursor = new ContactCursor(cursor);
            contactCursor.moveToFirst();
            Contact contact = contactCursor.getContact();
            contactCursor.close();

            return contact;
        }
        else {
            cursor.close();
            return null;
        }
    }

    public Contact getEmptyContact(){

        Contact contact = new Contact();

        contact.setOutgoingBlockedCount(0);
        contact.setOutGoingBlockedState(BlockState.DONT_BLOCK);
        contact.setIncomingBlockedCount(0);
        contact.setIncomingBlockedState(BlockState.DONT_BLOCK);
        contact.setIsNumberStandardized(false);
        contact.setIsContactVisible(ContactVisibilityState.VISIBLE);

        return contact;
    }

    public ContactCursor getNonStandardizedPhoneContacts(){

        Cursor cursor = getWritableDatabase().query(BLOCKED_LIST_TABLE, null, IS_NUMBER_STANDARDIZED + " = ? ", new String[]{"0"}, null, null, null);

        if(cursor != null && cursor.getCount() > 0) {

            return new ContactCursor(cursor);
        }

        else return null;
    }

    //overloaded method
    public boolean insertContact(Contact contact){

        ContentValues cv = new ContentValues();

        cv.put(ID, contact.getId());
        cv.put(NAME, contact.getContactName() == null ? contact.getDisplayNumber() : contact.getContactName());// = "name";
        cv.put(PHONE_NUMBER, contact.getPhoneNumber());// = "PhoneNumber";
        cv.put(DISPLAY_NUMBER, contact.getDisplayNumber());// = "DisplayNumber";
        cv.put(OUTGOING_CALL_BLOCKED, contact.getOutGoingBlockedState());// = "OutgoingCall";
        cv.put(INCOMING_CALL_BLOCKED, contact.getIncomingBlockedState());// = "IncomingCall";
        cv.put(NO_OF_TIMES_OUTGOING_BLOCKED, contact.getOutgoingBlockedCount());// = "BlockedOutgoingCalls";
        cv.put(NO_OF_TIMES_INCOMING_BLOCKED, contact.getIncomingBlockedCount());// = "BlockedIncomingCalls";
        cv.put(IS_NUMBER_STANDARDIZED, contact.isIsNumberStandardized());// = "IsNumberStandardized";
        cv.put(IS_CONTACT_VISIBLE, contact.isContactVisible()); // = "IsContactVisible"

        long result = getWritableDatabase().insert(BLOCKED_LIST_TABLE, null, cv);

        return result == -1 ? false : true;
    }

    public boolean deleteContact(Contact contact){

        int returnValue = getWritableDatabase().delete(BLOCKED_LIST_TABLE, ID + " = ? ", new String[]{String.valueOf(contact.getId())});

        return returnValue > 0 ? true : false;
    }

    public boolean deleteHiddenContacts(){

        int returnValue = getWritableDatabase().delete(BLOCKED_LIST_TABLE, IS_CONTACT_VISIBLE + " = ? ", new String[]{String.valueOf("0")});

        return returnValue > 0 ? true : false;
    }

    public boolean updateContact(Contact contact){

        ContentValues cv = new ContentValues();
        cv.put(NAME, contact.getContactName());
        cv.put(PHONE_NUMBER, contact.getPhoneNumber());
        cv.put(DISPLAY_NUMBER, contact.getDisplayNumber());
        cv.put(IS_NUMBER_STANDARDIZED, contact.isIsNumberStandardized());
        cv.put(OUTGOING_CALL_BLOCKED, contact.getOutGoingBlockedState());
        cv.put(INCOMING_CALL_BLOCKED, contact.getIncomingBlockedState());
        cv.put(NO_OF_TIMES_INCOMING_BLOCKED, contact.getIncomingBlockedCount());
        cv.put(NO_OF_TIMES_OUTGOING_BLOCKED, contact.getOutgoingBlockedCount());
        cv.put(IS_CONTACT_VISIBLE, contact.isContactVisible());

        int rows = getWritableDatabase().update(BLOCKED_LIST_TABLE, cv, ID + " = ? ", new String[]{String.valueOf(contact.getId())});

        return rows < 1 ? false : true;
    }

    public void updateContactId(long oldContactId, long newContactId){

       String query = "UPDATE " + BLOCKED_LIST_TABLE + " SET " + ID + " = " + newContactId + " WHERE " + ID + " = " + oldContactId;

       getWritableDatabase().execSQL(query);
    }

    public ContactCursor queryContacts(){
        //Equivalent to "select * from BlockList where IsContactVisible = 1 order by start_date asc"
        Cursor wrapped = getReadableDatabase().query(BLOCKED_LIST_TABLE, null, IS_CONTACT_VISIBLE + " = ? ", new String[]{"1"}, null, null, NAME + " asc");
        return new ContactCursor(wrapped);
    }

    public void unblockListByType(Integer blockType){

        ContentValues values = new ContentValues();

        switch(blockType){

            case BlockType.INCOMING:

                values.put(INCOMING_CALL_BLOCKED, BlockState.DONT_BLOCK);
                getWritableDatabase().update(BLOCKED_LIST_TABLE, values, INCOMING_CALL_BLOCKED + " <> ? ", new String[]{String.valueOf(BlockState.DONT_BLOCK)})  ;
                break;
            case BlockType.OUTGOING:

                values.put(OUTGOING_CALL_BLOCKED, BlockState.DONT_BLOCK);
                getWritableDatabase().update(BLOCKED_LIST_TABLE, values, OUTGOING_CALL_BLOCKED + " <> ? ", new String[]{String.valueOf(BlockState.DONT_BLOCK)});
                break;
            default:
            //error
        }
    }

    public void unblockScheduledListByType(Integer blockType){

        ContentValues values = new ContentValues();

        switch(blockType){

            case BlockType.INCOMING:

                values.put(INCOMING_CALL_BLOCKED, BlockState.DONT_BLOCK);
                getWritableDatabase().update(BLOCKED_LIST_TABLE, values, INCOMING_CALL_BLOCKED + " <> ? ", new String[]{String.valueOf(BlockState.DONT_BLOCK)});
                getWritableDatabase().delete(BLOCK_SCHEDULE_TABLE, BLOCK_SCHEDULE_BLOCK_TYPE + " = ? ", new String[]{String.valueOf(BlockType.INCOMING)});

                break;
            case BlockType.OUTGOING:

                values.put(OUTGOING_CALL_BLOCKED, BlockState.DONT_BLOCK);
                getWritableDatabase().update(BLOCKED_LIST_TABLE, values, OUTGOING_CALL_BLOCKED + " <> ? ", new String[]{String.valueOf(BlockState.DONT_BLOCK)});
                getWritableDatabase().delete(BLOCK_SCHEDULE_TABLE, BLOCK_SCHEDULE_BLOCK_TYPE + " = ? ", new String[]{String.valueOf(BlockType.OUTGOING)});

                break;
            default:
                //error
        }

    }

    public static class ContactCursor extends CursorWrapper{

        public ContactCursor(Cursor cursor) {
            super(cursor);
        }

        public Contact getContact(){
            if(isBeforeFirst() || isAfterLast()) return null;

            Contact contact = new Contact();

            long contactId = getLong(getColumnIndex(ID));
            String phoneNumber = getString(getColumnIndex(PHONE_NUMBER));
            String displayNumber = getString(getColumnIndex(DISPLAY_NUMBER));
            String contactName = getString(getColumnIndex(NAME));
            boolean isNumberStandardized = getInt(getColumnIndex(IS_NUMBER_STANDARDIZED)) == 1 ? true : false;
            int outgoingBlockedState = getInt(getColumnIndex(OUTGOING_CALL_BLOCKED));
            int incomingBlockedState = getInt(getColumnIndex(INCOMING_CALL_BLOCKED));
            int incomingBlockedCount = getInt(getColumnIndex(NO_OF_TIMES_INCOMING_BLOCKED));
            int outgoingBlockedCount = getInt(getColumnIndex(NO_OF_TIMES_OUTGOING_BLOCKED));
            boolean isContactVisible = getInt(getColumnIndex(IS_CONTACT_VISIBLE)) == 1 ? true : false;

            contact.setId(contactId);
            contact.setPhoneNumber(phoneNumber);
            contact.setDisplayNumber(displayNumber);
            contact.setContactName(contactName);
            contact.setIsNumberStandardized(isNumberStandardized);
            contact.setIncomingBlockedState(incomingBlockedState);
            contact.setOutGoingBlockedState(outgoingBlockedState);
            contact.setIncomingBlockedCount(incomingBlockedCount);
            contact.setOutgoingBlockedCount(outgoingBlockedCount);
            contact.setIsContactVisible(isContactVisible);

            return contact;
        }
    }

    public LogCursor queryAllLogs(){
        //
        Cursor wrapped = getReadableDatabase().rawQuery("SELECT A.*, B." + NAME + " AS ContactName, B." + DISPLAY_NUMBER + " AS ContactNumber FROM " + CALL_LOG_TABLE +
                " A INNER JOIN " + BLOCKED_LIST_TABLE + " B ON A." + CALL_LOG_CONTACT_ID + " = B." + ID + " ORDER BY " + CALL_LOG_TIME + " DESC ", null);//   query(BLOCKED_LIST_TABLE, null, null, null, null, null, NAME  + " asc");
        return new LogCursor(wrapped);
    }

    public LogCursor querySingleContactLog(long contactId){

        Cursor wrapped = getReadableDatabase().rawQuery("SELECT A.*, B." + NAME + " AS ContactName, B." + DISPLAY_NUMBER + " AS ContactNumber FROM " + CALL_LOG_TABLE +
                " A, " + BLOCKED_LIST_TABLE + " B WHERE A." + CALL_LOG_CONTACT_ID + " = ? ORDER BY " + CALL_LOG_TIME + " DESC ", new String[]{String.valueOf(contactId)}); //   query(BLOCKED_LIST_TABLE, null, null, null, null, null, NAME  + " asc");
        return new LogCursor(wrapped);
    }

    public boolean insertLog(long contactId, int blockType){

        ContentValues cv = new ContentValues();
        cv.put(CALL_LOG_CONTACT_ID, contactId);
        cv.put(CALL_LOG_BLOCK_TYPE, blockType);
        cv.put(CALL_LOG_TIME, (new Date()).getTime());

        if(getWritableDatabase().insert(CALL_LOG_TABLE, null, cv) == -1)
            return false;
        else return true;
    }

    //TODO : don't return any feedback after deleting? not good DB transaction management practice
    public boolean deleteLogs(){

        return getWritableDatabase().delete(CALL_LOG_TABLE, null, null) == 0 ? false : true;
    }


    public static class LogCursor extends CursorWrapper{

        public LogCursor(Cursor cursor) {
            super(cursor);
        }

        public LogRecord getLog(){

            if(isBeforeFirst() || isAfterLast()) return null;

            LogRecord logRecord = new LogRecord();

            int callLogId = getInt(getColumnIndex(CALL_LOG_ID));
            String contactId = getString(getColumnIndex(CALL_LOG_CONTACT_ID));
            int callBlockType = getInt(getColumnIndex(CALL_LOG_BLOCK_TYPE));
            long callLogTime = getLong(getColumnIndex(CALL_LOG_TIME));
            String contactName = getString(getColumnIndex("ContactName"));
            String contactNumber = getString(getColumnIndex("ContactNumber"));

            logRecord.setId(callLogId);
            logRecord.setBlockType(callBlockType);
            logRecord.setContactId(contactId);
            logRecord.setLogDate(new Date(callLogTime));
            logRecord.setContactName(contactName);
            logRecord.setContactPhone(contactNumber);

            return logRecord;
        }
    }

    //schedule
    public boolean insertSchedule(long contactId, Date startTime, Date endTime, int blockType, int weekDay){

        ContentValues cv = new ContentValues();
        cv.put(BLOCK_SCHEDULE_CONTACT_ID, contactId);
        cv.put(BLOCK_SCHEDULE_FROM, startTime.getTime());
        cv.put(BLOCK_SCHEDULE_TO, endTime.getTime());
        cv.put(BLOCK_SCHEDULE_BLOCK_TYPE, blockType);
        cv.put(BLOCK_SCHEDULE_WEEK_DAY, weekDay);

        return getWritableDatabase().insert(BLOCK_SCHEDULE_TABLE, null, cv) == -1 ? false : true;

    }

    public boolean updateSchedule(Schedule schedule){

        ContentValues cv = new ContentValues();

        cv.put(BLOCK_SCHEDULE_CONTACT_ID, schedule.getContactId());
        cv.put(BLOCK_SCHEDULE_FROM, schedule.getStartTime().getTime());
        cv.put(BLOCK_SCHEDULE_TO, schedule.getEndTime().getTime());
        cv.put(BLOCK_SCHEDULE_WEEK_DAY, schedule.getWeekDay());
        cv.put(BLOCK_SCHEDULE_BLOCK_TYPE, schedule.getBlockType());

        getWritableDatabase().update(BLOCK_SCHEDULE_TABLE, cv, BLOCK_SCHEDULE_ID + " = ? ", new String[]{String.valueOf(schedule.getId())});
        return true;
    }

    public boolean deleteSchedule(Schedule schedule){

       return getWritableDatabase().delete(BLOCK_SCHEDULE_TABLE, BLOCK_SCHEDULE_ID + " = ? ", new String[]{String.valueOf(schedule.getId())}) != 0 ? true : false;
    }


    public boolean deleteAllSchedulesForContact(long contactId, Integer blockType){

        return getWritableDatabase().delete(BLOCK_SCHEDULE_TABLE, BLOCK_SCHEDULE_CONTACT_ID + " = ? and " + BLOCK_SCHEDULE_BLOCK_TYPE + " = ? ", new String[]{String.valueOf(contactId), String.valueOf(blockType)}) != 0 ? true : false;
    }

    public Map<Integer,Schedule> queryContactSchedule(long contactId, int blockType){

        Cursor cursor = getReadableDatabase().query(BLOCK_SCHEDULE_TABLE, null, BLOCK_SCHEDULE_CONTACT_ID + " = ? and " + BLOCK_SCHEDULE_BLOCK_TYPE + " = ? ", new String[]{String.valueOf(contactId), String.valueOf(blockType)}, null, null, BLOCK_SCHEDULE_WEEK_DAY  + " asc");

        Map<Integer,Schedule> scheduleMap = new HashMap<>();
        Schedule schedule;
        int weekDay;

        while(cursor.moveToNext()){

            schedule = new Schedule();

            schedule.setId(cursor.getInt(cursor.getColumnIndex(BLOCK_SCHEDULE_ID)));
            schedule.setContactId(contactId);
            schedule.setStartTime(new Date(cursor.getLong(cursor.getColumnIndex(BLOCK_SCHEDULE_FROM))));
            schedule.setEndTime(new Date(cursor.getLong(cursor.getColumnIndex(BLOCK_SCHEDULE_TO))));
            weekDay = cursor.getInt(cursor.getColumnIndex(BLOCK_SCHEDULE_WEEK_DAY));
            schedule.setWeekDay(weekDay);
            schedule.setBlockType(cursor.getInt(cursor.getColumnIndex(BLOCK_SCHEDULE_BLOCK_TYPE)));

            scheduleMap.put(weekDay, schedule);
        }

        if(cursor != null){
            cursor.close();
        }

        return scheduleMap;
    }

    public boolean timeExistsInSchedule(long contactId, int blockType, int weekDay, long time){

        String query = "SELECT * FROM " + BLOCK_SCHEDULE_TABLE + " WHERE " + BLOCK_SCHEDULE_CONTACT_ID + " = ? AND " + BLOCK_SCHEDULE_BLOCK_TYPE +
                " = ? AND " + BLOCK_SCHEDULE_WEEK_DAY + " = ? AND " + BLOCK_SCHEDULE_FROM + " <= ?  AND " + BLOCK_SCHEDULE_TO + " >= ?";

        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(contactId), String.valueOf(blockType), String.valueOf(weekDay), String.valueOf(time), String.valueOf(time)});

        if(cursor != null && cursor.getCount() > 0)
            return true;
        else return false;
    }

    //TODO something should be returned from the query outcome
    public void cleanUpDuplicateContacts(){

        String targetContacts = " CREATE TEMP TABLE TargetContact AS SELECT " + PHONE_NUMBER + " FROM " + BLOCKED_LIST_TABLE + "  GROUP BY " + PHONE_NUMBER + " HAVING COUNT(*) > 1";
        String dropTargetContactTempTableQuery = " DROP TABLE TargetContact ";
        //TODO optimize the sub query . It is not a good idea to use sub-query on where clause
        String query = "DELETE FROM " + BLOCKED_LIST_TABLE + " WHERE " + ID + " NOT IN (" +
                "SELECT MIN(" + ID + ") FROM " + BLOCKED_LIST_TABLE + " WHERE " + PHONE_NUMBER + " IN (SELECT " + PHONE_NUMBER + " FROM TargetContact))";

        getWritableDatabase().execSQL( targetContacts);
        getWritableDatabase().execSQL(query);
        getWritableDatabase().execSQL(dropTargetContactTempTableQuery);
    }
}
