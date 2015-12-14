package com.example.bereket.callblocker;

import android.app.Fragment;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class SingleContactLogActivity extends SingleFragmentActivity implements SingleContactLogFragment.OnFragmentInteractionListener{

    @Override
    protected Fragment createFragment() {

        LogRecord logRecord = (LogRecord) getIntent().getSerializableExtra(SingleContactLogFragment.ARG_PARAM1);

        SingleContactLogFragment contactFragment = SingleContactLogFragment.newInstance(logRecord);

        return contactFragment;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
