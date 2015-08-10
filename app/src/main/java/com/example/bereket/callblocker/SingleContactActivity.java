package com.example.bereket.callblocker;

import android.app.Fragment;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class SingleContactActivity extends SingleFragmentActivity implements SingleContactFragment.OnFragmentInteractionListener{


    @Override
    protected Fragment createFragment() {

        Contact contactExtra = (Contact) getIntent().getSerializableExtra(SingleContactFragment.ARG_PARAM1);

        SingleContactFragment contactFragment = SingleContactFragment.newInstance(contactExtra);

        return contactFragment;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
