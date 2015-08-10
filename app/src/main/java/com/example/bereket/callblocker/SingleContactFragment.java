package com.example.bereket.callblocker;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.w3c.dom.Text;


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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Contact mContact;
    //UI Elements
    private CheckBox mOutgoingCheckBox;
    private CheckBox mIncomingCheckbox;
    private TextView mPhoneNumberTextView;
    private TextView mContactNameTextView;

    //Database property
    private DataBaseHelper dataBaseHelper;

    private OnFragmentInteractionListener mListener;

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
                   mContact.setIsIncomingBlocked(isChecked);
            }
        });

        mOutgoingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mContact.setIsOutGoingBlocked(isChecked);
            }
        });

        mPhoneNumberTextView = (TextView) view.findViewById(R.id.contact_phone_number_id);
        mContactNameTextView = (TextView) view.findViewById(R.id.contact_name_id);

        mPhoneNumberTextView.setText(mContact.getPhoneNumber());
        mContactNameTextView.setText(mContact.getContactName());
        mIncomingCheckbox.setChecked(mContact.isIsIncomingBlocked());
        mOutgoingCheckBox.setChecked(mContact.isIsOutGoingBlocked());

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

}
