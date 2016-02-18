package com.example.bereket.callblocker;

import android.content.Intent;
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
    public void onFragmentInteraction(String id) {

    }

    @Override
    protected Fragment createFragment() {

        BlockedListFragment fragment = new BlockedListFragment();
        Bundle args = new Bundle();
        args.putString("", "");
        args.putString("", "");
        fragment.setArguments(args);
        return fragment;
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

        bundle.putInt(Constants.FRAGMENT_ID, Constants.BLOCKED_LIST_FRAGMENT);
        intent.putExtras(bundle);

        return intent;
    }
}
