package com.example.bereket.callblocker;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

/**
 * Created by bereket on 8/5/15.
 */
public class ContactLoader extends AsyncTaskLoader<DataBaseHelper.ContactCursor> {

    private DataBaseHelper.ContactCursor mCursor;
    private DataBaseHelper dataHelper;

    public ContactLoader(Context context) {
        super(context);
        dataHelper = new DataBaseHelper(context);
    }

    @Override
    public DataBaseHelper.ContactCursor loadInBackground(){
        DataBaseHelper.ContactCursor cursor = loadData(); //TODO create data load here
        if(cursor !=  null){
            //Ensure that the content window is filled
            cursor.getCount();
        }
        return cursor;
    }

    @Override
    public void deliverResult(DataBaseHelper.ContactCursor data){
        Cursor oldCursor = mCursor;
        mCursor = data;
        if(isStarted()){
            super.deliverResult(data);
        }

        if(oldCursor != null && oldCursor != data && !oldCursor.isClosed()){
            oldCursor.close();
        }
    }

    private DataBaseHelper.ContactCursor loadData(){
        return dataHelper.queryContacts();
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
    public void onCanceled(DataBaseHelper.ContactCursor cursor){
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
