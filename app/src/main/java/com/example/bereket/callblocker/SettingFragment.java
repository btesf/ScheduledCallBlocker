package com.example.bereket.callblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by bereket on 11/7/15.
 */
public class SettingFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //attach preference xml to this preference
        addPreferencesFromResource(R.xml.preference);

        Resources resources = getResources();
        //get the incoming block disable/enable checkbox key
        Preference incomigCallCheckBoxPreference = findPreference(resources.getString(R.string.disable_incoming_block_notification_pref_key));
        Preference outgoigCallCheckBoxPreference = findPreference(resources.getString(R.string.disable_outgoing_block_notification_pref_key));
        Preference blockAllIncomingCallSwitchPreference = findPreference(resources.getString(R.string.block_all_incoming_numbers_pref_key));
        Preference blockAllOutgoingCallSwitchPreference = findPreference(resources.getString(R.string.block_all_outgoing_numbers_pref_key));

     /* preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Log.d("bere.bere.bere", "woooooooooooooooooooo  I am called wooooooooooo");
                return false;
            }
        });*/

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
                        }
                    });

                    builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ((SwitchPreference) preference).setChecked(false);
                        }
                    });

                    builder.show();
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
                        }
                    });

                    builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ((SwitchPreference) preference).setChecked(false);
                        }
                    });

                    builder.show();
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
                        }
                    });

                    builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

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
                        }
                    });

                    builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ((CheckBoxPreference) preference).setChecked(false);
                        }
                    });

                    builder.show();
                }

                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
