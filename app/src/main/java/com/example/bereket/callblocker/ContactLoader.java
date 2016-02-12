package com.example.bereket.callblocker;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by bereket on 8/5/15.
 */
public class ContactLoader extends AsyncTaskLoader<DataBaseHelper.ContactCursor> {

    public static final String QUERY_STRING_KEY = "query.string.key";
    private DataBaseHelper.ContactCursor mCursor;
    private ContactManager contactManager;
    private String queryString = null;

    public ContactLoader(Context context, Bundle args) {
        super(context);
        contactManager = ContactManager.getInstance(context);

        if(args != null) {

            queryString = args.getString(QUERY_STRING_KEY);
        }
    }

    @Override
    public DataBaseHelper.ContactCursor loadInBackground(){
        DataBaseHelper.ContactCursor cursor = loadData();
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

        DataBaseHelper.ContactCursor contactCursor;

        contactCursor = (queryString == null) ? contactManager.queryContacts() : contactManager.queryContacts(queryString);

        return contactCursor;
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
