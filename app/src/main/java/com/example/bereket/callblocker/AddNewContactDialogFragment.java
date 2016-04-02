package com.example.bereket.callblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by bereket on 9/21/15.
 */
public class AddNewContactDialogFragment extends DialogFragment {

    public static String CONTACT_TYPE = "com.example.bereket.callblocker.contactType";

    public static AddNewContactDialogFragment newInstance(int contactType) {
        AddNewContactDialogFragment fragment = new AddNewContactDialogFragment();
        Bundle args = new Bundle();
        args.putInt(CONTACT_TYPE, contactType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        // Inflate the layout for this fragment
        View v = getActivity().getLayoutInflater().inflate(R.layout.add_new_contact_fragment, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(R.string.add_contact);

        builder.setNegativeButton(R.string.button_cancel_button, null);

        Button addFromContact = (Button) v.findViewById(R.id.add_from_contact_button);
        Button addManually = (Button) v.findViewById(R.id.add_manually_button);

        addFromContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, Constants.REQUEST_NEW_CONTACT);
            }
        });

        addManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), AddNewPhoneActivity.class);
                startActivityForResult(intent, Constants.ADD_CONTACT_MANUALLY);
            }
        });

        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode != Activity.RESULT_OK) return;

        if (getTargetFragment() == null)
            return;
        //forward the returned data to calling list fragment (whitelistfragment/blocklistfragment)
        getTargetFragment().onActivityResult(requestCode, Activity.RESULT_OK, data);

        dismiss();
    }
}
