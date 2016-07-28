package ie.csis.dicosaure.DataModel

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import ie.csis.dicosaure.Controllers.R
import ie.csis.dicosaure.business.Translate.TranslateSQLITE
import ie.csis.dicosaure.business.dictionary.DictionarySQLITE
import ie.csis.dicosaure.business.word.WordSQLITE
import org.jetbrains.anko.db.ManagedSQLiteOpenHelper
import org.jetbrains.anko.db.dropTable
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by dineen on 13/06/2016.
 */

class DataBaseHelperKot(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "MyDatabase", null, 1) {
    companion object {
        private var instance: DataBaseHelperKot? = null

        @Synchronized
        fun getInstance(ctx: Context): DataBaseHelperKot {
            if (instance == null) {
                instance = DataBaseHelperKot(ctx.getApplicationContext())
            }
            return instance!!
        }
    }

    /**
     * This method opens the DB
     * @param db the database
     */
    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        db!!.execSQL("PRAGMA foreign_keys=ON");
    }

    /**
     * This method initializes the DB
     * @param db the database
     */
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
                    ${WordSQLITE.DB_COLUMN_ID_DICTIONARY} INTEGER NULL,
                    ${WordSQLITE.DB_COLUMN_IMAGE} BLOB NULL,
                    ${WordSQLITE.DB_COLUMN_SOUND} BLOB NULL,
                    CONSTRAINT pk_word PRIMARY KEY(${WordSQLITE.DB_COLUMN_ID}),
                    CONSTRAINT fk_word_dictionary FOREIGN KEY(${WordSQLITE.DB_COLUMN_ID_DICTIONARY}) REFERENCES ${DictionarySQLITE.DB_TABLE}(${DictionarySQLITE.DB_COLUMN_ID}) ON DELETE CASCADE,
                    CONSTRAINT unique_word UNIQUE(${WordSQLITE.DB_COLUMN_HEADWORD}, ${WordSQLITE.DB_COLUMN_ID_DICTIONARY})
                );
                """
        )
        db.execSQL(
                """
                CREATE TABLE ${TranslateSQLITE.DB_TABLE} (
                    ${TranslateSQLITE.DB_COLUMN_WORDTO} INTEGER NOT NULL,
                    ${TranslateSQLITE.DB_COLUMN_WORDFROM} INTEGER NOT NULL,
                    CONSTRAINT pk_translate_word PRIMARY KEY(${TranslateSQLITE.DB_COLUMN_WORDTO}, ${TranslateSQLITE.DB_COLUMN_WORDFROM}),
                    CONSTRAINT fk_translate_wordTo FOREIGN KEY(${TranslateSQLITE.DB_COLUMN_WORDTO}) REFERENCES ${WordSQLITE.DB_TABLE}(${WordSQLITE.DB_COLUMN_ID}) ON DELETE CASCADE,
                    CONSTRAINT fk_translate_wordFrom FOREIGN KEY(${TranslateSQLITE.DB_COLUMN_WORDFROM}) REFERENCES ${WordSQLITE.DB_TABLE}(${WordSQLITE.DB_COLUMN_ID}) ON DELETE CASCADE
                );
                """
        )
        db.execSQL(
                """
                INSERT INTO ${DictionarySQLITE.DB_TABLE}
                VALUES (0, 'translate', 'translate');
                """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db.dropTable(DictionarySQLITE.DB_TABLE)
        db.dropTable(WordSQLITE.DB_TABLE)
        db.dropTable(TranslateSQLITE.DB_TABLE)
        onCreate(db)
    }
}

// Access property for Context
val Context.database: DataBaseHelperKot
    get() = DataBaseHelperKot.getInstance(getApplicationContext())