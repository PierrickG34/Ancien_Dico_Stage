package com.antoine_charlotte_romain.dictionary.business.dictionary

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelperKot
import org.jetbrains.anko.db.*
import java.io.Serializable
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
        val c = this.db.select(DictionarySQLITE.DB_TABLE).exec {
            while(this.moveToNext()) {
                res.add(Dictionary(id = this.getString(this.getColumnIndex("id")),
                        inLang = this.getString(this.getColumnIndex("inLang")),
                        outLang = this.getString(this.getColumnIndex("outLang"))))
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

    fun selectDictionary(idDictionary: String): Int {
        return this.db.delete(DictionarySQLITE.DB_TABLE,
                """${DictionarySQLITE.DB_COLUMN_ID} = ${idDictionary}""")
    }


    fun read() {
        throw UnsupportedOperationException()
    }

}
