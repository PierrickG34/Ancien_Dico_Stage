package com.dicosaure.Business.Dictionary

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.dicosaure.XML.WordXML
import com.dicosaure.DataModel.DataBaseHelper
import org.jetbrains.anko.db.insert
import java.util.*

/**
 * Created by dineen on 14/06/2016.
 */
class DictionarySQLITE : Dictionary {

    companion object {
        val DB_TABLE = "DICTIONARY"
        val DB_COLUMN_INLANG = "inLang"
        val DB_COLUMN_OUTLANG = "outLang"
        val DB_COLUMN_ID = "id"
    }

    var db : SQLiteDatabase

    constructor(ctx : Context, inLang : String, outLang : String, id : String? = null) : super(inLang, outLang, id) {
        this.db = DataBaseHelper.getInstance(ctx).readableDatabase
    }

//    constructor(ctx : Context, c : Cursor) : super(c.p) {
//        super("titi", "toto")
//    }

    override fun save() {
        this.db.insert(DictionarySQLITE.DB_TABLE,
                DictionarySQLITE.DB_COLUMN_INLANG to this.inLang,
                DictionarySQLITE.DB_COLUMN_OUTLANG to this.outLang)
    }

    override fun delete() {
        throw UnsupportedOperationException()
    }

    override fun read() {
        throw UnsupportedOperationException()
    }

}
