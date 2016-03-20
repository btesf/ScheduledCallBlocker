package com.example.bereket.callblocker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by bereket on 11/7/15.
 */
public class SettingFragment extends HideNotificationPreferenceFragment{

    private static final int ABOUT_THE_APP_DIALOG_REQUEST_CODE = 1;

    private Preference incomigCallCheckBoxPreference;
    private Preference outgoigCallCheckBoxPreference;
    private Preference blockAllIncomingCallSwitchPreference;
    private Preference blockAllOutgoingCallSwitchPreference;
    private Preference blockAllIncomingCallExceptWhitelistSwitchPreference;
    private Preference blockAllOutgoingCallExceptWhitelistSwitchPreference;
    private Preference enableVibrationOnInterceptionPreference;
    private Preference aboutUsPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //attach preference xml to this preference
        addPreferencesFromResource(R.xml.preference);

        Resources resources = getResources();
        //get the incoming block disable/enable checkbox key
        incomigCallCheckBoxPreference = findPreference(resources.getString(R.string.disable_incoming_block_notification_pref_key));
        outgoigCallCheckBoxPreference = findPreference(resources.getString(R.string.disable_outgoing_block_notification_pref_key));
        blockAllIncomingCallSwitchPreference = findPreference(resources.getString(R.string.block_all_incoming_numbers_pref_key));
        blockAllOutgoingCallSwitchPreference = findPreference(resources.getString(R.string.block_all_outgoing_numbers_pref_key));
        blockAllIncomingCallExceptWhitelistSwitchPreference = findPreference(resources.getString(R.string.incoming_whitelist_pref_key));
        blockAllOutgoingCallExceptWhitelistSwitchPreference = findPreference(resources.getString(R.string.outgoing_whitelist_pref_key));
        enableVibrationOnInterceptionPreference = findPreference(resources.getString(R.string.enable_vibration_pref_key));
        aboutUsPreference = findPreference(resources.getString(R.string.about_the_app_pref_key));

        if(!((SwitchPreference)blockAllIncomingCallSwitchPreference).isChecked()){

            blockAllIncomingCallExceptWhitelistSwitchPreference.setEnabled(false);
        }
        else blockAllIncomingCallExceptWhitelistSwitchPreference.setEnabled(true);

        if(!((SwitchPreference)blockAllOutgoingCallSwitchPreference).isChecked()){

            blockAllOutgoingCallExceptWhitelistSwitchPreference.setEnabled(false);
        }
        else blockAllOutgoingCallExceptWhitelistSwitchPreference.setEnabled(true);


        blockAllIncomingCallSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object o) {
                //if enable is selected show an confirmation dialog and get confirmation from the user
                if (((Boolean) o).equals(Boolean.TRUE)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.confirm_block_all_incoming_calls_title);
                    builder.setMessage(R.string.confirm_block_all_incoming_calls);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ((SwitchPreference) preference).setChecked(true);
                            //enable whitelist switch preference
                            setWhitelistPreference(BlockType.INCOMING, true);
                            Toast.makeText(getActivity(), R.string.all_incoming_calls_are_blocked, Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ((SwitchPreference) preference).setChecked(false);
                        }
                    });

                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                            ((SwitchPreference) preference).setChecked(false);
                        }
                    });

                    builder.show();

                } else{

                    //disalbe whitelist switch preference
                    setWhitelistPreference(BlockType.INCOMING, false);
                }

                return true;
            }
        });

        blockAllOutgoingCallSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object o) {
                //if enable is selected show an confirmation dialog and get confirmation from the user
                if (((Boolean) o).equals(Boolean.TRUE)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.confirm_block_all_outgoing_calls_title);
                    builder.setMessage(R.string.confirm_block_all_outgoing_calls);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ((SwitchPreference) preference).setChecked(true);
                            //enable whitelist switch preference
                            setWhitelistPreference(BlockType.OUTGOING, true);
                            Toast.makeText(getActivity(), R.string.all_outgoing_calls_are_blocked, Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ((SwitchPreference) preference).setChecked(false);
                        }
                    });

                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                            ((SwitchPreference) preference).setChecked(false);
                        }
                    });

                    builder.show();
                } else{

                    //disalbe whitelist switch preference
                    setWhitelistPreference(BlockType.OUTGOING, false);
                }

                return true;
            }
        });

        incomigCallCheckBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object o) {

                //if disable is selected show an confirmation dialog and get confirmation from the user
                if (((Boolean) o).equals(Boolean.TRUE)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.confirm_disable_incoming_call_notification_title);
                    builder.setMessage(R.string.confirm_disable_incoming_call_notification);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ((CheckBoxPreference) preference).setChecked(true);
                            Toast.makeText(getActivity(), R.string.all_incoming_block_notification_blocked, Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ((CheckBoxPreference) preference).setChecked(false);
                        }
                    });

                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                            ((CheckBoxPreference) preference).setChecked(false);
                        }
                    });

                    builder.show();
                }

                return true;
            }
        });


        outgoigCallCheckBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object o) {

                //if disable is selected show an confirmation dialog and get confirmation from the user
                if (((Boolean) o).equals(Boolean.TRUE)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.confirm_disable_outgoing_call_notification_title);
                    builder.setMessage(R.string.confirm_disable_outgoing_call_notification);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ((CheckBoxPreference) preference).setChecked(true);
                            Toast.makeText(getActivity(), R.string.all_outgoing_block_notification_blocked, Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ((CheckBoxPreference) preference).setChecked(false);
                        }
                    });

                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                            ((CheckBoxPreference) preference).setChecked(false);
                        }
                    });

                    builder.show();
                }

                return true;
            }
        });

        aboutUsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AboutUsFragment aboutUsFragment = new AboutUsFragment();
                aboutUsFragment.setTargetFragment(SettingFragment.this, ABOUT_THE_APP_DIALOG_REQUEST_CODE);
                aboutUsFragment.show(getFragmentManager(), "");

                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setWhitelistPreference(int blockType, boolean isEnabled){

        Preference switchPreference = null;

        switch(blockType){

            case BlockType.INCOMING:

                switchPreference = blockAllIncomingCallExceptWhitelistSwitchPreference;
                break;
            case BlockType.OUTGOING:

                switchPreference = blockAllOutgoingCallExceptWhitelistSwitchPreference;
                break;
        }

        if(switchPreference != null){

            switchPreference.setEnabled(isEnabled);
        }
    }

    @Override
    public void doOnBroadcastReceived() {

        //TODO set string from xml/resources
        Toast.makeText(getActivity(), "New incoming call is blocked.", Toast.LENGTH_SHORT).show();
    }
}
