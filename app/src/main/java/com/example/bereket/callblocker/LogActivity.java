package com.example.bereket.callblocker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by bereket on 10/6/15.
 */
public class LogActivity extends SingleFragmentActivity implements LogFragment.OnFragmentInteractionListener{

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected Fragment createFragment() {

        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        args.putString("", "");
        args.putString("", "");
        fragment.setArguments(args);
        return fragment;
    }
}
