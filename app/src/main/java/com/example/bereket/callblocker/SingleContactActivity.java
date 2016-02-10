package com.example.bereket.callblocker;

import android.app.Fragment;
import android.net.Uri;


public class SingleContactActivity extends SingleFragmentActivity implements SingleContactFragment.OnFragmentInteractionListener{


    @Override
    protected Fragment createFragment() {

        Contact contactExtra = (Contact) getIntent().getSerializableExtra(SingleContactFragment.ARG_PARAM1);
        boolean isContactFromPhoneBookExtra = (boolean) getIntent().getBooleanExtra(SingleContactFragment.ARG_CONTACT_FROM_PHONEBOOK, true);

        SingleContactFragment contactFragment = SingleContactFragment.newInstance(contactExtra, isContactFromPhoneBookExtra);

        return contactFragment;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
