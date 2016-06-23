package  com.antoine_charlotte_romain.dictionary.business.word

import java.io.Serializable
import java.sql.Blob
import java.sql.Date
import java.util.*

/**
 * Created by dineen on 15/06/2016.
 */
open class Word(idWord: String? = null, note : String? = null, image : ByteArray? = null, sound : ByteArray? = null, headword
: String, dateView: Date? = null, idDictionary: String? = null) : Serializable {

    val idWord : String? = idWord
    var note : String? = note
    var image : ByteArray? = image
    var sound : ByteArray? = sound
    var headword : String = headword
    var dateView : Date? = dateView
    var idDictionary : String? = idDictionary

    override fun toString(): String{
        return "Word(idWord=$idWord, " +
                "note=$note, " +
                "image=$image, " +
                "sound=$sound, " +
                "headword='$headword', " +
                "dateView=$dateView, " +
                "idDictionary=$idDictionary)"
    }

}
