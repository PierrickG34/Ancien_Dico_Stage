package com.antoine_charlotte_romain.dictionary.DataModel

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import com.dicosaure.Business.Translate.TranslateSQLITE
import org.jetbrains.anko.db.ManagedSQLiteOpenHelper
import org.jetbrains.anko.db.dropTable
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import com.antoine_charlotte_romain.dictionary.R
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

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        db!!.execSQL("PRAGMA foreign_keys=ON");
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

    fun showTable(ctx : Context) {
        val c = this.readableDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                Toast.makeText(ctx, "Table Name=> " + c.getString(0), Toast.LENGTH_LONG).show();
                val col = this.readableDatabase.rawQuery("SELECT * FROM ${c.getString(0)}", null);
                for (table in col.columnNames) {
                    Toast.makeText(ctx, "Table Name=> " + table, Toast.LENGTH_LONG).show();
                }
                c.moveToNext();
            }
        }
    }

    fun insertTest(ctx : Context) {
        var dico = DictionarySQLITE(ctx = ctx, inLang = "eng", outLang = "fr", id = "1")
        //dico.delete("1")
        Toast.makeText(ctx, """Dictionary=> ${dico.save()}""", Toast.LENGTH_LONG).show();
        println("DataBaseHelperKot.kt -- TOUS LES DICOS - "+dico.selectAll())
    }

    fun imageTest(ctx : Context) {
        var img = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_action_create!!)
        var bos: ByteArrayOutputStream? = ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 100, bos);
        val bArray : ByteArray = bos!!.toByteArray()

        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var utilDate : java.util.Date = formatter.parse("2016-11-12")
//        var sqlDate : java.sql.Date = java.sql.Date(utilDate.getTime())
        var sqlDate : java.sql.Date = java.sql.Date(Calendar.getInstance().getTime().getTime())
        println("SQL - "+sqlDate)
        var test1: WordSQLITE? = WordSQLITE(ctx, "1", "note", bArray, bArray, "sqlDate", sqlDate, "1")
        test1!!.save()
        var test2 = WordSQLITE(ctx, "2", "note", bArray, bArray, "Au revoir", sqlDate, "0")
        test2!!.save()
//        var trad = TranslateSQLITE(ctx, test1, test2)
        var test3 = WordSQLITE(ctx, "3", "note", bArray, bArray, "hola", sqlDate, "1")
        test3!!.save()
        var test4 = WordSQLITE(ctx, "4", "note", bArray, bArray, "Bonjour", sqlDate, "0")
        test4!!.save()
        var trad2 = TranslateSQLITE(ctx, test1, test3)
        trad2.save()
//        trad.save()
//        println("getAllTranslationText :" + test1.getAllTranslationText())
        // Creation of the day's  with the good format for the database
        var formatter1 : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var utilDate1 : java.util.Date = formatter1.parse("2016-11-12")
        var sqlDate1 : java.sql.Date = java.sql.Date(Calendar.getInstance().getTime().getTime())
//        println("DataBaseHelperKot.kt -- salDate -" + sqlDate)
//
        var test: WordSQLITE? = WordSQLITE(ctx, "1premier", "premiereNote", bArray, bArray, "premierHeadzord", sqlDate1, "1")
        //test!!.save()
        //var allWord: List<Word>? = test.selectAll()
        //println("DataBaseHelperKot.kt -- allWord.size - " + allWord!!.size)
        //println("DataBaseHelperKot.kt -- allWord - " + allWord)
//        test.deleteAll()
//        println("Apres avoir deleteAll")
//        allWord = test.selectAll()
//        println("DataBaseHelperKot.kt -- allWord.size - " + allWord!!.size)
//        println("DataBaseHelperKot.kt -- allWord - " + allWord)

//        var orderBy: WordSQLITE? = WordSQLITE(ctx, "1", "note1", bArray, bArray, "headword1", sqlDate, "1")
//        orderBy!!.save()
        var historyLimit = 10
        var historyOffset = 3
//        var allWordOrderBy: List<Word>? = orderBy.selectAll(historyOffset, historyLimit)
//        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
//        var utilDate1 : java.util.Date = formatter.parse("2016-11-12")
//        var sqlDate : java.sql.Date = java.sql.Date(Calendar.getInstance().getTime().getTime())
//        println("DataBaseHelperKot.kt -- salDate -" + sqlDate)

//        var test: WordSQLITE? = WordSQLITE(ctx, "1", "note11", bArray, bArray, "headword11", sqlDate, "11")
//        test!!.save()
//        var allWord: List<Word>? = test.selectAll()
//        println("DataBaseHelperKot.kt -- allWord.size - " + allWord!!.size)
//        println("DataBaseHelperKot.kt -- allWord - " + allWord)
//
//        var orderBy: WordSQLITE? = WordSQLITE(ctx, "1", "note1", bArray, bArray, "headword1", sqlDate, "1")
//        orderBy!!.save()
//        var historyLimit = 10
//        var historyOffset = 3
//        var allWordOrderBy: List<Word>? = orderBy.selectAll(historyOffset, historyLimit)
//        println("DataBaseHelperKot.kt -- allWordOrderBy.size - " + allWordOrderBy!!.size)
//        println("DataBaseHelperKot.kt -- allWordOrderBy - " + allWordOrderBy)



        //TEST SELECT BY DATE
//        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
//        var formatDateBefore : java.util.Date = formatter.parse("2016-06-29")
//        var formatDateAfter : java.util.Date = formatter.parse("2016-07-01")
//        var dateBefore : java.sql.Date = java.sql.Date(formatDateBefore.getTime())
//        var dateAfter : java.sql.Date = java.sql.Date(formatDateAfter.getTime())
//        println("DataBaseHelperKot.kt -- dateBefore - " + dateBefore)
//        println("DataBaseHelperKot.kt -- dateAfter - " + dateAfter)
//
//        var allWordBetweenDate: List<Word>? = test.selectBetweenDate(dateBefore, dateAfter)
//        println("DataBaseHelperKot.kt -- allWordBetweenDate.size - " + allWordBetweenDate!!.size)
//        println("DataBaseHelperKot.kt -- allWordBetweenDate - " + allWordBetweenDate)
////
//        var allWordBeforeDate: List<Word>? = test.selectBeforeDate(dateAfter)
//        println("DataBaseHelperKot.kt -- allWordBeforeDate.size - " + allWordBeforeDate!!.size)
//        println("DataBaseHelperKot.kt -- allWordBeforeDate - " + allWordBeforeDate)
//
//        var allWordAfterDate: List<Word>? = test.selectAfterDate(dateBefore)
//        println("DataBaseHelperKot.kt -- allWordAfterDate.size - " + allWordAfterDate!!.size)
//        println("DataBaseHelperKot.kt -- allWordAfterDate - " + allWordAfterDate)
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