package com.cse535.assignments.group6;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

public class DatabaseHelperClass extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static String tableName;
    PatientInfo patientInfo;
    boolean isDbOpen;
    private SQLiteStatement sqlInsertStatement;

    public DatabaseHelperClass(Context context, String dbPathFromRoot, String dbName, PatientInfo patientInfo) {
        super(new DatabaseContext(context, dbPathFromRoot), dbName, null, DATABASE_VERSION);
        this.patientInfo = patientInfo;
        this.tableName = patientInfo.toString();
        this.isDbOpen = false;
    }

    public String getDbFilePath() {
        return getWritableDatabase().getPath();
    }

    @Override
    public void onCreate(SQLiteDatabase db) throws SQLException {
        this.isDbOpen = true;
        String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + " time_stamp TEXT DEFAULT CURRENT_TIMESTAMP,"
                + " x REAL NOT NULL,"
                + " y REAL NOT NULL,"
                + " z REAL NOT NULL)";
        db.execSQL(CREATE_TABLE_USER);
        final String insertStr = String.format("INSERT INTO %s (time_stamp, x, y, z) VALUES ( ?, ?, ?, ? )", this.tableName);
        sqlInsertStatement = db.compileStatement(insertStr);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int j) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        this.onCreate(db);
    }

    public void addAccelerometerDataToDb(AccelerometerData accelerometer) {
        try {
            if (!isDbOpen) {
                onCreate(getWritableDatabase());
                isDbOpen = true;
            }
            sqlInsertStatement.clearBindings();
            sqlInsertStatement.bindString(1, accelerometer.getTimestamp().toString());
            sqlInsertStatement.bindDouble(2, accelerometer.getX());
            sqlInsertStatement.bindDouble(3, accelerometer.getY());
            sqlInsertStatement.bindDouble(4, accelerometer.getZ());
            long insertCount = sqlInsertStatement.executeInsert();
            Log.i(this.getClass().getName(), "No. of rows inserted = " + insertCount + ".....................................");
        } catch (Exception ex) {
            Log.e(this.getClass().getName(), ex.getMessage());
        }
    }

    public void safeCloseDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null)
            db.close();
        this.isDbOpen = false;
    }

    public ArrayList<AccelerometerData> getAccelerometerDataFromDb(int limit) {
        ArrayList<AccelerometerData> accDataList = new ArrayList<AccelerometerData>(limit);
        Cursor cur = null;
        SQLiteDatabase db = null;
        try {
            db = this.getReadableDatabase();
            String sqlReadStr = String.format("SELECT time_stamp, x, y, z FROM %s DESC LIMIT %d", this.tableName, limit);
            cur = db.rawQuery(sqlReadStr, null);
            if (cur.moveToFirst()) {
                do {
                    Date dt = new Date(cur.getString(0));
                    double x = Double.parseDouble(cur.getString(1));
                    double y = Double.parseDouble(cur.getString(2));
                    double z = Double.parseDouble(cur.getString(3));
                    accDataList.add(new AccelerometerData(dt, x, y, z));
                } while (cur.moveToNext());
            }
        } catch (Exception ex) {
            Log.e(this.getClass().getName(), ex.getMessage());
        } finally {
            if (cur != null)
                cur.close();
            if (db != null)
                db.close();
        }
        return accDataList;
    }


}
