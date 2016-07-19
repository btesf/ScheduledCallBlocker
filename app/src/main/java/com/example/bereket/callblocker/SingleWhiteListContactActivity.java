package com.example.bereket.callblocker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;


public  class SingleWhiteListContactActivity extends SingleFragmentActivity implements SingleWhiteListContactFragment.OnFragmentInteractionListener{


    @Override
    protected Fragment createFragment() {

        Contact contactExtra = (Contact) getIntent().getSerializableExtra(SingleContactFragment.CONTACT);
        boolean isContactFromPhoneBookExtra = (boolean) getIntent().getBooleanExtra(SingleContactFragment.ARG_CONTACT_FROM_PHONEBOOK, true);

        SingleWhiteListContactFragment contactFragment = SingleWhiteListContactFragment.newInstance(contactExtra, isContactFromPhoneBookExtra);

        return contactFragment;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * This method is included to override the back to parent button behaviour. Since I am returning to a specific tab(fragement)
     * in the activity, I want to add an extra in the MainActivity to send me to a specific tab.
     * @return
     */
    @Override
    public Intent getParentActivityIntent() { // getParentActivityIntent() if you are not using the Support Library
        final Bundle bundle = new Bundle();
        final Intent intent = new Intent(this, MainAppActivity.class);

        bundle.putInt(Constants.FRAGMENT_ID, Constants.WHITE_LIST_FRAGMENT);
        intent.putExtras(bundle);

        return intent;
    }
}
