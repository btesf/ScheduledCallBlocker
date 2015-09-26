package com.example.bereket.callblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by bereket on 9/21/15.
 */
public class PickTimeFragment extends DialogFragment {

    public static String SCHEDULE = "com.example.bereket.callblocker.schedule";
    public static String BLOCK_TYPE = "com.example.bereket.callblocker.blockType";
    public static final int TIME_PICKER_DIALOG_REQUEST_CODE = 0;


    private Schedule mSchedule;

    //private Button startTimeButton;
    //private Button endTimeButton;
    private TextView startTimeTextView;
    private TextView endTimeTextView;
    private Button clearTimeButton;

    public static PickTimeFragment newInstance(Schedule schedule, int blockType) {
        PickTimeFragment fragment = new PickTimeFragment();
        Bundle args = new Bundle();
        args.putSerializable(SCHEDULE, schedule);
        args.putInt(BLOCK_TYPE, blockType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Schedule schedule = (Schedule)getArguments().getSerializable(SCHEDULE);
            if(schedule != null){
                mSchedule = schedule;
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        // Inflate the layout for this fragment
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_pick_time, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(R.string.pick_time);
        builder.setPositiveButton(R.string.button_save_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (getTargetFragment() == null)
                    return;

                Intent intent = new Intent();
                intent.putExtra(SCHEDULE, mSchedule);
                getTargetFragment().onActivityResult(SingleContactFragment.PICK_SCHEDULE_TIME_REQUEST_CODE, Activity.RESULT_OK, intent);
            }
        });

        builder.setNegativeButton(R.string.button_cancel_button, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getTargetFragment().onActivityResult(SingleContactFragment.PICK_SCHEDULE_TIME_REQUEST_CODE, Activity.RESULT_CANCELED, null);
            }
        });
       // builder.setNegativeButton(R.string.button_cancel_button, null);

        //startTimeButton = (Button) v.findViewById(R.id.start_time_button);
        //endTimeButton = (Button) v.findViewById(R.id.end_time_button);
        startTimeTextView = (TextView) v.findViewById(R.id.start_time_label);
        endTimeTextView = (TextView) v.findViewById(R.id.end_time_label);
        clearTimeButton = (Button) v.findViewById(R.id.clear_time_button);

        startTimeTextView.setText(getFormattedTime(mSchedule.getStartTime()));
        endTimeTextView.setText(getFormattedTime(mSchedule.getEndTime()));

/*        startTimeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(mSchedule.getStartTime(), TimePickerFragment.START_TIME);
                timePickerFragment.setTargetFragment(PickTimeFragment.this,TIME_PICKER_DIALOG_REQUEST_CODE);
                timePickerFragment.show(getFragmentManager(), "bere.bere.bere");
            }
        });*/

        startTimeTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(mSchedule.getStartTime(), TimePickerFragment.START_TIME);
                timePickerFragment.setTargetFragment(PickTimeFragment.this,TIME_PICKER_DIALOG_REQUEST_CODE);
                timePickerFragment.show(getFragmentManager(), "bere.bere.bere");
            }
        });

/*        endTimeButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(mSchedule.getEndTime(), TimePickerFragment.END_TIME);
                timePickerFragment.setTargetFragment(PickTimeFragment.this,TIME_PICKER_DIALOG_REQUEST_CODE);
                timePickerFragment.show(getFragmentManager(), "bere.bere.bere");
            }
        });*/

        endTimeTextView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(mSchedule.getEndTime(), TimePickerFragment.END_TIME);
                timePickerFragment.setTargetFragment(PickTimeFragment.this,TIME_PICKER_DIALOG_REQUEST_CODE);
                timePickerFragment.show(getFragmentManager(), "bere.bere.bere");
            }
        });

        clearTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return builder.create();
    }

    private String getFormattedTime(Date date){

        String formattedTime = "";

        if(date != null){

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            formattedTime += cal.get(Calendar.HOUR_OF_DAY) + " : " + cal.get(Calendar.MINUTE);
        }
        else{

            formattedTime += " -- : -- ";
        }

        return formattedTime;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode != Activity.RESULT_OK) return;

        switch(requestCode){

            case TIME_PICKER_DIALOG_REQUEST_CODE:

                if(data != null){

                    Date returnedTime = (Date) data.getSerializableExtra(TimePickerFragment.SELECTED_TIME);
                    int changeTarget = data.getIntExtra(TimePickerFragment.CHANGE_TARGET, 0);

                    //block type should be either 1 or 2. Otherwise (if default value is assigned) it is an error
                    if(changeTarget == 0 ){

                        Toast.makeText(getActivity(), R.string.block_type_data_missing, Toast.LENGTH_SHORT);
                        return;
                    }

                    String formattedTime = "";
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(returnedTime);

                    //validate date - check if end time is not earlier than start time
                    if(changeTarget == TimePickerFragment.START_TIME){

                        if(mSchedule.getEndTime() != null && returnedTime.compareTo(mSchedule.getEndTime()) > 1){ //start time is older than end time
                            Toast.makeText(getActivity(), R.string.start_time_is_older_than_end_time, Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                    }
                    else if(changeTarget == TimePickerFragment.END_TIME){

                        if(mSchedule.getStartTime() != null && returnedTime.compareTo(mSchedule.getStartTime()) < 1){ //end time is earlier than start time
                            Toast.makeText(getActivity(), R.string.end_time_is_earlier_than_end_time, Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                    }


                    if(changeTarget == TimePickerFragment.START_TIME){

                        mSchedule.setStartTime(returnedTime);
                        startTimeTextView.setText(cal.get(Calendar.HOUR_OF_DAY) + " : " + cal.get(Calendar.MINUTE));
                    }
                    else if(changeTarget == TimePickerFragment.END_TIME){

                        mSchedule.setEndTime(returnedTime);
                        endTimeTextView.setText(cal.get(Calendar.HOUR_OF_DAY) + " : " + cal.get(Calendar.MINUTE));
                    }
                }

                break;
        }
    }
}
