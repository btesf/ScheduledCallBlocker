package com.example.bereket.callblocker;

import android.net.Uri;
import android.support.v4.app.Fragment;


public  class SingleWhiteListContactActivity extends SingleFragmentActivity implements SingleWhiteListContactFragment.OnFragmentInteractionListener{


    @Override
    protected Fragment createFragment() {

        Contact contactExtra = (Contact) getIntent().getSerializableExtra(SingleContactFragment.ARG_PARAM1);
        boolean isContactFromPhoneBookExtra = (boolean) getIntent().getBooleanExtra(SingleContactFragment.ARG_CONTACT_FROM_PHONEBOOK, true);

        SingleWhiteListContactFragment contactFragment = SingleWhiteListContactFragment.newInstance(contactExtra, isContactFromPhoneBookExtra);

        return contactFragment;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
