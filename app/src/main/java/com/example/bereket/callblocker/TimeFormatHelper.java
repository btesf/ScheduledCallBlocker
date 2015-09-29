package com.example.bereket.callblocker;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by bereket on 9/29/15.
 *
 * Singleton class
 */
public class TimeFormatHelper {

    private static TimeFormatHelper mTimeFormatHelper;
    private Context mContext;

    private TimeFormatHelper(Context context){

        this.mContext = context;
    }

    public static TimeFormatHelper getInstance(Context context){

        if(mTimeFormatHelper == null){

            mTimeFormatHelper = new TimeFormatHelper(context);
        }

        return mTimeFormatHelper;
    }

    /*
    returns time in the form of 24/12 hour format based on the system settings
     */
    public String getTimeWithSystemTimeFormat(Date date){

        String time = "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        boolean is24HourFormat = DateFormat.is24HourFormat(mContext);

        time += is24HourFormat ? cal.get(Calendar.HOUR_OF_DAY) : cal.get(Calendar.HOUR);
        time += ": " + cal.get(Calendar.MINUTE);

        if(!is24HourFormat){
            time += cal.get(Calendar.AM_PM) ==  Calendar.PM ? " PM" : " AM";
        }

        return time;
    }


}
