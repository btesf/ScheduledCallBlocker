package com.example.bereket.callblocker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

/**
 * Created by bereket on 2/11/16.
 *
 * for commonly used functions/routines
 */
public class Utility {

    //prevent dialogs from stacking up. Cancel the previous dialog before creating and posting the new one
    private static AlertDialog alertDialog = null;

    public static void showCallInterceptionAlertDialog(final Context context){

        if(alertDialog != null) alertDialog.dismiss();

        alertDialog = new AlertDialog.Builder(context, R.style.CallBlockerDialogStyle).create();
        alertDialog.setTitle(context.getString(R.string.incoming_call_blocked_string));
        alertDialog.setMessage(context.getString(R.string.incoming_call_intercepted_title));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.see_log_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent i  = new Intent(context, MainAppActivity.class);

                        i.putExtra(Constants.FRAGMENT_ID, Constants.LOG_LIST_FRAGMENT);
                        context.startActivity(i);
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.cancel_see_log_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }
}
