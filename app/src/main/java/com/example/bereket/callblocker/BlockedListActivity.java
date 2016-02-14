package com.example.bereket.callblocker;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;


public class BlockedListActivity extends SingleFragmentActivity implements BlockedListFragment.OnFragmentInteractionListener{

    @Override
    protected android.app.Fragment createFragment() {

        String arg1 = "";
        String arg2 = "";

        return BlockedListFragment.newInstance(arg1, arg2);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
