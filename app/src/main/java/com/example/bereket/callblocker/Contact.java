package com.example.bereket.callblocker;

import java.io.Serializable;

/**
 * Created by bereket on 7/21/15.
 */
public class Contact implements Serializable {

    private String mId;
    private String mPhoneNumber;
    private String mContactName;
    private boolean mIsOutGoingBlocked;
    private boolean mIsIncomingBlocked;
    private int mIncomingBlockedCount;
    private int mOutgoingBlockedCount;

    public String getId(){
        return mId;
    }

    public void setId(String id){
        mId = id;
    }

    public String getContactName() {
        return mContactName;
    }

    public void setContactName(String mContactName) {
        this.mContactName = mContactName;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String mPhoneNumber) {
        this.mPhoneNumber = mPhoneNumber;
    }

    public boolean isIsOutGoingBlocked() {
        return mIsOutGoingBlocked;
    }

    public void setIsOutGoingBlocked(boolean mIsOutGoingBlocked) {
        this.mIsOutGoingBlocked = mIsOutGoingBlocked;
    }

    public boolean isIsIncomingBlocked() {
        return mIsIncomingBlocked;
    }

    public void setIsIncomingBlocked(boolean mIsIncomingBlocked) {
        this.mIsIncomingBlocked = mIsIncomingBlocked;
    }

    public int getIncomingBlockedCount() {
        return mIncomingBlockedCount;
    }

    public void setIncomingBlockedCount(int mIncomingBlockedCount) {
        this.mIncomingBlockedCount = mIncomingBlockedCount;
    }

    public int getOutgoingBlockedCount() {
        return mOutgoingBlockedCount;
    }

    public void setOutgoingBlockedCount(int mOutgoingBlockedCount) {
        this.mOutgoingBlockedCount = mOutgoingBlockedCount;
    }
}
