package com.example.bereket.callblocker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
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

    private static final int ALL_INCOMING_CALL_SWITCH = 1;
    private static final int ALL_OUTGOING_CALL_SWITCH = 2;
    private static final int INCOMING_CALL_CHECK_BOX = 3;
    private static final int OUTGOING_CALL_CHECK_BOX = 4;

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

                    FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                    Fragment prev = getActivity().getFragmentManager().findFragmentByTag("blockAllIncomingCallSwitchPreference_confirm_dialog");

                    if (prev != null) {
                        ft.remove(prev);
                    }


                    DialogFragment newFragment = newSettingAlertDialogFragmentInstance(ALL_INCOMING_CALL_SWITCH, R.string.confirm_block_all_incoming_calls_title, R.string.confirm_block_all_incoming_calls);
                    newFragment.show(getFragmentManager(), "blockAllIncomingCallSwitchPreference_confirm_dialog");

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


                    FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                    Fragment prev = getActivity().getFragmentManager().findFragmentByTag("blockAllOutgoingCallSwitchPreference_confirm_dialog");

                    if (prev != null) {
                        ft.remove(prev);
                    }


                    DialogFragment newFragment = newSettingAlertDialogFragmentInstance(ALL_OUTGOING_CALL_SWITCH, R.string.confirm_block_all_outgoing_calls_title, R.string.confirm_block_all_outgoing_calls);
                    newFragment.show(getFragmentManager(), "blockAllOutgoingCallSwitchPreference_confirm_dialog");

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

                    FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                    Fragment prev = getActivity().getFragmentManager().findFragmentByTag("incomigCallCheckBoxPreference_confirm_dialog");

                    if (prev != null) {
                        ft.remove(prev);
                    }


                    DialogFragment newFragment = newSettingAlertDialogFragmentInstance(INCOMING_CALL_CHECK_BOX, R.string.confirm_disable_incoming_call_notification_title, R.string.confirm_disable_incoming_call_notification);
                    newFragment.show(getFragmentManager(), "incomigCallCheckBoxPreference_confirm_dialog");
                }

                return true;
            }
        });


        outgoigCallCheckBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object o) {

                //if disable is selected show an confirmation dialog and get confirmation from the user
                if (((Boolean) o).equals(Boolean.TRUE)) {

                    FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                    Fragment prev = getActivity().getFragmentManager().findFragmentByTag("outgoigCallCheckBoxPreference_confirm_dialog");

                    if (prev != null) {
                        ft.remove(prev);
                    }


                    DialogFragment newFragment = newSettingAlertDialogFragmentInstance(OUTGOING_CALL_CHECK_BOX, R.string.confirm_disable_outgoing_call_notification_title, R.string.confirm_disable_outgoing_call_notification);
                    newFragment.show(getFragmentManager(), "outgoigCallCheckBoxPreference_confirm_dialog");
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

    private SettingAlertDialogFragment newSettingAlertDialogFragmentInstance(final int preferenceType, final int title, final int message) {

        SettingAlertDialogFragment fragement = new SettingAlertDialogFragment();

        Bundle args = new Bundle();

        args.putInt("title", title);
        args.putInt("message", message);
        args.putInt("preferenceType", preferenceType);
        fragement.setArguments(args);

        return fragement;
    }

    //callback method called inside the DialogFragment.
    private void preferenceCallBack(int preferenceType, boolean isPositive){

        switch(preferenceType){

            case ALL_INCOMING_CALL_SWITCH:

                if(isPositive){

                    ((SwitchPreference) blockAllIncomingCallSwitchPreference).setChecked(true);
                    //enable whitelist switch preference
                    setWhitelistPreference(BlockType.INCOMING, true);
                    Toast.makeText(getActivity(), R.string.all_incoming_calls_are_blocked, Toast.LENGTH_SHORT).show();
                }
                else{

                    ((SwitchPreference) blockAllIncomingCallSwitchPreference).setChecked(false);
                }

                break;
            case ALL_OUTGOING_CALL_SWITCH:

                if(isPositive){

                    ((SwitchPreference) blockAllOutgoingCallSwitchPreference).setChecked(true);
                    //enable whitelist switch preference
                    setWhitelistPreference(BlockType.OUTGOING, true);
                    Toast.makeText(getActivity(), R.string.all_outgoing_calls_are_blocked, Toast.LENGTH_SHORT).show();
                }
                else{

                    ((SwitchPreference) blockAllOutgoingCallSwitchPreference).setChecked(false);
                }

                break;
            case INCOMING_CALL_CHECK_BOX:

                if(isPositive){

                    ((CheckBoxPreference) incomigCallCheckBoxPreference).setChecked(true);
                    Toast.makeText(getActivity(), R.string.all_incoming_block_notification_blocked, Toast.LENGTH_SHORT).show();
                }
                else{

                    ((CheckBoxPreference) incomigCallCheckBoxPreference).setChecked(false);
                }

                break;
            case OUTGOING_CALL_CHECK_BOX:

                if(isPositive){

                    ((CheckBoxPreference) outgoigCallCheckBoxPreference).setChecked(true);
                    Toast.makeText(getActivity(), R.string.all_outgoing_block_notification_blocked, Toast.LENGTH_SHORT).show();
                }
                else{

                    ((CheckBoxPreference) outgoigCallCheckBoxPreference).setChecked(false);
                }

                break;
            default:
        }
    }


    /**
     * This DialogFragment is used to contain the confirmation alert dialogs, so that on screen orientation
     * they may be recreated.
     *
     * I created a static private element alertDialog, so that only one instance is available and returned on
     * ..CreateDialog() is called. On Screen orientation, this method is invoked multiple times so that multiple alert dialogs are
     * created and stacked upon one another.
     *
     * The static member avoids this issue.
     */
    public class SettingAlertDialogFragment extends DialogFragment {

        private int preferenceType;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            int title;
            int message;

            if(savedInstanceState != null) {

                title = savedInstanceState.getInt("title");
                message = savedInstanceState.getInt("message");
                preferenceType = savedInstanceState.getInt("preferenceType");
            }
            else{

                title = getArguments().getInt("title");
                message = getArguments().getInt("message");
                preferenceType = getArguments().getInt("preferenceType");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    preferenceCallBack(preferenceType, true);
                }
            });

            builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    preferenceCallBack(preferenceType, false);
                }
            });

            return builder.create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {

            preferenceCallBack(preferenceType, false);

            if(isAdded()) {
                //remove this fragment instance from backstack. Otherwise on screen rotation, all the created fragments will be recreated
                getActivity().getFragmentManager().beginTransaction().remove(this).commit();
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {

            preferenceCallBack(preferenceType, false);

            if(isAdded()) {
                //remove this fragment instance from backstack. Otherwise on screen rotation, all the created fragments will be recreated
                getActivity().getFragmentManager().beginTransaction().remove(this).commit();
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {

            outState.putInt("title", getArguments().getInt("title"));
            outState.putInt("message", getArguments().getInt("message"));
            outState.putInt("preferenceType", getArguments().getInt("preferenceType"));

            super.onSaveInstanceState(outState);
        }
    }

}

