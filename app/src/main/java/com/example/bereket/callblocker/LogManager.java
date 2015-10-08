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
        mDatabaseHelper = new DataBaseHelper(context);
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

}
