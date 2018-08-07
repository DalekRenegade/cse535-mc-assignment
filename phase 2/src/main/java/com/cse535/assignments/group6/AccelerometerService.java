package com.cse535.assignments.group6;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class AccelerometerService extends Service implements SensorEventListener {

    public static AccelerometerService ServiceObject;

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private boolean serviceStopped;
    private long lastUpdated = 0;

    public AccelerometerService() {
        ServiceObject = this;
        serviceStopped = false;
        sensorManager = (SensorManager) MainActivity.sensorManagerCallback.getSystemServiceCallback(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceStopped = false;
        resumeSensor();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        serviceStopped = true;
        pauseSensor();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (!serviceStopped && sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currTime = System.currentTimeMillis();
            if (currTime - lastUpdated > Constants.DELAY) {
                AccelerometerData data = new AccelerometerData(currTime, sensorEvent.values[0],
                        sensorEvent.values[1], sensorEvent.values[2]);
                sendBroadcast(data);
            }
        }
    }

    private void sendBroadcast(AccelerometerData data) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Constants.BROADCAST_ACTION);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.SER_KEY, data);
        broadcastIntent.putExtras(bundle);
        if (!serviceStopped)
            sendBroadcast(broadcastIntent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void pauseSensor() {
        sensorManager.unregisterListener(this);
    }

    public void resumeSensor() {
        pauseSensor();
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
