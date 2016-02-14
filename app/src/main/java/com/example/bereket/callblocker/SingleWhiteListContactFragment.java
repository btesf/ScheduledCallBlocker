package com.example.bereket.callblocker;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SingleWhiteListContactFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SingleWhiteListContactFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SingleWhiteListContactFragment extends HideNotificationFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_CONTACT_FROM_PHONEBOOK = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Contact mContact;
    //UI Elements
    private TextView mPhoneNumberTextView;
    private TextView mContactNameTextView;

    private RadioButton clearWhitelistIncomingRadio, addToWhitelistIncomingRadio;
    private RadioButton clearWhitelistOutgoingRadio, addToWhitelistOutgoingRadio;

    //Database property
    private ContactManager mContactManager;

   // private boolean isBroadcastReceived

    private OnFragmentInteractionListener mListener;

    {   }

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
    public static SingleWhiteListContactFragment newInstance(Contact param1, boolean isContactFromPhoneBook) {
        SingleWhiteListContactFragment fragment = new SingleWhiteListContactFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        args.putSerializable(ARG_CONTACT_FROM_PHONEBOOK, isContactFromPhoneBook);
        fragment.setArguments(args);
        return fragment;
    }

    public SingleWhiteListContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //instantiate DB Helper
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
        View view =  inflater.inflate(R.layout.fragment_whitelist_single_contact, container, false);

        mContactNameTextView = (TextView) view.findViewById(R.id.contact_name_id);
        mContactNameTextView.setText(mContact.getContactName());

        addToWhitelistIncomingRadio = (RadioButton) view.findViewById(R.id.addToWhitelistIncomingRadio);
        clearWhitelistIncomingRadio = (RadioButton) view.findViewById(R.id.removeFromWhitelistIncomingRadio);
        addToWhitelistOutgoingRadio = (RadioButton) view.findViewById(R.id.addToWhitelistOutgoingRadio);
        clearWhitelistOutgoingRadio = (RadioButton) view.findViewById(R.id.removeFromWhitelistOutgoingRadio);

        addToWhitelistIncomingRadio.setOnClickListener(new IncomingRadioButtonsClickedListener());
        clearWhitelistIncomingRadio.setOnClickListener(new IncomingRadioButtonsClickedListener());
        clearWhitelistOutgoingRadio.setOnClickListener(new OutgoingRadioButtonsClickedListener());
        addToWhitelistOutgoingRadio.setOnClickListener(new OutgoingRadioButtonsClickedListener());
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
                case R.id.addToWhitelistOutgoingRadio:
                    if(checked) {
                        mContact.setOutGoingBlockedState(BlockState.WHITE_LIST);
                    }
                    break;
                case R.id.removeFromWhitelistOutgoingRadio:
                    if(checked){
                        mContact.setOutGoingBlockedState(BlockState.DONT_BLOCK);
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
                case R.id.addToWhitelistIncomingRadio:
                    if(checked) {
                        mContact.setIncomingBlockedState(BlockState.WHITE_LIST);
                    }
                    break;
                case R.id.removeFromWhitelistIncomingRadio:
                    if(checked){
                        mContact.setIncomingBlockedState(BlockState.DONT_BLOCK);
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
    }

    /*
        generic type of ui controls updater. Based on the block type(incoming/outgoing)
        it changes the corresponding UI controls
     */
    private void updateUI(int blockType){

        int blockedState = (blockType == BlockType.INCOMING) ? mContact.getIncomingBlockedState() : mContact.getOutGoingBlockedState();
        RadioButton addTowhiteListRadio = (blockType == BlockType.INCOMING) ? addToWhitelistIncomingRadio : addToWhitelistOutgoingRadio;
        RadioButton removeFromWhiteListRadio = (blockType == BlockType.INCOMING) ? clearWhitelistIncomingRadio : clearWhitelistOutgoingRadio;

        switch(blockedState){
            case BlockState.WHITE_LIST:
                //check the always block radio button
                //hide the scheduled view
                addTowhiteListRadio.setChecked(true);

                break;
            default:
                //check the don't block radio button
                //hide the scheduled view
                removeFromWhiteListRadio.setChecked(true);
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        //unregister the broadcast listener that is released from SaveFromPhoneBookService
        getActivity().unregisterReceiver(mOnUpdateContactFromPhoneBook);
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
        IntentFilter filter = new IntentFilter(SaveFromPhoneBookService.ACTION_REFRESH_BLOCKED_LIST_UI);
        //only receive broadcasts which are sent through the valid private permission - we don't want to receive a broadcast just matching an intent - we want the permission too
        getActivity().registerReceiver(mOnUpdateContactFromPhoneBook, filter, SaveFromPhoneBookService.PRIVATE_PERMISSION, null);
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
