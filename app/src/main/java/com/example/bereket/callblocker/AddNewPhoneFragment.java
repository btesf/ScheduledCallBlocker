package com.example.bereket.callblocker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddNewPhoneFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddNewPhoneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddNewPhoneFragment extends HideNotificationFragment {
    //intent extra key string
    public static final String NEW_PHONE_NUMBER_EXTRA_KEY = "addNewPhoneFragment.phoneNumber";

    private EditText newPhoneNumberEdit;
    private Button saveButton;
    private Button cancelButton;

    private String newPhoneNumber;
    private ContactManager mContactManager;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddNewPhoneFragment.
     */
    public static AddNewPhoneFragment newInstance() {
        AddNewPhoneFragment fragment = new AddNewPhoneFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AddNewPhoneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContactManager = ContactManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_add_new_contact, container, false);

        saveButton = (Button) v.findViewById(R.id.add_new_phone_button);
        cancelButton = (Button) v.findViewById(R.id.cancel_add_phone_button);
        newPhoneNumberEdit = (EditText) v.findViewById(R.id.edit_box_new_phone_number);
        final TextInputLayout floatingUsernameLabel = (TextInputLayout) v.findViewById(R.id.phone_text_input_layout);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(newPhoneNumber ==null || newPhoneNumber.isEmpty()){

                    floatingUsernameLabel.setError(getResources().getString(R.string.nothing_to_save));
                    floatingUsernameLabel.setErrorEnabled(true);
                }
                else if(!mContactManager.numberHasProperFormat(newPhoneNumber.toString())) {//if number is not of a proper format, show an error message

                    floatingUsernameLabel.setError(getResources().getString(R.string.wrong_phone_number_format));
                    floatingUsernameLabel.setErrorEnabled(true);
                }
                else {
                    Intent intent = new Intent();

                    intent.putExtra(NEW_PHONE_NUMBER_EXTRA_KEY, newPhoneNumber);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });


        newPhoneNumberEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s != null) {

                    floatingUsernameLabel.setErrorEnabled(false);
                    newPhoneNumber = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        public void onFragmentInteraction(Uri uri);
    }

}
