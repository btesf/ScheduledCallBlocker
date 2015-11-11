package com.example.bereket.callblocker;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by bereket on 10/6/15.
 */
public class LogManager {

    private Context mContext;
    private DataBaseHelper mDatabaseHelper;
    private ContactManager mContactManager;
    private static LogManager mLogManager;

    private LogManager(Context context){

        mContext = context;
        mContactManager = ContactManager.getInstance(context);
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

    public DataBaseHelper.LogCursor querySingleContactLog(long contactId){

        return mDatabaseHelper.querySingleContactLog(contactId);
    }

    public boolean insertLog(long contactId, int blockType){

        return mDatabaseHelper.insertLog(contactId, blockType);
    }

    /*
    this method inserts log record for  block, as well as increments the contact blocked call count.
     */
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

        return insertLog(contact.getId(), blockType) && mContactManager.updateContact(contact);
    }

    public boolean log(String phoneNumber, int blockType){
        //try to get number from list
        Contact blockedContact = mContactManager.getContactByStandardizedPhoneNumber(phoneNumber);
        //if contact is not found, insert a hidden contact - which won't be visible on contacts list - will serve for log purposes only
        if(blockedContact == null){
            //non-block list contact - insert hidden contact
            blockedContact = mContactManager.getEmptyContact();

            blockedContact.setId(mContactManager.getArbitraryContactId());
            blockedContact.setPhoneNumber(phoneNumber);
            blockedContact.setDisplayNumber(phoneNumber);
            blockedContact.setIsContactVisible(false); //this will be invisible contact log
            blockedContact.setIsNumberStandardized(true);

            mContactManager.insertContact(blockedContact);
        }
        else{

            int count;

            if(blockType == BlockType.INCOMING){
                count = blockedContact.getIncomingBlockedCount();
                blockedContact.setIncomingBlockedCount(++count);//increment count by 1
            }
            else{ // OUTGOING
                count = blockedContact.getOutgoingBlockedCount();
                blockedContact.setOutgoingBlockedCount(++count);//increment count by 1
            }
            //update existing contact
            mContactManager.updateContact(blockedContact);
        }

        return insertLog(blockedContact.getId(), blockType);
    }

    public boolean deleteLogs(){
       //the line below may return false if there are no hidden contacts (count will be zero and as a result return false)
       mDatabaseHelper.deleteHiddenContacts();

       return mDatabaseHelper.deleteLogs();
    }

}
