package com.example.bereket.callblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by bereket on 2/13/16.
 */
public class AboutUsFragment extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = getActivity().getLayoutInflater().inflate(R.layout.about_us, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        //custom -  prepare centered title
        TextView customTitle = new TextView(getActivity());
        customTitle.setText(R.string.about_us_title);
        customTitle.setTextAppearance(getActivity(), R.style.TextAppearance_AppCompat_Title);
        customTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
        customTitle.setPadding(10, 10, 10, 10); //I don't really know why I need padding for such a small title
        customTitle.setGravity(Gravity.CENTER);
        //set custom title
        builder.setCustomTitle(customTitle);

        builder.setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (getTargetFragment() == null)
                    return;

             }
        });

        return builder.create();
    }
}
