package com.cse535.assignments.group6;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AccelerometerBroadcastReceiver extends BroadcastReceiver {

    GraphUpdateCallback callback = null;

    // This decouples the receiver class from the activity class
    // which would not be possible if context was passed to this receiver's constructor
    public AccelerometerBroadcastReceiver(GraphUpdateCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AccelerometerData data = (AccelerometerData) intent.getExtras().getSerializable(Constants.SER_KEY);
        this.callback.controlGraph(true, data);
    }
}
