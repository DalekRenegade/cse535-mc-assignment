package com.cse535.assignments.group6;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Environment;

import java.io.File;

public class DatabaseContext extends ContextWrapper {

    private String dbFolderPath;

    public DatabaseContext(Context base, String dbFolderPath) {
        super(base);
        this.dbFolderPath = dbFolderPath;
    }

    @Override
    public File getDatabasePath(String name) {
        String rootPath = getApplicationInfo().dataDir;
        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard != null)
            rootPath = sdcard.getAbsolutePath();
        String dbFilePath = rootPath + File.separator + dbFolderPath + File.separator + name;
        if (!dbFilePath.endsWith(".db")) {
            dbFilePath += ".db";
        }

        File result = new File(dbFilePath);
        if (!result.getParentFile().exists()) {
            result.getParentFile().mkdirs();
        }
        return result;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
        return openOrCreateDatabase(name, mode, factory, null);

    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory,
                                               DatabaseErrorHandler errorHandler) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), null, errorHandler);
        return result;
    }
}
