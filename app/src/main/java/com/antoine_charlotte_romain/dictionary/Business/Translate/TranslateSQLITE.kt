package com.dicosaure.Business.Translate

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelperKot
import com.antoine_charlotte_romain.dictionary.Utilities.StringsUtility
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
 * Eng : This class make the comunication with the SQLite database for the Translation class.
 *       This class use the framework Anko
 * Fr : Cette classe effectue la comunication avec la base de données pour la classe Translation.
 *      Cette classe utilise le framework Anko.
 * Created by dineen on 15/06/2016.
 */
class TranslateSQLITE(ctx : Context, wordTo: Word?, wordFrom: Word?) : Translate(wordTo, wordFrom) {

    companion object {
        val DB_TABLE = "TRANSLATE"
        val DB_COLUMN_WORDTO = "wordTo"
        val DB_COLUMN_WORDFROM = "wordFrom"
    }

    val db : SQLiteDatabase = DataBaseHelperKot.getInstance(ctx).readableDatabase

    /**
     * Save the translation by inserting it in the db
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    fun save() : Int {
        return this.db.insert(TranslateSQLITE.DB_TABLE,
                TranslateSQLITE.DB_COLUMN_WORDTO to super.wordInLang!!.idWord!!,
                TranslateSQLITE.DB_COLUMN_WORDFROM to super.wordOutLang!!.idWord!!).toInt()
    }

    /**
     * TODO verifier
     * Delete the current translation
     * @return the number of rows affected, 0 otherwise.
     */
    fun delete() : Int {
        return this.db.delete(TranslateSQLITE.DB_TABLE,"",
                TranslateSQLITE.DB_COLUMN_WORDTO to super.wordInLang!!.idWord!!,
                TranslateSQLITE.DB_COLUMN_WORDFROM to super.wordOutLang!!.idWord!!)
    }

    /**
     * Update the class translate and the db
     * TODO
     */
    fun update(wordToNew : Word, wordFromNew: Word) : Int {
        super.wordInLang = wordToNew
        super.wordOutLang = wordFromNew
        return this.db.update(TranslateSQLITE.DB_TABLE,
                TranslateSQLITE.DB_COLUMN_WORDTO to super.wordInLang!!.idWord!!,
                TranslateSQLITE.DB_COLUMN_WORDFROM to super.wordOutLang!!.idWord!!).exec()
    }

    /**
     * @return List<Translate> the list of all Translate in the db
     */
    fun selectAll(): List<Translate> {
        var res : MutableList<Translate> = ArrayList<Translate>()
        val c = this.db.select(TranslateSQLITE.DB_TABLE).exec {
            while(this.moveToNext()) {
                res.add(Translate(super.wordInLang, super.wordOutLang))
            }
        }
        return res
    }

    /**
     * TODO je ne comprends pas ce que ca fait -> Fonction useless ?
     */
    fun selectAllTranslations(word : Word) {
        this.db.select(TranslateSQLITE.DB_TABLE, WordSQLITE.DB_TABLE, WordSQLITE.DB_TABLE + ".*")
                .where("""${WordSQLITE.DB_TABLE}.${WordSQLITE.DB_COLUMN_ID} = ${TranslateSQLITE.DB_TABLE}.${TranslateSQLITE.DB_COLUMN_WORDTO}""")
                .exec {
                }
    }

    /**
     *
     */
    fun selectWordToByWordFrom(idWord: String, id : Long): MutableList<String>? {
        var idWord = idWord
        var dictionaryID = id
        var res: MutableList<String>? = ArrayList<String>()
        Log.d("selectWordToByWordFrom", "$idWord")
        Log.d("WordToByWordFromIF", "$dictionaryID")
        val c = this.db.select(TranslateSQLITE.DB_TABLE).where("""(${TranslateSQLITE.DB_COLUMN_WORDFROM} = '${idWord}')""").exec {
            while (this.moveToNext()) {
                res!!.add(this.getString(this.getColumnIndex("wordTo")))
                var l = this.getString(this.getColumnIndex("wordTo"))
                Log.d("WordToByWordFrom ID1", "$l")
            }
        }
        return res
    }

}