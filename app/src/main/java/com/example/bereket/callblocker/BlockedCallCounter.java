package com.example.bereket.callblocker;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bereket on 11/24/15.
 */
public class BlockedCallCounter {

    private Context mContext;

    public BlockedCallCounter(Context context){

        mContext = context;
    }

    private static final String INCOMING_BLOCK_CALL_COUNTER = "incoming.block.counter.preference";
    private static final String OUTGOING_BLOCK_CALL_COUNTER = "outgoing.block.counter.preference";

    private void setBlockedCallCounterPreference(Integer callCount, int blockType){

        if(blockType == BlockType.INCOMING){

            PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).edit().putInt(INCOMING_BLOCK_CALL_COUNTER, callCount).commit();
        }
        else{

            PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).edit().putInt(OUTGOING_BLOCK_CALL_COUNTER, callCount).commit();
        }
    }

    private Integer getBlockedCallCounterPreference(int blockType){

        if(blockType == BlockType.INCOMING){

            return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getInt(INCOMING_BLOCK_CALL_COUNTER, 0);
        }
        else{

            return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getInt(OUTGOING_BLOCK_CALL_COUNTER, 0);
        }
    }

    public void resetCounter(){

        setBlockedCallCounterPreference(0, BlockType.INCOMING);
        setBlockedCallCounterPreference(0, BlockType.OUTGOING);
    }

    public int getBlockCount(int callType){

        if(callType == BlockType.INCOMING) return getBlockedCallCounterPreference(BlockType.INCOMING);
        else return getBlockedCallCounterPreference(BlockType.OUTGOING);
    }

    public int incrementAndGetBlockCount(int blockType){

        int blockCount = getBlockedCallCounterPreference(blockType);
        //increment block counter by one
        blockCount++;
        //set the preference value
        setBlockedCallCounterPreference(blockCount, blockType);

        return blockCount;
    }

}
