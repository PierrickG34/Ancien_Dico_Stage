package com.antoine_charlotte_romain.dictionary.business.dictionary

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelper
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select

/**
 * Created by dineen on 14/06/2016.
 */
class DictionarySQLITE(ctx : Context, inLang : String? = null, outLang : String? = null, id : String? = null) : Dictionary(inLang, outLang, id) {

    companion object {
        val DB_TABLE = "DICTIONARY"
        val DB_COLUMN_INLANG = "inLang"
        val DB_COLUMN_OUTLANG = "outLang"
        val DB_COLUMN_ID = "id"
    }

    var db : SQLiteDatabase = DataBaseHelper.getInstance(ctx).readableDatabase

    fun save() {
        this.db.insert(DictionarySQLITE.DB_TABLE,
                DictionarySQLITE.DB_COLUMN_INLANG to super.inLang!!,
                DictionarySQLITE.DB_COLUMN_OUTLANG to super.outLang!!)
    }

    fun selectAll(): List<Dictionary> {
        return this.db.select(DictionarySQLITE.DB_TABLE).parseList(classParser<com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary>())
    }

    fun delete() {
        throw UnsupportedOperationException()
    }

    fun read() {
        throw UnsupportedOperationException()
    }

}
