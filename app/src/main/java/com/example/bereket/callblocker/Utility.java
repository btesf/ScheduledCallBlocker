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

        //TODO replace texts with those coming fom xml
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Incoming call intercepted");
        alertDialog.setMessage("New call is blocked. ");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SEE LOG",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, LogActivity.class);
                        context.startActivity(intent);
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }
}
