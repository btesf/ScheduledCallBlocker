package com.example.bereket.callblocker;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;


import java.util.List;

public class SettingActivity extends AppCompatActivity{

    public interface ConfirmationDialogInteractionListener {
        public void onConfirmationDialogInteractionListener(int preferenceType, boolean isPositive);
    }

    private ConfirmationDialogInteractionListener confirmationDialogInteractionListener;
    private int returnView;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        /*
        I have created a Preference Fragment to stop using the deprecated methods which were used in PreferenceActivity before API 11
        From that API on these methods are no longer supported and are transferred to PreferenceFragment
         */
        super.onCreate(savedInstanceState);
        //return view is the tab(fragment) where the settings action is fired. Keep it and when 'home'/back button it will be returned
        //so that the right tab is selected in MainActivity
        returnView = getIntent().getIntExtra(Constants.FRAGMENT_ID, Constants.BLOCKED_LIST_FRAGMENT);
        SettingFragment settingFragment = new SettingFragment();
        confirmationDialogInteractionListener = settingFragment;
        getFragmentManager().beginTransaction().replace(android.R.id.content, settingFragment).commit();
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
        //putback the returnView(tab position)
        bundle.putInt(Constants.FRAGMENT_ID, returnView);
        intent.putExtras(bundle);

        return intent;
    }

    public void preferenceCallBack(int preferenceType, boolean isPositive){

        confirmationDialogInteractionListener.onConfirmationDialogInteractionListener(preferenceType, isPositive);
    }
}
