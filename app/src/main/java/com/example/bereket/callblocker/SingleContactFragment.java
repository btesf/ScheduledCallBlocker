package com.example.bereket.callblocker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
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

    private int WEEK_DAY_BUTTON_POSITION_IN_LAYOUT = 1;
    private int WEEK_DAY_TEXTVIEW_POSITION_IN_LAYOUT = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Contact mContact;
    private Map<Integer,Schedule> mIncomingSchedule;
    private Map<Integer,Schedule> mOutgoingSchedule;
    private boolean mIsIncomingScheduleChanged = false;
    private boolean mIsOutgoingScheduleChanged = false;
    //UI Elements
    private TextView mPhoneNumberTextView;
    private TextView mContactNameTextView;

    private TableLayout outgoingScheduleTable;
    private TableLayout incomingScheduleTable;

    private RadioButton dontBlockIncomingRadio, alwaysBlockIncomingRadio, scheduledIncomingRadio;
    private RadioButton dontBlockOutgoingRadio, alwaysBlockOutgoingRadio, scheduledOutgoingRadio;

    private List<Button> incomingCallWeekDayButtons;
    private List<Button> outgoingCallWeekDayButtons;

    //Database property
    private DataBaseHelper dataBaseHelper;
    //time format helper class
    TimeHelper mTimeHelper;

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
        mTimeHelper = TimeHelper.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_single_contact, container, false);

        mPhoneNumberTextView = (TextView) view.findViewById(R.id.contact_phone_number_id);
        mContactNameTextView = (TextView) view.findViewById(R.id.contact_name_id);

        mPhoneNumberTextView.setText(mContact.getPhoneNumber());
        mContactNameTextView.setText(mContact.getContactName());

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

        for (int i = 0; i < incomingScheduleTable.getChildCount(); i++) {
            TableRow row = (TableRow)incomingScheduleTable.getChildAt(i);
            Button button = (Button) row.getChildAt(WEEK_DAY_BUTTON_POSITION_IN_LAYOUT);
            TextView textView = (TextView) row.getChildAt(WEEK_DAY_TEXTVIEW_POSITION_IN_LAYOUT);
            //get string resource id from its name and set the value to text view
            textView.setText(getActivity().getResources().getIdentifier("weekDay_"+i, "string", getActivity().getPackageName()));
            incomingCallWeekDayButtons.add(button);
        }

        for (int i = 0; i < outgoingScheduleTable.getChildCount(); i++) {
            TableRow row = (TableRow)outgoingScheduleTable.getChildAt(i);
            Button button = (Button) row.getChildAt(WEEK_DAY_BUTTON_POSITION_IN_LAYOUT);
            TextView textView = (TextView) row.getChildAt(WEEK_DAY_TEXTVIEW_POSITION_IN_LAYOUT);
            //get string resource id from its name and set the value to text view
            textView.setText(getActivity().getResources().getIdentifier("weekDay_"+i, "string", getActivity().getPackageName()));
            outgoingCallWeekDayButtons.add(button);
        }

        //set the view according to the model data
        updateUI(BlockType.INCOMING);
        updateUI(BlockType.OUTGOING);

        return view;
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
                    }
                    break;
                case R.id.alwaysBlockOutgoingRadio:
                    if(checked){
                        mContact.setOutGoingBlockedState(BlockState.ALWAYS_BLOCK);
                    }
                    break;
                case R.id.scheduledOutgoingRadio:

                    if(checked){
                        mContact.setOutGoingBlockedState(BlockState.SCHEDULED_BLOCK);
                    }
                    break;
            }
            //update the UI
            updateUI(BlockType.OUTGOING);
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
                    }
                    break;
                case R.id.alwaysBlockIncomingRadio:
                    if(checked){
                        mContact.setIncomingBlockedState(BlockState.ALWAYS_BLOCK);
                    }
                    break;
                case R.id.scheduledIncomingRadio:
                    if(checked){
                        mContact.setIncomingBlockedState(BlockState.SCHEDULED_BLOCK);
                    }
                    break;
            }
            //update the UI
            updateUI(BlockType.INCOMING);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode != Activity.RESULT_OK) return;

        switch(requestCode){
            case PICK_SCHEDULE_TIME_REQUEST_CODE:

                if(data != null){

                    Schedule schedule = (Schedule) data.getSerializableExtra(PickTimeFragment.SCHEDULE);
                    //if only one of start or end times is set do nothing - it is an incomplete time setting
                    if((schedule.getEndTime() == null && schedule.getStartTime() != null) ||
                            (schedule.getEndTime() != null && schedule.getStartTime() == null)){

                        return;
                    }

                    Map<Integer, Schedule> scheduleMap = null;
                    List<Button> buttonList = null;
                    int weekDay = schedule.getWeekDay();
                    boolean mapChangedFlag = false;

                    if(schedule.getBlockType() == BlockType.INCOMING){

                        scheduleMap = mIncomingSchedule;
                        buttonList = incomingCallWeekDayButtons;
                    }
                    else if(schedule.getBlockType() == BlockType.OUTGOING){

                        scheduleMap = mOutgoingSchedule;
                        buttonList = outgoingCallWeekDayButtons;
                    }

                    Button weekDayButton = buttonList.get(weekDay);
                    Schedule existingSchedule = scheduleMap.get(weekDay);

                    //if both start and end times are set to null, then the scheduled is canceled - check and remove from scheduleMap
                    if(schedule.getEndTime() == null && schedule.getStartTime() == null){
                        //if an element exists with current date, remove from map - else do nothing
                        if(existingSchedule != null){
                            scheduleMap.remove(weekDay);
                            setSingleButtonLabel(weekDayButton, null);
                            mapChangedFlag = true;
                        }
                    }
                    //check if this is a new schedule or the updated schedule start and/or end time is different from original
                    else if(existingSchedule == null ||
                            (existingSchedule.getStartTime().compareTo(schedule.getStartTime()) != 0 ||
                                existingSchedule.getEndTime().compareTo(schedule.getEndTime()) != 0)){
                        //update the list
                        scheduleMap.put(weekDay, schedule);
                        //update the button label
                        setSingleButtonLabel(weekDayButton, schedule);
                        //set mapChangedFlag to refleced changed
                        mapChangedFlag = true;
                    }

                    //update corresponding map content changed flag
                    if(mapChangedFlag){

                        if(schedule.getBlockType() == BlockType.INCOMING){
                            mIsIncomingScheduleChanged = true;
                        }
                        else{
                            mIsOutgoingScheduleChanged = true;
                        }
                    }
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
            schedule = scheduleMap.get(i);

            setSingleButtonLabel(weekDayButton, schedule);
        }
    }

    private void setSingleButtonLabel(Button button, Schedule schedule){

        if(schedule == null){

            button.setText(R.string.scheduledNotSetButtonLabel);
        }
        else{

            String startTime = mTimeHelper.getTimeWithSystemTimeFormat(schedule.getStartTime());
            String endTime = mTimeHelper.getTimeWithSystemTimeFormat(schedule.getEndTime());
            button.setText(startTime + " - " + endTime);
        }
    }

    /*
        generic type of ui controls updater. Based on the block type(incoming/outgoing)
        it changes the corresponding UI controls
     */
    private void updateUI(int blockType){

        int blockedState = (blockType == BlockType.INCOMING) ? mContact.getIncomingBlockedState() : mContact.getOutGoingBlockedState();
        RadioButton alwaysBlockRadio = (blockType == BlockType.INCOMING) ? alwaysBlockIncomingRadio : alwaysBlockOutgoingRadio;
        RadioButton dontBlockRadio = (blockType == BlockType.INCOMING) ? dontBlockIncomingRadio : dontBlockOutgoingRadio;
        RadioButton scheduledBlockRadio = (blockType == BlockType.INCOMING) ? scheduledIncomingRadio : scheduledOutgoingRadio;
        TableLayout scheduleTableLayout = (blockType == BlockType.INCOMING) ? incomingScheduleTable : outgoingScheduleTable;
        Map<Integer,Schedule> scheduleMap = (blockType == BlockType.INCOMING) ? mIncomingSchedule : mOutgoingSchedule;
        List<Button> buttonList = (blockType == BlockType.INCOMING) ? incomingCallWeekDayButtons : outgoingCallWeekDayButtons;

        switch(blockedState){
            case BlockState.ALWAYS_BLOCK:
                //check the always block radio button
                //hide the scheduled view
                alwaysBlockRadio.setChecked(true);
                scheduleTableLayout.setVisibility(View.GONE);

                break;
            case BlockState.DONT_BLOCK:
                //check the don't block radio button
                //hide the scheduled view
                dontBlockRadio.setChecked(true);
                scheduleTableLayout.setVisibility(View.GONE);
                break;
            case BlockState.SCHEDULED_BLOCK:
                //check the scheduled radio button
                //show the scheduled view
                scheduledBlockRadio.setChecked(true);
                scheduleTableLayout.setVisibility(View.VISIBLE);
                //this method's purpose is to change view. But below, DB query is included to reduce the number of places
                //query is done. In addition whenever the query was made this method would also be called anyways.
                if(scheduleMap == null) {

                    scheduleMap = dataBaseHelper.queryContactSchedule(mContact.getId(), blockType);
                    //assign the queried map to the corresponding(incoming/outoing) map
                    if(blockType == BlockType.INCOMING){
                        mIncomingSchedule = scheduleMap;
                    }
                    else mOutgoingSchedule = scheduleMap;
                }
                setButtonLabels(buttonList, scheduleMap);
                attachClickListenersToWeekDayButtons(blockType);
                break;
            default:
        }
    }

    private void attachClickListenersToWeekDayButtons(final int blockType){

        final Map<Integer,Schedule> scheduleMap = (blockType == BlockType.INCOMING) ? mIncomingSchedule : mOutgoingSchedule;
        List<Button> buttonList = (blockType == BlockType.INCOMING) ? incomingCallWeekDayButtons : outgoingCallWeekDayButtons;

        for(int i = 0; i < buttonList.size(); i++){
            Button weekDayButton = buttonList.get(i);
            final int index = i;
            weekDayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Schedule existingSchedule = scheduleMap.get(index);
                    Schedule schedule = new Schedule();;

                    if(existingSchedule == null){

                        schedule.setWeekDay(index);
                        schedule.setContactId(mContact.getId());
                        schedule.setBlockType(blockType);
                    }
                    else{
                        //copy values to new schedule. Otherwise the schedule in the map will be passes by reference and it will be modified directly
                        schedule.copySchedule(existingSchedule);
                    }
                    PickTimeFragment pickTimeFragment = PickTimeFragment.newInstance(schedule, blockType);
                    pickTimeFragment.setTargetFragment(SingleContactFragment.this,PICK_SCHEDULE_TIME_REQUEST_CODE);
                    pickTimeFragment.show(getFragmentManager(), "bere.bere.bere");
                }
            });
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        dataBaseHelper.updateContact(mContact);
        if(mIsIncomingScheduleChanged) dataBaseHelper.updateSchedules(mIncomingSchedule);
        if(mIsOutgoingScheduleChanged)  dataBaseHelper.updateSchedules(mOutgoingSchedule);
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
}
