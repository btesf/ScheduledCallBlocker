package com.example.bereket.callblocker;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

/**
 * Created by bereket on 10/8/15.
 */
public class LogLoader extends AsyncTaskLoader<DataBaseHelper.LogCursor> {

    private DataBaseHelper.LogCursor mCursor;
    private LogManager logManager;

    public LogLoader(Context context) {
        super(context);
        logManager = LogManager.getInstance(context);
    }

    @Override
    public DataBaseHelper.LogCursor loadInBackground(){
        DataBaseHelper.LogCursor cursor = loadData();
        if(cursor !=  null){
            //Ensure that the content window is filled
            cursor.getCount();
        }
        return cursor;
    }

    @Override
    public void deliverResult(DataBaseHelper.LogCursor data){
        Cursor oldCursor = mCursor;
        mCursor = data;
        if(isStarted()){
            super.deliverResult(data);
        }

        if(oldCursor != null && oldCursor != data && !oldCursor.isClosed()){
            oldCursor.close();
        }
    }

    private DataBaseHelper.LogCursor loadData(){
        return logManager.queryAllLogs();
    }

    @Override
    protected void onStartLoading(){
        if(mCursor != null){
            deliverResult(mCursor);
        }
        if(takeContentChanged() || mCursor == null){
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading(){
        //Attempt to cancel the current load task if possible
        cancelLoad();
    }

    @Override
    public void onCanceled(DataBaseHelper.LogCursor cursor){
        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }
    }

    @Override
    protected void onReset(){
        super.onReset();

        //Ensure the loader is stopped
        onStopLoading();

        if(mCursor != null && !mCursor.isClosed()){
            mCursor.close();
        }
        mCursor = null;
    }
}
