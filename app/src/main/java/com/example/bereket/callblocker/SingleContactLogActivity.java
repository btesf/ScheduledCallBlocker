package com.example.bereket.callblocker;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
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

    /**
     * This method is included to override the back to parent button behaviour. Since I am returning to a specific tab(fragement)
     * in the activity, I want to add an extra in the MainActivity to send me to a specific tab.
     * @return
     */
    @Override
    public Intent getParentActivityIntent() { // getParentActivityIntent() if you are not using the Support Library
        final Bundle bundle = new Bundle();
        final Intent intent = new Intent(this, MainAppActivity.class);

        bundle.putInt(Constants.FRAGMENT_ID, Constants.LOG_LIST_FRAGMENT);
        intent.putExtras(bundle);

        return intent;
    }
}
