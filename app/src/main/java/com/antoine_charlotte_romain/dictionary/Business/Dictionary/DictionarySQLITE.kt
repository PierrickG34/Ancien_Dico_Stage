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
 * Eng : This class make the comunication with the SQLite database for the Dictionary class
 *       This class use the framework Anko
 *       The dictionary 0, contains all translations. It's only for storage.
 * Fr : Cette classe effectue la comunication avec la base de données pour la classe Dictionary
 *      Cette classe utilise le framework Anko.
 *      LE dictionnaire 0 contient toute les traductions, il ne sert donc que pour le stockage.
 * Created by dineen on 14/06/2016.
 */
class DictionarySQLITE(ctx : Context, inLang : String? = null, outLang : String? = null, id : String? = null) : Dictionary(inLang = inLang, outLang = outLang, id = id), Serializable {

    companion object {
        val DB_TABLE = "DICTIONARY"
        val DB_COLUMN_INLANG = "inLang"
        val DB_COLUMN_OUTLANG = "outLang"
        val DB_COLUMN_ID = "id"
    }

    //make db not serializable
    @Transient var db : SQLiteDatabase = DataBaseHelperKot.getInstance(ctx).readableDatabase

    /**
     * This method insert the dictionary in the database
     * @return Int : the row ID of the newly inserted row, or -1 if an error occurred
     */
    fun save() : Int {
        var log = this.db.insert(DictionarySQLITE.DB_TABLE,
                DictionarySQLITE.DB_COLUMN_INLANG to super.inLang!!,
                DictionarySQLITE.DB_COLUMN_OUTLANG to super.outLang!!).toInt()
        if (log > 0) {
            this.db.select(DictionarySQLITE.DB_TABLE,"last_insert_rowid() AS rowid").exec {
                this.moveToLast()
                super.idDictionary = this.getString(this.getColumnIndex("rowid"))
            }
        }
        return log
    }

    /**
     * Select all existing dictionaries in the database. Except the dico 0 who is for the translations.
     * @return List<Dictionary> a list who contains all dictionaries.
     */
    fun selectAll(): List<Dictionary> {
        var res : MutableList<Dictionary> = ArrayList<Dictionary>()
        val c = this.db.select(DictionarySQLITE.DB_TABLE)
                .where("""${DictionarySQLITE.DB_COLUMN_ID} != 0""")
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

    /**
     *   @return all the word of the current dictionary
     */
    fun selectAllWords(): List<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

        this.db.select("""${DictionarySQLITE.DB_TABLE}, ${WordSQLITE.DB_TABLE}""", "${WordSQLITE.DB_TABLE}.*")
                .where("""${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = ${super.idDictionary}""")
                .distinct()
                .orderBy(WordSQLITE.DB_COLUMN_HEADWORD)
                .exec {
                    while (this.moveToNext()) {
                        var sqlDate : java.sql.Date? = null
                        if (!this.isNull(this.getColumnIndex(WordSQLITE.DB_COLUMN_DATE))) {
                            var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex(WordSQLITE.DB_COLUMN_DATE)))
                            sqlDate = java.sql.Date(utilDate.getTime())
                        }
                        res.add(Word(
                                idWord = this.getString(this.getColumnIndex(WordSQLITE.DB_COLUMN_ID)),
                                note = this.getString(this.getColumnIndex(WordSQLITE.DB_COLUMN_NOTE)),
                                image = this.getBlob(this.getColumnIndex(WordSQLITE.DB_COLUMN_IMAGE)),
                                sound = this.getBlob(this.getColumnIndex(WordSQLITE.DB_COLUMN_SOUND)),
                                headword = this.getString(this.getColumnIndex(WordSQLITE.DB_COLUMN_HEADWORD)),
                                dateView = sqlDate,
                                idDictionary = this.getString(this.getColumnIndex(WordSQLITE.DB_COLUMN_ID_DICTIONARY))))
                    }
                }
        return res
    }

    /**
     * Delete the row of the dictionary with the ID in parameter
     * @param String : The id of the Dictionary to delete
     * @return the number of rows affected
     */
    fun delete(id : String) : Int {
        return this.db.delete(DictionarySQLITE.DB_TABLE,
                """${DictionarySQLITE.DB_COLUMN_ID} = ${id}""")
    }

    /**
     * @param String : The inLang
     * @param String : The outLang
     * @return true if there is a dictionary with same inLang and outLang in parameter
     */
    fun existByLang(inLang : String, outLang : String) : Boolean {
        var exist : Boolean = false
        this.db.select(DictionarySQLITE.DB_TABLE)
                .where("""(${DictionarySQLITE.DB_COLUMN_INLANG} = '${inLang}') AND (${DictionarySQLITE.DB_COLUMN_OUTLANG} = '${outLang}')""")
                .exec {
                    exist = this.count > 0
                }
        return exist
    }

    /**
     * Change the inLang and the outLang if they are not already existing.
     * @param String : The new inLang
     * @param String : The new outLang
     * @return 1 if inLang and outLang have changed, -1 if there is a dictionary who already have the same InLang and outLang
     */
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

    /**
     * get the id of the dictionary who have the same name.
     * @param int : name (Like : InLang -> outLang)
     * @return Long : the id of the dictionary who have the same name as parameter
     */
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

    /**
     * TODO il y a une petit soucis sur cette méthode ... non ? =) nom c'est un selectDictionnary pourtant y'a un delete dedans
     *
     */
    fun selectDictionary(idDictionary: String): Int {
        return this.db.delete(DictionarySQLITE.DB_TABLE,
                """${DictionarySQLITE.DB_COLUMN_ID} = ${idDictionary}""")
    }

    /**
     * Set the idDictionary, inLang  and the outLang to the supper class with the InLang and the OutLang
     */
    fun readByInLangOutLang() {
        val c = this.db.select(DictionarySQLITE.DB_TABLE)
                .where("""(${DictionarySQLITE.DB_COLUMN_INLANG} = '${inLang}') AND (${DictionarySQLITE.DB_COLUMN_OUTLANG} = '${outLang}')""")
                .exec {
                    while(this.moveToNext()) {
                        super.idDictionary = this.getString(this.getColumnIndex("id"))
                        super.inLang = this.getString(this.getColumnIndex("inLang"))
                        super.outLang = this.getString(this.getColumnIndex("outLang"))
                    }
                }
    }

    /**
     * Set the idDictionary, inLang  and the outLang to the supper class with the idDictionnary
     */
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


