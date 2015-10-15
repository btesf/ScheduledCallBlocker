package com.example.bereket.callblocker;

import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class AddNewPhoneActivity extends SingleFragmentActivity implements AddNewPhoneFragment.OnFragmentInteractionListener {



    @Override
    protected Fragment createFragment() {

        String arg1 = "";
        String arg2 = "";

        AddNewPhoneFragment addNewPhoneFragment = AddNewPhoneFragment.newInstance(arg1, arg2);

        return addNewPhoneFragment;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
