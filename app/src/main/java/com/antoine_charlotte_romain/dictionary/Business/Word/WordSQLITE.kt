package  com.antoine_charlotte_romain.dictionary.business.word

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.antoine_charlotte_romain.dictionary.DataModel.DataBaseHelperKot
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import java.sql.Blob
import java.sql.Date

/**
 * Created by dineen on 15/06/2016.
 */
class WordSQLITE(ctx : Context, idWord: String? = null, note : String? = null, image : Blob? = null, sound : Blob? = null, headword
: String, dateView: Date? = null, dictionary: Dictionary) : Word(idWord, note, image, sound, headword, dateView, dictionary) {

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

    val db : SQLiteDatabase = DataBaseHelperKot.getInstance(ctx).readableDatabase

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
