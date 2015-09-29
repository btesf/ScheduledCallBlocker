package com.example.bereket.callblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by bereket on 9/21/15.
 */
public class TimePickerFragment extends DialogFragment {

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

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_time, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(R.string.pick_time);
        builder.setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (getTargetFragment() == null)
                    return;

                Intent intent = new Intent();
                intent.putExtra(SELECTED_TIME, getArguments().getSerializable(SELECTED_TIME));
                intent.putExtra(CHANGE_TARGET, changeTarget);
                getTargetFragment().onActivityResult(PickTimeFragment.TIME_PICKER_DIALOG_REQUEST_CODE, Activity.RESULT_OK, intent);
            }
        });

        builder.setNegativeButton(R.string.button_cancel_button, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getTargetFragment().onActivityResult(PickTimeFragment.TIME_PICKER_DIALOG_REQUEST_CODE, Activity.RESULT_CANCELED, null);
            }
        });

        final TimePicker timePicker = (TimePicker)v.findViewById(R.id.time_picker);

        changeTarget = getArguments().getInt(CHANGE_TARGET);
        Object dateObject = getArguments().getSerializable(CURRENT_TIME);
        mDate = dateObject == null ? new Date() : (Date) dateObject;

        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);

        timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
        timePicker.setIs24HourView(DateFormat.is24HourFormat(getActivity()));

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener(){

            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

               Calendar cal = Calendar.getInstance();
               cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
               cal.set(Calendar.MINUTE, minute);

               mDate = cal.getTime();
               getArguments().putSerializable(SELECTED_TIME, mDate);
            }
        });

        return builder.create();
    }
}
