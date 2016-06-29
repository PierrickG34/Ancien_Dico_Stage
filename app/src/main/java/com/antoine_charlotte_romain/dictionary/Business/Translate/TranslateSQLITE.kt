package com.dicosaure.Business.Translate

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelperKot
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE
import com.antoine_charlotte_romain.dictionary.business.word.Word
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by dineen on 15/06/2016.
 */
class TranslateSQLITE(ctx : Context, wordTo: Word?, wordFrom: Word?) : Translate(wordTo, wordFrom) {

    companion object {
        val DB_TABLE = "TRANSLATE"
        val DB_COLUMN_WORDTO = "wordTo"
        val DB_COLUMN_WORDFROM = "wordFrom"
    }

    val db : SQLiteDatabase = DataBaseHelperKot.getInstance(ctx).readableDatabase

    fun save() : Int {
        return this.db.insert(TranslateSQLITE.DB_TABLE,
                TranslateSQLITE.DB_COLUMN_WORDTO to super.wordTo!!.idWord!!,
                TranslateSQLITE.DB_COLUMN_WORDFROM to super.wordFrom!!.idWord!!).toInt()
    }

    fun delete() : Int {
        return this.db.delete(TranslateSQLITE.DB_TABLE,"",
                TranslateSQLITE.DB_COLUMN_WORDTO to super.wordTo!!.idWord!!,
                TranslateSQLITE.DB_COLUMN_WORDFROM to super.wordFrom!!.idWord!!)
    }


    fun update(wordToNew : Word, wordFromNew: Word) : Int {
        super.wordTo = wordToNew
        super.wordFrom = wordFromNew
        return this.db.update(TranslateSQLITE.DB_TABLE,
                TranslateSQLITE.DB_COLUMN_WORDTO to super.wordTo!!.idWord!!,
                TranslateSQLITE.DB_COLUMN_WORDFROM to super.wordFrom!!.idWord!!).exec()
    }

    fun selectAll(): List<Translate> {
        var res : MutableList<Translate> = ArrayList<Translate>()
        val c = this.db.select(TranslateSQLITE.DB_TABLE).exec {
            while(this.moveToNext()) {
                res.add(Translate(super.wordTo, super.wordFrom))
            }
        }
        return res
    }

}