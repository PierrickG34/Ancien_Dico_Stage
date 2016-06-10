package com.antoine_charlotte_romain.dictionary.DataModel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by summer1 on 22/06/2015.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "dictionary_database.db";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DictionaryDataModel.SQL_CREATE_DICTIONARY);
        db.execSQL(WordDataModel.SQL_CREATE_WORD);
        db.execSQL(SearchDateDataModel.SQL_CREATE_SEARCH_DATE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SearchDateDataModel.SQL_DELETE_SEARCH_DATE);
        db.execSQL(WordDataModel.SQL_DELETE_WORD);
        db.execSQL(DictionaryDataModel.SQL_DELETE_DICTIONARY);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
