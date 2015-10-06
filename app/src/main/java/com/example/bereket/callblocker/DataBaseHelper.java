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

    private static String NO_NAME_CONTACT = "No name";


    public DataBaseHelper(Context context){
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
                ID + " char(10) primary key, " +
                NAME + " varchar(100), " +
                PHONE_NUMBER + " char(20), " +
                DISPLAY_NUMBER + " char(20), " +
                OUTGOING_CALL_BLOCKED + " tinyint default 0, " +
                INCOMING_CALL_BLOCKED + " tinyint default 0, " +
                IS_NUMBER_STANDARDIZED + " boolean, " +
                NO_OF_TIMES_OUTGOING_BLOCKED + " int default 0, " +
                NO_OF_TIMES_INCOMING_BLOCKED + " int default 0," +
                " FOREIGN KEY (" + OUTGOING_CALL_BLOCKED + ") REFERENCES " + BLOCK_STATE_TABLE + "(" + BLOCK_STATE_ID + "), "  +
                " FOREIGN KEY (" + INCOMING_CALL_BLOCKED + ") REFERENCES " + BLOCK_STATE_TABLE + "(" + BLOCK_STATE_ID + ")"  +
                ")";

        //scheduled block table
        String createBlockScheduleTable = " CREATE TABLE " + BLOCK_SCHEDULE_TABLE + " (" +
                BLOCK_SCHEDULE_CONTACT_ID + " char(10), " +
                BLOCK_SCHEDULE_WEEK_DAY + " tinyint, " +
                BLOCK_SCHEDULE_BLOCK_TYPE + " tinyint, " +
                BLOCK_SCHEDULE_FROM + " long, " +
                BLOCK_SCHEDULE_TO + " long, " +
                " PRIMARY KEY (" + BLOCK_SCHEDULE_CONTACT_ID + ", " + BLOCK_SCHEDULE_WEEK_DAY + ")" +
                " FOREIGN KEY (" + BLOCK_SCHEDULE_CONTACT_ID + ") REFERENCES " + BLOCKED_LIST_TABLE + "(" + ID + "), "  +
                " FOREIGN KEY (" + BLOCK_SCHEDULE_BLOCK_TYPE + ") REFERENCES " + BLOCK_TYPE_TABLE + "(" + BLOCK_TYPE_ID + ") "  +
                ")";

        //call log table
        String createCallLogTable = " CREATE TABLE " +  CALL_LOG_TABLE + " ( " +
                CALL_LOG_ID + " integer primary key autoincrement, " +
                CALL_LOG_CONTACT_ID + " char(10), " +
                CALL_LOG_BLOCK_TYPE + " tinyint, " +
                CALL_LOG_TIME + " datetime," +
                " FOREIGN KEY (" + CALL_LOG_CONTACT_ID + ") REFERENCES " + BLOCKED_LIST_TABLE + "(" + ID + "), "  +
                " FOREIGN KEY (" + CALL_LOG_BLOCK_TYPE + ") REFERENCES " + BLOCK_TYPE_TABLE + "(" + BLOCK_TYPE_ID + ") "  +
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

    /*
     * <writable_database>.insert(..., <content_value>) is not used in favor of INSERT OR REPLACE statement
     * to avoid multiple queries to verify uniqueness of contact ID but updating some columns
     */
    public void insertContact(String contactId, String phoneNumber,  String displayNumber, String contactName, Boolean isNumberStandardized){

        if(phoneNumber == null || phoneNumber.isEmpty()) return;

        String query = "INSERT OR REPLACE INTO " + BLOCKED_LIST_TABLE  + " (" +
                        ID + ", " + PHONE_NUMBER + ", " + DISPLAY_NUMBER + ", " + NAME + ", " +
                        OUTGOING_CALL_BLOCKED + ", " + INCOMING_CALL_BLOCKED + ", " + IS_NUMBER_STANDARDIZED + ", " +
                        NO_OF_TIMES_OUTGOING_BLOCKED + ", " + NO_OF_TIMES_INCOMING_BLOCKED + ") " +
                        "VALUES ('" + contactId + "', '" + phoneNumber + "', '" + displayNumber + "', '" +
                        ((contactName == null || contactName.isEmpty()) ? NO_NAME_CONTACT : contactName) + "', '" +
                        isNumberStandardized + "', " +
                        "COALESCE((SELECT " + INCOMING_CALL_BLOCKED + " FROM " + BLOCKED_LIST_TABLE + " WHERE " + ID + " = " + contactId + "), 0), " + //default not blocked
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
        }
        else {
            return null;
        }
    }

    public ContactCursor getNonStandardizedPhoneContacts(){

        Cursor cursor = getWritableDatabase().query(BLOCKED_LIST_TABLE, null, IS_NUMBER_STANDARDIZED + " = ? ", new String[]{"0"}, null, null, null);

        if(cursor != null && cursor.getCount() > 0) {

            return new ContactCursor(cursor);
        }

        else return null;
    }

    public boolean deleteContact(Contact contact){

        int returnValue = getWritableDatabase().delete(BLOCKED_LIST_TABLE, ID + " = ? ", new String[]{contact.getId()});

        return returnValue > 0 ? true : false;
    }

    public void updateContact(Contact contact){

        ContentValues cv = new ContentValues();
        cv.put(NAME, contact.getContactName());
        cv.put(PHONE_NUMBER, contact.getPhoneNumber());
        cv.put(DISPLAY_NUMBER, contact.getDisplayNumber());
        cv.put(IS_NUMBER_STANDARDIZED, contact.isIsNumberStandardized());
        cv.put(OUTGOING_CALL_BLOCKED, contact.getOutGoingBlockedState());
        cv.put(INCOMING_CALL_BLOCKED, contact.getIncomingBlockedState());
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
            boolean isNumberStandardized = getInt(getColumnIndex(IS_NUMBER_STANDARDIZED)) == 1 ? true : false;
            int outgoingBlockedState = getInt(getColumnIndex(OUTGOING_CALL_BLOCKED));
            int incomingBlockedState = getInt(getColumnIndex(INCOMING_CALL_BLOCKED));
            int incomingBlockedCount = getInt(getColumnIndex(NO_OF_TIMES_INCOMING_BLOCKED));
            int outgoingBlockedCount = getInt(getColumnIndex(NO_OF_TIMES_OUTGOING_BLOCKED));

            contact.setId(contactId);
            contact.setPhoneNumber(phoneNumber);
            contact.setDisplayNumber(displayNumber);
            contact.setContactName(contactName);
            contact.setIsNumberStandardized(isNumberStandardized);
            contact.setIncomingBlockedState(incomingBlockedState);
            contact.setOutGoingBlockedState(outgoingBlockedState);
            contact.setIncomingBlockedCount(incomingBlockedCount);
            contact.setOutgoingBlockedCount(outgoingBlockedCount);

            return contact;
        }
    }

    public LogCursor queryLogs(){
        //Equivalent to "select * from LogRecord order by start_date asc"
        Cursor wrapped = getReadableDatabase().rawQuery("SELECT A.*, B." + NAME + " AS ContactName, B." + DISPLAY_NUMBER + " AS ContactNumber FROM " + CALL_LOG_TABLE +
                " A, " + BLOCKED_LIST_TABLE + " B ORDER BY " + CALL_LOG_TIME + " DESC ", null);//   query(BLOCKED_LIST_TABLE, null, null, null, null, null, NAME  + " asc");
        return new LogCursor(wrapped);
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
    public boolean insertSchedule(String contactId, Date startTime, Date endTime, int blockType){

        ContentValues cv = new ContentValues();
        cv.put(BLOCK_SCHEDULE_CONTACT_ID, contactId);
        cv.put(BLOCK_SCHEDULE_FROM, startTime.getTime());
        cv.put(BLOCK_SCHEDULE_TO, endTime.getTime());
        cv.put(BLOCK_SCHEDULE_BLOCK_TYPE, blockType);

        return getWritableDatabase().insert(BLOCK_SCHEDULE_TABLE, null, cv) == -1 ? false : true;

    }

    public boolean deleteSchedule(Schedule schedule){

       return getWritableDatabase().delete(BLOCK_SCHEDULE_TABLE, BLOCK_SCHEDULE_CONTACT_ID + " = ? and " + BLOCK_SCHEDULE_WEEK_DAY + " = ?", new String[]{schedule.getContactId(), String.valueOf(schedule.getWeekDay())}) != 0 ? true : false;
    }

    public Map<Integer,Schedule> queryContactSchedule(String contactId, int blockType){

        Cursor cursor = getReadableDatabase().query(BLOCK_SCHEDULE_TABLE, null, BLOCK_SCHEDULE_CONTACT_ID + " = ? and " + BLOCK_SCHEDULE_BLOCK_TYPE + " = ? ", new String[]{contactId, String.valueOf(blockType)}, null, null, BLOCK_SCHEDULE_WEEK_DAY  + " asc");

        Map<Integer,Schedule> scheduleMap = new HashMap<>();
        Schedule schedule;
        int weekDay;

        while(cursor.moveToNext()){

            schedule = new Schedule();

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

    public boolean updateSchedule(Schedule schedule){

        ContentValues cv = new ContentValues();

        cv.put(BLOCK_SCHEDULE_CONTACT_ID, schedule.getContactId());
        cv.put(BLOCK_SCHEDULE_FROM, schedule.getStartTime().getTime());
        cv.put(BLOCK_SCHEDULE_TO, schedule.getEndTime().getTime());
        cv.put(BLOCK_SCHEDULE_WEEK_DAY, schedule.getWeekDay());
        cv.put(BLOCK_SCHEDULE_BLOCK_TYPE, schedule.getBlockType());

        getWritableDatabase().update(BLOCK_SCHEDULE_TABLE, cv, BLOCK_SCHEDULE_CONTACT_ID + " = ? and " + BLOCK_SCHEDULE_WEEK_DAY + " = ?", new String[]{schedule.getContactId(), String.valueOf(schedule.getWeekDay())});
        return true;
    }

    public void updateSchedules(Map<Integer, Schedule> schedules){

        if(schedules.keySet().size() < 1) return;

        String query = "INSERT OR REPLACE INTO " + BLOCK_SCHEDULE_TABLE + " (" +
                BLOCK_SCHEDULE_CONTACT_ID + ", " +
                BLOCK_SCHEDULE_WEEK_DAY + ", " +
                BLOCK_SCHEDULE_FROM + ", " +
                BLOCK_SCHEDULE_TO + ", " +
                BLOCK_SCHEDULE_BLOCK_TYPE + ") values ";

        StringBuilder insertQueryBuilder = new StringBuilder();
        StringBuilder deleteQueryIdsBuilder = new StringBuilder();
//TODO bad way of accessing a single schedule
        Schedule tempSchedule = null;
        for(Schedule schedule : schedules.values()){
            if(tempSchedule == null) tempSchedule = schedule;
            insertQueryBuilder.append("( '" +  schedule.getContactId() + "', '" + schedule.getWeekDay() + "', '" + schedule.getStartTime().getTime() +"', '" + schedule.getEndTime().getTime() + "', '" + schedule.getBlockType() + "'),");
            deleteQueryIdsBuilder.append(schedule.getWeekDay() + ",");
        }
        //remove the last comma
        insertQueryBuilder.deleteCharAt(insertQueryBuilder.length() - 1);
        deleteQueryIdsBuilder.deleteCharAt(deleteQueryIdsBuilder.length() - 1);

        getWritableDatabase().execSQL(query + insertQueryBuilder.toString());
        //delete removed schedules - if any
        getWritableDatabase().execSQL("DELETE FROM " + BLOCK_SCHEDULE_TABLE + " WHERE " + BLOCK_SCHEDULE_CONTACT_ID + " = '" + tempSchedule.getContactId() + "' AND " + BLOCK_SCHEDULE_BLOCK_TYPE + " = " + tempSchedule.getBlockType() + " AND " + BLOCK_SCHEDULE_WEEK_DAY + " NOT IN (" + deleteQueryIdsBuilder.toString() + " )");
    }

    public boolean timeExistsInSchedule(String contactId, int blockType, int weekDay, long time){

        String query = "SELECT * FROM " + BLOCK_SCHEDULE_TABLE + " WHERE " + BLOCK_SCHEDULE_CONTACT_ID + " = ? AND " + BLOCK_SCHEDULE_BLOCK_TYPE +
                " = ? AND " + BLOCK_SCHEDULE_WEEK_DAY + " = ? AND " + BLOCK_SCHEDULE_FROM + " <= ?  AND " + BLOCK_SCHEDULE_TO + " >= ?";

        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{contactId, String.valueOf(blockType), String.valueOf(weekDay), String.valueOf(time), String.valueOf(time)});

        if(cursor != null && cursor.getCount() > 0)
            return true;
        else return false;
    }
}
