package com.dicosaure.Business.Word

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.dicosaure.Business.Dictionary.Dictionary
import com.dicosaure.Business.Dictionary.DictionarySQLITE
import com.dicosaure.DataModel.DataBaseHelper
import java.sql.Blob
import java.sql.Date

/**
 * Created by dineen on 15/06/2016.
 */
class WordSQLITE : Word {

    companion object {
        val DB_TABLE = "WORD"
        val DB_COLUMN_ID = "id"
        val DB_COLUMN_NOTE = "note"
        val DB_COLUMN_IMAGE = "image"
        val DB_COLUMN_SOUND = "sound"
        val DB_COLUMN_HEADWORD = "headword"
        val DB_COLUMN_DATE = "dateView"
        val DB_COLUMN_ID_DICTIONARY = "idDictionary"
    }

    val db : SQLiteDatabase

    constructor(ctx : Context, idWord: String? = null, note : String? = null, image : Blob? = null, sound : Blob? = null, headword
    : String, dateView: Date? = null, dictionary: Dictionary) : super(idWord, note, image, sound, headword, dateView, dictionary) {
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
