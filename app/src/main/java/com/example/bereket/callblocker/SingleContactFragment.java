package com.example.bereket.callblocker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SingleContactFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SingleContactFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SingleContactFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM1 = "param1";
    public static final int PICK_SCHEDULE_TIME_REQUEST_CODE = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Contact mContact;
    private Map<Integer,Schedule> mIncomingSchedule;
    private Map<Integer,Schedule> mOutgoingSchedule;
    //UI Elements
    private CheckBox mOutgoingCheckBox;
    private CheckBox mIncomingCheckbox;
    private TextView mPhoneNumberTextView;
    private TextView mContactNameTextView;

    private TableLayout outgoingScheduleTable;
    private TableLayout incomingScheduleTable;

    private RadioButton dontBlockIncomingRadio, alwaysBlockIncomingRadio, scheduledIncomingRadio;
    private RadioButton dontBlockOutgoingRadio, alwaysBlockOutgoingRadio, scheduledOutgoingRadio;

    //ugly and many controls
    private Button incomingCallMondayButton, incomingCallTuesdayButton, incomingCallWednesdayButton, incomingCallThursdayButton, incomingCallFridayButton, incomingCallSaturdayButton, incomingCallSundayButton;
    private Button outgoingCallMondayButton, outgoingCallTuesdayButton, outgoingCallWednesdayButton, outgoingCallThursdayButton, outgoingCallFridayButton, outgoingCallSaturdayButton, outgoingCallSundayButton;
    private List<Button> incomingCallWeekDayButtons;
    private List<Button> outgoingCallWeekDayButtons;

    //Database property
    private DataBaseHelper dataBaseHelper;

    private OnFragmentInteractionListener mListener;

    {
        incomingCallWeekDayButtons = new ArrayList<>();
        outgoingCallWeekDayButtons = new ArrayList<>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment SingleContactFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SingleContactFragment newInstance(Contact param1) {
        SingleContactFragment fragment = new SingleContactFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public SingleContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Contact contact = (Contact)getArguments().getSerializable(ARG_PARAM1);
            if(contact != null){
                mContact = contact;
            }
        }
        //instantiate DB Helper
        dataBaseHelper = new DataBaseHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_single_contact, container, false);

        mIncomingCheckbox = (CheckBox) view.findViewById(R.id.incoming_call_blocked_checkbox);
        mOutgoingCheckBox = (CheckBox) view.findViewById(R.id.outgoing_call_blocked_checkbox);

        mIncomingCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                   mContact.setIncomingBlockedState(isChecked ? 1 : 0);
            }
        });

        mOutgoingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mContact.setOutGoingBlockedState(isChecked ? 1 : 0);
            }
        });

        mPhoneNumberTextView = (TextView) view.findViewById(R.id.contact_phone_number_id);
        mContactNameTextView = (TextView) view.findViewById(R.id.contact_name_id);

        mPhoneNumberTextView.setText(mContact.getPhoneNumber());
        mContactNameTextView.setText(mContact.getContactName());
        mIncomingCheckbox.setChecked(mContact.getIncomingBlockedState() == 1 ? true : false);
        mOutgoingCheckBox.setChecked(mContact.getOutGoingBlockedState() == 1 ? true : false);


        outgoingScheduleTable = (TableLayout)view.findViewById(R.id.outgoingScheduleTable);
        incomingScheduleTable = (TableLayout) view.findViewById(R.id.incomingScheduleTable);

        alwaysBlockIncomingRadio = (RadioButton) view.findViewById(R.id.alwaysBlockIncomingRadio);
        dontBlockIncomingRadio = (RadioButton) view.findViewById(R.id.dontBlockIncomingRadio);
        scheduledIncomingRadio = (RadioButton) view.findViewById(R.id.scheduledIncomingRadio);
        dontBlockOutgoingRadio = (RadioButton) view.findViewById(R.id.dontBlockOutgoingRadio);
        alwaysBlockOutgoingRadio = (RadioButton) view.findViewById(R.id.alwaysBlockOutgoingRadio);
        scheduledOutgoingRadio = (RadioButton) view.findViewById(R.id.scheduledOutgoingRadio);

        alwaysBlockIncomingRadio.setOnClickListener(new IncomingRadioButtonsClickedListener());
        dontBlockIncomingRadio.setOnClickListener(new IncomingRadioButtonsClickedListener());
        scheduledIncomingRadio.setOnClickListener(new IncomingRadioButtonsClickedListener());
        dontBlockOutgoingRadio.setOnClickListener(new OutgoingRadioButtonsClickedListener());
        alwaysBlockOutgoingRadio.setOnClickListener(new OutgoingRadioButtonsClickedListener());
        scheduledOutgoingRadio.setOnClickListener(new OutgoingRadioButtonsClickedListener());

        incomingCallMondayButton = (Button) view.findViewById(R.id.incomingCallMondayButton);
        incomingCallTuesdayButton = (Button) view.findViewById(R.id.incomingCallTuesdayButton);
        incomingCallWednesdayButton = (Button) view.findViewById(R.id.incomingCallWednesdayButton);
        incomingCallThursdayButton = (Button) view.findViewById(R.id.incomingCallThursdayButton);
        incomingCallFridayButton = (Button) view.findViewById(R.id.incomingCallFridayButton);
        incomingCallSaturdayButton = (Button) view.findViewById(R.id.incomingCallSaturdayButton);
        incomingCallSundayButton = (Button) view.findViewById(R.id.incomingCallSundayButton);
        outgoingCallMondayButton = (Button) view.findViewById(R.id.outgoingCallMondayButton);
        outgoingCallTuesdayButton = (Button) view.findViewById(R.id.outgoingCallTuesdayButton);
        outgoingCallWednesdayButton = (Button) view.findViewById(R.id.outgoingCallWednesdayButton);
        outgoingCallThursdayButton = (Button) view.findViewById(R.id.outgoingCallThursdayButton);
        outgoingCallFridayButton = (Button) view.findViewById(R.id.outgoingCallFridayButton);
        outgoingCallSaturdayButton = (Button) view.findViewById(R.id.outgoingCallSaturdayButton);
        outgoingCallSundayButton = (Button) view.findViewById(R.id.outgoingCallSundayButton);

        //set the view according to the model data
        switch(mContact.getIncomingBlockedState()){
            case BlockState.ALWAYS_BLOCK:
                //check the always block radio button
                //uncheck other radio buttons
                //hide the scheduled view
                alwaysBlockIncomingRadio.setChecked(true);
                dontBlockIncomingRadio.setChecked(false);
                scheduledIncomingRadio.setChecked(false);
                incomingScheduleTable.setVisibility(View.GONE);

                break;
            case BlockState.DONT_BLOCK:
                //check the don't block radio button
                //uncheck other radio buttons
                //hide the scheduled view
                alwaysBlockIncomingRadio.setChecked(false);
                dontBlockIncomingRadio.setChecked(true);
                scheduledIncomingRadio.setChecked(false);
                incomingScheduleTable.setVisibility(View.GONE);

                break;
            case BlockState.SCHEDULED_BLOCK:
                //check the scheduled radio button
                //uncheck other radio buttons
                //show the scheduled view
                //iterate though the scheduled block list for this number and set the button labels accordingly
                alwaysBlockIncomingRadio.setChecked(false);
                dontBlockIncomingRadio.setChecked(false);
                scheduledIncomingRadio.setChecked(true);
                incomingScheduleTable.setVisibility(View.VISIBLE);
                //read incoming block schedule for current contact
                mIncomingSchedule = dataBaseHelper.queryContactSchedule(mContact.getId(), BlockType.INCOMING);
                setButtonLabels(incomingCallWeekDayButtons, mIncomingSchedule);
                break;
            default:
        }

        switch(mContact.getOutGoingBlockedState()){
            case BlockState.ALWAYS_BLOCK:
                //check the always block radio button
                //uncheck other radio buttons
                //hide the scheduled view
                alwaysBlockOutgoingRadio.setChecked(true);
                dontBlockOutgoingRadio.setChecked(false);
                scheduledOutgoingRadio.setChecked(false);
                outgoingScheduleTable.setVisibility(View.GONE);

                break;
            case BlockState.DONT_BLOCK:
                //check the don't block radio button
                //uncheck other radio buttons
                //hide the scheduled view
                alwaysBlockOutgoingRadio.setChecked(false);
                dontBlockOutgoingRadio.setChecked(true);
                scheduledOutgoingRadio.setChecked(false);
                outgoingScheduleTable.setVisibility(View.GONE);
                break;
            case BlockState.SCHEDULED_BLOCK:
                //check the scheduled radio button
                //uncheck other radio buttons
                //show the scheduled view
                //iterate though the scheduled block list for this number and set the button labels accordingly
                alwaysBlockOutgoingRadio.setChecked(false);
                dontBlockOutgoingRadio.setChecked(false);
                scheduledOutgoingRadio.setChecked(true);
                outgoingScheduleTable.setVisibility(View.VISIBLE);
                setButtonLabels(outgoingCallWeekDayButtons, mOutgoingSchedule);

                break;
            default:
        }

        incomingCallWeekDayButtons.add(incomingCallMondayButton);
        incomingCallWeekDayButtons.add(incomingCallTuesdayButton);
        incomingCallWeekDayButtons.add(incomingCallWednesdayButton);
        incomingCallWeekDayButtons.add(incomingCallThursdayButton);
        incomingCallWeekDayButtons.add(incomingCallFridayButton);
        incomingCallWeekDayButtons.add(incomingCallSaturdayButton);
        incomingCallWeekDayButtons.add(incomingCallSundayButton);

        outgoingCallWeekDayButtons.add(outgoingCallMondayButton);
        outgoingCallWeekDayButtons.add(outgoingCallTuesdayButton);
        outgoingCallWeekDayButtons.add(outgoingCallWednesdayButton);
        outgoingCallWeekDayButtons.add(outgoingCallThursdayButton);
        outgoingCallWeekDayButtons.add(outgoingCallFridayButton);
        outgoingCallWeekDayButtons.add(outgoingCallSaturdayButton);
        outgoingCallWeekDayButtons.add(outgoingCallSundayButton);

        //for(Button weekDayButton : incomingCallWeekDayButtons){
        for(int i = 0; i < incomingCallWeekDayButtons.size(); i++){
            Button weekDayButton = incomingCallWeekDayButtons.get(i);
            final int index = i;
            weekDayButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Schedule schedule = mIncomingSchedule.get(index);

                    if(schedule == null){

                        schedule = new Schedule();
                        schedule.setWeekDay(index);
                        schedule.setContactId(mContact.getId());
                        schedule.setBlockType(BlockType.INCOMING);
                    }

                    PickTimeFragment pickTimeFragment = PickTimeFragment.newInstance(schedule, BlockType.INCOMING);
                    pickTimeFragment.setTargetFragment(SingleContactFragment.this,PICK_SCHEDULE_TIME_REQUEST_CODE);
                    pickTimeFragment.show(getFragmentManager(), "bere.bere.bere");
                }
            });
        }

        for(int i = 0; i < outgoingCallWeekDayButtons.size(); i++){
            Button weekDayButton = outgoingCallWeekDayButtons.get(i);
            final int index = i + 1;
            weekDayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Schedule schedule = mOutgoingSchedule.get(index);

                    if(schedule == null){

                        schedule = new Schedule();
                        schedule.setWeekDay(index);
                        schedule.setContactId(mContact.getId());
                        schedule.setBlockType(BlockType.OUTGOING);
                    }

                    PickTimeFragment pickTimeFragment = PickTimeFragment.newInstance(schedule, BlockType.OUTGOING);
                    pickTimeFragment.setTargetFragment(SingleContactFragment.this,PICK_SCHEDULE_TIME_REQUEST_CODE);
                    pickTimeFragment.show(getFragmentManager(), "bere.bere.bere");
                }
            });
        }

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        dataBaseHelper.updateContact(mContact);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private class OutgoingRadioButtonsClickedListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            //is the button checked?
            boolean checked = ((RadioButton) view).isChecked();
            //check which radio button is checked
            switch(view.getId()){
                case R.id.dontBlockOutgoingRadio:
                    if(checked) {
                        mContact.setOutGoingBlockedState(BlockState.DONT_BLOCK);
                        outgoingScheduleTable.setVisibility(View.GONE);
                    }
                    break;
                case R.id.alwaysBlockOutgoingRadio:
                    if(checked){
                        mContact.setOutGoingBlockedState(BlockState.ALWAYS_BLOCK);
                        outgoingScheduleTable.setVisibility(View.GONE);
                    }
                    break;
                case R.id.scheduledOutgoingRadio:

                    if(checked){
                        mContact.setOutGoingBlockedState(BlockState.SCHEDULED_BLOCK);
                        outgoingScheduleTable.setVisibility(View.VISIBLE);
                        //read outgoing block schedule for current contact
                        mOutgoingSchedule = dataBaseHelper.queryContactSchedule(mContact.getId(), BlockType.OUTGOING);
                        setButtonLabels(outgoingCallWeekDayButtons, mOutgoingSchedule);
                    }
                    break;
            }
        }
    }

    private class IncomingRadioButtonsClickedListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            //is the button checked?
            boolean checked = ((RadioButton) view).isChecked();
            //check which radio button is checked
            switch(view.getId()){
                case R.id.dontBlockIncomingRadio:
                    if(checked) {
                        mContact.setIncomingBlockedState(BlockState.DONT_BLOCK);
                        incomingScheduleTable.setVisibility(View.GONE);
                    }
                    break;
                case R.id.alwaysBlockIncomingRadio:
                    if(checked){
                        mContact.setIncomingBlockedState(BlockState.ALWAYS_BLOCK);
                        incomingScheduleTable.setVisibility(View.GONE);
                    }
                    break;
                case R.id.scheduledIncomingRadio:
                    if(checked){

                        mContact.setOutGoingBlockedState(BlockState.SCHEDULED_BLOCK);
                        incomingScheduleTable.setVisibility(View.VISIBLE);
                        //read outgoing block schedule for current contact
                        mIncomingSchedule = dataBaseHelper.queryContactSchedule(mContact.getId(), BlockType.INCOMING);
                        setButtonLabels(incomingCallWeekDayButtons, mIncomingSchedule);
                    }
                    break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode != Activity.RESULT_OK) return;

        switch(requestCode){
            case PICK_SCHEDULE_TIME_REQUEST_CODE:

                if(data != null){

                    Schedule schedule = (Schedule) data.getSerializableExtra(PickTimeFragment.SCHEDULE);
                    //TODO: will schedule always be non-null?
                    Map<Integer, Schedule> scheduleMap = null;
                    List<Button> buttonList = null;

                    if(schedule.getBlockType() == BlockType.INCOMING){

                        scheduleMap = mIncomingSchedule;
                        buttonList = incomingCallWeekDayButtons;
                    }
                    else if(schedule.getBlockType() == BlockType.OUTGOING){

                        scheduleMap = mOutgoingSchedule;
                        buttonList = outgoingCallWeekDayButtons;
                    }
                    //update the list
                    int weekDay = schedule.getWeekDay();
                    scheduleMap.put(weekDay, schedule);
                    //update the button
                    Button weekDayButton = buttonList.get(weekDay);
                    setSingleButtonLabel(weekDayButton, schedule);
                }
                break;
        }

    }

    private void setButtonLabels(List<Button> buttonsList, Map<Integer, Schedule> scheduleMap){

        Button weekDayButton;
        Schedule schedule;
        //set the button labels
        for(int i = 0; i < buttonsList.size(); i++){

            weekDayButton = buttonsList.get(i);
            schedule = scheduleMap.get(i+1);

           setSingleButtonLabel(weekDayButton, schedule);
        }
    }

    private void setSingleButtonLabel(Button button, Schedule schedule){

        if(schedule == null){

            button.setText(R.string.scheduledNotSetButtonLabel);
        }
        else{

            Calendar cal = Calendar.getInstance();
            cal.setTime(schedule.getStartTime());
            String startTime = "";
            startTime += cal.get(Calendar.HOUR);
            startTime += ": " + cal.get(Calendar.MINUTE);

            String endTime = "";
            cal.setTime(schedule.getEndTime());
            endTime+= cal.get(Calendar.HOUR);
            endTime+=": " + cal.get(Calendar.MINUTE);

            button.setText(startTime + " - " + endTime);
        }
    }
}
