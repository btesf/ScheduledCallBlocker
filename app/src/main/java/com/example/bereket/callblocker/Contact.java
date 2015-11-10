package com.example.bereket.callblocker;

import java.io.Serializable;

/**
 * Created by bereket on 7/21/15.
 */
public class Contact implements Serializable {

    private long mId;
    private String mPhoneNumber;
    private String mDisplayNumber;
    private String mContactName;
    private boolean mIsNumberStandardized; //this decides if the number should be standardized before used in any telephone activity
    private int mOutGoingBlockedState;
    private int mIncomingBlockedState;
    private int mIncomingBlockedCount;
    private int mOutgoingBlockedCount;

    private boolean mContactVisible; //used for new contacts to be logged - those which the user doesn't included them explicitly.

    public long getId(){
        return mId;
    }

    public void setId(long id){
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

    public String getDisplayNumber() {
        return mDisplayNumber;
    }

    public void setDisplayNumber(String mDisplayNumber) {
        this.mDisplayNumber = mDisplayNumber;
    }

    public int getOutGoingBlockedState() {
        return mOutGoingBlockedState;
    }

    public void setOutGoingBlockedState(int mOutGoingBlockedState) {
        this.mOutGoingBlockedState = mOutGoingBlockedState;
    }

    public int getIncomingBlockedState() {
        return mIncomingBlockedState;
    }

    public void setIncomingBlockedState(int mIncomingBlockedState) {
        this.mIncomingBlockedState = mIncomingBlockedState;
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

    public boolean isIsNumberStandardized() {
        return mIsNumberStandardized;
    }

    public void setIsNumberStandardized(boolean mIsNumberStandardized) {
        this.mIsNumberStandardized = mIsNumberStandardized;
    }

    public boolean isContactVisible() {
        return mContactVisible;
    }

    public void setIsContactVisible(boolean mContactVisible) {
        this.mContactVisible = mContactVisible;
    }
}
