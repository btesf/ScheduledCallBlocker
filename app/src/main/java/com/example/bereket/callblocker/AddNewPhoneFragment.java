package com.example.bereket.callblocker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
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
public class AddNewPhoneFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //intent extra key string
    public static final String NEW_PHONE_NUMBER_EXTRA_KEY = "addNewPhoneFragment.phoneNumber";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddNewPhoneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddNewPhoneFragment newInstance(String param1, String param2) {
        AddNewPhoneFragment fragment = new AddNewPhoneFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AddNewPhoneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mContactManager = ContactManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_add_new_contact, container, false);

        saveButton = (Button) v.findViewById(R.id.add_new_phone_button);
        cancelButton = (Button) v.findViewById(R.id.cancel_add_phone_button);
        newPhoneNumberEdit = (EditText) v.findViewById(R.id.edit_box_new_phone_number);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if number is not of a proper format, show an error message
                if(!mContactManager.numberHasProperFormat(newPhoneNumber.toString())){
                    Toast.makeText(getActivity(), R.string.wrong_phone_number_format, Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent();
                    //TODO set proper extra key name
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
                if (!s.toString().isEmpty()) {
                    newPhoneNumber = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
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