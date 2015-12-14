package com.example.bereket.callblocker;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bereket on 10/6/15.
 */
public class LogRecord implements Serializable{

    private int mId;
    private String mContactId;
    private int mBlockType;
    private Date mLogDate;
    private String mContactName;
    private String mContactPhone;

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getContactId() {
        return mContactId;
    }

    public void setContactId(String mContactId) {
        this.mContactId = mContactId;
    }

    public int getBlockType() {
        return mBlockType;
    }

    public void setBlockType(int mBlockType) {
        this.mBlockType = mBlockType;
    }

    public Date getLogDate() {
        return mLogDate;
    }

    public void setLogDate(Date mLogDate) {
        this.mLogDate = mLogDate;
    }

    public String getContactName() {
        return mContactName;
    }

    public void setContactName(String mContactName) {
        this.mContactName = mContactName;
    }

    public String getContactPhone() {
        return mContactPhone;
    }

    public void setContactPhone(String mContactPhone) {
        this.mContactPhone = mContactPhone;
    }

}
