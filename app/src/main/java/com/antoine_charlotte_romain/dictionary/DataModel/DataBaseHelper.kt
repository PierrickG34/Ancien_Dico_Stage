package com.antoine_charlotte_romain.dictionary.DataModel

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import com.dicosaure.Business.Translate.TranslateSQLITE
import org.jetbrains.anko.db.ManagedSQLiteOpenHelper
import org.jetbrains.anko.db.select

/**
 * Created by dineen on 13/06/2016.
 */

class DataBaseHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "MyDatabase", null, 1) {
    companion object {
        private var instance: DataBaseHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): DataBaseHelper {
            if (instance == null) {
                instance = DataBaseHelper(ctx.getApplicationContext())
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
                """
                CREATE TABLE ${DictionarySQLITE.DB_TABLE} (
                    ${DictionarySQLITE.DB_COLUMN_ID} INTEGER,
                    ${DictionarySQLITE.DB_COLUMN_INLANG} TEXT NOT NULL,
                    ${DictionarySQLITE.DB_COLUMN_OUTLANG} TEXT NOT NULL,
                    CONSTRAINT pk_dictionary PRIMARY KEY(${DictionarySQLITE.DB_COLUMN_ID}),
                    CONSTRAINT unique_dictionary UNIQUE(${DictionarySQLITE.DB_COLUMN_INLANG}, ${DictionarySQLITE.DB_COLUMN_OUTLANG})
                );
                """
        )
        db.execSQL(
                """
                CREATE TABLE ${WordSQLITE.DB_TABLE} (
                    ${WordSQLITE.DB_COLUMN_ID} INTEGER,
                    ${WordSQLITE.DB_COLUMN_NOTE} TEXT NULL,
                    ${WordSQLITE.DB_COLUMN_DATE} DATE NULL,
                    ${WordSQLITE.DB_COLUMN_HEADWORD} TEXT NOT NULL,
                    ${WordSQLITE.DB_COLUMN_ID_DICTIONARY} DATE NULL,
                    ${WordSQLITE.DB_COLUMN_IMAGE} BLOB NULL,
                    ${WordSQLITE.DB_COLUMN_SOUND} BLOB NULL,
                    CONSTRAINT pk_word PRIMARY KEY(${WordSQLITE.DB_COLUMN_ID}),
                    CONSTRAINT fk_word_dictionary FOREIGN KEY(${WordSQLITE.DB_COLUMN_ID_DICTIONARY}) REFERENCES ${DictionarySQLITE.DB_TABLE}(${DictionarySQLITE.DB_COLUMN_ID})
                );
                """
        )
        db.execSQL(
                """
                CREATE TABLE ${TranslateSQLITE.DB_TABLE} (
                    ${TranslateSQLITE.DB_COLUMN_WORDTO} INTEGER NOT NULL,
                    ${TranslateSQLITE.DB_COLUMN_WORDFROM} INTEGER NOT NULL,
                    CONSTRAINT pk_translate_word PRIMARY KEY(${TranslateSQLITE.DB_COLUMN_WORDTO}, ${TranslateSQLITE.DB_COLUMN_WORDFROM}),
                    CONSTRAINT fk_translate_wordTo FOREIGN KEY(${TranslateSQLITE.DB_COLUMN_WORDTO}) REFERENCES ${WordSQLITE.DB_TABLE}(${WordSQLITE.DB_COLUMN_ID}),
                    CONSTRAINT fk_translate_wordFrom FOREIGN KEY(${TranslateSQLITE.DB_COLUMN_WORDFROM}) REFERENCES ${WordSQLITE.DB_TABLE}(${WordSQLITE.DB_COLUMN_ID})
                );
                """
        )
    }

    fun showTable(ctx : Context) {
        val c = this.readableDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                Toast.makeText(ctx, "Table Name=> "+c.getString(0), Toast.LENGTH_LONG).show();
                c.moveToNext();
            }
        }
    }

    fun insertTest(ctx : Context) {
//        var dico = DictionarySQLITE(ctx, "fr", "en")
//        dico.save()
//        dico = DictionarySQLITE(ctx, "en", "fr")
//        dico.save()
        val c = this.readableDatabase.select(DictionarySQLITE.DB_TABLE).exec {
            this.moveToFirst()
            Toast.makeText(ctx, """Dictionary=> ${this.getString(0)} ${this.getString(1)}""", Toast.LENGTH_LONG).show();
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
    }
}

// Access property for Context
val Context.database: DataBaseHelper
    get() = DataBaseHelper.getInstance(getApplicationContext())
