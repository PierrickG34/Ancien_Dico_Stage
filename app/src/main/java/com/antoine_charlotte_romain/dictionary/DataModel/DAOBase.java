package com.antoine_charlotte_romain.dictionary.DataModel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class DAOBase {

    protected SQLiteDatabase myDb = null;
    protected DataBaseHelper myDbHelper =  null;
    protected Context context = null;

    public DAOBase(Context context) {
        this.myDbHelper = new DataBaseHelper(context);
        this.context = context;
    }

    public SQLiteDatabase open() {
        myDb = myDbHelper.getWritableDatabase();
        return myDb;
    }

    public void close() {
        myDb.close();
    }

    public SQLiteDatabase getDb() {
        return myDb;
    }
}