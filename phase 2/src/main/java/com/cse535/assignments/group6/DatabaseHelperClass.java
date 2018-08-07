package com.cse535.assignments.group6;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Deque;
import java.util.LinkedList;

//By Varun
public class DatabaseHelperClass {

    private SQLiteDatabase db;
    private String tableName;

    public String getDbPath() {
        return db.getPath();
    }

    public void initializeDatabase(String pathFromRoot) throws SQLException {

        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String dbFilePath = sdcardPath + File.separator + pathFromRoot + File.separator + Constants.DATABASE_NAME;
        if (!dbFilePath.endsWith(".db"))
            dbFilePath += ".db";
        File result = new File(dbFilePath);
        if (!result.getParentFile().exists())
            result.getParentFile().mkdirs();
        db = SQLiteDatabase.openOrCreateDatabase(dbFilePath, null);
    }

    public void createPatientTable(String tableName) throws SQLException {
        this.tableName = tableName;
        String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + " time_stamp INTEGER NOT NULL,"
                + " x REAL NOT NULL,"
                + " y REAL NOT NULL,"
                + " z REAL NOT NULL)";
        db.execSQL(CREATE_TABLE_USER);
    }

    public void addAccelerometerDataToDb(AccelerometerData accelerometer) throws SQLException {
        if (accelerometer != null) {
            final String insertStr = String.format("INSERT INTO %s (time_stamp, x, y, z) VALUES ( %d, %.2f, %.2f, %.2f )",
                    this.tableName, accelerometer.getTimestamp(), accelerometer.getX(), accelerometer.getY(), accelerometer.getZ());
            db.execSQL(insertStr);
            Log.i(this.getClass().getName(), "Record inserted");
        }
    }

    public Deque<AccelerometerData> getAccelerometerDataFromDb(String tableName, int limit) throws SQLException {
        this.tableName = tableName;
        Deque<AccelerometerData> accDataList = new LinkedList<AccelerometerData>();
        String sqlReadStr = String.format("SELECT time_stamp, x, y, z FROM %s DESC LIMIT %d", this.tableName, limit);
        Cursor cur = db.rawQuery(sqlReadStr, null);
        if (cur.moveToFirst()) {
            do {
                long dt = Long.parseLong(cur.getString(0));
                double x = Double.parseDouble(cur.getString(1));
                double y = Double.parseDouble(cur.getString(2));
                double z = Double.parseDouble(cur.getString(3));
                accDataList.addFirst(new AccelerometerData(dt, x, y, z));
            } while (cur.moveToNext());
        }
        return accDataList;
    }

}
