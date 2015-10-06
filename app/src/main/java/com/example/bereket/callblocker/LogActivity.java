package com.example.bereket.callblocker;

import android.app.Fragment;

/**
 * Created by bereket on 10/6/15.
 */
public class LogActivity extends SingleFragmentActivity implements BlockedListFragment.OnFragmentInteractionListener{
    @Override
    protected Fragment createFragment() {

        String arg1 = "";
        String arg2 = "";

        return LogFragment.newInstance(arg1, arg2);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
