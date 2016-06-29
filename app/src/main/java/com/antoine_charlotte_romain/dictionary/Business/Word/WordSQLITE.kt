package  com.antoine_charlotte_romain.dictionary.business.word

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelperKot
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import com.dicosaure.Business.Translate.TranslateSQLITE
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import java.sql.Blob
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by dineen on 15/06/2016.
 */
class WordSQLITE(ctx : Context, idWord: String? = null, note : String? = null, image : ByteArray? = null, sound : ByteArray? = null, headword: String? = null, dateView: Date? = null, idDictionary: String? = null)
: Word(idWord, note, image, sound, headword, dateView, idDictionary) {

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

    var db: SQLiteDatabase = DataBaseHelperKot.getInstance(ctx).readableDatabase

    fun save(): Int {
        return this.db.insert(WordSQLITE.DB_TABLE,
                WordSQLITE.DB_COLUMN_NOTE to super.note!!,
                WordSQLITE.DB_COLUMN_IMAGE to super.image!!,
                WordSQLITE.DB_COLUMN_SOUND to super.sound!!,
                WordSQLITE.DB_COLUMN_HEADWORD to super.headword!!,
                WordSQLITE.DB_COLUMN_DATE to super.dateView.toString()!!,
                WordSQLITE.DB_COLUMN_ID_DICTIONARY to super.idDictionary!!).toInt()
    }

    fun selectAll(): List<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .orderBy(WordSQLITE.DB_COLUMN_HEADWORD)
                .exec {
            while (this.moveToNext()) {
                var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                var sqlDate : java.sql.Date = java.sql.Date(utilDate.getTime())
                res.add(Word(idWord = this.getString(this.getColumnIndex("id")),
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

    fun selectAllTranslations() : List<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        this.db.select("""${TranslateSQLITE.DB_TABLE} as t, ${WordSQLITE.DB_TABLE} as w""", "w.*")
                .where("""t.${TranslateSQLITE.DB_COLUMN_WORDTO} = '${super.idWord}' AND t.${TranslateSQLITE.DB_COLUMN_WORDFROM} = w.${WordSQLITE.DB_COLUMN_ID}""")
                .orderBy("""w.${WordSQLITE.DB_COLUMN_HEADWORD}""")
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

    fun getAllTranslationText() : String {
        var translation = ""
        var translations = this.selectAllTranslations()
        for (word in translations) {
            translation += word.headword + " "
        }
        return translation
    }

    fun selectByDate(beforeDate: Date, afterDate: Date): List<Word> {
        return this.db.select(WordSQLITE.DB_TABLE)
                .where("(beforeDate > {dateView}) and (afterDate < {dateView})", "beforeDate" to beforeDate, "afterDate" to afterDate)
                .parseList(classParser<Word>())
    }

    fun delete(id: String): Int {
        return this.db.delete(WordSQLITE.DB_TABLE,
                """${WordSQLITE.DB_COLUMN_ID} = '${id}'""")
    }


    fun modify() {
        throw UnsupportedOperationException()
    }
}
