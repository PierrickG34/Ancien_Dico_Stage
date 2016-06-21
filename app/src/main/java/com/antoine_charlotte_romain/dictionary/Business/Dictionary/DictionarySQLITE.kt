package com.antoine_charlotte_romain.dictionary.business.dictionary

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelper
import org.jetbrains.anko.db.*

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

    fun save() : Int {
        return this.db.insert(DictionarySQLITE.DB_TABLE,
                DictionarySQLITE.DB_COLUMN_INLANG to super.inLang!!,
                DictionarySQLITE.DB_COLUMN_OUTLANG to super.outLang!!).toInt()
    }

    fun selectAll(): List<Dictionary> {
        return this.db.select(DictionarySQLITE.DB_TABLE).parseList(classParser<Dictionary>())
    }

    fun delete(id : String) : Int {
        return this.db.delete(DictionarySQLITE.DB_TABLE,"",
                DictionarySQLITE.DB_COLUMN_ID to id)
    }

    fun update(inLangNew : String, outLangNew : String) : Int {
        super.inLang = inLangNew
        super.outLang = outLangNew
        return this.db.update(DictionarySQLITE.DB_TABLE,
                DictionarySQLITE.DB_COLUMN_INLANG to super.inLang!!,
                DictionarySQLITE.DB_COLUMN_OUTLANG to super.outLang!!).exec()
    }

    fun read() {
        throw UnsupportedOperationException()
    }

}
