package com.example.bereket.callblocker;

import android.os.Bundle;
import android.app.DialogFragment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by bereket on 2/12/16.
 */
public class TestAuthenticator extends DialogFragment {

    private Integer nNum = null;
    private String password = null;
    private EditText tv;
    private TextView textView;

    static TestAuthenticator newInstance(int num) {
        TestAuthenticator f = new TestAuthenticator();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nNum = getArguments().getInt("num");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.test_dialog_fragment, container, false);
        getDialog().setTitle("Enter Test password");

        // Watch for button clicks.
        Button button = (Button)rootView.findViewById(R.id.ok_button_id);
        textView = (TextView) rootView.findViewById(R.id.counter_text_id);
        textView.setText("You have " + (3 - Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("app_password_counter_key", "10")) + " attempts left"));
        tv = (EditText)rootView.findViewById(R.id.password_edit_id);

        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    password = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if(password == null || password.isEmpty())
                    return;

                int attemptCounter = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("app_password_counter_key", "10"));

                if(attemptCounter > 2){

                    PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putBoolean("app_unlocked_key", false).commit();

                    dismiss();
                    getActivity().finish();
                    System.exit(0);
                }

                String passwordFromPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("app_test_password_key", "8292398239823983298");

                if(passwordFromPreferences.equals(password)){

                    PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putBoolean("app_unlocked_key", true).commit();
                    return;
                }

                attemptCounter++;
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putString("app_password_counter_key", String.valueOf(attemptCounter)).commit();

                tv.setText("");

                textView.setText("You have " + (3 - attemptCounter) + " attempts left");

                Log.d("bere.bere.bere", "Ok is cliced");
            }
        });

        return rootView;
    }
}
