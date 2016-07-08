package com.antoine_charlotte_romain.dictionary.business.dictionary

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelperKot

import com.antoine_charlotte_romain.dictionary.Utilities.StringsUtility

import com.antoine_charlotte_romain.dictionary.business.word.Word
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE

import org.jetbrains.anko.db.*
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by dineen on 14/06/2016.
 */
class DictionarySQLITE(ctx : Context, inLang : String? = null, outLang : String? = null, id : String? = null) : Dictionary(inLang = inLang, outLang = outLang, id = id), Serializable {

    companion object {
        val DB_TABLE = "DICTIONARY"
        val DB_COLUMN_INLANG = "inLang"
        val DB_COLUMN_OUTLANG = "outLang"
        val DB_COLUMN_ID = "id"
    }

    @Transient var db : SQLiteDatabase = DataBaseHelperKot.getInstance(ctx).readableDatabase

    fun save() : Int {
        var log = this.db.insert(DictionarySQLITE.DB_TABLE,
            DictionarySQLITE.DB_COLUMN_INLANG to super.inLang!!,
            DictionarySQLITE.DB_COLUMN_OUTLANG to super.outLang!!).toInt()
        if (log > 0) {
            this.db.select(DictionarySQLITE.DB_TABLE,"last_insert_rowid() AS rowid").exec {
                this.moveToLast()
                println(this.getString(this.getColumnIndex("rowid")))
                super.idDictionary = this.getString(this.getColumnIndex("rowid"))
            }
        }
        return log
    }

    fun selectAll(): List<Dictionary> {
        var res : MutableList<Dictionary> = ArrayList<Dictionary>()
        val c = this.db.select(DictionarySQLITE.DB_TABLE)
                .orderBy(DictionarySQLITE.DB_COLUMN_INLANG)
                .exec {
            while(this.moveToNext()) {
                res.add(Dictionary(id = this.getString(this.getColumnIndex("id")),
                        inLang = this.getString(this.getColumnIndex("inLang")),
                        outLang = this.getString(this.getColumnIndex("outLang"))))
            }
        }
        return res
    }

    fun selectAllWords(): List<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        this.db.select("""${DictionarySQLITE.DB_TABLE}, ${WordSQLITE.DB_TABLE}""", "${WordSQLITE.DB_TABLE}.*")
                .where("""${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = ${super.idDictionary}""")
                .distinct()
                .orderBy(WordSQLITE.DB_COLUMN_HEADWORD)
                .exec {
                    while (this.moveToNext()) {
                        var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                        var sqlDate : java.sql.Date = java.sql.Date(utilDate.getTime())
                        res.add(Word(
                                idWord = this.getString(this.getColumnIndex("id")),
                                note = this.getString(this.getColumnIndex("note")),
                                image = this.getBlob(this.getColumnIndex("image")),
                                sound = this.getBlob(this.getColumnIndex("sound")),
                                headword = this.getString(this.getColumnIndex("headword")),
                                dateView = sqlDate,
                                idDictionary = this.getString(this.getColumnIndex("idDictionary"))))
                    }
                }
        return res
    }

    fun delete(id : String) : Int {
        return this.db.delete(DictionarySQLITE.DB_TABLE,
                """${DictionarySQLITE.DB_COLUMN_ID} = ${id}""")
    }

    fun existByLang(inLang : String, outLang : String) : Boolean {
        var exist : Boolean = false
        this.db.select(DictionarySQLITE.DB_TABLE)
                .where("""(${DictionarySQLITE.DB_COLUMN_INLANG} = '${inLang}') AND (${DictionarySQLITE.DB_COLUMN_OUTLANG} = '${outLang}')""")
                .exec {
                    exist = this.count > 0
                }
        return exist
    }

    fun update(inLangNew : String, outLangNew : String) : Int {
        super.inLang = inLangNew
        super.outLang = outLangNew
        if (this.existByLang(inLangNew, outLangNew)) {
            return - 1
        }
        else {
            return this.db.update(DictionarySQLITE.DB_TABLE,
                    DictionarySQLITE.DB_COLUMN_INLANG to super.inLang!!,
                    DictionarySQLITE.DB_COLUMN_OUTLANG to super.outLang!!)
                    .where("""${DictionarySQLITE.DB_COLUMN_ID} = ${super.idDictionary}""")
                    .exec()
        }
    }


    fun getIdByName(name : String) : Long
    {
        var id : Long? = null
        val c = this.db.select(DictionarySQLITE.DB_TABLE).exec {
            var inlang: String
            var outlang: String
                while (this.moveToNext()) {
                    inlang = this.getString(this.getColumnIndex("inLang"))
                    outlang = this.getString(this.getColumnIndex("outLang"))
                    if ("""${inlang} -> ${outlang}""" == name) {
                        id = (this.getString(this.getColumnIndex("id")).toLong())
                        Log.d("mytag2", "$id")
                    }
                }
            }
        return id!!
    }


    fun selectDictionary(idDictionary: String): Int {
        return this.db.delete(DictionarySQLITE.DB_TABLE,
                """${DictionarySQLITE.DB_COLUMN_ID} = ${idDictionary}""")
    }



    fun read() {
        val c = this.db.select(DictionarySQLITE.DB_TABLE)
                .where("""${DictionarySQLITE.DB_COLUMN_ID} = ${super.idDictionary}""")
                .exec {
                    while(this.moveToNext()) {
                        super.idDictionary = this.getString(this.getColumnIndex("id"))
                        super.inLang = this.getString(this.getColumnIndex("inLang"))
                        super.outLang = this.getString(this.getColumnIndex("outLang"))
                    }
        }
    }


}
