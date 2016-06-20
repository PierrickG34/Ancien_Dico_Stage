package com.dicosaure.Business.Translate

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelper
import com.antoine_charlotte_romain.dictionary.business.word.Word

/**
 * Created by dineen on 15/06/2016.
 */
class TranslateSQLITE : Translate {

    companion object {
        val DB_TABLE = "TRANSLATE"
        val DB_COLUMN_WORDTO = "wordTo"
        val DB_COLUMN_WORDFROM = "wordFrom"
    }

    val db : SQLiteDatabase

    constructor(ctx: Context, wordTo: Word, wordFrom: Word) : super(wordTo, wordFrom) {
        this.db = DataBaseHelper.getInstance(ctx).readableDatabase
    }

    override fun save() {
        throw UnsupportedOperationException()
    }

    override fun delete() {
        throw UnsupportedOperationException()
    }

    override fun modify() {
        throw UnsupportedOperationException()
    }

}