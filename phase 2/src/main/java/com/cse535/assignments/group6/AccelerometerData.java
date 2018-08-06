package com.cse535.assignments.group6;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AccelerometerData implements Serializable {
    private Date timestamp;
    private double x, y, z;

    public AccelerometerData(Date timestamp, double x, double y, double z) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public Date getTimestamp() {
        return timestamp;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public String toString() {
        return String.format("%s_%.2f_%.2f_%.2f", (new SimpleDateFormat(Constants.DATE_FORMAT)).format(this.timestamp), this.x, this.y, this.z);
    }
}
