package com.example.bereket.callblocker;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bereket on 9/29/15.
 *
 * Singleton class
 */
public class TimeHelper {

    private static TimeHelper mTimeHelper;
    private Context mContext;
    private SimpleDateFormat amPmDateFormat = new SimpleDateFormat("hh:mm a");
    private SimpleDateFormat _24HourFormat = new SimpleDateFormat("HH:mm");

    private TimeHelper(Context context){

        this.mContext = context;
    }

    public static TimeHelper getInstance(Context context){

        if(mTimeHelper == null){

            mTimeHelper = new TimeHelper(context);
        }

        return mTimeHelper;
    }

    /*
    returns time in the form of 24/12 hour format based on the system settings
     */
    public String getTimeWithSystemTimeFormat(Date date){

        if(date == null){

            return " – – : – –  ";
        }

        boolean is24HourFormat = DateFormat.is24HourFormat(mContext);

        if(is24HourFormat){

            return _24HourFormat.format(date);
        }
        else{//am/pm format

            return amPmDateFormat.format(date);
        }
    }

    //this method compares the time part of a date. Used the underlying binary(long int) value of date to make it faster
    //another option is using SimpleDateFormatter to mask the date part and compare the formatted dates - but that will be slow
    //returns true 0 if date1 = date2, +ve value if date1 is older than date 2, or returns -ve value if date2 is older than date1
    public int compareTimes(Date date1, Date date2){

        int t1 = (int) date1.getTime();
        int t2 = (int) date2.getTime();

        return t1 - t2;
    }

    /*
      convert java calendar day of week value with the one used in this application.
      In this application Monday = 0, Tuesday = 1, ... Sunday = 6
      Java Calendar: Sunday = 1, Monday = 2, ... Saturday = 7
     */
    public static int convertJavaDayOfWeekWithCallBlockerType(int javaCalendarDayOfWeek){

        if(javaCalendarDayOfWeek == Calendar.SUNDAY) return 6;
        else return javaCalendarDayOfWeek - 2 ;
    }

    public static Calendar getBenchmarkCalendar(){
        Calendar cal = Calendar.getInstance();
        //Always set a constant date to be stored in DB - jan 1, 2000 xx:xx:000;
        cal.set(Calendar.YEAR, 2000);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    public static Calendar setCalendarToBenchmarkTime(Calendar cal){
        //Always set a constant date to be stored in DB - jan 1, 2000 xx:xx:000;
        cal.set(Calendar.YEAR, 2000);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    public String formattedLogDate(Date date){

        String formattedDate = "";
        String dateFormatSegment = "";

        if(DateUtils.isToday(date.getTime())){

            formattedDate += "Today";
        }
        else {
            //to check yesterday's date : add one day from the date given and use isToay()
            Calendar c1 = Calendar.getInstance();
            c1.setTime(date);
            c1.add(Calendar.DAY_OF_YEAR, +1);

            if(DateUtils.isToday(c1.getTime().getTime())){

                formattedDate += "Yesteday";
            }
            else{

                //segment -- Monday, January 30, 2016
                dateFormatSegment += "EEEE, MMMM d, yyyy";
            }
        }

        boolean is24HourFormat = DateFormat.is24HourFormat(mContext);

        if(is24HourFormat){
            //segment -- 24 hour format hour:minute
            dateFormatSegment += " k:mm";
        }
        else{//am/pm format
            //segment -- am/pm time format
            dateFormatSegment += " h:mm a";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatSegment);

        return formattedDate + sdf.format(date);
    }
}
