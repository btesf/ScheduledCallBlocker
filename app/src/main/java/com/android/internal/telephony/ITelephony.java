package com.android.internal.telephony;

/**
 * Created by bereket on 8/18/15.
 */
public interface ITelephony {

    boolean endCall();

    void answerRingingCall();

    void silenceRinger();
}