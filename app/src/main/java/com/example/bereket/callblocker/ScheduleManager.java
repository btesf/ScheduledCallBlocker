package com.example.bereket.callblocker;

import android.content.Context;

import java.util.Date;
import java.util.List;

/**
 * Created by bereket on 9/15/15.
 */
public class ScheduleManager {

    private DataBaseHelper mDataHelper;
    private Context mContext;

    public static String NON_STANDARDIZED_NUMBER_EXIST = "non.standard.number.exists";
    private static ScheduleManager mScheduleManager = null;

    private ScheduleManager(Context context){

        mContext = context;
        mDataHelper = new DataBaseHelper(context);
    }

    public static ScheduleManager getInstance(Context context){

        if(mScheduleManager == null){

            mScheduleManager = new ScheduleManager(context);
        }

        return mScheduleManager;
    }

    public void insertSchedule(String contactId, Date startTime, Date endTime, int blockType, int weekDay){

        mDataHelper.insertSchedule(contactId, startTime, endTime, blockType, weekDay);
    }

    public boolean deleteSchedule(Schedule schedule){

        return mDataHelper.deleteSchedule(schedule);
    }

    public List<Schedule> querySchedule(Schedule schedule){

        //return mDataHelper.querySchedule(schedule);
        return null;
    }

    public boolean updateSchedule(Schedule schedule){

        return mDataHelper.updateSchedule(schedule);
    }

    public boolean timeExistsInSchedule(String contactId, int blockType, int weekDay,Date currentTime){

        return mDataHelper.timeExistsInSchedule(contactId, blockType, weekDay, currentTime.getTime());
    }
}
