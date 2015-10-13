package com.example.bereket.callblocker;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by bereket on 10/6/15.
 */
public class LogManager {

    private Context mContext;
    private DataBaseHelper mDatabaseHelper;
    private static LogManager mLogManager;

    private LogManager(Context context){

        mContext = context;
        mDatabaseHelper = DataBaseHelper.getInstance(context);
    }

    public static LogManager getInstance(Context context){

        if(mLogManager == null){

            mLogManager = new LogManager(context);
        }

        return mLogManager;
    }

    public DataBaseHelper.LogCursor queryAllLogs(){

        return mDatabaseHelper.queryAllLogs();
    }

    public DataBaseHelper.LogCursor querySingleContactLog(String contactId){

        return mDatabaseHelper.querySingleContactLog(contactId);
    }

    public boolean insertLog(String contactId, int blockType){

        return mDatabaseHelper.insertLog(contactId, blockType);
    }

    public boolean log(Contact contact, int blockType){

        int count;

        if(blockType == BlockType.INCOMING){
            count = contact.getIncomingBlockedCount();
            contact.setIncomingBlockedCount(++count);//increment count by 1
        }
        else{ // OUTGOING
            count = contact.getOutgoingBlockedCount();
            contact.setOutgoingBlockedCount(++count);//increment count by 1
        }

        return insertLog(contact.getId(), blockType) && mDatabaseHelper.updateContact(contact);
    }

    public boolean deleteLogs(){

       return mDatabaseHelper.deleteLogs();
    }

}
