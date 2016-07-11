package  com.antoine_charlotte_romain.dictionary.business.word

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelperKot
import com.antoine_charlotte_romain.dictionary.DataModel.WordDataModel
import com.antoine_charlotte_romain.dictionary.Utilities.StringsUtility
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import com.antoine_charlotte_romain.dictionary.business.dictionary.DictionarySQLITE
import com.dicosaure.Business.Translate.TranslateSQLITE
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.*
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

    /**
     * Save the word in the database
     * @return an int which indicates if the Word had been inserted in the databases
     */

    fun save(): Int {
        var log : Int
        if (super.image == null && super.sound == null && super.dateView == null) {
            log = this.db.insert(WordSQLITE.DB_TABLE,
                    WordSQLITE.DB_COLUMN_NOTE to super.note!!,
                    WordSQLITE.DB_COLUMN_HEADWORD to super.headword!!,
                    WordSQLITE.DB_COLUMN_ID_DICTIONARY to super.idDictionary!!).toInt()
            if (log > 0) {
                this.db.select(WordSQLITE.DB_TABLE,"last_insert_rowid() AS rowid").exec {
                    this.moveToLast()
                    super.idWord = this.getString(this.getColumnIndex("rowid"))
                }
            }
            return log
        }
        else {
            log = this.db.insert(WordSQLITE.DB_TABLE,
                    WordSQLITE.DB_COLUMN_NOTE to super.note!!,
                    WordSQLITE.DB_COLUMN_IMAGE to super.image!!,
                    WordSQLITE.DB_COLUMN_SOUND to super.sound!!,
                    WordSQLITE.DB_COLUMN_HEADWORD to super.headword!!,
                    WordSQLITE.DB_COLUMN_DATE to super.dateView.toString()!!,
                    WordSQLITE.DB_COLUMN_ID_DICTIONARY to super.idDictionary!!).toInt()
            if (log > 0) {
                this.db.select(WordSQLITE.DB_TABLE,"last_insert_rowid() AS rowid").exec {
                    this.moveToLast()
                    super.idWord = this.getString(this.getColumnIndex("rowid"))
                }
            }
            return log
        }
    }

    /**
     * Select all the searchDate where the headword starts with the string in param or the date contains this string
     * @param search the string in which we are wanted to find
     * @return all the searchDate in which the headword starts with the search string or the date contains this search string
     */
    fun select(search : String) : MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .where("""(${WordSQLITE.DB_COLUMN_DATE} != 'null') AND ${WordSQLITE.DB_COLUMN_HEADWORD} LIKE "${search}%"
                    OR ${WordSQLITE.DB_COLUMN_DATE} LIKE "%${search}%" """)
                .orderBy(WordSQLITE.DB_COLUMN_DATE ,SqlOrderDirection.DESC)
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

    /**
     * Select all word and order by headword in the database where the date is not null
     * @return MutableList of Word
     */
    fun selectAll(): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .where("""${WordSQLITE.DB_COLUMN_ID_DICTIONARY} != '0'""")
                .orderBy(WordSQLITE.DB_COLUMN_HEADWORD)
                .where("""(${WordSQLITE.DB_COLUMN_DATE} != 'null')""")
                .exec {
            while (this.moveToNext()) {
                var sqlDate : java.sql.Date? = null
                if (!this.isNull(this.getColumnIndex("dateView"))) {
                    var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                    sqlDate = java.sql.Date(utilDate.getTime())
                }
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
     * Select all word with limit and offset and order by headword in the database where the date is not null
     * @return MutableList of word
     */
    fun selectAll(historyLimit: Int, historyOffset: Int): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .orderBy(WordSQLITE.DB_COLUMN_DATE, SqlOrderDirection.DESC)
                .where("""(${WordSQLITE.DB_COLUMN_DATE} != 'null')""")
                .limit(historyOffset,historyLimit)
                .exec {
            while (this.moveToNext()) {
                var sqlDate : java.sql.Date? = null
                if (!this.isNull(this.getColumnIndex("dateView"))) {
                    var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                    sqlDate = java.sql.Date(utilDate.getTime())
                }
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
     * Select all translation for a word
     * @return List of Word
     */
    fun selectAllTranslations() : List<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        this.db.select("""${TranslateSQLITE.DB_TABLE} as t, ${WordSQLITE.DB_TABLE} as w""", "w.*")
                .where("""t.${TranslateSQLITE.DB_COLUMN_WORDTO} = '${super.idWord}' AND t.${TranslateSQLITE.DB_COLUMN_WORDFROM} = w.${WordSQLITE.DB_COLUMN_ID}""")
                .orderBy("""w.${WordSQLITE.DB_COLUMN_HEADWORD}""")
                .exec {
                    while (this.moveToNext()) {
                        var sqlDate : java.sql.Date? = null
                        if (!this.isNull(this.getColumnIndex("dateView"))) {
                            var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                            sqlDate = java.sql.Date(utilDate.getTime())
                        }
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
    /**
     * Select all word between two dates
     * @param beforeDate Date before
     * @param afterDate Date after
     * @return MutableList of Word between the dates
     */
    fun selectBetweenDate(beforeDate: Date, afterDate: Date): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .where("""(${WordSQLITE.DB_COLUMN_DATE} <= '${beforeDate}') AND (${WordSQLITE.DB_COLUMN_DATE} >= '${afterDate}') AND (${WordSQLITE.DB_COLUMN_DATE} != 'null')""")
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
     * @return MutableList of Word before the date
     */
    fun selectBeforeDate(beforeDate: Date): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .where("""(${WordSQLITE.DB_COLUMN_DATE} <= '${beforeDate}') AND (${WordSQLITE.DB_COLUMN_DATE} != 'null')""")
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
     * @return MutableList of Word after the date
     */
    fun selectAfterDate(afterDate: Date): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .where("""(${WordSQLITE.DB_COLUMN_DATE} >= '${afterDate}') AND (${WordSQLITE.DB_COLUMN_DATE} != 'null')""")
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
     * Delete a word in the database in function of its id
     * @param id the string containing the id
     * @return  an int which indicates if the Word had been deleted in the database
     */


    fun delete(id: String): Int {
        return this.db.delete(WordSQLITE.DB_TABLE,
                """${WordSQLITE.DB_COLUMN_ID} = '${id}'""")
    }



    fun read() {
//        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
//        val c = this.db.select(WordSQLITE.DB_TABLE)
//                .where("""${WordSQLITE.DB_COLUMN_HEADWORD} = '${super.headword}' AND ${WordSQLITE.DB_COLUMN_NOTE} = '${super.note}' AND ${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = '${super.idDictionary}'""")
//                .exec {
//                    if(this.moveToFirst()) {
//                        var sqlDate : java.sql.Date? = null
//                        if (!this.isNull(this.getColumnIndex("dateView"))) {
//                            var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
//                            sqlDate = java.sql.Date(utilDate.getTime())
//                        }
//                        super.idWord = this.getString(this.getColumnIndex("id"))
//                        super.note = this.getString(this.getColumnIndex("note"))
//                        super.image = this.getBlob(this.getColumnIndex("image"))
//                        super.sound = this.getBlob(this.getColumnIndex("sound"))
//                        super.headword = this.getString(this.getColumnIndex("headword"))
//                        super.dateView = sqlDate
//                        super.idDictionary = this.getString(this.getColumnIndex("idDictionary"))
//                    }
//                }
    }

    fun readByHeadWord() {
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE)
                .where("""
                ${WordSQLITE.DB_COLUMN_HEADWORD} = '${super.headword}'
                AND ${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = '${super.idDictionary}'""")
                .exec {
                    if(this.moveToFirst()) {
                        var sqlDate : java.sql.Date? = null
                        if (!this.isNull(this.getColumnIndex("dateView"))) {
                            var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                            sqlDate = java.sql.Date(utilDate.getTime())
                        }
                        super.idWord = this.getString(this.getColumnIndex("id"))
                        super.note = this.getString(this.getColumnIndex("note"))
                        super.image = this.getBlob(this.getColumnIndex("image"))
                        super.sound = this.getBlob(this.getColumnIndex("sound"))
                        super.headword = this.getString(this.getColumnIndex("headword"))
                        super.dateView = sqlDate
                        super.idDictionary = this.getString(this.getColumnIndex("idDictionary"))
                    }
                }
    }


    /**
     * Update a word in the database in function of the entry parameters
     * @param noteNew the new note that will replace the old one. If it is null, nothing is done.
     * @param imageNew the new image that will replace the old one. If it is null, nothing is done.
     * @param soundNew the new sound that will replace the old one. If it is null, nothing is done.
     * @param headwordNew the new headword that will replace the old one. If it is null, nothing is done.
     * @param dateViewNew the new date (last date the word was searched) that will replace the old one. If it is null, nothing is done.
     * @param idDictionaryNew the new dictionnary id  that will replace the old one. If it is null, nothing is done.
     * @return  an int which indicates if the Word had been updated in the database
     */
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

    /**
     * Change the date attribute to 'null' for all word
     * @return The value if the request work
     */
    fun deleteAll() : Int {
        return this.db.update(WordSQLITE.DB_TABLE,
                WordSQLITE.DB_COLUMN_DATE to "null")
                .exec()
    }

    /*
     * Find a word in all the dictionaries with the beginning, the middle and the end of its headword
     * @param begin the start of the headword
     * @param middle the middle of the headword
     * @param end the end of the headword
     * @return A list of word which have this begin, this middle and this end in the headword
     */

    fun selectHeadword(begin: String, middle: String, end: String): MutableList<Word>
    {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_HEADWORD} LIKE '$begin%$middle%$end')""").exec {
                while (this.moveToNext()) {
                    var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                    var sqlDate: java.sql.Date = java.sql.Date(utilDate.getTime())
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
     * Find a word in a dictionary with the beginning, the middle and the end of its headword
     * @param begin the start of the headword
     * @param middle the middle of the headword
     * @param end the end of the headword
     * @param dictionaryID the ID of the dictionary in we wish we are searching
     * @return A list of word which have this begin, this middle and this end in the headword
     */
    fun selectHeadwordByIdDico(begin: String, middle: String, end: String, dictionaryID: Long): MutableList<Word>{
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = '${dictionaryID}') AND (${WordSQLITE.DB_COLUMN_HEADWORD} LIKE '$begin%$middle%$end')""").exec {
            while (this.moveToNext()) {
                var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                var sqlDate: java.sql.Date = java.sql.Date(utilDate.getTime())
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
     * Find a word in a dictionary with the beginning, the middle and the end of its headword, translation or note
     * @param begin the start of the headword, translation or note
     * *
     * @param middle the middle of the headword, translation or note
     * *
     * @param end the end of the headword, translation or note
     * *
     * @param dictionaryID the ID of the dictionary in we wish we are searching (set this param to Word.ALL_DICTIONARIES to look in all the dictionaries)
     * *
     * @return A list of word which have this begin, this middle and this end in the headword, translation or note
     */
    fun selectWholeWord(begin: String, middle: String, end: String, dictionaryID: Long): MutableList<Word> {

        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val search = StringsUtility.removeAccents("$begin%$middle%$end")
        val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = '${dictionaryID}') AND (${WordSQLITE.DB_COLUMN_HEADWORD} = '${search}')""").exec {
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


    /**
     * Find a word in all the dictionaries with exactly the specified headword
     * @param headWord the headword of the word we want to find
     * *
     * *
     * @return A list of word which have exactly this headword
     */
    fun selectWholeHeadword(headWord: String): MutableList<Word> {
        var headWord = headWord
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        headWord = StringsUtility.removeAccents(headWord)
        val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_HEADWORD} = '${headWord}')""").exec {
            while (this.moveToNext()) {
                var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                var sqlDate: java.sql.Date = java.sql.Date(utilDate.getTime())
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
     * Find a word in a dictionary with exactly the specified headword
     * @param headWord the headword of the word we want to find
     * *
     * @param dictionaryID the ID of the dictionary in which we are searching
     * *
     * @return A list of word which have exactly this headword in the selected dictionary
     */
    fun selectWholeHeadwordByIdDico(headWord: String, dictionaryID: Long): MutableList<Word> {
        var headWord = headWord
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        headWord = StringsUtility.removeAccents(headWord)
            val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = '${dictionaryID}') AND (${WordSQLITE.DB_COLUMN_HEADWORD} = '${headWord}')""").exec {
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

    /**
     * Find a word in a dictionary which contains exactly the noteword (part of the note)
     * @param noteword the part of the note of the word we want to find
     * @return A list of word which have exactly this part of note
     */
    fun selectWholeNote(noteword: String): MutableList<Word> {
        var noteWord = noteword
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        noteWord = StringsUtility.removeAccents(noteWord)
            val c = this.db.select(WordSQLITE.DB_TABLE).exec {
                while (this.moveToNext()) {
                    var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                    var sqlDate : java.sql.Date = java.sql.Date(utilDate.getTime())
                    var note = this.getString(this.getColumnIndex("note"))
                    if ((this.getString(this.getColumnIndex("note")).contains(noteWord, true)))
                    {
                        res.add(Word(idWord = this.getString(this.getColumnIndex("id")),
                                note = this.getString(this.getColumnIndex("note")),
                                image = this.getBlob(this.getColumnIndex("image")),
                                sound = this.getBlob(this.getColumnIndex("sound")),
                                headword = this.getString(this.getColumnIndex("headword")),
                                dateView = sqlDate,
                                idDictionary = this.getString(this.getColumnIndex("idDictionary"))))
                    }
                }
            }
        return res
    }

    /**
     * Find a word in a dictionary which contains exactly the noteword (part of the note)
     * @param noteword the part of the note of the word we want to find
     * @param dictionaryID the ID of the dictionary in which we are searching
     * @return A list of word which have exactly this part of note in the selected dictionary
     */
    fun selectWholeNoteByIdDico(noteword: String, dictionaryID: Long): MutableList<Word> {
        var noteWord = noteword
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        noteWord = StringsUtility.removeAccents(noteWord)
        val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = '${dictionaryID}')""").exec {
            while (this.moveToNext()) {
                var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                var sqlDate: java.sql.Date = java.sql.Date(utilDate.getTime())
                var note = this.getString(this.getColumnIndex("note"))
                if ((this.getString(this.getColumnIndex("note")).contains(noteWord, true))) {
                    res.add(Word(idWord = this.getString(this.getColumnIndex("id")),
                            note = this.getString(this.getColumnIndex("note")),
                            image = this.getBlob(this.getColumnIndex("image")),
                            sound = this.getBlob(this.getColumnIndex("sound")),
                            headword = this.getString(this.getColumnIndex("headword")),
                            dateView = sqlDate,
                            idDictionary = this.getString(this.getColumnIndex("idDictionary"))))
                }
            }
        }
    return res
    }

    /**
     * Find a word in all the dictionaries the input string exactly in the headword or partly in the note
     * @param stringToFind the string to find in the note or the headword
     * @return A list of word which have exactly this part of note or the headword
     */

    fun selectWholeNoteOrHeadword(stringToFind: String): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var stringToFind = StringsUtility.removeAccents(stringToFind)
            val c = this.db.select(WordSQLITE.DB_TABLE).exec {
                while (this.moveToNext()) {
                    var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                    var sqlDate : java.sql.Date = java.sql.Date(utilDate.getTime())
                    var note = this.getString(this.getColumnIndex("note"))
                    if ( (this.getString(this.getColumnIndex("note")).contains(stringToFind, true)).or((this.getString(this.getColumnIndex("headword")).equals(stringToFind)) ))
                    {
                        res.add(Word(idWord = this.getString(this.getColumnIndex("id")),
                                note = this.getString(this.getColumnIndex("note")),
                                image = this.getBlob(this.getColumnIndex("image")),
                                sound = this.getBlob(this.getColumnIndex("sound")),
                                headword = this.getString(this.getColumnIndex("headword")),
                                dateView = sqlDate,
                                idDictionary = this.getString(this.getColumnIndex("idDictionary"))))
                    }
                }
            }
        return res
    }

    /**
     * Find a word in a dictionary the input string exactly in the headword or partly in the note
     * @param stringToFind the string to find in the note or the headword
     * @param dictionaryID the ID of the dictionary in which we are searching
     * @return A list of word which have exactly this part of note or the headword in the selected dictionary
     */

    fun selectWholeNoteOrHeadwordByIdDico(stringToFind: String, dictionaryID: Long): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var stringToFind = StringsUtility.removeAccents(stringToFind)
        val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = '${dictionaryID}')""").exec {
            while (this.moveToNext()) {
                var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                var sqlDate: java.sql.Date = java.sql.Date(utilDate.getTime())
                var note = this.getString(this.getColumnIndex("note"))
                if ( (this.getString(this.getColumnIndex("note")).contains(stringToFind, true)).or((this.getString(this.getColumnIndex("headword")).equals(stringToFind)) )) {
                    res.add(Word(idWord = this.getString(this.getColumnIndex("id")),
                            note = this.getString(this.getColumnIndex("note")),
                            image = this.getBlob(this.getColumnIndex("image")),
                            sound = this.getBlob(this.getColumnIndex("sound")),
                            headword = this.getString(this.getColumnIndex("headword")),
                            dateView = sqlDate,
                            idDictionary = this.getString(this.getColumnIndex("idDictionary"))))
                }
            }
        }
    return res
    }

    /**
     * Find a word in all the dictionaries with the beginning, the middle and the end of its headword or note
     * @param begin the start of the string to find
     * @param middle the middle of the string to find
     * @param end the end of the string to find
     * @return A list of word which have this begin, this middle and this end in the headword or in the note
     */

    fun selectNoteOrHeadword(begin: String, middle: String, end: String): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_HEADWORD} LIKE '$begin%$middle%$end') OR (${WordSQLITE.DB_COLUMN_NOTE} LIKE '$begin%$middle%$end')""").exec {
                while (this.moveToNext()) {
                    var utilDate : java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                    var sqlDate : java.sql.Date = java.sql.Date(utilDate.getTime())
                    var note = this.getString(this.getColumnIndex("note"))
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
     * Find a word in a dictionary with the beginning, the middle and the end of its headword or note
     * @param begin the start of the string to find
     * @param middle the middle of the string to find
     * @param end the end of the string to find
     * @param dictionaryID the ID of the dictionary in we wish we are searching
     * @return A list of word which have this begin, this middle and this end in the headword or in the note in ther+ selected dictionary
     */
    fun selectNoteOrHeadwordByIdDico(begin: String, middle: String, end: String, dictionaryID: Long): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = '${dictionaryID}') AND ((${WordSQLITE.DB_COLUMN_HEADWORD} LIKE '$begin%$middle%$end') OR (${WordSQLITE.DB_COLUMN_NOTE} LIKE '$begin%$middle%$end' ))""").exec {
            while (this.moveToNext()) {
                var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                var sqlDate: java.sql.Date = java.sql.Date(utilDate.getTime())
                var note = this.getString(this.getColumnIndex("note"))
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
     *  Return a word in function of an id
     *  @param wordId id of the word to find
     *  @return the word which has the wordId as id
     */
    fun getWordById(wordId: String): Word
    {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_ID} = '${wordId}')""").exec {
                while (this.moveToNext()) {
                    var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                    var sqlDate: java.sql.Date = java.sql.Date(utilDate.getTime())
                    res.add(Word(idWord = this.getString(this.getColumnIndex("id")),
                            note = this.getString(this.getColumnIndex("note")),
                            image = this.getBlob(this.getColumnIndex("image")),
                            sound = this.getBlob(this.getColumnIndex("sound")),
                            headword = this.getString(this.getColumnIndex("headword")),
                            dateView = sqlDate,
                            idDictionary = this.getString(this.getColumnIndex("idDictionary"))))
                }
            }
        return res.component1()
    }

    /**
     *  Find a word in function of an id in a specific dictionary
     *  @param wordId id of the word to find
     *  @param dictionaryID id of the dictionary in which we want to find the word
     *  @return the word which has the wordId as id and a specific dictionary
     */
    fun getWordByIdByIdDico(wordId: String, dictionaryID: Long): Word{
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = '${dictionaryID}') AND (${WordSQLITE.DB_COLUMN_ID} = '${wordId}')""").exec {
            while (this.moveToNext()) {
                var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                var sqlDate: java.sql.Date = java.sql.Date(utilDate.getTime())
                res.add(Word(idWord = this.getString(this.getColumnIndex("id")),
                        note = this.getString(this.getColumnIndex("note")),
                        image = this.getBlob(this.getColumnIndex("image")),
                        sound = this.getBlob(this.getColumnIndex("sound")),
                        headword = this.getString(this.getColumnIndex("headword")),
                        dateView = sqlDate,
                        idDictionary = this.getString(this.getColumnIndex("idDictionary"))))
            }
        }
    return res.component1()
    }

    /**
     * Find a word in all the dictionaries with the beginning, the middle and the end of its note
     * @param begin the start of the note
     * @param middle the middle of the note
     * @param end the end of the note
     * @return A list of word which have this begin, this middle and this end in the note
     */

    fun selectNote(begin: String, middle: String, end: String): MutableList<Word> {

        var res: MutableList<Word> = ArrayList<Word>()
        var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_NOTE} LIKE '$begin%$middle%$end')""").exec {
                while (this.moveToNext()) {
                    var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                    var sqlDate: java.sql.Date = java.sql.Date(utilDate.getTime())
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
     * Find a word in a dictionary with the beginning, the middle and the end of its note
     * @param begin the start of the note
     * @param middle the middle of the note
     * @param end the end of the note
     * @param dictionaryID the ID of the dictionary in we wish we are searching
     * @return A list of word which have this begin, this middle and this end in the note in a specific dictionary
     */

    fun selectNoteByIdDico(begin: String, middle: String, end: String, dictionaryID: Long): MutableList<Word> {
        var res: MutableList<Word> = ArrayList<Word>()
        var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val c = this.db.select(WordSQLITE.DB_TABLE).where("""(${WordSQLITE.DB_COLUMN_ID_DICTIONARY} = '${dictionaryID}') AND (${WordSQLITE.DB_COLUMN_NOTE} LIKE '$begin%$middle%$end')""").exec {
            while (this.moveToNext()) {
                var utilDate: java.util.Date = formatter.parse(this.getString(this.getColumnIndex("dateView")))
                var sqlDate: java.sql.Date = java.sql.Date(utilDate.getTime())
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
}
