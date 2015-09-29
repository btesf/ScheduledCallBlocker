package com.example.bereket.callblocker;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bereket on 9/16/15.
 */
public class Schedule implements Serializable{

    private int mId;
    private String mContactId;
    private int mWeekDay;
    private Date mStartTime;
    private Date mEndTime;

    private int mBlockType;

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

    public int getWeekDay() {
        return mWeekDay;
    }

    public void setWeekDay(int mWeekDay) {
        this.mWeekDay = mWeekDay;
    }

    public Date getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Date endTime) {
        this.mEndTime = endTime;
    }

    public Date getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Date startTime) {
        this.mStartTime = startTime;
    }

    public int getBlockType() {
        return mBlockType;
    }

    public void setBlockType(int mBlockType) {
        this.mBlockType = mBlockType;
    }

    public void copySchedule(Schedule schedule){

        mId = schedule.getId();
        mContactId = schedule.getContactId();
        mWeekDay = schedule.getWeekDay();
        mStartTime = schedule.getStartTime();
        mEndTime = schedule.getEndTime();
        mBlockType = schedule.getBlockType();
    }
}
