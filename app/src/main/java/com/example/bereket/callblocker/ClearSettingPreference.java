package com.example.bereket.callblocker;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by bereket on 11/6/15.
 */
public class ClearSettingPreference extends DialogPreference implements DialogInterface {

    private Integer clearValue = null;
    private ContactManager contactManager;

    public ClearSettingPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        for(int i = 0; i< attrs.getAttributeCount(); i++){

            if(attrs.getAttributeName(i).equals("clearValue")){

                clearValue = attrs.getAttributeIntValue(i, 0);
                break;
            }
        }

        contactManager = ContactManager.getInstance(getContext());
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        switch(which) {
            case BUTTON_POSITIVE:

                if(clearValue != null){

                    switch(clearValue){

                        case 1: //clear all 'always block' incoming

                            contactManager.unblockBlockedNumbersByType(BlockType.INCOMING);
                            break;
                        case 2: //clear all scheduled incoming

                            contactManager.unblockScheduledListByType(BlockType.INCOMING);
                            break;
                        case 3: //clear all 'always block' outgoing

                            contactManager.unblockBlockedNumbersByType(BlockType.OUTGOING);
                            break;
                        case 4: //clear all scheduled outgoing

                            contactManager.unblockScheduledListByType(BlockType.OUTGOING);
                            break;
                        default:
                    }
                }

                break;
            case BUTTON_NEGATIVE: //we don't want to do anything here
            default:
        }
    }

    @Override
    public void cancel() {

    }

    @Override
    public void dismiss() {

    }
}
