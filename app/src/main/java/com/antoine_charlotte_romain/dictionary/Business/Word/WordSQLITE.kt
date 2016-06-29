package  com.antoine_charlotte_romain.dictionary.business.word

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelperKot
import com.antoine_charlotte_romain.dictionary.DataModel.WordDataModel
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE
import org.jetbrains.anko.db.*
import java.sql.Blob
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by dineen on 15/06/2016.
 */
class WordSQLITE(ctx : Context, idWord: String? = null, note : String? = null,
                 image : ByteArray? = null, sound : ByteArray? = null, headword: String,
                 dateView: Date? = null, idDictionary: String? = null)
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
                WordSQLITE.DB_COLUMN_HEADWORD to super.headword,
                WordSQLITE.DB_COLUMN_DATE to super.dateView.toString(),
                WordSQLITE.DB_COLUMN_ID_DICTIONARY to super.idDictionary!!).toInt()
    }

    /**
     * Select all the searchDate where the headword starts with the string in param or the date contains this string
     * @param search the string in which we are wanted to find
     * @return all the searchDate in which the headword starts with the search string or the date contains this search string
     */
    fun select(search : String) : MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE).exec {
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
        // TODO Ecrire la requete
//        private val SQL_SELECT_SEARCH_DATE_FROM_WORD_OR_DATE =
//
//                " SELECT sd." + SearchDateEntry._ID + " FROM " + SearchDateEntry.TABLE_NAME +
//                " sd INNER JOIN " + WordDataModel.WordEntry.TABLE_NAME + " w ON sd." +
//                SearchDateEntry.COLUMN_NAME_WORD_ID + "=w." + WordDataModel.WordEntry._ID +
//                " WHERE w." + WordDataModel.WordEntry.COLUMN_NAME_HEADWORD + " LIKE ?" +
//                OR sd." + SearchDateEntry.COLUMN_NAME_SEARCH_DATE + " LIKE ? ORDER BY "
//                + SearchDateEntry.COLUMN_NAME_SEARCH_DATE + " DESC;"

    }

    fun selectAll(): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE).exec {
            while (this.moveToNext()) {
                var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                var sqlDate : java.sql.Date = java.sql.Date(utilDate.time)
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

    fun selectAll(historyLimit: Int, historyOffset: Int): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .orderBy(WordSQLITE.DB_COLUMN_DATE, SqlOrderDirection.DESC)
                .limit(historyOffset,historyLimit)
                .exec {
            while (this.moveToNext()) {
                var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                var sqlDate : java.sql.Date = java.sql.Date(utilDate.time)
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

    /**
     * Select all word between two dates
     * @param beforeDate Date before
     * @param afterDate Date after
     * @return List of Word between the two dates
     */
    fun selectBetweenDate(beforeDate: Date, afterDate: Date): MutableList<Word>? {
        println(beforeDate)
        println(dateView)
        println(afterDate)
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .where("""(${WordSQLITE.DB_COLUMN_DATE} < '${beforeDate}') AND (${WordSQLITE.DB_COLUMN_DATE} > '${afterDate}')""")
                .exec {
                    while (this.moveToNext()) {
                        var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                        var sqlDate: java.sql.Date = java.sql.Date(utilDate.time)
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

    /**
     * Select all word before the date
     * @param beforeDate Date before
     * @return List of Word before the date
     */
    fun selectBeforeDate(beforeDate: Date): MutableList<Word> {
        println(beforeDate)
        println(dateView)
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .where("""(${WordSQLITE.DB_COLUMN_DATE} < '${beforeDate}')""")
                .exec {
                    while (this.moveToNext()) {
                        var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                        var sqlDate: java.sql.Date = java.sql.Date(utilDate.time)
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

    /**
     * Select all word after the date
     * @param afterDate Date after
     * @return List of Word after the date
     */
    fun selectAfterDate(afterDate: Date): MutableList<Word> {
        println(dateView)
        println(afterDate)
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .where("""(${WordSQLITE.DB_COLUMN_DATE} > '${afterDate}')""")
                .exec {
                    while (this.moveToNext()) {
                        var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                        var sqlDate: java.sql.Date = java.sql.Date(utilDate.time)
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

//    private static final String SQL_SELECT_SEARCH_DATE_BEFORE = "SELECT * FROM " + SearchDateEntry.TABLE_NAME + " WHERE " + SearchDateEntry.COLUMN_NAME_SEARCH_DATE + " < ?;";

    fun delete(id: String): Int {
        return this.db.delete(WordSQLITE.DB_TABLE, "", WordSQLITE.DB_COLUMN_ID to id)
    }


    fun update(noteNew : String? = null,
               imageNew : ByteArray? = null, soundNew : ByteArray? = null, headwordNew: String,
               dateViewNew: Date? = null, idDictionaryNew: String? = null): Int {
        super.note = noteNew
        super.image = imageNew
        super.sound = soundNew
        super.headword = headwordNew
        super.dateView = dateViewNew
        super.idDictionary = idDictionaryNew
        return this.db.update(WordSQLITE.DB_TABLE,
                    WordSQLITE.DB_COLUMN_NOTE to super.note!!,
                    WordSQLITE.DB_COLUMN_IMAGE to super.image!!,
                    WordSQLITE.DB_COLUMN_SOUND to super.sound!!,
                    WordSQLITE.DB_COLUMN_HEADWORD to super.headword!!,
                    WordSQLITE.DB_COLUMN_DATE to super.dateView!!,
                    WordSQLITE.DB_COLUMN_ID_DICTIONARY to super.idDictionary!!)
                    .where("""${WordSQLITE.DB_COLUMN_ID} = ${super.idWord}""")
                    .exec()
    }
}
