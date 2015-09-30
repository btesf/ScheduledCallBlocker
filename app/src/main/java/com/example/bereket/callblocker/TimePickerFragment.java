package com.example.bereket.callblocker;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by bereket on 9/30/15.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    private Date mDate;
    private int changeTarget;

    public static final String SELECTED_TIME = "selected.time";
    public static final String CURRENT_TIME = "current.time";
    public static final String CHANGE_TARGET = "change.target";

    public static final int START_TIME = 1;
    public static final int END_TIME = 2;

    public static TimePickerFragment newInstance(Date date, int changeTarget){

        Bundle args = new Bundle();
        args.putSerializable(CURRENT_TIME, date);
        args.putInt(CHANGE_TARGET, changeTarget);
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setArguments(args);

        return timePickerFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        changeTarget = getArguments().getInt(CHANGE_TARGET);
        Object dateObject = getArguments().getSerializable(CURRENT_TIME);
        mDate = dateObject == null ? new Date() : (Date) dateObject;

        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);
        //Create and return a new instance of TimePickerDialog
        return new TimePickerDialog(getActivity(),this, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(getActivity()));
    }

    //onTimeSet() callback method
    public void onTimeSet(TimePicker view, int hourOfDay, int minute){

        Calendar cal = TimeHelper.getBenchmarkCalendar();
        //if time is end time, set the second to 59 (last second in a minute) - because start minute could be the same as end minute - and it is valid
        //the only way to differentiate them is by the second value
        if(changeTarget == START_TIME){
            cal.set(Calendar.SECOND, 0);
        }
        else{ //end time
            cal.set(Calendar.SECOND, 59);
        }

        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);

        mDate = cal.getTime();

        if (getTargetFragment() == null)
            return;

        Intent intent = new Intent();
        intent.putExtra(SELECTED_TIME, mDate);
        intent.putExtra(CHANGE_TARGET, changeTarget);
        getTargetFragment().onActivityResult(PickTimeFragment.TIME_PICKER_DIALOG_REQUEST_CODE, Activity.RESULT_OK, intent);
    }
}