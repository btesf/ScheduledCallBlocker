package com.example.bereket.callblocker;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
public class SingleContactFragment extends HideNotificationFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_CONTACT_FROM_PHONEBOOK = "param2";
    public static final int PICK_SCHEDULE_TIME_REQUEST_CODE = 0;

    private int WEEK_DAY_BUTTON_POSITION_IN_LAYOUT = 1;
    private int WEEK_DAY_TEXTVIEW_POSITION_IN_LAYOUT = 0;
    private int CLEAR_ALL_SCHEDULES_BUTTON_POSITION_IN_LAYOUT = 0;

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
    private ScheduleManager mScheduleManager;
    private ContactManager mContactManager;
    //time format helper class
    TimeHelper mTimeHelper;

   // private boolean isBroadcastReceived

    private OnFragmentInteractionListener mListener;

    {
        incomingCallWeekDayButtons = new ArrayList<>();
        outgoingCallWeekDayButtons = new ArrayList<>();
    }

    /*
    the purpose of this broadcast receiver is, if somehow, the number is added manually and the search(sync) service (that looks the number in the phone's contact)
    is not complete before this fragment is loaded, still the contact won't have proper full name and contact id. The synch service will send a broadcast message once it is done.
    When that message is received, update mContact and views with fresh details from blocked list
     */
    private BroadcastReceiver mOnUpdateContactFromPhoneBook = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //find contact from contact phone number and update it
            if(mContact != null){

                if(mContactManager != null){

                    mContact = mContactManager.getContactByPhoneNumber(mContact.getPhoneNumber());
                    //set contact name text
                    if(mContactNameTextView != null){

                        mContactNameTextView.setText(mContact.getContactName());
                    }
                }
            }
        }
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment SingleContactFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SingleContactFragment newInstance(Contact param1, boolean isContactFromPhoneBook) {
        SingleContactFragment fragment = new SingleContactFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        args.putSerializable(ARG_CONTACT_FROM_PHONEBOOK, isContactFromPhoneBook);
        fragment.setArguments(args);
        return fragment;
    }

    public SingleContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //instantiate DB Helper
        mTimeHelper = TimeHelper.getInstance(getActivity());
        mScheduleManager = ScheduleManager.getInstance(getActivity());
        mContactManager = ContactManager.getInstance(getActivity());

        if (getArguments() != null) {

            boolean isContactFromPhonebook = (boolean)getArguments().getBoolean(ARG_CONTACT_FROM_PHONEBOOK);
            Contact contact = (Contact)getArguments().getSerializable(ARG_PARAM1);

            if(contact != null){
                //check if the ,contact comes from phone book. If so, don't need to re-query - we have all the necessary information (complete fname, last name...)
                //otherwise, re-query to get the actual name - this happens when manually added number details are updated after this fragment is called and contact details are now different
                if(isContactFromPhonebook){

                    mContact = contact;
                }
                else{
                    //if contact is manually added, requery to get an updated contact details if sync service (SaveFromPhoneBookService) change/updated the block list with fresh detail from phonebook
                    mContact = mContactManager.getContactByPhoneNumber(contact.getPhoneNumber());
                }
            }
        }

        setHasOptionsMenu(true);
        //enable the 'UP' ancestoral navigation button, if parent is set in manifest for this activity
        if (NavUtils.getParentActivityName(getActivity()) != null) {

            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {

                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_single_contact, container, false);

        mContactNameTextView = (TextView) view.findViewById(R.id.contact_name_id);
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

        int incomingScheduleTableChildCount = incomingScheduleTable.getChildCount();
        int outgoingScheduleTableChildCount = outgoingScheduleTable.getChildCount();

        for (int i = 0; i < incomingScheduleTableChildCount - 1; i++) { //the last row is 'clear all' button
            TableRow row = (TableRow)incomingScheduleTable.getChildAt(i);
            Button button = (Button) row.getChildAt(WEEK_DAY_BUTTON_POSITION_IN_LAYOUT);
            TextView textView = (TextView) row.getChildAt(WEEK_DAY_TEXTVIEW_POSITION_IN_LAYOUT);
            //get string resource id from its name and set the value to text view
            textView.setText(getActivity().getResources().getIdentifier("weekDay_"+i, "string", getActivity().getPackageName()));
            incomingCallWeekDayButtons.add(button);
        }
        //last incoming schedule table row - clear all schedule button
        Button clearAllIncomingSchedule = (Button) ((TableRow)incomingScheduleTable.getChildAt(incomingScheduleTableChildCount -1)).getChildAt(CLEAR_ALL_SCHEDULES_BUTTON_POSITION_IN_LAYOUT);
        clearAllIncomingSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mScheduleManager.deleteAllSchedulesForContact(mContact.getId(), BlockType.INCOMING);
                mIncomingSchedule.clear();
                updateUI(BlockType.INCOMING);
            }
        });

        for (int i = 0; i < outgoingScheduleTableChildCount - 1; i++) { //the last row is 'clear all' button
            TableRow row = (TableRow)outgoingScheduleTable.getChildAt(i);
            Button button = (Button) row.getChildAt(WEEK_DAY_BUTTON_POSITION_IN_LAYOUT);
            TextView textView = (TextView) row.getChildAt(WEEK_DAY_TEXTVIEW_POSITION_IN_LAYOUT);
            //get string resource id from its name and set the value to text view
            textView.setText(getActivity().getResources().getIdentifier("weekDay_"+i, "string", getActivity().getPackageName()));
            outgoingCallWeekDayButtons.add(button);
        }
        //last outgoing schedule table row - clear schedule button
        Button clearAllOutgoingSchedule = (Button) ((TableRow)outgoingScheduleTable.getChildAt(outgoingScheduleTableChildCount -1)).getChildAt(CLEAR_ALL_SCHEDULES_BUTTON_POSITION_IN_LAYOUT);
        clearAllOutgoingSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mScheduleManager.deleteAllSchedulesForContact(mContact.getId(), BlockType.OUTGOING);
                mOutgoingSchedule.clear();
                updateUI(BlockType.OUTGOING);
            }
        });

        //set the view according to the model data
        updateUI(BlockType.INCOMING);
        updateUI(BlockType.OUTGOING);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {

                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

            mContactManager.updateContact(mContact);
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

            mContactManager.updateContact(mContact);
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
                            mScheduleManager.deleteSchedule(existingSchedule);
                        }
                    }
                    //check if this is a new schedule
                    else if(existingSchedule == null){
                        //update the list
                        scheduleMap.put(weekDay, schedule);
                        //update the button label
                        setSingleButtonLabel(weekDayButton, schedule);
                        //set mapChangedFlag to refleced changed
                        mScheduleManager.insertSchedule(schedule);
                    }
                    else if ((existingSchedule.getStartTime().compareTo(schedule.getStartTime()) != 0 ||
                                existingSchedule.getEndTime().compareTo(schedule.getEndTime()) != 0)){ //else update schedule start and/or end time is different from original
                        //update the list
                        scheduleMap.put(weekDay, schedule);
                        //update the button label
                        setSingleButtonLabel(weekDayButton, schedule);
                       mScheduleManager.updateSchedule(schedule);
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
            button.setText(startTime + " – " + endTime);
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

                    scheduleMap = mContactManager.queryContactSchedule(mContact.getId(), blockType);
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

        //unregister the broadcast listener that is released from SaveFromPhoneBookService
        getActivity().unregisterReceiver(mOnUpdateContactFromPhoneBook);

        Map<Integer, Schedule> scheduleMap = null;

        //if scheduled block is set but no schedule is set for that block type, change block type to 'don't block'
        if(mContact.getIncomingBlockedState() == BlockState.SCHEDULED_BLOCK){

            scheduleMap = mContactManager.queryContactSchedule(mContact.getId(), BlockType.INCOMING);
            if(scheduleMap.keySet().size() == 0){

                mContact.setIncomingBlockedState(BlockState.DONT_BLOCK);
                mContactManager.updateContact(mContact);
            }
        }

        if(mContact.getOutGoingBlockedState() == BlockState.SCHEDULED_BLOCK){

            scheduleMap = mContactManager.queryContactSchedule(mContact.getId(), BlockType.OUTGOING);
            if(scheduleMap.keySet().size() == 0){

                mContact.setOutGoingBlockedState(BlockState.DONT_BLOCK);
                mContactManager.updateContact(mContact);
            }
        }
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

    @Override
    public void onResume(){
        super.onResume();

        //register a receiver that gets notified when a blocked list contat is updated from phonebook
        IntentFilter filter = new IntentFilter(Constants.ACTION_REFRESH_BLOCKED_LIST_UI);
        //only receive broadcasts which are sent through the valid private permission - we don't want to receive a broadcast just matching an intent - we want the permission too
        getActivity().registerReceiver(mOnUpdateContactFromPhoneBook, filter, Constants.PRIVATE_PERMISSION, null);
    }

    @Override
    public void doOnBroadcastReceived() {

        Utility.showCallInterceptionAlertDialog(getActivity());
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
